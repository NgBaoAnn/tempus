package com.projectapp.tempus.data.social.repository

import android.util.Log
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.social.dto.*
import com.projectapp.tempus.domain.social.model.ConversationWithUser
import com.projectapp.tempus.domain.social.model.UserBasicInfo
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.*
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant


class SupabaseMessageRepository : MessageRepository {
    
    private val supabase = SupabaseClientProvider.client
    private var currentChannel: RealtimeChannel? = null
    
    companion object {
        private const val TAG = "SupabaseMessageRepo"
    }
    
    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")
    }
    
    
    override suspend fun getConversations(): Result<List<ConversationWithUser>> = runCatching {
        val currentUserId = getCurrentUserId()
        
        
        val blockedUserIds = getBlockedUserIds()
        
        
        val conversations = supabase.from("conversations")
            .select {
                filter {
                    or {
                        eq("participant1_id", currentUserId)
                        eq("participant2_id", currentUserId)
                    }
                }
                order("last_message_at", Order.DESCENDING)
            }
            .decodeList<ConversationDto>()
        
        
        val result = conversations.mapNotNull { conv ->
            val otherUserId = if (conv.participant1Id == currentUserId) {
                conv.participant2Id
            } else {
                conv.participant1Id
            }
            
            
            if (otherUserId in blockedUserIds) {
                return@mapNotNull null
            }
            
            
            val userInfo = fetchUserBasicInfo(otherUserId)
            if (userInfo != null) {
                ConversationWithUser(
                    conversation = conv,
                    otherUser = userInfo
                )
            } else {
                null
            }
        }
        
        result
    }
    
    override suspend fun getOrCreateConversation(otherUserId: String): Result<ConversationDto> = runCatching {
        val currentUserId = getCurrentUserId()
        
        
        val (p1, p2) = if (currentUserId < otherUserId) {
            currentUserId to otherUserId
        } else {
            otherUserId to currentUserId
        }
        
        
        val existing = supabase.from("conversations")
            .select {
                filter {
                    eq("participant1_id", p1)
                    eq("participant2_id", p2)
                }
            }
            .decodeSingleOrNull<ConversationDto>()
        
        if (existing != null) {
            return@runCatching existing
        }
        
        
        val newConversation = CreateConversationDto(
            participant1Id = p1,
            participant2Id = p2
        )
        
        supabase.from("conversations")
            .insert(newConversation) {
                select()
            }
            .decodeSingle<ConversationDto>()
    }
    
    
    override suspend fun getMessages(conversationId: String): Result<List<MessageDto>> = runCatching {
        supabase.from("messages")
            .select {
                filter {
                    eq("conversation_id", conversationId)
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<MessageDto>()
    }
    
    override suspend fun sendMessage(conversationId: String, content: String): Result<MessageDto> = runCatching {
        val currentUserId = getCurrentUserId()
        val now = Instant.now().toString()
        
        
        val createMessage = CreateMessageDto(
            conversationId = conversationId,
            senderId = currentUserId,
            content = content
        )
        
        val message = supabase.from("messages")
            .insert(createMessage) {
                select()
            }
            .decodeSingle<MessageDto>()
        
        
        val updateConversation = UpdateConversationDto(
            lastMessageAt = now,
            lastMessagePreview = content.take(100)
        )
        
        supabase.from("conversations")
            .update(updateConversation) {
                filter {
                    eq("id", conversationId)
                }
            }
        
        message
    }
    
    
    override fun subscribeToMessages(conversationId: String): Flow<MessageDto> = callbackFlow {
        try {
            
            currentChannel?.let {
                supabase.realtime.removeChannel(it)
            }
            
            
            val channel = supabase.realtime.channel("messages:$conversationId")
            
            
            val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "messages"
                filter = "conversation_id=eq.$conversationId"
            }
            
            
            channel.subscribe(blockUntilSubscribed = true)
            currentChannel = channel
            
            Log.d(TAG, "Subscribed to realtime for conversation: $conversationId")
            
            
            val job = launch {
                changeFlow.collect { change ->
                    try {
                        val record = change.record
                        val message = MessageDto(
                            id = record["id"]?.toString()?.removeSurrounding("\"") ?: "",
                            conversationId = record["conversation_id"]?.toString()?.removeSurrounding("\"") ?: "",
                            senderId = record["sender_id"]?.toString()?.removeSurrounding("\"") ?: "",
                            content = record["content"]?.toString()?.removeSurrounding("\"") ?: "",
                            messageType = record["message_type"]?.toString()?.removeSurrounding("\"") ?: "text",
                            isRead = record["is_read"]?.toString() == "true",
                            createdAt = record["created_at"]?.toString()?.removeSurrounding("\"") ?: ""
                        )
                        
                        Log.d(TAG, "Realtime message received: ${message.content}")
                        trySend(message)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing realtime message", e)
                    }
                }
            }
            
            awaitClose {
                job.cancel()
                Log.d(TAG, "Closing realtime subscription")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up realtime subscription", e)
            close(e)
        }
    }
    
    override suspend fun unsubscribeFromMessages() {
        currentChannel?.let {
            try {
                supabase.realtime.removeChannel(it)
                currentChannel = null
                Log.d(TAG, "Unsubscribed from realtime")
            } catch (e: Exception) {
                Log.e(TAG, "Error unsubscribing from realtime", e)
            }
        }
    }
    
    
    override suspend fun sendImageMessage(
        conversationId: String, 
        imageBytes: ByteArray
    ): Result<MessageDto> = runCatching {
        val currentUserId = getCurrentUserId()
        val now = Instant.now().toString()
        
        
        val fileName = "$conversationId/${currentUserId}_${System.currentTimeMillis()}.jpg"
        
        
        val bucket = supabase.storage.from("chat-images")
        bucket.upload(fileName, imageBytes)
        
        
        val imageUrl = bucket.publicUrl(fileName)
        
        Log.d(TAG, "Image uploaded: $imageUrl")
        
        
        val createMessage = CreateMessageDto(
            conversationId = conversationId,
            senderId = currentUserId,
            content = imageUrl,
            messageType = "image"
        )
        
        val message = supabase.from("messages")
            .insert(createMessage) {
                select()
            }
            .decodeSingle<MessageDto>()
        
        
        val updateConversation = UpdateConversationDto(
            lastMessageAt = now,
            lastMessagePreview = "📷 Hình ảnh"
        )
        
        supabase.from("conversations")
            .update(updateConversation) {
                filter {
                    eq("id", conversationId)
                }
            }
        
        message
    }
    
    
    private suspend fun fetchUserBasicInfo(userId: String): UserBasicInfo? {
        return try {
            val user = supabase.from("users")
                .select(columns = Columns.list("id", "username", "avatar")) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<UserDto>()
            
            user?.let {
                UserBasicInfo(
                    id = it.id,
                    username = it.username,
                    avatar = it.avatar
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user info for $userId", e)
            null
        }
    }
    
    private suspend fun getBlockedUserIds(): Set<String> {
        return try {
            val currentUserId = getCurrentUserId()
            
            
            val iBlocked = supabase.from("blocked_users")
                .select(columns = Columns.list("blocked_id")) {
                    filter {
                        eq("blocker_id", currentUserId)
                    }
                }
                .decodeList<BlockedIdOnly>()
                .map { it.blockedId }
            
            
            val blockedMe = supabase.from("blocked_users")
                .select(columns = Columns.list("blocker_id")) {
                    filter {
                        eq("blocked_id", currentUserId)
                    }
                }
                .decodeList<BlockerIdOnly>()
                .map { it.blockerId }
            
            (iBlocked + blockedMe).toSet()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching blocked users", e)
            emptySet()
        }
    }
}


@kotlinx.serialization.Serializable
private data class BlockedIdOnly(
    @kotlinx.serialization.SerialName("blocked_id")
    val blockedId: String
)

@kotlinx.serialization.Serializable
private data class BlockerIdOnly(
    @kotlinx.serialization.SerialName("blocker_id")
    val blockerId: String
)


@kotlinx.serialization.Serializable
private data class UserDto(
    val id: String,
    val username: String,
    val avatar: String? = null
)

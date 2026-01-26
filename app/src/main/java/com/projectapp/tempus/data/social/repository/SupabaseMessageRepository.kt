package com.projectapp.tempus.data.social.repository

import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.social.dto.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import java.time.Instant

/**
 * Implementation của MessageRepository sử dụng Supabase
 */
class SupabaseMessageRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : MessageRepository {

    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")
    }

    override suspend fun getConversations(): Result<List<ConversationDto>> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            supabase.from("conversations")
                .select(Columns.raw("id, participant1_id, participant2_id, last_message_at, last_message_preview, created_at")) {
                    filter {
                        or {
                            eq("participant1_id", currentUserId)
                            eq("participant2_id", currentUserId)
                        }
                    }
                    order("last_message_at", Order.DESCENDING)
                }
                .decodeList<ConversationDto>()
        }
    }

    override suspend fun getMessages(conversationId: String): Result<List<MessageDto>> {
        return runCatching {
            supabase.from("messages")
                .select(Columns.raw("id, conversation_id, sender_id, content, message_type, is_read, created_at")) {
                    filter {
                        eq("conversation_id", conversationId)
                    }
                    order("created_at", Order.ASCENDING)
                    limit(100)
                }
                .decodeList<MessageDto>()
        }
    }

    override suspend fun sendMessage(conversationId: String, content: String): Result<MessageDto> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            val now = Instant.now().toString()
            
            // 1. Insert message
            val messageDto = CreateMessageDto(
                conversationId = conversationId,
                senderId = currentUserId,
                content = content
            )
            
            val result = supabase.from("messages")
                .insert(messageDto) {
                    select(Columns.raw("id, conversation_id, sender_id, content, message_type, is_read, created_at"))
                }
                .decodeSingle<MessageDto>()
            
            // 2. Update conversation with last message info
            val updateDto = UpdateConversationDto(
                lastMessageAt = now,
                lastMessagePreview = content.take(100)
            )
            
            supabase.from("conversations")
                .update(updateDto) {
                    filter {
                        eq("id", conversationId)
                    }
                }
            
            result
        }
    }

    override suspend fun getOrCreateConversation(otherUserId: String): Result<ConversationDto> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            // Check if conversation exists (in either direction)
            val existing = supabase.from("conversations")
                .select(Columns.raw("id, participant1_id, participant2_id, last_message_at, last_message_preview, created_at")) {
                    filter {
                        or {
                            and {
                                eq("participant1_id", currentUserId)
                                eq("participant2_id", otherUserId)
                            }
                            and {
                                eq("participant1_id", otherUserId)
                                eq("participant2_id", currentUserId)
                            }
                        }
                    }
                    limit(1)
                }
                .decodeList<ConversationDto>()
            
            if (existing.isNotEmpty()) {
                return@runCatching existing.first()
            }
            
            // Create new conversation
            val createDto = CreateConversationDto(
                participant1Id = currentUserId,
                participant2Id = otherUserId
            )
            
            supabase.from("conversations")
                .insert(createDto) {
                    select(Columns.raw("id, participant1_id, participant2_id, last_message_at, last_message_preview, created_at"))
                }
                .decodeSingle<ConversationDto>()
        }
    }

    override suspend fun markMessagesAsRead(conversationId: String): Result<Unit> {
        return runCatching {
            val currentUserId = getCurrentUserId()
            
            supabase.from("messages")
                .update(mapOf("is_read" to true)) {
                    filter {
                        eq("conversation_id", conversationId)
                        neq("sender_id", currentUserId)
                        eq("is_read", false)
                    }
                }
        }
    }

    override fun getMessagesFlow(conversationId: String): kotlinx.coroutines.flow.Flow<List<MessageDto>> = kotlinx.coroutines.flow.flow {
        // 1. Emit initial data
        val initialData = getMessages(conversationId).getOrDefault(emptyList())
        emit(initialData)

        // 2. Setup Realtime
        try {
            val channel = supabase.channel("messages_$conversationId")
            
            val messageFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "messages"
                filter = "conversation_id=eq.$conversationId"
            }
            
            channel.subscribe()
            
            // 3. Listen to new messages
            messageFlow.collect { action ->
                // Re-fetch to get updated list
                val updatedList = getMessages(conversationId).getOrDefault(emptyList())
                emit(updatedList)
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "Realtime error: ${e.message}")
        }
    }
}

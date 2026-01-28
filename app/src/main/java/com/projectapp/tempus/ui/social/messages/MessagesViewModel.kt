package com.projectapp.tempus.ui.social.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.social.dto.ConversationDto
import com.projectapp.tempus.data.social.dto.MessageDto
import com.projectapp.tempus.data.social.dto.UserBasicDto
import com.projectapp.tempus.data.social.repository.MessageRepository
import com.projectapp.tempus.data.social.repository.SupabaseMessageRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State cho Messages
 */
data class MessagesUiState(
    val conversations: List<ConversationWithUser> = emptyList(),
    val currentMessages: List<MessageDto> = emptyList(),
    val currentConversation: ConversationDto? = null,
    val currentChatPartner: UserBasicDto? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

/**
 * Conversation với thông tin user
 */
data class ConversationWithUser(
    val conversation: ConversationDto,
    val otherUser: UserBasicDto,
    val unreadCount: Int = 0
)

/**
 * ViewModel cho Messages feature
 */
class MessagesViewModel(
    private val messageRepository: MessageRepository = SupabaseMessageRepository(),
    private val friendRepository: com.projectapp.tempus.data.social.repository.FriendRepository = com.projectapp.tempus.data.social.repository.SupabaseFriendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private val supabase = SupabaseClientProvider.client

    init {
        loadConversations()
        // Start listening for notifications
        listenForNewMessages()
    }

    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id ?: ""
    }

    /**
     * Load danh sách conversations
     * Merge với danh sách bạn bè để hiển thị cả những người chưa chat
     */
    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val currentUserId = getCurrentUserId()
            
            // 0. Get Blocked User IDs first (both directions)
            val blockedUserIds = friendRepository.getAllBlockedUserIds()
                .getOrDefault(emptyList())
                .toSet()
            
            // 1. Get Active Conversations
            val conversationsResult = messageRepository.getConversations()
            val activeConversations = conversationsResult.getOrDefault(emptyList())
            
            // 2. Get All Friends
            val friendsResult = friendRepository.getFriends()
            val friends = friendsResult.getOrDefault(emptyList())
            
            // 3. Process Active Conversations (filter out blocked users)
            val loadedConversations = activeConversations.mapNotNull { conv ->
                val otherUserId = if (conv.participant1Id == currentUserId) {
                    conv.participant2Id
                } else {
                    conv.participant1Id
                }
                
                // Skip if blocked
                if (otherUserId in blockedUserIds) {
                    return@mapNotNull null
                }
                
                val user = loadUserInfo(otherUserId)
                user?.let { ConversationWithUser(conv, it) }
            }.toMutableList()
            
            // 4. Add Empty Conversations for Friends who are not in list (and not blocked)
            friends.forEach { friend ->
                val friendId = friend.friendId
                
                // Skip if blocked
                if (friendId in blockedUserIds) {
                    return@forEach
                }
                
                val isAlreadyInList = loadedConversations.any { it.otherUser.id == friendId }
                
                if (!isAlreadyInList) {
                    // Create a placeholder conversation
                    val placeholderConv = ConversationDto(
                        id = "temporary_$friendId",
                        participant1Id = currentUserId,
                        participant2Id = friendId,
                        lastMessageAt = null,
                        lastMessagePreview = null,
                        createdAt = java.time.Instant.now().toString()
                    )
                    
                    val friendUser = UserBasicDto(
                        id = friendId,
                        username = friend.friendUsername,
                        avatar = friend.friendAvatar,
                        email = friend.friendEmail
                    )
                    
                    loadedConversations.add(
                        ConversationWithUser(
                            conversation = placeholderConv,
                            otherUser = friendUser,
                            unreadCount = 0
                        )
                    )
                }
            }
            
            // Sort by last message time desc (nulls last)
            loadedConversations.sortWith(compareByDescending<ConversationWithUser> { 
                it.conversation.lastMessageAt ?: "" 
            })
            
            _uiState.update { 
                it.copy(
                    conversations = loadedConversations,
                    isLoading = false
                ) 
            }
            
            if (conversationsResult.isFailure && friendsResult.isFailure) {
                 _uiState.update { it.copy(error = "Không thể tải dữ liệu") }
            }
        }
    }

    /**
     * Mở chat với bạn bè
     */
    /**
     * Mở chat với bạn bè
     */
    fun openChat(friendId: String, friendUsername: String, friendAvatar: String?) {
        viewModelScope.launch {
            // Optimistically set the chat partner so we have context even if conversation fails to load initially
            val partner = UserBasicDto(
                id = friendId,
                username = friendUsername,
                avatar = friendAvatar
            )
            
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    error = null,
                    currentChatPartner = partner, // Set immediately
                    currentConversation = null // Clear old one
                ) 
            }
            
            messageRepository.getOrCreateConversation(friendId)
                .onSuccess { conversation ->
                    _uiState.update { 
                        it.copy(
                            currentConversation = conversation,
                            isLoading = false
                        ) 
                    }
                    
                    // Load messages
                    loadMessages(conversation.id)
                }
                .onFailure { e ->
                    // Even if loading fails, we still have the partner info allowing user to TRY sending
                    _uiState.update { 
                        it.copy(
                            // Keep error internal mostly, or show small toast
                            // error = "Không thể tải cuộc trò chuyện", 
                            isLoading = false
                        ) 
                    }
                }
        }
    }

    /**
     * Load messages của conversation hiện tại (Realtime)
     */
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            messageRepository.getMessagesFlow(conversationId)
                .collect { messages ->
                    _uiState.update { it.copy(currentMessages = messages) }
                    
                    // Mark messages as read
                    // Check if last message is from other user and unread
                    val lastMessage = messages.lastOrNull()
                    if (lastMessage != null && lastMessage.senderId != getCurrentUserId() && !lastMessage.isRead) {
                        messageRepository.markMessagesAsRead(conversationId)
                    }
                    
                    // Simple notification trigger if we are in background logic (simulated)
                    // Note: This only works if VM is alive. For true background, need Service/Worker.
                    if (messages.isNotEmpty()) {
                        val limit = 3
                        // Logic to detect NEW message: compare size or ID
                        // Simplified: just update UI
                    }
                }
        }
    }

    /**
     * Gửi tin nhắn
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            
            val currentUserId = getCurrentUserId()
            
            // Get partner ID from currentChatPartner OR from conversation participants
            var partnerId = _uiState.value.currentChatPartner?.id
            if (partnerId == null) {
                // Try to get from conversation
                val conv = _uiState.value.currentConversation
                if (conv != null) {
                    partnerId = if (conv.participant1Id == currentUserId) {
                        conv.participant2Id
                    } else {
                        conv.participant1Id
                    }
                }
            }
            
            // Check if user is blocked before sending
            if (partnerId != null) {
                val isBlockedResult = friendRepository.isUserBlocked(partnerId)
                if (isBlockedResult.getOrDefault(false)) {
                    _uiState.update { 
                        it.copy(
                            isSending = false, 
                            error = "Người này đã bị chặn. Không thể gửi tin nhắn."
                        ) 
                    }
                    return@launch
                }
            }
            
            var conversationId = _uiState.value.currentConversation?.id
            
            // If we don't have a conversation ID yet (e.g. initial load failed/race cond), try to get it now
            if (conversationId == null) {
                if (partnerId != null) {
                    val result = messageRepository.getOrCreateConversation(partnerId)
                    result.onSuccess { conv ->
                        conversationId = conv.id
                        _uiState.update { it.copy(currentConversation = conv) }
                        // Start listening to this new conversation
                        loadMessages(conv.id)
                    }.onFailure { ex ->
                        _uiState.update { it.copy(isSending = false, error = "Lỗi kết nối: ${ex.message}") }
                        return@launch
                    }
                } else {
                    _uiState.update { it.copy(isSending = false, error = "Không xác định người nhận") }
                    return@launch
                }
            }
            
            // Now proceed with sending if we have an ID
            if (conversationId != null) {
                messageRepository.sendMessage(conversationId!!, content.trim())
                    .onSuccess { message ->
                        _uiState.update { state ->
                            state.copy(
                                currentMessages = state.currentMessages + message,
                                isSending = false
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { 
                            it.copy(
                                error = "Không thể gửi tin nhắn: ${e.message}",
                                isSending = false
                            ) 
                        }
                    }
            }
        }
    }

    /**
     * Đóng chat hiện tại
     */
    fun closeChat() {
        _uiState.update { 
            it.copy(
                currentConversation = null,
                currentChatPartner = null,
                currentMessages = emptyList()
            ) 
        }
    }

    /**
     * Xóa error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Load thông tin user từ database
     */
    // Notification State
    private val _notificationEvents = kotlinx.coroutines.flow.MutableSharedFlow<NotificationEvent>()
    val notificationEvents = _notificationEvents.asSharedFlow()

    data class NotificationEvent(
        val conversationId: String,
        val senderName: String, 
        val messageContent: String
    )

    /**
     * Listen for ANY new message addressed to current user to show notification
     */
    private fun listenForNewMessages() {
        viewModelScope.launch {
            try {
                val currentUserId = getCurrentUserId()
                if (currentUserId.isBlank()) return@launch

                val channel = supabase.channel("user_messages_$currentUserId")
                
                val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "messages"
                }
                
                channel.subscribe()
                
                flow.collect { action ->
                    // Get the new record from the action's record field
                    val record = action.record
                    val senderId = record["sender_id"] as? String ?: return@collect
                    val conversationId = record["conversation_id"] as? String ?: return@collect
                    val content = record["content"] as? String ?: return@collect
                    
                    if (senderId != currentUserId) {
                        // Check if we are NOT currently chatting with this user
                        val currentConvId = _uiState.value.currentConversation?.id
                        if (currentConvId != conversationId) {
                            // Fetch sender info
                             val sender = loadUserInfo(senderId)
                             sender?.let {
                                 _notificationEvents.emit(
                                     NotificationEvent(
                                         conversationId = conversationId,
                                         senderName = it.username,
                                         messageContent = content
                                     )
                                 )
                             }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MessagesVM", "Realtime error: ${e.message}")
            }
        }
    }
    private suspend fun loadUserInfo(userId: String): UserBasicDto? {
        return try {
            supabase.from("users")
                .select(Columns.raw("id, username, avatar, email")) {
                    filter {
                        eq("id", userId)
                    }
                    limit(1)
                }
                .decodeList<UserBasicDto>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}

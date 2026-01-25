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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val messageRepository: MessageRepository = SupabaseMessageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private val supabase = SupabaseClientProvider.client

    init {
        loadConversations()
    }

    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id ?: ""
    }

    /**
     * Load danh sách conversations
     */
    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            messageRepository.getConversations()
                .onSuccess { conversations ->
                    // Load user info cho mỗi conversation
                    val conversationsWithUsers = conversations.mapNotNull { conv ->
                        val otherUserId = if (conv.participant1Id == getCurrentUserId()) {
                            conv.participant2Id
                        } else {
                            conv.participant1Id
                        }
                        
                        val user = loadUserInfo(otherUserId)
                        user?.let { ConversationWithUser(conv, it) }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            conversations = conversationsWithUsers,
                            isLoading = false
                        ) 
                    }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            error = "Không thể tải tin nhắn",
                            isLoading = false
                        ) 
                    }
                }
        }
    }

    /**
     * Mở chat với bạn bè
     */
    fun openChat(friendId: String, friendUsername: String, friendAvatar: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            messageRepository.getOrCreateConversation(friendId)
                .onSuccess { conversation ->
                    val partner = UserBasicDto(
                        id = friendId,
                        username = friendUsername,
                        avatar = friendAvatar
                    )
                    
                    _uiState.update { 
                        it.copy(
                            currentConversation = conversation,
                            currentChatPartner = partner,
                            isLoading = false
                        ) 
                    }
                    
                    // Load messages
                    loadMessages(conversation.id)
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            error = "Không thể mở cuộc trò chuyện",
                            isLoading = false
                        ) 
                    }
                }
        }
    }

    /**
     * Load messages của conversation hiện tại
     */
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            messageRepository.getMessages(conversationId)
                .onSuccess { messages ->
                    _uiState.update { it.copy(currentMessages = messages) }
                    
                    // Mark messages as read
                    messageRepository.markMessagesAsRead(conversationId)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Không thể tải tin nhắn") }
                }
        }
    }

    /**
     * Gửi tin nhắn
     */
    fun sendMessage(content: String) {
        val conversationId = _uiState.value.currentConversation?.id ?: return
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            
            messageRepository.sendMessage(conversationId, content.trim())
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
                            error = "Không thể gửi tin nhắn",
                            isSending = false
                        ) 
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

package com.projectapp.tempus.ui.social.messages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.social.dto.MessageDto
import com.projectapp.tempus.data.social.repository.MessageRepository
import com.projectapp.tempus.data.social.repository.SupabaseMessageRepository
import com.projectapp.tempus.domain.social.model.ConversationWithUser
import com.projectapp.tempus.domain.social.model.NotificationEvent
import com.projectapp.tempus.domain.social.model.UserBasicInfo
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State cho Messages
 */
data class MessagesUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val conversations: List<ConversationWithUser> = emptyList(),
    val currentMessages: List<MessageDto> = emptyList(),
    val currentChatFriend: UserBasicInfo? = null,
    val currentConversationId: String? = null,
    val error: String? = null
)

/**
 * ViewModel quản lý messaging với realtime support
 */
class MessagesViewModel : ViewModel() {
    
    private val repository: MessageRepository = SupabaseMessageRepository()
    
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()
    
    private val _notificationEvents = MutableSharedFlow<NotificationEvent>()
    val notificationEvents: SharedFlow<NotificationEvent> = _notificationEvents.asSharedFlow()
    
    private var realtimeJob: Job? = null
    
    companion object {
        private const val TAG = "MessagesViewModel"
    }
    
    init {
        loadConversations()
    }
    
    /**
     * Tải danh sách conversations
     */
    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.getConversations()
                .onSuccess { conversations ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            conversations = conversations
                        ) 
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Error loading conversations", error)
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Không thể tải cuộc trò chuyện"
                        ) 
                    }
                }
        }
    }
    
    /**
     * Mở chat với một bạn bè
     */
    fun openChat(friendId: String, friendUsername: String, friendAvatar: String?) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    error = null,
                    currentChatFriend = UserBasicInfo(friendId, friendUsername, friendAvatar)
                ) 
            }
            
            // Get or create conversation
            repository.getOrCreateConversation(friendId)
                .onSuccess { conversation ->
                    _uiState.update { 
                        it.copy(currentConversationId = conversation.id) 
                    }
                    
                    // Load existing messages
                    loadMessages(conversation.id)
                    
                    // Subscribe to realtime updates
                    subscribeToRealtimeMessages(conversation.id)
                }
                .onFailure { error ->
                    Log.e(TAG, "Error opening chat", error)
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Không thể mở cuộc trò chuyện"
                        ) 
                    }
                }
        }
    }
    
    /**
     * Tải tin nhắn trong conversation
     */
    private suspend fun loadMessages(conversationId: String) {
        repository.getMessages(conversationId)
            .onSuccess { messages ->
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        currentMessages = messages
                    ) 
                }
            }
            .onFailure { error ->
                Log.e(TAG, "Error loading messages", error)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Không thể tải tin nhắn"
                    ) 
                }
            }
    }
    
    /**
     * Subscribe để nhận tin nhắn realtime
     */
    private fun subscribeToRealtimeMessages(conversationId: String) {
        // Cancel previous subscription
        realtimeJob?.cancel()
        
        val currentUserId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
        
        realtimeJob = viewModelScope.launch {
            repository.subscribeToMessages(conversationId)
                .collect { newMessage ->
                    Log.d(TAG, "New realtime message: ${newMessage.content}")
                    
                    // Add message to list if not already present
                    _uiState.update { state ->
                        if (state.currentMessages.none { it.id == newMessage.id }) {
                            state.copy(
                                currentMessages = state.currentMessages + newMessage
                            )
                        } else {
                            state
                        }
                    }
                    
                    // Send notification if message is from other user
                    if (newMessage.senderId != currentUserId) {
                        val senderName = _uiState.value.currentChatFriend?.username ?: "Bạn bè"
                        _notificationEvents.emit(
                            NotificationEvent(
                                conversationId = conversationId,
                                senderName = senderName,
                                messageContent = newMessage.content
                            )
                        )
                    }
                }
        }
    }
    
    /**
     * Gửi tin nhắn
     */
    fun sendMessage(content: String) {
        val conversationId = _uiState.value.currentConversationId ?: return
        
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            
            repository.sendMessage(conversationId, content.trim())
                .onSuccess { message ->
                    // Message will be added via realtime subscription
                    // But add immediately for better UX
                    _uiState.update { state ->
                        if (state.currentMessages.none { it.id == message.id }) {
                            state.copy(
                                isSending = false,
                                currentMessages = state.currentMessages + message
                            )
                        } else {
                            state.copy(isSending = false)
                        }
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Error sending message", error)
                    _uiState.update { 
                        it.copy(
                            isSending = false, 
                            error = "Không thể gửi tin nhắn"
                        ) 
                    }
                }
        }
    }
    
    /**
     * Đóng chat - cleanup realtime subscription
     */
    fun closeChat() {
        realtimeJob?.cancel()
        realtimeJob = null
        
        viewModelScope.launch {
            repository.unsubscribeFromMessages()
        }
        
        _uiState.update { 
            it.copy(
                currentMessages = emptyList(),
                currentChatFriend = null,
                currentConversationId = null
            ) 
        }
        
        // Reload conversations to update last message preview
        loadConversations()
    }
    
    /**
     * Xóa lỗi
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
        viewModelScope.launch {
            repository.unsubscribeFromMessages()
        }
    }
}

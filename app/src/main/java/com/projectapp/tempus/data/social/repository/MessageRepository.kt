package com.projectapp.tempus.data.social.repository

import com.projectapp.tempus.data.social.dto.ConversationDto
import com.projectapp.tempus.data.social.dto.MessageDto
import com.projectapp.tempus.domain.social.model.ConversationWithUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface cho messaging operations
 */
interface MessageRepository {
    
    // ============================================
    // CONVERSATIONS
    // ============================================
    
    /**
     * Lấy danh sách conversations của user hiện tại
     */
    suspend fun getConversations(): Result<List<ConversationWithUser>>
    
    /**
     * Lấy hoặc tạo conversation với một user khác
     */
    suspend fun getOrCreateConversation(otherUserId: String): Result<ConversationDto>
    
    // ============================================
    // MESSAGES
    // ============================================
    
    /**
     * Lấy tin nhắn trong một conversation
     */
    suspend fun getMessages(conversationId: String): Result<List<MessageDto>>
    
    /**
     * Gửi tin nhắn mới
     */
    suspend fun sendMessage(conversationId: String, content: String): Result<MessageDto>
    
    // ============================================
    // REALTIME
    // ============================================
    
    /**
     * Subscribe để nhận tin nhắn mới trong conversation
     * Flow sẽ emit mỗi khi có tin nhắn mới từ realtime
     */
    fun subscribeToMessages(conversationId: String): Flow<MessageDto>
    
    /**
     * Hủy subscription realtime hiện tại
     */
    suspend fun unsubscribeFromMessages()
    
    // ============================================
    // IMAGE MESSAGES
    // ============================================
    
    /**
     * Gửi tin nhắn hình ảnh
     * Upload ảnh lên storage và tạo message với type = "image"
     */
    suspend fun sendImageMessage(conversationId: String, imageBytes: ByteArray): Result<MessageDto>
}

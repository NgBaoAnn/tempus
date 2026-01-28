package com.projectapp.tempus.data.social.repository

import com.projectapp.tempus.data.social.dto.ConversationDto
import com.projectapp.tempus.data.social.dto.MessageDto

/**
 * Repository interface cho Messages feature
 */
interface MessageRepository {
    
    /**
     * Lấy danh sách conversations của user hiện tại
     */
    suspend fun getConversations(): Result<List<ConversationDto>>
    
    /**
     * Lấy messages của một conversation
     */
    suspend fun getMessages(conversationId: String): Result<List<MessageDto>>
    
    /**
     * Gửi tin nhắn mới
     */
    suspend fun sendMessage(conversationId: String, content: String): Result<MessageDto>
    
    /**
     * Lấy hoặc tạo conversation với một user
     * Nếu đã có conversation sẽ trả về, nếu chưa sẽ tạo mới
     */
    suspend fun getOrCreateConversation(otherUserId: String): Result<ConversationDto>
    
    /**
     * Đánh dấu messages đã đọc
     */
    suspend fun markMessagesAsRead(conversationId: String): Result<Unit>
    
    /**
     * Subscribe to messages flow for real-time updates
     */
    fun getMessagesFlow(conversationId: String): kotlinx.coroutines.flow.Flow<List<MessageDto>>
}

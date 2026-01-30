package com.projectapp.tempus.data.social.repository

import com.projectapp.tempus.data.social.dto.ConversationDto
import com.projectapp.tempus.data.social.dto.MessageDto
import com.projectapp.tempus.domain.social.model.ConversationWithUser
import kotlinx.coroutines.flow.Flow


interface MessageRepository {
    
    
    suspend fun getConversations(): Result<List<ConversationWithUser>>
    
    
    suspend fun getOrCreateConversation(otherUserId: String): Result<ConversationDto>
    
    
    suspend fun getMessages(conversationId: String): Result<List<MessageDto>>
    
    
    suspend fun sendMessage(conversationId: String, content: String): Result<MessageDto>
    
    
    fun subscribeToMessages(conversationId: String): Flow<MessageDto>
    
    
    suspend fun unsubscribeFromMessages()
    
    
    suspend fun sendImageMessage(conversationId: String, imageBytes: ByteArray): Result<MessageDto>
}

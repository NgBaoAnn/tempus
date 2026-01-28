package com.projectapp.tempus.domain.social.model

import com.projectapp.tempus.data.social.dto.ConversationDto

/**
 * Domain model cho conversation với thông tin user
 */
data class ConversationWithUser(
    val conversation: ConversationDto,
    val otherUser: UserBasicInfo
)

/**
 * Thông tin cơ bản của user
 */
data class UserBasicInfo(
    val id: String,
    val username: String,
    val avatar: String?
)

/**
 * Event để hiển thị notification khi có tin nhắn mới
 */
data class NotificationEvent(
    val conversationId: String,
    val senderName: String,
    val messageContent: String
)

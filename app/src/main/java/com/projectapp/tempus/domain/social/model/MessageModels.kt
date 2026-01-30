package com.projectapp.tempus.domain.social.model

import com.projectapp.tempus.data.social.dto.ConversationDto


data class ConversationWithUser(
    val conversation: ConversationDto,
    val otherUser: UserBasicInfo
)


data class UserBasicInfo(
    val id: String,
    val username: String,
    val avatar: String?
)


data class NotificationEvent(
    val conversationId: String,
    val senderName: String,
    val messageContent: String
)

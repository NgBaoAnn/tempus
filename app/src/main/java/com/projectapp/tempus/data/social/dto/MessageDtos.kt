package com.projectapp.tempus.data.social.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ConversationDto(
    val id: String,
    @SerialName("participant1_id")
    val participant1Id: String,
    @SerialName("participant2_id")
    val participant2Id: String,
    @SerialName("last_message_at")
    val lastMessageAt: String? = null,
    @SerialName("last_message_preview")
    val lastMessagePreview: String? = null,
    @SerialName("created_at")
    val createdAt: String
)


@Serializable
data class MessageDto(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("sender_id")
    val senderId: String,
    val content: String,
    @SerialName("message_type")
    val messageType: String = "text",
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    val createdAt: String
)


@Serializable
data class CreateConversationDto(
    @SerialName("participant1_id")
    val participant1Id: String,
    @SerialName("participant2_id")
    val participant2Id: String
)


@Serializable
data class CreateMessageDto(
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("sender_id")
    val senderId: String,
    val content: String,
    @SerialName("message_type")
    val messageType: String = "text"
)


@Serializable
data class UpdateConversationDto(
    @SerialName("last_message_at")
    val lastMessageAt: String,
    @SerialName("last_message_preview")
    val lastMessagePreview: String
)

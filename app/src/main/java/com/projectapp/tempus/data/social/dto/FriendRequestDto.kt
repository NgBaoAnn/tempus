package com.projectapp.tempus.data.social.dto

import com.projectapp.tempus.domain.social.model.FriendRequest
import com.projectapp.tempus.domain.social.model.FriendRequestStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant


@Serializable
data class FriendRequestDto(
    val id: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    
    val sender: UserBasicDto? = null,
    val receiver: UserBasicDto? = null
)


@Serializable
data class UserBasicDto(
    val id: String,
    val username: String,
    val avatar: String? = null,
    val email: String? = null
)


@Serializable
data class CreateFriendRequestDto(
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("receiver_id")
    val receiverId: String
)


@Serializable
data class UpdateFriendRequestDto(
    val status: String,
    @SerialName("updated_at")
    val updatedAt: String
)


fun FriendRequestDto.toDomain(): FriendRequest {
    return FriendRequest(
        id = id,
        senderId = senderId,
        senderUsername = sender?.username ?: "Unknown",
        senderAvatar = sender?.avatar,
        receiverId = receiverId,
        receiverUsername = receiver?.username ?: "Unknown",
        receiverAvatar = receiver?.avatar,
        status = FriendRequestStatus.fromString(status),
        createdAt = try {
            Instant.parse(createdAt)
        } catch (e: Exception) {
            Instant.now()
        },
        updatedAt = try {
            Instant.parse(updatedAt)
        } catch (e: Exception) {
            Instant.now()
        }
    )
}

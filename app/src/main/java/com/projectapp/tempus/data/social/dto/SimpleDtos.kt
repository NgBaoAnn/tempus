package com.projectapp.tempus.data.social.dto

import com.projectapp.tempus.domain.social.model.FriendRequest
import com.projectapp.tempus.domain.social.model.FriendRequestStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Simple DTO cho friend_requests table - không có FK joins
 */
@Serializable
data class FriendRequestSimpleDto(
    val id: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
) {
    fun toDomain(): FriendRequest {
        return FriendRequest(
            id = id,
            senderId = senderId,
            senderUsername = "User", // Placeholder
            senderAvatar = null,
            receiverId = receiverId,
            receiverUsername = "User", // Placeholder
            receiverAvatar = null,
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
}

/**
 * Simple DTO cho friendships table - không có FK joins
 */
@Serializable
data class FriendshipSimpleDto(
    val id: String,
    @SerialName("user1_id")
    val user1Id: String,
    @SerialName("user2_id")
    val user2Id: String,
    @SerialName("created_at")
    val createdAt: String
)

/**
 * Simple DTO cho blocked_users table - không có FK joins
 */
@Serializable
data class BlockedUserSimpleDto(
    val id: String,
    @SerialName("blocker_id")
    val blockerId: String,
    @SerialName("blocked_id")
    val blockedId: String,
    @SerialName("created_at")
    val createdAt: String
)

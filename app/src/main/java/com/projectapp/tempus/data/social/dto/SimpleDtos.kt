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
    val status: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null  // Nullable vì có thể chưa được set
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
            status = FriendRequestStatus.fromString(status ?: "pending"),
            createdAt = try {
                createdAt?.let { Instant.parse(it) } ?: Instant.now()
            } catch (e: Exception) {
                Instant.now()
            },
            updatedAt = try {
                updatedAt?.let { Instant.parse(it) } ?: Instant.now()
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
    val createdAt: String? = null  // Nullable để tương thích với database
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
    val createdAt: String? = null  // Nullable để tương thích với database
)

/**
 * Helper DTO for decoding blocked_id only
 */
@Serializable
data class BlockedIdDto(
    @SerialName("blocked_id")
    val blockedId: String
)

/**
 * Helper DTO for decoding blocker_id only
 */
@Serializable
data class BlockerIdDto(
    @SerialName("blocker_id")
    val blockerId: String
)

package com.projectapp.tempus.data.social.dto

import com.projectapp.tempus.domain.social.model.Friendship
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * DTO cho friendships table từ Supabase
 */
@Serializable
data class FriendshipDto(
    val id: String,
    @SerialName("user1_id")
    val user1Id: String,
    @SerialName("user2_id")
    val user2Id: String,
    @SerialName("created_at")
    val createdAt: String,
    // Joined user data - friend info
    val friend: UserBasicDto? = null
)

/**
 * DTO để tạo friendship mới
 */
@Serializable
data class CreateFriendshipDto(
    @SerialName("user1_id")
    val user1Id: String,
    @SerialName("user2_id")
    val user2Id: String
)

/**
 * Convert DTO to Domain model
 * @param currentUserId ID của user hiện tại để xác định friend là ai
 */
fun FriendshipDto.toDomain(currentUserId: String): Friendship {
    // Xác định friend ID dựa trên user hiện tại
    val friendId = if (user1Id == currentUserId) user2Id else user1Id
    
    return Friendship(
        id = id,
        friendId = friendId,
        friendUsername = friend?.username ?: "Unknown",
        friendAvatar = friend?.avatar,
        friendEmail = friend?.email ?: "",
        createdAt = try {
            Instant.parse(createdAt)
        } catch (e: Exception) {
            Instant.now()
        }
    )
}

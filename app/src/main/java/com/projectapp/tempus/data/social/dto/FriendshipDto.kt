package com.projectapp.tempus.data.social.dto

import com.projectapp.tempus.domain.social.model.Friendship
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant


@Serializable
data class FriendshipDto(
    val id: String,
    @SerialName("user1_id")
    val user1Id: String,
    @SerialName("user2_id")
    val user2Id: String,
    @SerialName("created_at")
    val createdAt: String,
    
    val friend: UserBasicDto? = null
)


@Serializable
data class CreateFriendshipDto(
    @SerialName("user1_id")
    val user1Id: String,
    @SerialName("user2_id")
    val user2Id: String
)


fun FriendshipDto.toDomain(currentUserId: String): Friendship {
    
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

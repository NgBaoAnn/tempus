package com.projectapp.tempus.domain.social.model

import java.time.Instant

/**
 * Domain model cho lời mời kết bạn
 */
data class FriendRequest(
    val id: String,
    val senderId: String,
    val senderUsername: String,
    val senderAvatar: String?,
    val receiverId: String,
    val receiverUsername: String,
    val receiverAvatar: String?,
    val status: FriendRequestStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

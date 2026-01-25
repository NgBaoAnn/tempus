package com.projectapp.tempus.domain.social.model

import java.time.Instant

/**
 * Domain model cho quan hệ bạn bè
 */
data class Friendship(
    val id: String,
    val friendId: String,
    val friendUsername: String,
    val friendAvatar: String?,
    val friendEmail: String,
    val createdAt: Instant
)

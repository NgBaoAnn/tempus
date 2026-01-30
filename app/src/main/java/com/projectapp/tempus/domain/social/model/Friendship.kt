package com.projectapp.tempus.domain.social.model

import java.time.Instant


data class Friendship(
    val id: String,
    val friendId: String,
    val friendUsername: String,
    val friendAvatar: String?,
    val friendEmail: String,
    val createdAt: Instant
)

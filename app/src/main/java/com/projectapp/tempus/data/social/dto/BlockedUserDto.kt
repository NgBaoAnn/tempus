package com.projectapp.tempus.data.social.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO cho blocked_users table từ Supabase
 */
@Serializable
data class BlockedUserDto(
    val id: String,
    @SerialName("blocker_id")
    val blockerId: String,
    @SerialName("blocked_id")
    val blockedId: String,
    @SerialName("created_at")
    val createdAt: String,
    // Joined blocked user info
    val blocked: UserBasicDto? = null
)

/**
 * DTO để block user
 */
@Serializable
data class CreateBlockedUserDto(
    @SerialName("blocker_id")
    val blockerId: String,
    @SerialName("blocked_id")
    val blockedId: String
)

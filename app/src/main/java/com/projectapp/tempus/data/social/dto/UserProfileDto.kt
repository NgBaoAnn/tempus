package com.projectapp.tempus.data.social.dto

import com.projectapp.tempus.data.social.repository.RelationshipStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserProfileDto(
    val id: String,
    val username: String,
    val avatar: String? = null,
    val email: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    
    
    @SerialName("friends_count")
    val friendsCount: Int = 0,
    
    @SerialName("trees_count")
    val treesCount: Int = 0
)


data class UserProfile(
    val id: String,
    val username: String,
    val avatar: String?,
    val email: String?,
    val joinedDate: String,
    val friendsCount: Int,
    val treesCount: Int,
    val relationshipStatus: RelationshipStatus
)

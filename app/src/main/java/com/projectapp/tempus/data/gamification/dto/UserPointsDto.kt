package com.projectapp.tempus.data.gamification.dto

import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserPointsDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("total_points")
    val totalPoints: Int = 0,
    @SerialName("current_streak")
    val currentStreak: Int = 0,
    @SerialName("best_streak")
    val bestStreak: Int = 0,
    @SerialName("last_active_date")
    val lastActiveDate: String? = null,
    val level: Int = 1,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)


fun UserPointsDto.toEntity(): UserPointsEntity {
    return UserPointsEntity(
        id = "current_user",
        totalPoints = totalPoints,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        lastActiveDate = lastActiveDate,
        level = level
    )
}


fun UserPointsEntity.toDto(userId: String): UserPointsDto {
    return UserPointsDto(
        userId = userId,
        totalPoints = totalPoints,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        lastActiveDate = lastActiveDate,
        level = level
    )
}


@Serializable
data class UserPointsUpdateDto(
    @SerialName("total_points")
    val totalPoints: Int,
    @SerialName("current_streak")
    val currentStreak: Int,
    @SerialName("best_streak")
    val bestStreak: Int,
    @SerialName("last_active_date")
    val lastActiveDate: String?,
    val level: Int
)

fun UserPointsEntity.toUpdateDto(): UserPointsUpdateDto {
    return UserPointsUpdateDto(
        totalPoints = totalPoints,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        lastActiveDate = lastActiveDate,
        level = level
    )
}

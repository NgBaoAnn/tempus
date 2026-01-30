package com.projectapp.tempus.data.gamification.dto

import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PointHistoryDto(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    val points: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)


@Serializable
data class PointHistoryInsertDto(
    @SerialName("user_id")
    val userId: String,
    val points: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)


fun PointHistoryDto.toEntity(): PointHistoryEntity {
    return PointHistoryEntity(
        id = id ?: 0,
        points = points,
        reason = reason,
        timestamp = timestamp
    )
}


fun PointHistoryEntity.toInsertDto(userId: String): PointHistoryInsertDto {
    return PointHistoryInsertDto(
        userId = userId,
        points = points,
        reason = reason,
        timestamp = timestamp
    )
}

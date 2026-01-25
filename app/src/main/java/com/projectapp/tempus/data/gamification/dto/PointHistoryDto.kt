package com.projectapp.tempus.data.gamification.dto

import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO để đọc từ bảng point_history trên Supabase
 */
@Serializable
data class PointHistoryDto(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    val points: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * DTO riêng cho INSERT - không có id (để Supabase auto-generate)
 */
@Serializable
data class PointHistoryInsertDto(
    @SerialName("user_id")
    val userId: String,
    val points: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Convert DTO sang Entity (khi đọc từ DB)
 */
fun PointHistoryDto.toEntity(): PointHistoryEntity {
    return PointHistoryEntity(
        id = id ?: 0,
        points = points,
        reason = reason,
        timestamp = timestamp
    )
}

/**
 * Convert Entity sang DTO (khi đọc)
 */
fun PointHistoryEntity.toDto(userId: String): PointHistoryDto {
    return PointHistoryDto(
        userId = userId,
        points = points,
        reason = reason,
        timestamp = timestamp
    )
}

/**
 * Convert Entity sang InsertDto (khi INSERT vào DB)
 */
fun PointHistoryEntity.toInsertDto(userId: String): PointHistoryInsertDto {
    return PointHistoryInsertDto(
        userId = userId,
        points = points,
        reason = reason,
        timestamp = timestamp
    )
}

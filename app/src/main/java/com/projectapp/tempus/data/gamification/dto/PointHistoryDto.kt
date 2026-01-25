package com.projectapp.tempus.data.gamification.dto

import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
<<<<<<< HEAD
 * DTO để đọc từ bảng point_history trên Supabase
=======
 * DTO để map với bảng point_history trên Supabase (cho SELECT)
>>>>>>> master
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
<<<<<<< HEAD
 * DTO riêng cho INSERT - không có id (để Supabase auto-generate)
=======
 * DTO cho INSERT - không có id vì bigserial tự generate
>>>>>>> master
 */
@Serializable
data class PointHistoryInsertDto(
    @SerialName("user_id")
    val userId: String,
    val points: Int,
<<<<<<< HEAD
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Convert DTO sang Entity (khi đọc từ DB)
=======
    val reason: String
    // timestamp sử dụng default của database
)

/**
 * Convert DTO sang Entity
>>>>>>> master
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
<<<<<<< HEAD
 * Convert Entity sang DTO (khi đọc)
=======
 * Convert Entity sang InsertDTO (cho insert)
>>>>>>> master
 */
fun PointHistoryEntity.toInsertDto(userId: String): PointHistoryInsertDto {
    return PointHistoryInsertDto(
        userId = userId,
        points = points,
        reason = reason
    )
}

<<<<<<< HEAD
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
=======
>>>>>>> master

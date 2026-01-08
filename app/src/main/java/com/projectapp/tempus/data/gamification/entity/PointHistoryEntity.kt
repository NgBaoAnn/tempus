package com.projectapp.tempus.data.gamification.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity lưu trữ lịch sử điểm đã kiếm/mất
 */
@Entity(tableName = "point_history")
data class PointHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val points: Int,  // Dương = earned, Âm = lost
    val reason: String,  // "TASK_COMPLETE", "POMODORO_COMPLETE", "STREAK_BONUS", "MISS_TASK", "PLANT_TREE"
    val timestamp: Long = System.currentTimeMillis()
)

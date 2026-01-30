package com.projectapp.tempus.data.gamification.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "point_history")
data class PointHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val points: Int,  
    val reason: String,  
    val timestamp: Long = System.currentTimeMillis()
)

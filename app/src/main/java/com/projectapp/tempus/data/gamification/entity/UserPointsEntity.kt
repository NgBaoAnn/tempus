package com.projectapp.tempus.data.gamification.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_points")
data class UserPointsEntity(
    @PrimaryKey
    val id: String = "current_user",
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,  
    val bestStreak: Int = 0,
    val lastActiveDate: String? = null,  
    val level: Int = 1
)

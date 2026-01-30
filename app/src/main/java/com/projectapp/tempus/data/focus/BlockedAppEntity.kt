package com.projectapp.tempus.data.focus

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val addedAt: Long = System.currentTimeMillis()
)

package com.projectapp.tempus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["userId"])
    ]
)
data class CategoryEntity(
    @PrimaryKey 
    val id: String,
    val userId: String,
    val name: String,
    val color: String = "#2196F3",
    val icon: String = "folder",
    
    
    val createdAt: Long = System.currentTimeMillis(),
    
    
    val syncStatus: String = SyncStatus.SYNCED.name,
    val localUpdatedAt: Long = System.currentTimeMillis()
)

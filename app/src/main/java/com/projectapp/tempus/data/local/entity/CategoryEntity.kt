package com.projectapp.tempus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity cho Category (danh mục task)
 * Tương ứng với bảng `categories` trên Supabase
 */
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
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    
    // ===== SYNC TRACKING =====
    val syncStatus: String = SyncStatus.SYNCED.name,
    val localUpdatedAt: Long = System.currentTimeMillis()
)

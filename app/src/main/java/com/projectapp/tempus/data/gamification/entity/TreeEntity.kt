package com.projectapp.tempus.data.gamification.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity lưu trữ thông tin cây của người dùng
 */
@Entity(tableName = "trees")
data class TreeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "My Tree",
    val treeType: String = "oak",  // oak, pine, sakura, bamboo, palm, apple
    val investedPoints: Int = 0,
    val state: String = "SEED",  // SEED, SPROUT, SAPLING, TREE, DEAD
    val createdAt: Long = System.currentTimeMillis(),
    val lastWateredAt: Long = System.currentTimeMillis(),
    val isAlive: Boolean = true
)

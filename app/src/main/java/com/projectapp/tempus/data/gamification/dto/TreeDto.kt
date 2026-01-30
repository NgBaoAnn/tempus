package com.projectapp.tempus.data.gamification.dto

import com.projectapp.tempus.data.gamification.entity.TreeEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class TreeDto(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    val name: String = "My Tree",
    @SerialName("tree_type")
    val treeType: String = "oak",
    @SerialName("invested_points")
    val investedPoints: Int = 0,
    val state: String = "SEED",
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("last_watered_at")
    val lastWateredAt: Long = System.currentTimeMillis(),
    @SerialName("is_alive")
    val isAlive: Boolean = true
)


fun TreeDto.toEntity(): TreeEntity {
    return TreeEntity(
        id = id ?: 0,
        name = name,
        treeType = treeType,
        investedPoints = investedPoints,
        state = state,
        createdAt = createdAt,
        lastWateredAt = lastWateredAt,
        isAlive = isAlive
    )
}


fun TreeEntity.toDto(userId: String): TreeDto {
    return TreeDto(
        id = if (id == 0L) null else id,
        userId = userId,
        name = name,
        treeType = treeType,
        investedPoints = investedPoints,
        state = state,
        createdAt = createdAt,
        lastWateredAt = lastWateredAt,
        isAlive = isAlive
    )
}


@Serializable
data class TreeUpdateDto(
    val name: String,
    @SerialName("tree_type")
    val treeType: String,
    @SerialName("invested_points")
    val investedPoints: Int,
    val state: String,
    @SerialName("last_watered_at")
    val lastWateredAt: Long,
    @SerialName("is_alive")
    val isAlive: Boolean
)

fun TreeEntity.toUpdateDto(): TreeUpdateDto {
    return TreeUpdateDto(
        name = name,
        treeType = treeType,
        investedPoints = investedPoints,
        state = state,
        lastWateredAt = lastWateredAt,
        isAlive = isAlive
    )
}


@Serializable
data class KillTreeDto(
    val state: String = "DEAD",
    @SerialName("is_alive")
    val isAlive: Boolean = false
)

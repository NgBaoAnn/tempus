package com.projectapp.tempus.domain.model

/**
 * Các trạng thái phát triển của cây
 */
enum class TreeState(
    val displayName: String,
    val minPoints: Int,
    val maxPoints: Int,
    val emoji: String
) {
    SEED("Hạt giống", 0, 99, "🌱"),
    SPROUT("Mầm non", 100, 249, "🌿"),
    SAPLING("Cây con", 250, 499, "🌲"),
    TREE("Cây trưởng thành", 500, Int.MAX_VALUE, "🌳"),
    DEAD("Đã chết", -1, -1, "💀");
    
    companion object {
        fun fromPoints(points: Int): TreeState {
            return entries.firstOrNull { points in it.minPoints..it.maxPoints } ?: SEED
        }
        
        fun fromString(state: String): TreeState {
            return entries.firstOrNull { it.name == state } ?: SEED
        }
    }
}

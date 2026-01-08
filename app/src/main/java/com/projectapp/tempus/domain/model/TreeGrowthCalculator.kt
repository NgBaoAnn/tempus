package com.projectapp.tempus.domain.model

import java.util.concurrent.TimeUnit

/**
 * Calculator để tính toán trạng thái phát triển của cây
 */
class TreeGrowthCalculator {
    
    companion object {
        const val DAYS_TO_DIE = 3  // Cây chết nếu không tưới 3 ngày
    }
    
    /**
     * Tính trạng thái cây dựa trên điểm đã đầu tư
     */
    fun calculateState(investedPoints: Int): TreeState {
        return TreeState.fromPoints(investedPoints)
    }
    
    /**
     * Tính phần trăm tiến độ trong trạng thái hiện tại (0-100)
     */
    fun getProgressPercent(investedPoints: Int): Float {
        val state = calculateState(investedPoints)
        if (state == TreeState.TREE) return 100f
        if (state == TreeState.DEAD) return 0f
        
        val pointsInState = investedPoints - state.minPoints
        val stateRange = state.maxPoints - state.minPoints + 1
        return (pointsInState.toFloat() / stateRange * 100).coerceIn(0f, 100f)
    }
    
    /**
     * Kiểm tra xem cây có nên chết không (không tưới quá 3 ngày)
     */
    fun shouldDie(lastWateredAt: Long): Boolean {
        val daysSinceWatered = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - lastWateredAt
        )
        return daysSinceWatered >= DAYS_TO_DIE
    }
    
    /**
     * Tính số ngày còn lại trước khi cây chết
     */
    fun getDaysUntilDeath(lastWateredAt: Long): Int {
        val daysSinceWatered = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - lastWateredAt
        ).toInt()
        return (DAYS_TO_DIE - daysSinceWatered).coerceAtLeast(0)
    }
    
    /**
     * Kiểm tra có đủ điểm để trồng cây không
     */
    fun canPlantTree(currentPoints: Int, treeType: TreeType): Boolean {
        return currentPoints >= treeType.costToPlant
    }
    
    /**
     * Tính điểm cần để lên level tiếp theo
     */
    fun getPointsToNextLevel(investedPoints: Int): Int? {
        val currentState = calculateState(investedPoints)
        return when (currentState) {
            TreeState.SEED -> TreeState.SPROUT.minPoints - investedPoints
            TreeState.SPROUT -> TreeState.SAPLING.minPoints - investedPoints
            TreeState.SAPLING -> TreeState.TREE.minPoints - investedPoints
            else -> null  // Đã max hoặc đã chết
        }
    }
}

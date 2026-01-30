package com.projectapp.tempus.domain.model

import java.util.concurrent.TimeUnit


class TreeGrowthCalculator {
    
    companion object {
        const val DAYS_TO_DIE = 3  
    }
    
    
    fun calculateState(investedPoints: Int): TreeState {
        return TreeState.fromPoints(investedPoints)
    }
    
    
    fun getProgressPercent(investedPoints: Int): Float {
        val state = calculateState(investedPoints)
        if (state == TreeState.TREE) return 100f
        if (state == TreeState.DEAD) return 0f
        
        val pointsInState = investedPoints - state.minPoints
        val stateRange = state.maxPoints - state.minPoints + 1
        return (pointsInState.toFloat() / stateRange * 100).coerceIn(0f, 100f)
    }
    
    
    fun shouldDie(lastWateredAt: Long): Boolean {
        val daysSinceWatered = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - lastWateredAt
        )
        return daysSinceWatered >= DAYS_TO_DIE
    }
    
    
    fun getDaysUntilDeath(lastWateredAt: Long): Int {
        val daysSinceWatered = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - lastWateredAt
        ).toInt()
        return (DAYS_TO_DIE - daysSinceWatered).coerceAtLeast(0)
    }
    
    
    fun canPlantTree(currentPoints: Int, treeType: TreeType): Boolean {
        return currentPoints >= treeType.costToPlant
    }
    
    
    fun getPointsToNextLevel(investedPoints: Int): Int? {
        val currentState = calculateState(investedPoints)
        return when (currentState) {
            TreeState.SEED -> TreeState.SPROUT.minPoints - investedPoints
            TreeState.SPROUT -> TreeState.SAPLING.minPoints - investedPoints
            TreeState.SAPLING -> TreeState.TREE.minPoints - investedPoints
            else -> null  
        }
    }
}

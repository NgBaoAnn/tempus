package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.data.gamification.GamificationRepository
import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import com.projectapp.tempus.domain.model.PointAction
import com.projectapp.tempus.domain.model.TreeGrowthCalculator
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate


class PointsManager(
    val repository: GamificationRepository  
) {
    companion object {
        const val STREAK_MULTIPLIER = 1.5f
    }
    
    private val treeCalculator = TreeGrowthCalculator()
    
    
    fun getUserPoints(): Flow<UserPointsEntity?> = repository.getUserPoints()
    
    
    suspend fun earnPoints(action: PointAction): Int {
        val current = repository.getOrCreateUserPoints()
        
        
        val hasStreak = current.currentStreak >= 3
        val multiplier = if (hasStreak && action.isEarning()) STREAK_MULTIPLIER else 1f
        val finalPoints = (action.points * multiplier).toInt()
        
        
        val newTotal = (current.totalPoints + finalPoints).coerceAtLeast(0)
        repository.updateUserPoints(current.copy(totalPoints = newTotal))
        
        
        repository.addPointHistory(PointHistoryEntity(
            points = finalPoints,
            reason = action.name
        ))
        
        return finalPoints
    }
    
    
    suspend fun earnPomodoroPoints(focusMinutes: Int): Int {
        val current = repository.getOrCreateUserPoints()
        
        
        val basePoints = focusMinutes.coerceAtLeast(1)
        
        
        val hasStreak = current.currentStreak >= 3
        val multiplier = if (hasStreak) STREAK_MULTIPLIER else 1f
        val finalPoints = (basePoints * multiplier).toInt()
        
        
        val newTotal = (current.totalPoints + finalPoints).coerceAtLeast(0)
        repository.updateUserPoints(current.copy(totalPoints = newTotal))
        
        
        repository.addPointHistory(PointHistoryEntity(
            points = finalPoints,
            reason = "POMODORO_${focusMinutes}m"
        ))
        
        return finalPoints
    }
    
    
    suspend fun spendPoints(amount: Int, reason: String): Boolean {
        val current = repository.getOrCreateUserPoints()
        if (current.totalPoints < amount) return false
        
        repository.updateUserPoints(current.copy(
            totalPoints = current.totalPoints - amount
        ))
        
        repository.addPointHistory(PointHistoryEntity(
            points = -amount,
            reason = reason
        ))
        
        return true
    }
    
    
    suspend fun updateStreak() {
        val today = LocalDate.now().toString()
        val current = repository.getOrCreateUserPoints()
        
        
        if (current.lastActiveDate == today) return
        
        val yesterday = LocalDate.now().minusDays(1).toString()
        val newStreak = if (current.lastActiveDate == yesterday) {
            current.currentStreak + 1
        } else {
            1  
        }
        
        repository.updateUserPoints(current.copy(
            currentStreak = newStreak,
            bestStreak = maxOf(current.bestStreak, newStreak),
            lastActiveDate = today
        ))
        
        
        when (newStreak) {
            3 -> earnPoints(PointAction.STREAK_BONUS_3)
            7 -> earnPoints(PointAction.STREAK_BONUS_7)
            30 -> earnPoints(PointAction.STREAK_BONUS_30)
        }
    }
    
    
    fun getPointHistory(): Flow<List<PointHistoryEntity>> = repository.getPointHistory()
    
    
    fun getAliveTrees(): Flow<List<TreeEntity>> = repository.getAliveTrees()
    
    
    suspend fun plantTree(type: TreeType, name: String = type.displayName): Long? {
        val current = repository.getOrCreateUserPoints()
        
        if (!treeCalculator.canPlantTree(current.totalPoints, type)) {
            return null
        }
        
        
        val success = spendPoints(type.costToPlant, "PLANT_${type.name}")
        if (!success) return null
        
        
        val tree = TreeEntity(
            name = name,
            treeType = type.name,
            state = TreeState.SEED.name
        )
        
        return repository.plantTree(tree)
    }
    
    
    suspend fun waterTree(treeId: Long, points: Int = 10): TreeState? {
        val tree = repository.getTreeById(treeId) ?: return null
        if (!tree.isAlive) return null
        
        
        val success = spendPoints(points, "WATER_TREE_$treeId")
        if (!success) return null
        
        
        val newInvestedPoints = tree.investedPoints + points
        val newState = treeCalculator.calculateState(newInvestedPoints)
        
        repository.updateTree(tree.copy(
            investedPoints = newInvestedPoints,
            state = newState.name,
            lastWateredAt = System.currentTimeMillis()
        ))
        
        return newState
    }
    
    
    suspend fun checkAndUpdateDeadTrees() {
        val trees = repository.getAliveTrees().first()
        
        trees.forEach { tree ->
            if (treeCalculator.shouldDie(tree.lastWateredAt)) {
                repository.killTree(tree.id)
            }
        }
    }
    
    
    suspend fun getTreeInfo(treeId: Long): TreeInfo? {
        val tree = repository.getTreeById(treeId) ?: return null
        val state = TreeState.fromString(tree.state)
        val type = TreeType.fromString(tree.treeType)
        
        return TreeInfo(
            entity = tree,
            state = state,
            type = type,
            progressPercent = treeCalculator.getProgressPercent(tree.investedPoints),
            daysUntilDeath = if (tree.isAlive) treeCalculator.getDaysUntilDeath(tree.lastWateredAt) else 0,
            pointsToNextLevel = treeCalculator.getPointsToNextLevel(tree.investedPoints)
        )
    }
}


data class TreeInfo(
    val entity: TreeEntity,
    val state: TreeState,
    val type: TreeType,
    val progressPercent: Float,
    val daysUntilDeath: Int,
    val pointsToNextLevel: Int?
)

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

/**
 * Manager quản lý tất cả logic liên quan đến điểm và cây
 */
class PointsManager(
    private val repository: GamificationRepository
) {
    companion object {
        const val STREAK_MULTIPLIER = 1.5f
    }
    
    private val treeCalculator = TreeGrowthCalculator()
    
    // ==================== Points Operations ====================
    
    /**
     * Lấy điểm hiện tại của user (Flow để observe changes)
     */
    fun getUserPoints(): Flow<UserPointsEntity?> = repository.getUserPoints()
    
    /**
     * Kiếm điểm từ một action
     * @return Số điểm thực tế được cộng (sau khi áp dụng streak bonus)
     */
    suspend fun earnPoints(action: PointAction): Int {
        val current = repository.getOrCreateUserPoints()
        
        // Áp dụng streak multiplier nếu đang có streak và action là kiếm điểm
        val hasStreak = current.currentStreak >= 3
        val multiplier = if (hasStreak && action.isEarning()) STREAK_MULTIPLIER else 1f
        val finalPoints = (action.points * multiplier).toInt()
        
        // Update total points
        val newTotal = (current.totalPoints + finalPoints).coerceAtLeast(0)
        repository.updateUserPoints(current.copy(totalPoints = newTotal))
        
        // Log history
        repository.addPointHistory(PointHistoryEntity(
            points = finalPoints,
            reason = action.name
        ))
        
        return finalPoints
    }
    
    /**
     * Dùng điểm (trừ điểm)
     * @return true nếu thành công, false nếu không đủ điểm
     */
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
    
    /**
     * Cập nhật streak khi user hoàn thành task trong ngày
     */
    suspend fun updateStreak() {
        val today = LocalDate.now().toString()
        val current = repository.getOrCreateUserPoints()
        
        // Đã active hôm nay rồi, không cần update
        if (current.lastActiveDate == today) return
        
        val yesterday = LocalDate.now().minusDays(1).toString()
        val newStreak = if (current.lastActiveDate == yesterday) {
            current.currentStreak + 1
        } else {
            1  // Reset streak
        }
        
        repository.updateUserPoints(current.copy(
            currentStreak = newStreak,
            bestStreak = maxOf(current.bestStreak, newStreak),
            lastActiveDate = today
        ))
        
        // Award streak bonuses
        when (newStreak) {
            3 -> earnPoints(PointAction.STREAK_BONUS_3)
            7 -> earnPoints(PointAction.STREAK_BONUS_7)
            30 -> earnPoints(PointAction.STREAK_BONUS_30)
        }
    }
    
    /**
     * Lấy lịch sử điểm
     */
    fun getPointHistory(): Flow<List<PointHistoryEntity>> = repository.getPointHistory()
    
    // ==================== Tree Operations ====================
    
    /**
     * Lấy danh sách cây còn sống
     */
    fun getAliveTrees(): Flow<List<TreeEntity>> = repository.getAliveTrees()
    
    /**
     * Trồng cây mới
     * @return ID của cây mới, null nếu không đủ điểm
     */
    suspend fun plantTree(type: TreeType, name: String = type.displayName): Long? {
        val current = repository.getOrCreateUserPoints()
        
        if (!treeCalculator.canPlantTree(current.totalPoints, type)) {
            return null
        }
        
        // Trừ điểm
        val success = spendPoints(type.costToPlant, "PLANT_${type.name}")
        if (!success) return null
        
        // Tạo cây mới
        val tree = TreeEntity(
            name = name,
            treeType = type.name,
            state = TreeState.SEED.name
        )
        
        return repository.plantTree(tree)
    }
    
    /**
     * Tưới cây (đầu tư điểm vào cây)
     * @return TreeState mới sau khi tưới, null nếu thất bại
     */
    suspend fun waterTree(treeId: Long, points: Int = 10): TreeState? {
        val tree = repository.getTreeById(treeId) ?: return null
        if (!tree.isAlive) return null
        
        // Trừ điểm
        val success = spendPoints(points, "WATER_TREE_$treeId")
        if (!success) return null
        
        // Cập nhật cây
        val newInvestedPoints = tree.investedPoints + points
        val newState = treeCalculator.calculateState(newInvestedPoints)
        
        repository.updateTree(tree.copy(
            investedPoints = newInvestedPoints,
            state = newState.name,
            lastWateredAt = System.currentTimeMillis()
        ))
        
        return newState
    }
    
    /**
     * Kiểm tra và cập nhật cây chết (gọi mỗi khi mở app)
     */
    suspend fun checkAndUpdateDeadTrees() {
        val trees = repository.getAliveTrees().first()
        
        trees.forEach { tree ->
            if (treeCalculator.shouldDie(tree.lastWateredAt)) {
                repository.killTree(tree.id)
            }
        }
    }
    
    /**
     * Lấy thông tin chi tiết của một cây
     */
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

/**
 * Data class chứa thông tin đầy đủ của một cây
 */
data class TreeInfo(
    val entity: TreeEntity,
    val state: TreeState,
    val type: TreeType,
    val progressPercent: Float,
    val daysUntilDeath: Int,
    val pointsToNextLevel: Int?
)

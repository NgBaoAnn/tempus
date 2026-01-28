package com.projectapp.tempus.data.gamification

import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local Repository cho Gamification - chỉ làm việc với Room
 */
class LocalGamificationRepository(
    private val dao: GamificationDao
) {
    // ==================== User Points ====================
    
    fun getUserPoints(): Flow<UserPointsEntity?> = dao.getUserPoints()
    
    suspend fun getUserPointsOnce(): UserPointsEntity? = dao.getUserPointsOnce()
    
    suspend fun updateUserPoints(points: UserPointsEntity) = dao.updateUserPoints(points)
    
    // ==================== Point History ====================
    
    suspend fun addPointHistory(history: PointHistoryEntity) = dao.addPointHistory(history)
    
    fun getPointHistory(): Flow<List<PointHistoryEntity>> = dao.getPointHistory()
    
    suspend fun getPointsEarnedSince(startTime: Long): Int = dao.getPointsEarnedSince(startTime) ?: 0
    
    // ==================== Trees ====================
    
    fun getAliveTrees(): Flow<List<TreeEntity>> = dao.getAliveTrees()
    
    fun getAllTrees(): Flow<List<TreeEntity>> = dao.getAllTrees()
    
    suspend fun getTreeById(treeId: Long): TreeEntity? = dao.getTreeById(treeId)
    
    suspend fun getAliveTreeCount(): Int = dao.getAliveTreeCount()
    
    suspend fun plantTree(tree: TreeEntity): Long = dao.plantTree(tree)
    
    suspend fun updateTree(tree: TreeEntity) = dao.updateTree(tree)
    
    suspend fun killTree(treeId: Long) = dao.killTree(treeId)
    
    // ==================== Clear Data (for logout) ====================
    
    suspend fun clearAllData() {
        // Note: GamificationDao cần thêm các @Query DELETE
        // Tạm thời để trống - gamification data có thể giữ lại vì có userId
    }
}

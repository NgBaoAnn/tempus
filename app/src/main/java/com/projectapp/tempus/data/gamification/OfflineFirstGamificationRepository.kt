package com.projectapp.tempus.data.gamification

import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import kotlinx.coroutines.flow.Flow


class OfflineFirstGamificationRepository(
    private val localRepo: LocalGamificationRepository
) : GamificationRepository {
    
    
    override fun getUserPoints(): Flow<UserPointsEntity?> = localRepo.getUserPoints()
    
    override suspend fun getUserPointsOnce(): UserPointsEntity? = localRepo.getUserPointsOnce()
    
    override suspend fun getOrCreateUserPoints(): UserPointsEntity {
        val existing = localRepo.getUserPointsOnce()
        if (existing != null) return existing
        
        val newPoints = UserPointsEntity(
            id = "current_user",
            totalPoints = 0,
            currentStreak = 0,
            level = 1
        )
        localRepo.updateUserPoints(newPoints)
        return newPoints
    }
    
    override suspend fun updateUserPoints(points: UserPointsEntity) {
        localRepo.updateUserPoints(points)
    }
    
    
    override suspend fun addPointHistory(history: PointHistoryEntity) {
        localRepo.addPointHistory(history)
    }
    
    override fun getPointHistory(): Flow<List<PointHistoryEntity>> = localRepo.getPointHistory()
    
    
    override fun getAliveTrees(): Flow<List<TreeEntity>> = localRepo.getAliveTrees()
    
    override fun getAllTrees(): Flow<List<TreeEntity>> = localRepo.getAllTrees()
    
    override suspend fun getTreeById(treeId: Long): TreeEntity? = localRepo.getTreeById(treeId)
    
    override suspend fun getAliveTreeCount(): Int = localRepo.getAliveTreeCount()
    
    override suspend fun plantTree(tree: TreeEntity): Long = localRepo.plantTree(tree)
    
    override suspend fun updateTree(tree: TreeEntity) = localRepo.updateTree(tree)
    
    override suspend fun killTree(treeId: Long) = localRepo.killTree(treeId)
}

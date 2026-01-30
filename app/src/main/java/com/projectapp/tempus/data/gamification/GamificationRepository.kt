package com.projectapp.tempus.data.gamification

import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import kotlinx.coroutines.flow.Flow


interface GamificationRepository {
    
    
    fun getUserPoints(): Flow<UserPointsEntity?>
    
    
    suspend fun getUserPointsOnce(): UserPointsEntity?
    
    
    suspend fun getOrCreateUserPoints(): UserPointsEntity
    
    
    suspend fun updateUserPoints(points: UserPointsEntity)
    
    
    suspend fun addPointHistory(history: PointHistoryEntity)
    
    
    fun getPointHistory(): Flow<List<PointHistoryEntity>>
    
    
    fun getAliveTrees(): Flow<List<TreeEntity>>
    
    
    fun getAllTrees(): Flow<List<TreeEntity>>
    
    
    suspend fun getTreeById(treeId: Long): TreeEntity?
    
    
    suspend fun getAliveTreeCount(): Int
    
    
    suspend fun plantTree(tree: TreeEntity): Long
    
    
    suspend fun updateTree(tree: TreeEntity)
    
    
    suspend fun killTree(treeId: Long)
}

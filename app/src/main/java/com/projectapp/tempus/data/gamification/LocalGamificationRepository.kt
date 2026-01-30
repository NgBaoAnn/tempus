package com.projectapp.tempus.data.gamification

import android.util.Log
import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import kotlinx.coroutines.flow.Flow


class LocalGamificationRepository(
    private val dao: GamificationDao
) {
    
    
    fun getUserPoints(): Flow<UserPointsEntity?> = dao.getUserPoints()
    
    suspend fun getUserPointsOnce(): UserPointsEntity? = dao.getUserPointsOnce()
    
    suspend fun updateUserPoints(points: UserPointsEntity) {
        Log.d("LocalGamRepo", "updateUserPoints: id=${points.id}, totalPoints=${points.totalPoints}")
        dao.updateUserPoints(points)
        Log.d("LocalGamRepo", "updateUserPoints: DONE")
    }
    
    
    suspend fun addPointHistory(history: PointHistoryEntity) {
        Log.d("LocalGamRepo", "addPointHistory: points=${history.points}, reason=${history.reason}")
        dao.addPointHistory(history)
    }
    
    fun getPointHistory(): Flow<List<PointHistoryEntity>> = dao.getPointHistory()
    
    fun getPomodoroHistory(): Flow<List<PointHistoryEntity>> = dao.getPomodoroHistory()
    
    suspend fun getPomodoroHistoryInRange(startTime: Long, endTime: Long): List<PointHistoryEntity> = 
        dao.getPomodoroHistoryInRange(startTime, endTime)
    
    suspend fun getPointsEarnedSince(startTime: Long): Int = dao.getPointsEarnedSince(startTime) ?: 0
    
    suspend fun clearPointHistory() {
        Log.d("LocalGamRepo", "clearPointHistory: Clearing all point history for sync")
        dao.clearPointHistory()
    }
    
    suspend fun insertPointHistoryWithReplace(history: PointHistoryEntity) {
        dao.insertPointHistoryWithReplace(history)
    }
    
    
    fun getAliveTrees(): Flow<List<TreeEntity>> = dao.getAliveTrees()
    
    fun getAllTrees(): Flow<List<TreeEntity>> = dao.getAllTrees()
    
    suspend fun getTreeById(treeId: Long): TreeEntity? = dao.getTreeById(treeId)
    
    suspend fun getAliveTreeCount(): Int = dao.getAliveTreeCount()
    
    suspend fun plantTree(tree: TreeEntity): Long = dao.plantTree(tree)
    
    suspend fun updateTree(tree: TreeEntity) = dao.updateTree(tree)
    
    suspend fun killTree(treeId: Long) = dao.killTree(treeId)
    
    
    suspend fun clearAllData() {
        
        
    }
}

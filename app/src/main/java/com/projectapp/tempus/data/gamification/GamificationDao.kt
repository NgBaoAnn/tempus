package com.projectapp.tempus.data.gamification

import androidx.room.*
import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho các operations liên quan đến Gamification
 */
@Dao
interface GamificationDao {
    
    // ==================== User Points ====================
    
    @Query("SELECT * FROM user_points WHERE id = 'current_user'")
    fun getUserPoints(): Flow<UserPointsEntity?>
    
    @Query("SELECT * FROM user_points WHERE id = 'current_user'")
    suspend fun getUserPointsOnce(): UserPointsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserPoints(points: UserPointsEntity)
    
    // ==================== Point History ====================
    
    @Insert
    suspend fun addPointHistory(history: PointHistoryEntity)
    
    @Query("SELECT * FROM point_history ORDER BY timestamp DESC LIMIT 50")
    fun getPointHistory(): Flow<List<PointHistoryEntity>>
    
    @Query("SELECT SUM(points) FROM point_history WHERE timestamp >= :startTime")
    suspend fun getPointsEarnedSince(startTime: Long): Int?
    
    // ==================== Trees ====================
    
    @Query("SELECT * FROM trees WHERE isAlive = 1 ORDER BY createdAt DESC")
    fun getAliveTrees(): Flow<List<TreeEntity>>
    
    @Query("SELECT * FROM trees ORDER BY createdAt DESC")
    fun getAllTrees(): Flow<List<TreeEntity>>
    
    @Query("SELECT * FROM trees WHERE id = :treeId")
    suspend fun getTreeById(treeId: Long): TreeEntity?
    
    @Query("SELECT COUNT(*) FROM trees WHERE isAlive = 1")
    suspend fun getAliveTreeCount(): Int
    
    @Insert
    suspend fun plantTree(tree: TreeEntity): Long
    
    @Update
    suspend fun updateTree(tree: TreeEntity)
    
    @Query("UPDATE trees SET state = 'DEAD', isAlive = 0 WHERE id = :treeId")
    suspend fun killTree(treeId: Long)
}

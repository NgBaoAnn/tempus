package com.projectapp.tempus.data.gamification

import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface cho các operations liên quan đến Gamification
 * Có thể implement bằng Room (local) hoặc Supabase (cloud)
 */
interface GamificationRepository {
    
    // ==================== User Points ====================
    
    /**
     * Lấy điểm của user hiện tại (Flow để observe changes)
     */
    fun getUserPoints(): Flow<UserPointsEntity?>
    
    /**
     * Lấy điểm của user một lần (suspend function)
     */
    suspend fun getUserPointsOnce(): UserPointsEntity?
    
    /**
     * Lấy hoặc tạo mới user points nếu chưa có
     */
    suspend fun getOrCreateUserPoints(): UserPointsEntity
    
    /**
     * Cập nhật điểm của user
     */
    suspend fun updateUserPoints(points: UserPointsEntity)
    
    // ==================== Point History ====================
    
    /**
     * Thêm lịch sử điểm
     */
    suspend fun addPointHistory(history: PointHistoryEntity)
    
    /**
     * Lấy lịch sử điểm (50 records gần nhất)
     */
    fun getPointHistory(): Flow<List<PointHistoryEntity>>
    
    // ==================== Trees ====================
    
    /**
     * Lấy danh sách cây còn sống
     */
    fun getAliveTrees(): Flow<List<TreeEntity>>
    
    /**
     * Lấy tất cả cây
     */
    fun getAllTrees(): Flow<List<TreeEntity>>
    
    /**
     * Lấy thông tin một cây theo ID
     */
    suspend fun getTreeById(treeId: Long): TreeEntity?
    
    /**
     * Đếm số cây còn sống
     */
    suspend fun getAliveTreeCount(): Int
    
    /**
     * Trồng cây mới
     * @return ID của cây mới
     */
    suspend fun plantTree(tree: TreeEntity): Long
    
    /**
     * Cập nhật thông tin cây
     */
    suspend fun updateTree(tree: TreeEntity)
    
    /**
     * Đánh dấu cây là đã chết
     */
    suspend fun killTree(treeId: Long)
}

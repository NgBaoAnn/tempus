package com.projectapp.tempus.data.gamification

import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.gamification.dto.KillTreeDto
import com.projectapp.tempus.data.gamification.dto.PointHistoryDto
import com.projectapp.tempus.data.gamification.dto.TreeDto
import com.projectapp.tempus.data.gamification.dto.TreeUpdateDto
import com.projectapp.tempus.data.gamification.dto.UserPointsDto
import com.projectapp.tempus.data.gamification.dto.toDto
import com.projectapp.tempus.data.gamification.dto.toEntity
import com.projectapp.tempus.data.gamification.dto.toUpdateDto
import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation của GamificationRepository sử dụng Supabase
 */
class SupabaseGamificationRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : GamificationRepository {
    
    /**
     * Lấy user ID hiện tại, return null nếu chưa đăng nhập
     */
    private fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }
    
    // ==================== User Points ====================
    
    override fun getUserPoints(): Flow<UserPointsEntity?> = flow {
        while (true) {
            val result = try {
                getUserPointsOnce()
            } catch (e: Exception) {
                android.util.Log.e("GamificationRepo", "Error getting user points: ${e.message}")
                null
            }
            emit(result)
            delay(5000) // Poll every 5 seconds
        }
    }
    
    override suspend fun getUserPointsOnce(): UserPointsEntity? {
        val userId = getCurrentUserId() ?: return null
        
        return try {
            val result = supabase.from("user_points")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserPointsDto>()
            
            result.firstOrNull()?.toEntity()
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error fetching user points: ${e.message}")
            null
        }
    }
    
    override suspend fun getOrCreateUserPoints(): UserPointsEntity {
        val userId = getCurrentUserId() 
            ?: return UserPointsEntity() // Return default nếu chưa login
        
        return try {
            val existing = getUserPointsOnce()
            if (existing != null) return existing
            
            // Tạo mới nếu chưa có
            val newPoints = UserPointsDto(userId = userId)
            
            supabase.from("user_points")
                .insert(newPoints)
            
            UserPointsEntity()
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error in getOrCreateUserPoints: ${e.message}")
            UserPointsEntity()
        }
    }
    
    override suspend fun updateUserPoints(points: UserPointsEntity) {
        val userId = getCurrentUserId() ?: return
        
        android.util.Log.d("GamificationRepo", "updateUserPoints called with totalPoints=${points.totalPoints}")
        
        try {
            // Kiểm tra xem đã có record chưa
            val existing = getUserPointsOnce()
            android.util.Log.d("GamificationRepo", "Existing points: ${existing?.totalPoints}")
            
            if (existing == null) {
                // Insert mới
                android.util.Log.d("GamificationRepo", "Inserting new user_points record")
                supabase.from("user_points")
                    .insert(points.toDto(userId))
            } else {
                // Update
                android.util.Log.d("GamificationRepo", "Updating user_points: new total=${points.totalPoints}")
                supabase.from("user_points")
                    .update(points.toUpdateDto()) {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                android.util.Log.d("GamificationRepo", "Update completed successfully")
            }
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error updating user points: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // ==================== Point History ====================
    
    override suspend fun addPointHistory(history: PointHistoryEntity) {
        val userId = getCurrentUserId() ?: return
        
        try {
            supabase.from("point_history")
                .insert(history.toDto(userId))
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error adding point history: ${e.message}")
        }
    }
    
    override fun getPointHistory(): Flow<List<PointHistoryEntity>> = flow {
        while (true) {
            val userId = getCurrentUserId()
            if (userId == null) {
                emit(emptyList())
                delay(5000)
                continue
            }
            
            val result = try {
                supabase.from("point_history")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                        order("timestamp", Order.DESCENDING)
                        limit(50)
                    }
                    .decodeList<PointHistoryDto>()
                    .map { it.toEntity() }
            } catch (e: Exception) {
                android.util.Log.e("GamificationRepo", "Error getting point history: ${e.message}")
                emptyList()
            }
            
            emit(result)
            delay(5000)
        }
    }
    
    // ==================== Trees ====================
    
    override fun getAliveTrees(): Flow<List<TreeEntity>> = flow {
        while (true) {
            val userId = getCurrentUserId()
            if (userId == null) {
                emit(emptyList())
                delay(5000)
                continue
            }
            
            val result = try {
                supabase.from("trees")
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("is_alive", true)
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<TreeDto>()
                    .map { it.toEntity() }
            } catch (e: Exception) {
                android.util.Log.e("GamificationRepo", "Error getting alive trees: ${e.message}")
                emptyList()
            }
            
            emit(result)
            delay(5000)
        }
    }
    
    /**
     * Get alive trees once (no polling) - for immediate refresh
     */
    suspend fun getAliveTreesOnce(): List<TreeEntity> {
        val userId = getCurrentUserId() ?: return emptyList()
        
        return try {
            supabase.from("trees")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_alive", true)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<TreeDto>()
                .map { it.toEntity() }
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error getting alive trees once: ${e.message}")
            emptyList()
        }
    }
    
    override fun getAllTrees(): Flow<List<TreeEntity>> = flow {
        while (true) {
            val userId = getCurrentUserId()
            if (userId == null) {
                emit(emptyList())
                delay(5000)
                continue
            }
            
            val result = try {
                supabase.from("trees")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<TreeDto>()
                    .map { it.toEntity() }
            } catch (e: Exception) {
                android.util.Log.e("GamificationRepo", "Error getting all trees: ${e.message}")
                emptyList()
            }
            
            emit(result)
            delay(5000)
        }
    }
    
    override suspend fun getTreeById(treeId: Long): TreeEntity? {
        val userId = getCurrentUserId() ?: return null
        
        return try {
            val result = supabase.from("trees")
                .select {
                    filter {
                        eq("id", treeId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<TreeDto>()
            
            result.firstOrNull()?.toEntity()
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error getting tree by id: ${e.message}")
            null
        }
    }
    
    override suspend fun getAliveTreeCount(): Int {
        val userId = getCurrentUserId() ?: return 0
        
        return try {
            val result = supabase.from("trees")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_alive", true)
                    }
                }
                .decodeList<TreeDto>()
            
            result.size
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error getting alive tree count: ${e.message}")
            0
        }
    }
    
    override suspend fun plantTree(tree: TreeEntity): Long {
        val userId = getCurrentUserId() ?: return 0
        
        return try {
            val result = supabase.from("trees")
                .insert(tree.toDto(userId)) {
                    select()
                }
                .decodeSingle<TreeDto>()
            
            result.id ?: 0
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error planting tree: ${e.message}")
            0
        }
    }
    
    override suspend fun updateTree(tree: TreeEntity) {
        val userId = getCurrentUserId() ?: return
        
        try {
            supabase.from("trees")
                .update(tree.toUpdateDto()) {
                    filter {
                        eq("id", tree.id)
                        eq("user_id", userId)
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error updating tree: ${e.message}")
        }
    }
    
    override suspend fun killTree(treeId: Long) {
        val userId = getCurrentUserId() ?: return
        
        try {
            supabase.from("trees")
                .update(KillTreeDto()) {
                    filter {
                        eq("id", treeId)
                        eq("user_id", userId)
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error killing tree: ${e.message}")
        }
    }
    
    /**
     * Xóa cây hoàn toàn khỏi database
     */
    suspend fun deleteTree(treeId: Long) {
        val userId = getCurrentUserId() ?: return
        
        try {
            supabase.from("trees")
                .delete {
                    filter {
                        eq("id", treeId)
                        eq("user_id", userId)
                    }
                }
            android.util.Log.d("GamificationRepo", "Tree $treeId deleted successfully")
        } catch (e: Exception) {
            android.util.Log.e("GamificationRepo", "Error deleting tree: ${e.message}")
            throw e
        }
    }
}

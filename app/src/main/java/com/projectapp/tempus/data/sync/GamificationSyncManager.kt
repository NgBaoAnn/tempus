package com.projectapp.tempus.data.sync

import android.util.Log
import com.projectapp.tempus.data.gamification.LocalGamificationRepository
import com.projectapp.tempus.data.gamification.SupabaseGamificationRepository
import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import kotlinx.coroutines.flow.first

/**
 * Sync Manager cho Gamification data (points, trees, history)
 * Push local Room data lên Supabase khi logout
 */
class GamificationSyncManager(
    private val localRepo: LocalGamificationRepository,
    private val remoteRepo: SupabaseGamificationRepository
) {
    companion object {
        private const val TAG = "GamificationSync"
    }
    
    /**
     * Push tất cả gamification data từ Room lên Supabase
     * Gọi trước khi logout
     */
    suspend fun pushToServer(): Result<GamificationSyncResult> {
        return try {
            var pointsSynced = false
            var treesSynced = 0
            var historySynced = 0
            
            // 1. Sync User Points
            val localPoints = localRepo.getUserPointsOnce()
            if (localPoints != null) {
                try {
                    remoteRepo.updateUserPoints(localPoints)
                    pointsSynced = true
                    Log.d(TAG, "Synced user points: ${localPoints.totalPoints}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync user points", e)
                }
            }
            
            // 2. Sync Trees using upsert to avoid duplicate key errors
            val localTrees = localRepo.getAliveTrees().first()
            for (tree in localTrees) {
                try {
                    remoteRepo.upsertTree(tree)
                    treesSynced++
                    Log.d(TAG, "Upserted tree: ${tree.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync tree ${tree.id}", e)
                }
            }
            
            // 3. Sync Point History (last 50 records)
            val localHistory = localRepo.getPointHistory().first()
            for (history in localHistory.take(50)) {
                try {
                    remoteRepo.addPointHistory(history)
                    historySynced++
                } catch (e: Exception) {
                    // Ignore duplicate errors
                    Log.d(TAG, "Point history may already exist: ${history.id}")
                }
            }
            
            val result = GamificationSyncResult(
                pointsSynced = pointsSynced,
                treesSynced = treesSynced,
                historySynced = historySynced
            )
            
            Log.d(TAG, "Sync completed: $result")
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Pull gamification data từ Supabase về Room
     * Gọi sau khi login
     */
    suspend fun pullFromServer(): Result<GamificationSyncResult> {
        return try {
            var pointsSynced = false
            var treesSynced = 0
            
            Log.d(TAG, "=== GAMIFICATION PULL START ===")
            
            // 1. Pull User Points
            Log.d(TAG, "Fetching user points from Supabase...")
            val serverPoints = remoteRepo.getUserPointsOnce()
            Log.d(TAG, "Server points: ${serverPoints?.totalPoints ?: "null"}")
            
            if (serverPoints != null) {
                Log.d(TAG, "Saving to Room: totalPoints=${serverPoints.totalPoints}, level=${serverPoints.level}")
                localRepo.updateUserPoints(serverPoints)
                pointsSynced = true
                Log.d(TAG, "Pulled user points: ${serverPoints.totalPoints}")
                
                // Verify saved
                val savedPoints = localRepo.getUserPointsOnce()
                Log.d(TAG, "Verification - Room now has: ${savedPoints?.totalPoints ?: "null"}")
            } else {
                Log.d(TAG, "No user points found on server")
            }
            
            // 2. Pull Trees
            Log.d(TAG, "Fetching trees from Supabase...")
            val serverTrees = remoteRepo.getAliveTreesOnce()
            Log.d(TAG, "Found ${serverTrees.size} trees on server")
            
            for (tree in serverTrees) {
                try {
                    val existingTree = localRepo.getTreeById(tree.id)
                    if (existingTree == null) {
                        localRepo.plantTree(tree)
                        Log.d(TAG, "Inserted tree: ${tree.id}")
                    } else {
                        localRepo.updateTree(tree)
                        Log.d(TAG, "Updated tree: ${tree.id}")
                    }
                    treesSynced++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to pull tree ${tree.id}", e)
                }
            }
            
            val result = GamificationSyncResult(
                pointsSynced = pointsSynced,
                treesSynced = treesSynced,
                historySynced = 0
            )
            
            Log.d(TAG, "=== GAMIFICATION PULL COMPLETED: $result ===")
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Pull failed", e)
            Result.failure(e)
        }
    }
}

/**
 * Kết quả sync gamification
 */
data class GamificationSyncResult(
    val pointsSynced: Boolean,
    val treesSynced: Int,
    val historySynced: Int
) {
    fun summary(): String {
        return "Points: $pointsSynced, Trees: $treesSynced, History: $historySynced"
    }
}

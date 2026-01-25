package com.projectapp.tempus.data.focus

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for blocked apps operations
 */
@Dao
interface BlockedAppDao {
    
    @Query("SELECT * FROM blocked_apps ORDER BY addedAt DESC")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>
    
    @Query("SELECT * FROM blocked_apps")
    suspend fun getAllBlockedAppsSync(): List<BlockedAppEntity>
    
    @Query("SELECT packageName FROM blocked_apps")
    suspend fun getAllBlockedPackages(): List<String>
    
    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE packageName = :packageName)")
    suspend fun isAppBlocked(packageName: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedAppEntity)
    
    @Delete
    suspend fun deleteBlockedApp(app: BlockedAppEntity)
    
    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
    
    @Query("DELETE FROM blocked_apps")
    suspend fun clearAll()
}

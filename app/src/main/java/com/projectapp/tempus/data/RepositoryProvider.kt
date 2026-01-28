package com.projectapp.tempus.data

import android.content.Context
import com.projectapp.tempus.data.local.LocalScheduleRepository
import com.projectapp.tempus.data.local.TempusDatabase
import com.projectapp.tempus.data.schedule.OfflineFirstScheduleRepository
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.data.sync.SyncManager

/**
 * Simple dependency provider for repositories
 * Centralizes creation of repositories and ensures proper singleton behavior
 * 
 * Usage:
 * val repo = RepositoryProvider.getScheduleRepository(context)
 * val syncManager = RepositoryProvider.getSyncManager(context)
 */
object RepositoryProvider {
    
    @Volatile
    private var scheduleRepository: OfflineFirstScheduleRepository? = null
    
    @Volatile
    private var localRepository: LocalScheduleRepository? = null
    
    @Volatile
    private var remoteRepository: SupabaseScheduleRepository? = null
    
    @Volatile
    private var syncManager: SyncManager? = null
    
    /**
     * Get the offline-first schedule repository (main repository for UI)
     */
    fun getScheduleRepository(context: Context): OfflineFirstScheduleRepository {
        return scheduleRepository ?: synchronized(this) {
            scheduleRepository ?: createScheduleRepository(context).also {
                scheduleRepository = it
            }
        }
    }
    
    /**
     * Get local repository (Room operations only)
     */
    fun getLocalRepository(context: Context): LocalScheduleRepository {
        return localRepository ?: synchronized(this) {
            localRepository ?: createLocalRepository(context).also {
                localRepository = it
            }
        }
    }
    
    /**
     * Get remote repository (Supabase operations, for sync only)
     */
    fun getRemoteRepository(): SupabaseScheduleRepository {
        return remoteRepository ?: synchronized(this) {
            remoteRepository ?: SupabaseScheduleRepository().also {
                remoteRepository = it
            }
        }
    }
    
    /**
     * Get sync manager
     */
    fun getSyncManager(context: Context): SyncManager {
        return syncManager ?: synchronized(this) {
            syncManager ?: createSyncManager(context).also {
                syncManager = it
            }
        }
    }
    
    /**
     * Clear all instances (call on logout)
     */
    fun clear() {
        scheduleRepository = null
        localRepository = null
        syncManager = null
        // Note: Don't clear remoteRepository as it's stateless
    }
    
    // ==================== PRIVATE FACTORY METHODS ====================
    
    private fun createScheduleRepository(context: Context): OfflineFirstScheduleRepository {
        val localRepo = getLocalRepository(context)
        return OfflineFirstScheduleRepository(localRepo)
    }
    
    private fun createLocalRepository(context: Context): LocalScheduleRepository {
        val database = TempusDatabase.getDatabase(context)
        return LocalScheduleRepository(database.scheduleDao())
    }
    
    private fun createSyncManager(context: Context): SyncManager {
        val localRepo = getLocalRepository(context)
        val remoteRepo = getRemoteRepository()
        return SyncManager(localRepo, remoteRepo)
    }
}

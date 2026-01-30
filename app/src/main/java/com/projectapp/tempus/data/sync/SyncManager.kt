package com.projectapp.tempus.data.sync

import android.util.Log
import com.projectapp.tempus.data.local.LocalScheduleRepository
import com.projectapp.tempus.data.local.entity.*
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SyncManager(
    private val localRepo: LocalScheduleRepository,
    private val remoteRepo: SupabaseScheduleRepository
) {
    companion object {
        private const val TAG = "SyncManager"
    }
    
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    
    suspend fun pullFromServer(userId: String): Result<Int> {
        Log.d(TAG, "Starting pull from server for user: $userId")
        _syncState.update { it.copy(isSyncing = true, error = null, progress = SyncProgress("Đang tải dữ liệu...", 0, 3)) }
        
        return try {
            
            _syncState.update { it.copy(progress = SyncProgress("Đang tải lịch trình...", 1, 3)) }
            val remoteSchedules = remoteRepo.getAllSchedules(userId)
            Log.d(TAG, "Fetched ${remoteSchedules.size} schedules from server")
            
            
            _syncState.update { it.copy(progress = SyncProgress("Đang tải trạng thái...", 2, 3)) }
            val taskIds = remoteSchedules.map { it.id }
            val remoteItems = if (taskIds.isNotEmpty()) {
                
                val today = java.time.LocalDate.now()
                val startDate = today.minusDays(30).toString()
                val endDate = today.plusDays(30).toString()
                remoteRepo.getScheduleItemsByRange(startDate, endDate, taskIds)
            } else emptyList()
            Log.d(TAG, "Fetched ${remoteItems.size} schedule items from server")
            
            
            _syncState.update { it.copy(progress = SyncProgress("Đang tải công việc con...", 3, 3)) }
            val remoteSubTasks = if (taskIds.isNotEmpty()) {
                remoteRepo.getSubTasksBatch(taskIds)
            } else emptyList()
            Log.d(TAG, "Fetched ${remoteSubTasks.size} subtasks from server")
            
            
            val scheduleEntities = remoteSchedules.map { ScheduleEntity.fromRow(it) }
            val itemEntities = remoteItems.map { ScheduleItemEntity.fromRow(it) }
            val subTaskEntities = remoteSubTasks.map { SubTaskEntity.fromRow(it) }
            
            localRepo.replaceAllData(
                userId = userId,
                schedules = scheduleEntities,
                items = itemEntities,
                subTasks = subTaskEntities
            )
            
            val totalItems = scheduleEntities.size + itemEntities.size + subTaskEntities.size
            _syncState.update { 
                it.copy(
                    isSyncing = false, 
                    lastSyncTime = System.currentTimeMillis(),
                    pendingChanges = 0,
                    progress = null
                ) 
            }
            
            Log.d(TAG, "Pull complete. Total items synced: $totalItems")
            Result.success(totalItems)
            
        } catch (e: Exception) {
            Log.e(TAG, "Pull failed", e)
            _syncState.update { 
                it.copy(
                    isSyncing = false, 
                    error = e.message ?: "Đồng bộ thất bại",
                    progress = null
                ) 
            }
            Result.failure(e)
        }
    }
    
    
    suspend fun pushToServer(): Result<SyncResult> {
        Log.d(TAG, "Starting push to server")
        _syncState.update { it.copy(isSyncing = true, error = null) }
        
        val result = SyncResult()
        
        return try {
            
            val pendingSchedules = localRepo.getPendingSchedules()
            val pendingItems = localRepo.getPendingItems()
            val pendingSubTasks = localRepo.getPendingSubTasks()
            
            val totalPending = pendingSchedules.size + pendingItems.size + pendingSubTasks.size
            Log.d(TAG, "Found $totalPending pending changes")
            
            if (totalPending == 0) {
                _syncState.update { 
                    it.copy(
                        isSyncing = false, 
                        lastSyncTime = System.currentTimeMillis()
                    ) 
                }
                return Result.success(result)
            }
            
            var processed = 0
            
            
            for (schedule in pendingSchedules) {
                processed++
                _syncState.update { 
                    it.copy(progress = SyncProgress("Đang đồng bộ lịch trình...", processed, totalPending)) 
                }
                
                try {
                    when (schedule.syncStatus) {
                        SyncStatus.PENDING_CREATE.name -> {
                            remoteRepo.insertSchedule(schedule.toInsertMap())
                            localRepo.markScheduleSynced(schedule.id)
                            result.schedulesCreated++
                            Log.d(TAG, "Created schedule: ${schedule.id}")
                        }
                        SyncStatus.PENDING_UPDATE.name -> {
                            
                            val remote = remoteRepo.getScheduleById(schedule.id)
                            if (remote == null || shouldLocalWin(schedule, remote)) {
                                remoteRepo.updateSchedule(schedule.id, schedule.toUpdateMap())
                                Log.d(TAG, "Updated schedule: ${schedule.id}")
                            } else {
                                Log.d(TAG, "Server version is newer, skipping update for: ${schedule.id}")
                            }
                            localRepo.markScheduleSynced(schedule.id)
                            result.schedulesUpdated++
                        }
                        SyncStatus.PENDING_DELETE.name -> {
                            remoteRepo.deleteSchedule(schedule.id)
                            localRepo.hardDeleteSchedule(schedule.id)
                            result.schedulesDeleted++
                            Log.d(TAG, "Deleted schedule: ${schedule.id}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync schedule: ${schedule.id}", e)
                    result.errors.add("Schedule ${schedule.name}: ${e.message}")
                }
            }
            
            
            for (item in pendingItems) {
                processed++
                _syncState.update { 
                    it.copy(progress = SyncProgress("Đang đồng bộ trạng thái...", processed, totalPending)) 
                }
                
                try {
                    when (item.syncStatus) {
                        SyncStatus.PENDING_CREATE.name, SyncStatus.PENDING_UPDATE.name -> {
                            remoteRepo.upsertScheduleItem(
                                item.taskId,
                                item.date,
                                item.toStatusType()
                            )
                            localRepo.markItemSynced(item.id)
                            result.itemsUpdated++
                        }
                        
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync item: ${item.id}", e)
                    result.errors.add("Item ${item.date}: ${e.message}")
                }
            }
            
            
            for (subTask in pendingSubTasks) {
                processed++
                _syncState.update { 
                    it.copy(progress = SyncProgress("Đang đồng bộ công việc con...", processed, totalPending)) 
                }
                
                try {
                    when (subTask.syncStatus) {
                        SyncStatus.PENDING_CREATE.name -> {
                            
                            
                            localRepo.markSubTaskSynced(subTask.id)
                            result.subTasksCreated++
                        }
                        SyncStatus.PENDING_UPDATE.name -> {
                            remoteRepo.updateSubTaskStatus(subTask.id, subTask.isDone)
                            localRepo.markSubTaskSynced(subTask.id)
                            result.subTasksUpdated++
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync subtask: ${subTask.id}", e)
                    result.errors.add("SubTask ${subTask.title}: ${e.message}")
                }
            }
            
            _syncState.update { 
                it.copy(
                    isSyncing = false, 
                    lastSyncTime = System.currentTimeMillis(),
                    pendingChanges = 0,
                    progress = null,
                    error = if (result.hasErrors) "Một số mục đồng bộ thất bại" else null
                ) 
            }
            
            Log.d(TAG, "Push complete. Result: ${result.summary()}")
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Push failed", e)
            _syncState.update { 
                it.copy(
                    isSyncing = false, 
                    error = e.message ?: "Đồng bộ thất bại",
                    progress = null
                ) 
            }
            Result.failure(e)
        }
    }
    
    
    suspend fun fullSync(userId: String): Result<Unit> {
        Log.d(TAG, "Starting full sync for user: $userId")
        
        
        val pushResult = pushToServer()
        if (pushResult.isFailure) {
            return Result.failure(pushResult.exceptionOrNull() ?: Exception("Push failed"))
        }
        
        
        val pullResult = pullFromServer(userId)
        if (pullResult.isFailure) {
            return Result.failure(pullResult.exceptionOrNull() ?: Exception("Pull failed"))
        }
        
        return Result.success(Unit)
    }
    
    
    private fun shouldLocalWin(local: ScheduleEntity, remote: com.projectapp.tempus.data.schedule.dto.ScheduleRow): Boolean {
        val remoteTime = remote.createdAt?.let { 
            try {
                java.time.OffsetDateTime.parse(it).toInstant().toEpochMilli()
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
        
        return local.localUpdatedAt > remoteTime
    }
    
    
    fun updatePendingCount(count: Int) {
        _syncState.update { it.copy(pendingChanges = count) }
    }
    
    
    fun clearError() {
        _syncState.update { it.copy(error = null) }
    }
}

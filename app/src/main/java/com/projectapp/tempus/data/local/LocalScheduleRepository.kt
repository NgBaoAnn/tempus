package com.projectapp.tempus.data.local

import com.projectapp.tempus.data.local.dao.ScheduleDao
import com.projectapp.tempus.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID


class LocalScheduleRepository(
    private val scheduleDao: ScheduleDao
) {
    
    
    suspend fun getAllSchedules(userId: String): List<ScheduleEntity> {
        return scheduleDao.getActiveSchedules(userId)
    }
    
    fun getAllSchedulesFlow(userId: String): Flow<List<ScheduleEntity>> {
        return scheduleDao.getAllSchedulesFlow(userId)
    }
    
    suspend fun getScheduleById(id: String): ScheduleEntity? {
        return scheduleDao.getScheduleById(id)
    }
    
    
    suspend fun insertSchedule(entity: ScheduleEntity): ScheduleEntity {
        val withSyncStatus = entity.copy(
            id = if (entity.id.isBlank()) UUID.randomUUID().toString() else entity.id,
            syncStatus = SyncStatus.PENDING_CREATE.name,
            localUpdatedAt = System.currentTimeMillis()
        )
        scheduleDao.insertSchedule(withSyncStatus)
        return withSyncStatus
    }
    
    
    suspend fun updateSchedule(entity: ScheduleEntity): ScheduleEntity {
        val withSyncStatus = entity.copy(
            syncStatus = if (entity.syncStatus == SyncStatus.PENDING_CREATE.name) {
                
                SyncStatus.PENDING_CREATE.name
            } else {
                SyncStatus.PENDING_UPDATE.name
            },
            localUpdatedAt = System.currentTimeMillis()
        )
        scheduleDao.updateSchedule(withSyncStatus)
        return withSyncStatus
    }
    
    
    suspend fun deleteSchedule(id: String) {
        val schedule = scheduleDao.getScheduleById(id) ?: return
        
        if (schedule.syncStatus == SyncStatus.PENDING_CREATE.name) {
            
            scheduleDao.deleteSchedule(id)
        } else {
            
            scheduleDao.updateSchedule(
                schedule.copy(
                    syncStatus = SyncStatus.PENDING_DELETE.name,
                    localUpdatedAt = System.currentTimeMillis()
                )
            )
        }
    }
    
    
    suspend fun hardDeleteSchedule(id: String) {
        scheduleDao.deleteSchedule(id)
    }
    
    
    suspend fun getItemsByDate(taskIds: List<String>, date: String): List<ScheduleItemEntity> {
        return scheduleDao.getItemsByDate(taskIds, date)
    }
    
    suspend fun getItemsByRange(taskIds: List<String>, startDate: String, endDate: String): List<ScheduleItemEntity> {
        return scheduleDao.getItemsByRange(taskIds, startDate, endDate)
    }
    
    suspend fun getItemByTaskAndDate(taskId: String, date: String): ScheduleItemEntity? {
        return scheduleDao.getItemByTaskAndDate(taskId, date)
    }
    
    
    suspend fun upsertScheduleItem(taskId: String, date: String, status: String): ScheduleItemEntity {
        val existing = scheduleDao.getItemByTaskAndDate(taskId, date)
        
        return if (existing != null) {
            val updated = existing.copy(
                status = status,
                syncStatus = if (existing.syncStatus == SyncStatus.PENDING_CREATE.name) {
                    SyncStatus.PENDING_CREATE.name
                } else {
                    SyncStatus.PENDING_UPDATE.name
                },
                localUpdatedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            scheduleDao.updateScheduleItem(updated)
            updated
        } else {
            val newItem = ScheduleItemEntity(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                date = date,
                status = status,
                syncStatus = SyncStatus.PENDING_CREATE.name,
                localUpdatedAt = System.currentTimeMillis()
            )
            scheduleDao.insertScheduleItem(newItem)
            newItem
        }
    }
    
    
    suspend fun getSubTasks(scheduleId: String): List<SubTaskEntity> {
        return scheduleDao.getSubTasks(scheduleId)
    }
    
    suspend fun getSubTasksBatch(scheduleIds: List<String>): List<SubTaskEntity> {
        return scheduleDao.getSubTasksBatch(scheduleIds)
    }
    
    suspend fun insertSubTask(entity: SubTaskEntity): SubTaskEntity {
        val withSyncStatus = entity.copy(
            id = if (entity.id.isBlank()) UUID.randomUUID().toString() else entity.id,
            syncStatus = SyncStatus.PENDING_CREATE.name,
            localUpdatedAt = System.currentTimeMillis()
        )
        scheduleDao.insertSubTask(withSyncStatus)
        return withSyncStatus
    }
    
    suspend fun updateSubTaskStatus(id: String, isDone: Boolean) {
        val subTask = scheduleDao.getSubTaskById(id) ?: return
        scheduleDao.updateSubTask(
            subTask.copy(
                isDone = isDone,
                syncStatus = if (subTask.syncStatus == SyncStatus.PENDING_CREATE.name) {
                    SyncStatus.PENDING_CREATE.name
                } else {
                    SyncStatus.PENDING_UPDATE.name
                },
                localUpdatedAt = System.currentTimeMillis()
            )
        )
    }
    
    suspend fun deleteSubTasksByScheduleId(scheduleId: String) {
        scheduleDao.deleteSubTasksByScheduleId(scheduleId)
    }
    
    
    suspend fun getEditedVersionsByIds(ids: List<String>): List<EditedVersionEntity> {
        return scheduleDao.getEditedVersionsByIds(ids)
    }
    
    suspend fun insertEditedVersion(entity: EditedVersionEntity): EditedVersionEntity {
        val withSyncStatus = entity.copy(
            id = if (entity.id.isBlank()) UUID.randomUUID().toString() else entity.id,
            syncStatus = SyncStatus.PENDING_CREATE.name,
            localUpdatedAt = System.currentTimeMillis()
        )
        scheduleDao.insertEditedVersion(withSyncStatus)
        return withSyncStatus
    }
    
    
    suspend fun getCategories(userId: String): List<CategoryEntity> {
        return scheduleDao.getCategories(userId)
    }
    
    fun getCategoriesFlow(userId: String): Flow<List<CategoryEntity>> {
        return scheduleDao.getCategoriesFlow(userId)
    }
    
    
    suspend fun getPendingSchedules(): List<ScheduleEntity> {
        return scheduleDao.getPendingSchedules()
    }
    
    suspend fun getPendingItems(): List<ScheduleItemEntity> {
        return scheduleDao.getPendingItems()
    }
    
    suspend fun getPendingSubTasks(): List<SubTaskEntity> {
        return scheduleDao.getPendingSubTasks()
    }
    
    fun getTotalPendingCountFlow(): Flow<Int> {
        return scheduleDao.getTotalPendingCountFlow()
    }
    
    suspend fun markScheduleSynced(id: String, serverTime: Long = System.currentTimeMillis()) {
        scheduleDao.markScheduleSynced(id, serverTime)
    }
    
    suspend fun markItemSynced(id: String) {
        scheduleDao.markItemSynced(id)
    }
    
    suspend fun markSubTaskSynced(id: String) {
        scheduleDao.markSubTaskSynced(id)
    }
    
    
    suspend fun replaceAllData(
        userId: String,
        schedules: List<ScheduleEntity>,
        items: List<ScheduleItemEntity>,
        subTasks: List<SubTaskEntity>,
        categories: List<CategoryEntity> = emptyList()
    ) {
        scheduleDao.replaceAllDataForUser(userId, schedules, items, subTasks, categories)
    }
    
    
    suspend fun insertSchedules(schedules: List<ScheduleEntity>) {
        scheduleDao.insertSchedules(schedules)
    }
    
    suspend fun insertScheduleItems(items: List<ScheduleItemEntity>) {
        scheduleDao.insertScheduleItems(items)
    }
    
    suspend fun insertSubTasks(subTasks: List<SubTaskEntity>) {
        scheduleDao.insertSubTasks(subTasks)
    }
    
    
    suspend fun clearAllLocalData() {
        scheduleDao.clearAllLocalData()
    }
}

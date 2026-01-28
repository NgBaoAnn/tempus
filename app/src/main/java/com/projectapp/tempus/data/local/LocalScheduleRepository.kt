package com.projectapp.tempus.data.local

import com.projectapp.tempus.data.local.dao.ScheduleDao
import com.projectapp.tempus.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository cho các thao tác local với Room database
 * Tất cả CRUD operations đều làm trên local và đánh dấu syncStatus phù hợp
 */
class LocalScheduleRepository(
    private val scheduleDao: ScheduleDao
) {
    // ==================== SCHEDULE OPERATIONS ====================
    
    suspend fun getAllSchedules(userId: String): List<ScheduleEntity> {
        return scheduleDao.getActiveSchedules(userId)
    }
    
    fun getAllSchedulesFlow(userId: String): Flow<List<ScheduleEntity>> {
        return scheduleDao.getAllSchedulesFlow(userId)
    }
    
    suspend fun getScheduleById(id: String): ScheduleEntity? {
        return scheduleDao.getScheduleById(id)
    }
    
    /**
     * Insert schedule mới (tạo local, chưa sync)
     */
    suspend fun insertSchedule(entity: ScheduleEntity): ScheduleEntity {
        val withSyncStatus = entity.copy(
            id = if (entity.id.isBlank()) UUID.randomUUID().toString() else entity.id,
            syncStatus = SyncStatus.PENDING_CREATE.name,
            localUpdatedAt = System.currentTimeMillis()
        )
        scheduleDao.insertSchedule(withSyncStatus)
        return withSyncStatus
    }
    
    /**
     * Update schedule (đánh dấu pending update)
     */
    suspend fun updateSchedule(entity: ScheduleEntity): ScheduleEntity {
        val withSyncStatus = entity.copy(
            syncStatus = if (entity.syncStatus == SyncStatus.PENDING_CREATE.name) {
                // Nếu vẫn đang pending create, giữ nguyên status
                SyncStatus.PENDING_CREATE.name
            } else {
                SyncStatus.PENDING_UPDATE.name
            },
            localUpdatedAt = System.currentTimeMillis()
        )
        scheduleDao.updateSchedule(withSyncStatus)
        return withSyncStatus
    }
    
    /**
     * Delete schedule (soft delete nếu đã sync, hard delete nếu chưa)
     */
    suspend fun deleteSchedule(id: String) {
        val schedule = scheduleDao.getScheduleById(id) ?: return
        
        if (schedule.syncStatus == SyncStatus.PENDING_CREATE.name) {
            // Chưa từng sync lên server, xóa luôn
            scheduleDao.deleteSchedule(id)
        } else {
            // Đã sync, đánh dấu xóa để sync sau
            scheduleDao.updateSchedule(
                schedule.copy(
                    syncStatus = SyncStatus.PENDING_DELETE.name,
                    localUpdatedAt = System.currentTimeMillis()
                )
            )
        }
    }
    
    /**
     * Hard delete (thực sự xóa khỏi Room, gọi sau khi sync xong)
     */
    suspend fun hardDeleteSchedule(id: String) {
        scheduleDao.deleteSchedule(id)
    }
    
    // ==================== SCHEDULE ITEM OPERATIONS ====================
    
    suspend fun getItemsByDate(taskIds: List<String>, date: String): List<ScheduleItemEntity> {
        return scheduleDao.getItemsByDate(taskIds, date)
    }
    
    suspend fun getItemsByRange(taskIds: List<String>, startDate: String, endDate: String): List<ScheduleItemEntity> {
        return scheduleDao.getItemsByRange(taskIds, startDate, endDate)
    }
    
    suspend fun getItemByTaskAndDate(taskId: String, date: String): ScheduleItemEntity? {
        return scheduleDao.getItemByTaskAndDate(taskId, date)
    }
    
    /**
     * Upsert schedule item (tạo hoặc cập nhật trạng thái cho ngày cụ thể)
     */
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
    
    // ==================== SUBTASK OPERATIONS ====================
    
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
    
    // ==================== EDITED VERSION OPERATIONS ====================
    
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
    
    // ==================== CATEGORY OPERATIONS ====================
    
    suspend fun getCategories(userId: String): List<CategoryEntity> {
        return scheduleDao.getCategories(userId)
    }
    
    fun getCategoriesFlow(userId: String): Flow<List<CategoryEntity>> {
        return scheduleDao.getCategoriesFlow(userId)
    }
    
    // ==================== SYNC QUERIES ====================
    
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
    
    // ==================== BULK OPERATIONS ====================
    
    /**
     * Replace tất cả data cho user (dùng khi pull từ server)
     */
    suspend fun replaceAllData(
        userId: String,
        schedules: List<ScheduleEntity>,
        items: List<ScheduleItemEntity>,
        subTasks: List<SubTaskEntity>,
        categories: List<CategoryEntity> = emptyList()
    ) {
        scheduleDao.replaceAllDataForUser(userId, schedules, items, subTasks, categories)
    }
    
    /**
     * Insert batch (không clear existing, dùng cho merge)
     */
    suspend fun insertSchedules(schedules: List<ScheduleEntity>) {
        scheduleDao.insertSchedules(schedules)
    }
    
    suspend fun insertScheduleItems(items: List<ScheduleItemEntity>) {
        scheduleDao.insertScheduleItems(items)
    }
    
    suspend fun insertSubTasks(subTasks: List<SubTaskEntity>) {
        scheduleDao.insertSubTasks(subTasks)
    }
    
    // ==================== CLEAR DATA (for logout) ====================
    
    /**
     * Clear ALL local data - gọi khi logout để đảm bảo data isolation giữa các users
     */
    suspend fun clearAllLocalData() {
        scheduleDao.clearAllLocalData()
    }
}

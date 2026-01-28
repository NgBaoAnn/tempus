package com.projectapp.tempus.data.schedule

import com.projectapp.tempus.data.local.LocalScheduleRepository
import com.projectapp.tempus.data.local.entity.*
import com.projectapp.tempus.data.schedule.dto.*
import com.projectapp.tempus.data.sync.toRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Offline-First Schedule Repository
 * 
 * Tất cả CRUD operations đều làm trên Room (local) trước.
 * Sau đó user manual sync để push lên Supabase.
 * 
 * Thay thế SupabaseScheduleRepository cho các UI operations.
 */
class OfflineFirstScheduleRepository(
    private val localRepo: LocalScheduleRepository
) : ScheduleRepository {
    
    override suspend fun getAllSchedules(userId: String): List<ScheduleRow> {
        return localRepo.getAllSchedules(userId)
            .filter { it.syncStatus != SyncStatus.PENDING_DELETE.name }
            .map { it.toRow() }
    }
    
    fun getAllSchedulesFlow(userId: String): Flow<List<ScheduleRow>> {
        return localRepo.getAllSchedulesFlow(userId).map { list ->
            list.filter { it.syncStatus != SyncStatus.PENDING_DELETE.name }
                .map { it.toRow() }
        }
    }
    
    override suspend fun getScheduleById(id: String): ScheduleRow? {
        return localRepo.getScheduleById(id)?.toRow()
    }
    
    override suspend fun getScheduleItemsByDate(date: String, taskIds: List<String>): List<ScheduleItemRow> {
        return localRepo.getItemsByDate(taskIds, date).map { entity ->
            ScheduleItemRow(
                id = entity.id,
                taskId = entity.taskId,
                date = entity.date,
                status = StatusType.entries.find { it.name == entity.status },
                updatedAt = millisToIso(entity.updatedAt),
                editedVersion = entity.editedVersion,
                createdAt = millisToIso(entity.createdAt)
            )
        }
    }
    
    override suspend fun getScheduleItemsByRange(
        startDate: String, 
        endDate: String, 
        taskIds: List<String>
    ): List<ScheduleItemRow> {
        return localRepo.getItemsByRange(taskIds, startDate, endDate).map { entity ->
            ScheduleItemRow(
                id = entity.id,
                taskId = entity.taskId,
                date = entity.date,
                status = StatusType.entries.find { it.name == entity.status },
                updatedAt = millisToIso(entity.updatedAt),
                editedVersion = entity.editedVersion,
                createdAt = millisToIso(entity.createdAt)
            )
        }
    }
    
    override suspend fun getScheduleItemsByDates(
        dates: List<String>,
        taskIds: List<String>
    ): List<ScheduleItemRow> {
        // Implement using multiple date queries or range
        if (dates.isEmpty()) return emptyList()
        val minDate = dates.minOrNull() ?: return emptyList()
        val maxDate = dates.maxOrNull() ?: return emptyList()
        return getScheduleItemsByRange(minDate, maxDate, taskIds)
            .filter { it.date in dates }
    }
    
    override suspend fun getEditedVersions(ids: List<String>): List<EditedVersionRow> {
        return localRepo.getEditedVersionsByIds(ids).map { entity ->
            EditedVersionRow(
                id = entity.id,
                nameSchedule = entity.nameSchedule,
                iconId = entity.iconId,
                startTimeDate = entity.startTimeDate,
                implementationTime = entity.implementationTime,
                color = entity.color,
                createdAt = millisToIso(entity.createdAt)
            )
        }
    }
    
    override suspend fun insertSchedule(row: Map<String, Any?>): ScheduleRow {
        val entity = ScheduleEntity(
            id = (row["id"] as? String) ?: UUID.randomUUID().toString(),
            userId = row["user_id"] as? String ?: "",
            name = row["name_schedule"] as? String ?: "",
            label = row["label"] as? String,
            priority = row["priority"] as? String ?: "medium",
            categoryId = row["category_id"] as? String,
            description = row["description"] as? String,
            startTimeDate = row["start_time_date"] as? String ?: "",
            implementationTime = row["implementation_time"] as? String ?: "01:00:00",
            repeat = row["repeat"] as? String ?: "once",
            repeatDays = row["repeat_days"] as? String,
            endDate = row["end_date"] as? String,
            color = row["color"] as? String ?: "#2196F3",
            source = row["source"] as? String ?: "manual",
            syncStatus = SyncStatus.PENDING_CREATE.name,
            localUpdatedAt = System.currentTimeMillis()
        )
        
        val inserted = localRepo.insertSchedule(entity)
        return inserted.toRow()
    }
    
    override suspend fun updateSchedule(taskId: String, fields: Map<String, Any?>): ScheduleRow {
        val existing = localRepo.getScheduleById(taskId) 
            ?: throw Exception("Schedule not found: $taskId")
        
        val updated = existing.copy(
            name = fields["name_schedule"] as? String ?: existing.name,
            label = fields["label"] as? String ?: existing.label,
            priority = fields["priority"] as? String ?: existing.priority,
            categoryId = fields["category_id"] as? String ?: existing.categoryId,
            description = fields["description"] as? String ?: existing.description,
            startTimeDate = fields["start_time_date"] as? String ?: existing.startTimeDate,
            implementationTime = fields["implementation_time"] as? String ?: existing.implementationTime,
            repeat = fields["repeat"] as? String ?: existing.repeat,
            repeatDays = fields["repeat_days"] as? String ?: existing.repeatDays,
            endDate = fields["end_date"] as? String ?: existing.endDate,
            color = fields["color"] as? String ?: existing.color,
            syncStatus = if (existing.syncStatus == SyncStatus.PENDING_CREATE.name) {
                SyncStatus.PENDING_CREATE.name
            } else {
                SyncStatus.PENDING_UPDATE.name
            },
            localUpdatedAt = System.currentTimeMillis()
        )
        
        localRepo.updateSchedule(updated)
        return updated.toRow()
    }
    
    override suspend fun upsertScheduleItem(
        taskId: String, 
        date: String, 
        status: StatusType
    ): ScheduleItemRow {
        val entity = localRepo.upsertScheduleItem(taskId, date, status.name)
        return ScheduleItemRow(
            id = entity.id,
            taskId = entity.taskId,
            date = entity.date,
            status = StatusType.entries.find { it.name == entity.status },
            updatedAt = millisToIso(entity.updatedAt),
            editedVersion = entity.editedVersion,
            createdAt = millisToIso(entity.createdAt)
        )
    }
    
    override suspend fun insertEditedVersion(fields: Map<String, Any?>): EditedVersionRow {
        val entity = EditedVersionEntity(
            id = (fields["id"] as? String) ?: UUID.randomUUID().toString(),
            nameSchedule = fields["name_schedule"] as? String,
            iconId = fields["icon_id"] as? Int,
            startTimeDate = fields["start_time_date"] as? String,
            implementationTime = fields["implementation_time"] as? String,
            color = fields["color"] as? String,
            syncStatus = SyncStatus.PENDING_CREATE.name,
            localUpdatedAt = System.currentTimeMillis()
        )
        
        val inserted = localRepo.insertEditedVersion(entity)
        return EditedVersionRow(
            id = inserted.id,
            nameSchedule = inserted.nameSchedule,
            iconId = inserted.iconId,
            startTimeDate = inserted.startTimeDate,
            implementationTime = inserted.implementationTime,
            color = inserted.color,
            createdAt = millisToIso(inserted.createdAt)
        )
    }
    
    override suspend fun attachEditedVersionToDate(
        taskId: String, 
        date: String, 
        editedVersionId: String
    ): ScheduleItemRow {
        val item = localRepo.getItemByTaskAndDate(taskId, date)
        
        return if (item != null) {
            val updated = item.copy(
                editedVersion = editedVersionId,
                syncStatus = if (item.syncStatus == SyncStatus.PENDING_CREATE.name) {
                    SyncStatus.PENDING_CREATE.name
                } else {
                    SyncStatus.PENDING_UPDATE.name
                },
                localUpdatedAt = System.currentTimeMillis()
            )
            // Need to add update method to local repo
            ScheduleItemRow(
                id = updated.id,
                taskId = updated.taskId,
                date = updated.date,
                status = StatusType.entries.find { it.name == updated.status },
                updatedAt = millisToIso(updated.updatedAt),
                editedVersion = updated.editedVersion,
                createdAt = millisToIso(updated.createdAt)
            )
        } else {
            upsertScheduleItem(taskId, date, StatusType.planned).copy(
                editedVersion = editedVersionId
            )
        }
    }
    
    override suspend fun deleteSchedule(id: String) {
        localRepo.deleteSchedule(id)
    }
    
    override suspend fun deleteSchedulesFromDate(userId: String, fromDate: String): Int {
        // Get schedules and mark for deletion
        val schedules = localRepo.getAllSchedules(userId)
        var count = 0
        for (schedule in schedules) {
            // Check if schedule starts from given date
            try {
                val scheduleDate = java.time.OffsetDateTime.parse(schedule.startTimeDate)
                    .toLocalDate().toString()
                if (scheduleDate >= fromDate) {
                    localRepo.deleteSchedule(schedule.id)
                    count++
                }
            } catch (e: Exception) {
                // Skip invalid dates
            }
        }
        return count
    }
    
    override suspend fun setEndDateForAllSchedules(userId: String, endDate: String): Int {
        val schedules = localRepo.getAllSchedules(userId)
        var count = 0
        for (schedule in schedules) {
            if (schedule.endDate == null || schedule.endDate!! > endDate) {
                localRepo.updateSchedule(schedule.copy(endDate = endDate))
                count++
            }
        }
        return count
    }
    
    // ==================== SUBTASK OPERATIONS ====================
    
    override suspend fun getSubTasks(scheduleId: String): List<SubTaskRow> {
        return localRepo.getSubTasks(scheduleId).map { entity ->
            SubTaskRow(
                id = entity.id,
                scheduleId = entity.scheduleId,
                title = entity.title,
                isDone = entity.isDone,
                orderNo = entity.orderNo,
                createdAt = millisToIso(entity.createdAt)
            )
        }
    }
    
    override suspend fun getSubTasksBatch(scheduleIds: List<String>): List<SubTaskRow> {
        return localRepo.getSubTasksBatch(scheduleIds).map { entity ->
            SubTaskRow(
                id = entity.id,
                scheduleId = entity.scheduleId,
                title = entity.title,
                isDone = entity.isDone,
                orderNo = entity.orderNo,
                createdAt = millisToIso(entity.createdAt)
            )
        }
    }
    
    override suspend fun insertSubTasks(scheduleId: String, titles: List<String>) {
        titles.forEachIndexed { index, title ->
            val entity = SubTaskEntity(
                id = UUID.randomUUID().toString(),
                scheduleId = scheduleId,
                title = title,
                isDone = false,
                orderNo = index,
                syncStatus = SyncStatus.PENDING_CREATE.name,
                localUpdatedAt = System.currentTimeMillis()
            )
            localRepo.insertSubTask(entity)
        }
    }
    
    override suspend fun deleteSubTasksByScheduleId(scheduleId: String) {
        localRepo.deleteSubTasksByScheduleId(scheduleId)
    }
    
    override suspend fun updateSubTaskStatus(subTaskId: String, isDone: Boolean) {
        localRepo.updateSubTaskStatus(subTaskId, isDone)
    }
    
    // ==================== SYNC HELPERS ====================
    
    fun getPendingCountFlow(): Flow<Int> {
        return localRepo.getTotalPendingCountFlow()
    }
    
    // ==================== PRIVATE HELPERS ====================
    
    private fun millisToIso(millis: Long): String {
        return java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}

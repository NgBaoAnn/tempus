package com.projectapp.tempus.data.local.dao

import androidx.room.*
import com.projectapp.tempus.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object cho Schedule và các entity liên quan
 */
@Dao
interface ScheduleDao {
    
    // ==================== SCHEDULE QUERIES ====================
    
    @Query("SELECT * FROM schedules WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY createdAt DESC")
    fun getAllSchedulesFlow(userId: String): Flow<List<ScheduleEntity>>
    
    @Query("SELECT * FROM schedules WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllSchedules(userId: String): List<ScheduleEntity>
    
    @Query("SELECT * FROM schedules WHERE userId = :userId AND syncStatus != 'PENDING_DELETE'")
    suspend fun getActiveSchedules(userId: String): List<ScheduleEntity>
    
    /**
     * Get today's tasks for widget display
     * Filters by date, excludes deleted, sorts by start time, limits to 5
     */
    @Query("""
        SELECT * FROM schedules 
        WHERE userId = :userId 
        AND syncStatus != 'PENDING_DELETE' 
        AND substr(startTimeDate, 1, 10) = :date
        ORDER BY startTimeDate ASC 
        LIMIT 5
    """)
    suspend fun getTodayTasksForWidget(userId: String, date: String): List<ScheduleEntity>
    
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: String): ScheduleEntity?
    
    @Query("SELECT * FROM schedules WHERE id IN (:ids)")
    suspend fun getSchedulesByIds(ids: List<String>): List<ScheduleEntity>
    
    // ==================== SCHEDULE ITEM QUERIES ====================
    
    @Query("SELECT * FROM schedule_items WHERE taskId IN (:taskIds) AND date = :date")
    suspend fun getItemsByDate(taskIds: List<String>, date: String): List<ScheduleItemEntity>
    
    @Query("SELECT * FROM schedule_items WHERE taskId IN (:taskIds) AND date BETWEEN :startDate AND :endDate")
    suspend fun getItemsByRange(taskIds: List<String>, startDate: String, endDate: String): List<ScheduleItemEntity>
    
    @Query("SELECT * FROM schedule_items WHERE taskId IN (:taskIds) AND date IN (:dates)")
    suspend fun getItemsByDates(taskIds: List<String>, dates: List<String>): List<ScheduleItemEntity>
    
    @Query("SELECT * FROM schedule_items WHERE taskId = :taskId AND date = :date")
    suspend fun getItemByTaskAndDate(taskId: String, date: String): ScheduleItemEntity?
    
    @Query("SELECT * FROM schedule_items WHERE id = :id")
    suspend fun getItemById(id: String): ScheduleItemEntity?
    
    // ==================== SUBTASK QUERIES ====================
    
    @Query("SELECT * FROM sub_tasks WHERE scheduleId = :scheduleId ORDER BY orderNo")
    suspend fun getSubTasks(scheduleId: String): List<SubTaskEntity>
    
    @Query("SELECT * FROM sub_tasks WHERE scheduleId = :scheduleId ORDER BY orderNo")
    fun getSubTasksFlow(scheduleId: String): Flow<List<SubTaskEntity>>
    
    @Query("SELECT * FROM sub_tasks WHERE scheduleId IN (:scheduleIds)")
    suspend fun getSubTasksBatch(scheduleIds: List<String>): List<SubTaskEntity>
    
    @Query("SELECT * FROM sub_tasks WHERE id = :id")
    suspend fun getSubTaskById(id: String): SubTaskEntity?
    
    // ==================== CATEGORY QUERIES ====================
    
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name")
    suspend fun getCategories(userId: String): List<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name")
    fun getCategoriesFlow(userId: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): CategoryEntity?
    
    // ==================== EDITED VERSION QUERIES ====================
    
    @Query("SELECT * FROM edited_versions WHERE id = :id")
    suspend fun getEditedVersionById(id: String): EditedVersionEntity?
    
    @Query("SELECT * FROM edited_versions WHERE id IN (:ids)")
    suspend fun getEditedVersionsByIds(ids: List<String>): List<EditedVersionEntity>
    
    // ==================== INSERT OPERATIONS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleItemEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItems(items: List<ScheduleItemEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTasks(subTasks: List<SubTaskEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditedVersion(editedVersion: EditedVersionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditedVersions(editedVersions: List<EditedVersionEntity>)
    
    // ==================== UPDATE OPERATIONS ====================
    
    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)
    
    @Update
    suspend fun updateScheduleItem(item: ScheduleItemEntity)
    
    @Update
    suspend fun updateSubTask(subTask: SubTaskEntity)
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    // ==================== DELETE OPERATIONS ====================
    
    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteSchedule(id: String)
    
    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteScheduleItem(id: String)
    
    @Query("DELETE FROM schedule_items WHERE taskId = :taskId")
    suspend fun deleteScheduleItemsByTaskId(taskId: String)
    
    @Query("DELETE FROM sub_tasks WHERE id = :id")
    suspend fun deleteSubTask(id: String)
    
    @Query("DELETE FROM sub_tasks WHERE scheduleId = :scheduleId")
    suspend fun deleteSubTasksByScheduleId(scheduleId: String)
    
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)
    
    @Query("DELETE FROM edited_versions WHERE id = :id")
    suspend fun deleteEditedVersion(id: String)
    
    // ==================== SYNC QUERIES ====================
    
    @Query("SELECT * FROM schedules WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSchedules(): List<ScheduleEntity>
    
    @Query("SELECT * FROM schedule_items WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingItems(): List<ScheduleItemEntity>
    
    @Query("SELECT * FROM sub_tasks WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSubTasks(): List<SubTaskEntity>
    
    @Query("SELECT * FROM categories WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingCategories(): List<CategoryEntity>
    
    @Query("SELECT * FROM edited_versions WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingEditedVersions(): List<EditedVersionEntity>
    
    // Pending count flows for UI badge
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM schedules WHERE syncStatus != 'SYNCED') +
            (SELECT COUNT(*) FROM schedule_items WHERE syncStatus != 'SYNCED') +
            (SELECT COUNT(*) FROM sub_tasks WHERE syncStatus != 'SYNCED')
    """)
    fun getTotalPendingCountFlow(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM schedules WHERE syncStatus != 'SYNCED'")
    fun getPendingScheduleCountFlow(): Flow<Int>
    
    // Mark as synced
    @Query("UPDATE schedules SET syncStatus = 'SYNCED', serverUpdatedAt = :serverTime WHERE id = :id")
    suspend fun markScheduleSynced(id: String, serverTime: Long)
    
    @Query("UPDATE schedule_items SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markItemSynced(id: String)
    
    @Query("UPDATE sub_tasks SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markSubTaskSynced(id: String)
    
    @Query("UPDATE categories SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markCategorySynced(id: String)
    
    @Query("UPDATE edited_versions SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markEditedVersionSynced(id: String)
    
    // ==================== BULK OPERATIONS (for initial sync) ====================
    
    @Query("DELETE FROM schedules WHERE userId = :userId")
    suspend fun clearSchedulesForUser(userId: String)
    
    @Query("DELETE FROM schedule_items WHERE taskId IN (SELECT id FROM schedules WHERE userId = :userId)")
    suspend fun clearItemsForUser(userId: String)
    
    @Query("DELETE FROM sub_tasks WHERE scheduleId IN (SELECT id FROM schedules WHERE userId = :userId)")
    suspend fun clearSubTasksForUser(userId: String)
    
    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun clearCategoriesForUser(userId: String)
    
    @Transaction
    suspend fun replaceAllDataForUser(
        userId: String,
        schedules: List<ScheduleEntity>,
        items: List<ScheduleItemEntity>,
        subTasks: List<SubTaskEntity>,
        categories: List<CategoryEntity>
    ) {
        // Clear existing data
        clearSubTasksForUser(userId)
        clearItemsForUser(userId)
        clearSchedulesForUser(userId)
        clearCategoriesForUser(userId)
        
        // Insert new data
        insertCategories(categories)
        insertSchedules(schedules)
        insertScheduleItems(items)
        insertSubTasks(subTasks)
    }
    
    // ==================== CLEAR ALL DATA (for logout) ====================
    
    @Query("DELETE FROM schedules")
    suspend fun deleteAllSchedules()
    
    @Query("DELETE FROM schedule_items")
    suspend fun deleteAllScheduleItems()
    
    @Query("DELETE FROM sub_tasks")
    suspend fun deleteAllSubTasks()
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
    
    @Query("DELETE FROM edited_versions")
    suspend fun deleteAllEditedVersions()
    
    /**
     * Clear ALL local data - gọi khi logout để đảm bảo data isolation giữa các users
     */
    @Transaction
    suspend fun clearAllLocalData() {
        deleteAllSubTasks()
        deleteAllScheduleItems()
        deleteAllEditedVersions()
        deleteAllSchedules()
        deleteAllCategories()
    }
}

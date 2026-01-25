package com.projectapp.tempus.data.schedule

import com.projectapp.tempus.data.schedule.dto.*

interface ScheduleRepository {
    suspend fun getAllSchedules(userId: String): List<ScheduleRow>
    suspend fun getScheduleItemsByDate(date: String, taskIds: List<String>): List<ScheduleItemRow>
    suspend fun getScheduleItemsByRange(startDate: String, endDate: String, taskIds: List<String>): List<ScheduleItemRow>
    suspend fun getScheduleItemsByDates(dates: List<String>, taskIds: List<String>): List<ScheduleItemRow>
    suspend fun getEditedVersions(ids: List<String>): List<EditedVersionRow>
    suspend fun insertSchedule(row: Map<String, Any?>): ScheduleRow
    suspend fun upsertScheduleItem(taskId: String, date: String, status: StatusType): ScheduleItemRow
    suspend fun updateSchedule(taskId: String, fields: Map<String, Any?>): ScheduleRow
    suspend fun insertEditedVersion(fields: Map<String, Any?>): EditedVersionRow
    suspend fun attachEditedVersionToDate(taskId: String, date: String, editedVersionId: String): ScheduleItemRow
    suspend fun getScheduleById(id: String): ScheduleRow?
    suspend fun deleteSchedule(id: String)
    
    /**
     * Delete all schedules for a user that start from a specific date onwards
     * @param userId The user ID
     * @param fromDate The date from which to delete (ISO format)
     * @return Number of deleted schedules
     */
    suspend fun deleteSchedulesFromDate(userId: String, fromDate: String): Int
    
    /**
     * Set end_date for all schedules of a user to stop them from appearing from today onwards.
     * This keeps historical data but prevents schedules from appearing in future dates.
     * @param userId The user ID
     * @param endDate The end date to set (YYYY-MM-DD format)
     * @return Number of updated schedules
     */
    suspend fun setEndDateForAllSchedules(userId: String, endDate: String): Int
    
    // Subtask methods
    suspend fun getSubTasks(scheduleId: String): List<SubTaskRow>
    suspend fun insertSubTasks(scheduleId: String, titles: List<String>)
    suspend fun deleteSubTasksByScheduleId(scheduleId: String)
    suspend fun updateSubTaskStatus(subTaskId: String, isDone: Boolean)
}

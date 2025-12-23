package com.projectapp.tempus.data.schedule

import com.projectapp.tempus.data.schedule.dto.*

interface ScheduleRepository {
    suspend fun getAllSchedules(userId: String): List<ScheduleRow>
    suspend fun getScheduleItemsByDate(date: String): List<ScheduleItemRow>
    suspend fun getEditedVersions(ids: List<String>): List<EditedVersionRow>

    suspend fun insertSchedule(row: Map<String, Any?>): ScheduleRow
    suspend fun upsertScheduleItem(taskId: String, date: String, status: StatusType): ScheduleItemRow

    suspend fun updateSchedule(taskId: String, fields: Map<String, Any?>): ScheduleRow

    suspend fun insertEditedVersion(fields: Map<String, Any?>): EditedVersionRow

    suspend fun attachEditedVersionToDate(taskId: String, date: String, editedVersionId: String): ScheduleItemRow

    suspend fun getScheduleById(id: String): ScheduleRow?
    suspend fun deleteSchedule(id: String)
}
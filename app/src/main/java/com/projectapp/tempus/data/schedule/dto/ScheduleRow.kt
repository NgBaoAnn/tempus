package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name_schedule") val name: String,

    val label: ScheduleLabel? = null,
    val priority: PriorityType? = PriorityType.medium,
    @SerialName("category_id") val categoryId: String? = null,
    val description: String? = null,
    // ----------------------------------------------------

    @SerialName("start_time_date") val startTimeDate: String, // ISO string (VD: 2025-12-21 07:00:00+07)
    @SerialName("implementation_time") val implementationTime: String, // "HH:MM:SS"
    val repeat: RepeatType,
    @SerialName("repeat_days") val repeatDays: String? = null, // Các thứ lặp lại, VD: "1,3,5" = Thứ 2, Thứ 4, Thứ 6 (1=Mon, 7=Sun)
    @SerialName("end_date") val endDate: String? = null, // ISO date string (VD: 2026-01-26) - schedule won't show after this date
    val color: String? = null,
    val source: SourceType? = null,
    @SerialName("created_at") val createdAt: String? = null
)


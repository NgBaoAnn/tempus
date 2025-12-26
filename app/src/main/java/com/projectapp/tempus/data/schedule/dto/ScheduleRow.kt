package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name_schedule") val name: String,

    val label: String? = null,
    // ----------------------------------------------------

    @SerialName("start_time_date") val startTimeDate: String, // ISO string (VD: 2025-12-21 07:00:00+07)
    @SerialName("implementation_time") val implementationTime: String, // "HH:MM:SS"
    val repeat: RepeatType,
    val color: String? = null,
    val source: SourceType? = null,
    @SerialName("created_at") val createdAt: String? = null
)
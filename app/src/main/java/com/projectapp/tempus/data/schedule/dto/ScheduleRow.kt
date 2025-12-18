package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name_schedule") val name: String,
    @SerialName("icon_id") val iconId: Int? = null,
    @SerialName("start_time_date") val startTimeDate: String, // ISO string
    @SerialName("implementation_time") val implementationTime: String, // interval dạng "HH:MM:SS"
    val repeat: RepeatType,
    val color: String? = null,
    val source: SourceType? = null,
    @SerialName("created_at") val createdAt: String? = null
)
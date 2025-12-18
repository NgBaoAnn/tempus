package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleItemRow(
    val id: String,
    @SerialName("task_id") val taskId: String,
    val date: String,                 // "YYYY-MM-DD"
    val status: StatusType? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("edited_version") val editedVersion: String? = null
)

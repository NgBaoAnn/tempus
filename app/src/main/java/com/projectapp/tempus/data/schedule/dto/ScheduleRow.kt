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

    
    @SerialName("start_time_date") val startTimeDate: String, 
    @SerialName("implementation_time") val implementationTime: String, 
    val repeat: RepeatType,
    @SerialName("repeat_days") val repeatDays: String? = null, 
    @SerialName("end_date") val endDate: String? = null, 

    val color: String? = null,
    val source: SourceType? = null,
    @SerialName("created_at") val createdAt: String? = null
)

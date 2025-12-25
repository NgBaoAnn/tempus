package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditedVersionRow(
    val id: String,

    @SerialName("name_schedule") val name: String,
    val label: ScheduleLabel? = null,

    // ----------------------------------------------------

    val color: String? = null,
    @SerialName("start_time_date") val startTimeDate: String? = null,
    @SerialName("implementation_time") val implementationTime: String? = null
)
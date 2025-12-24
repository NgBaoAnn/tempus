package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditedVersionRow(
    val id: String,

    @SerialName("name_schedule") val name: String,
    @SerialName("label") val label: String? = null,
    // ----------------------------------------------------

    val color: String? = null,
    @SerialName("start_time_date") val startTimeDate: String? = null,
    @SerialName("implementation_time") val implementationTime: String? = null
)
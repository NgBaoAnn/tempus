package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditedVersionRow(
    val id: String,

    @SerialName("name_schedule") val name: String? = null,
    @SerialName("icon_id") val iconId: String? = null,
    val label: String? = null,
    // ----------------------------------------------------

    val color: String? = null,
    @SerialName("start_time_date") val startTimeDate: String? = null,
    @SerialName("implementation_time") val implementationTime: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
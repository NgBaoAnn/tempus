package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class PriorityType {
    @SerialName("high") high,
    @SerialName("medium") medium,
    @SerialName("low") low
}

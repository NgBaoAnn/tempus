package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubTaskRow(
    val id: String? = null,
    
    @SerialName("schedule_id")
    val scheduleId: String,
    
    val title: String,
    
    @SerialName("is_done")
    val isDone: Boolean = false,
    
    @SerialName("order_no")
    val orderNo: Int = 0,
    
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class SubTaskInsert(
    @SerialName("schedule_id")
    val scheduleId: String,
    
    val title: String,
    
    @SerialName("is_done")
    val isDone: Boolean = false,
    
    @SerialName("order_no")
    val orderNo: Int = 0
)

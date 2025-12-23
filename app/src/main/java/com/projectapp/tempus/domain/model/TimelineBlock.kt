package com.projectapp.tempus.domain.model

import com.projectapp.tempus.data.schedule.dto.StatusType
import java.time.Duration
import java.time.LocalDateTime

data class TimelineBlock(
    val taskId: String,
    val scheduleItemId: String?,   // null nếu chưa có item
    val title: String,
    val label: String,
    val color: String,
    val startTime: LocalDateTime,
    val duration: Duration,
    // ---------------------------

    val status: StatusType
)
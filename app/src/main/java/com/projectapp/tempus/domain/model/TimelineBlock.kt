package com.projectapp.tempus.domain.model

import com.projectapp.tempus.data.schedule.dto.StatusType

data class TimelineBlock(
    val taskId: String,
    val scheduleItemId: String?,   // null nếu chưa có item (chưa done/skip/delete)
    val title: String,
    val iconId: Int?,
    val color: String?,
    val startIso: String,          // ISO datetime
    val durationInterval: String,  // "HH:MM:SS"
    val status: StatusType         // planned/done/skip/delete
)

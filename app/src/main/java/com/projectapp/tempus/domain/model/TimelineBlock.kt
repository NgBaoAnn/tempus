package com.projectapp.tempus.domain.model

import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.StatusType
import java.time.Duration
import java.time.LocalDateTime

data class TimelineBlock(
    val taskId: String,
    val scheduleItemId: String?,   
    val title: String,
    val label: String,
    val labelEnum: ScheduleLabel = ScheduleLabel.book, 
    val color: String,
    val startTime: LocalDateTime,
    val duration: Duration,
    
    val priority: PriorityType = PriorityType.medium,
    val status: StatusType,
    val createdAt: LocalDateTime? = null, 
    val subtasks: List<SubtaskInfo> = emptyList()
)

data class SubtaskInfo(
    val id: String,
    val title: String,
    val isDone: Boolean
)
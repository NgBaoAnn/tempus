package com.projectapp.tempus.data.sync

import com.projectapp.tempus.data.local.entity.*
import com.projectapp.tempus.data.schedule.dto.*


fun ScheduleEntity.toRow(): ScheduleRow {
    return ScheduleRow(
        id = id,
        userId = userId,
        name = name,
        label = label?.let { ScheduleLabel.entries.find { l -> l.name == it } },
        priority = PriorityType.entries.find { it.name == priority } ?: PriorityType.medium,
        categoryId = categoryId,
        description = description,
        startTimeDate = startTimeDate,
        implementationTime = implementationTime,
        repeat = RepeatType.entries.find { it.name == repeat } ?: RepeatType.once,
        repeatDays = repeatDays,
        endDate = endDate,
        color = color,
        source = SourceType.entries.find { it.name == source } ?: SourceType.manual,
        createdAt = millisToIso(createdAt)
    )
}

fun ScheduleEntity.toInsertMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "user_id" to userId,
        "name_schedule" to name,
        "label" to label,
        "priority" to priority,
        "category_id" to categoryId,
        "description" to description,
        "start_time_date" to startTimeDate,
        "implementation_time" to implementationTime,
        "repeat" to repeat,
        "repeat_days" to repeatDays,
        "end_date" to endDate,
        "color" to color,
        "source" to source
    )
}

fun ScheduleEntity.toUpdateMap(): Map<String, Any?> {
    return mapOf(
        "name_schedule" to name,
        "label" to label,
        "priority" to priority,
        "category_id" to categoryId,
        "description" to description,
        "start_time_date" to startTimeDate,
        "implementation_time" to implementationTime,
        "repeat" to repeat,
        "repeat_days" to repeatDays,
        "end_date" to endDate,
        "color" to color
    )
}


fun ScheduleItemEntity.toStatusType(): StatusType {
    return StatusType.entries.find { it.name == status } ?: StatusType.planned
}


fun SubTaskEntity.toRow(): SubTaskRow {
    return SubTaskRow(
        id = id,
        scheduleId = scheduleId,
        title = title,
        isDone = isDone,
        orderNo = orderNo,
        createdAt = millisToIso(createdAt)
    )
}

fun SubTaskEntity.toInsertMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "schedule_id" to scheduleId,
        "title" to title,
        "is_done" to isDone,
        "order_no" to orderNo
    )
}


private fun millisToIso(millis: Long): String {
    return java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

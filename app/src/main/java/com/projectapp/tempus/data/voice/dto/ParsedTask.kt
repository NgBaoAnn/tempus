package com.projectapp.tempus.data.voice.dto

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * Parsed task from voice input
 */
data class ParsedTask(
    val rawText: String,
    val taskName: String?,
    val date: LocalDate?,
    val time: LocalTime?,
    val duration: Duration?,
    val confidence: Float = 0f
) {
    // Valid if we have at least a task name - date/time have fallback defaults
    val isValid: Boolean
        get() = !taskName.isNullOrBlank()
}

/**
 * Time slot for suggestions
 */
data class TimeSlot(
    val startTime: LocalTime,
    val date: LocalDate,
    val score: Float = 1f
) {
    val displayText: String
        get() = "${date.dayOfMonth}/${date.monthValue} lúc ${startTime.hour}:${String.format("%02d", startTime.minute)}"
}

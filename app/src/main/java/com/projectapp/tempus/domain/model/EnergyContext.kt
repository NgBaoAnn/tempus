package com.projectapp.tempus.domain.model

import java.time.DayOfWeek

/**
 * User energy and productivity context
 * Used by AI to optimize task scheduling based on user's patterns
 */
data class EnergyContext(
    /**
     * User ID
     */
    val userId: String,
    
    /**
     * Wake up time (e.g., "07:00")
     */
    val wakeTime: String = "07:00",
    
    /**
     * Sleep time (e.g., "23:00")
     */
    val sleepTime: String = "23:00",
    
    /**
     * Hours when user is most productive (24h format)
     * e.g., [9, 10, 11, 14, 15] = 9-11 AM and 2-3 PM
     */
    val peakHours: List<Int> = listOf(9, 10, 11, 14, 15),
    
    /**
     * Hours when user has low energy
     * e.g., [13, 22] = 1 PM (post-lunch) and 10 PM
     */
    val lowEnergyHours: List<Int> = listOf(13, 22),
    
    /**
     * Days when user typically works/studies
     */
    val workDays: List<DayOfWeek> = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    ),
    
    /**
     * Preferred break duration between tasks (minutes)
     */
    val preferredBreakDuration: Int = 15,
    
    /**
     * Maximum hours of focused work per day
     */
    val maxFocusHoursPerDay: Int = 6
) {
    /**
     * Check if a given hour is during peak productivity
     */
    fun isPeakHour(hour: Int): Boolean = hour in peakHours
    
    /**
     * Check if a given hour is low energy
     */
    fun isLowEnergyHour(hour: Int): Boolean = hour in lowEnergyHours
    
    /**
     * Get best time slots for difficult tasks
     */
    fun getBestSlotsForDifficultTasks(): List<String> {
        return peakHours.map { hour ->
            String.format("%02d:00", hour)
        }
    }
    
    /**
     * Get time slots for easier tasks (review, light work)
     */
    fun getSlotsForLightTasks(): List<String> {
        val allHours = (wakeTime.substringBefore(":").toIntOrNull() ?: 7)..
                       (sleepTime.substringBefore(":").toIntOrNull() ?: 23)
        
        return allHours
            .filter { it !in peakHours && it !in lowEnergyHours }
            .map { String.format("%02d:00", it) }
    }
    
    /**
     * Convert to AI context string for prompt
     */
    fun toContextString(): String {
        val workDaysStr = workDays.joinToString(", ") { it.name.lowercase() }
        val peakHoursStr = peakHours.joinToString(", ") { "${it}:00" }
        
        return """
            |Thời gian hoạt động: $wakeTime - $sleepTime
            |Ngày làm việc: $workDaysStr
            |Giờ productive nhất: $peakHoursStr
            |Thời gian nghỉ giữa tasks: ${preferredBreakDuration} phút
            |Tối đa ${maxFocusHoursPerDay}h tập trung/ngày
        """.trimMargin()
    }
}

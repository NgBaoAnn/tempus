package com.projectapp.tempus.domain.model

import java.util.UUID

/**
 * Represents a schedule suggestion parsed from AI response
 * User can preview and accept/reject before saving to database
 */
data class ScheduleSuggestion(
    val id: String = UUID.randomUUID().toString(),
    val name: String,              // Task name (e.g., "Học Toán")
    val startTime: String,         // HH:mm format (e.g., "08:00")
    val endTime: String? = null,   // HH:mm format (optional)
    val durationMinutes: Int,      // Duration in minutes
    val date: String,              // yyyy-MM-dd format
    val priority: Priority = Priority.MEDIUM,
    val isAccepted: Boolean = false
) {
    enum class Priority {
        HIGH, MEDIUM, LOW
    }
    
    /**
     * Calculate end time from start time and duration
     */
    fun calculateEndTime(): String {
        if (endTime != null) return endTime
        
        val parts = startTime.split(":")
        if (parts.size != 2) return startTime
        
        val startHour = parts[0].toIntOrNull() ?: return startTime
        val startMinute = parts[1].toIntOrNull() ?: return startTime
        
        val totalMinutes = startHour * 60 + startMinute + durationMinutes
        val endHour = (totalMinutes / 60) % 24
        val endMinute = totalMinutes % 60
        
        return String.format("%02d:%02d", endHour, endMinute)
    }
    
    /**
     * Format time range for display
     */
    fun formatTimeRange(): String {
        return "$startTime - ${calculateEndTime()}"
    }
    
    /**
     * Convert to implementation time format (HH:MM:SS)
     */
    fun toImplementationTime(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return String.format("%02d:%02d:00", hours, minutes)
    }
}

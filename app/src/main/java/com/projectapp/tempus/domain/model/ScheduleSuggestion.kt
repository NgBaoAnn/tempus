package com.projectapp.tempus.domain.model

import java.util.UUID


data class ScheduleSuggestion(
    val id: String = UUID.randomUUID().toString(),
    val name: String,              
    val startTime: String,         
    val endTime: String? = null,   
    val durationMinutes: Int,      
    val date: String,              
    val priority: Priority = Priority.MEDIUM,
    val isAccepted: Boolean = false
) {
    enum class Priority {
        HIGH, MEDIUM, LOW
    }
    
    
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
    
    
    fun formatTimeRange(): String {
        return "$startTime - ${calculateEndTime()}"
    }
    
    
    fun toImplementationTime(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return String.format("%02d:%02d:00", hours, minutes)
    }
}

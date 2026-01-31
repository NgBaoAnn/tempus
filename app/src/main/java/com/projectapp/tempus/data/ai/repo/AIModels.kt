package com.projectapp.tempus.data.ai.repo

/**
 * Data class for chat messages in AI conversation history
 */
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String? = null
)

/**
 * Schedule slot returned from AI for preview
 */
data class ScheduleSlot(
    val name: String,
    val startTime: String,
    val endTime: String,
    val priority: String,
    val label: String
)

/**
 * Data class for personalization task input
 */
data class PersonalizationTaskInput(
    val name: String,
    val description: String = "",
    val estimatedMinutes: Int,
    val priority: String // "high", "medium", "low"
)

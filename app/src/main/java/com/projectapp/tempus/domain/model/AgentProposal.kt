package com.projectapp.tempus.domain.model

import java.util.UUID

/**
 * Represents an AI-generated proposal for actions
 * This is the dry-run result before user confirmation
 */
data class AgentProposal(
    val id: String = UUID.randomUUID().toString(),
    
    /**
     * Human-readable description of what the AI intends to do
     * Example: "Tạo lịch học cho ngày mai"
     */
    val intent: String,
    
    /**
     * List of specific actions to be performed
     */
    val actions: List<ProposedAction>,
    
    /**
     * Summary of database impact
     * Example: "Thêm 3 công việc mới vào lịch"
     */
    val impact: String,
    
    /**
     * Raw AI response for debugging
     */
    val rawResponse: String,
    
    /**
     * Timestamp when proposal was created
     */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A single action proposed by the AI
 */
data class ProposedAction(
    val type: ActionType,
    val description: String,
    val data: Map<String, Any?> = emptyMap()
) {
    /**
     * Get schedule data if this is a schedule-related action
     */
    fun getScheduleData(): ScheduleActionData? {
        if (type !in listOf(ActionType.CREATE_SCHEDULE, ActionType.UPDATE_SCHEDULE)) {
            return null
        }
        
        return try {
            ScheduleActionData(
                name = data["name"] as? String ?: "",
                startTime = data["start"] as? String 
                    ?: data["startTime"] as? String 
                    ?: data["time"] as? String  // AI sometimes returns "time" instead of "start"
                    ?: "",
                endTime = data["end"] as? String ?: data["endTime"] as? String,
                durationMinutes = (data["duration"] as? Number)?.toInt() ?: 60,
                date = data["date"] as? String
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Schedule-specific action data
 */
data class ScheduleActionData(
    val name: String,
    val startTime: String,
    val endTime: String?,
    val durationMinutes: Int,
    val date: String?
)

/**
 * Types of actions the AI can propose
 */
enum class ActionType {
    CREATE_SCHEDULE,
    UPDATE_SCHEDULE,
    DELETE_SCHEDULE,
    SKIP_INSTANCE,     // Skip one occurrence of recurring activity
    CREATE_NOTE,
    UPDATE_NOTE,
    DELETE_NOTE,
    OTHER
}

/**
 * Result of executing a proposal
 */
data class ExecutionResult(
    /**
     * Whether execution was successful
     */
    val success: Boolean,
    
    /**
     * List of changes that were applied
     */
    val changesApplied: List<String>,
    
    /**
     * Time taken to execute in milliseconds
     */
    val executionTimeMs: Long,
    
    /**
     * Error message if failed
     */
    val errorMessage: String? = null
)

/**
 * Audit log entry for tracking AI actions
 */
data class AuditLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: AuditEventType,
    val proposalId: String?,
    val details: String
)

enum class AuditEventType {
    AI_PROPOSED,
    USER_ACCEPTED,
    USER_CANCELLED,
    EXECUTED,
    EXECUTION_FAILED
}

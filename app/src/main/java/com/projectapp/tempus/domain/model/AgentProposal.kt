package com.projectapp.tempus.domain.model

import java.util.UUID


data class AgentProposal(
    val id: String = UUID.randomUUID().toString(),
    
    
    val intent: String,
    
    
    val actions: List<ProposedAction>,
    
    
    val impact: String,
    
    
    val rawResponse: String,
    
    
    val createdAt: Long = System.currentTimeMillis()
)


data class ProposedAction(
    val type: ActionType,
    val description: String,
    val data: Map<String, Any?> = emptyMap()
) {
    
    fun getScheduleData(): ScheduleActionData? {
        if (type !in listOf(ActionType.CREATE_SCHEDULE, ActionType.UPDATE_SCHEDULE)) {
            return null
        }
        
        return try {
            ScheduleActionData(
                name = data["name"] as? String ?: "",
                startTime = data["start"] as? String 
                    ?: data["startTime"] as? String 
                    ?: data["time"] as? String  
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


data class ScheduleActionData(
    val name: String,
    val startTime: String,
    val endTime: String?,
    val durationMinutes: Int,
    val date: String?
)


enum class ActionType {
    CREATE_SCHEDULE,
    UPDATE_SCHEDULE,
    DELETE_SCHEDULE,
    SKIP_INSTANCE,     
    CREATE_NOTE,
    UPDATE_NOTE,
    DELETE_NOTE,
    OTHER
}


data class ExecutionResult(
    
    val success: Boolean,
    
    
    val changesApplied: List<String>,
    
    
    val executionTimeMs: Long,
    
    
    val errorMessage: String? = null
)


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

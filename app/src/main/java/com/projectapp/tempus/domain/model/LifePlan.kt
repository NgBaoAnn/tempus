package com.projectapp.tempus.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID


data class LifePlan(
    val id: String = UUID.randomUUID().toString(),
    
    
    val title: String,
    
    
    val description: String,
    
    
    val startDate: LocalDate,
    
    
    val endDate: LocalDate,
    
    
    val milestones: List<Milestone>,
    
    
    val estimatedHoursPerWeek: Int,
    
    
    val status: PlanStatus = PlanStatus.DRAFT,
    
    
    val tips: List<String> = emptyList(),
    
    
    val warnings: List<String> = emptyList(),
    
    
    val createdAt: Long = System.currentTimeMillis()
)


enum class PlanStatus {
    DRAFT,      
    ACTIVE,     
    PAUSED,     
    COMPLETED,  
    ARCHIVED    
}


data class Milestone(
    val id: String = UUID.randomUUID().toString(),
    
    
    val title: String,
    
    
    val weekNumber: Int,
    
    
    val targetDate: LocalDate,
    
    
    val scheduledTasks: List<ScheduledTask>,
    
    
    val progress: Float = 0f,
    
    
    val status: MilestoneStatus = MilestoneStatus.PENDING
)


enum class MilestoneStatus {
    PENDING,     
    IN_PROGRESS, 
    COMPLETED,   
    SKIPPED      
}


data class ScheduledTask(
    
    val title: String,
    
    
    val dayOfWeek: DayOfWeek,
    
    
    val preferredTime: String,
    
    
    val durationMinutes: Int,
    
    
    val label: String = "star"
)


data class LifePlanProposal(
    val id: String = UUID.randomUUID().toString(),
    
    
    val plan: LifePlan,
    
    
    val totalTasksToCreate: Int,
    
    
    val rawResponse: String,
    
    
    val createdAt: Long = System.currentTimeMillis()
)


sealed class LifePlanState {
    
    object Idle : LifePlanState()
    
    
    object Analyzing : LifePlanState()
    
    
    data class AwaitingApproval(val proposal: LifePlanProposal) : LifePlanState()
    
    
    object Creating : LifePlanState()
    
    
    data class Done(
        val plan: LifePlan,
        val schedulesCreated: Int
    ) : LifePlanState()
    
    
    data class Error(val message: String) : LifePlanState()
}

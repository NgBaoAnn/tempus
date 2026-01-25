package com.projectapp.tempus.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

/**
 * Represents a long-term life plan created by AI
 * Contains goals broken down into milestones and scheduled tasks
 */
data class LifePlan(
    val id: String = UUID.randomUUID().toString(),
    
    /**
     * Title of the plan (e.g., "Học IELTS 7.0")
     */
    val title: String,
    
    /**
     * Detailed description of the goal
     */
    val description: String,
    
    /**
     * When the plan starts
     */
    val startDate: LocalDate,
    
    /**
     * Target completion date
     */
    val endDate: LocalDate,
    
    /**
     * List of milestones to achieve the goal
     */
    val milestones: List<Milestone>,
    
    /**
     * Estimated hours per week required
     */
    val estimatedHoursPerWeek: Int,
    
    /**
     * Current status of the plan
     */
    val status: PlanStatus = PlanStatus.DRAFT,
    
    /**
     * Helpful tips from AI
     */
    val tips: List<String> = emptyList(),
    
    /**
     * Warnings if plan is too ambitious
     */
    val warnings: List<String> = emptyList(),
    
    /**
     * Timestamp when created
     */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Status of a life plan
 */
enum class PlanStatus {
    DRAFT,      // Created but not started
    ACTIVE,     // Currently in progress
    PAUSED,     // Temporarily paused
    COMPLETED,  // Successfully completed
    ARCHIVED    // Cancelled or archived
}

/**
 * A milestone within a life plan
 * Represents a significant checkpoint toward the goal
 */
data class Milestone(
    val id: String = UUID.randomUUID().toString(),
    
    /**
     * Title of the milestone (e.g., "Hoàn thành Listening basics")
     */
    val title: String,
    
    /**
     * Week number within the plan (1-indexed)
     */
    val weekNumber: Int,
    
    /**
     * Target date to complete this milestone
     */
    val targetDate: LocalDate,
    
    /**
     * Tasks scheduled to achieve this milestone
     */
    val scheduledTasks: List<ScheduledTask>,
    
    /**
     * Completion progress (0.0 - 1.0)
     */
    val progress: Float = 0f,
    
    /**
     * Status of the milestone
     */
    val status: MilestoneStatus = MilestoneStatus.PENDING
)

/**
 * Status of a milestone
 */
enum class MilestoneStatus {
    PENDING,     // Not started yet
    IN_PROGRESS, // Currently working on
    COMPLETED,   // Successfully completed
    SKIPPED      // Skipped by user
}

/**
 * A scheduled task within a milestone
 * Will be converted to actual Schedule entries
 */
data class ScheduledTask(
    /**
     * Task title (e.g., "Luyện nghe IELTS")
     */
    val title: String,
    
    /**
     * Day of week to perform this task
     */
    val dayOfWeek: DayOfWeek,
    
    /**
     * Preferred time (e.g., "09:00")
     */
    val preferredTime: String,
    
    /**
     * Duration in minutes
     */
    val durationMinutes: Int
)

/**
 * AI-generated life plan proposal
 * This is the dry-run result before user confirmation
 */
data class LifePlanProposal(
    val id: String = UUID.randomUUID().toString(),
    
    /**
     * The proposed life plan
     */
    val plan: LifePlan,
    
    /**
     * Total number of tasks that will be created
     */
    val totalTasksToCreate: Int,
    
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
 * State machine for Life Planner Mode
 * 
 * Flow: Idle → Analyzing → AwaitingApproval → Creating → Done
 *                              ↓
 *                           Reject → Idle
 */
sealed class LifePlanState {
    /**
     * Initial state - waiting for user input
     */
    object Idle : LifePlanState()
    
    /**
     * AI is analyzing the goal and creating a plan
     */
    object Analyzing : LifePlanState()
    
    /**
     * Plan ready, waiting for user to approve
     */
    data class AwaitingApproval(val proposal: LifePlanProposal) : LifePlanState()
    
    /**
     * User approved, creating schedules
     */
    object Creating : LifePlanState()
    
    /**
     * Plan created successfully
     */
    data class Done(
        val plan: LifePlan,
        val schedulesCreated: Int
    ) : LifePlanState()
    
    /**
     * Error occurred
     */
    data class Error(val message: String) : LifePlanState()
}

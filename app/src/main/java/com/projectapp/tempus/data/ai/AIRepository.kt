package com.projectapp.tempus.data.ai

import com.projectapp.tempus.data.ai.repo.AgentModeRepository
import com.projectapp.tempus.data.ai.repo.AskModeRepository
import com.projectapp.tempus.data.ai.repo.ChatMessage
import com.projectapp.tempus.data.ai.repo.LifePlannerRepository
import com.projectapp.tempus.data.ai.repo.PersonalizationRepository
import com.projectapp.tempus.data.ai.repo.PersonalizationTaskInput
import com.projectapp.tempus.data.ai.repo.ScheduleSlot
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.domain.model.AgentProposal
import com.projectapp.tempus.domain.model.EnergyContext
import com.projectapp.tempus.domain.model.ExecutionResult
import com.projectapp.tempus.domain.model.LifePlan
import com.projectapp.tempus.domain.model.LifePlanProposal


/**
 * AI Repository Facade
 * 
 * This class acts as a facade that delegates to specialized AI repositories:
 * - AskModeRepository: Q&A conversations, voice commands, chat history
 * - AgentModeRepository: Schedule proposals and execution
 * - LifePlannerRepository: Long-term goal planning
 * - PersonalizationRepository: Schedule generation from preferences
 */
class AIRepository(
    private val scheduleRepository: ScheduleRepository? = null,
    private val userId: String? = null
) {
    
    // Lazy initialization of sub-repositories
    private val askModeRepo by lazy { AskModeRepository() }
    
    private val agentModeRepo by lazy {
        if (scheduleRepository != null && userId != null) {
            AgentModeRepository(scheduleRepository, userId)
        } else null
    }
    
    private val lifePlannerRepo by lazy {
        if (scheduleRepository != null && userId != null) {
            LifePlannerRepository(scheduleRepository, userId)
        } else null
    }
    
    private val personalizationRepo by lazy {
        scheduleRepository?.let { PersonalizationRepository(it) }
    }
    
    // ==================== ASK MODE ====================
    
    /**
     * Send message in Ask Mode with conversation context
     */
    suspend fun sendAskModeMessage(message: String): Result<String> {
        return askModeRepo.sendMessage(message)
    }
    
    /**
     * Alias for sendAskModeMessage
     */
    suspend fun sendMessage(message: String): Result<String> = sendAskModeMessage(message)
    
    /**
     * Parse voice command to JSON format
     */
    suspend fun parseVoiceCommand(prompt: String): Result<String> {
        return askModeRepo.parseVoiceCommand(prompt)
    }
    
    /**
     * Generate a short title for a chat conversation
     */
    suspend fun generateChatTitle(firstMessage: String): Result<String> {
        return askModeRepo.generateChatTitle(firstMessage)
    }
    
    /**
     * Clear conversation history
     */
    fun clearHistory() {
        askModeRepo.clearHistory()
    }
    
    /**
     * Get conversation history as ChatMessage list
     */
    fun getHistory(): List<ChatMessage> {
        return askModeRepo.getHistory()
    }
    
    // ==================== AGENT MODE ====================
    
    /**
     * Type alias for AgentResponse
     */
    sealed class AgentResponse {
        data class Proposal(val proposal: AgentProposal) : AgentResponse()
        data class TextOnly(val text: String) : AgentResponse()
    }
    
    /**
     * Request AI to analyze message and generate schedule proposals
     */
    suspend fun requestProposal(message: String): Result<AgentResponse> {
        val repo = agentModeRepo
            ?: return Result.failure(IllegalStateException("AgentMode requires scheduleRepository and userId"))
        
        return repo.requestProposal(message).map { response ->
            when (response) {
                is AgentModeRepository.AgentResponse.Proposal -> AgentResponse.Proposal(response.proposal)
                is AgentModeRepository.AgentResponse.TextOnly -> AgentResponse.TextOnly(response.text)
            }
        }
    }
    
    /**
     * Execute approved proposal actions
     */
    suspend fun executeProposal(proposal: AgentProposal): Result<ExecutionResult> {
        val repo = agentModeRepo
            ?: return Result.failure(IllegalStateException("AgentMode requires scheduleRepository and userId"))
        
        return repo.executeProposal(proposal)
    }
    
    /**
     * Request schedule suggestions (convenience method)
     */
    suspend fun requestScheduleSuggestions(userRequest: String): Result<String> {
        return requestProposal(userRequest).map { response ->
            when (response) {
                is AgentResponse.Proposal -> response.proposal.rawResponse
                is AgentResponse.TextOnly -> response.text
            }
        }
    }
    
    // ==================== LIFE PLANNER MODE ====================
    
    /**
     * Request AI to generate a life plan for the given goal
     */
    suspend fun requestLifePlan(
        goal: String,
        energyContext: EnergyContext? = null
    ): Result<LifePlanProposal> {
        val repo = lifePlannerRepo
            ?: return Result.failure(IllegalStateException("LifePlanner requires scheduleRepository and userId"))
        
        return repo.requestLifePlan(goal, energyContext)
    }
    
    /**
     * Execute approved life plan - creates schedules in database
     */
    suspend fun executeLifePlan(plan: LifePlan): Result<Int> {
        val repo = lifePlannerRepo
            ?: return Result.failure(IllegalStateException("LifePlanner requires scheduleRepository and userId"))
        
        return repo.executeLifePlan(plan)
    }
    
    // ==================== PERSONALIZATION ====================
    
    /**
     * Generate schedule preview from personalization settings
     */
    suspend fun generateSchedulePreview(
        wakeUpTime: String,
        sleepTime: String,
        tasks: List<PersonalizationTaskInput>,
        activeDays: List<Int>
    ): Result<List<ScheduleSlot>> {
        val repo = personalizationRepo
            ?: return Result.failure(IllegalStateException("Personalization requires scheduleRepository"))
        
        return repo.generateSchedulePreview(wakeUpTime, sleepTime, tasks, activeDays)
    }
    
    /**
     * Save generated schedule slots to database after user confirmation
     */
    suspend fun saveGeneratedSchedulesToDatabase(
        slots: List<ScheduleSlot>,
        activeDays: List<Int>
    ): Result<Int> {
        val repo = personalizationRepo
            ?: return Result.failure(IllegalStateException("Personalization requires scheduleRepository"))
        
        return repo.saveGeneratedSchedulesToDatabase(slots, activeDays)
    }
}

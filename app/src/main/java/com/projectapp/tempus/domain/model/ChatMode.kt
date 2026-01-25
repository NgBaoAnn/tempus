package com.projectapp.tempus.domain.model

/**
 * Chat modes for the AI assistant
 */
enum class ChatMode {
    /**
     * Ask Mode - Q&A only, no database writes
     * AI provides information and explanations
     */
    ASK,
    
    /**
     * Agent Mode - AI proposes actions, user must accept
     * Database writes only happen after user confirmation
     */
    AGENT,
    
    /**
     * Life Planner Mode - Long-term goal planning
     * AI creates multi-week plans with milestones
     */
    LIFE_PLANNER
}

/**
 * State machine for Agent Mode
 * 
 * Flow: Idle → Proposing → AwaitingAccept → Executing → Done
 *                              ↓
 *                           Cancel → Idle
 */
sealed class AgentState {
    /**
     * Initial state - waiting for user input
     */
    object Idle : AgentState()
    
    /**
     * AI is generating a proposal (dry-run)
     */
    object Proposing : AgentState()
    
    /**
     * Proposal ready, waiting for user to Accept/Cancel
     */
    data class AwaitingAccept(val proposal: AgentProposal) : AgentState()
    
    /**
     * User accepted, executing the proposal
     */
    object Executing : AgentState()
    
    /**
     * Execution completed successfully
     */
    data class Done(val result: ExecutionResult) : AgentState()
    
    /**
     * Execution failed
     */
    data class Error(val message: String, val proposal: AgentProposal? = null) : AgentState()
}

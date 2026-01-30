package com.projectapp.tempus.domain.model


enum class ChatMode {
    
    ASK,
    
    
    AGENT,
    
    
    LIFE_PLANNER
}


sealed class AgentState {
    
    object Idle : AgentState()
    
    
    object Proposing : AgentState()
    
    
    data class AwaitingAccept(val proposal: AgentProposal) : AgentState()
    
    
    object Executing : AgentState()
    
    
    data class Done(val result: ExecutionResult) : AgentState()
    
    
    data class Error(val message: String, val proposal: AgentProposal? = null) : AgentState()
}

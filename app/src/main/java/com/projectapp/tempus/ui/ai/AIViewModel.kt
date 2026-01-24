package com.projectapp.tempus.ui.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.ai.AIRepository
import com.projectapp.tempus.data.ai.ChatMessage
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.domain.model.AgentProposal
import com.projectapp.tempus.domain.model.AgentState
import com.projectapp.tempus.domain.model.ChatMode
import com.projectapp.tempus.domain.model.ExecutionResult
import com.projectapp.tempus.domain.model.ScheduleSuggestion
import com.projectapp.tempus.domain.usecase.ParseScheduleSuggestionUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel for AI Chat with Ask/Agent modes
 * Implements state machine for Agent Mode flow
 */
class AIViewModel(
    scheduleRepository: ScheduleRepository? = null,
    userId: String? = null
) : ViewModel() {
    
    private val aiRepository = AIRepository(scheduleRepository, userId)
    private val parseScheduleUseCase = ParseScheduleSuggestionUseCase()
    
    // ============================================
    // CHAT MODE STATE
    // ============================================
    
    private val _chatMode = MutableLiveData(ChatMode.ASK)
    val chatMode: LiveData<ChatMode> = _chatMode
    
    // ============================================
    // AGENT STATE MACHINE
    // ============================================
    
    private val _agentState = MutableLiveData<AgentState>(AgentState.Idle)
    val agentState: LiveData<AgentState> = _agentState
    
    // ============================================
    // CHAT MESSAGES
    // ============================================
    
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // ============================================
    // SCHEDULE SUGGESTIONS (Legacy support)
    // ============================================
    
    private val _suggestions = MutableLiveData<List<ScheduleSuggestion>>(emptyList())
    val suggestions: LiveData<List<ScheduleSuggestion>> = _suggestions
    
    private val _showSuggestionSheet = MutableLiveData(false)
    val showSuggestionSheet: LiveData<Boolean> = _showSuggestionSheet
    
    private var welcomeMessageShown = false
    
    init {
        showWelcomeMessage()
    }
    
    // ============================================
    // MODE TOGGLE
    // ============================================
    
    fun toggleMode() {
        val newMode = when (_chatMode.value) {
            ChatMode.ASK -> ChatMode.AGENT
            ChatMode.AGENT -> ChatMode.ASK
            else -> ChatMode.ASK
        }
        setMode(newMode)
    }
    
    fun setMode(mode: ChatMode) {
        // Reset agent state when switching modes
        if (_chatMode.value != mode) {
            _agentState.value = AgentState.Idle
        }
        _chatMode.value = mode
        
        // Add mode switch message
        val modeMessage = when (mode) {
            ChatMode.ASK -> "💬 Đã chuyển sang chế độ Ask. Tôi sẽ trả lời câu hỏi của bạn."
            ChatMode.AGENT -> "🤖 Đã chuyển sang chế độ Agent. Tôi sẽ đề xuất hành động và chờ bạn xác nhận."
        }
        
        addSystemMessage(modeMessage)
    }
    
    // ============================================
    // SEND MESSAGE (MODE-AWARE)
    // ============================================
    
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            addUserMessage(text)
            _isLoading.value = true
            _error.value = null
            
            when (_chatMode.value) {
                ChatMode.ASK -> handleAskMode(text)
                ChatMode.AGENT -> handleAgentMode(text)
                else -> handleAskMode(text)
            }
            
            _isLoading.value = false
        }
    }
    
    // ============================================
    // ASK MODE HANDLER
    // ============================================
    
    private suspend fun handleAskMode(text: String) {
        val result = aiRepository.sendAskModeMessage(text)
        
        result.onSuccess { responseText ->
            addAIMessage(responseText)
        }.onFailure { exception ->
            handleError(exception)
        }
    }
    
    // ============================================
    // AGENT MODE HANDLERS
    // ============================================
    
    private suspend fun handleAgentMode(text: String) {
        // Step 1: Request proposal (dry-run)
        _agentState.value = AgentState.Proposing
        
        val result = aiRepository.requestProposal(text)
        
        result.onSuccess { response ->
            when (response) {
                is AIRepository.AgentResponse.Proposal -> {
                    // Got a structured proposal - show proposal card
                    _agentState.value = AgentState.AwaitingAccept(response.proposal)
                    addAIMessage("📋 Tôi đã chuẩn bị một đề xuất cho bạn. Vui lòng xem xét và Accept hoặc Cancel.")
                }
                is AIRepository.AgentResponse.TextOnly -> {
                    // AI responded with text (not an action request)
                    _agentState.value = AgentState.Idle
                    addAIMessage(response.text)
                }
            }
        }.onFailure { exception ->
            _agentState.value = AgentState.Error(exception.message ?: "Không thể xử lý yêu cầu")
            handleError(exception)
        }
    }
    
    /**
     * Accept the current proposal (Step 2: Execute)
     */
    fun acceptProposal() {
        val currentState = _agentState.value
        if (currentState !is AgentState.AwaitingAccept) return
        
        val proposal = currentState.proposal
        
        viewModelScope.launch {
            _agentState.value = AgentState.Executing
            _isLoading.value = true
            
            val result = aiRepository.executeProposal(proposal)
            
            result.onSuccess { executionResult ->
                _agentState.value = AgentState.Done(executionResult)
                
                // Add success message to chat
                val changesText = executionResult.changesApplied.joinToString("\n") { "• $it" }
                addAIMessage("✅ Đã thực hiện thành công!\n\n$changesText\n\n⏱️ ${executionResult.executionTimeMs}ms")
                
            }.onFailure { exception ->
                _agentState.value = AgentState.Error(
                    exception.message ?: "Lỗi khi thực hiện",
                    proposal
                )
                addAIMessage("❌ Thực hiện thất bại: ${exception.message}")
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Cancel the current proposal
     */
    fun cancelProposal() {
        _agentState.value = AgentState.Idle
        addSystemMessage("🚫 Đã hủy đề xuất.")
    }
    
    /**
     * Reset agent state (after viewing Done/Error)
     */
    fun resetAgentState() {
        _agentState.value = AgentState.Idle
    }
    
    // ============================================
    // HELPER METHODS
    // ============================================
    
    private fun showWelcomeMessage() {
        if (!welcomeMessageShown) {
            val welcomeMessage = ChatMessage(
                text = "Xin chào! 👋 Tôi là Tiramisu AI.\n\n" +
                       "💬 **Ask Mode**: Hỏi đáp, tư vấn\n" +
                       "🤖 **Agent Mode**: Đề xuất và thực hiện hành động\n\n" +
                       "Chuyển đổi chế độ bằng toggle ở trên!",
                isFromUser = false
            )
            _messages.value = listOf(welcomeMessage)
            welcomeMessageShown = true
        }
    }
    
    private fun addUserMessage(text: String) {
        val message = ChatMessage(text = text.trim(), isFromUser = true)
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(message)
        _messages.value = currentMessages
    }
    
    private fun addAIMessage(text: String) {
        val message = ChatMessage(text = text, isFromUser = false)
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(message)
        _messages.value = currentMessages
    }
    
    private fun addSystemMessage(text: String) {
        addAIMessage(text)
    }
    
    private fun handleError(exception: Throwable) {
        _error.value = exception.message ?: "Đã xảy ra lỗi"
        addAIMessage("❌ Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau.")
    }
    
    // ============================================
    // LEGACY SUPPORT
    // ============================================
    
    fun acceptSuggestions(accepted: List<ScheduleSuggestion>) {
        viewModelScope.launch {
            _showSuggestionSheet.value = false
            
            val confirmMessage = ChatMessage(
                text = "✅ Đã thêm ${accepted.size} công việc vào lịch!",
                isFromUser = false
            )
            val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
            updatedMessages.add(confirmMessage)
            _messages.value = updatedMessages
            
            _suggestions.value = emptyList()
        }
    }
    
    fun dismissSuggestions() {
        _showSuggestionSheet.value = false
        _suggestions.value = emptyList()
    }
    
    fun clearChat() {
        aiRepository.clearHistory()
        welcomeMessageShown = false
        _messages.value = emptyList()
        _error.value = null
        _agentState.value = AgentState.Idle
        _suggestions.value = emptyList()
        _showSuggestionSheet.value = false
        showWelcomeMessage()
    }
    
    fun clearError() {
        _error.value = null
    }
}

package com.projectapp.tempus.ui.ai

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.ai.AIHistoryRepository
import com.projectapp.tempus.data.ai.AIRepository
import com.projectapp.tempus.data.ai.ChatMessage
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.domain.model.AgentProposal
import com.projectapp.tempus.domain.model.AgentState
import com.projectapp.tempus.domain.model.ChatMode
import com.projectapp.tempus.domain.model.EnergyContext
import com.projectapp.tempus.domain.model.ExecutionResult
import com.projectapp.tempus.domain.model.LifePlanProposal
import com.projectapp.tempus.domain.model.LifePlanState
import com.projectapp.tempus.domain.model.ScheduleSuggestion
import com.projectapp.tempus.domain.usecase.ParseScheduleSuggestionUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.projectapp.tempus.R


class AIViewModel(
    application: Application,
    scheduleRepository: ScheduleRepository? = null,
    userId: String? = null
) : AndroidViewModel(application) {
    
    private val aiRepository = AIRepository(scheduleRepository, userId)
    private val aiHistoryRepository = AIHistoryRepository()
    private val parseScheduleUseCase = ParseScheduleSuggestionUseCase()
    
    
    private var historyLoaded = false
    
    
    private val _chatMode = MutableLiveData(ChatMode.ASK)
    val chatMode: LiveData<ChatMode> = _chatMode
    
    
    private val _agentState = MutableLiveData<AgentState>(AgentState.Idle)
    val agentState: LiveData<AgentState> = _agentState
    
    
    private val _lifePlanState = MutableLiveData<LifePlanState>(LifePlanState.Idle)
    val lifePlanState: LiveData<LifePlanState> = _lifePlanState
    
    
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    
    private val _suggestions = MutableLiveData<List<ScheduleSuggestion>>(emptyList())
    val suggestions: LiveData<List<ScheduleSuggestion>> = _suggestions
    
    private val _showSuggestionSheet = MutableLiveData(false)
    val showSuggestionSheet: LiveData<Boolean> = _showSuggestionSheet
    
    
    private val _showHistorySheet = MutableLiveData(false)
    val showHistorySheet: LiveData<Boolean> = _showHistorySheet
    
    private val _historySessions = MutableLiveData<List<com.projectapp.tempus.ui.ai.compose.ChatSession>>(emptyList())
    val historySessions: LiveData<List<com.projectapp.tempus.ui.ai.compose.ChatSession>> = _historySessions
    
    private val _isLoadingHistory = MutableLiveData(false)
    val isLoadingHistory: LiveData<Boolean> = _isLoadingHistory
    
    
    private var currentSessionId: String = java.util.UUID.randomUUID().toString()
    private var currentSessionTitle: String? = null
    private var isFirstMessageInSession: Boolean = true
    
    private var welcomeMessageShown = false
    
    init {
        loadChatHistory()
    }
    
    
    private fun loadChatHistory() {
        if (historyLoaded) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val history = aiHistoryRepository.getHistoryForDisplay(limit = 100)
                
                if (history.isEmpty()) {
                    
                    showWelcomeMessage()
                } else {
                    
                    val messages = mutableListOf<ChatMessage>()
                    
                    for (record in history) {
                        
                        record.prompt?.let { prompt ->
                            messages.add(ChatMessage(
                                text = prompt,
                                isFromUser = true,
                                id = "${record.id}_prompt"
                            ))
                        }
                        
                        
                        record.response?.let { response ->
                            messages.add(ChatMessage(
                                text = response,
                                isFromUser = false,
                                id = "${record.id}_response"
                            ))
                        }
                    }
                    
                    _messages.value = messages
                    welcomeMessageShown = true
                    Log.d("AIViewModel", "Loaded ${history.size} conversation records")
                }
                
                historyLoaded = true
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to load chat history: ${e.message}", e)
                
                showWelcomeMessage()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    
    private fun saveToHistory(prompt: String, response: String) {
        val mode = when (_chatMode.value) {
            ChatMode.ASK -> "ask"
            ChatMode.AGENT -> "agent"
            ChatMode.LIFE_PLANNER -> "life_planner"
            else -> "ask"
        }
        
        viewModelScope.launch {
            try {
                
                val shouldGenerateTitle = isFirstMessageInSession && currentSessionTitle == null
                if (shouldGenerateTitle) {
                    
                    val titleResult = aiRepository.generateChatTitle(prompt)
                    titleResult.onSuccess { title ->
                        currentSessionTitle = title
                        Log.d("AIViewModel", "Generated session title: $title")
                    }.onFailure { e ->
                        Log.e("AIViewModel", "Title generation failed: ${e.message}")
                        
                        currentSessionTitle = prompt.take(30)
                    }
                }
                
                
                aiHistoryRepository.saveConversation(
                    prompt = prompt,
                    response = response,
                    sessionId = currentSessionId,
                    title = currentSessionTitle,
                    mode = mode
                )
                
                isFirstMessageInSession = false
                Log.d("AIViewModel", "Saved conversation with sessionId=$currentSessionId, title=$currentSessionTitle")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to save conversation: ${e.message}", e)
            }
        }
    }
    
    
    fun startNewChat() {
        
        currentSessionId = java.util.UUID.randomUUID().toString()
        currentSessionTitle = null
        isFirstMessageInSession = true
        
        
        _messages.value = emptyList()
        aiRepository.clearHistory()
        welcomeMessageShown = false
        showWelcomeMessage()
        
        
        _agentState.value = AgentState.Idle
        _lifePlanState.value = LifePlanState.Idle
        
        
        _showHistorySheet.value = false
        
        Log.d("AIViewModel", "Started new chat session: $currentSessionId")
    }
    
    
    fun openHistorySheet() {
        _showHistorySheet.value = true
        loadHistorySessions()
    }
    
    
    fun closeHistorySheet() {
        _showHistorySheet.value = false
    }
    
    
    private fun loadHistorySessions() {
        viewModelScope.launch {
            _isLoadingHistory.value = true
            try {
                val sessionList = aiHistoryRepository.getSessionList()
                
                val sessions = sessionList.map { record ->
                    val sessionId = record.sessionId ?: return@map null
                    
                    
                    val displayTitle = record.title 
                        ?: formatDisplayDate(record.createdAt?.take(10) ?: "")
                    
                    val previewText = record.prompt?.take(100) ?: ""
                    
                    com.projectapp.tempus.ui.ai.compose.ChatSession(
                        sessionId = sessionId,
                        title = displayTitle,
                        date = record.createdAt?.take(10) ?: "",
                        displayDate = formatDisplayDate(record.createdAt?.take(10) ?: ""),
                        conversations = emptyList(), 
                        previewText = previewText,
                        messageCount = 0
                    )
                }.filterNotNull()
                
                _historySessions.value = sessions
                Log.d("AIViewModel", "Loaded ${sessions.size} chat sessions")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to load history sessions: ${e.message}", e)
                _historySessions.value = emptyList()
            } finally {
                _isLoadingHistory.value = false
            }
        }
    }
    
    
    fun loadSession(session: com.projectapp.tempus.ui.ai.compose.ChatSession) {
        viewModelScope.launch {
            try {
                
                val sessionMessages = aiHistoryRepository.getSessionMessages(session.sessionId)
                
                
                val messages = mutableListOf<ChatMessage>()
                
                for (record in sessionMessages) {
                    record.prompt?.let { prompt ->
                        messages.add(ChatMessage(
                            text = prompt,
                            isFromUser = true,
                            id = "${record.id}_prompt"
                        ))
                    }
                    
                    record.response?.let { response ->
                        messages.add(ChatMessage(
                            text = response,
                            isFromUser = false,
                            id = "${record.id}_response"
                        ))
                    }
                }
                
                _messages.value = messages
                welcomeMessageShown = true
                _showHistorySheet.value = false
                
                
                currentSessionId = session.sessionId
                currentSessionTitle = session.title
                isFirstMessageInSession = false
                
                Log.d("AIViewModel", "Loaded session ${session.sessionId} with ${messages.size} messages")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to load session: ${e.message}", e)
            }
        }
    }
    
    
    fun deleteSession(session: com.projectapp.tempus.ui.ai.compose.ChatSession) {
        viewModelScope.launch {
            try {
                
                aiHistoryRepository.deleteSession(session.sessionId)
                
                
                loadHistorySessions()
                Log.d("AIViewModel", "Deleted session: ${session.sessionId}")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to delete session: ${e.message}", e)
            }
        }
    }
    
    
    private fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = java.time.LocalDate.parse(dateStr)
            val today = java.time.LocalDate.now()
            
            when {
                date == today -> getApplication<Application>().getString(R.string.ai_history_today)
                date == today.minusDays(1) -> getApplication<Application>().getString(R.string.ai_history_yesterday)
                else -> {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    date.format(formatter)
                }
            }
        } catch (e: Exception) {
            dateStr
        }
    }
    
    
    fun toggleMode() {
        val newMode = when (_chatMode.value) {
            ChatMode.ASK -> ChatMode.AGENT
            ChatMode.AGENT -> ChatMode.LIFE_PLANNER
            ChatMode.LIFE_PLANNER -> ChatMode.ASK
            else -> ChatMode.ASK
        }
        setMode(newMode)
    }
    
    fun setMode(mode: ChatMode) {
        
        if (_chatMode.value != mode) {
            _agentState.value = AgentState.Idle
            _lifePlanState.value = LifePlanState.Idle
        }
        _chatMode.value = mode
        
        
        val modeMessage = when (mode) {
            ChatMode.ASK -> getApplication<Application>().getString(R.string.ai_mode_switch_ask)
            ChatMode.AGENT -> getApplication<Application>().getString(R.string.ai_mode_switch_agent)
            ChatMode.LIFE_PLANNER -> getApplication<Application>().getString(R.string.ai_mode_switch_planner)
        }
        
        addSystemMessage(modeMessage)
    }
    
    
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            addUserMessage(text)
            _isLoading.value = true
            _error.value = null
            
            when (_chatMode.value) {
                ChatMode.ASK -> handleAskMode(text)
                ChatMode.AGENT -> handleAgentMode(text)
                ChatMode.LIFE_PLANNER -> handleLifePlannerMode(text)
                else -> handleAskMode(text)
            }
            
            _isLoading.value = false
        }
    }
    
    
    private suspend fun handleAskMode(text: String) {
        val result = aiRepository.sendAskModeMessage(text)
        
        result.onSuccess { responseText ->
            addAIMessage(responseText)
            
            saveToHistory(text, responseText)
        }.onFailure { exception ->
            handleError(exception)
        }
    }
    
    
    private suspend fun handleAgentMode(text: String) {
        
        _agentState.value = AgentState.Proposing
        
        val result = aiRepository.requestProposal(text)
        
        result.onSuccess { response ->
            when (response) {
                is AIRepository.AgentResponse.Proposal -> {
                    
                    _agentState.value = AgentState.AwaitingAccept(response.proposal)
                    val responseText = getApplication<Application>().getString(R.string.ai_proposal_ready)
                    addAIMessage(responseText)
                    
                    saveToHistory(text, responseText + "\n[Proposal: ${response.proposal.intent}]")
                }
                is AIRepository.AgentResponse.TextOnly -> {
                    
                    _agentState.value = AgentState.Idle
                    addAIMessage(response.text)
                    
                    saveToHistory(text, response.text)
                }
            }
        }.onFailure { exception ->
            _agentState.value = AgentState.Error(exception.message ?: "Không thể xử lý yêu cầu")
            handleError(exception)
        }
    }
    
    
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
                
                
                val changesText = executionResult.changesApplied.joinToString("\n") { "• $it" }
                val context = getApplication<Application>()
                addAIMessage(context.getString(R.string.ai_proposal_executed_success, changesText, executionResult.executionTimeMs))
                
            }.onFailure { exception ->
                _agentState.value = AgentState.Error(
                    exception.message ?: getApplication<Application>().getString(R.string.msg_error),
                    proposal
                )
                addAIMessage(getApplication<Application>().getString(R.string.ai_proposal_failed, exception.message))
            }
            
            _isLoading.value = false
        }
    }
    
    
    fun cancelProposal() {
        _agentState.value = AgentState.Idle
        addSystemMessage(getApplication<Application>().getString(R.string.ai_proposal_cancelled))
    }
    
    
    fun resetAgentState() {
        _agentState.value = AgentState.Idle
    }
    
    
    private suspend fun handleLifePlannerMode(text: String) {
        _lifePlanState.value = LifePlanState.Analyzing
        addAIMessage(getApplication<Application>().getString(R.string.ai_planner_analyzing))
        
        val result = aiRepository.requestLifePlan(text)
        
        result.onSuccess { proposal ->
            _lifePlanState.value = LifePlanState.AwaitingApproval(proposal)
            
            val plan = proposal.plan
            val summaryMessage = """
                |🎯 **${plan.title}**
                |
                |📝 ${plan.description}
                |
                |📅 **Milestones:** ${plan.milestones.size}
                |⏰ **Est:** ~${plan.estimatedHoursPerWeek}h/week
                |📋 **Tasks:** ~${proposal.totalTasksToCreate}
                |
                |${if (plan.warnings.isNotEmpty()) "⚠️ " + plan.warnings.joinToString("\n") else ""}
            """.trimMargin()
            
            addAIMessage(summaryMessage)
            
            saveToHistory(text, summaryMessage)
        }.onFailure { exception ->
            _lifePlanState.value = LifePlanState.Error(exception.message ?: getApplication<Application>().getString(R.string.ai_planner_failed, ""))
            addAIMessage(getApplication<Application>().getString(R.string.ai_planner_failed, exception.message))
        }
    }
    
    
    fun acceptLifePlan() {
        val currentState = _lifePlanState.value
        if (currentState !is LifePlanState.AwaitingApproval) return
        
        val proposal = currentState.proposal
        
        viewModelScope.launch {
            _lifePlanState.value = LifePlanState.Creating
            _isLoading.value = true
            
            val result = aiRepository.executeLifePlan(proposal.plan)
            
            result.onSuccess { schedulesCreated ->
                _lifePlanState.value = LifePlanState.Done(
                    plan = proposal.plan,
                    schedulesCreated = schedulesCreated
                )
                
                val tips = proposal.plan.tips.joinToString("\n") { "• $it" }
                addAIMessage(
                    getApplication<Application>().getString(R.string.ai_planner_success, schedulesCreated, tips)
                )
            }.onFailure { exception ->
                _lifePlanState.value = LifePlanState.Error(exception.message ?: getApplication<Application>().getString(R.string.ai_planner_failed, ""))
                addAIMessage(getApplication<Application>().getString(R.string.ai_planner_failed, exception.message))
            }
            
            _isLoading.value = false
        }
    }
    
    
    fun rejectLifePlan() {
        _lifePlanState.value = LifePlanState.Idle
        addSystemMessage(getApplication<Application>().getString(R.string.ai_planner_cancelled))
    }
    
    
    fun resetLifePlanState() {
        _lifePlanState.value = LifePlanState.Idle
    }
    
    
    private fun showWelcomeMessage() {
        if (!welcomeMessageShown) {
            val welcomeMessage = ChatMessage(
                text = getApplication<Application>().getString(R.string.ai_welcome_message),
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
        _error.value = exception.message ?: getApplication<Application>().getString(R.string.msg_error)
        addAIMessage(getApplication<Application>().getString(R.string.msg_error))
    }
    
    
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
        viewModelScope.launch {
            
            try {
                aiHistoryRepository.clearHistory()
                Log.d("AIViewModel", "Cleared history from database")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to clear history from database: ${e.message}", e)
            }
        }
        
        
        aiRepository.clearHistory()
        welcomeMessageShown = false
        historyLoaded = false
        _messages.value = emptyList()
        _error.value = null
        _agentState.value = AgentState.Idle
        _lifePlanState.value = LifePlanState.Idle
        _suggestions.value = emptyList()
        _showSuggestionSheet.value = false
        showWelcomeMessage()
    }
    
    fun clearError() {
        _error.value = null
    }
}

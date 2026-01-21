package com.projectapp.tempus.ui.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.ai.AIRepository
import com.projectapp.tempus.data.ai.ChatMessage
import com.projectapp.tempus.domain.model.ScheduleSuggestion
import com.projectapp.tempus.domain.usecase.ParseScheduleSuggestionUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel for AI Chat Fragment
 * Manages chat state, communicates with AIRepository, and handles schedule suggestions
 */
class AIViewModel : ViewModel() {
    
    private val aiRepository = AIRepository()
    private val parseScheduleUseCase = ParseScheduleSuggestionUseCase()
    
    // Chat messages list
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages
    
    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error state
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Schedule suggestions state
    private val _suggestions = MutableLiveData<List<ScheduleSuggestion>>(emptyList())
    val suggestions: LiveData<List<ScheduleSuggestion>> = _suggestions
    
    // Show suggestions sheet
    private val _showSuggestionSheet = MutableLiveData(false)
    val showSuggestionSheet: LiveData<Boolean> = _showSuggestionSheet
    
    // Welcome message shown flag
    private var welcomeMessageShown = false
    
    // Keywords that trigger schedule parsing
    private val scheduleKeywords = listOf(
        "lên lịch", "lập lịch", "tạo lịch", "sắp xếp lịch",
        "schedule", "plan", "lịch học", "lịch làm việc",
        "thời gian biểu", "kế hoạch", "lịch trình"
    )
    
    init {
        showWelcomeMessage()
    }
    
    /**
     * Show initial welcome message from AI
     */
    private fun showWelcomeMessage() {
        if (!welcomeMessageShown) {
            val welcomeMessage = ChatMessage(
                text = "Xin chào! 👋 Tôi là Tiramisu AI, trợ lý lập kế hoạch thông minh của bạn.\n\nTôi có thể giúp bạn:\n• Lên lịch công việc hàng ngày\n• Đề xuất thời gian phù hợp\n• Nhắc nhở về deadline\n• Tư vấn quản lý thời gian\n\nHãy cho tôi biết bạn cần gì nhé!",
                isFromUser = false
            )
            _messages.value = listOf(welcomeMessage)
            welcomeMessageShown = true
        }
    }
    
    /**
     * Check if message is a scheduling request
     */
    private fun isSchedulingRequest(text: String): Boolean {
        val lowerText = text.lowercase()
        return scheduleKeywords.any { lowerText.contains(it) }
    }
    
    /**
     * Send a message to AI
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            // Add user message to list
            val userMessage = ChatMessage(
                text = text.trim(),
                isFromUser = true
            )
            
            val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
            currentMessages.add(userMessage)
            _messages.value = currentMessages
            
            // Show loading
            _isLoading.value = true
            _error.value = null
            
            // Check if this is a scheduling request
            if (isSchedulingRequest(text)) {
                handleSchedulingRequest(text)
            } else {
                handleNormalMessage(text)
            }
        }
    }
    
    /**
     * Handle normal chat message
     */
    private suspend fun handleNormalMessage(text: String) {
        val result = aiRepository.sendMessage(text)
        
        result.onSuccess { responseText ->
            val aiMessage = ChatMessage(
                text = responseText,
                isFromUser = false
            )
            val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
            updatedMessages.add(aiMessage)
            _messages.value = updatedMessages
        }.onFailure { exception ->
            handleError(exception)
        }
        
        _isLoading.value = false
    }
    
    /**
     * Handle scheduling request - get structured response and parse suggestions
     */
    private suspend fun handleSchedulingRequest(text: String) {
        val result = aiRepository.requestScheduleSuggestions(text)
        
        result.onSuccess { responseText ->
            // Parse suggestions from AI response
            val targetDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val parsedSuggestions = parseScheduleUseCase.parse(responseText, targetDate)
            
            if (parsedSuggestions.isNotEmpty()) {
                // Show response in chat
                val aiMessage = ChatMessage(
                    text = "📅 Tôi đã tạo ${parsedSuggestions.size} gợi ý lịch trình cho bạn. Vui lòng xem và chọn những mục bạn muốn thêm vào lịch!",
                    isFromUser = false
                )
                val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
                updatedMessages.add(aiMessage)
                _messages.value = updatedMessages
                
                // Update suggestions and show sheet
                _suggestions.value = parsedSuggestions
                _showSuggestionSheet.value = true
            } else {
                // Fallback: show raw response if parsing fails
                val aiMessage = ChatMessage(
                    text = responseText,
                    isFromUser = false
                )
                val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
                updatedMessages.add(aiMessage)
                _messages.value = updatedMessages
            }
        }.onFailure { exception ->
            handleError(exception)
        }
        
        _isLoading.value = false
    }
    
    /**
     * Handle error response
     */
    private fun handleError(exception: Throwable) {
        _error.value = exception.message ?: "Đã xảy ra lỗi khi kết nối với AI"
        
        val errorMessage = ChatMessage(
            text = "❌ Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau.",
            isFromUser = false
        )
        val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
        updatedMessages.add(errorMessage)
        _messages.value = updatedMessages
    }
    
    /**
     * Accept selected schedule suggestions
     * TODO: Integrate with ScheduleRepository to create real tasks
     */
    fun acceptSuggestions(accepted: List<ScheduleSuggestion>) {
        viewModelScope.launch {
            // Close sheet
            _showSuggestionSheet.value = false
            
            // Add confirmation message
            val confirmMessage = ChatMessage(
                text = "✅ Đã thêm ${accepted.size} công việc vào lịch của bạn!\n\n" +
                       accepted.joinToString("\n") { "• ${it.formatTimeRange()} - ${it.name}" },
                isFromUser = false
            )
            val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
            updatedMessages.add(confirmMessage)
            _messages.value = updatedMessages
            
            // Clear suggestions
            _suggestions.value = emptyList()
            
            // TODO: Actually save to database via ScheduleRepository
            // This requires injecting ScheduleRepository and user ID
        }
    }
    
    /**
     * Dismiss suggestions sheet
     */
    fun dismissSuggestions() {
        _showSuggestionSheet.value = false
        _suggestions.value = emptyList()
    }
    
    /**
     * Clear chat history
     */
    fun clearChat() {
        aiRepository.clearHistory()
        welcomeMessageShown = false
        _messages.value = emptyList()
        _error.value = null
        _suggestions.value = emptyList()
        _showSuggestionSheet.value = false
        showWelcomeMessage()
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
}

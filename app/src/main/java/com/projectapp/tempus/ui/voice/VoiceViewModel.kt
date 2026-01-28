package com.projectapp.tempus.ui.voice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.ai.AIRepository
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.data.voice.SpeechRecognitionManager
import com.projectapp.tempus.data.voice.SpeechResult
import com.projectapp.tempus.data.voice.TaskParserService
import com.projectapp.tempus.data.voice.dto.ParsedTask
import com.projectapp.tempus.ui.voice.compose.VoiceInputState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel for Voice Input functionality
 * 
 * Flow: Voice → Speech-to-Text → AI API (Gemini) → ParsedTask → Create Schedule
 */
class VoiceViewModel(
    application: Application,
    private val scheduleRepository: ScheduleRepository,
    private val userId: String
) : AndroidViewModel(application) {
    
    // Reuse existing AI infrastructure
    private val aiRepository = AIRepository(scheduleRepository, userId)
    private val speechManager = SpeechRecognitionManager(application)
    // Legacy parser no longer needed for new flow, but keeping if we need fallback logic later
    // private val taskParser = TaskParserService(aiRepository) 
    
    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    val state: StateFlow<VoiceInputState> = _state.asStateFlow()
    
    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()
    
    private var currentProposal: com.projectapp.tempus.domain.model.AgentProposal? = null
    
    /**
     * Check if speech recognition is available
     */
    fun isAvailable(): Boolean = speechManager.isAvailable()
    
    /**
     * Start voice recognition
     */
    fun startListening() {
        _state.value = VoiceInputState.Listening
        _partialText.value = ""
        
        viewModelScope.launch {
            speechManager.startListening().collect { result ->
                when (result) {
                    is SpeechResult.Listening -> _state.value = VoiceInputState.Listening
                    is SpeechResult.Speaking -> { /* Keep listening state */ }
                    is SpeechResult.RmsChanged -> { /* Could update waveform */ }
                    is SpeechResult.Partial -> _partialText.value = result.text
                    is SpeechResult.Processing -> _state.value = VoiceInputState.Processing
                    is SpeechResult.Success -> processVoiceInput(result.text)
                    is SpeechResult.Error -> _state.value = VoiceInputState.Error(result.message)
                }
            }
        }
    }
    
    /**
     * Stop voice recognition
     */
    fun stopListening() {
        speechManager.stopListening()
        if (_partialText.value.isNotEmpty()) {
            processVoiceInput(_partialText.value)
        } else {
            _state.value = VoiceInputState.Idle
        }
    }
    
    /**
     * Process voice input text using AI Agent
     */
    private fun processVoiceInput(text: String) {
        viewModelScope.launch {
            _state.value = VoiceInputState.Processing
            
            // Use AI Repository to get a full proposal (Add/Edit/Delete supported)
            val result = aiRepository.requestProposal(text)
            
            result.onSuccess { response ->
                when (response) {
                    is AIRepository.AgentResponse.Proposal -> {
                        currentProposal = response.proposal
                        _state.value = VoiceInputState.ProposalReady(response.proposal)
                    }
                    is AIRepository.AgentResponse.TextOnly -> {
                        // For voice, if it's text only, we treat it as an error/info
                        _state.value = VoiceInputState.Error("AI: ${response.text}")
                    }
                }
            }.onFailure { e ->
                _state.value = VoiceInputState.Error("Lỗi kết nối AI: ${e.message}")
            }
        }
    }
    
    /**
     * Confirm and execute the current proposal
     */
    fun confirmProposal() {
        val proposal = currentProposal ?: return
        
        viewModelScope.launch {
            try {
                _state.value = VoiceInputState.Processing
                
                val result = aiRepository.executeProposal(proposal)
                
                result.onSuccess {
                    // Success - reset state
                    reset()
                }.onFailure { e ->
                    _state.value = VoiceInputState.Error("Không thể thực hiện: ${e.message}")
                }
                
            } catch (e: Exception) {
                _state.value = VoiceInputState.Error("Lỗi thực thi: ${e.message}")
            }
        }
    }
    
    /**
     * Legacy support method - not used in new flow but kept for interface compatibility if needed
     */
    fun createTask(task: ParsedTask) {
        // Redirect to new flow if possible, or just ignore
    }
    
    /**
     * Get final task for creation - Legacy
     */
    fun getFinalTask(): ParsedTask? = null // Deprecated
    
    /**
     * Reset state
     */
    fun reset() {
        _state.value = VoiceInputState.Idle
        _partialText.value = ""
        currentProposal = null
    }
    
    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
    }
}

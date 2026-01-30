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


class VoiceViewModel(
    application: Application,
    private val scheduleRepository: ScheduleRepository,
    private val userId: String
) : AndroidViewModel(application) {
    
    
    private val aiRepository = AIRepository(scheduleRepository, userId)
    private val speechManager = SpeechRecognitionManager(application)
    
    
    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    val state: StateFlow<VoiceInputState> = _state.asStateFlow()
    
    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()
    
    private var currentProposal: com.projectapp.tempus.domain.model.AgentProposal? = null
    
    
    fun isAvailable(): Boolean = speechManager.isAvailable()
    
    
    fun startListening() {
        _state.value = VoiceInputState.Listening
        _partialText.value = ""
        
        viewModelScope.launch {
            speechManager.startListening().collect { result ->
                when (result) {
                    is SpeechResult.Listening -> _state.value = VoiceInputState.Listening
                    is SpeechResult.Speaking -> {  }
                    is SpeechResult.RmsChanged -> {  }
                    is SpeechResult.Partial -> _partialText.value = result.text
                    is SpeechResult.Processing -> _state.value = VoiceInputState.Processing
                    is SpeechResult.Success -> processVoiceInput(result.text)
                    is SpeechResult.Error -> _state.value = VoiceInputState.Error(result.message)
                }
            }
        }
    }
    
    
    fun stopListening() {
        speechManager.stopListening()
        if (_partialText.value.isNotEmpty()) {
            processVoiceInput(_partialText.value)
        } else {
            _state.value = VoiceInputState.Idle
        }
    }
    
    
    private fun processVoiceInput(text: String) {
        viewModelScope.launch {
            _state.value = VoiceInputState.Processing
            
            
            val result = aiRepository.requestProposal(text)
            
            result.onSuccess { response ->
                when (response) {
                    is AIRepository.AgentResponse.Proposal -> {
                        currentProposal = response.proposal
                        _state.value = VoiceInputState.ProposalReady(response.proposal)
                    }
                    is AIRepository.AgentResponse.TextOnly -> {
                        
                        _state.value = VoiceInputState.Error("AI: ${response.text}")
                    }
                }
            }.onFailure { e ->
                _state.value = VoiceInputState.Error("Lỗi kết nối AI: ${e.message}")
            }
        }
    }
    
    
    fun confirmProposal() {
        val proposal = currentProposal ?: return
        
        viewModelScope.launch {
            try {
                _state.value = VoiceInputState.Processing
                
                val result = aiRepository.executeProposal(proposal)
                
                result.onSuccess {
                    
                    reset()
                }.onFailure { e ->
                    _state.value = VoiceInputState.Error("Không thể thực hiện: ${e.message}")
                }
                
            } catch (e: Exception) {
                _state.value = VoiceInputState.Error("Lỗi thực thi: ${e.message}")
            }
        }
    }
    
    
    fun createTask(task: ParsedTask) {
        
    }
    
    
    fun getFinalTask(): ParsedTask? = null 
    
    
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

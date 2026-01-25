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
    private val taskParser = TaskParserService(aiRepository)
    
    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    val state: StateFlow<VoiceInputState> = _state.asStateFlow()
    
    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()
    
    private var currentParsedTask: ParsedTask? = null
    
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
     * Process voice input text using AI
     */
    private fun processVoiceInput(text: String) {
        viewModelScope.launch {
            _state.value = VoiceInputState.Processing
            
            // Use AI to parse the voice text
            val parsed = taskParser.parse(text)
            currentParsedTask = parsed
            
            _state.value = VoiceInputState.Parsed(parsed)
        }
    }
    

    /**
     * Create task in database
     */
    fun createTask(task: ParsedTask) {
        if (!task.isValid) return
        
        viewModelScope.launch {
            try {
                _state.value = VoiceInputState.Processing
                
                // Build schedule data
                val dateStr = task.date?.format(DateTimeFormatter.ISO_LOCAL_DATE) 
                    ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val timeStr = task.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "09:00"
                val startTimeDate = "${dateStr}T${timeStr}:00+07:00"
                
                val durationMinutes = task.duration?.toMinutes()?.toInt() ?: 60
                val hours = durationMinutes / 60
                val minutes = durationMinutes % 60
                val implementationTime = String.format("%02d:%02d:00", hours, minutes)
                
                val row = mapOf(
                    "user_id" to userId,
                    "name_schedule" to (task.taskName ?: "Công việc mới"),
                    "start_time_date" to startTimeDate,
                    "implementation_time" to implementationTime,
                    "repeat" to "once"
                )
                
                scheduleRepository.insertSchedule(row)
                
                // Success - reset state
                reset()
                
            } catch (e: Exception) {
                _state.value = VoiceInputState.Error("Không thể tạo task: ${e.message}")
            }
        }
    }
    
    /**
     * Get final task for creation
     */
    fun getFinalTask(): ParsedTask? = currentParsedTask
    
    /**
     * Reset state
     */
    fun reset() {
        _state.value = VoiceInputState.Idle
        _partialText.value = ""
        currentParsedTask = null
    }
    
    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
    }
}

package com.projectapp.tempus.ui.setting

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.personalization.CustomTimePeriod
import com.projectapp.tempus.data.personalization.LifestylePreset
import com.projectapp.tempus.data.personalization.PersonalizationSettings
import com.projectapp.tempus.data.personalization.SharedPrefsPersonalizationRepository
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.SourceType
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * UI State for Personalization screen
 */
data class PersonalizationUiState(
    val lifestyle: LifestylePreset = LifestylePreset.CUSTOM,
    val wakeUpTime: String = "07:00",
    val sleepTime: String = "23:00",
    val workStartTime: String = "08:00",
    val workEndTime: String = "17:00",
    val customTimePeriods: List<CustomTimePeriod> = emptyList(),
    val activeDays: List<Int> = listOf(1, 2, 3, 4, 5, 6), // Mon-Sat by default
    val showResetConfirmation: Boolean = false,
    val showAddCustomPeriodScreen: Boolean = false,
    val showLifestyleSheet: Boolean = false,
    val showLabelSheet: Boolean = false,
    val isLoading: Boolean = false,
    val showTimePickerFor: TimePickerTarget? = null,
    // For add custom period screen
    val newPeriodName: String = "",
    val newPeriodDescription: String = "",
    val newPeriodStartTime: String = "08:00",
    val newPeriodEndTime: String = "09:00",
    val newPeriodColor: String = "#007AFF",
    val newPeriodLabel: ScheduleLabel = ScheduleLabel.book
)

/**
 * Enum to identify which time picker is being shown
 */
enum class TimePickerTarget {
    WAKE_UP, SLEEP, WORK_START, WORK_END, NEW_PERIOD_START, NEW_PERIOD_END
}

/**
 * ViewModel for Personalization settings screen
 */
class PersonalizationViewModel(application: Application) : AndroidViewModel(application) {

    private val personalizationRepo = SharedPrefsPersonalizationRepository(application)
    private val scheduleRepo = SupabaseScheduleRepository()

    private val _uiState = MutableStateFlow(PersonalizationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val settings = personalizationRepo.getSettings()
        val lifestyle = try {
            LifestylePreset.valueOf(settings.lifestyle)
        } catch (e: Exception) {
            LifestylePreset.CUSTOM
        }
        
        _uiState.value = _uiState.value.copy(
            lifestyle = lifestyle,
            wakeUpTime = settings.wakeUpTime,
            sleepTime = settings.sleepTime,
            workStartTime = settings.workStartTime,
            workEndTime = settings.workEndTime,
            customTimePeriods = settings.customTimePeriods,
            activeDays = settings.activeDays
        )
    }

    private fun saveSettings() {
        val currentState = _uiState.value
        val settings = PersonalizationSettings(
            lifestyle = currentState.lifestyle.name,
            wakeUpTime = currentState.wakeUpTime,
            sleepTime = currentState.sleepTime,
            workStartTime = currentState.workStartTime,
            workEndTime = currentState.workEndTime,
            customTimePeriods = currentState.customTimePeriods,
            activeDays = currentState.activeDays
        )
        personalizationRepo.saveSettings(settings)
    }

    // Lifestyle preset selection
    fun showLifestyleSheet() {
        _uiState.value = _uiState.value.copy(showLifestyleSheet = true)
    }

    fun dismissLifestyleSheet() {
        _uiState.value = _uiState.value.copy(showLifestyleSheet = false)
    }

    fun selectLifestyle(preset: LifestylePreset) {
        _uiState.value = _uiState.value.copy(
            lifestyle = preset,
            wakeUpTime = preset.wakeUpTime,
            sleepTime = preset.sleepTime,
            workStartTime = preset.workStartTime,
            workEndTime = preset.workEndTime,
            showLifestyleSheet = false
        )
        saveSettings()
    }

    // Day of week selection
    fun toggleDay(dayOfWeek: Int) {
        val currentDays = _uiState.value.activeDays.toMutableList()
        if (currentDays.contains(dayOfWeek)) {
            if (currentDays.size > 1) { // Keep at least one day
                currentDays.remove(dayOfWeek)
            }
        } else {
            currentDays.add(dayOfWeek)
        }
        currentDays.sort()
        _uiState.value = _uiState.value.copy(activeDays = currentDays)
        saveSettings()
    }

    // Time change handlers
    fun onWakeUpTimeChange(time: String) {
        _uiState.value = _uiState.value.copy(
            wakeUpTime = time,
            lifestyle = LifestylePreset.CUSTOM // Switch to custom when manually changing
        )
        saveSettings()
    }

    fun onSleepTimeChange(time: String) {
        _uiState.value = _uiState.value.copy(
            sleepTime = time,
            lifestyle = LifestylePreset.CUSTOM
        )
        saveSettings()
    }

    fun onWorkStartTimeChange(time: String) {
        _uiState.value = _uiState.value.copy(
            workStartTime = time,
            lifestyle = LifestylePreset.CUSTOM
        )
        saveSettings()
    }

    fun onWorkEndTimeChange(time: String) {
        _uiState.value = _uiState.value.copy(
            workEndTime = time,
            lifestyle = LifestylePreset.CUSTOM
        )
        saveSettings()
    }

    // Time picker dialog handlers
    fun showTimePicker(target: TimePickerTarget) {
        _uiState.value = _uiState.value.copy(showTimePickerFor = target)
    }

    fun dismissTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePickerFor = null)
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        val timeString = String.format("%02d:%02d", hour, minute)
        when (_uiState.value.showTimePickerFor) {
            TimePickerTarget.WAKE_UP -> onWakeUpTimeChange(timeString)
            TimePickerTarget.SLEEP -> onSleepTimeChange(timeString)
            TimePickerTarget.WORK_START -> onWorkStartTimeChange(timeString)
            TimePickerTarget.WORK_END -> onWorkEndTimeChange(timeString)
            TimePickerTarget.NEW_PERIOD_START -> {
                _uiState.value = _uiState.value.copy(newPeriodStartTime = timeString)
            }
            TimePickerTarget.NEW_PERIOD_END -> {
                _uiState.value = _uiState.value.copy(newPeriodEndTime = timeString)
            }
            null -> {}
        }
        dismissTimePicker()
    }

    // Label sheet handlers
    fun showLabelSheet() {
        _uiState.value = _uiState.value.copy(showLabelSheet = true)
    }

    fun dismissLabelSheet() {
        _uiState.value = _uiState.value.copy(showLabelSheet = false)
    }

    fun selectLabel(label: ScheduleLabel) {
        _uiState.value = _uiState.value.copy(
            newPeriodLabel = label,
            showLabelSheet = false
        )
    }

    // Add custom period screen handlers
    fun showAddCustomPeriodScreen() {
        _uiState.value = _uiState.value.copy(
            showAddCustomPeriodScreen = true,
            newPeriodName = "",
            newPeriodDescription = "",
            newPeriodStartTime = "08:00",
            newPeriodEndTime = "09:00",
            newPeriodColor = "#007AFF",
            newPeriodLabel = ScheduleLabel.book
        )
    }

    fun dismissAddCustomPeriodScreen() {
        _uiState.value = _uiState.value.copy(showAddCustomPeriodScreen = false)
    }

    fun updateNewPeriodName(name: String) {
        _uiState.value = _uiState.value.copy(newPeriodName = name)
    }

    fun updateNewPeriodDescription(description: String) {
        _uiState.value = _uiState.value.copy(newPeriodDescription = description)
    }

    fun updateNewPeriodColor(color: String) {
        _uiState.value = _uiState.value.copy(newPeriodColor = color)
    }

    fun saveNewCustomPeriod() {
        val state = _uiState.value
        if (state.newPeriodName.isBlank()) return

        val newPeriod = CustomTimePeriod(
            id = UUID.randomUUID().toString(),
            name = state.newPeriodName,
            startTime = state.newPeriodStartTime,
            endTime = state.newPeriodEndTime,
            color = state.newPeriodColor,
            description = state.newPeriodDescription,
            label = state.newPeriodLabel.name
        )
        val updatedList = state.customTimePeriods + newPeriod
        _uiState.value = state.copy(
            customTimePeriods = updatedList,
            showAddCustomPeriodScreen = false
        )
        saveSettings()
    }

    fun removeCustomTimePeriod(id: String) {
        val updatedList = _uiState.value.customTimePeriods.filter { it.id != id }
        _uiState.value = _uiState.value.copy(customTimePeriods = updatedList)
        saveSettings()
    }

    // Reset confirmation handlers
    fun showResetConfirmation() {
        _uiState.value = _uiState.value.copy(showResetConfirmation = true)
    }

    fun dismissResetConfirmation() {
        _uiState.value = _uiState.value.copy(showResetConfirmation = false)
    }

    /**
     * Confirms reset:
     * 1. Deletes all schedules from today onwards
     * 2. Creates new daily repeating tasks for wake up, sleep, work, and custom periods
     */
    fun confirmReset() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, showResetConfirmation = false)
                
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
                if (userId == null) {
                    Log.e("PersonalizationVM", "User not logged in")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                val state = _uiState.value
                val today = LocalDate.now()
                
                // Step 1: Delete all schedules from today onwards
                val todayStart = today.atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                
                val deletedCount = scheduleRepo.deleteSchedulesFromDate(userId, todayStart)
                Log.d("PersonalizationVM", "Deleted $deletedCount schedules from today onwards")

                // Step 2: Create Wake Up task
                createDailyTask(
                    userId = userId,
                    name = "Thức dậy",
                    timeStr = state.wakeUpTime,
                    date = today,
                    label = ScheduleLabel.wakeup,
                    color = "#FF9500", // Orange
                    duration = "00:30:00"
                )

                // Step 3: Create Sleep task
                createDailyTask(
                    userId = userId,
                    name = "Đi ngủ",
                    timeStr = state.sleepTime,
                    date = today,
                    label = ScheduleLabel.sleep,
                    color = "#5856D6", // Purple
                    duration = "00:30:00"
                )

                // Step 4: Create Work Start task
                createDailyTask(
                    userId = userId,
                    name = "Làm việc",
                    timeStr = state.workStartTime,
                    date = today,
                    label = ScheduleLabel.book,
                    color = "#34C759", // Green
                    duration = calculateDuration(state.workStartTime, state.workEndTime)
                )

                // Step 5: Create custom time period tasks
                state.customTimePeriods.forEach { period ->
                    val periodLabel = try {
                        ScheduleLabel.valueOf(period.label)
                    } catch (e: Exception) {
                        ScheduleLabel.book
                    }
                    
                    createDailyTask(
                        userId = userId,
                        name = period.name,
                        timeStr = period.startTime,
                        date = today,
                        label = periodLabel,
                        color = period.color,
                        duration = calculateDuration(period.startTime, period.endTime)
                    )
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
                Log.d("PersonalizationVM", "Successfully created daily routine tasks")

            } catch (e: Exception) {
                Log.e("PersonalizationVM", "Error creating daily tasks", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Creates a daily repeating task on the timeline
     */
    private suspend fun createDailyTask(
        userId: String,
        name: String,
        timeStr: String,
        date: LocalDate,
        label: ScheduleLabel,
        color: String,
        duration: String
    ) {
        try {
            val timeParts = timeStr.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            val time = LocalTime.of(hour, minute)

            val localDT = LocalDateTime.of(date, time)
            val isoDate = localDT.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            val mapData = mapOf<String, Any?>(
                "user_id" to userId,
                "name_schedule" to name,
                "start_time_date" to isoDate,
                "color" to color,
                "label" to label.name,
                "source" to SourceType.manual.name,
                "implementation_time" to duration,
                "repeat" to RepeatType.daily.name,
                "priority" to "medium"
            )

            scheduleRepo.insertSchedule(mapData)
            Log.d("PersonalizationVM", "Created task: $name at $timeStr")

        } catch (e: Exception) {
            Log.e("PersonalizationVM", "Error creating task $name", e)
            throw e
        }
    }

    /**
     * Calculates duration between two time strings (HH:mm format)
     * Returns duration in HH:MM:SS format
     */
    private fun calculateDuration(startTime: String, endTime: String): String {
        return try {
            val startParts = startTime.split(":")
            val endParts = endTime.split(":")
            
            val startMinutes = (startParts[0].toInt() * 60) + startParts[1].toInt()
            var endMinutes = (endParts[0].toInt() * 60) + endParts[1].toInt()
            
            // Handle overnight duration (e.g., 22:00 to 06:00)
            if (endMinutes < startMinutes) {
                endMinutes += 24 * 60
            }
            
            val durationMinutes = endMinutes - startMinutes
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            
            String.format("%02d:%02d:00", hours, minutes)
        } catch (e: Exception) {
            "01:00:00" // Default 1 hour
        }
    }

    /**
     * Get active days as formatted string
     */
    fun getActiveDaysLabel(): String {
        val state = _uiState.value
        return when {
            state.activeDays == listOf(1, 2, 3, 4, 5, 6, 7) -> "Mỗi ngày"
            state.activeDays == listOf(1, 2, 3, 4, 5) -> "Thứ 2 - Thứ 6"
            state.activeDays == listOf(1, 2, 3, 4, 5, 6) -> "Thứ 2 - Thứ 7"
            state.activeDays == listOf(6, 7) || state.activeDays == listOf(7, 6) -> "Cuối tuần"
            else -> {
                state.activeDays.sorted().joinToString(", ") { day ->
                    when (day) {
                        1 -> "T2"
                        2 -> "T3"
                        3 -> "T4"
                        4 -> "T5"
                        5 -> "T6"
                        6 -> "T7"
                        7 -> "CN"
                        else -> ""
                    }
                }
            }
        }
    }
    
    /**
     * Get label display name
     */
    fun getLabelDisplayName(label: ScheduleLabel): String {
        return when (label) {
            ScheduleLabel.wakeup -> "Thức dậy"
            ScheduleLabel.eat -> "Ăn uống"
            ScheduleLabel.exercise -> "Tập luyện"
            ScheduleLabel.rest -> "Nghỉ ngơi"
            ScheduleLabel.water -> "Uống nước"
            ScheduleLabel.book -> "Học tập/Làm việc"
            ScheduleLabel.sleep -> "Ngủ"
            ScheduleLabel.clean -> "Dọn dẹp"
            ScheduleLabel.cook -> "Nấu ăn"
            ScheduleLabel.garden -> "Làm vườn"
            ScheduleLabel.UNKNOWN -> "Khác"
        }
    }
}

package com.projectapp.tempus.ui.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.SourceType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel

data class EditState(
    var applyTodayOnly: Boolean = false,
    val isEditMode: Boolean = false,
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate = LocalDate.now(), // Ngày user đang xem trên timeline (dùng cho delete)
    val time: LocalTime = LocalTime.now(),
    val color: String = "#FFA726",
    val iconLabel: ScheduleLabel = ScheduleLabel.book,
    val repeat: RepeatType = RepeatType.daily,
    val repeatDays: List<Int> = listOf(1, 2, 3, 4, 5), // Các thứ lặp lại (1=Mon, 7=Sun), mặc định Thứ 2-6
    val duration: String = "00:30:00",
    val priority: PriorityType = PriorityType.medium,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val subtasks: List<String> = emptyList() // Subtask titles
)

class EditScheduleViewModel(
    private val repo: ScheduleRepository,
    private val userId: String
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state = _state.asStateFlow()

    private val _saveSuccessEvent = Channel<Unit>()
    val saveSuccessEvent = _saveSuccessEvent.receiveAsFlow()

    private val _errorEvent = Channel<String>()
    val errorEvent = _errorEvent.receiveAsFlow()


    fun initialize(taskId: String?, initialDateString: String? = null) {
        // Parse ngày đang xem từ timeline
        val viewingDate = if (initialDateString != null) {
            try { LocalDate.parse(initialDateString) } catch (e: Exception) { LocalDate.now() }
        } else {
            LocalDate.now()
        }
        
        if (taskId == null) {
            _state.value = EditState(isEditMode = false, date = viewingDate, selectedDate = viewingDate)
        } else {
            viewModelScope.launch {
                try {
                    val task = repo.getScheduleById(taskId)
                    task?.let { t ->
                        val odt = java.time.OffsetDateTime.parse(
                            t.startTimeDate,
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        )
                        val localZdt = odt.atZoneSameInstant(ZoneId.systemDefault())

                        // Load existing subtasks
                        val existingSubtasks = repo.getSubTasks(taskId)
                        val subtaskTitles = existingSubtasks.map { it.title }
                        
                        _state.value = EditState(
                            isEditMode = true,
                            id = t.id,
                            title = t.name,
                            date = localZdt.toLocalDate(),
                            selectedDate = viewingDate, // Ngày user đang xem (cho delete)
                            time = localZdt.toLocalTime(),
                            color = t.color ?: "#FFA726",
                            iconLabel = t.label ?: ScheduleLabel.book,
                            repeat = t.repeat,
                            repeatDays = t.repeatDays?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: listOf(1, 2, 3, 4, 5),
                            duration = t.implementationTime ?: "00:30:00",
                            priority = t.priority ?: PriorityType.medium,
                            subtasks = subtaskTitles
                        )
                    }
                } catch (e: Exception) {
                    Log.e("EditViewModel", "Error initializing task", e)
                }
            }
        }
    }

    fun saveTask(title: String, desc: String, subtaskTitles: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                val s = _state.value

                val localDT = LocalDateTime.of(s.date, s.time)
                val isoDate = localDT.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                val mapData = mutableMapOf<String, Any?>(
                    "user_id" to userId,
                    "name_schedule" to title,
                    "start_time_date" to isoDate,
                    "color" to s.color,
                    "label" to s.iconLabel.name,
                    "source" to SourceType.manual.name,
                    "implementation_time" to s.duration,
                    "repeat" to s.repeat.name,
                    "repeat_days" to if (s.repeat == RepeatType.custom) s.repeatDays.joinToString(",") else null,
                    "priority" to s.priority.name
                )

                if (!s.isEditMode || s.id == null) {
                    // Create new schedule
                    val newSchedule = repo.insertSchedule(mapData)
                    // Insert subtasks for the new schedule
                    if (subtaskTitles.isNotEmpty()) {
                        repo.insertSubTasks(newSchedule.id, subtaskTitles)
                    }
                    _saveSuccessEvent.send(Unit)
                    return@launch
                }

                val taskId = s.id

                if (!s.applyTodayOnly) {
                    repo.updateSchedule(taskId, mapData)
                    // Update subtasks: delete old ones, insert new ones
                    repo.deleteSubTasksByScheduleId(taskId)
                    if (subtaskTitles.isNotEmpty()) {
                        repo.insertSubTasks(taskId, subtaskTitles)
                    }
                } else {
                    val editedFields = mapOf(
                        "start_time_date" to isoDate,
                        "color" to s.color,
                        "implementation_time" to s.duration
                    )
                    val ev = repo.insertEditedVersion(editedFields)
                    repo.attachEditedVersionToDate(taskId, s.date.toString(), ev.id)
                }

                _saveSuccessEvent.send(Unit)
            } catch (e: Exception) {
                Log.e("EditViewModel", "Error saving task: ${e.message}", e)
                _errorEvent.send("Lỗi lưu dữ liệu: ${e.message}")
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            try {
                _state.value.id?.let {
                    repo.deleteSchedule(it)
                    _saveSuccessEvent.send(Unit)
                }
            } catch (e: Exception) {
                Log.e("EditViewModel", "Error deleting task", e)
            }
        }
    }

    /**
     * Xóa chỉ cho ngày đang xem - đặt status = delete trong schedule_items
     * Tác vụ vẫn sẽ hiện ở các ngày khác
     */
    fun deleteForToday() {
        viewModelScope.launch {
            try {
                val taskId = _state.value.id ?: return@launch
                val dateStr = _state.value.selectedDate.toString() // Dùng selectedDate - ngày user đang xem
                repo.upsertScheduleItem(taskId, dateStr, com.projectapp.tempus.data.schedule.dto.StatusType.delete)
                Log.d("EditViewModel", "Deleted task for date: $taskId date=$dateStr")
                _saveSuccessEvent.send(Unit)
            } catch (e: Exception) {
                Log.e("EditViewModel", "Error deleting task for today", e)
                _errorEvent.send("Lỗi xóa tác vụ: ${e.message}")
            }
        }
    }

    /**
     * Xóa từ ngày đang xem trở đi - đặt end_date cho schedule
     * Tác vụ vẫn sẽ hiện ở các ngày trước đó
     */
    fun deleteFromToday() {
        viewModelScope.launch {
            try {
                val taskId = _state.value.id ?: return@launch
                val endDateStr = _state.value.selectedDate.toString() // Dùng selectedDate - ngày user đang xem
                repo.updateSchedule(taskId, mapOf("end_date" to endDateStr))
                Log.d("EditViewModel", "Set end_date for task: $taskId endDate=$endDateStr")
                _saveSuccessEvent.send(Unit)
            } catch (e: Exception) {
                Log.e("EditViewModel", "Error setting end date", e)
                _errorEvent.send("Lỗi xóa tác vụ: ${e.message}")
            }
        }
    }

    /**
     * Kiểm tra xem tác vụ có phải là tác vụ lặp lại không
     */
    fun isRecurringTask(): Boolean {
        return _state.value.repeat != RepeatType.once
    }

    fun setApplyTodayOnly(v: Boolean) {
        _state.value = _state.value.copy(applyTodayOnly = v)
    }

    fun setRepeat(r: RepeatType) {
        _state.value = _state.value.copy(repeat = r)
    }

    fun setIcon(label: ScheduleLabel) {
        _state.value = _state.value.copy(iconLabel = label)
    }

    fun setDuration(d: String) { _state.value = _state.value.copy(duration = d) }
    fun setDate(d: LocalDate) { _state.value = _state.value.copy(date = d) }
    fun setTime(t: LocalTime) { _state.value = _state.value.copy(time = t) }
    fun setColor(c: String) { _state.value = _state.value.copy(color = c) }
    fun setPriority(p: PriorityType) { _state.value = _state.value.copy(priority = p) }
    fun setSubtasks(list: List<String>) { _state.value = _state.value.copy(subtasks = list) }
    
    fun setRepeatDays(days: List<Int>) { 
        _state.value = _state.value.copy(repeatDays = days.sorted()) 
    }
    
    fun toggleRepeatDay(day: Int) {
        val currentDays = _state.value.repeatDays.toMutableList()
        if (currentDays.contains(day)) {
            if (currentDays.size > 1) { // Giữ ít nhất 1 ngày
                currentDays.remove(day)
            }
        } else {
            currentDays.add(day)
        }
        _state.value = _state.value.copy(repeatDays = currentDays.sorted())
    }
    
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

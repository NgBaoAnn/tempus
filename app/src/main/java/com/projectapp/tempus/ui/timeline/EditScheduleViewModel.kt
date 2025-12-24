package com.projectapp.tempus.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.schedule.ScheduleRepository
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

data class EditState(
    var applyTodayOnly: Boolean = false,
    val isEditMode: Boolean = false,
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val color: String = "#FFA726", // Cam mặc định
    val iconLabel: String = "book",
    val repeat: RepeatType = RepeatType.daily,   // ✅ THÊM

    val loading: Boolean = false
)

class EditScheduleViewModel(
    private val repo: ScheduleRepository,
    private val userId: String
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state = _state.asStateFlow()

    private val _saveSuccessEvent = Channel<Unit>()
    val saveSuccessEvent = _saveSuccessEvent.receiveAsFlow()

    fun initialize(taskId: String?) {
        if (taskId == null) {
            _state.value = EditState(isEditMode = false)
        } else {
            viewModelScope.launch {
                val task = repo.getScheduleById(taskId)
                task?.let { t ->
                    try {
                        val odt = java.time.OffsetDateTime.parse(
                            t.startTimeDate,
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        )
                        val localZdt = odt.atZoneSameInstant(ZoneId.systemDefault())

                        _state.value = EditState(
                            isEditMode = true,
                            id = t.id,
                            title = t.name,
                            date = localZdt.toLocalDate(),
                            time = localZdt.toLocalTime(),
                            color = t.color ?: "#FFA726",
                            iconLabel = t.label ?: "book",
                            repeat = t.repeat // ✅ LOAD repeat từ DB
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun saveTask(title: String, desc: String) {
        viewModelScope.launch {
            try {
                val s = _state.value

                val localDT = LocalDateTime.of(s.date, s.time)
                val isoDate = localDT.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                val mapData = mapOf(
                    "user_id" to userId,
                    "name_schedule" to title,
                    "start_time_date" to isoDate,
                    "color" to s.color,
                    "source" to SourceType.manual.name,
                    "implementation_time" to "00:30:00",
                    "repeat" to s.repeat.name // ✅ lấy từ state
                )

                if (!s.isEditMode || s.id == null) {
                    repo.insertSchedule(mapData)
                    _saveSuccessEvent.send(Unit)
                    return@launch
                }

                val taskId = s.id

                if (!s.applyTodayOnly) {
                    repo.updateSchedule(taskId, mapData)
                } else {
                    // NOTE: edited_version của bạn hiện không có cột repeat -> không đưa repeat vào editedFields
                    val editedFields = mapOf(
                        "start_time_date" to isoDate,
                        "color" to s.color,
                        "implementation_time" to "00:30:00"
                    )
                    val ev = repo.insertEditedVersion(editedFields)
                    repo.attachEditedVersionToDate(taskId, s.date.toString(), ev.id)
                }

                _saveSuccessEvent.send(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
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
                e.printStackTrace()
            }
        }
    }

    fun setApplyTodayOnly(v: Boolean) {
        _state.value = _state.value.copy(applyTodayOnly = v)
    }

    fun setRepeat(r: RepeatType) { // ✅ THÊM
        _state.value = _state.value.copy(repeat = r)
    }

    fun setDate(d: LocalDate) { _state.value = _state.value.copy(date = d) }
    fun setTime(t: LocalTime) { _state.value = _state.value.copy(time = t) }
    fun setColor(c: String) { _state.value = _state.value.copy(color = c) }
}

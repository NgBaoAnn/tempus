package com.projectapp.tempus.ui.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.domain.model.TimelineBlock
import com.projectapp.tempus.domain.usecase.BuildTimelineUseCase
import java.time.LocalDate
import java.time.OffsetDateTime

data class TimelineUiState(
    val date: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val blocks: List<TimelineBlock> = emptyList(),
    val error: String? = null
)

class TimelineViewModel(
    private val userId: String,
    private val repo: ScheduleRepository,
    private val builder: BuildTimelineUseCase = BuildTimelineUseCase()
) : ViewModel() {

    private val _ui = MutableStateFlow(TimelineUiState())
    val ui: StateFlow<TimelineUiState> = _ui

    fun onSelectDate(date: LocalDate) {
        Log.d("Timeline", "onSelectDate: $date")
        _ui.value = _ui.value.copy(date = date, error = null)
        load(date)
    }

    fun onRefresh() {
        Log.d("Timeline", "onRefresh date=${_ui.value.date}")
        load(_ui.value.date)
    }

    fun onClickAddDummyTask() {
        Log.d("Timeline", "onClickAddDummyTask")
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isLoading = true, error = null)

                val now = OffsetDateTime.now()
                    .withHour(8).withMinute(0).withSecond(0).withNano(0)

                val body = mapOf(
                    "user_id" to userId,
                    "name_schedule" to "Dummy Task",
                    "icon_id" to 1,
                    "start_time_date" to now.toString(),
                    "implementation_time" to "01:00:00",
                    "repeat" to "once",
                    "color" to "#7C4DFF",
                    "source" to "manual"
                )

                val inserted = repo.insertSchedule(body)
                Log.d("Timeline", "insertSchedule success id=${inserted.id}")

                _ui.value = _ui.value.copy(isLoading = false)
                load(_ui.value.date)

            } catch (e: Exception) {
                Log.e("Timeline", "insertSchedule FAILED: ${e.message}", e)
                _ui.value = _ui.value.copy(isLoading = false, error = "Insert failed: ${e.message}")
            }
        }
    }

    fun onToggleStatus(taskId: String, status: StatusType) {
        val dateStr = _ui.value.date.toString()
        Log.d("Timeline", "onToggleStatus taskId=$taskId date=$dateStr status=$status")
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isLoading = true, error = null)
                val item = repo.upsertScheduleItem(taskId, dateStr, status)
                Log.d("Timeline", "upsertScheduleItem ok itemId=${item.id} status=${item.status}")
                _ui.value = _ui.value.copy(isLoading = false)
                load(_ui.value.date)
            } catch (e: Exception) {
                Log.e("Timeline", "upsertScheduleItem FAILED: ${e.message}", e)
                _ui.value = _ui.value.copy(isLoading = false, error = "Upsert failed: ${e.message}")
            }
        }
    }

    fun onClickBlock(taskId: String) {
        Log.d("Timeline", "onClickBlock taskId=$taskId")
    }

    fun onEditName(taskId: String, newName: String) {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isLoading = true, error = null)
                repo.updateSchedule(taskId, mapOf("name_schedule" to newName))
                Log.d("Timeline", "edit name ok taskId=$taskId")
                _ui.value = _ui.value.copy(isLoading = false)
                load(_ui.value.date)
            } catch (e: Exception) {
                Log.e("Timeline", "edit name FAILED: ${e.message}", e)
                _ui.value = _ui.value.copy(isLoading = false, error = "Edit name failed: ${e.message}")
            }
        }
    }

    fun onEditTime(taskId: String, newStart: String, newDuration: String) {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isLoading = true, error = null)
                repo.updateSchedule(
                    taskId,
                    mapOf(
                        "start_time_date" to newStart,          // dạng "YYYY-MM-DDTHH:mm:ss"
                        "implementation_time" to newDuration    // dạng "HH:mm:ss"
                    )
                )
                Log.d("Timeline", "edit time ok taskId=$taskId start=$newStart dur=$newDuration")
                _ui.value = _ui.value.copy(isLoading = false)
                load(_ui.value.date)
            } catch (e: Exception) {
                Log.e("Timeline", "edit time FAILED: ${e.message}", e)
                _ui.value = _ui.value.copy(isLoading = false, error = "Edit time failed: ${e.message}")
            }
        }
    }



    private fun load(date: LocalDate) {
        val dateStr = date.toString()
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isLoading = true, error = null)
                Log.d("Timeline", "load start date=$dateStr userId=$userId")

                val dateStr = date.toString()

                val schedules = repo.getAllSchedules(userId)
                val taskIds = schedules.map { it.id }

                val scheduleItems = repo.getScheduleItemsByDate(dateStr, taskIds)

                val editedIds = scheduleItems.mapNotNull { it.editedVersion }.distinct()
                val editedMap = repo.getEditedVersions(editedIds).associateBy { it.id }

                val blocks = builder.build(date, schedules, scheduleItems, editedMap)

                Log.d("Timeline", "build blocks=${blocks.size}")

                _ui.value = _ui.value.copy(
                    isLoading = false,
                    blocks = blocks,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("Timeline", "load FAILED: ${e.message}", e)
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    blocks = emptyList(),
                    error = "Load failed: ${e.message}"
                )
            }
        }
    }
}

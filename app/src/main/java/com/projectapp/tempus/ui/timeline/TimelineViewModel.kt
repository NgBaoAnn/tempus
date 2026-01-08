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
import java.time.YearMonth
import java.time.DayOfWeek
import java.time.ZoneId

data class TimelineUiState(
    val date: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val blocks: List<TimelineBlock> = emptyList(),
    val error: String? = null
)

class TimelineViewModel(
    private var cachedSchedules: List<com.projectapp.tempus.data.schedule.dto.ScheduleRow> = emptyList(),
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

    fun setCurrentWeekForHeader(anyDayInWeek: LocalDate) {
        _ui.value = _ui.value.copy(date = anyDayInWeek)
    }

    suspend fun getMonthIcons(ym: YearMonth): Map<java.time.LocalDate, List<String>> {
        if (cachedSchedules.isEmpty()) {
            cachedSchedules = repo.getAllSchedules(userId)
        }

        val days = (1..ym.lengthOfMonth()).map { ym.atDay(it) }
        val dateStrs = days.map { it.toString() }
        val taskIds = cachedSchedules.map { it.id }

        // 1) load schedule_items của cả tháng cho các task
        val items = repo.getScheduleItemsByDates(dateStrs, taskIds)

        // 2) map (date|taskId) -> item
        val itemByKey = items.associateBy { it.date + "|" + it.taskId }

        // 3) collect edited_version ids -> fetch edited_versions -> map id -> row
        val editedIds = items.mapNotNull { it.editedVersion }.distinct()
        val editedMap = if (editedIds.isNotEmpty()) {
            repo.getEditedVersions(editedIds).associateBy { it.id }   // ✅ dùng hàm sẵn có của bạn
        } else emptyMap()

        fun occursOnDate(s: com.projectapp.tempus.data.schedule.dto.ScheduleRow, d: java.time.LocalDate): Boolean {
            val startZdt = try {
                OffsetDateTime.parse(s.startTimeDate.replace(" ", "T"))
                    .atZoneSameInstant(ZoneId.systemDefault())
            } catch (_: Exception) {
                // fallback: nếu format lạ
                java.time.LocalDate.parse(s.startTimeDate.split(" ")[0]).atStartOfDay(ZoneId.systemDefault())
            }
            val startDate = startZdt.toLocalDate()

            return when (s.repeat) {
                com.projectapp.tempus.data.schedule.dto.RepeatType.once -> d == startDate
                com.projectapp.tempus.data.schedule.dto.RepeatType.daily -> !d.isBefore(startDate)
                com.projectapp.tempus.data.schedule.dto.RepeatType.weekly ->
                    !d.isBefore(startDate) && d.dayOfWeek == startDate.dayOfWeek
                com.projectapp.tempus.data.schedule.dto.RepeatType.monthly ->
                    !d.isBefore(startDate) && d.dayOfMonth == startDate.dayOfMonth
            }
        }

        val res = HashMap<java.time.LocalDate, MutableList<String>>()

        for (d in days) {
            val list = ArrayList<String>()

            for (s in cachedSchedules) {
                if (!occursOnDate(s, d)) continue

                val key = d.toString() + "|" + s.id
                val item = itemByKey[key]

                // delete => ẩn khỏi lịch
                if (item?.status == StatusType.delete) continue

                // ✅ icon label: edited_version (nếu có) > schedule gốc > book
                val evLabel = item?.editedVersion?.let { editedMap[it]?.label }
                val labelStr = (evLabel ?: s.label) ?: "book"

                list.add(labelStr)
            }

            res[d] = list
        }

        return res
    }
}

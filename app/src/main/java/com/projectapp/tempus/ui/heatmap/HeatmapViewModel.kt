package com.projectapp.tempus.ui.heatmap

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.domain.model.TimelineBlock
import com.projectapp.tempus.domain.usecase.BuildTimelineUseCase
import com.projectapp.tempus.domain.usecase.GetHeatmapUseCase
import com.projectapp.tempus.domain.usecase.MonthHeatmapData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * UI State for Heatmap Calendar Screen.
 */
data class HeatmapUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val monthData: MonthHeatmapData? = null,
    val selectedDate: LocalDate? = null,
    val selectedDayTasks: List<TimelineBlock> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingDayDetail: Boolean = false,
    val showDayDetailSheet: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for Heatmap Calendar functionality.
 * Manages month data loading, navigation, and day selection.
 */
class HeatmapViewModel(
    private val userId: String,
    private val repository: ScheduleRepository,
    private val heatmapUseCase: GetHeatmapUseCase = GetHeatmapUseCase(),
    private val buildTimelineUseCase: BuildTimelineUseCase = BuildTimelineUseCase()
) : ViewModel() {

    private val _state = MutableStateFlow(HeatmapUiState())
    val state: StateFlow<HeatmapUiState> = _state

    // Cache for loaded months to avoid re-fetching
    private val monthCache = mutableMapOf<YearMonth, MonthHeatmapData>()
    private var cachedSchedules: List<com.projectapp.tempus.data.schedule.dto.ScheduleRow> = emptyList()

    init {
        loadMonth(YearMonth.now())
    }

    /**
     * Load heatmap data for a specific month.
     */
    fun loadMonth(yearMonth: YearMonth) {
        // Check cache first
        monthCache[yearMonth]?.let { cached ->
            _state.update { it.copy(
                currentMonth = yearMonth,
                monthData = cached,
                isLoading = false,
                error = null
            )}
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    currentMonth = yearMonth,
                    isLoading = true,
                    error = null
                )}

                Log.d("HeatmapVM", "Loading month: $yearMonth for user: $userId")

                // Fetch schedules (cache for performance)
                if (cachedSchedules.isEmpty()) {
                    cachedSchedules = repository.getAllSchedules(userId)
                    Log.d("HeatmapVM", "Fetched ${cachedSchedules.size} schedules")
                }

                val taskIds = cachedSchedules.map { it.id }
                
                // Fetch schedule items for the month range
                val startDate = yearMonth.atDay(1).toString()
                val endDate = yearMonth.atEndOfMonth().toString()
                
                val items = if (taskIds.isNotEmpty()) {
                    repository.getScheduleItemsByRange(startDate, endDate, taskIds)
                } else {
                    emptyList()
                }
                
                Log.d("HeatmapVM", "Fetched ${items.size} items for range $startDate to $endDate")

                // Calculate heatmap data
                val monthData = heatmapUseCase.execute(yearMonth, cachedSchedules, items)
                
                // Cache the result
                monthCache[yearMonth] = monthData

                _state.update { it.copy(
                    monthData = monthData,
                    isLoading = false
                )}

            } catch (e: Exception) {
                Log.e("HeatmapVM", "Failed to load month: ${e.message}", e)
                _state.update { it.copy(
                    isLoading = false,
                    error = "Không thể tải dữ liệu: ${e.message}"
                )}
            }
        }
    }

    /**
     * Navigate to previous month.
     */
    fun navigatePrevMonth() {
        val prevMonth = _state.value.currentMonth.minusMonths(1)
        loadMonth(prevMonth)
    }

    /**
     * Navigate to next month.
     */
    fun navigateNextMonth() {
        val nextMonth = _state.value.currentMonth.plusMonths(1)
        loadMonth(nextMonth)
    }

    /**
     * Select a date and load its tasks for the detail sheet.
     */
    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            _state.update { it.copy(
                selectedDate = date,
                isLoadingDayDetail = true,
                showDayDetailSheet = true
            )}

            try {
                val dateStr = date.toString()
                val taskIds = cachedSchedules.map { it.id }
                
                // Fetch items for the selected date
                val items = if (taskIds.isNotEmpty()) {
                    repository.getScheduleItemsByDate(dateStr, taskIds)
                } else {
                    emptyList()
                }

                // Get edited versions if any
                val editedIds = items.mapNotNull { it.editedVersion }.distinct()
                val editedMap = if (editedIds.isNotEmpty()) {
                    repository.getEditedVersions(editedIds).associateBy { it.id }
                } else {
                    emptyMap()
                }

                // Batch load subtasks
                val allSubtasks = if (taskIds.isNotEmpty()) {
                    repository.getSubTasksBatch(taskIds)
                } else {
                    emptyList()
                }
                val subtasksMap = allSubtasks.groupBy { it.scheduleId }

                // Build timeline blocks for the day
                val blocks = buildTimelineUseCase.build(
                    date,
                    cachedSchedules,
                    items,
                    editedMap,
                    subtasksMap
                )

                Log.d("HeatmapVM", "Loaded ${blocks.size} tasks for $date")

                _state.update { it.copy(
                    selectedDayTasks = blocks,
                    isLoadingDayDetail = false
                )}

            } catch (e: Exception) {
                Log.e("HeatmapVM", "Failed to load day detail: ${e.message}", e)
                _state.update { it.copy(
                    isLoadingDayDetail = false,
                    error = "Không thể tải chi tiết ngày"
                )}
            }
        }
    }

    /**
     * Dismiss the day detail bottom sheet.
     */
    fun dismissDayDetail() {
        _state.update { it.copy(
            showDayDetailSheet = false,
            selectedDate = null,
            selectedDayTasks = emptyList()
        )}
    }

    /**
     * Toggle task completion status from the day detail sheet.
     */
    fun toggleTaskStatus(task: TimelineBlock) {
        val newStatus = if (task.status == com.projectapp.tempus.data.schedule.dto.StatusType.done) {
            com.projectapp.tempus.data.schedule.dto.StatusType.planned
        } else {
            com.projectapp.tempus.data.schedule.dto.StatusType.done
        }

        val dateStr = _state.value.selectedDate?.toString() ?: return

        viewModelScope.launch {
            try {
                repository.upsertScheduleItem(task.taskId, dateStr, newStatus)
                Log.d("HeatmapVM", "Toggled task ${task.taskId} to $newStatus")

                // Refresh the selected date's data
                _state.value.selectedDate?.let { selectDate(it) }

                // Clear month cache to reflect changes
                monthCache.remove(_state.value.currentMonth)
                loadMonth(_state.value.currentMonth)

            } catch (e: Exception) {
                Log.e("HeatmapVM", "Failed to toggle task: ${e.message}", e)
            }
        }
    }

    /**
     * Clear cached data (e.g., when user changes).
     */
    fun clearCache() {
        monthCache.clear()
        cachedSchedules = emptyList()
    }

    /**
     * Refresh current month data.
     */
    fun refresh() {
        monthCache.remove(_state.value.currentMonth)
        loadMonth(_state.value.currentMonth)
    }
}

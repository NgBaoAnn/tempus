package com.projectapp.tempus.ui.timeline

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.projectapp.tempus.data.quote.QuoteRepository
import com.projectapp.tempus.data.quote.dto.QuoteDto
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.domain.model.TimelineBlock
import com.projectapp.tempus.domain.model.PointAction
import com.projectapp.tempus.domain.usecase.BuildTimelineUseCase
import com.projectapp.tempus.domain.usecase.PointsManager
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.DayOfWeek
import java.time.ZoneId

// Sort options for timeline
enum class SortOption {
    START_TIME,   // Default - by start time
    PRIORITY,     // By priority level (high -> medium -> low)
    CREATED_AT    // By creation date (newest first)
}

data class TimelineUiState(
    val date: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val blocks: List<TimelineBlock> = emptyList(),
    val filteredBlocks: List<TimelineBlock> = emptyList(), // After applying filters
    val error: String? = null,
    val dailyQuote: QuoteDto? = null,
    // Search/Sort/Filter state
    val searchQuery: String = "",
    val sortBy: SortOption = SortOption.START_TIME,
    val filterLabels: Set<ScheduleLabel> = emptySet(),
    val filterPriorities: Set<PriorityType> = emptySet(),
    val filterStatus: StatusType? = null, // null = show all
    val isFilterActive: Boolean = false,
    // Points earned event (set to null after consuming)
    val earnedPoints: Int? = null,
    val earnedReason: String? = null
)

class TimelineViewModel(
    application: Application,
    private var cachedSchedules: List<com.projectapp.tempus.data.schedule.dto.ScheduleRow> = emptyList(),
    private val userId: String,
    private val repo: ScheduleRepository,
    private val pointsManager: PointsManager? = null,
    private val builder: BuildTimelineUseCase = BuildTimelineUseCase()
) : AndroidViewModel(application) {

    private val quoteRepository = QuoteRepository(application)
    
    private val _ui = MutableStateFlow(TimelineUiState())
    val ui: StateFlow<TimelineUiState> = _ui
    
    init {
        loadDailyQuote()
    }
    
    private fun loadDailyQuote() {
        val quote = quoteRepository.getTodayQuote()
        _ui.value = _ui.value.copy(dailyQuote = quote)
    }

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
                
                // Handle points based on status change
                var earnedPoints: Int? = null
                var earnedReason: String? = null
                
                if (pointsManager != null) {
                    when (status) {
                        StatusType.done -> {
                            // Award points when task is completed
                            earnedPoints = pointsManager.earnPoints(PointAction.TASK_COMPLETE)
                            earnedReason = "Hoàn thành Task"
                            pointsManager.updateStreak()
                            Log.d("Timeline", "Awarded $earnedPoints points for task completion")
                        }
                        StatusType.planned -> {
                            // Deduct points when task is uncompleted (prevent abuse)
                            earnedPoints = pointsManager.earnPoints(PointAction.TASK_UNCOMPLETE)
                            earnedReason = "Huỷ hoàn thành Task"
                            Log.d("Timeline", "Deducted $earnedPoints points for task uncompletion")
                        }
                        else -> { /* No points change for delete */ }
                    }
                }
                
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    earnedPoints = earnedPoints,
                    earnedReason = earnedReason
                )
                load(_ui.value.date)
            } catch (e: Exception) {
                Log.e("Timeline", "upsertScheduleItem FAILED: ${e.message}", e)
                _ui.value = _ui.value.copy(isLoading = false, error = "Upsert failed: ${e.message}")
            }
        }
    }
    
    fun clearEarnedPoints() {
        _ui.value = _ui.value.copy(earnedPoints = null, earnedReason = null)
    }

    fun onSubtaskToggle(subtaskId: String, isDone: Boolean) {
        Log.d("Timeline", "onSubtaskToggle subtaskId=$subtaskId isDone=$isDone")
        viewModelScope.launch {
            try {
                repo.updateSubTaskStatus(subtaskId, isDone)
                Log.d("Timeline", "updateSubTaskStatus ok")
                load(_ui.value.date)
            } catch (e: Exception) {
                Log.e("Timeline", "updateSubTaskStatus FAILED: ${e.message}", e)
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

    /**
     * Kết thúc schedule từ một ngày cụ thể trở đi.
     * Schedule sẽ không hiển thị từ ngày endDate trở về sau.
     * @param taskId ID của task cần kết thúc
     * @param endDate Ngày bắt đầu kết thúc (format: YYYY-MM-DD)
     */
    fun onEndScheduleFromDate(taskId: String, endDate: String) {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isLoading = true, error = null)
                repo.updateSchedule(taskId, mapOf("end_date" to endDate))
                Log.d("Timeline", "set end_date ok taskId=$taskId endDate=$endDate")
                _ui.value = _ui.value.copy(isLoading = false)
                // Clear cached schedules to force reload
                cachedSchedules = emptyList()
                load(_ui.value.date)
            } catch (e: Exception) {
                Log.e("Timeline", "set end_date FAILED: ${e.message}", e)
                _ui.value = _ui.value.copy(isLoading = false, error = "Set end date failed: ${e.message}")
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

                // Load subtasks for all schedules
                val subtasksMap = mutableMapOf<String, List<com.projectapp.tempus.data.schedule.dto.SubTaskRow>>()
                for (taskId in taskIds) {
                    subtasksMap[taskId] = repo.getSubTasks(taskId)
                }

                val blocks = builder.build(date, schedules, scheduleItems, editedMap, subtasksMap)

                Log.d("Timeline", "build blocks=${blocks.size}")

                // Apply current filters and sort
                val filtered = applyFiltersAndSort(blocks)

                _ui.value = _ui.value.copy(
                    isLoading = false,
                    blocks = blocks,
                    filteredBlocks = filtered,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("Timeline", "load FAILED: ${e.message}", e)
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    blocks = emptyList(),
                    filteredBlocks = emptyList(),
                    error = "Load failed: ${e.message}"
                )
            }
        }
    }

    // ==================== SEARCH / SORT / FILTER ====================

    /**
     * Handle search query change - filters in real-time
     */
    fun onSearchQueryChanged(query: String) {
        _ui.value = _ui.value.copy(searchQuery = query)
        reapplyFilters()
    }

    /**
     * Handle sort option change
     */
    fun onSortChanged(sortOption: SortOption) {
        _ui.value = _ui.value.copy(sortBy = sortOption)
        reapplyFilters()
    }

    /**
     * Toggle label filter (OR logic - any matching label shows)
     */
    fun onFilterLabelToggle(label: ScheduleLabel) {
        val current = _ui.value.filterLabels.toMutableSet()
        if (label in current) {
            current.remove(label)
        } else {
            current.add(label)
        }
        _ui.value = _ui.value.copy(filterLabels = current)
        reapplyFilters()
    }

    /**
     * Toggle priority filter (OR logic - any matching priority shows)
     */
    fun onFilterPriorityToggle(priority: PriorityType) {
        val current = _ui.value.filterPriorities.toMutableSet()
        if (priority in current) {
            current.remove(priority)
        } else {
            current.add(priority)
        }
        _ui.value = _ui.value.copy(filterPriorities = current)
        reapplyFilters()
    }

    /**
     * Change status filter (null = show all)
     */
    fun onFilterStatusChanged(status: StatusType?) {
        _ui.value = _ui.value.copy(filterStatus = status)
        reapplyFilters()
    }

    /**
     * Clear all filters and search
     */
    fun clearAllFilters() {
        _ui.value = _ui.value.copy(
            searchQuery = "",
            sortBy = SortOption.START_TIME,
            filterLabels = emptySet(),
            filterPriorities = emptySet(),
            filterStatus = null,
            isFilterActive = false
        )
        reapplyFilters()
    }

    /**
     * Re-apply current filters to the loaded blocks
     */
    private fun reapplyFilters() {
        val filtered = applyFiltersAndSort(_ui.value.blocks)
        val hasActiveFilter = _ui.value.searchQuery.isNotEmpty() ||
                _ui.value.filterLabels.isNotEmpty() ||
                _ui.value.filterPriorities.isNotEmpty() ||
                _ui.value.filterStatus != null ||
                _ui.value.sortBy != SortOption.START_TIME
        
        _ui.value = _ui.value.copy(
            filteredBlocks = filtered,
            isFilterActive = hasActiveFilter
        )
    }

    /**
     * Apply search, filters, and sort to a list of blocks
     */
    private fun applyFiltersAndSort(blocks: List<TimelineBlock>): List<TimelineBlock> {
        val state = _ui.value
        
        return blocks
            .filter { block ->
                // Search filter - case insensitive contains on title
                val matchesSearch = state.searchQuery.isEmpty() || 
                    block.title.contains(state.searchQuery, ignoreCase = true)
                
                // Label filter - OR logic (show if matches ANY selected label)
                val matchesLabel = state.filterLabels.isEmpty() || 
                    block.labelEnum in state.filterLabels
                
                // Priority filter - OR logic
                val matchesPriority = state.filterPriorities.isEmpty() || 
                    block.priority in state.filterPriorities
                
                // Status filter - exact match
                val matchesStatus = state.filterStatus == null || 
                    block.status == state.filterStatus

                // All filters are AND-ed together
                matchesSearch && matchesLabel && matchesPriority && matchesStatus
            }
            .let { filtered ->
                when (state.sortBy) {
                    SortOption.START_TIME -> filtered.sortedBy { it.startTime }
                    SortOption.PRIORITY -> filtered.sortedBy { 
                        when (it.priority) {
                            PriorityType.high -> 0
                            PriorityType.medium -> 1
                            PriorityType.low -> 2
                        }
                    }
                    SortOption.CREATED_AT -> filtered.sortedByDescending { it.createdAt }
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

            // Check end_date - if set, schedule doesn't appear after this date
            val endDate = s.endDate?.let { 
                try { java.time.LocalDate.parse(it.split("T")[0].split(" ")[0]) } 
                catch (_: Exception) { null } 
            }
            if (endDate != null && !d.isBefore(endDate)) {
                return false // Schedule has ended
            }

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
                val labelStr = (evLabel?.name ?: s.label?.name) ?: "book"

                list.add(labelStr)
            }

            res[d] = list
        }

        return res
    }
}

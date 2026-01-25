package com.projectapp.tempus

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.gamification.GamificationRepository
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.domain.usecase.GetStatisticsUseCase
import com.projectapp.tempus.domain.usecase.StatisticsResult
import com.projectapp.tempus.ui.statistics.compose.StatisticsUiData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val result: StatisticsResult, val startDate: LocalDate, val endDate: LocalDate) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

class StatisticsViewModel(
    private val userId: String,
    private val repository: ScheduleRepository,
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val gamificationRepository: GamificationRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState

    // Compose UI data
    private val _composeUiData = MutableStateFlow(StatisticsUiData())
    val composeUiData: StateFlow<StatisticsUiData> = _composeUiData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentStartDate: LocalDate = LocalDate.now()
    private var currentEndDate: LocalDate = LocalDate.now()
    private var isWeekMode: Boolean = true

    fun setMode(isWeek: Boolean) {
        isWeekMode = isWeek
        val today = LocalDate.now()
        if (isWeek) {
            // Bắt đầu từ Thứ 2 của tuần này
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = start.plusDays(6)
            loadStatistics(start, end)
        } else {
            // Bắt đầu từ ngày 1 của tháng này
            val start = today.withDayOfMonth(1)
            val end = today.withDayOfMonth(today.lengthOfMonth())
            loadStatistics(start, end)
        }
    }

    fun loadStatistics(startDate: LocalDate, endDate: LocalDate) {
        currentStartDate = startDate
        currentEndDate = endDate
        
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            _isLoading.value = true
            
            try {
                val schedules = repository.getAllSchedules(userId)
                val taskIds = schedules.map { it.id }
                
                // Lấy các item trong khoảng thời gian
                val items = if (taskIds.isNotEmpty()) {
                    repository.getScheduleItemsByRange(
                        startDate.toString(),
                        endDate.toString(),
                        taskIds
                    )
                } else emptyList()
                
                val result = getStatisticsUseCase.execute(startDate, endDate, schedules, items)
                _uiState.value = StatisticsUiState.Success(result, startDate, endDate)
                
                // Update Compose UI data
                val rangeLabel = formatRangeLabel(startDate, endDate, isWeekMode)
                
                // Get Pomodoro stats
                val (pomodoroCount, pomodoroMinutes) = getPomodoroStats(startDate, endDate)
                
                _composeUiData.value = StatisticsUiData(
                    rangeLabel = rangeLabel,
                    totalTasks = result.totalTasksInRange,
                    completedTasks = result.completedTasksInRange,
                    dailyStats = result.dailyStats,
                    topCategories = result.topCategories,
                    insights = result.insights,
                    isWeekMode = isWeekMode,
                    pomodoroCount = pomodoroCount,
                    pomodoroMinutes = pomodoroMinutes
                )
                
                _isLoading.value = false
                
            } catch (e: Exception) {
                Log.e("StatisticsVM", "Error loading statistics: ${e.message}")
                _uiState.value = StatisticsUiState.Error(e.message ?: "Unknown error")
                _isLoading.value = false
            }
        }
    }

    private suspend fun getPomodoroStats(startDate: LocalDate, endDate: LocalDate): Pair<Int, Int> {
        if (gamificationRepository == null) return Pair(0, 0)
        
        try {
            val history = gamificationRepository.getPointHistory().first()
            
            val startMillis = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
            val endMillis = endDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
            
            val pomodoroHistory = history.filter { it.reason == "POMODORO_COMPLETE" && it.timestamp >= startMillis && it.timestamp < endMillis }
            
            val count = pomodoroHistory.size
            // Giả định mỗi phiên Pomodoro là 25 phút (có thể điều chỉnh nếu có dữ liệu thực)
            val minutes = count * 25
            
            return Pair(count, minutes)
        } catch (e: Exception) {
            Log.e("StatisticsVM", "Error getting pomodoro stats: ${e.message}")
            return Pair(0, 0)
        }
    }

    private fun formatRangeLabel(startDate: LocalDate, endDate: LocalDate, isWeek: Boolean): String {
        return if (isWeek) {
            val formatter = DateTimeFormatter.ofPattern("dd/MM")
            "${startDate.format(formatter)} - ${endDate.format(formatter)}"
        } else {
            val formatter = DateTimeFormatter.ofPattern("MM/yyyy")
            "Tháng ${startDate.format(formatter)}"
        }
    }

    fun navigateRange(direction: Int) {
        if (isWeekMode) {
            currentStartDate = currentStartDate.plusWeeks(direction.toLong())
            currentEndDate = currentEndDate.plusWeeks(direction.toLong())
        } else {
            currentStartDate = currentStartDate.plusMonths(direction.toLong()).withDayOfMonth(1)
            currentEndDate = currentStartDate.withDayOfMonth(currentStartDate.lengthOfMonth())
        }
        loadStatistics(currentStartDate, currentEndDate)
    }
}

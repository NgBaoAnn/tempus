package com.projectapp.tempus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.domain.usecase.GetStatisticsUseCase
import com.projectapp.tempus.domain.usecase.StatisticsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val result: StatisticsResult, val startDate: LocalDate, val endDate: LocalDate) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

class StatisticsViewModel(
    private val userId: String,
    private val repository: ScheduleRepository,
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState

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
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error(e.message ?: "Unknown error")
            }
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

package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.ScheduleItemRow
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import com.projectapp.tempus.data.schedule.dto.StatusType
import java.time.*
import java.time.format.DateTimeFormatter

// ======================== DATA MODELS ========================

/**
 * Heat level for visual color mapping
 */
enum class HeatLevel {
    NONE,      // Không có task nào
    LOW,       // < 30% completion
    MEDIUM,    // 30-60% completion
    HIGH,      // 60-80% completion
    EXCELLENT  // > 80% completion
}

/**
 * Heatmap data for a single day
 */
data class DayHeatmapData(
    val date: LocalDate,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Float,  // 0.0 - 1.0
    val heatLevel: HeatLevel,
    val topLabels: List<ScheduleLabel>  // Max 3 labels for display
)

/**
 * Summary statistics for a month
 */
data class MonthStats(
    val totalTasks: Int,
    val completedTasks: Int,
    val avgCompletionRate: Float,
    val bestDay: LocalDate?,
    val worstDay: LocalDate?,
    val daysWithTasks: Int
)

/**
 * Complete heatmap data for a month
 */
data class MonthHeatmapData(
    val yearMonth: YearMonth,
    val days: List<DayHeatmapData>,
    val monthStats: MonthStats
)

// ======================== USE CASE ========================

/**
 * UseCase to calculate heatmap data for calendar visualization.
 * Reuses logic from BuildTimelineUseCase for recurring task calculation.
 */
class GetHeatmapUseCase {

    /**
     * Parse timestamp string from database to ZonedDateTime.
     * Handles various formats from Postgres.
     */
    private fun parseToZonedDateTime(s: String): ZonedDateTime {
        val isoString = s.replace(" ", "T")
        return try {
            OffsetDateTime.parse(isoString).toZonedDateTime()
        } catch (_: Exception) {
            try {
                LocalDateTime.parse(isoString).atZone(ZoneId.of("UTC"))
            } catch (_: Exception) {
                LocalDate.parse(s.split(" ")[0]).atStartOfDay(ZoneId.systemDefault())
            }
        }
    }

    /**
     * Check if a schedule occurs on a specific date.
     * Handles all repeat types: once, daily, weekly, monthly, custom.
     */
    private fun occursOnDate(schedule: ScheduleRow, targetDate: LocalDate): Boolean {
        val systemZone = ZoneId.systemDefault()
        val startZdt = parseToZonedDateTime(schedule.startTimeDate).withZoneSameInstant(systemZone)
        val startDate = startZdt.toLocalDate()

        // Check end_date - if set, schedule doesn't appear after this date
        val endDate = schedule.endDate?.let {
            try { LocalDate.parse(it.split("T")[0].split(" ")[0]) }
            catch (_: Exception) { null }
        }
        if (endDate != null && !targetDate.isBefore(endDate)) {
            return false // Schedule has ended
        }

        // Must be after start date
        if (targetDate.isBefore(startDate)) return false

        return when (schedule.repeat) {
            RepeatType.once -> targetDate == startDate
            RepeatType.daily -> true // Already checked isBefore
            RepeatType.weekly -> targetDate.dayOfWeek == startDate.dayOfWeek
            RepeatType.monthly -> targetDate.dayOfMonth == startDate.dayOfMonth
            RepeatType.custom -> {
                // Parse repeat_days: "1,3,5" = Mon, Wed, Fri (1=Monday, 7=Sunday)
                val repeatDays = schedule.repeatDays?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                if (repeatDays.isEmpty()) return false
                repeatDays.contains(targetDate.dayOfWeek.value)
            }
        }
    }

    /**
     * Convert completion rate to heat level for color mapping.
     */
    private fun rateToHeatLevel(rate: Float, hasTasks: Boolean): HeatLevel {
        if (!hasTasks) return HeatLevel.NONE
        return when {
            rate >= 0.8f -> HeatLevel.EXCELLENT
            rate >= 0.6f -> HeatLevel.HIGH
            rate >= 0.3f -> HeatLevel.MEDIUM
            else -> HeatLevel.LOW
        }
    }

    /**
     * Calculate heatmap data for an entire month.
     *
     * @param yearMonth The month to calculate
     * @param schedules All user's schedules
     * @param items Schedule items (status per date) for the month
     * @return MonthHeatmapData with day-by-day data and summary stats
     */
    fun execute(
        yearMonth: YearMonth,
        schedules: List<ScheduleRow>,
        items: List<ScheduleItemRow>
    ): MonthHeatmapData {
        val systemZone = ZoneId.systemDefault()
        
        // Group items by date and taskId for quick lookup
        val itemsByDateAndTask = items.groupBy { it.date }
            .mapValues { (_, list) -> list.associateBy { it.taskId } }
        
        val days = mutableListOf<DayHeatmapData>()
        var totalMonthTasks = 0
        var totalMonthCompleted = 0
        var daysWithTasks = 0
        var bestDay: Pair<LocalDate, Float>? = null
        var worstDay: Pair<LocalDate, Float>? = null

        // Iterate through each day in the month
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        var currentDate = firstDay

        while (!currentDate.isAfter(lastDay)) {
            val dateStr = currentDate.toString()
            val itemsForDay = itemsByDateAndTask[dateStr] ?: emptyMap()

            var dayTotal = 0
            var dayCompleted = 0
            val labelsForDay = mutableListOf<ScheduleLabel>()

            for (schedule in schedules) {
                if (!occursOnDate(schedule, currentDate)) continue

                val item = itemsForDay[schedule.id]
                val status = item?.status ?: StatusType.planned

                // Skip deleted items
                if (status == StatusType.delete) continue

                dayTotal++
                if (status == StatusType.done) {
                    dayCompleted++
                }

                // Collect labels for display (max 3 unique)
                schedule.label?.let { label ->
                    if (label !in labelsForDay && labelsForDay.size < 3) {
                        labelsForDay.add(label)
                    }
                }
            }

            // Calculate completion rate
            val completionRate = if (dayTotal > 0) {
                dayCompleted.toFloat() / dayTotal.toFloat()
            } else {
                0f
            }

            val heatLevel = rateToHeatLevel(completionRate, dayTotal > 0)

            days.add(
                DayHeatmapData(
                    date = currentDate,
                    totalTasks = dayTotal,
                    completedTasks = dayCompleted,
                    completionRate = completionRate,
                    heatLevel = heatLevel,
                    topLabels = labelsForDay.toList()
                )
            )

            // Update month totals
            totalMonthTasks += dayTotal
            totalMonthCompleted += dayCompleted
            if (dayTotal > 0) {
                daysWithTasks++
                
                // Track best/worst day (only for days with tasks)
                if (bestDay == null || completionRate > bestDay.second) {
                    bestDay = Pair(currentDate, completionRate)
                }
                if (worstDay == null || completionRate < worstDay.second) {
                    worstDay = Pair(currentDate, completionRate)
                }
            }

            currentDate = currentDate.plusDays(1)
        }

        // Calculate month average
        val avgRate = if (totalMonthTasks > 0) {
            totalMonthCompleted.toFloat() / totalMonthTasks.toFloat()
        } else {
            0f
        }

        val monthStats = MonthStats(
            totalTasks = totalMonthTasks,
            completedTasks = totalMonthCompleted,
            avgCompletionRate = avgRate,
            bestDay = bestDay?.first,
            worstDay = worstDay?.first,
            daysWithTasks = daysWithTasks
        )

        return MonthHeatmapData(
            yearMonth = yearMonth,
            days = days,
            monthStats = monthStats
        )
    }

    /**
     * Calculate heatmap data for a date range (for multi-month views).
     */
    fun executeForRange(
        startDate: LocalDate,
        endDate: LocalDate,
        schedules: List<ScheduleRow>,
        items: List<ScheduleItemRow>
    ): List<DayHeatmapData> {
        val itemsByDateAndTask = items.groupBy { it.date }
            .mapValues { (_, list) -> list.associateBy { it.taskId } }
        
        val days = mutableListOf<DayHeatmapData>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            val dateStr = currentDate.toString()
            val itemsForDay = itemsByDateAndTask[dateStr] ?: emptyMap()

            var dayTotal = 0
            var dayCompleted = 0
            val labelsForDay = mutableListOf<ScheduleLabel>()

            for (schedule in schedules) {
                if (!occursOnDate(schedule, currentDate)) continue

                val item = itemsForDay[schedule.id]
                val status = item?.status ?: StatusType.planned

                if (status == StatusType.delete) continue

                dayTotal++
                if (status == StatusType.done) {
                    dayCompleted++
                }

                schedule.label?.let { label ->
                    if (label !in labelsForDay && labelsForDay.size < 3) {
                        labelsForDay.add(label)
                    }
                }
            }

            val completionRate = if (dayTotal > 0) {
                dayCompleted.toFloat() / dayTotal.toFloat()
            } else {
                0f
            }

            days.add(
                DayHeatmapData(
                    date = currentDate,
                    totalTasks = dayTotal,
                    completedTasks = dayCompleted,
                    completionRate = completionRate,
                    heatLevel = rateToHeatLevel(completionRate, dayTotal > 0),
                    topLabels = labelsForDay.toList()
                )
            )

            currentDate = currentDate.plusDays(1)
        }

        return days
    }
}

package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.ScheduleItemRow
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import com.projectapp.tempus.data.schedule.dto.StatusType
import java.time.*
import java.time.format.DateTimeFormatter

data class DayStats(
    val date: LocalDate,
    val completionPercentage: Float,
    val totalTasks: Int,
    val completedTasks: Int
)

data class CategoryStats(
    val label: String,
    val totalCount: Int,
    val completedCount: Int,
    val percentage: Int
)

data class StatisticsResult(
    val dailyStats: List<DayStats>,
    val topCategories: List<CategoryStats>,
    val totalTasksInRange: Int,
    val completedTasksInRange: Int
)

class GetStatisticsUseCase {

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

    fun execute(
        startDate: LocalDate,
        endDate: LocalDate,
        schedules: List<ScheduleRow>,
        items: List<ScheduleItemRow>
    ): StatisticsResult {
        val systemZone = ZoneId.systemDefault()
        val dailyStats = mutableListOf<DayStats>()
        val categoryMap = mutableMapOf<String, Pair<Int, Int>>()

        var totalInRange = 0
        var completedInRange = 0

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dateStr = currentDate.toString()
            val itemsForDay = items.filter { it.date == dateStr }.associateBy { it.taskId }

            var dayTotal = 0
            var dayCompleted = 0

            for (s in schedules) {
                val startZdt = parseToZonedDateTime(s.startTimeDate).withZoneSameInstant(systemZone)
                val startLocalDate = startZdt.toLocalDate()

                val occurs = when (s.repeat) {
                    RepeatType.once -> currentDate == startLocalDate
                    RepeatType.daily -> !currentDate.isBefore(startLocalDate)
                    RepeatType.weekly -> !currentDate.isBefore(startLocalDate) && currentDate.dayOfWeek == startLocalDate.dayOfWeek
                    RepeatType.monthly -> !currentDate.isBefore(startLocalDate) && currentDate.dayOfMonth == startLocalDate.dayOfMonth
                }

                if (occurs) {
                    val item = itemsForDay[s.id]
                    val status = item?.status ?: StatusType.planned

                    if (status != StatusType.delete) {
                        dayTotal++
                        if (status == StatusType.done) {
                            dayCompleted++
                        }

                        val labelName = s.label?.name ?: "other"
                        val currentCat = categoryMap.getOrDefault(labelName, Pair(0, 0))
                        categoryMap[labelName] = Pair(
                            currentCat.first + 1,
                            currentCat.second + (if (status == StatusType.done) 1 else 0)
                        )
                    }
                }
            }

            // [SỬA QUAN TRỌNG] Đảm bảo không bị chia cho 0 và làm tròn đúng
            val percentage = if (dayTotal > 0) {
                (dayCompleted.toFloat() / dayTotal.toFloat()) * 100f
            } else {
                0f
            }
            
            dailyStats.add(DayStats(currentDate, percentage, dayTotal, dayCompleted))
            
            totalInRange += dayTotal
            completedInRange += dayCompleted
            
            currentDate = currentDate.plusDays(1)
        }

        val topCategories = categoryMap.map { (label, counts) ->
            CategoryStats(
                label = label,
                totalCount = counts.first,
                completedCount = counts.second,
                percentage = if (counts.first > 0) (counts.second * 100 / counts.first) else 0
            )
        }.sortedByDescending { it.completedCount }

        return StatisticsResult(
            dailyStats = dailyStats,
            topCategories = topCategories,
            totalTasksInRange = totalInRange,
            completedTasksInRange = completedInRange
        )
    }
}

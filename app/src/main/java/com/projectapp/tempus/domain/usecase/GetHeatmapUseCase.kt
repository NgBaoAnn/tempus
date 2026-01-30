package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.ScheduleItemRow
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import com.projectapp.tempus.data.schedule.dto.StatusType
import java.time.*
import java.time.format.DateTimeFormatter


enum class HeatLevel {
    NONE,      
    LOW,       
    MEDIUM,    
    HIGH,      
    EXCELLENT  
}


data class DayHeatmapData(
    val date: LocalDate,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Float,  
    val heatLevel: HeatLevel,
    val topLabels: List<ScheduleLabel>  
)


data class MonthStats(
    val totalTasks: Int,
    val completedTasks: Int,
    val avgCompletionRate: Float,
    val bestDay: LocalDate?,
    val worstDay: LocalDate?,
    val daysWithTasks: Int
)


data class MonthHeatmapData(
    val yearMonth: YearMonth,
    val days: List<DayHeatmapData>,
    val monthStats: MonthStats
)


class GetHeatmapUseCase {

    
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

    
    private fun occursOnDate(schedule: ScheduleRow, targetDate: LocalDate): Boolean {
        val systemZone = ZoneId.systemDefault()
        val startZdt = parseToZonedDateTime(schedule.startTimeDate).withZoneSameInstant(systemZone)
        val startDate = startZdt.toLocalDate()

        
        val endDate = schedule.endDate?.let {
            try { LocalDate.parse(it.split("T")[0].split(" ")[0]) }
            catch (_: Exception) { null }
        }
        if (endDate != null && !targetDate.isBefore(endDate)) {
            return false 
        }

        
        if (targetDate.isBefore(startDate)) return false

        return when (schedule.repeat) {
            RepeatType.once -> targetDate == startDate
            RepeatType.daily -> true 
            RepeatType.weekly -> targetDate.dayOfWeek == startDate.dayOfWeek
            RepeatType.monthly -> targetDate.dayOfMonth == startDate.dayOfMonth
            RepeatType.custom -> {
                
                val repeatDays = schedule.repeatDays?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                if (repeatDays.isEmpty()) return false
                repeatDays.contains(targetDate.dayOfWeek.value)
            }
        }
    }

    
    private fun rateToHeatLevel(rate: Float, hasTasks: Boolean): HeatLevel {
        if (!hasTasks) return HeatLevel.NONE
        return when {
            rate >= 0.8f -> HeatLevel.EXCELLENT
            rate >= 0.6f -> HeatLevel.HIGH
            rate >= 0.3f -> HeatLevel.MEDIUM
            else -> HeatLevel.LOW
        }
    }

    
    fun execute(
        yearMonth: YearMonth,
        schedules: List<ScheduleRow>,
        items: List<ScheduleItemRow>
    ): MonthHeatmapData {
        val systemZone = ZoneId.systemDefault()
        
        
        val itemsByDateAndTask = items.groupBy { it.date }
            .mapValues { (_, list) -> list.associateBy { it.taskId } }
        
        val days = mutableListOf<DayHeatmapData>()
        var totalMonthTasks = 0
        var totalMonthCompleted = 0
        var daysWithTasks = 0
        var bestDay: Pair<LocalDate, Float>? = null
        var worstDay: Pair<LocalDate, Float>? = null

        
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

            
            totalMonthTasks += dayTotal
            totalMonthCompleted += dayCompleted
            if (dayTotal > 0) {
                daysWithTasks++
                
                
                if (bestDay == null || completionRate > bestDay.second) {
                    bestDay = Pair(currentDate, completionRate)
                }
                if (worstDay == null || completionRate < worstDay.second) {
                    worstDay = Pair(currentDate, completionRate)
                }
            }

            currentDate = currentDate.plusDays(1)
        }

        
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

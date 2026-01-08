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

// ===== PRODUCTIVITY INSIGHTS =====

enum class TrendType { UP, DOWN, STABLE }

data class InsightsData(
    val avgCompletionRate: Float,              // Tỷ lệ hoàn thành trung bình (%)
    val bestDay: Pair<String, Float>?,         // (Tên ngày, %) 
    val worstDay: Pair<String, Float>?,        // (Tên ngày, %)
    val trend: TrendType,                      // Xu hướng so với kỳ trước
    val suggestion: String,                    // Gợi ý thông minh
    val comparisonToPrevious: Float?           // So với kỳ trước (+/- %)
)

data class StatisticsResult(
    val dailyStats: List<DayStats>,
    val topCategories: List<CategoryStats>,
    val totalTasksInRange: Int,
    val completedTasksInRange: Int,
    val insights: InsightsData? = null          // NEW: Productivity insights
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

        // ===== CALCULATE INSIGHTS =====
        val insights = calculateInsights(dailyStats, totalInRange, completedInRange)

        return StatisticsResult(
            dailyStats = dailyStats,
            topCategories = topCategories,
            totalTasksInRange = totalInRange,
            completedTasksInRange = completedInRange,
            insights = insights
        )
    }

    private fun calculateInsights(
        dailyStats: List<DayStats>,
        totalTasks: Int,
        completedTasks: Int
    ): InsightsData {
        val today = java.time.LocalDate.now()
        
        // Lấy ngày cuối cùng của kỳ thống kê
        val lastDayOfPeriod = dailyStats.maxByOrNull { it.date }?.date
        
        // Kiểm tra xem kỳ này có chứa ngày hôm nay không (kỳ hiện tại)
        val isCurrentPeriod = lastDayOfPeriod != null && !lastDayOfPeriod.isBefore(today)
        
        // Nếu là kỳ hiện tại: chỉ xét đến hôm nay
        // Nếu là kỳ đã qua: xét toàn bộ
        val relevantDays = if (isCurrentPeriod) {
            dailyStats.filter { !it.date.isAfter(today) }
        } else {
            dailyStats
        }
        
        // Tính total và completed cho các ngày được xét
        val relevantTotalTasks = relevantDays.sumOf { it.totalTasks }
        val relevantCompletedTasks = relevantDays.sumOf { it.completedTasks }
        
        // 1. Tỷ lệ hoàn thành trung bình
        val avgRate = if (relevantTotalTasks > 0) {
            (relevantCompletedTasks.toFloat() / relevantTotalTasks.toFloat()) * 100f
        } else 0f

        // 2. Tìm ngày tốt nhất / tệ nhất (xét TẤT CẢ các ngày, kể cả 0%)
        val bestDayStats = relevantDays.maxByOrNull { it.completionPercentage }
        val worstDayStats = relevantDays.minByOrNull { it.completionPercentage }

        // Kiểm tra xem là tuần hay tháng để format cho phù hợp
        val isWeekMode = dailyStats.size <= 7
        val dayFormatter = if (isWeekMode) {
            java.time.format.DateTimeFormatter.ofPattern("EEEE", java.util.Locale("vi")) // Thứ Hai, Thứ Ba...
        } else {
            java.time.format.DateTimeFormatter.ofPattern("'Ngày' d", java.util.Locale("vi")) // Ngày 1, Ngày 2...
        }
        
        val bestDay = bestDayStats?.let {
            Pair(it.date.format(dayFormatter), it.completionPercentage)
        }
        val worstDay = worstDayStats?.let {
            Pair(it.date.format(dayFormatter), it.completionPercentage)
        }

        // 3. Xu hướng (so sánh nửa đầu vs nửa cuối của kỳ được xét)
        val trend = calculateTrend(relevantDays)

        // 4. Gợi ý thông minh
        val suggestion = generateSuggestion(avgRate, worstDay, trend)

        return InsightsData(
            avgCompletionRate = avgRate,
            bestDay = bestDay,
            worstDay = worstDay,
            trend = trend,
            suggestion = suggestion,
            comparisonToPrevious = null // Sẽ được cập nhật bởi ViewModel khi fetch kỳ trước
        )
    }

    private fun calculateTrend(dailyStats: List<DayStats>): TrendType {
        if (dailyStats.size < 2) return TrendType.STABLE
        
        val midPoint = dailyStats.size / 2
        val firstHalf = dailyStats.take(midPoint)
        val secondHalf = dailyStats.drop(midPoint)

        val avgFirst = if (firstHalf.isNotEmpty()) 
            firstHalf.map { it.completionPercentage }.average() else 0.0
        val avgSecond = if (secondHalf.isNotEmpty()) 
            secondHalf.map { it.completionPercentage }.average() else 0.0

        val diff = avgSecond - avgFirst
        return when {
            diff > 5 -> TrendType.UP
            diff < -5 -> TrendType.DOWN
            else -> TrendType.STABLE
        }
    }

    private fun generateSuggestion(avgRate: Float, worstDay: Pair<String, Float>?, trend: TrendType): String {
        return when {
            avgRate >= 80f -> "Xuất sắc! Bạn rất có kỷ luật! ⭐"
            avgRate >= 60f && trend == TrendType.UP -> "Tuyệt vời! Bạn đang tiến bộ 📈"
            avgRate >= 60f -> "Tốt lắm! Hãy duy trì nhịp độ này 💪"
            worstDay != null && worstDay.second < 30f -> "Thử giảm task vào ${worstDay.first} để dễ hoàn thành hơn"
            trend == TrendType.DOWN -> "Hãy cố gắng duy trì nhịp độ nhé! 💪"
            avgRate < 40f -> "Hãy thử đặt ít task hơn để dễ hoàn thành"
            else -> "Tiếp tục cố gắng! Mỗi ngày một tiến bộ 🚀"
        }
    }
}

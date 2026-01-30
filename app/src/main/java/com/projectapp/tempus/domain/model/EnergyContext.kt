package com.projectapp.tempus.domain.model

import java.time.DayOfWeek


data class EnergyContext(
    
    val userId: String,
    
    
    val wakeTime: String = "07:00",
    
    
    val sleepTime: String = "23:00",
    
    
    val peakHours: List<Int> = listOf(9, 10, 11, 14, 15),
    
    
    val lowEnergyHours: List<Int> = listOf(13, 22),
    
    
    val workDays: List<DayOfWeek> = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    ),
    
    
    val preferredBreakDuration: Int = 15,
    
    
    val maxFocusHoursPerDay: Int = 6
) {
    
    fun isPeakHour(hour: Int): Boolean = hour in peakHours
    
    
    fun isLowEnergyHour(hour: Int): Boolean = hour in lowEnergyHours
    
    
    fun getBestSlotsForDifficultTasks(): List<String> {
        return peakHours.map { hour ->
            String.format("%02d:00", hour)
        }
    }
    
    
    fun getSlotsForLightTasks(): List<String> {
        val allHours = (wakeTime.substringBefore(":").toIntOrNull() ?: 7)..
                       (sleepTime.substringBefore(":").toIntOrNull() ?: 23)
        
        return allHours
            .filter { it !in peakHours && it !in lowEnergyHours }
            .map { String.format("%02d:00", it) }
    }
    
    
    fun toContextString(): String {
        val workDaysStr = workDays.joinToString(", ") { it.name.lowercase() }
        val peakHoursStr = peakHours.joinToString(", ") { "${it}:00" }
        
        return """
            |Thời gian hoạt động: $wakeTime - $sleepTime
            |Ngày làm việc: $workDaysStr
            |Giờ productive nhất: $peakHoursStr
            |Thời gian nghỉ giữa tasks: ${preferredBreakDuration} phút
            |Tối đa ${maxFocusHoursPerDay}h tập trung/ngày
        """.trimMargin()
    }
}

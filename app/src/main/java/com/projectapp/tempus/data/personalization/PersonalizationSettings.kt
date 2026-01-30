package com.projectapp.tempus.data.personalization

import kotlinx.serialization.Serializable


@Serializable
data class CustomTimePeriod(
    val id: String,
    val name: String,
    val startTime: String,  
    val endTime: String,    
    val color: String = "#007AFF",
    val description: String = "",
    val label: String = "book" 
)


enum class LifestylePreset(
    val displayName: String,
    val wakeUpTime: String,
    val sleepTime: String,
    val workStartTime: String,
    val workEndTime: String
) {
    STUDENT(
        displayName = "Học sinh/Sinh viên",
        wakeUpTime = "06:30",
        sleepTime = "23:00",
        workStartTime = "07:30",
        workEndTime = "17:00"
    ),
    OFFICE_WORKER(
        displayName = "Nhân viên văn phòng",
        wakeUpTime = "06:00",
        sleepTime = "22:30",
        workStartTime = "08:00",
        workEndTime = "17:30"
    ),
    FREELANCER(
        displayName = "Freelancer",
        wakeUpTime = "08:00",
        sleepTime = "00:00",
        workStartTime = "09:00",
        workEndTime = "18:00"
    ),
    NIGHT_SHIFT(
        displayName = "Ca đêm",
        wakeUpTime = "15:00",
        sleepTime = "07:00",
        workStartTime = "22:00",
        workEndTime = "06:00"
    ),
    ELDERLY(
        displayName = "Người cao tuổi",
        wakeUpTime = "05:30",
        sleepTime = "21:00",
        workStartTime = "07:00",
        workEndTime = "11:00"
    ),
    STAY_AT_HOME(
        displayName = "Nội trợ",
        wakeUpTime = "06:00",
        sleepTime = "22:00",
        workStartTime = "07:00",
        workEndTime = "12:00"
    ),
    CUSTOM(
        displayName = "Tùy chỉnh",
        wakeUpTime = "07:00",
        sleepTime = "23:00",
        workStartTime = "08:00",
        workEndTime = "17:00"
    )
}


@Serializable
data class PersonalizationSettings(
    val lifestyle: String = LifestylePreset.CUSTOM.name,
    val wakeUpTime: String = "07:00",      
    val sleepTime: String = "23:00",       
    val workStartTime: String = "08:00",   
    val workEndTime: String = "17:00",     
    val customTimePeriods: List<CustomTimePeriod> = emptyList(),
    
    val activeDays: List<Int> = listOf(1, 2, 3, 4, 5, 6) 
)

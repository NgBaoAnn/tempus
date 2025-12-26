package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable enum class RepeatType { once, daily, weekly, monthly }
@Serializable enum class SourceType { manual, ai }
@Serializable enum class StatusType { planned, done, skip, delete }

@Serializable
enum class ScheduleLabel {
    @SerialName("wakeup") wakeup,
    @SerialName("eat") eat,
    @SerialName("exercise") exercise,
    @SerialName("rest") rest,
    @SerialName("water") water,
    @SerialName("book") book,
    @SerialName("sleep") sleep,
    @SerialName("clean") clean,

    @SerialName("unknown") UNKNOWN;

    companion object {
        fun fromDb(v: String?): ScheduleLabel {
            if (v.isNullOrBlank()) return book
            return values().firstOrNull { it.name == v } ?: UNKNOWN
        }
    }
}
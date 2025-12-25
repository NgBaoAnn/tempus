package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RepeatType {
    @SerialName("once") once,
    @SerialName("daily") daily,
    @SerialName("weekly") weekly,
    @SerialName("monthly") monthly
}

@Serializable
enum class StatusType {
    @SerialName("planned") planned,
    @SerialName("done") done,
    @SerialName("delete") delete
}

@Serializable
enum class SourceType {
    @SerialName("manual") manual,
    @SerialName("ai") ai
}

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
    @SerialName("cook") cook
}

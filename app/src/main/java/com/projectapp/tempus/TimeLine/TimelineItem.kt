package com.projectapp.tempus.TimeLine

data class TimelineItem(
    val title: String,
    val type: String,
    val startTime: String,
    val durationMinutes: Int,
    val iconId: Int,
    val color: String,
    val status: String,
    val source: String
)

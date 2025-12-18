package com.projectapp.tempus.data.schedule.dto

import kotlinx.serialization.Serializable

@Serializable enum class RepeatType { once, daily, weekly, monthly }
@Serializable enum class SourceType { manual, ai }
@Serializable enum class StatusType { planned, done, skip, delete }
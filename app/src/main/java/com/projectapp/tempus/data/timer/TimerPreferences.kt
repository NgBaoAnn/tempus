package com.projectapp.tempus.data.timer

import android.content.Context
import android.content.SharedPreferences

class TimerPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "tempus_timer_prefs"
        private const val KEY_TARGET_TIME = "target_time"
        private const val KEY_TOTAL_SECONDS = "total_seconds"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_IS_PAUSED = "is_paused"
        private const val KEY_REMAINING_AT_PAUSE = "remaining_at_pause"
    }

    fun saveTimerState(
        targetTime: Long,
        totalSeconds: Long,
        isRunning: Boolean,
        isPaused: Boolean,
        remainingAtPause: Long
    ) {
        prefs.edit().apply {
            putLong(KEY_TARGET_TIME, targetTime)
            putLong(KEY_TOTAL_SECONDS, totalSeconds)
            putBoolean(KEY_IS_RUNNING, isRunning)
            putBoolean(KEY_IS_PAUSED, isPaused)
            putLong(KEY_REMAINING_AT_PAUSE, remainingAtPause)
            apply()
        }
    }

    fun clearTimerState() {
        prefs.edit().clear().apply()
    }

    fun getTimerState(): Dictionary {
        return Dictionary(
            targetTime = prefs.getLong(KEY_TARGET_TIME, 0),
            totalSeconds = prefs.getLong(KEY_TOTAL_SECONDS, 0),
            isRunning = prefs.getBoolean(KEY_IS_RUNNING, false),
            isPaused = prefs.getBoolean(KEY_IS_PAUSED, false),
            remainingAtPause = prefs.getLong(KEY_REMAINING_AT_PAUSE, 0)
        )
    }

    data class Dictionary(
        val targetTime: Long,
        val totalSeconds: Long,
        val isRunning: Boolean,
        val isPaused: Boolean,
        val remainingAtPause: Long
    )
}

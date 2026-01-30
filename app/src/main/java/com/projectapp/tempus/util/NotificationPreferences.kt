package com.projectapp.tempus.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationPreferences {

    private const val PREF_NAME = "tempus_notification_prefs"
    private const val KEY_TIMER_ENABLED = "pref_notification_timer"
    private const val KEY_TIMELINE_ENABLED = "pref_notification_timeline"

    private lateinit var prefs: SharedPreferences

    private val _timerEnabled = MutableStateFlow(true)
    val timerEnabled: StateFlow<Boolean> = _timerEnabled.asStateFlow()

    private val _timelineEnabled = MutableStateFlow(true)
    val timelineEnabled: StateFlow<Boolean> = _timelineEnabled.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        _timerEnabled.value = prefs.getBoolean(KEY_TIMER_ENABLED, true)
        _timelineEnabled.value = prefs.getBoolean(KEY_TIMELINE_ENABLED, true)
    }

    fun setTimerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TIMER_ENABLED, enabled).apply()
        _timerEnabled.value = enabled
    }

    fun setTimelineEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TIMELINE_ENABLED, enabled).apply()
        _timelineEnabled.value = enabled
    }

    fun isTimerEnabled(): Boolean {
        if (!::prefs.isInitialized) return true // Default safely
        return prefs.getBoolean(KEY_TIMER_ENABLED, true)
    }

    fun isTimelineEnabled(): Boolean {
        if (!::prefs.isInitialized) return true // Default safely
        return prefs.getBoolean(KEY_TIMELINE_ENABLED, true)
    }

    // ============================================================================================
    // Notification History & Duplicate Prevention
    // ============================================================================================

    private const val PREFIX_NOTIFIED = "notified_"

    private fun getPrefs(context: Context): SharedPreferences {
        if (!::prefs.isInitialized) {
            init(context)
        }
        return prefs
    }

    fun isTaskNotified(context: Context, taskId: String, date: String): Boolean {
        val key = "$PREFIX_NOTIFIED${taskId}_$date"
        return getPrefs(context).getBoolean(key, false)
    }

    fun markTaskNotified(context: Context, taskId: String, date: String) {
        val key = "$PREFIX_NOTIFIED${taskId}_$date"
        getPrefs(context).edit().putBoolean(key, true).apply()
    }

    fun clearTaskNotified(context: Context, taskId: String, date: String) {
        val key = "$PREFIX_NOTIFIED${taskId}_$date"
        getPrefs(context).edit().remove(key).apply()
    }

    fun clearOldNotifications(context: Context) {
        val prefs = getPrefs(context)
        val allEntries = prefs.all
        val editor = prefs.edit()
        val today = java.time.LocalDate.now()
        
        // Remove keys older than 2 days to keep prefs clean
        try {
            allEntries.keys.filter { it.startsWith(PREFIX_NOTIFIED) }.forEach { key ->
                // key format: notified_{taskId}_{date}
                // We just check if it parses and is old. The simple date string usually fits ISO local date
                val parts = key.split("_")
                val dateStr = parts.lastOrNull() // Warning: if taskId contains _, this is risky. 
                // Better approach: date is usually yyyy-MM-dd (10 chars) at the end if we use standard format.
                // Or just keep it simple: clear everything > 7 days or just clear everything if we don't care about history too much.
                // The original code comment said "older than 7 days".
                
                if (!dateStr.isNullOrEmpty()) {
                    try {
                        // Assuming date is correct. If strict parsing fails, ignore.
                        // Simple cleanup: if we can parse the date and it's old, delete.
                        // For now, let's just leave this empty or minimal if we don't know the exact date format used before, 
                        // but ReminderReceiver uses LocalDate.now().toString() which is YYYY-MM-DD.
                        val entryDate = java.time.LocalDate.parse(dateStr)
                        if (entryDate.isBefore(today.minusDays(7))) {
                            editor.remove(key)
                        }
                    } catch (e: Exception) {
                        // If date parse fails, maybe cleanup anyway if it looks like a notified key? 
                        // Safer to leave it if unsure.
                    }
                }
            }
            editor.apply()
        } catch (e: Exception) {
            // Ignore errors during cleanup
        }
    }
}

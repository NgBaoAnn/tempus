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
        if (!::prefs.isInitialized) return true 
        return prefs.getBoolean(KEY_TIMER_ENABLED, true)
    }

    fun isTimelineEnabled(): Boolean {
        if (!::prefs.isInitialized) return true 
        return prefs.getBoolean(KEY_TIMELINE_ENABLED, true)
    }

    
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
        
        
        try {
            allEntries.keys.filter { it.startsWith(PREFIX_NOTIFIED) }.forEach { key ->
                
                
                val parts = key.split("_")
                val dateStr = parts.lastOrNull() 
                
                
                if (!dateStr.isNullOrEmpty()) {
                    try {
                        
                        
                        val entryDate = java.time.LocalDate.parse(dateStr)
                        if (entryDate.isBefore(today.minusDays(7))) {
                            editor.remove(key)
                        }
                    } catch (e: Exception) {
                        
                        
                    }
                }
            }
            editor.apply()
        } catch (e: Exception) {
            
        }
    }
}

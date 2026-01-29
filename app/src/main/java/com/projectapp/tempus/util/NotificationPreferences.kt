package com.projectapp.tempus.util

import android.content.Context
import java.time.LocalDate

/**
 * SharedPreferences để track các task đã được thông báo
 * Key format: taskId_date để hỗ trợ task lặp lại hàng ngày
 */
object NotificationPreferences {
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_PREFIX = "notified_"
    
    private fun getPrefs(context: Context) = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Check if a task has been notified for a specific date
     */
    fun isTaskNotified(context: Context, taskId: String, date: String): Boolean {
        val key = "$KEY_PREFIX${taskId}_$date"
        return getPrefs(context).getBoolean(key, false)
    }
    
    /**
     * Mark a task as notified for a specific date
     */
    fun markTaskNotified(context: Context, taskId: String, date: String) {
        val key = "$KEY_PREFIX${taskId}_$date"
        getPrefs(context).edit().putBoolean(key, true).apply()
    }
    
    /**
     * Clear notification status for a task (used by snooze to allow re-notification)
     */
    fun clearTaskNotified(context: Context, taskId: String, date: String) {
        val key = "$KEY_PREFIX${taskId}_$date"
        getPrefs(context).edit().remove(key).apply()
    }
    
    /**
     * Clear notifications older than 7 days to save storage
     */
    fun clearOldNotifications(context: Context) {
        val prefs = getPrefs(context)
        val allEntries = prefs.all
        val today = LocalDate.now()
        val cutoffDate = today.minusDays(7)
        
        val editor = prefs.edit()
        allEntries.keys.filter { it.startsWith(KEY_PREFIX) }.forEach { key ->
            try {
                // Key format: notified_taskId_2026-01-29
                val datePart = key.substringAfterLast("_")
                val entryDate = LocalDate.parse(datePart)
                if (entryDate.isBefore(cutoffDate)) {
                    editor.remove(key)
                }
            } catch (e: Exception) {
                // Invalid date format, remove it
                editor.remove(key)
            }
        }
        editor.apply()
    }
}

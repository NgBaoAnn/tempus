package com.projectapp.tempus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.projectapp.tempus.service.ReminderScheduler
import com.projectapp.tempus.util.NotificationPreferences
import java.time.LocalDate

/**
 * Handles notification snooze action
 * Reschedules the alarm to fire again in 5 minutes
 */
class NotificationSnoozeReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SNOOZE = "com.projectapp.tempus.NOTIFICATION_SNOOZE"
        const val SNOOZE_DURATION_MINUTES = 5L
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SNOOZE) return

        val taskId = intent.getStringExtra("TASK_ID") ?: return
        val title = intent.getStringExtra("TITLE") ?: "Task"
        val startTime = intent.getStringExtra("START_TIME") ?: ""
        val endTime = intent.getStringExtra("END_TIME") ?: ""
        val priority = intent.getStringExtra("PRIORITY") ?: "medium"
        val categoryLabel = intent.getStringExtra("CATEGORY_LABEL") ?: ""
        val color = intent.getStringExtra("COLOR") ?: "#2196F3"

        Log.d("SnoozeReceiver", "⏰ Snoozing task '$title' for $SNOOZE_DURATION_MINUTES minutes")

        // Cancel current notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(taskId.hashCode())

        // IMPORTANT: Clear the "notified" status so the snoozed alarm won't be skipped
        val today = LocalDate.now().toString()
        NotificationPreferences.clearTaskNotified(context, taskId, today)
        Log.d("SnoozeReceiver", "Cleared notification status for reschedule")

        // Schedule a new alarm in 5 minutes
        val snoozeTime = java.time.OffsetDateTime.now().plusMinutes(SNOOZE_DURATION_MINUTES)
        val snoozeTimeStr = snoozeTime.format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)

        val scheduler = ReminderScheduler(context)
        scheduler.scheduleReminderWithDetails(
            taskId = taskId,
            title = title,
            startDateTime = snoozeTimeStr,
            endDateTime = endTime,
            priority = priority,
            categoryLabel = categoryLabel,
            color = color,
            isSnooze = true  // Use different request code to avoid being overwritten
        )

        Log.d("SnoozeReceiver", "✅ Snoozed until ${snoozeTime.toLocalTime()}")
    }
}

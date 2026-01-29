package com.projectapp.tempus.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.projectapp.tempus.util.NotificationPreferences
import com.projectapp.tempus.util.TimelineNotificationHelper
import java.time.LocalDate

/**
 * BroadcastReceiver for handling scheduled task reminders
 * Checks for duplicate notifications before showing
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "========================================")
        Log.d("ReminderReceiver", "🔔 ALARM TRIGGERED!")
        
        val taskId = intent.getStringExtra("TASK_ID") ?: run {
            Log.e("ReminderReceiver", "❌ No TASK_ID found")
            return
        }
        val title = intent.getStringExtra("TITLE") ?: "Scheduled Task"
        val startTime = intent.getStringExtra("START_TIME") ?: ""
        val endTime = intent.getStringExtra("END_TIME") ?: ""
        val priority = intent.getStringExtra("PRIORITY") ?: "medium"
        val categoryLabel = intent.getStringExtra("CATEGORY_LABEL") ?: ""
        val color = intent.getStringExtra("COLOR") ?: "#2196F3"
        val today = LocalDate.now().toString()

        Log.d("ReminderReceiver", "Task ID: $taskId")
        Log.d("ReminderReceiver", "Title: $title")
        Log.d("ReminderReceiver", "Time: $startTime - $endTime")
        Log.d("ReminderReceiver", "Priority: $priority")
        
        // Check if task has already been notified today (prevents duplicates)
        if (NotificationPreferences.isTaskNotified(context, taskId, today)) {
            Log.d("ReminderReceiver", "⏭️ Task already notified today, skipping")
            Log.d("ReminderReceiver", "========================================")
            return
        }

        Log.d("ReminderReceiver", "Showing notification...")
        
        TimelineNotificationHelper.showTaskNotification(
            context = context,
            taskId = taskId,
            title = title,
            startTime = startTime,
            endTime = endTime,
            priority = priority,
            categoryLabel = categoryLabel,
            color = color
        )
        
        // Mark task as notified for today
        NotificationPreferences.markTaskNotified(context, taskId, today)
        
        Log.d("ReminderReceiver", "✅ Notification shown and marked as notified")
        Log.d("ReminderReceiver", "========================================")
    }
}

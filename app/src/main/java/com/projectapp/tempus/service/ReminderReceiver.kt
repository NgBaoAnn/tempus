package com.projectapp.tempus.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.projectapp.tempus.util.TimelineNotificationHelper

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

        Log.d("ReminderReceiver", "Task ID: $taskId")
        Log.d("ReminderReceiver", "Title: $title")
        Log.d("ReminderReceiver", "Time: $startTime - $endTime")
        Log.d("ReminderReceiver", "Showing notification...")
        
        TimelineNotificationHelper.showTaskNotification(
            context,
            taskId,
            title,
            startTime,
            endTime
        )
        
        Log.d("ReminderReceiver", "✅ Notification shown")
        Log.d("ReminderReceiver", "========================================")
    }
}

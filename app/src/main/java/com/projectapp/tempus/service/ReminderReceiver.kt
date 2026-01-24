package com.projectapp.tempus.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.projectapp.tempus.util.TimelineNotificationHelper

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("TASK_ID") ?: return
        val title = intent.getStringExtra("TITLE") ?: "Scheduled Task"
        val startTime = intent.getStringExtra("START_TIME") ?: ""

        Log.d("ReminderReceiver", "Showing notification for task: $title")
        
        TimelineNotificationHelper.showTaskNotification(
            context,
            taskId,
            title,
            startTime
        )
    }
}

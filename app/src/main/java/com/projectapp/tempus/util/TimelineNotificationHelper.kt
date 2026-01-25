package com.projectapp.tempus.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.R

object TimelineNotificationHelper {

    const val CHANNEL_ID = "timeline_channel"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Schedule Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled tasks"
                enableVibration(true)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showTaskNotification(
        context: Context,
        taskId: String,
        title: String,
        startTime: String,
        endTime: String
    ) {
        createNotificationChannel(context)

        // Intent to open MainActivity and navigate to Timeline
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "timeline")
            putExtra("TASK_ID", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeRange = if (endTime.isNotEmpty()) "Từ $startTime đến $endTime" else "Bắt đầu lúc $startTime"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer) // Reuse timer icon or app icon
            .setContentTitle(title)
            .setContentText(timeRange)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFF34C759.toInt()) // Standard Green

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(taskId.hashCode(), builder.build())
    }
}

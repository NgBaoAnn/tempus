package com.projectapp.tempus.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.R
import com.projectapp.tempus.receiver.TimerActionReceiver

object TimerNotificationHelper {

    const val CHANNEL_ID = "timer_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Timer countdown notifications"
                setSound(null, null)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showTimerNotification(
        context: Context,
        timeRemaining: String,
        isPaused: Boolean
    ) {
        NotificationPreferences.init(context)
        if (!NotificationPreferences.isTimerEnabled()) return

        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "timer")
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(timeRemaining) 
            .setContentText("Focus Timer")  
            .setSmallIcon(R.drawable.ic_timer)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(0xFF34C759.toInt()) 
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1) 
            )

        
        if (isPaused) {
            builder.addAction(
                R.drawable.ic_notification_play, 
                "Resume",
                createActionPendingIntent(context, TimerActionReceiver.ACTION_RESUME)
            )
        } else {
            builder.addAction(
                R.drawable.ic_notification_pause, 
                "Pause",
                createActionPendingIntent(context, TimerActionReceiver.ACTION_PAUSE)
            )
        }

        builder.addAction(
            R.drawable.ic_notification_stop, 
            "Stop",
            createActionPendingIntent(context, TimerActionReceiver.ACTION_STOP)
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createActionPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, TimerActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
}

package com.projectapp.tempus.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.R
import com.projectapp.tempus.receiver.NotificationSnoozeReceiver

object TimelineNotificationHelper {

    const val CHANNEL_ID = "timeline_channel"
    
    // Priority colors
    private const val COLOR_HIGH = 0xFFE53935.toInt()    // Red
    private const val COLOR_MEDIUM = 0xFFFFA726.toInt()  // Orange  
    private const val COLOR_LOW = 0xFF43A047.toInt()     // Green
    private const val COLOR_DEFAULT = 0xFF2196F3.toInt() // Blue
    
    // Category label icons mapping
    private val CATEGORY_ICONS = mapOf(
        "wakeup" to R.drawable.ic_timer,
        "eat" to R.drawable.ic_timer,
        "exercise" to R.drawable.ic_timer,
        "rest" to R.drawable.ic_timer,
        "work" to R.drawable.ic_timer,
        "study" to R.drawable.ic_note,
        "meeting" to R.drawable.ic_social,
        "focus" to R.drawable.ic_focus_mode
    )
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Schedule Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled tasks"
                enableVibration(true)
                enableLights(true)
                lightColor = Color.BLUE
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Show enhanced task notification with:
     * - Priority-based colors
     * - Snooze action button
     * - Expanded BigText style
     * - Category-based icon
     */
    fun showTaskNotification(
        context: Context,
        taskId: String,
        title: String,
        startTime: String,
        endTime: String,
        priority: String = "medium",
        categoryLabel: String = "",
        color: String = "#2196F3"
    ) {
        createNotificationChannel(context)

        // Intent to open MainActivity and navigate to Timeline
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "timeline")
            putExtra("TASK_ID", taskId)
        }
        
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action intent
        val snoozeIntent = Intent(context, NotificationSnoozeReceiver::class.java).apply {
            action = NotificationSnoozeReceiver.ACTION_SNOOZE
            putExtra("TASK_ID", taskId)
            putExtra("TITLE", title)
            putExtra("START_TIME", startTime)
            putExtra("END_TIME", endTime)
            putExtra("PRIORITY", priority)
            putExtra("CATEGORY_LABEL", categoryLabel)
            putExtra("COLOR", color)
        }
        
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 1000, // Different request code
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Determine notification color based on priority
        val notificationColor = getPriorityColor(priority, color)
        
        // Choose icon based on category
        val iconRes = getCategoryIcon(categoryLabel)
        
        // Build time range text
        val timeRange = if (endTime.isNotEmpty()) "Từ $startTime đến $endTime" else "Bắt đầu lúc $startTime"
        
        // Build priority label
        val priorityEmoji = when (priority.lowercase()) {
            "high" -> "🔴"
            "medium" -> "🟠"
            "low" -> "🟢"
            else -> "🔵"
        }
        
        // Build expanded content
        val expandedText = buildString {
            append("$priorityEmoji Độ ưu tiên: ${getPriorityText(priority)}\n")
            append("⏰ $timeRange")
            if (categoryLabel.isNotEmpty()) {
                append("\n📁 Danh mục: ${getCategoryDisplayName(categoryLabel)}")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(timeRange)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(expandedText)
                .setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setColor(notificationColor)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(
                R.drawable.ic_timer,
                "⏰ Nhắc sau 5 phút",
                snoozePendingIntent
            )

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(taskId.hashCode(), builder.build())
    }
    
    private fun getPriorityColor(priority: String, fallbackColor: String): Int {
        return when (priority.lowercase()) {
            "high" -> COLOR_HIGH
            "medium" -> COLOR_MEDIUM
            "low" -> COLOR_LOW
            else -> try {
                Color.parseColor(fallbackColor)
            } catch (e: Exception) {
                COLOR_DEFAULT
            }
        }
    }
    
    private fun getPriorityText(priority: String): String {
        return when (priority.lowercase()) {
            "high" -> "Cao"
            "medium" -> "Trung bình"
            "low" -> "Thấp"
            else -> "Bình thường"
        }
    }
    
    private fun getCategoryIcon(label: String): Int {
        return CATEGORY_ICONS[label.lowercase()] ?: R.drawable.ic_timer
    }
    
    private fun getCategoryDisplayName(label: String): String {
        return when (label.lowercase()) {
            "wakeup" -> "Thức dậy"
            "eat" -> "Ăn uống"
            "exercise" -> "Tập thể dục"
            "rest" -> "Nghỉ ngơi"
            "work" -> "Công việc"
            "study" -> "Học tập"
            "meeting" -> "Họp"
            "focus" -> "Tập trung"
            else -> label.replaceFirstChar { it.uppercase() }
        }
    }
}

package com.projectapp.tempus.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.projectapp.tempus.data.schedule.dto.ScheduleItemRow
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(taskId: String, title: String, startDateTime: String, endDateTime: String) {
        Log.d("ReminderScheduler", "=== SCHEDULING REMINDER ===")
        Log.d("ReminderScheduler", "Task ID: $taskId")
        Log.d("ReminderScheduler", "Title: $title")
        Log.d("ReminderScheduler", "Start: $startDateTime")
        Log.d("ReminderScheduler", "End: $endDateTime")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canSchedule = alarmManager.canScheduleExactAlarms()
            Log.d("ReminderScheduler", "Can schedule exact alarms: $canSchedule")
            if (!canSchedule) {
                Log.w("ReminderScheduler", "⚠️ Permission not granted, alarm will not be scheduled")
                return
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TASK_ID", taskId)
            putExtra("TITLE", title)
            putExtra("START_TIME", formatTime(startDateTime))
            putExtra("END_TIME", formatTime(endDateTime))
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val triggerTime = parseDateTime(startDateTime)
            val currentTime = System.currentTimeMillis()
            val delayMinutes = (triggerTime - currentTime) / 60000
            
            Log.d("ReminderScheduler", "Current time: $currentTime")
            Log.d("ReminderScheduler", "Trigger time: $triggerTime")
            Log.d("ReminderScheduler", "Delay: $delayMinutes minutes")
            
            if (triggerTime > currentTime) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d("ReminderScheduler", "✅ Alarm scheduled successfully!")
            } else {
                Log.w("ReminderScheduler", "⚠️ Time is in the past, not scheduling")
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "❌ Failed to schedule alarm", e)
        }
    }

    fun cancelReminder(taskId: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("ReminderScheduler", "Cancelled alarm for task $taskId")
    }

    private fun parseDateTime(dateTimeStr: String): Long {
        return try {
            // Use OffsetDateTime to properly handle timezone information
            val offsetDateTime = java.time.OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            offsetDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to parse datetime: $dateTimeStr", e)
            System.currentTimeMillis()
        }
    }
    
    private fun formatTime(dateTimeStr: String): String {
        return try {
            // Use OffsetDateTime and convert to system timezone for display
            val offsetDateTime = java.time.OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            val localDateTime = offsetDateTime.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime()
            localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            dateTimeStr
        }
    }
}

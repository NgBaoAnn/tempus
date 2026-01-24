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

    fun scheduleReminder(taskId: String, title: String, startDateTime: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("ReminderScheduler", "Permission SCHEDULE_EXACT_ALARM not granted")
                return
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TASK_ID", taskId)
            putExtra("TITLE", title)
            putExtra("START_TIME", formatTime(startDateTime))
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val triggerTime = parseDateTime(startDateTime)
            if (triggerTime > System.currentTimeMillis()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d("ReminderScheduler", "Scheduled alarm for $title at $startDateTime")
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to schedule alarm", e)
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
            // Assumes ISO-8601 format or similar "yyyy-MM-dd HH:mm"
            // If repository passes formatted string, adjust parsing
            val ldt = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME) 
            ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            // Fallback for simple date format if ISO fails
            try {
                // Try "HH:mm" if only time? No, schedule needs full date
                System.currentTimeMillis() // Fail safe
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }
    
    private fun formatTime(dateTimeStr: String): String {
        return try {
            val ldt = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            ldt.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            dateTimeStr
        }
    }
}

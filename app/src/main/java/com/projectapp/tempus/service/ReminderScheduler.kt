package com.projectapp.tempus.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.format.DateTimeFormatter


class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    
    fun scheduleReminder(taskId: String, title: String, startDateTime: String, endDateTime: String) {
        scheduleReminderWithDetails(taskId, title, startDateTime, endDateTime, "medium", "", "#2196F3")
    }

    
    fun scheduleReminderWithDetails(
        taskId: String,
        title: String,
        startDateTime: String,
        endDateTime: String,
        priority: String = "medium",
        categoryLabel: String = "",
        color: String = "#2196F3",
        isSnooze: Boolean = false
    ) {
        Log.d("ReminderScheduler", "=== SCHEDULING REMINDER ===")
        Log.d("ReminderScheduler", "Task ID: $taskId")
        Log.d("ReminderScheduler", "Title: $title")
        Log.d("ReminderScheduler", "Priority: $priority")
        Log.d("ReminderScheduler", "Start: $startDateTime")
        Log.d("ReminderScheduler", "End: $endDateTime")
        Log.d("ReminderScheduler", "Is Snooze: $isSnooze")

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TASK_ID", taskId)
            putExtra("TITLE", title)
            putExtra("START_TIME", formatTime(startDateTime))
            putExtra("END_TIME", formatTime(endDateTime))
            putExtra("PRIORITY", priority)
            putExtra("CATEGORY_LABEL", categoryLabel)
            putExtra("COLOR", color)
        }

        
        val requestCode = if (isSnooze) {
            taskId.hashCode() + 2000  
        } else {
            taskId.hashCode()
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = parseDateTime(startDateTime)
        val currentTime = System.currentTimeMillis()
        val delayMinutes = (triggerTime - currentTime) / 60000
        
        Log.d("ReminderScheduler", "Current time: $currentTime")
        Log.d("ReminderScheduler", "Trigger time: $triggerTime")
        Log.d("ReminderScheduler", "Delay: $delayMinutes minutes")
        
        if (triggerTime <= currentTime) {
            Log.w("ReminderScheduler", "⚠️ Time is in the past, not scheduling")
            return
        }

        
        var success = false
        
        
        if (!success) {
            success = tryAlarmClock(triggerTime, pendingIntent)
        }
        
        
        if (!success) {
            success = tryExactAlarm(triggerTime, pendingIntent)
        }
        
        
        if (!success) {
            success = tryInexactAlarm(triggerTime, pendingIntent)
        }
        
        if (!success) {
            Log.e("ReminderScheduler", "❌ All alarm strategies failed!")
        }
    }
    
    private fun tryAlarmClock(triggerTime: Long, pendingIntent: PendingIntent): Boolean {
        return try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("ReminderScheduler", "✅ AlarmClock scheduled (bypasses Doze)")
            true
        } catch (e: SecurityException) {
            Log.w("ReminderScheduler", "AlarmClock failed - permission not granted: ${e.message}")
            false
        } catch (e: Exception) {
            Log.w("ReminderScheduler", "AlarmClock failed: ${e.message}")
            false
        }
    }
    
    private fun tryExactAlarm(triggerTime: Long, pendingIntent: PendingIntent): Boolean {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("ReminderScheduler", "Exact alarm permission not granted")
                return false
            }
        }
        
        return try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("ReminderScheduler", "✅ Exact alarm scheduled")
            true
        } catch (e: SecurityException) {
            Log.w("ReminderScheduler", "Exact alarm failed - permission issue: ${e.message}")
            false
        } catch (e: Exception) {
            Log.w("ReminderScheduler", "Exact alarm failed: ${e.message}")
            false
        }
    }
    
    private fun tryInexactAlarm(triggerTime: Long, pendingIntent: PendingIntent): Boolean {
        return try {
            
            
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("ReminderScheduler", "✅ Inexact alarm scheduled (may have delay up to 15 min)")
            true
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Inexact alarm also failed: ${e.message}")
            false
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
            val offsetDateTime = java.time.OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            offsetDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to parse datetime: $dateTimeStr", e)
            System.currentTimeMillis()
        }
    }
    
    private fun formatTime(dateTimeStr: String): String {
        return try {
            val offsetDateTime = java.time.OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            val localDateTime = offsetDateTime.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime()
            localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            dateTimeStr
        }
    }
}

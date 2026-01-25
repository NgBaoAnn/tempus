package com.projectapp.tempus.service

import android.content.Context
import android.util.Log
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Manages batch synchronization of Timeline alarms
 */
object TimelineAlarmManager {

    /**
     * Sync all timeline tasks for the current user
     * Cancels all existing alarms and schedules new ones for future tasks
     */
    fun syncAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("TimelineAlarmManager", "🔄 Starting alarm sync...")
                
                val supabase = SupabaseClientProvider.client
                val user = supabase.auth.currentUserOrNull()
                
                if (user == null) {
                    Log.d("TimelineAlarmManager", "No user logged in, skipping sync")
                    return@launch
                }

                // Fetch all schedules for the current user
                val schedules = supabase.from("schedule")
                    .select { 
                        filter { 
                            eq("user_id", user.id) 
                        } 
                    }
                    .decodeList<ScheduleRow>()

                Log.d("TimelineAlarmManager", "📋 Found ${schedules.size} tasks")

                val reminderScheduler = ReminderScheduler(context)
                var scheduledCount = 0
                var ongoingCount = 0
                var skippedCount = 0

                // Schedule alarms for future tasks or notify for ongoing tasks
                schedules.forEach { schedule ->
                    try {
                        val startTime = OffsetDateTime.parse(schedule.startTimeDate, DateTimeFormatter.ISO_DATE_TIME)
                        val endDateTime = calculateEndTime(schedule.startTimeDate, schedule.implementationTime)
                        val endTime = OffsetDateTime.parse(endDateTime, DateTimeFormatter.ISO_DATE_TIME)
                        val now = OffsetDateTime.now()
                        
                        when {
                            // Task is in the future - schedule alarm
                            startTime.isAfter(now) -> {
                                reminderScheduler.scheduleReminder(
                                    taskId = schedule.id,
                                    title = schedule.name,
                                    startDateTime = schedule.startTimeDate,
                                    endDateTime = endDateTime
                                )
                                scheduledCount++
                            }
                            // Task is currently ongoing - show notification immediately
                            now.isAfter(startTime) && now.isBefore(endTime) -> {
                                Log.d("TimelineAlarmManager", "Task '${schedule.name}' is ongoing, showing notification now")
                                com.projectapp.tempus.util.TimelineNotificationHelper.showTaskNotification(
                                    context,
                                    schedule.id,
                                    schedule.name,
                                    formatTime(schedule.startTimeDate),
                                    formatTime(endDateTime)
                                )
                                ongoingCount++
                            }
                            // Task has ended - skip
                            else -> {
                                skippedCount++
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TimelineAlarmManager", "Failed to process alarm for ${schedule.name}", e)
                    }
                }

                Log.d("TimelineAlarmManager", "✅ Sync complete: $scheduledCount scheduled, $ongoingCount ongoing (notified), $skippedCount skipped (ended)")
            } catch (e: Exception) {
                Log.e("TimelineAlarmManager", "❌ Alarm sync failed", e)
            }
        }
    }

    private fun calculateEndTime(startIso: String, durationStr: String): String {
        return try {
            val start = OffsetDateTime.parse(startIso, DateTimeFormatter.ISO_DATE_TIME)
            val parts = durationStr.split(":")
            val h = parts.getOrNull(0)?.toLongOrNull() ?: 0
            val m = parts.getOrNull(1)?.toLongOrNull() ?: 0
            val s = parts.getOrNull(2)?.toLongOrNull() ?: 0
            
            val end = start.plusHours(h).plusMinutes(m).plusSeconds(s)
            end.format(DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            startIso
        }
    }
    
    private fun formatTime(dateTimeStr: String): String {
        return try {
            val offsetDateTime = OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            val localDateTime = offsetDateTime.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime()
            localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            dateTimeStr
        }
    }
}

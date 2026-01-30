package com.projectapp.tempus.service

import android.content.Context
import android.util.Log
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.local.TempusDatabase
import com.projectapp.tempus.util.NotificationPreferences
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


object TimelineAlarmManager {

    
    fun syncAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("TimelineAlarmManager", "🔄 Starting alarm sync (LOCAL data)...")
                
                
                NotificationPreferences.clearOldNotifications(context)
                
                
                val supabase = SupabaseClientProvider.client
                val userId = supabase.auth.currentUserOrNull()?.id
                
                if (userId.isNullOrEmpty()) {
                    Log.d("TimelineAlarmManager", "No user logged in, skipping sync")
                    return@launch
                }

                
                val database = TempusDatabase.getDatabase(context)
                val schedules = database.scheduleDao().getSchedulesForAlarm(userId)

                Log.d("TimelineAlarmManager", "📋 Found ${schedules.size} tasks from local DB")

                val reminderScheduler = ReminderScheduler(context)
                var scheduledCount = 0
                var skippedCount = 0
                val today = LocalDate.now()

                
                schedules.forEach { schedule ->
                    try {
                        val startTime = OffsetDateTime.parse(schedule.startTimeDate, DateTimeFormatter.ISO_DATE_TIME)
                        val now = OffsetDateTime.now()
                        
                        
                        if (startTime.isAfter(now)) {
                            val endDateTime = calculateEndTime(schedule.startTimeDate, schedule.implementationTime)
                            
                            
                            reminderScheduler.scheduleReminderWithDetails(
                                taskId = schedule.id,
                                title = schedule.name,
                                startDateTime = schedule.startTimeDate,
                                endDateTime = endDateTime,
                                priority = schedule.priority,
                                categoryLabel = schedule.label ?: "",
                                color = schedule.color ?: "#2196F3"
                            )
                            scheduledCount++
                        } else {
                            
                            skippedCount++
                        }
                    } catch (e: Exception) {
                        Log.e("TimelineAlarmManager", "Failed to process alarm for ${schedule.name}", e)
                    }
                }

                Log.d("TimelineAlarmManager", "✅ Sync complete: $scheduledCount scheduled, $skippedCount skipped")
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
}

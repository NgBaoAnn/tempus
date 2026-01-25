package com.projectapp.tempus.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Returning

class SyncScheduleWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val supabase = SupabaseClientProvider.client
    private val reminderScheduler = ReminderScheduler(context)

    override suspend fun doWork(): Result {
        Log.d("SyncScheduleWorker", "Starting schedule sync...")
        return try {
            val user = supabase.auth.currentUserOrNull()
            if (user == null) {
                Log.d("SyncScheduleWorker", "No user logged in, skipping sync")
                return Result.success()
            }

            // Fetch schedules for the user
            val schedules = supabase.from("schedule")
                .select { 
                    filter { 
                        eq("user_id", user.id) 
                    } 
                }
                .decodeList<ScheduleRow>()

            Log.d("SyncScheduleWorker", "Fetched ${schedules.size} schedules")

            // Schedule alarms for each item
            schedules.forEach { schedule ->
                val endDateTime = calculateEndTime(schedule.startTimeDate, schedule.implementationTime)
                reminderScheduler.scheduleReminder(
                    taskId = schedule.id,
                    title = schedule.name,
                    startDateTime = schedule.startTimeDate,
                    endDateTime = endDateTime
                )
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncScheduleWorker", "Error syncing schedules", e)
            Result.retry()
        }
    }
    
    private fun calculateEndTime(startIso: String, durationStr: String): String {
        return try {
            val start = java.time.LocalDateTime.parse(startIso, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            // durationStr format "HH:mm:ss"
            val parts = durationStr.split(":")
            val h = parts.getOrNull(0)?.toLongOrNull() ?: 0
            val m = parts.getOrNull(1)?.toLongOrNull() ?: 0
            val s = parts.getOrNull(2)?.toLongOrNull() ?: 0
            
            val end = start.plusHours(h).plusMinutes(m).plusSeconds(s)
            end.format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            startIso
        }
    }
}

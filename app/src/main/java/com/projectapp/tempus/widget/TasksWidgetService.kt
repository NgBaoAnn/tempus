package com.projectapp.tempus.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.projectapp.tempus.R
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * RemoteViewsService cung cấp data cho widget ListView
 */
class TasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TasksRemoteViewsFactory(applicationContext)
    }
}

class TasksRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private val tasks = mutableListOf<ScheduleRow>()
    private val supabase = SupabaseClientProvider.client

    override fun onCreate() {
        Log.d("TasksWidget", "RemoteViewsFactory onCreate")
        try {
            // Initialize
        } catch (e: Exception) {
            Log.e("TasksWidget", "Error in onCreate", e)
        }
    }

    override fun onDataSetChanged() {
        Log.d("TasksWidget", "onDataSetChanged called")
        try {
            // Load data synchronously (called on binder thread)
            loadTasks()
        } catch (e: Exception) {
            Log.e("TasksWidget", "Error in onDataSetChanged", e)
            tasks.clear()
        }
    }

    private fun loadTasks() {
        try {
            Log.d("TasksWidget", "=== Loading tasks for widget ===")
            
            runBlocking {
                // CRITICAL: Load session from storage first
                try {
                    Log.d("TasksWidget", "Attempting to restore session from storage...")
                    supabase.auth.loadFromStorage()
                    Log.d("TasksWidget", "Session loaded from storage")
                } catch (e: Exception) {
                    Log.w("TasksWidget", "Could not load session from storage", e)
                }
                
                val currentSession = supabase.auth.currentSessionOrNull()
                val userId = currentSession?.user?.id
                
                Log.d("TasksWidget", "Current session: ${if (currentSession != null) "exists" else "null"}")
                Log.d("TasksWidget", "User ID: $userId")
                
                if (userId == null) {
                    Log.w("TasksWidget", "No user logged in, cannot load tasks")
                    tasks.clear()
                    return@runBlocking
                }

                val today = LocalDate.now()
                Log.d("TasksWidget", "Querying tasks for date: $today")
                
                // Fetch all user's tasks (since we don't have a 'date' field to filter on)
                val allSchedules = supabase.from("schedule")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<ScheduleRow>()

                Log.d("TasksWidget", "Query returned ${allSchedules.size} total tasks")

                // Filter for today's tasks by parsing start_time_date
                val todaySchedules = allSchedules.filter { schedule ->
                    try {
                        val startTime = OffsetDateTime.parse(schedule.startTimeDate, DateTimeFormatter.ISO_DATE_TIME)
                        val scheduleDate = startTime.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDate()
                        scheduleDate == today
                    } catch (e: Exception) {
                        Log.e("TasksWidget", "Failed to parse date for task ${schedule.name}: ${schedule.startTimeDate}", e)
                        false
                    }
                }

                Log.d("TasksWidget", "Found ${todaySchedules.size} tasks for today")

                // Sort by start time and take max 5
                tasks.clear()
                tasks.addAll(
                    todaySchedules
                        .sortedBy { it.startTimeDate }
                        .take(5)
                )

                Log.d("TasksWidget", "Widget will display ${tasks.size} tasks")
                tasks.forEachIndexed { index, task ->
                    Log.d("TasksWidget", "Task $index: ${task.name} at ${task.startTimeDate}")
                }
            }
        } catch (e: Exception) {
            Log.e("TasksWidget", "Failed to load tasks", e)
            tasks.clear()
        }
    }

    override fun onDestroy() {
        tasks.clear()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        try {
            if (position >= tasks.size) {
                return RemoteViews(context.packageName, R.layout.widget_task_item)
            }

            val task = tasks[position]
            val views = RemoteViews(context.packageName, R.layout.widget_task_item)

            // Set task name
            views.setTextViewText(R.id.widget_task_name, task.name)

            // Format time range
            try {
                val startTime = OffsetDateTime.parse(task.startTimeDate, DateTimeFormatter.ISO_DATE_TIME)
                val localStart = startTime.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalTime()
                
                // Calculate end time
                val duration = task.implementationTime.split(":")
                val endTime = localStart
                    .plusHours(duration.getOrNull(0)?.toLongOrNull() ?: 0)
                    .plusMinutes(duration.getOrNull(1)?.toLongOrNull() ?: 0)
                
                val timeRange = "${localStart.format(DateTimeFormatter.ofPattern("HH:mm"))}-${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                views.setTextViewText(R.id.widget_task_time, timeRange)
            } catch (e: Exception) {
                Log.e("TasksWidget", "Error formatting time", e)
                views.setTextViewText(R.id.widget_task_time, "--:--")
            }

            // Set priority color as status indicator
            val priorityColor = when (task.priority) {
                PriorityType.high -> android.graphics.Color.parseColor("#F44336") // Red
                PriorityType.medium -> android.graphics.Color.parseColor("#FFC107") // Amber
                PriorityType.low -> android.graphics.Color.parseColor("#4CAF50") // Green
                else -> android.graphics.Color.parseColor("#9E9E9E") // Gray
            }
            // Use setBackgroundColor for ImageView (works reliably on most launchers)
            views.setInt(R.id.widget_task_status, "setBackgroundColor", priorityColor)

            // Setup click intent with task ID
            val fillIntent = Intent().apply {
                putExtra("TASK_ID", task.id)
            }
            views.setOnClickFillInIntent(R.id.widget_task_item_container, fillIntent)

            return views
        } catch (e: Exception) {
            Log.e("TasksWidget", "Error in getViewAt position $position", e)
            return RemoteViews(context.packageName, R.layout.widget_task_item)
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}

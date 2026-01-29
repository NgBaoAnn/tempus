package com.projectapp.tempus.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.projectapp.tempus.R
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.RepositoryProvider
import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.domain.model.TimelineBlock
import com.projectapp.tempus.domain.usecase.BuildTimelineUseCase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * RemoteViewsService cung cấp data cho widget ListView
 * Sử dụng ScheduleRepository (giống Timeline) để lấy dữ liệu
 */
class TasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TasksRemoteViewsFactory(applicationContext)
    }
}

class TasksRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private val tasks = mutableListOf<TimelineBlock>()
    private val supabase = SupabaseClientProvider.client
    private val scheduleRepository by lazy { RepositoryProvider.getScheduleRepository(context) }
    private val builder = BuildTimelineUseCase()

    override fun onCreate() {
        Log.d("TasksWidget", "RemoteViewsFactory onCreate")
    }

    override fun onDataSetChanged() {
        Log.d("TasksWidget", "onDataSetChanged called")
        try {
            loadTasks()
        } catch (e: Exception) {
            Log.e("TasksWidget", "Error in onDataSetChanged", e)
            tasks.clear()
        }
    }

    private fun loadTasks() {
        try {
            Log.d("TasksWidget", "=== Loading tasks via ScheduleRepository ===")
            
            runBlocking {
                // Get user ID from Supabase session
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
                val dateStr = today.toString()
                Log.d("TasksWidget", "Loading tasks for date: $dateStr")

                // Use ScheduleRepository (same as Timeline)
                val schedules = scheduleRepository.getAllSchedules(userId)
                Log.d("TasksWidget", "Got ${schedules.size} schedules from repository")
                
                if (schedules.isEmpty()) {
                    Log.d("TasksWidget", "No schedules found")
                    tasks.clear()
                    return@runBlocking
                }
                
                val taskIds = schedules.map { it.id }
                
                // Get schedule items for today
                val scheduleItems = scheduleRepository.getScheduleItemsByDate(dateStr, taskIds)
                Log.d("TasksWidget", "Got ${scheduleItems.size} schedule items for today")
                
                // Get edited versions if any
                val editedIds = scheduleItems.mapNotNull { it.editedVersion }.distinct()
                val editedMap = scheduleRepository.getEditedVersions(editedIds).associateBy { it.id }
                
                // Get subtasks
                val allSubtasks = scheduleRepository.getSubTasksBatch(taskIds)
                val subtasksMap = allSubtasks.groupBy { it.scheduleId }
                
                // Build timeline blocks (same logic as Timeline)
                val blocks = builder.build(today, schedules, scheduleItems, editedMap, subtasksMap)
                Log.d("TasksWidget", "Built ${blocks.size} timeline blocks for today")
                
                // Filter out completed tasks and take max 5 sorted by start time
                tasks.clear()
                tasks.addAll(
                    blocks
                        .filter { it.status != StatusType.done } // Không hiển thị task đã hoàn thành
                        .sortedBy { it.startTime }
                        .take(5)
                )
                
                Log.d("TasksWidget", "Widget will display ${tasks.size} tasks")
                tasks.forEachIndexed { index, task ->
                    Log.d("TasksWidget", "Task $index: ${task.title} at ${task.startTime}")
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

    override fun getCount(): Int {
        Log.d("TasksWidget", "getCount called, returning ${tasks.size}")
        return tasks.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        Log.d("TasksWidget", "getViewAt called for position $position, tasks.size=${tasks.size}")
        try {
            if (position >= tasks.size) {
                Log.w("TasksWidget", "Position $position >= tasks.size ${tasks.size}, returning empty view")
                return RemoteViews(context.packageName, R.layout.widget_task_item)
            }

            val task = tasks[position]
            Log.d("TasksWidget", "Rendering task: ${task.title}")
            val views = RemoteViews(context.packageName, R.layout.widget_task_item)

            // Set task icon based on label and tint with task color
            val iconRes = getLabelIconResource(task.labelEnum)
            views.setImageViewResource(R.id.widget_task_icon, iconRes)
            try {
                val iconColor = android.graphics.Color.parseColor(task.color)
                views.setInt(R.id.widget_task_icon, "setColorFilter", iconColor)
            } catch (e: Exception) {
                Log.w("TasksWidget", "Invalid color: ${task.color}, using default")
            }

            // Set priority color for circle dot (use setColorFilter for ImageView)
            val priorityColor = when (task.priority) {
                PriorityType.high -> android.graphics.Color.parseColor("#F44336") // Red
                PriorityType.medium -> android.graphics.Color.parseColor("#FFC107") // Amber
                PriorityType.low -> android.graphics.Color.parseColor("#4CAF50") // Green
                else -> android.graphics.Color.parseColor("#9E9E9E") // Gray
            }
            views.setInt(R.id.widget_task_priority_dot, "setColorFilter", priorityColor)

            // Set task name
            views.setTextViewText(R.id.widget_task_name, task.title)

            // Format time range
            try {
                val startTime = task.startTime
                val endTime = startTime.plus(task.duration)
                val timeRange = "${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}-${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                views.setTextViewText(R.id.widget_task_time, timeRange)
            } catch (e: Exception) {
                Log.e("TasksWidget", "Error formatting time", e)
                views.setTextViewText(R.id.widget_task_time, "--:--")
            }

            // Setup container click - navigate to timeline
            val containerFillIntent = Intent().apply {
                action = TasksWidgetProvider.ACTION_TASK_CLICK
            }
            views.setOnClickFillInIntent(R.id.widget_task_item_container, containerFillIntent)

            // Setup complete button click - mark task as done
            val completeFillIntent = Intent().apply {
                action = TasksWidgetProvider.ACTION_COMPLETE_TASK
                putExtra(TasksWidgetProvider.EXTRA_TASK_ID, task.taskId)
                putExtra(TasksWidgetProvider.EXTRA_DATE, LocalDate.now().toString())
            }
            views.setOnClickFillInIntent(R.id.widget_task_complete_btn, completeFillIntent)
            
            // Check if task is pending completion (show check icon with background)
            val isPendingComplete = TasksWidgetProvider.pendingCompleteTasks.contains(task.taskId)
            
            try {
                val taskColorInt = android.graphics.Color.parseColor(task.color)
                
                if (isPendingComplete) {
                    // Show check circle icon (completed state) - use bitmap drawable directly
                    // Note: setBackgroundTintList doesn't work with RemoteViews int parameter
                    views.setImageViewResource(R.id.widget_task_complete_btn, R.drawable.ic_check_circle)
                    views.setInt(R.id.widget_task_complete_btn, "setColorFilter", taskColorInt)
                } else {
                    // Show circle outline with task color tint (incomplete state)
                    views.setImageViewResource(R.id.widget_task_complete_btn, R.drawable.shape_circle_outline_gray)
                    views.setInt(R.id.widget_task_complete_btn, "setColorFilter", taskColorInt)
                }
            } catch (e: Exception) {
                // Default gray if color is invalid
                views.setImageViewResource(R.id.widget_task_complete_btn, R.drawable.shape_circle_outline_gray)
                views.setInt(R.id.widget_task_complete_btn, "setColorFilter", android.graphics.Color.parseColor("#757575"))
            }

            return views
        } catch (e: Exception) {
            Log.e("TasksWidget", "Error in getViewAt position $position", e)
            return RemoteViews(context.packageName, R.layout.widget_task_item)
        }
    }

    /**
     * Map ScheduleLabel to drawable resource
     */
    private fun getLabelIconResource(label: ScheduleLabel): Int {
        return when (label) {
            ScheduleLabel.wakeup -> R.drawable.wakeup
            ScheduleLabel.eat -> R.drawable.eat
            ScheduleLabel.exercise -> R.drawable.exercise
            ScheduleLabel.rest -> R.drawable.rest
            ScheduleLabel.water -> R.drawable.water
            ScheduleLabel.book -> R.drawable.book
            ScheduleLabel.sleep -> R.drawable.sleep
            ScheduleLabel.clean -> R.drawable.clean
            ScheduleLabel.cook -> R.drawable.cook
            ScheduleLabel.garden -> R.drawable.ic_garden
            ScheduleLabel.UNKNOWN -> R.drawable.book
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}

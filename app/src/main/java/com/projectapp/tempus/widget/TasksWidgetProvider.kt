package com.projectapp.tempus.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.R
import com.projectapp.tempus.data.RepositoryProvider
import com.projectapp.tempus.data.schedule.dto.StatusType
import kotlinx.coroutines.runBlocking
import java.time.LocalDate


class TasksWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        android.util.Log.d("TasksWidget", "onUpdate called for ${appWidgetIds.size} widgets")
        try {
            
            appWidgetIds.forEach { widgetId ->
                updateWidget(context, appWidgetManager, widgetId)
            }
        } catch (e: Exception) {
            android.util.Log.e("TasksWidget", "Error in onUpdate", e)
        }
    }

    override fun onEnabled(context: Context) {
        
    }

    override fun onDisabled(context: Context) {
        
    }

    companion object {
        const val ACTION_TASK_CLICK = "com.projectapp.tempus.TASK_CLICK"
        private const val ACTION_ADD_TASK = "com.projectapp.tempus.ADD_TASK"
        const val ACTION_REFRESH = "com.projectapp.tempus.WIDGET_REFRESH"
        const val ACTION_COMPLETE_TASK = "com.projectapp.tempus.COMPLETE_TASK"
        const val ACTION_FINALIZE_COMPLETE = "com.projectapp.tempus.FINALIZE_COMPLETE"
        
        const val EXTRA_TASK_ID = "TASK_ID"
        const val EXTRA_DATE = "DATE"
        
        
        val pendingCompleteTasks = mutableSetOf<String>()

        
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            try {
                android.util.Log.d("TasksWidget", "Updating widget $widgetId")
                
                val views = RemoteViews(context.packageName, R.layout.widget_tasks)

                
                val today = LocalDate.now()
                val dayOfWeekText = when (today.dayOfWeek.value) {
                    1 -> "Thứ 2"
                    2 -> "Thứ 3"
                    3 -> "Thứ 4"
                    4 -> "Thứ 5"
                    5 -> "Thứ 6"
                    6 -> "Thứ 7"
                    7 -> "Chủ nhật"
                    else -> ""
                }
                val dateText = "${today.dayOfMonth}/${today.monthValue}/${today.year}"
                views.setTextViewText(R.id.widget_header_day_of_week, dayOfWeekText)
                views.setTextViewText(R.id.widget_header_date, dateText)

                
                val serviceIntent = Intent(context, TasksWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
                views.setRemoteAdapter(R.id.widget_tasks_list, serviceIntent)

                
                views.setEmptyView(R.id.widget_tasks_list, R.id.widget_empty_view)

                
                val addIntent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_ADD_TASK
                    putExtra("NAVIGATE_TO", "timeline")
                    putExtra("OPEN_ADD_TASK", true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val addPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_add_button, addPendingIntent)

                
                val openTimelineIntent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_TASK_CLICK
                    putExtra("NAVIGATE_TO", "timeline")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openTimelinePendingIntent = PendingIntent.getActivity(
                    context,
                    1, 
                    openTimelineIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                views.setOnClickPendingIntent(R.id.widget_header_title, openTimelinePendingIntent)
                
                views.setOnClickPendingIntent(R.id.widget_empty_view, openTimelinePendingIntent)

                
                val templateIntent = Intent(context, TasksWidgetProvider::class.java).apply {
                    
                }
                val templatePendingIntent = PendingIntent.getBroadcast(
                    context,
                    2,
                    templateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                views.setPendingIntentTemplate(R.id.widget_tasks_list, templatePendingIntent)

                
                appWidgetManager.updateAppWidget(widgetId, views)
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_tasks_list)
                
                android.util.Log.d("TasksWidget", "Widget $widgetId updated successfully")
            } catch (e: Exception) {
                android.util.Log.e("TasksWidget", "Failed to update widget $widgetId", e)
                
                try {
                    val errorViews = RemoteViews(context.packageName, R.layout.widget_tasks)
                    appWidgetManager.updateAppWidget(widgetId, errorViews)
                } catch (e2: Exception) {
                    android.util.Log.e("TasksWidget", "Failed to create fallback widget", e2)
                }
            }
        }

        
        fun refreshAllWidgets(context: Context) {
            val intent = Intent(context, TasksWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_REFRESH -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, TasksWidgetProvider::class.java)
                )
                onUpdate(context, appWidgetManager, widgetIds)
            }
            ACTION_TASK_CLICK -> {
                
                Log.d("TasksWidget", "Opening Timeline...")
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra("NAVIGATE_TO", "timeline")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(openIntent)
            }
            ACTION_COMPLETE_TASK -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                val date = intent.getStringExtra(EXTRA_DATE) ?: LocalDate.now().toString()
                
                Log.d("TasksWidget", "Complete task clicked: taskId=$taskId")
                
                if (taskId != null) {
                    
                    pendingCompleteTasks.add(taskId)
                    
                    
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val widgetIds = appWidgetManager.getAppWidgetIds(
                        android.content.ComponentName(context, TasksWidgetProvider::class.java)
                    )
                    widgetIds.forEach { widgetId ->
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_tasks_list)
                    }
                    
                    
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val finalizeIntent = Intent(context, TasksWidgetProvider::class.java).apply {
                            action = ACTION_FINALIZE_COMPLETE
                            putExtra(EXTRA_TASK_ID, taskId)
                            putExtra(EXTRA_DATE, date)
                        }
                        context.sendBroadcast(finalizeIntent)
                    }, 500) 
                }
            }
            ACTION_FINALIZE_COMPLETE -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                val date = intent.getStringExtra(EXTRA_DATE) ?: LocalDate.now().toString()
                
                Log.d("TasksWidget", "Finalizing complete: taskId=$taskId")
                
                if (taskId != null) {
                    
                    pendingCompleteTasks.remove(taskId)
                    
                    try {
                        runBlocking {
                            val repo = RepositoryProvider.getScheduleRepository(context)
                            repo.upsertScheduleItem(taskId, date, StatusType.done)
                            Log.d("TasksWidget", "Task $taskId marked as done in database")
                        }
                        
                        WidgetRefreshHelper.refreshTasksWidget(context)
                    } catch (e: Exception) {
                        Log.e("TasksWidget", "Failed to finalize complete task", e)
                    }
                }
            }
        }
    }
}

package com.projectapp.tempus.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.R

/**
 * Widget Provider cho Today's Tasks Widget
 */
class TasksWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        android.util.Log.d("TasksWidget", "onUpdate called for ${appWidgetIds.size} widgets")
        try {
            // Update each widget instance
            appWidgetIds.forEach { widgetId ->
                updateWidget(context, appWidgetManager, widgetId)
            }
        } catch (e: Exception) {
            android.util.Log.e("TasksWidget", "Error in onUpdate", e)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget is created
    }

    override fun onDisabled(context: Context) {
        // Last widget is removed
    }

    companion object {
        private const val ACTION_TASK_CLICK = "com.projectapp.tempus.TASK_CLICK"
        private const val ACTION_ADD_TASK = "com.projectapp.tempus.ADD_TASK"
        const val ACTION_REFRESH = "com.projectapp.tempus.WIDGET_REFRESH"
        
        private const val EXTRA_TASK_ID = "task_id"

        /**
         * Update widget với dữ liệu mới
         */
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            try {
                android.util.Log.d("TasksWidget", "Updating widget $widgetId")
                
                val views = RemoteViews(context.packageName, R.layout.widget_tasks)

                // Setup service intent for the ListView
                val serviceIntent = Intent(context, TasksWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
                views.setRemoteAdapter(R.id.widget_tasks_list, serviceIntent)

                // Setup empty view
                views.setEmptyView(R.id.widget_tasks_list, R.id.widget_empty_view)

                // Setup add task button click
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

                // Setup item click template
                val clickIntent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_TASK_CLICK
                    putExtra("NAVIGATE_TO", "timeline")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val clickPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                views.setPendingIntentTemplate(R.id.widget_tasks_list, clickPendingIntent)

                // Notify widget manager
                appWidgetManager.updateAppWidget(widgetId, views)
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_tasks_list)
                
                android.util.Log.d("TasksWidget", "Widget $widgetId updated successfully")
            } catch (e: Exception) {
                android.util.Log.e("TasksWidget", "Failed to update widget $widgetId", e)
                // Create a simple fallback widget
                try {
                    val errorViews = RemoteViews(context.packageName, R.layout.widget_tasks)
                    appWidgetManager.updateAppWidget(widgetId, errorViews)
                } catch (e2: Exception) {
                    android.util.Log.e("TasksWidget", "Failed to create fallback widget", e2)
                }
            }
        }

        /**
         * Refresh all widget instances
         */
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
        }
    }
}

package com.projectapp.tempus.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.projectapp.tempus.R


object WidgetRefreshHelper {
    private const val TAG = "WidgetRefreshHelper"
    
    
    fun refreshTasksWidget(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, TasksWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
            
            if (widgetIds.isEmpty()) {
                Log.d(TAG, "No widget instances found, skipping refresh")
                return
            }
            
            Log.d(TAG, "Refreshing ${widgetIds.size} widget(s)")
            
            
            widgetIds.forEach { widgetId ->
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_tasks_list)
            }
            
            Log.d(TAG, "Widget refresh triggered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh widget", e)
        }
    }
}

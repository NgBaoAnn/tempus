package com.projectapp.tempus.service.focus

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings


class AppUsageManager(private val context: Context) {
    
    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    
    
    fun hasUsageStatsPermission(): Boolean {
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 60,
            time
        )
        return stats != null && stats.isNotEmpty()
    }
    
    
    fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    
    
    fun getForegroundApp(): String? {
        val time = System.currentTimeMillis()
        
        
        val usageEvents = usageStatsManager.queryEvents(time - 10000, time)
        
        var lastForegroundApp: String? = null
        var lastEventTime = 0L
        
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            
            
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (event.timeStamp > lastEventTime) {
                    lastEventTime = event.timeStamp
                    lastForegroundApp = event.packageName
                }
            }
        }
        
        return lastForegroundApp
    }
    
    
    fun isAppInForeground(packageName: String): Boolean {
        return getForegroundApp() == packageName
    }
    
    
    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    
    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

package com.projectapp.tempus.service.focus

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * Manager class to detect which app is currently in foreground
 * Uses UsageStatsManager to query recent app usage
 */
class AppUsageManager(private val context: Context) {
    
    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    
    /**
     * Check if the app has permission to access usage stats
     */
    fun hasUsageStatsPermission(): Boolean {
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 60,
            time
        )
        return stats != null && stats.isNotEmpty()
    }
    
    /**
     * Open system settings to grant usage stats permission
     */
    fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    
    /**
     * Get the package name of the currently foreground app
     * Returns null if unable to detect
     */
    fun getForegroundApp(): String? {
        val time = System.currentTimeMillis()
        
        // Query usage events from the last 10 seconds
        val usageEvents = usageStatsManager.queryEvents(time - 10000, time)
        
        var lastForegroundApp: String? = null
        var lastEventTime = 0L
        
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            
            // Look for ACTIVITY_RESUMED event (app came to foreground)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (event.timeStamp > lastEventTime) {
                    lastEventTime = event.timeStamp
                    lastForegroundApp = event.packageName
                }
            }
        }
        
        return lastForegroundApp
    }
    
    /**
     * Check if the given package is currently in foreground
     */
    fun isAppInForeground(packageName: String): Boolean {
        return getForegroundApp() == packageName
    }
    
    /**
     * Check if overlay permission is granted
     */
    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    /**
     * Open system settings to grant overlay permission
     */
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

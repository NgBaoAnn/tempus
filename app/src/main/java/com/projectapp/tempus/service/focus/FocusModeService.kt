package com.projectapp.tempus.service.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.R
import com.projectapp.tempus.data.focus.FocusModeDatabase
import com.projectapp.tempus.data.focus.FocusModePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Foreground Service that monitors app usage during Focus Mode
 * Detects when blocked apps are opened and shows blocking overlay
 */
class FocusModeService : Service() {
    
    companion object {
        private const val TAG = "FocusModeService"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "focus_mode_channel"
        
        const val ACTION_START = "com.projectapp.tempus.FOCUS_START"
        const val ACTION_STOP = "com.projectapp.tempus.FOCUS_STOP"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        
        private const val POLLING_INTERVAL_MS = 1000L // Check every 1 second
        
        fun startFocusMode(context: Context, durationSeconds: Long) {
            val intent = Intent(context, FocusModeService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopFocusMode(context: Context) {
            val intent = Intent(context, FocusModeService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
    
    private lateinit var appUsageManager: AppUsageManager
    private lateinit var blockingOverlay: FocusBlockingOverlay
    private lateinit var preferences: FocusModePreferences
    private lateinit var database: FocusModeDatabase
    
    private val handler = Handler(Looper.getMainLooper())
    private var monitoringJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    private var blockedPackages: List<String> = emptyList()
    private var remainingSeconds: Long = 0L
    private var isMonitoring = false
    
    private val monitorRunnable = object : Runnable {
        override fun run() {
            if (!isMonitoring) return
            
            checkForegroundApp()
            
            // Decrement remaining time
            if (remainingSeconds > 0) {
                remainingSeconds--
                updateNotification()
            } else {
                // Time's up, stop monitoring
                stopSelf()
                return
            }
            
            handler.postDelayed(this, POLLING_INTERVAL_MS)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        appUsageManager = AppUsageManager(this)
        blockingOverlay = FocusBlockingOverlay(this)
        preferences = FocusModePreferences(this)
        database = FocusModeDatabase.getInstance(this)
        
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION_SECONDS, 0L)
                startMonitoring(duration)
            }
            ACTION_STOP -> {
                stopMonitoring()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startMonitoring(durationSeconds: Long) {
        if (isMonitoring) return
        
        remainingSeconds = durationSeconds
        isMonitoring = true
        
        // Start foreground service
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        // Load blocked apps
        serviceScope.launch {
            blockedPackages = database.blockedAppDao().getAllBlockedPackages()
            Log.d(TAG, "Focus Mode started. Blocking ${blockedPackages.size} apps")
            
            // Start polling
            handler.post(monitorRunnable)
        }
    }
    
    private fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacks(monitorRunnable)
        blockingOverlay.hide()
        
        // Save focus time to preferences
        serviceScope.launch {
            val focusedMinutes = (remainingSeconds / 60)
            preferences.addFocusTime(focusedMinutes)
        }
        
        Log.d(TAG, "Focus Mode stopped")
    }
    
    private fun checkForegroundApp() {
        val foregroundApp = appUsageManager.getForegroundApp()
        
        if (foregroundApp != null && blockedPackages.contains(foregroundApp)) {
            Log.d(TAG, "Blocked app detected: $foregroundApp")
            
            // Increment blocked attempts
            serviceScope.launch {
                preferences.incrementBlockedAttempts()
            }
            
            // Get app name
            val appName = getAppName(foregroundApp)
            val remainingTimeText = formatTime(remainingSeconds)
            
            // Show overlay
            if (appUsageManager.hasOverlayPermission()) {
                blockingOverlay.show(appName, remainingTimeText)
            }
        } else {
            // Hide overlay if visible and current app is not blocked
            if (blockingOverlay.isVisible()) {
                blockingOverlay.hide()
            }
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Mode",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Focus Mode is active - blocking distracting apps"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FocusModeService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎯 Focus Mode Active")
            .setContentText("Còn lại: ${formatTime(remainingSeconds)}")
            .setSmallIcon(R.drawable.ic_focus_mode)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Dừng", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }
}

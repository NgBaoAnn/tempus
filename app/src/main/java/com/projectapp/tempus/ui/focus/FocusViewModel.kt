package com.projectapp.tempus.ui.focus

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.focus.BlockedAppEntity
import com.projectapp.tempus.data.focus.FocusModeDatabase
import com.projectapp.tempus.data.focus.FocusModePreferences
import com.projectapp.tempus.service.focus.AppUsageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents an installed app
 */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isBlocked: Boolean = false
)

/**
 * UI State for Focus Mode settings
 */
data class FocusUiState(
    val focusModeEnabled: Boolean = false,
    val autoStartWithTimer: Boolean = true,
    val showOverlay: Boolean = true,
    val blockedApps: List<BlockedAppEntity> = emptyList(),
    val installedApps: List<InstalledApp> = emptyList(),
    val totalFocusTime: Long = 0L,
    val blockedAttempts: Int = 0,
    val hasUsagePermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val isLoadingApps: Boolean = false,
    val showAppPicker: Boolean = false
)

/**
 * ViewModel for Focus Mode settings and blocked apps management
 */
class FocusViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val preferences = FocusModePreferences(context)
    private val database = FocusModeDatabase.getInstance(context)
    private val appUsageManager = AppUsageManager(context)
    
    private val _showAppPicker = MutableStateFlow(false)
    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val _isLoadingApps = MutableStateFlow(false)
    
    val uiState: StateFlow<FocusUiState> = combine(
        preferences.focusModeEnabled,
        preferences.autoStartWithTimer,
        preferences.showOverlay,
        database.blockedAppDao().getAllBlockedApps(),
        preferences.totalFocusTime,
        preferences.blockedAttempts,
        _showAppPicker,
        _installedApps,
        _isLoadingApps
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        FocusUiState(
            focusModeEnabled = values[0] as Boolean,
            autoStartWithTimer = values[1] as Boolean,
            showOverlay = values[2] as Boolean,
            blockedApps = values[3] as List<BlockedAppEntity>,
            totalFocusTime = values[4] as Long,
            blockedAttempts = values[5] as Int,
            showAppPicker = values[6] as Boolean,
            installedApps = values[7] as List<InstalledApp>,
            isLoadingApps = values[8] as Boolean,
            hasUsagePermission = appUsageManager.hasUsageStatsPermission(),
            hasOverlayPermission = appUsageManager.hasOverlayPermission()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusUiState()
    )
    
    /**
     * Toggle Focus Mode enabled state
     */
    fun toggleFocusMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setFocusModeEnabled(enabled)
        }
    }
    
    /**
     * Toggle auto-start with timer
     */
    fun toggleAutoStart(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoStartWithTimer(enabled)
        }
    }
    
    /**
     * Toggle show overlay setting
     */
    fun toggleShowOverlay(show: Boolean) {
        viewModelScope.launch {
            preferences.setShowOverlay(show)
        }
    }
    
    /**
     * Show app picker bottom sheet
     */
    fun showAppPicker() {
        _showAppPicker.value = true
        loadInstalledApps()
    }
    
    /**
     * Hide app picker
     */
    fun hideAppPicker() {
        _showAppPicker.value = false
    }
    
    /**
     * Load installed apps list
     */
    private fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            
            val apps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val blockedPackages = database.blockedAppDao().getAllBlockedPackages().toSet()
                
                val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledApplications(PackageManager.GET_META_DATA)
                }
                
                installedApps
                    .filter { appInfo ->
                        // Filter out system apps and the Tempus app itself
                        val isLaunchable = pm.getLaunchIntentForPackage(appInfo.packageName) != null
                        val isNotSelf = appInfo.packageName != context.packageName
                        isLaunchable && isNotSelf
                    }
                    .map { appInfo ->
                        InstalledApp(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                            isBlocked = blockedPackages.contains(appInfo.packageName)
                        )
                    }
                    .sortedWith(compareBy({ !it.isBlocked }, { it.appName.lowercase() }))
            }
            
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }
    
    /**
     * Add an app to blocked list
     */
    fun blockApp(app: InstalledApp) {
        viewModelScope.launch {
            val entity = BlockedAppEntity(
                packageName = app.packageName,
                appName = app.appName
            )
            database.blockedAppDao().insertBlockedApp(entity)
            
            // Update installed apps list
            _installedApps.value = _installedApps.value.map {
                if (it.packageName == app.packageName) it.copy(isBlocked = true) else it
            }
        }
    }
    
    /**
     * Remove an app from blocked list
     */
    fun unblockApp(packageName: String) {
        viewModelScope.launch {
            database.blockedAppDao().deleteByPackageName(packageName)
            
            // Update installed apps list
            _installedApps.value = _installedApps.value.map {
                if (it.packageName == packageName) it.copy(isBlocked = false) else it
            }
        }
    }
    
    /**
     * Request usage stats permission
     */
    fun requestUsagePermission() {
        appUsageManager.requestUsageStatsPermission()
    }
    
    /**
     * Request overlay permission
     */
    fun requestOverlayPermission() {
        appUsageManager.requestOverlayPermission()
    }
    
    /**
     * Check if all required permissions are granted
     */
    fun hasAllPermissions(): Boolean {
        return appUsageManager.hasUsageStatsPermission() && appUsageManager.hasOverlayPermission()
    }
}

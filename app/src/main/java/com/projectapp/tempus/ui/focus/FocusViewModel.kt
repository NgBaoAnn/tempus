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


data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isBlocked: Boolean = false
)


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
    
    
    fun toggleFocusMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setFocusModeEnabled(enabled)
        }
    }
    
    
    fun toggleAutoStart(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoStartWithTimer(enabled)
        }
    }
    
    
    fun toggleShowOverlay(show: Boolean) {
        viewModelScope.launch {
            preferences.setShowOverlay(show)
        }
    }
    
    
    fun showAppPicker() {
        _showAppPicker.value = true
        loadInstalledApps()
    }
    
    
    fun hideAppPicker() {
        _showAppPicker.value = false
    }
    
    
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
    
    
    fun blockApp(app: InstalledApp) {
        viewModelScope.launch {
            val entity = BlockedAppEntity(
                packageName = app.packageName,
                appName = app.appName
            )
            database.blockedAppDao().insertBlockedApp(entity)
            
            
            _installedApps.value = _installedApps.value.map {
                if (it.packageName == app.packageName) it.copy(isBlocked = true) else it
            }
        }
    }
    
    
    fun unblockApp(packageName: String) {
        viewModelScope.launch {
            database.blockedAppDao().deleteByPackageName(packageName)
            
            
            _installedApps.value = _installedApps.value.map {
                if (it.packageName == packageName) it.copy(isBlocked = false) else it
            }
        }
    }
    
    
    fun requestUsagePermission() {
        appUsageManager.requestUsageStatsPermission()
    }
    
    
    fun requestOverlayPermission() {
        appUsageManager.requestOverlayPermission()
    }
    
    
    fun hasAllPermissions(): Boolean {
        return appUsageManager.hasUsageStatsPermission() && appUsageManager.hasOverlayPermission()
    }
}

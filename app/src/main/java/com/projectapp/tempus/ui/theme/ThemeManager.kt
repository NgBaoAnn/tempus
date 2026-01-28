package com.projectapp.tempus.ui.theme

import android.content.Context
import com.projectapp.tempus.data.user.SupabaseUserRepository
import com.projectapp.tempus.data.user.UserProfileCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Theme mode options
 */
enum class ThemeMode(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");
    
    companion object {
        fun fromValue(value: String): ThemeMode {
            return entries.find { it.value == value } ?: SYSTEM
        }
    }
}

/**
 * Singleton manager for app-wide theme state
 * Handles theme persistence and provides observable state for Compose
 */
object ThemeManager {
    
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private var isInitialized = false
    
    // Coroutine scope for background operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userRepository = SupabaseUserRepository()
    
    /**
     * Initialize ThemeManager with saved theme preference
     * Should be called after UserProfileCache.init()
     */
    fun init(context: Context) {
        if (isInitialized) return
        
        // Ensure UserProfileCache is initialized
        UserProfileCache.init(context)
        
        // Load saved theme from cache
        val savedTheme = UserProfileCache.getThemeMode()
        _themeMode.value = ThemeMode.fromValue(savedTheme)
        
        isInitialized = true
    }
    
    /**
     * Set theme mode, persist to cache, and sync to Supabase
     * @param mode The theme mode to set
     */
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        
        // Save locally first (instant)
        UserProfileCache.saveThemeMode(mode.value)
        
        // Sync to Supabase in background (async)
        scope.launch {
            try {
                userRepository.updateThemeColor(mode.value)
            } catch (e: Exception) {
                // Silently fail - local cache will still have the correct value
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Update theme state and local cache ONLY
     * Used when fetching fresh data from backend to avoid circular sync
     * @param mode The theme mode to set
     */
    fun updateThemeLocally(mode: ThemeMode) {
        _themeMode.value = mode
        UserProfileCache.saveThemeMode(mode.value)
    }

    /**
     * Check if dark mode should be used based on current theme setting
     * For SYSTEM mode, caller should check isSystemInDarkTheme()
     */
    fun isDarkTheme(isSystemDark: Boolean): Boolean {
        return when (_themeMode.value) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemDark
        }
    }
}

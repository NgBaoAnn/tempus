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


object ThemeManager {
    
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private var isInitialized = false
    
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userRepository = SupabaseUserRepository()
    
    
    fun init(context: Context) {
        if (isInitialized) return
        
        
        UserProfileCache.init(context)
        
        
        val savedTheme = UserProfileCache.getThemeMode()
        _themeMode.value = ThemeMode.fromValue(savedTheme)
        
        isInitialized = true
    }
    
    
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        
        
        UserProfileCache.saveThemeMode(mode.value)
        
        
        scope.launch {
            try {
                userRepository.updateThemeColor(mode.value)
            } catch (e: Exception) {
                
                e.printStackTrace()
            }
        }
    }
    
    
    fun updateThemeLocally(mode: ThemeMode) {
        _themeMode.value = mode
        UserProfileCache.saveThemeMode(mode.value)
    }

    
    fun isDarkTheme(isSystemDark: Boolean): Boolean {
        return when (_themeMode.value) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemDark
        }
    }
}

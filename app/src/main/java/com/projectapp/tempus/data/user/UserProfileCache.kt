package com.projectapp.tempus.data.user

import android.content.Context
import android.content.SharedPreferences

/**
 * Cached profile data
 */
data class CachedProfile(
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val themeMode: String? = null // "light", "dark", "system"
)

/**
 * Singleton cache for user profile data using SharedPreferences
 * Stores name, email, avatar URL, and theme preference offline
 */
object UserProfileCache {
    
    private const val PREF_NAME = "user_profile_cache"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_AVATAR_URL = "avatar_url"
    private const val KEY_THEME_MODE = "theme_mode"
    
    // Default theme mode
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    
    private var prefs: SharedPreferences? = null
    
    /**
     * Initialize the cache with application context
     * Should be called once during app initialization
     */
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Save profile data to cache
     */
    fun saveProfile(username: String, email: String, avatarUrl: String?, themeMode: String? = null) {
        prefs?.edit()?.apply {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_AVATAR_URL, avatarUrl)
            if (themeMode != null) {
                putString(KEY_THEME_MODE, themeMode)
            }
            apply()
        }
    }
    
    /**
     * Save theme mode to cache
     */
    fun saveThemeMode(themeMode: String) {
        prefs?.edit()?.putString(KEY_THEME_MODE, themeMode)?.apply()
    }
    
    /**
     * Get cached theme mode
     * Returns "system" as default if not set
     */
    fun getThemeMode(): String {
        return prefs?.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    /**
     * Get cached profile data
     * Returns null if cache is empty or not initialized
     */
    fun getProfile(): CachedProfile? {
        val prefs = prefs ?: return null
        
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, null)
        val themeMode = prefs.getString(KEY_THEME_MODE, THEME_SYSTEM)
        
        return CachedProfile(username, email, avatarUrl, themeMode)
    }
    
    /**
     * Check if cache exists and has data
     */
    fun hasCache(): Boolean {
        return prefs?.contains(KEY_USERNAME) == true
    }
    
    /**
     * Clear all cached profile data
     * Should be called on logout
     */
    fun clearCache() {
        prefs?.edit()?.clear()?.apply()
    }
}

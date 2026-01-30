package com.projectapp.tempus.data.user

import android.content.Context
import android.content.SharedPreferences


data class CachedProfile(
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val themeMode: String? = null 
)


object UserProfileCache {
    
    private const val PREF_NAME = "user_profile_cache"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_AVATAR_URL = "avatar_url"
    private const val KEY_THEME_MODE = "theme_mode"
    
    
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    
    private var prefs: SharedPreferences? = null
    
    
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }
    
    
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
    
    
    fun saveThemeMode(themeMode: String) {
        prefs?.edit()?.putString(KEY_THEME_MODE, themeMode)?.apply()
    }
    
    
    fun getThemeMode(): String {
        return prefs?.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    
    fun getProfile(): CachedProfile? {
        val prefs = prefs ?: return null
        
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, null)
        val themeMode = prefs.getString(KEY_THEME_MODE, THEME_SYSTEM)
        
        return CachedProfile(username, email, avatarUrl, themeMode)
    }
    
    
    fun hasCache(): Boolean {
        return prefs?.contains(KEY_USERNAME) == true
    }
    
    
    fun clearCache() {
        prefs?.edit()?.clear()?.apply()
    }

    
    private const val KEY_LANGUAGE_CODE = "language_code"
    const val LANG_VI = "vi"
    const val LANG_EN = "en"

    fun saveLanguage(languageCode: String) {
        prefs?.edit()?.putString(KEY_LANGUAGE_CODE, languageCode)?.apply()
    }

    fun getLanguage(): String? {
        
        return prefs?.getString(KEY_LANGUAGE_CODE, null)
    }
}

package com.projectapp.tempus.ui.language

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.projectapp.tempus.data.user.UserProfileCache
import java.util.Locale

/**
 * Singleton manager to handle application language switching
 * Uses AndroidX AppCompatDelegate for modern locale management
 */
object LanguageManager {

    /**
     * Initialize language from cache or system default
     * Should be called in Application.onCreate()
     */
    fun init(context: Context) {
        // Ensure cache is initialized
        UserProfileCache.init(context)

        val savedLanguage = UserProfileCache.getLanguage()
        if (savedLanguage != null) {
            // If user has explicitly chosen a language, apply it
            applyLanguage(savedLanguage)
        } else {
            // If no preference, let the system decide (empty locale list = follow system)
            // But if we want to enforce Vietnamese as one of the supported languages when logic dictates
            // For now, following system default is the standard "unset" behavior.
            // However, the requirement says "Ngôn ngữ hệ thống" if not selected.
            // So we do nothing, or explicitly set empty to follow system.
             AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }
    }

    /**
     * Set the application language
     * @param code Language code ("vi" or "en")
     */
    fun setLanguage(code: String) {
        // 1. Save to cache
        UserProfileCache.saveLanguage(code)
        
        // 2. Apply to app
        applyLanguage(code)
    }

    /**
     * Get current selected language
     * returns "vi" or "en", defaulting to "vi" if cannot determine or system
     */
    fun getCurrentLanguage(): String {
        val saved = UserProfileCache.getLanguage()
        if (saved != null) return saved
        
        // If not saved, check current locale
        val current = AppCompatDelegate.getApplicationLocales().get(0)
        return current?.language ?: "vi" // Default fall back
    }

    private fun applyLanguage(code: String) {
        val appLocale = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}

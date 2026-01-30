package com.projectapp.tempus.ui.language

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.projectapp.tempus.data.user.UserProfileCache
import java.util.Locale


object LanguageManager {

    
    fun init(context: Context) {
        
        UserProfileCache.init(context)

        val savedLanguage = UserProfileCache.getLanguage()
        if (savedLanguage != null) {
            
            applyLanguage(savedLanguage)
        } else {
            
            
             AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }
    }

    
    fun setLanguage(code: String) {
        
        UserProfileCache.saveLanguage(code)
        
        
        applyLanguage(code)
    }

    
    fun getCurrentLanguage(): String {
        val saved = UserProfileCache.getLanguage()
        if (saved != null) return saved
        
        
        val current = AppCompatDelegate.getApplicationLocales().get(0)
        return current?.language ?: "vi" 
    }

    private fun applyLanguage(code: String) {
        val appLocale = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}

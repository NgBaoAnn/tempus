package com.projectapp.tempus.data.personalization

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


interface PersonalizationRepository {
    fun getSettings(): PersonalizationSettings
    fun saveSettings(settings: PersonalizationSettings)
    fun resetToDefaults()
}


class SharedPrefsPersonalizationRepository(context: Context) : PersonalizationRepository {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    override fun getSettings(): PersonalizationSettings {
        val jsonString = prefs.getString(KEY_SETTINGS, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString<PersonalizationSettings>(jsonString)
            } catch (e: Exception) {
                PersonalizationSettings()
            }
        } else {
            PersonalizationSettings()
        }
    }
    
    override fun saveSettings(settings: PersonalizationSettings) {
        val jsonString = json.encodeToString(settings)
        prefs.edit().putString(KEY_SETTINGS, jsonString).apply()
    }
    
    override fun resetToDefaults() {
        prefs.edit().remove(KEY_SETTINGS).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "personalization_prefs"
        private const val KEY_SETTINGS = "personalization_settings"
    }
}

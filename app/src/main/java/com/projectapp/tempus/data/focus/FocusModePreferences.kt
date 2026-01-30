package com.projectapp.tempus.data.focus

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.focusModeDataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_mode_prefs")


class FocusModePreferences(private val context: Context) {
    
    companion object {
        private val KEY_FOCUS_MODE_ENABLED = booleanPreferencesKey("focus_mode_enabled")
        private val KEY_AUTO_START_WITH_TIMER = booleanPreferencesKey("auto_start_with_timer")
        private val KEY_SHOW_OVERLAY = booleanPreferencesKey("show_overlay")
        private val KEY_TOTAL_FOCUS_TIME = longPreferencesKey("total_focus_time_minutes")
        private val KEY_BLOCKED_ATTEMPTS = intPreferencesKey("blocked_attempts")
        private val KEY_LAST_SESSION_DATE = longPreferencesKey("last_session_date")
    }
    
    val focusModeEnabled: Flow<Boolean> = context.focusModeDataStore.data.map { prefs ->
        prefs[KEY_FOCUS_MODE_ENABLED] ?: false
    }
    
    val autoStartWithTimer: Flow<Boolean> = context.focusModeDataStore.data.map { prefs ->
        prefs[KEY_AUTO_START_WITH_TIMER] ?: true
    }
    
    val showOverlay: Flow<Boolean> = context.focusModeDataStore.data.map { prefs ->
        prefs[KEY_SHOW_OVERLAY] ?: true
    }
    
    val totalFocusTime: Flow<Long> = context.focusModeDataStore.data.map { prefs ->
        prefs[KEY_TOTAL_FOCUS_TIME] ?: 0L
    }
    
    val blockedAttempts: Flow<Int> = context.focusModeDataStore.data.map { prefs ->
        prefs[KEY_BLOCKED_ATTEMPTS] ?: 0
    }
    
    suspend fun setFocusModeEnabled(enabled: Boolean) {
        context.focusModeDataStore.edit { prefs ->
            prefs[KEY_FOCUS_MODE_ENABLED] = enabled
        }
    }
    
    suspend fun setAutoStartWithTimer(enabled: Boolean) {
        context.focusModeDataStore.edit { prefs ->
            prefs[KEY_AUTO_START_WITH_TIMER] = enabled
        }
    }
    
    suspend fun setShowOverlay(show: Boolean) {
        context.focusModeDataStore.edit { prefs ->
            prefs[KEY_SHOW_OVERLAY] = show
        }
    }
    
    suspend fun addFocusTime(minutes: Long) {
        context.focusModeDataStore.edit { prefs ->
            val current = prefs[KEY_TOTAL_FOCUS_TIME] ?: 0L
            prefs[KEY_TOTAL_FOCUS_TIME] = current + minutes
        }
    }
    
    suspend fun incrementBlockedAttempts() {
        context.focusModeDataStore.edit { prefs ->
            val current = prefs[KEY_BLOCKED_ATTEMPTS] ?: 0
            prefs[KEY_BLOCKED_ATTEMPTS] = current + 1
        }
    }
    
    suspend fun resetDailyStats() {
        context.focusModeDataStore.edit { prefs ->
            prefs[KEY_BLOCKED_ATTEMPTS] = 0
            prefs[KEY_LAST_SESSION_DATE] = System.currentTimeMillis()
        }
    }
}

package com.projectapp.tempus.ui.setting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.projectapp.tempus.ui.setting.compose.NotificationSettingsScreen
import com.projectapp.tempus.ui.theme.TempusTheme
import com.projectapp.tempus.util.NotificationPreferences

class NotificationSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure prefs are initialized
        NotificationPreferences.init(this)
        
        setContent {
            TempusTheme {
                val timerEnabled by NotificationPreferences.timerEnabled.collectAsState()
                val timelineEnabled by NotificationPreferences.timelineEnabled.collectAsState()
                
                NotificationSettingsScreen(
                    timerEnabled = timerEnabled,
                    timelineEnabled = timelineEnabled,
                    onTimerToggle = { NotificationPreferences.setTimerEnabled(it) },
                    onTimelineToggle = { NotificationPreferences.setTimelineEnabled(it) },
                    onBackClick = { finish() }
                )
            }
        }
    }
}

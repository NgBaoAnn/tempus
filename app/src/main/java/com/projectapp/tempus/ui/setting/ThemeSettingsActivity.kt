package com.projectapp.tempus.ui.setting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.projectapp.tempus.ui.setting.compose.ThemeSettingsScreen
import com.projectapp.tempus.ui.theme.ThemeManager
import com.projectapp.tempus.ui.theme.TempusTheme


class ThemeSettingsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TempusTheme {
                val currentTheme by ThemeManager.themeMode.collectAsState()
                
                ThemeSettingsScreen(
                    currentThemeMode = currentTheme,
                    onThemeSelected = { mode ->
                        ThemeManager.setThemeMode(mode)
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}

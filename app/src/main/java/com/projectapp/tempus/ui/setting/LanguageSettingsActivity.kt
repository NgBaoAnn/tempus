package com.projectapp.tempus.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.projectapp.tempus.ui.language.LanguageManager
import com.projectapp.tempus.ui.setting.compose.LanguageSettingsScreen
import com.projectapp.tempus.ui.theme.TempusTheme

class LanguageSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TempusTheme {
                LanguageSettingsScreen(
                    currentLanguageCode = LanguageManager.getCurrentLanguage(),
                    onLanguageSelected = { code ->
                        LanguageManager.setLanguage(code)
                        // Activity recreation is handled automatically by AppCompatDelegate 
                        // but sometimes needs a little nudge or just finish/restart for clean statel
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}

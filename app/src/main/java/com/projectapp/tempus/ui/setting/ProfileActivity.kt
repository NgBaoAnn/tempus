package com.projectapp.tempus.ui.setting

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.ui.setting.profile.ProfileScreen
import com.projectapp.tempus.ui.theme.TempusTheme

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TempusTheme {
                ProfileScreen(
                    onNavigateToGarden = {
                        // Navigate to MainActivity with Garden tab selected
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("navigate_to", "garden")
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    },
                    onBack = {
                        finish()
                    }
                )
            }
        }
    }
}

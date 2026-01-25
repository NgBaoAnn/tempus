package com.projectapp.tempus.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.ui.auth.LoginActivity
import io.github.jan.supabase.gotrue.auth

/**
 * Onboarding Activity - Entry point của app
 * 
 * Flow:
 * 1. Đã đăng nhập → MainActivity
 * 2. Chưa đăng nhập + Đã xem onboarding → LoginActivity
 * 3. Chưa đăng nhập + Chưa xem onboarding → Onboarding → LoginActivity
 */
class OnboardingActivity : ComponentActivity() {
    
    companion object {
        private const val PREFS_NAME = "tempus_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        
        fun isOnboardingCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        }
        
        private fun setOnboardingCompleted(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Check login status
        val isLoggedIn = SupabaseClientProvider.client.auth.currentUserOrNull() != null
        
        if (isLoggedIn) {
            navigateToMain()
            return
        }
        
        // 2. Check onboarding status
        if (isOnboardingCompleted(this)) {
            navigateToLogin()
            return
        }
        
        // 3. Show onboarding
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            OnboardingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = OnboardingColors.BackgroundLight
                ) {
                    OnboardingScreen(
                        onFinish = {
                            setOnboardingCompleted(this@OnboardingActivity)
                            navigateToLogin()
                        }
                    )
                }
            }
        }
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

/**
 * Light theme for Onboarding
 */
@Composable
fun OnboardingTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = OnboardingColors.GradientStart,
        secondary = OnboardingColors.GradientEnd,
        background = OnboardingColors.BackgroundLight,
        surface = OnboardingColors.BackgroundLight,
        onPrimary = Color.White,
        onBackground = OnboardingColors.TextPrimary,
        onSurface = OnboardingColors.TextPrimary
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

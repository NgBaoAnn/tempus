package com.projectapp.tempus.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.ui.auth.LoginActivity
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch


class OnboardingActivity : ComponentActivity() {
    
    companion object {
        private const val PREFS_NAME = "tempus_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val TAG = "OnboardingActivity"
        
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
        
        
        lifecycleScope.launch {
            try {
                
                SupabaseClientProvider.client.auth.loadFromStorage()
                Log.d(TAG, "Session loaded from storage")
                
                val currentUser = SupabaseClientProvider.client.auth.currentUserOrNull()
                Log.d(TAG, "Current user: ${currentUser?.id}")
                
                if (currentUser != null) {
                    navigateToMain()
                    return@launch
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading session: ${e.message}", e)
            }
            
            
            if (isOnboardingCompleted(this@OnboardingActivity)) {
                navigateToLogin()
                return@launch
            }
            
            
            showOnboarding()
        }
    }
    
    private fun showOnboarding() {
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

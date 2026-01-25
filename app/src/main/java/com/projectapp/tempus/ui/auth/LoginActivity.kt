package com.projectapp.tempus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.lifecycle.lifecycleScope
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.auth.AuthService
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Login Activity using Jetpack Compose
 */
class LoginActivity : ComponentActivity() {
    
    private lateinit var authService: AuthService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        authService = AuthService(supabaseClient = SupabaseClientProvider.client)
        
        setContent {
            AuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AuthColors.Background
                ) {
                    LoginScreen(
                        onLoginClick = { email, password -> handleLogin(email, password) },
                        onGoogleClick = { handleGoogleLogin() },
                        onForgotPasswordClick = { email -> handleForgotPassword(email) },
                        onRegisterClick = { navigateToRegister() }
                    )
                }
            }
        }
    }
    
    private fun handleLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                authService.login(email, password)
                Toast.makeText(this@LoginActivity, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } catch (e: HttpException) {
                Log.e("LoginActivity", "HTTP Error: ${e.code()}", e)
                Toast.makeText(this@LoginActivity, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LoginActivity", "Login Error", e)
                Toast.makeText(this@LoginActivity, "Lỗi không xác định", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handleGoogleLogin() {
        Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
    }
    
    private fun handleForgotPassword(email: String) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                authService.resetPassword(email)
                Toast.makeText(this@LoginActivity, "Vui lòng kiểm tra email để đặt lại mật khẩu", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("LoginActivity", "Reset Password Error", e)
                Toast.makeText(this@LoginActivity, "Lỗi: Không thể gửi email khôi phục", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}

/**
 * Auth color scheme
 */
object AuthColors {
    val Background = Color(0xFFF5F5F5)
    val CardBackground = Color.White
    val InputBackground = Color(0xFFFAFAFA)
    val PrimaryBlue = Color(0xFF1877F2)
    val TextPrimary = Color(0xFF333333)
    val TextSecondary = Color(0xFF797474)
    val TempusBlue = Color(0xFF0082BB)
    val BorderGray = Color(0xFFCCCCCC)
}

/**
 * Auth Theme
 */
@Composable
fun AuthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AuthColors.PrimaryBlue,
            background = AuthColors.Background,
            surface = AuthColors.CardBackground,
            onPrimary = Color.White,
            onBackground = AuthColors.TextPrimary,
            onSurface = AuthColors.TextPrimary
        ),
        content = content
    )
}

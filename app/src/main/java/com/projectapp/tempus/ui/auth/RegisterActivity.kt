package com.projectapp.tempus.ui.auth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.auth.AuthService
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch

/**
 * Register Activity using Jetpack Compose
 */
class RegisterActivity : ComponentActivity() {
    
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
                    RegisterScreen(
                        onRegisterClick = { fullName, email, password, confirmPassword ->
                            handleRegister(fullName, email, password, confirmPassword)
                        },
                        onLoginClick = { finish() }
                    )
                }
            }
        }
    }
    
    private fun handleRegister(fullName: String, email: String, password: String, confirmPassword: String) {
        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password.length < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password != confirmPassword) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                authService.register(email, password, fullName)
                Toast.makeText(this@RegisterActivity, "Đăng ký thành công! Vui lòng kiểm tra email.", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: RestException) {
                Log.e("RegisterActivity", "Supabase Error: ${e.message}")
                val errorMsg = when {
                    e.message?.contains("already registered") == true -> "Email này đã được sử dụng"
                    else -> "Lỗi: ${e.message}"
                }
                Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Error: ${e.message}")
                Toast.makeText(this@RegisterActivity, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

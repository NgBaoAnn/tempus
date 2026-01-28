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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.R
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.auth.AuthService
import kotlinx.coroutines.launch
import retrofit2.HttpException
import io.github.jan.supabase.gotrue.auth

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
                
                // Auto-sync: Pull data from Supabase to local Room
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    try {
                        Toast.makeText(this@LoginActivity, "Đang đồng bộ dữ liệu...", Toast.LENGTH_SHORT).show()
                        
                        // 1. Pull Schedule data
                        val syncManager = com.projectapp.tempus.data.RepositoryProvider.getSyncManager(this@LoginActivity)
                        val scheduleResult = syncManager.pullFromServer(userId)
                        Log.d("LoginActivity", "Schedule sync: ${scheduleResult.getOrNull()} items")
                        
                        // 2. Pull Gamification data
                        val gamificationSyncManager = com.projectapp.tempus.data.RepositoryProvider.getGamificationSyncManager(this@LoginActivity)
                        val gamificationResult = gamificationSyncManager.pullFromServer()
                        Log.d("LoginActivity", "Gamification sync: ${gamificationResult.getOrNull()?.summary()}")
                        
                        // 3. Pull Notes data
                        val notesSyncManager = com.projectapp.tempus.data.RepositoryProvider.getNotesSyncManager(this@LoginActivity)
                        val notesResult = notesSyncManager.pullFromServer(userId)
                        Log.d("LoginActivity", "Notes sync: ${notesResult.getOrNull()?.summary()}")
                        
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "Auto-sync failed, continuing anyway", e)
                        // Continue to main screen even if sync fails
                    }
                }
                
                // Fetch user profile to update cache and theme (from master)
                val userRepo = com.projectapp.tempus.data.user.SupabaseUserRepository()
                val user = userRepo.getCurrentUser()
                
                user.themeColor?.takeIf { it.isNotEmpty() }?.let { themeColor ->
                    val mode = com.projectapp.tempus.ui.theme.ThemeMode.fromValue(themeColor)
                    com.projectapp.tempus.ui.theme.ThemeManager.updateThemeLocally(mode)
                }
                
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
        lifecycleScope.launch {
            try {
                // Khởi tạo Credential Manager
                val credentialManager = CredentialManager.create(this@LoginActivity)
                
                // Tạo Google ID Option với Web Client ID
                val googleIdOption = GetSignInWithGoogleOption.Builder(
                    getString(R.string.google_web_client_id)
                ).build()


                // Tạo request để lấy credential
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                
                // Lấy credential từ Google
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity,
                )
                
                // Xử lý credential nhận được
                val credential = result.credential
                
                // Kiểm tra xem có phải Google ID Token không
                if (credential is androidx.credentials.CustomCredential && 
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    Log.d("LoginActivity", "Google Sign-In successful, authenticating with Supabase...")
                    
                    // Đăng nhập với Supabase sử dụng ID Token
                    // Đăng nhập với Supabase sử dụng ID Token
                    authService.signInWithGoogle(idToken)
                    
<<<<<<< HEAD
                    // Auto-sync: Pull data from Supabase to local Room
                    val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        try {
                            Toast.makeText(this@LoginActivity, "Đang đồng bộ dữ liệu...", Toast.LENGTH_SHORT).show()
                            
                            // 1. Pull Schedule data
                            val syncManager = com.projectapp.tempus.data.RepositoryProvider.getSyncManager(this@LoginActivity)
                            val scheduleResult = syncManager.pullFromServer(userId)
                            Log.d("LoginActivity", "Schedule sync: ${scheduleResult.getOrNull()} items")
                            
                            // 2. Pull Gamification data
                            val gamificationSyncManager = com.projectapp.tempus.data.RepositoryProvider.getGamificationSyncManager(this@LoginActivity)
                            val gamificationResult = gamificationSyncManager.pullFromServer()
                            Log.d("LoginActivity", "Gamification sync: ${gamificationResult.getOrNull()?.summary()}")
                            
                            // 3. Pull Notes data
                            val notesSyncManager = com.projectapp.tempus.data.RepositoryProvider.getNotesSyncManager(this@LoginActivity)
                            val notesResult = notesSyncManager.pullFromServer(userId)
                            Log.d("LoginActivity", "Notes sync: ${notesResult.getOrNull()?.summary()}")
                            
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Auto-sync failed, continuing anyway", e)
                        }
=======
                    // Fetch user profile to update cache and theme
                    val userRepo = com.projectapp.tempus.data.user.SupabaseUserRepository()
                    try {
                        val user = userRepo.getCurrentUser()
                        user.themeColor?.takeIf { it.isNotEmpty() }?.let { themeColor ->
                            val mode = com.projectapp.tempus.ui.theme.ThemeMode.fromValue(themeColor)
                            com.projectapp.tempus.ui.theme.ThemeManager.updateThemeLocally(mode)
                        }
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "Failed to fetch user profile after Google login", e)
                        // Continue anyway, just theme might be wrong initially
>>>>>>> origin/master
                    }
                    
                    Toast.makeText(this@LoginActivity, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.e("LoginActivity", "Unexpected credential type: ${credential.type}")
                    Toast.makeText(this@LoginActivity, "Lỗi: Loại credential không hợp lệ", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: GetCredentialCancellationException) {
                Log.d("LoginActivity", "User cancelled Google Sign-In")
                // Người dùng hủy, không cần hiển thị lỗi
            } catch (e: NoCredentialException) {
                Log.e("LoginActivity", "No Google account found", e)
                Toast.makeText(this@LoginActivity, "Không tìm thấy tài khoản Google", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Log.e("LoginActivity", "Supabase authentication failed: ${e.code()}", e)
                Toast.makeText(this@LoginActivity, "Lỗi xác thực với server", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LoginActivity", "Google Sign-In failed", e)
                Toast.makeText(this@LoginActivity, "Lỗi đăng nhập Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handleForgotPassword(email: String) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                authService.resetPassword(email)
                Toast.makeText(this@LoginActivity, "Mã xác nhận đã được gửi tới email", Toast.LENGTH_LONG).show()
                
                // Navigate to VerifyOtpActivity
                val intent = Intent(this@LoginActivity, VerifyOtpActivity::class.java)
                intent.putExtra("EMAIL", email)
                startActivity(intent)
                
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

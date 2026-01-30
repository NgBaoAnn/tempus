package com.projectapp.tempus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.auth.AuthService
import kotlinx.coroutines.launch

class ResetPasswordActivity : ComponentActivity() {

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
                    ResetPasswordScreen(
                        onConfirmClick = { password -> handleUpdatePassword(password) }
                    )
                }
            }
        }
    }

    private fun handleUpdatePassword(password: String) {
        lifecycleScope.launch {
            try {
                authService.updatePassword(password)
                Toast.makeText(this@ResetPasswordActivity, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                
                
                startActivity(Intent(this@ResetPasswordActivity, MainActivity::class.java))
                finishAffinity()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ResetPasswordActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(
    onConfirmClick: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        Text(
            text = "Đặt lại mật khẩu",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AuthColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Nhập mật khẩu mới cho tài khoản của bạn",
            fontSize = 16.sp,
            color = AuthColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu mới") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuthColors.PrimaryBlue,
                unfocusedBorderColor = AuthColors.BorderGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Xác nhận mật khẩu") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuthColors.PrimaryBlue,
                unfocusedBorderColor = AuthColors.BorderGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        
        Button(
            onClick = { 
                if (password == confirmPassword) {
                    onConfirmClick(password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuthColors.PrimaryBlue),
            enabled = password.isNotEmpty() && password == confirmPassword
        ) {
            Text("Đổi mật khẩu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Mật khẩu không khớp", color = Color.Red, fontSize = 14.sp)
        }
    }
}

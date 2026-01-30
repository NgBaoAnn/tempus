package com.projectapp.tempus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.auth.AuthService
import kotlinx.coroutines.launch

class VerifyOtpActivity : ComponentActivity() {

    private lateinit var authService: AuthService
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authService = AuthService(supabaseClient = SupabaseClientProvider.client)
        email = intent.getStringExtra("EMAIL")

        setContent {
            AuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AuthColors.Background
                ) {
                    VerifyOtpScreen(
                        email = email ?: "",
                        onVerifyClick = { token -> handleVerify(token) },
                        onBackClick = { finish() },
                        onResendClick = { handleResend() }
                    )
                }
            }
        }
    }

    private fun handleVerify(token: String) {
        val userEmail = email
        if (userEmail == null) {
            Toast.makeText(this, "Không tìm thấy email", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                authService.verifyRecoveryOtp(userEmail, token)
                Toast.makeText(this@VerifyOtpActivity, "Xác thực thành công", Toast.LENGTH_SHORT).show()
                
                val intent = Intent(this@VerifyOtpActivity, ResetPasswordActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@VerifyOtpActivity, "Mã xác nhận không đúng hoặc đã hết hạn", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleResend() {
        val userEmail = email ?: return
        lifecycleScope.launch {
            try {
                authService.resetPassword(userEmail)
                Toast.makeText(this@VerifyOtpActivity, "Đã gửi lại mã", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@VerifyOtpActivity, "Không thể gửi lại mã", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun VerifyOtpScreen(
    email: String,
    onVerifyClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onResendClick: () -> Unit
) {
    var otp by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        Text(
            text = "Nhập mã xác nhận",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AuthColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = "Mã xác nhận 8 số đã được gửi tới email:",
            fontSize = 16.sp,
            color = AuthColors.TextSecondary
        )
        Text(
            text = email,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = AuthColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        OutlinedTextField(
            value = otp,
            onValueChange = { 
                if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                    otp = it 
                }
            },
            label = { Text("Mã OTP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onVerifyClick(otp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuthColors.PrimaryBlue),
            enabled = otp.length == 8
        ) {
            Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Không nhận được mã? ", color = AuthColors.TextSecondary)
            TextButton(onClick = onResendClick) {
                Text("Gửi lại", color = AuthColors.PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(onClick = onBackClick) {
            Text("Quay lại đăng nhập", color = AuthColors.TextSecondary)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

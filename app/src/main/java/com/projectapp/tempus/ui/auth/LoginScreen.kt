package com.projectapp.tempus.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onGoogleClick: () -> Unit,
    onForgotPasswordClick: (email: String) -> Unit,
    onRegisterClick: () -> Unit,
    isLoading: Boolean = false,
    loadingMessage: String = ""
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(26.dp))
        
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_tiramisu),
                contentDescription = "Logo",
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Tempus", fontSize = 30.sp, fontWeight = FontWeight.Black, color = AuthColors.TempusBlue)
        }
        
        Spacer(modifier = Modifier.height(70.dp))
        
        
        Text("Chào mừng trở lại", fontSize = 26.sp, fontWeight = FontWeight.Bold, 
            color = AuthColors.TextPrimary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Đăng nhập để tiếp tục", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
            color = AuthColors.TextSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(50.dp))
        
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AuthColors.CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                
                Text("Email", fontSize = 16.sp, color = AuthColors.TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                AuthInputField(
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Default.Email,
                    placeholder = "Nhập email của bạn",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                
                Text("Mật khẩu", fontSize = 16.sp, color = AuthColors.TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                AuthPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Nhập mật khẩu",
                    passwordVisible = passwordVisible,
                    onVisibilityToggle = { passwordVisible = !passwordVisible },
                    onImeAction = { focusManager.clearFocus(); onLoginClick(email, password) }
                )
            }
        }
        
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showForgotPasswordDialog = true }) {
                Text("Quên mật khẩu?", color = AuthColors.TextSecondary)
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        
        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuthColors.PrimaryBlue)
        ) {
            Text("Đăng nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        
        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Đăng nhập với Google", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AuthColors.TextPrimary)
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        
        TextButton(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) {
            Text("Chưa có tài khoản? Đăng ký ngay", fontSize = 16.sp, color = AuthColors.PrimaryBlue)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
    
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onSend = { showForgotPasswordDialog = false; onForgotPasswordClick(it) }
        )
    }
    
    // Loading overlay
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = AuthColors.PrimaryBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = loadingMessage,
                        fontSize = 16.sp,
                        color = AuthColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onImeAction: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = AuthColors.InputBackground) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(26.dp), AuthColors.TextSecondary)
            Spacer(Modifier.width(8.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
                singleLine = true,
                placeholder = { Text(placeholder, color = AuthColors.TextSecondary) }
            )
        }
    }
}

@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    passwordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    onImeAction: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = AuthColors.InputBackground) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, null, Modifier.size(26.dp), AuthColors.TextSecondary)
            Spacer(Modifier.width(8.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onImeAction() }),
                singleLine = true,
                placeholder = { Text(placeholder, color = AuthColors.TextSecondary) }
            )
            IconButton(onClick = onVisibilityToggle, modifier = Modifier.size(26.dp)) {
                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = AuthColors.TextSecondary)
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(onDismiss: () -> Unit, onSend: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quên mật khẩu", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Nhập email để nhận link đặt lại mật khẩu")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { onSend(email) }) { Text("Gửi") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } }
    )
}

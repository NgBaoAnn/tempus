package com.projectapp.tempus.ui.auth

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    onRegisterClick: (fullName: String, email: String, password: String, confirmPassword: String) -> Unit,
    onLoginClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Title
        Text(
            "Tạo tài khoản mới",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = AuthColors.TextPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(50.dp))
        
        // Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AuthColors.CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Full Name
                RegisterField(
                    label = "Họ và tên",
                    value = fullName,
                    onValueChange = { fullName = it },
                    icon = Icons.Default.Person,
                    placeholder = "Nhập họ và tên của bạn",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Email
                RegisterField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Default.Email,
                    placeholder = "example@email.com",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Password
                RegisterPasswordFieldComp(
                    label = "Mật khẩu",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Nhập mật khẩu (≥6 ký tự)",
                    passwordVisible = passwordVisible,
                    onVisibilityToggle = { passwordVisible = !passwordVisible },
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Confirm Password
                RegisterPasswordFieldComp(
                    label = "Nhập lại mật khẩu",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Xác nhận mật khẩu",
                    passwordVisible = confirmPasswordVisible,
                    onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus(); onRegisterClick(fullName, email, password, confirmPassword) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Register Button
        Button(
            onClick = { onRegisterClick(fullName, email, password, confirmPassword) },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuthColors.PrimaryBlue)
        ) {
            Text("Đăng ký", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        // Login Link
        TextButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text("Đã có tài khoản? Đăng nhập", fontSize = 16.sp, color = AuthColors.PrimaryBlue)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onImeAction: () -> Unit
) {
    Column {
        Text(label, fontSize = 16.sp, color = AuthColors.TextPrimary, modifier = Modifier.padding(bottom = 4.dp))
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = AuthColors.InputBackground) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(26.dp), AuthColors.TextSecondary)
                Spacer(Modifier.width(10.dp))
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
}

@Composable
private fun RegisterPasswordFieldComp(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    passwordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    imeAction: ImeAction,
    onImeAction: () -> Unit
) {
    Column {
        Text(label, fontSize = 16.sp, color = AuthColors.TextPrimary, modifier = Modifier.padding(bottom = 4.dp))
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = AuthColors.InputBackground) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, Modifier.size(26.dp), AuthColors.TextSecondary)
                Spacer(Modifier.width(10.dp))
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
                    keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
                    singleLine = true,
                    placeholder = { Text(placeholder, color = AuthColors.TextSecondary) }
                )
                IconButton(onClick = onVisibilityToggle, modifier = Modifier.size(26.dp)) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = AuthColors.TextSecondary)
                }
            }
        }
    }
}

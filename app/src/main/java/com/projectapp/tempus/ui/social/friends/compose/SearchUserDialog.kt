package com.projectapp.tempus.ui.social.friends.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.projectapp.tempus.data.social.dto.UserBasicDto

/**
 * Dialog tìm kiếm và gửi lời mời kết bạn
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserDialog(
    searchResults: List<UserBasicDto>,
    isSearching: Boolean,
    onSearch: (String) -> Unit,
    onSendRequest: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(24.dp),
            color = SocialColors.CardBackground,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tìm bạn bè",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialColors.TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = SocialColors.TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        if (it.length >= 2) {
                            onSearch(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập username...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                onSearch("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch(searchQuery)
                            keyboardController?.hide()
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SocialColors.Primary,
                        cursorColor = SocialColors.Primary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Results
                when {
                    isSearching -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SocialColors.Primary)
                        }
                    }
                    searchQuery.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = SocialColors.TextSecondary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tìm kiếm theo username",
                                    color = SocialColors.TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    searchResults.isEmpty() && searchQuery.length >= 2 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không tìm thấy người dùng nào",
                                color = SocialColors.TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults, key = { it.id }) { user ->
                                SearchResultItem(
                                    user = user,
                                    onSendRequest = { onSendRequest(user.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    user: UserBasicDto,
    onSendRequest: () -> Unit
) {
    var requestSent by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SocialColors.PrimaryLight.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SocialColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.firstOrNull()?.uppercase() ?: "?",
                    color = SocialColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialColors.TextPrimary
                )
                user.email?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = SocialColors.TextSecondary
                    )
                }
            }
            
            // Add button
            if (requestSent) {
                Text(
                    text = "Đã gửi",
                    color = SocialColors.Secondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                IconButton(
                    onClick = {
                        onSendRequest()
                        requestSent = true
                    }
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "Gửi lời mời",
                        tint = SocialColors.Primary
                    )
                }
            }
        }
    }
}

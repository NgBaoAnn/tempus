package com.projectapp.tempus.ui.social.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.projectapp.tempus.data.social.dto.UserProfile
import com.projectapp.tempus.data.social.repository.RelationshipStatus
import com.projectapp.tempus.ui.theme.TempusDesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: FriendProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = androidx.compose.material3.SnackbarHostState()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                uiState.profile?.let { profile ->
                    ProfileContent(
                        profile = profile,
                        isActionLoading = uiState.isActionLoading,
                        onBack = onBack,
                        onSendRequest = { viewModel.sendFriendRequest(profile.id) },
                        onBlock = { viewModel.blockUser(profile.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    isActionLoading: Boolean,
    onBack: () -> Unit,
    onSendRequest: () -> Unit,
    onBlock: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Hero Header with Gradient Mesh
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Top Bar
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            // Avatar & Name
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                ) {
                    if (!profile.avatar.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(TempusDesignSystem.PrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.username.first().uppercase(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = profile.username,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // Status Badge
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = when(profile.relationshipStatus) {
                            RelationshipStatus.Friends -> "Bạn bè"
                            RelationshipStatus.RequestSent -> "Đã gửi lời mời"
                            RelationshipStatus.RequestReceived -> "Đã nhận lời mời"
                            RelationshipStatus.Blocked -> "Đã chặn"
                            else -> "Người lạ"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-30).dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatCard("Bạn bè", profile.friendsCount.toString())
            StatCard("Cây trồng", profile.treesCount.toString()) // Placeholder stat
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Action Button
            Button(
                onClick = {
                    when(profile.relationshipStatus) {
                        RelationshipStatus.None -> onSendRequest()
                        else -> {} // Handle other statuses later (unfriend, etc.)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isActionLoading && profile.relationshipStatus == RelationshipStatus.None,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isActionLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = when(profile.relationshipStatus) {
                            RelationshipStatus.Friends -> Icons.Default.Check
                            RelationshipStatus.RequestSent -> Icons.Default.Check
                            else -> Icons.Default.PersonAdd
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when(profile.relationshipStatus) {
                            RelationshipStatus.Friends -> "Đã là bạn bè"
                            RelationshipStatus.RequestSent -> "Đã gửi yêu cầu"
                            RelationshipStatus.RequestReceived -> "Phản hồi lời mời"
                            RelationshipStatus.Blocked -> "Đã chặn"
                            RelationshipStatus.None -> "Thêm bạn bè"
                            else -> "Thêm bạn bè"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Secondary Action (Block)
            if (profile.relationshipStatus != RelationshipStatus.Blocked) {
                OutlinedButton(
                    onClick = onBlock,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Block, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chặn người dùng")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Thông tin thêm",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                InfoRow(Icons.Default.Email, "Email", profile.email ?: "Ẩn")
                // Add Joined Date using profile.joinedDate
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

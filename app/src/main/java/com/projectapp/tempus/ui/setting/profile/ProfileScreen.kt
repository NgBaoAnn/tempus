package com.projectapp.tempus.ui.setting.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToGarden: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: ProfileViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(application) as T
            }
        }
    )
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }
    
    // Show success message
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            viewModel.clearSaveSuccess()
        }
    }

    // Show error message
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
        }
    }
    
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    viewModel.uploadAvatar(bytes)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi đọc ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Avatar
                ProfileAvatar(
                    avatarUrl = uiState.avatarUrl,
                    onAvatarClick = { 
                        avatarPicker.launch("image/*")
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name with edit button
                ProfileName(
                    name = uiState.fullName,
                    onEditClick = { showEditDialog = true }
                )
                
                // Email
                Text(
                    text = uiState.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Stats Section
                Text(
                    text = "📊 Thống kê",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                StatsRow(
                    points = uiState.totalPoints,
                    streak = uiState.currentStreak,
                    trees = uiState.treeCount
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Garden Link
                GardenLinkCard(onClick = onNavigateToGarden)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Member since
                if (uiState.memberSince.isNotEmpty()) {
                    Text(
                        text = "📅 Thành viên từ: ${uiState.memberSince}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Edit Name Dialog
    if (showEditDialog) {
        EditNameDialog(
            currentName = uiState.fullName,
            isSaving = uiState.isSaving,
            onDismiss = { showEditDialog = false },
            onSave = { newName ->
                viewModel.updateName(newName)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun ProfileAvatar(
    avatarUrl: String?,
    onAvatarClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onAvatarClick),
        contentAlignment = Alignment.Center
    ) {
        // Use AsyncImage for everything to maintain stable node structure
        // If avatarUrl is null, it acts as if loading model=null, triggering fallback/error
        AsyncImage(
            model = coil.request.ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            // Use fallback/error painter to show the icon if no image
            error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person),
            fallback = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person),
            placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person)
        )
        
        // Edit icon overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change Avatar",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ProfileName(
    name: String,
    onEditClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Invisible spacer to balance the IconButton on the right, ensuring Text is centered
        Spacer(modifier = Modifier.width(48.dp))
        
        Text(
            text = name.ifEmpty { "Chưa đặt tên" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f, fill = false)
        )
        
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Chỉnh sửa tên",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StatsRow(
    points: Int,
    streak: Int,
    trees: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            emoji = "🎯",
            value = points.toString(),
            label = "Điểm",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            emoji = "🔥",
            value = streak.toString(),
            label = "Streak",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            emoji = "🌳",
            value = trees.toString(),
            label = "Cây",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun GardenLinkCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌳", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Vườn cây của tôi",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa tên") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên của bạn") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                enabled = !isSaving && name.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Lưu")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

package com.projectapp.tempus.ui.notes.compose
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium Note Editor Screen with Modern Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    title: String,
    content: String,
    isNew: Boolean,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val saveButtonScale by animateFloatAsState(
        targetValue = if (isSaving) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "save_scale"
    )
    
    Scaffold(
        modifier = modifier,
        topBar = {
            EditorTopBar(
                isNew = isNew,
                isSaving = isSaving,
                saveButtonScale = saveButtonScale,
                onBackClick = onBackClick,
                onDeleteClick = { showDeleteDialog = true },
                onSaveClick = onSaveClick
            )
        },
        containerColor = NotesDesignSystem.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header decoration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                NotesDesignSystem.Primary,
                                NotesDesignSystem.Accent,
                                NotesDesignSystem.Primary.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title field with premium styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = NotesDesignSystem.TextPrimary,
                        lineHeight = 36.sp
                    ),
                    cursorBrush = SolidColor(NotesDesignSystem.Primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (title.isEmpty()) {
                                Text(
                                    text = "Tiêu đề",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NotesDesignSystem.TextMuted.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Modern divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NotesDesignSystem.Primary.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.width(12.dp))
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = NotesDesignSystem.TextMuted.copy(alpha = 0.15f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Content field with premium styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .defaultMinSize(minHeight = 400.dp)
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = NotesDesignSystem.TextPrimary,
                        lineHeight = 26.sp
                    ),
                    cursorBrush = SolidColor(NotesDesignSystem.Primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (content.isEmpty()) {
                                Text(
                                    text = "Bắt đầu viết ghi chú...",
                                    fontSize = 16.sp,
                                    color = NotesDesignSystem.TextMuted.copy(alpha = 0.5f),
                                    lineHeight = 26.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // Modern Delete Dialog
    if (showDeleteDialog) {
        ModernDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteClick()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    isNew: Boolean,
    isSaving: Boolean,
    saveButtonScale: Float,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    TopAppBar(
        title = {
            AnimatedContent(
                targetState = isNew,
                transitionSpec = {
                    slideInVertically { -it } + fadeIn() togetherWith
                    slideOutVertically { it } + fadeOut()
                },
                label = "title_transition"
            ) { isNewNote ->
                Text(
                    text = if (isNewNote) "Ghi chú mới" else "Chỉnh sửa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NotesDesignSystem.TextPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NotesDesignSystem.SurfaceElevated)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = NotesDesignSystem.TextPrimary
                )
            }
        },
        actions = {
            // Delete button (only for editing)
            AnimatedVisibility(
                visible = !isNew,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Save button with premium styling
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = NotesDesignSystem.Primary.copy(alpha = 0.2f)
                    )
            ) {
                Button(
                    onClick = onSaveClick,
                    enabled = !isSaving,
                    modifier = Modifier
                        .height(40.dp)
                        .animateContentSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NotesDesignSystem.Primary,
                        disabledContainerColor = NotesDesignSystem.Primary.copy(alpha = 0.7f)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    AnimatedContent(
                        targetState = isSaving,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "save_content"
                    ) { saving ->
                        if (saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Lưu",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun ModernDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = NotesDesignSystem.Surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                "Xóa ghi chú?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NotesDesignSystem.TextPrimary
            )
        },
        text = {
            Text(
                "Ghi chú này sẽ bị xóa vĩnh viễn. Bạn không thể hoàn tác hành động này.",
                fontSize = 15.sp,
                color = NotesDesignSystem.TextSecondary,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    "Xóa",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Hủy",
                    color = NotesDesignSystem.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

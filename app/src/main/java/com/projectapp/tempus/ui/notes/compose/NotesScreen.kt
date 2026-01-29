package com.projectapp.tempus.ui.notes.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R
import com.projectapp.tempus.data.notes.entity.NoteEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern Design System Colors - Flat Design with Premium Feel
 */
object NotesDesignSystem {
    // Primary Palette
    val Primary = Color(0xFF3B82F6)        // Blue 500
    val PrimaryLight = Color(0xFF60A5FA)    // Blue 400
    val PrimaryDark = Color(0xFF1D4ED8)     // Blue 700
    
    // Accent Colors
    val Accent = Color(0xFF60A5FA)          // Blue 400 (was Violet)
    val AccentLight = Color(0xFF93C5FD)     // Blue 300
    val Success = Color(0xFF10B981)         // Emerald 500
    val Warning = Color(0xFFF59E0B)         // Amber 500
    
    // Backgrounds
    val Background = Color(0xFFF8FAFC)      // Slate 50
    val Surface = Color(0xFFFFFFFF)
    val SurfaceElevated = Color(0xFFF1F5F9) // Slate 100
    
    // Text Colors
    val TextPrimary = Color(0xFF0F172A)     // Slate 900
    val TextSecondary = Color(0xFF475569)   // Slate 600
    val TextMuted = Color(0xFF94A3B8)       // Slate 400
    
    // Note Card Colors - Soft pastels
    val CardYellow = Color(0xFFFEF3C7)      // Amber 100
    val CardBlue = Color(0xFFDBEAFE)        // Blue 100
    val CardGreen = Color(0xFFD1FAE5)       // Emerald 100
    val CardPink = Color(0xFFFCE7F3)        // Pink 100
    val CardPurple = Color(0xFFEDE9FE)      // Violet 100
    val CardOrange = Color(0xFFFFEDD5)      // Orange 100
    val CardDefault = Color(0xFFFFFFFF)
    
    // Gradients
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))  // Blue gradient
    )
    val WarmGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFF97316), Color(0xFFEC4899))
    )
    val CoolGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    )
}

/**
 * Main Notes Screen - Modern Flat Design with Premium Feel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<NoteEntity>,
    searchQuery: String,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onAddClick: () -> Unit,
    onPinClick: (NoteEntity) -> Unit,
    onDeleteClick: (NoteEntity) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        topBar = {
            ModernTopBar(
                title = "Ghi chú",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            ModernFAB(onClick = onAddClick)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Modern Search Bar with animation
            AnimatedSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClearClick = onClearSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
            
            // Content with smooth transitions
            AnimatedContent(
                targetState = when {
                    isLoading -> "loading"
                    notes.isEmpty() -> "empty"
                    else -> "content"
                },
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(200))
                },
                label = "content_transition"
            ) { state ->
                when (state) {
                    "loading" -> LoadingState()
                    "empty" -> ModernEmptyState(
                        isSearching = searchQuery.isNotBlank(),
                        onAddClick = onAddClick
                    )
                    else -> NotesGrid(
                        notes = notes,
                        onNoteClick = onNoteClick,
                        onPinClick = onPinClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun ModernFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Thêm ghi chú",
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun AnimatedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFocused = remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isFocused.value || query.isNotBlank()) 4.dp else 0.dp,
        animationSpec = tween(200),
        label = "search_elevation"
    )
    
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
        placeholder = {
            Text(
                "Tìm kiếm ghi chú...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 15.sp
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = if (query.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotBlank(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun NotesGrid(
    notes: List<NoteEntity>,
    onNoteClick: (NoteEntity) -> Unit,
    onPinClick: (NoteEntity) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 100.dp
        ),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(notes, key = { it.id }) { note ->
            ModernNoteCard(
                note = note,
                onClick = { onNoteClick(note) },
                onPinClick = { onPinClick(note) }
            )
        }
    }
}

@Composable
private fun ModernNoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onPinClick: () -> Unit
) {
    // Determine card background color
    // We can keep the pastel colors but ensure they work in dark mode or tone them down
    // For now, let's map them to somewhat safe colors, or just ensure text is legible.
    // Ideally, "yellow" etc. should perhaps map to surfaceContainerHigh + a tint in Dark Mode.
    // For simplicity and "sync theme" goal, let's use surface colors but maybe tinted if feasible.
    // Or, keep the specific colors but ensure text contrast.
    
    // For now, I will map "default" to surface, and others I'll leave as custom but careful.
    // However, if the user wants FULL theme, maybe colored notes are part of the feature?
    // Let's keep the colored cards as they are likely a feature (color coding), 
    // BUT we must ensure the text color is readable.
    // In Dark Mode, these pastel colors might be too bright.
    
    // Strategy: Use the same colors but maybe alpha-blended with primaryContainer if dark?
    // Or simply use them. Let's assume they are "sticky note" colors and intended to be light.
    // BUT we must ensure the Text color on top is black/dark grey, NOT white (if app is dark theme).
    // So for the CARD content, we unfortunately need dark text even in dark mode if the background is light pastel.
    
    val backgroundColor = when (note.color) {
        "yellow" -> Color(0xFFFEF3C7)
        "blue" -> Color(0xFFDBEAFE)
        "green" -> Color(0xFFD1FAE5)
        "pink" -> Color(0xFFFCE7F3)
        "purple" -> Color(0xFFEDE9FE)
        "orange" -> Color(0xFFFFEDD5)
        else -> MaterialTheme.colorScheme.surface
    }
    
    // If background is specific color (not surface), force dark text
    val contentColor = if (note.color != "default" && note.color != null) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
    val mutedColor = if (note.color != "default" && note.color != null) Color(0xFF64748B) else MaterialTheme.colorScheme.onSurfaceVariant
    
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale("vi")) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (note.isPinned) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .clickable { onPinClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pin),
                            contentDescription = "Đã ghim",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            if (note.title.isNotBlank() && note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Content preview
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    color = convertToDarker(contentColor, 0.8f), // Slightly lighter than pure black
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer with date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateFormatter.format(Date(note.updatedAt)),
                    fontSize = 12.sp,
                    color = mutedColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helper to avoid heavy dependencies, just returning color as is for now or logic
private fun convertToDarker(color: Color, alpha: Float): Color {
    return color.copy(alpha = alpha)
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun ModernEmptyState(
    isSearching: Boolean,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon container
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_note),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(56.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (isSearching) "Không tìm thấy kết quả" else "Bắt đầu ghi chú",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isSearching) "Thử từ khóa khác" else "Ghi lại ý tưởng của bạn ngay bây giờ",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (!isSearching) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .height(52.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 28.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Tạo ghi chú đầu tiên",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

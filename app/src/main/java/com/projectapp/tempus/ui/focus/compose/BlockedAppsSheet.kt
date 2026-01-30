package com.projectapp.tempus.ui.focus.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.ui.focus.InstalledApp


private object SheetDesignTokens {
    
    val backgroundDark = Color(0xFF0A0E1A)
    val surfaceGlass = Color.White.copy(alpha = 0.05f)
    val surfaceGlassElevated = Color.White.copy(alpha = 0.08f)
    val borderGlass = Color.White.copy(alpha = 0.08f)
    
    val primaryBlue = Color(0xFF3B82F6)
    val accentGreen = Color(0xFF10B981)
    val accentRed = Color(0xFFEF4444)
    
    val textPrimary = Color.White
    val textSecondary = Color.White.copy(alpha = 0.7f)
    val textMuted = Color.White.copy(alpha = 0.4f)
    
    
    val headlineMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
    val bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    )
    val labelSmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedAppsSheet(
    installedApps: List<InstalledApp>,
    isLoading: Boolean,
    onBlockApp: (InstalledApp) -> Unit,
    onUnblockApp: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isEmpty()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SheetDesignTokens.backgroundDark,
        dragHandle = {
            
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SheetDesignTokens.textMuted)
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp)
        ) {
            
            SheetHeader(
                appCount = installedApps.size,
                blockedCount = installedApps.count { it.isBlocked },
                onDismiss = onDismiss
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            
            if (isLoading) {
                LoadingContent()
            } else if (filteredApps.isEmpty()) {
                EmptySearchContent()
            } else {
                AppList(
                    apps = filteredApps,
                    onBlockApp = onBlockApp,
                    onUnblockApp = onUnblockApp
                )
            }
        }
    }
}


@Composable
private fun SheetHeader(
    appCount: Int,
    blockedCount: Int,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Chọn ứng dụng",
                style = SheetDesignTokens.headlineMedium,
                color = SheetDesignTokens.textPrimary
            )
            Text(
                text = "$blockedCount / $appCount ứng dụng bị chặn",
                style = SheetDesignTokens.labelSmall,
                color = SheetDesignTokens.textMuted
            )
        }
        
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SheetDesignTokens.surfaceGlass)
        ) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Close",
                tint = SheetDesignTokens.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SheetDesignTokens.surfaceGlass)
            .border(
                width = 1.dp,
                color = SheetDesignTokens.borderGlass,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = SheetDesignTokens.textMuted,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Tìm kiếm ứng dụng...",
                        style = SheetDesignTokens.bodyMedium,
                        color = SheetDesignTokens.textMuted
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = SheetDesignTokens.bodyMedium.copy(
                        color = SheetDesignTokens.textPrimary
                    ),
                    cursorBrush = SolidColor(SheetDesignTokens.primaryBlue),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.Clear,
                        contentDescription = "Xoá",
                        tint = SheetDesignTokens.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = SheetDesignTokens.primaryBlue,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Đang tải...",
            style = SheetDesignTokens.bodyMedium,
            color = SheetDesignTokens.textMuted,
            modifier = Modifier.alpha(alpha)
        )
    }
}

@Composable
private fun EmptySearchContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SheetDesignTokens.surfaceGlassElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.SearchOff,
                contentDescription = null,
                tint = SheetDesignTokens.textMuted,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Không tìm thấy",
            style = SheetDesignTokens.bodyLarge,
            color = SheetDesignTokens.textSecondary
        )
        
        Text(
            text = "Thử tìm với từ khác",
            style = SheetDesignTokens.labelSmall,
            color = SheetDesignTokens.textMuted
        )
    }
}

@Composable
private fun AppList(
    apps: List<InstalledApp>,
    onBlockApp: (InstalledApp) -> Unit,
    onUnblockApp: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        
        val blockedApps = apps.filter { it.isBlocked }
        val unblockedApps = apps.filter { !it.isBlocked }
        
        if (blockedApps.isNotEmpty()) {
            item {
                Text(
                    text = "ĐANG CHẶN",
                    style = SheetDesignTokens.labelSmall,
                    color = SheetDesignTokens.accentRed,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(blockedApps) { app ->
                AppRow(
                    app = app,
                    onToggle = { onUnblockApp(app.packageName) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
        
        if (unblockedApps.isNotEmpty()) {
            item {
                Text(
                    text = "CÓ THỂ CHẶN",
                    style = SheetDesignTokens.labelSmall,
                    color = SheetDesignTokens.textMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(unblockedApps) { app ->
                AppRow(
                    app = app,
                    onToggle = { onBlockApp(app) }
                )
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SheetDesignTokens.surfaceGlass)
            .border(
                width = 1.dp,
                color = if (app.isBlocked) 
                    SheetDesignTokens.accentRed.copy(alpha = 0.3f) 
                else 
                    SheetDesignTokens.borderGlass,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (app.isBlocked)
                        SheetDesignTokens.accentRed.copy(alpha = 0.15f)
                    else
                        SheetDesignTokens.surfaceGlassElevated
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.appName.firstOrNull()?.uppercase() ?: "?",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (app.isBlocked)
                    SheetDesignTokens.accentRed
                else
                    SheetDesignTokens.textSecondary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = SheetDesignTokens.bodyLarge,
                color = SheetDesignTokens.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName.split(".").takeLast(2).joinToString("."),
                style = SheetDesignTokens.labelSmall,
                color = SheetDesignTokens.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (app.isBlocked)
                        SheetDesignTokens.accentRed.copy(alpha = 0.2f)
                    else
                        SheetDesignTokens.accentGreen.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (app.isBlocked)
                    Icons.Outlined.RemoveCircle
                else
                    Icons.Outlined.AddCircle,
                contentDescription = if (app.isBlocked) "Bỏ chặn" else "Chặn",
                tint = if (app.isBlocked)
                    SheetDesignTokens.accentRed
                else
                    SheetDesignTokens.accentGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

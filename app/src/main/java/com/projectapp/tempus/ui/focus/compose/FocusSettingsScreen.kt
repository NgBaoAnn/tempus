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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.data.focus.BlockedAppEntity
import com.projectapp.tempus.ui.focus.FocusUiState
import com.projectapp.tempus.ui.focus.InstalledApp

// ===== DESIGN TOKENS =====
private object SettingsDesignTokens {
    // Colors - Light Mode
    val backgroundDark = Color(0xFFF8FAFC)          // Light gray
    val backgroundGradientStart = Color(0xFFFFFFFF)  // White
    val backgroundGradientEnd = Color(0xFFF1F5F9)    // Slate 100
    
    val surfaceGlass = Color(0xFF0F172A).copy(alpha = 0.03f)
    val surfaceGlassElevated = Color(0xFF0F172A).copy(alpha = 0.05f)
    val borderGlass = Color(0xFF0F172A).copy(alpha = 0.08f)
    
    val primaryBlue = Color(0xFF3B82F6)
    val primaryBlueGlow = Color(0xFF60A5FA)
    val accentGreen = Color(0xFF10B981)
    val accentOrange = Color(0xFFF97316)
    val accentRed = Color(0xFFEF4444)
    
    val textPrimary = Color(0xFF0F172A)              // Slate 900
    val textSecondary = Color(0xFF475569)            // Slate 600
    val textMuted = Color(0xFF94A3B8)                // Slate 400
    
    // Typography
    val headlineLarge = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )
    val headlineMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
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

/**
 * Premium Focus Mode Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSettingsScreen(
    uiState: FocusUiState,
    onBackClick: () -> Unit,
    onToggleFocusMode: (Boolean) -> Unit,
    onToggleAutoStart: (Boolean) -> Unit,
    onToggleShowOverlay: (Boolean) -> Unit,
    onShowAppPicker: () -> Unit,
    onUnblockApp: (String) -> Unit,
    onRequestUsagePermission: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SettingsDesignTokens.backgroundGradientStart,
                        SettingsDesignTokens.backgroundGradientEnd,
                        SettingsDesignTokens.backgroundDark
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                PremiumHeader(onBackClick = onBackClick)
            }
            
            // Stats Card
            item {
                StatsCard(
                    totalMinutes = uiState.totalFocusTime,
                    blockedAttempts = uiState.blockedAttempts,
                    isEnabled = uiState.focusModeEnabled
                )
            }
            
            // Permissions Card
            if (!uiState.hasUsagePermission || !uiState.hasOverlayPermission) {
                item {
                    PermissionsCard(
                        hasUsagePermission = uiState.hasUsagePermission,
                        hasOverlayPermission = uiState.hasOverlayPermission,
                        onRequestUsagePermission = onRequestUsagePermission,
                        onRequestOverlayPermission = onRequestOverlayPermission
                    )
                }
            }
            
            // Settings Section
            item {
                SectionTitle(text = "Cài đặt")
            }
            
            item {
                SettingsCard(
                    focusModeEnabled = uiState.focusModeEnabled,
                    autoStartWithTimer = uiState.autoStartWithTimer,
                    showOverlay = uiState.showOverlay,
                    onToggleFocusMode = onToggleFocusMode,
                    onToggleAutoStart = onToggleAutoStart,
                    onToggleShowOverlay = onToggleShowOverlay
                )
            }
            
            // Blocked Apps Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(text = "Ứng dụng bị chặn")
                    Text(
                        text = "${uiState.blockedApps.size} ứng dụng",
                        style = SettingsDesignTokens.labelSmall,
                        color = SettingsDesignTokens.textMuted
                    )
                }
            }
            
            // Add App Button
            item {
                AddAppButton(onClick = onShowAppPicker)
            }
            
            // Blocked Apps List
            if (uiState.blockedApps.isEmpty()) {
                item {
                    EmptyAppsCard()
                }
            } else {
                items(uiState.blockedApps) { app ->
                    BlockedAppCard(
                        app = app,
                        onRemove = { onUnblockApp(app.packageName) }
                    )
                }
            }
        }
    }
}

// ===== COMPONENTS =====

@Composable
private fun PremiumHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SettingsDesignTokens.surfaceGlass)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = SettingsDesignTokens.textPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = "Chế độ Tập trung",
                style = SettingsDesignTokens.headlineLarge,
                color = SettingsDesignTokens.textPrimary
            )
            Text(
                text = "Chặn phân tâm, tăng hiệu suất",
                style = SettingsDesignTokens.bodyMedium,
                color = SettingsDesignTokens.textMuted
            )
        }
    }
}

@Composable
private fun StatsCard(
    totalMinutes: Long,
    blockedAttempts: Int,
    isEnabled: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = formatFocusTime(totalMinutes),
                label = "Thời gian",
                icon = Icons.Outlined.Timer,
                color = SettingsDesignTokens.primaryBlue
            )
            
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(SettingsDesignTokens.borderGlass)
            )
            
            StatItem(
                value = blockedAttempts.toString(),
                label = "Đã chặn",
                icon = Icons.Outlined.Block,
                color = SettingsDesignTokens.accentRed
            )
            
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(SettingsDesignTokens.borderGlass)
            )
            
            StatItem(
                value = if (isEnabled) "BẬT" else "TẮT",
                label = "Trạng thái",
                icon = Icons.Outlined.PowerSettingsNew,
                color = if (isEnabled) SettingsDesignTokens.accentGreen else SettingsDesignTokens.textMuted,
                showPulse = isEnabled,
                pulseAlpha = glowAlpha
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    showPulse: Boolean = false,
    pulseAlpha: Float = 1f
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (showPulse) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .alpha(pulseAlpha)
                        .blur(8.dp)
                        .background(color.copy(alpha = 0.5f), CircleShape)
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = SettingsDesignTokens.headlineMedium,
            color = SettingsDesignTokens.textPrimary
        )
        
        Text(
            text = label,
            style = SettingsDesignTokens.labelSmall,
            color = SettingsDesignTokens.textMuted
        )
    }
}

@Composable
private fun PermissionsCard(
    hasUsagePermission: Boolean,
    hasOverlayPermission: Boolean,
    onRequestUsagePermission: () -> Unit,
    onRequestOverlayPermission: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = SettingsDesignTokens.accentOrange.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SettingsDesignTokens.accentOrange.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = SettingsDesignTokens.accentOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Cần cấp quyền",
                    style = SettingsDesignTokens.bodyLarge,
                    color = SettingsDesignTokens.accentOrange
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!hasUsagePermission) {
                PermissionRow(
                    icon = Icons.Outlined.Visibility,
                    title = "Quyền truy cập dữ liệu",
                    description = "Phát hiện ứng dụng đang chạy",
                    onClick = onRequestUsagePermission
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (!hasOverlayPermission) {
                PermissionRow(
                    icon = Icons.Outlined.Layers,
                    title = "Quyền hiển thị trên ứng dụng",
                    description = "Hiện màn hình khoá",
                    onClick = onRequestOverlayPermission
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SettingsDesignTokens.surfaceGlass)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SettingsDesignTokens.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SettingsDesignTokens.bodyMedium,
                color = SettingsDesignTokens.textPrimary
            )
            Text(
                text = description,
                style = SettingsDesignTokens.labelSmall,
                color = SettingsDesignTokens.textMuted
            )
        }
        
        Icon(
            Icons.Outlined.OpenInNew,
            contentDescription = "Mở",
            tint = SettingsDesignTokens.primaryBlue,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsCard(
    focusModeEnabled: Boolean,
    autoStartWithTimer: Boolean,
    showOverlay: Boolean,
    onToggleFocusMode: (Boolean) -> Unit,
    onToggleAutoStart: (Boolean) -> Unit,
    onToggleShowOverlay: (Boolean) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(4.dp)) {
            SettingToggleRow(
                icon = Icons.Outlined.CenterFocusStrong,
                title = "Bật Chế độ Tập trung",
                description = "Chặn ứng dụng gây phân tâm",
                checked = focusModeEnabled,
                onCheckedChange = onToggleFocusMode,
                accentColor = SettingsDesignTokens.accentGreen
            )
            
            SettingsDivider()
            
            SettingToggleRow(
                icon = Icons.Outlined.PlayCircle,
                title = "Tự động bật cùng Timer",
                description = "Kích hoạt khi bắt đầu đếm giờ",
                checked = autoStartWithTimer,
                onCheckedChange = onToggleAutoStart,
                enabled = focusModeEnabled,
                accentColor = SettingsDesignTokens.primaryBlue
            )
            
            SettingsDivider()
            
            SettingToggleRow(
                icon = Icons.Outlined.Fullscreen,
                title = "Hiện màn hình khoá",
                description = "Màn hình tập trung toàn màn hình",
                checked = showOverlay,
                onCheckedChange = onToggleShowOverlay,
                enabled = focusModeEnabled,
                accentColor = SettingsDesignTokens.primaryBlue
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (checked && enabled) accentColor.copy(alpha = 0.2f)
                    else SettingsDesignTokens.surfaceGlass
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked && enabled) accentColor else SettingsDesignTokens.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SettingsDesignTokens.bodyLarge,
                color = SettingsDesignTokens.textPrimary
            )
            Text(
                text = description,
                style = SettingsDesignTokens.labelSmall,
                color = SettingsDesignTokens.textMuted
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = SettingsDesignTokens.textMuted,
                uncheckedTrackColor = SettingsDesignTokens.surfaceGlass
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = SettingsDesignTokens.borderGlass
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = SettingsDesignTokens.headlineMedium,
        color = SettingsDesignTokens.textPrimary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun AddAppButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = SettingsDesignTokens.primaryBlue.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(SettingsDesignTokens.primaryBlue.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = null,
            tint = SettingsDesignTokens.primaryBlue
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Thêm ứng dụng cần chặn",
            style = SettingsDesignTokens.bodyLarge,
            color = SettingsDesignTokens.primaryBlue
        )
    }
}

@Composable
private fun EmptyAppsCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SettingsDesignTokens.surfaceGlassElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AppBlocking,
                    contentDescription = null,
                    tint = SettingsDesignTokens.textMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Chưa có ứng dụng bị chặn",
                style = SettingsDesignTokens.bodyLarge,
                color = SettingsDesignTokens.textSecondary
            )
            
            Text(
                text = "Thêm ứng dụng gây phân tâm để tập trung",
                style = SettingsDesignTokens.labelSmall,
                color = SettingsDesignTokens.textMuted
            )
        }
    }
}

@Composable
private fun BlockedAppCard(
    app: BlockedAppEntity,
    onRemove: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SettingsDesignTokens.accentRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Block,
                    contentDescription = null,
                    tint = SettingsDesignTokens.accentRed,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = SettingsDesignTokens.bodyLarge,
                    color = SettingsDesignTokens.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName.split(".").lastOrNull() ?: "",
                    style = SettingsDesignTokens.labelSmall,
                    color = SettingsDesignTokens.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SettingsDesignTokens.surfaceGlass)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Xoá",
                    tint = SettingsDesignTokens.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ===== SHARED COMPONENTS =====

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SettingsDesignTokens.borderGlass,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        color = SettingsDesignTokens.surfaceGlass,
        shape = RoundedCornerShape(20.dp)
    ) {
        content()
    }
}

// ===== UTILITIES =====

private fun formatFocusTime(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins}m"
    }
}

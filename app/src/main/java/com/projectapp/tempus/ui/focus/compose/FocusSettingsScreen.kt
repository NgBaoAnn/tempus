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


import androidx.compose.ui.res.stringResource
import com.projectapp.tempus.R


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
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
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
            
            item {
                PremiumHeader(onBackClick = onBackClick)
            }
            
            
            item {
                StatsCard(
                    totalMinutes = uiState.totalFocusTime,
                    blockedAttempts = uiState.blockedAttempts,
                    isEnabled = uiState.focusModeEnabled
                )
            }
            
            
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
            
            
            item {
                SectionTitle(text = stringResource(R.string.nav_settings))
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
            
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(text = stringResource(R.string.focus_blocked_apps_title))
                    Text(
                        text = "${uiState.blockedApps.size} ${stringResource(R.string.focus_apps_count_suffix)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            
            item {
                AddAppButton(onClick = onShowAppPicker)
            }
            
            
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
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = stringResource(R.string.focus_settings_title_screen),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.focus_settings_desc_screen),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                label = stringResource(R.string.focus_stats_total_time),
                icon = Icons.Outlined.Timer,
                color = MaterialTheme.colorScheme.primary
            )
            
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
            
            StatItem(
                value = blockedAttempts.toString(),
                label = stringResource(R.string.focus_stats_blocked_attempts),
                icon = Icons.Outlined.Block,
                color = MaterialTheme.colorScheme.error
            )
            
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
            
            StatItem(
                value = if (isEnabled) stringResource(R.string.focus_status_on) else stringResource(R.string.focus_status_off),
                label = stringResource(R.string.focus_stats_status),
                icon = Icons.Outlined.PowerSettingsNew,
                color = if (isEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant, 
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
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f) 
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = stringResource(R.string.focus_permissions_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!hasUsagePermission) {
                PermissionRow(
                    icon = Icons.Outlined.Visibility,
                    title = stringResource(R.string.focus_permission_usage_title),
                    description = stringResource(R.string.focus_permission_usage_desc),
                    onClick = onRequestUsagePermission
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (!hasOverlayPermission) {
                PermissionRow(
                    icon = Icons.Outlined.Layers,
                    title = stringResource(R.string.focus_permission_overlay_title),
                    description = stringResource(R.string.focus_permission_overlay_desc),
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            Icons.Outlined.OpenInNew,
            contentDescription = stringResource(R.string.content_desc_open),
            tint = MaterialTheme.colorScheme.primary,
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
                title = stringResource(R.string.focus_mode_enable),
                description = stringResource(R.string.focus_mode_enable_desc),
                checked = focusModeEnabled,
                onCheckedChange = onToggleFocusMode,
                accentColor = Color(0xFF10B981) 
            )
            
            SettingsDivider()
            
            SettingToggleRow(
                icon = Icons.Outlined.PlayCircle,
                title = stringResource(R.string.focus_auto_start),
                description = stringResource(R.string.focus_auto_start_desc),
                checked = autoStartWithTimer,
                onCheckedChange = onToggleAutoStart,
                enabled = focusModeEnabled,
                accentColor = MaterialTheme.colorScheme.primary
            )
            
            SettingsDivider()
            
            SettingToggleRow(
                icon = Icons.Outlined.Fullscreen,
                title = stringResource(R.string.focus_show_overlay),
                description = stringResource(R.string.focus_show_overlay_desc),
                checked = showOverlay,
                onCheckedChange = onToggleShowOverlay,
                enabled = focusModeEnabled,
                accentColor = MaterialTheme.colorScheme.primary
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
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked && enabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.focus_add_app_button),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AppBlocking,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.focus_no_apps_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = stringResource(R.string.focus_no_apps_desc),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName.split(".").lastOrNull() ?: "",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), 
        shape = RoundedCornerShape(20.dp)
    ) {
        content()
    }
}


private fun formatFocusTime(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins}m"
    }
}

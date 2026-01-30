package com.projectapp.tempus.ui.setting.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R
import com.projectapp.tempus.ui.components.TempusCard
import com.projectapp.tempus.ui.theme.TempusDesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    timerEnabled: Boolean,
    timelineEnabled: Boolean,
    onTimerToggle: (Boolean) -> Unit,
    onTimelineToggle: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_item_notifications)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            // Section Header
            Text(
                text = "TÙY CHỈNH THÔNG BÁO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
            )

            TempusCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface,
                elevation = 0.dp
            ) {
                Column {
                    // Timer Toggle
                    NotificationSwitchItem(
                        icon = Icons.Outlined.Timer,
                        iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Hẹn giờ", // Timer
                        subtitle = "Hiển thị thông báo khi hẹn giờ đang chạy",
                        checked = timerEnabled,
                        onCheckedChange = onTimerToggle
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Timeline Toggle
                    NotificationSwitchItem(
                        icon = Icons.Outlined.ViewDay, // Timeline icon
                        iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "Dòng thời gian", // Timeline
                        subtitle = "Nhắc nhở công việc theo lịch trình",
                        checked = timelineEnabled,
                        onCheckedChange = onTimelineToggle
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationSwitchItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

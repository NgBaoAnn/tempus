package com.projectapp.tempus.ui.setting.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage

import com.projectapp.tempus.ui.theme.TempusDesignSystem
import com.projectapp.tempus.ui.components.TempusCard

/**
 * User information data class for SettingsScreen
 */
data class UserInfo(
    val name: String = "",
    val email: String = "",
    val avatar: String? = null
)
/**
 * Modern Settings Screen - iOS-inspired design
 */
@Composable
fun SettingsScreen(
    userInfo: UserInfo,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onPersonalizationClick: () -> Unit,
    onThemeClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onExportJsonClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onDeleteDataClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Cài đặt",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // Sections container with padding
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Profile Card
            ProfileCard(
                userName = userInfo.name.ifEmpty { "Người dùng" },
                userEmail = userInfo.email.ifEmpty { "user@email.com" },
                avatarUrl = userInfo.avatar,
                onClick = onProfileClick
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section: CHUNG
            SectionHeader(text = "CHUNG")
            
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Default.Settings,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Giao diện",
                    onClick = onThemeClick
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    iconBgColor = TempusDesignSystem.WarningLight,
                    iconTint = TempusDesignSystem.Warning,
                    title = "Thông báo",
                    onClick = onNotificationsClick
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Outlined.Face,
                    iconBgColor = TempusDesignSystem.AccentLight,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "Cá nhân hóa",
                    onClick = onPersonalizationClick
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Outlined.Settings,
                    iconBgColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = TempusDesignSystem.TextMuted,
                    title = "Nâng cao",
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section: DỮ LIỆU & QUYỀN RIÊNG TƯ
            SectionHeader(text = "DỮ LIỆU & QUYỀN RIÊNG TƯ")
            
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Đồng bộ dữ liệu",
                    onClick = onExportJsonClick
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Delete,
                    iconBgColor = TempusDesignSystem.ErrorLight,
                    iconTint = TempusDesignSystem.Error,
                    title = "Xóa dữ liệu",
                    onClick = onDeleteDataClick
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section: HỖ TRỢ
            SectionHeader(text = "HỖ TRỢ")
            
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Outlined.Email,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Liên hệ",
                    onClick = {}
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Send,
                    iconBgColor = TempusDesignSystem.WarningLight,
                    iconTint = TempusDesignSystem.Warning,
                    title = "Gửi phản hồi",
                    onClick = {}
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Outlined.Star,
                    iconBgColor = TempusDesignSystem.WarningLight,
                    iconTint = TempusDesignSystem.Warning,
                    title = "Viết đánh giá ứng dụng",
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section: PHÁP LÝ
            SectionHeader(text = "PHÁP LÝ")
            
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    iconBgColor = TempusDesignSystem.AccentLight,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "Chính sách quyền riêng tư",
                    onClick = onPrivacyClick
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Info,
                    iconBgColor = TempusDesignSystem.SuccessLight,
                    iconTint = TempusDesignSystem.Success,
                    title = "Điều khoản dịch vụ",
                    onClick = onTermsClick
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section: THÔNG TIN
            SectionHeader(text = "THÔNG TIN")
            
            SettingsGroup {
                SettingsItemValue(
                    icon = Icons.Outlined.Info,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Phiên bản",
                    value = "1.0.0"
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Logout Button
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Section header with uppercase styling
 */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
    )
}

/**
 * Settings group container - iOS-style card
 */
@Composable
private fun SettingsGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    TempusCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        elevation = 0.dp
    ) {
        Column {
            content()
        }
    }
}

/**
 * Settings item with icon badge
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container - rounded square
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
        
        // Title
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        
        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = TempusDesignSystem.TextMuted
        )
    }
}

/**
 * Settings item with value (no chevron)
 */
@Composable
private fun SettingsItemValue(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp),
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
        
        // Title
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        
        // Value
        Text(
            text = value,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Divider with indent
 */
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}

/**
 * Profile Card with user info
 */
@Composable
private fun ProfileCard(
    userName: String,
    userEmail: String,
    avatarUrl: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person),
                    fallback = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person),
                    placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
        }
    }
}

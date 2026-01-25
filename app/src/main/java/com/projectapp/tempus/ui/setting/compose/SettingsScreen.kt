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
import com.projectapp.tempus.R

data class UserInfo(
    val name: String = "",
    val email: String = ""
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
            .background(SettingsColors.Background)
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
                color = SettingsColors.TextPrimary
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
                onClick = onProfileClick
            )
            
            Spacer(modifier = Modifier.height(SettingsDimens.SectionSpacing))
            // Section: CHUNG
            SectionHeader(text = "CHUNG")
            
            SettingsGroup {
                SettingsItem(
                icon = Icons.Default.Settings,
                iconBgColor = SettingsColors.IconBgBlue,
                iconTint = SettingsColors.IconBlue,
                title = "Giao diện",
                onClick = onThemeClick
            )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    iconBgColor = SettingsColors.IconBgOrange,
                    iconTint = SettingsColors.IconOrange,
                    title = "Thông báo",
                    onClick = onNotificationsClick
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Outlined.Face,
                    iconBgColor = SettingsColors.IconBgPurple,
                    iconTint = SettingsColors.IconPurple,
                    title = "Cá nhân hóa",
                    onClick = onPersonalizationClick
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Outlined.Settings,
                    iconBgColor = SettingsColors.IconBgGray,
                    iconTint = SettingsColors.IconGray,
                    title = "Nâng cao",
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(SettingsDimens.SectionSpacing))
            
            // Section: DỮ LIỆU & QUYỀN RIÊNG TƯ
            SectionHeader(text = "DỮ LIỆU & QUYỀN RIÊNG TƯ")
            
            SettingsGroup {
                SettingsItem(
                icon = Icons.Default.Refresh,
                iconBgColor = SettingsColors.IconBgCyan,
                iconTint = SettingsColors.IconCyan,
                title = "Đồng bộ dữ liệu",
                onClick = onExportJsonClick
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Delete,
                iconBgColor = SettingsColors.IconBgPink,
                iconTint = SettingsColors.IconPink,
                title = "Xóa dữ liệu",
                onClick = onDeleteDataClick
            )
            }
            
            Spacer(modifier = Modifier.height(SettingsDimens.SectionSpacing))
            
            // Section: HỖ TRỢ
            SectionHeader(text = "HỖ TRỢ")
            
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Outlined.Email,
                    iconBgColor = SettingsColors.IconBgBlue,
                    iconTint = SettingsColors.IconBlue,
                    title = "Liên hệ",
                    onClick = {}
                )
                SettingsDivider()
                SettingsItem(
                icon = Icons.Default.Send,
                iconBgColor = SettingsColors.IconBgOrange,
                iconTint = SettingsColors.IconOrange,
                title = "Gửi phản hồi",
                onClick = {}
            )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Outlined.Star,
                    iconBgColor = SettingsColors.IconBgYellow,
                    iconTint = SettingsColors.IconYellow,
                    title = "Viết đánh giá ứng dụng",
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(SettingsDimens.SectionSpacing))
            
            // Section: PHÁP LÝ
            SectionHeader(text = "PHÁP LÝ")
            
            SettingsGroup {
                SettingsItem(
                icon = Icons.Default.Lock,
                iconBgColor = SettingsColors.IconBgPurple,
                iconTint = SettingsColors.IconPurple,
                title = "Chính sách quyền riêng tư",
                onClick = onPrivacyClick
            )
            SettingsDivider()
            SettingsItem(
                icon = Icons.Default.Info,
                iconBgColor = SettingsColors.IconBgGreen,
                iconTint = SettingsColors.IconGreen,
                title = "Điều khoản dịch vụ",
                onClick = onTermsClick
            )
            }
            
            Spacer(modifier = Modifier.height(SettingsDimens.SectionSpacing))
            
            // Section: THÔNG TIN
            SectionHeader(text = "THÔNG TIN")
            
            SettingsGroup {
                SettingsItemValue(
                    icon = Icons.Outlined.Info,
                    iconBgColor = SettingsColors.IconBgBlue,
                    iconTint = SettingsColors.IconBlue,
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
                    containerColor = SettingsColors.LogoutBackground
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
        color = SettingsColors.TextSecondary,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsDimens.CardCornerRadius),
        color = SettingsColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
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
            .height(SettingsDimens.RowHeight)
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
                .size(SettingsDimens.IconContainerSize)
                .clip(RoundedCornerShape(SettingsDimens.IconCornerRadius))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(SettingsDimens.IconSize),
                tint = iconTint
            )
        }
        
        Spacer(modifier = Modifier.width(SettingsDimens.ItemSpacing))
        
        // Title
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = SettingsColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        
        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(SettingsDimens.ChevronSize),
            tint = SettingsColors.TextMuted
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
            .height(SettingsDimens.RowHeight)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(SettingsDimens.IconContainerSize)
                .clip(RoundedCornerShape(SettingsDimens.IconCornerRadius))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(SettingsDimens.IconSize),
                tint = iconTint
            )
        }
        
        Spacer(modifier = Modifier.width(SettingsDimens.ItemSpacing))
        
        // Title
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = SettingsColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        
        // Value
        Text(
            text = value,
            fontSize = 17.sp,
            color = SettingsColors.TextSecondary
        )
    }
}

/**
 * Divider with indent
 */
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = SettingsDimens.DividerIndent),
        thickness = 0.5.dp,
        color = SettingsColors.Divider
    )
}

/**
 * Profile Card with user info
 */
@Composable
private fun ProfileCard(
    userName: String,
    userEmail: String,
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
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
                    ),
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
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Gray
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

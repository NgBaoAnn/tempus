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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.rememberVectorPainter


data class UserInfo(
    val name: String = "",
    val email: String = "",
    val avatar: String? = null
)

@Composable
fun SettingsScreen(
    userInfo: UserInfo,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onPersonalizationClick: () -> Unit,
    onThemeClick: () -> Unit,
    onPrivacyClick: () -> Unit,
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
            .padding(SettingsDimens.ScreenPadding)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Header
        Text(
            text = "Cài đặt",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = SettingsColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Card
        ProfileCard(
            userName = userInfo.name.ifEmpty { "Tên người dùng" },
            userEmail = userInfo.email.ifEmpty { "user@email.com" },
            avatarUrl = userInfo.avatar,
            onClick = onProfileClick
        )
        
        Spacer(modifier = Modifier.height(SettingsDimens.SectionSpacing))
        
        // Section: CHUNG
        SectionHeader(text = "CHUNG")
        
        SettingsGroup {
            SettingsItemIcon(
                icon = Icons.Filled.Notifications,
                iconBgColor = SettingsColors.IconBgBlue,
                iconTint = SettingsColors.IconBlue,
                title = "Thông báo",
                onClick = onNotificationsClick
            )
            SettingsDivider()
            SettingsItemIcon(
                icon = Icons.Filled.Person,
                iconBgColor = SettingsColors.IconBgPurple,
                iconTint = SettingsColors.IconPurple,
                title = "Cá nhân hóa",
                onClick = onPersonalizationClick
            )
            SettingsDivider()
            SettingsItemIcon(
                icon = Icons.Filled.Settings,
                iconBgColor = SettingsColors.IconBgTeal,
                iconTint = SettingsColors.IconTeal,
                title = "Giao diện",
                onClick = onThemeClick
            )
        }
        
        Spacer(modifier = Modifier.height(SettingsDimens.SectionSpacing))
        
        // Section: DỮ LIỆU & RIÊNG TƯ
        SectionHeader(text = "DỮ LIỆU & RIÊNG TƯ")
        
        SettingsGroup {
            SettingsItemIcon(
                icon = Icons.Filled.Lock,
                iconBgColor = SettingsColors.IconBgGray,
                iconTint = SettingsColors.IconGray,
                title = "Chính sách bảo mật",
                onClick = onPrivacyClick
            )
            SettingsDivider()
            SettingsItemDrawable(
                drawableRes = R.drawable.ic_export,
                iconBgColor = SettingsColors.IconBgBlue,
                iconTint = SettingsColors.IconBlue,
                title = "Xuất dữ liệu",
                subtitle = "Định dạng JSON",
                onClick = onExportJsonClick
            )
            SettingsDivider()
            SettingsItemDrawable(
                drawableRes = R.drawable.ic_export,
                iconBgColor = SettingsColors.IconBgGreen,
                iconTint = SettingsColors.IconGreen,
                title = "Xuất dữ liệu",
                subtitle = "Định dạng CSV",
                onClick = onExportCsvClick
            )
        }
        
        Spacer(modifier = Modifier.height(SettingsDimens.SectionSpacing))
        
        // Section: DANGER ZONE
        SectionHeader(
            text = "VÙNG NGUY HIỂM",
            color = SettingsColors.IconRed
        )
        
        SettingsGroup {
            SettingsItemIcon(
                icon = Icons.Filled.Delete,
                iconBgColor = SettingsColors.IconBgRed,
                iconTint = SettingsColors.IconRed,
                title = "Xóa tất cả dữ liệu",
                titleColor = SettingsColors.IconRed,
                subtitle = "Hành động này không thể hoàn tác",
                showChevron = false,
                onClick = onDeleteDataClick
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Logout Button
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SettingsColors.LogoutBackground
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.ExitToApp,
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
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileCard(
    userName: String,
    userEmail: String,
    avatarUrl: String?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsDimens.ProfileCardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = SettingsDimens.ProfileCardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SettingsColors.ProfileGradient)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(SettingsDimens.AvatarSize)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.Person),
                    fallback = rememberVectorPainter(Icons.Default.Person),
                    placeholder = rememberVectorPainter(Icons.Default.Person)
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
                    color = Color.White.copy(alpha = 0.8f)
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

@Composable
private fun SectionHeader(
    text: String,
    color: Color = SettingsColors.TextSecondary
) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
    )
}

@Composable
private fun SettingsGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsDimens.CardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = SettingsDimens.CardElevation),
        colors = CardDefaults.cardColors(containerColor = SettingsColors.Surface)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsItemIcon(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    titleColor: Color = SettingsColors.TextPrimary,
    subtitle: String? = null,
    showChevron: Boolean = true,
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
        
        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = SettingsColors.TextSecondary
                )
            }
        }
        
        // Chevron
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(SettingsDimens.ChevronSize),
                tint = SettingsColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SettingsItemDrawable(
    drawableRes: Int,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    titleColor: Color = SettingsColors.TextPrimary,
    subtitle: String? = null,
    showChevron: Boolean = true,
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
        // Icon container
        Box(
            modifier = Modifier
                .size(SettingsDimens.IconContainerSize)
                .clip(RoundedCornerShape(SettingsDimens.IconCornerRadius))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(drawableRes),
                contentDescription = null,
                modifier = Modifier.size(SettingsDimens.IconSize),
                colorFilter = ColorFilter.tint(iconTint)
            )
        }
        
        Spacer(modifier = Modifier.width(SettingsDimens.ItemSpacing))
        
        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = SettingsColors.TextSecondary
                )
            }
        }
        
        // Chevron
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(SettingsDimens.ChevronSize),
                tint = SettingsColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = SettingsDimens.DividerIndent),
        thickness = 1.dp,
        color = SettingsColors.Divider
    )
}

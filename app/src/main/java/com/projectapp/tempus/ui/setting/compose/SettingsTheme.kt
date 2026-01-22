package com.projectapp.tempus.ui.setting.compose

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Settings screen color palette
 */
object SettingsColors {
    // Background
    val Background = Color(0xFFF8F8FA)
    val Surface = Color.White
    
    // Profile gradient
    val ProfileGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
    )
    
    // Icon backgrounds
    val IconBgBlue = Color(0xFFEEF4FF)
    val IconBlue = Color(0xFF3B82F6)
    
    val IconBgPurple = Color(0xFFF3E8FF)
    val IconPurple = Color(0xFF8B5CF6)
    
    val IconBgGreen = Color(0xFFECFDF5)
    val IconGreen = Color(0xFF10B981)
    
    val IconBgTeal = Color(0xFFF0FDFA)
    val IconTeal = Color(0xFF14B8A6)
    
    val IconBgGray = Color(0xFFF3F4F6)
    val IconGray = Color(0xFF6B7280)
    
    val IconBgRed = Color(0xFFFEF2F2)
    val IconRed = Color(0xFFEF4444)
    
    // Text
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    
    // Divider
    val Divider = Color(0xFFE5E7EB)
    
    // Logout button
    val LogoutBackground = Color(0xFF1F2937)
}

/**
 * Settings screen dimensions
 */
object SettingsDimens {
    val ScreenPadding = 20.dp
    val CardCornerRadius = 16.dp
    val CardElevation = 2.dp
    val ProfileCardCornerRadius = 20.dp
    val ProfileCardElevation = 4.dp
    val IconContainerSize = 40.dp
    val IconSize = 22.dp
    val IconCornerRadius = 10.dp
    val RowHeight = 64.dp
    val SectionSpacing = 28.dp
    val ItemSpacing = 14.dp
    val DividerIndent = 70.dp
    val AvatarSize = 64.dp
    val AvatarBorderWidth = 3.dp
    val ChevronSize = 20.dp
}

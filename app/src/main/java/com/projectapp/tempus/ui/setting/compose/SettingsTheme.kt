package com.projectapp.tempus.ui.setting.compose

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Modern Settings screen color palette - iOS-inspired
 */
object SettingsColors {
    // Background
    val Background = Color(0xFFF2F2F7)  // iOS system gray 6
    val Surface = Color.White
    
    // Pro/Premium gradient - coral to pink
    val ProGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
    )
    
    // Profile gradient - purple
    val ProfileGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
    )
    
    // Icon backgrounds - soft pastels matching reference
    val IconBgBlue = Color(0xFF007AFF).copy(alpha = 0.12f)
    val IconBlue = Color(0xFF007AFF)
    
    val IconBgPurple = Color(0xFFAF52DE).copy(alpha = 0.12f)
    val IconPurple = Color(0xFFAF52DE)
    
    val IconBgGreen = Color(0xFF34C759).copy(alpha = 0.12f)
    val IconGreen = Color(0xFF34C759)
    
    val IconBgTeal = Color(0xFF5AC8FA).copy(alpha = 0.12f)
    val IconTeal = Color(0xFF5AC8FA)
    
    val IconBgOrange = Color(0xFFFF9500).copy(alpha = 0.12f)
    val IconOrange = Color(0xFFFF9500)
    
    val IconBgPink = Color(0xFFFF2D55).copy(alpha = 0.12f)
    val IconPink = Color(0xFFFF2D55)
    
    val IconBgYellow = Color(0xFFFFCC00).copy(alpha = 0.12f)
    val IconYellow = Color(0xFFFFCC00)
    
    val IconBgGray = Color(0xFF8E8E93).copy(alpha = 0.12f)
    val IconGray = Color(0xFF8E8E93)
    
    val IconBgRed = Color(0xFFFF3B30).copy(alpha = 0.12f)
    val IconRed = Color(0xFFFF3B30)
    
    val IconBgCyan = Color(0xFF32ADE6).copy(alpha = 0.12f)
    val IconCyan = Color(0xFF32ADE6)
    
    // Text
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF8E8E93)
    val TextMuted = Color(0xFFC7C7CC)
    
    // Divider
    val Divider = Color(0xFFC6C6C8)
    
    // Logout button
    val LogoutBackground = Color(0xFF1C1C1E)
}

/**
 * Settings screen dimensions - iOS-inspired
 */
object SettingsDimens {
    val ScreenPadding = 16.dp
    val CardCornerRadius = 12.dp
    val CardElevation = 0.dp
    val ProfileCardCornerRadius = 16.dp
    val ProfileCardElevation = 0.dp
    val IconContainerSize = 32.dp
    val IconSize = 18.dp
    val IconCornerRadius = 8.dp
    val RowHeight = 56.dp
    val SectionSpacing = 24.dp
    val ItemSpacing = 12.dp
    val DividerIndent = 56.dp
    val AvatarSize = 56.dp
    val AvatarBorderWidth = 2.dp
    val ChevronSize = 16.dp
}

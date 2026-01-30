package com.projectapp.tempus.ui.setting.compose

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.ui.theme.TempusDesignSystem


object SettingsColors {
    
    val Background = TempusDesignSystem.Slate50
    val Surface = TempusDesignSystem.White
    
    
    val ProGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
    )
    
    
    val ProfileGradient = Brush.linearGradient(
        colors = listOf(TempusDesignSystem.Primary, TempusDesignSystem.PrimaryLight)
    )
    
    
    val IconBgBlue = TempusDesignSystem.Primary.copy(alpha = 0.12f)
    val IconBlue = TempusDesignSystem.Primary
    
    val IconBgPurple = TempusDesignSystem.Accent.copy(alpha = 0.12f)
    val IconPurple = TempusDesignSystem.Accent
    
    val IconBgGreen = TempusDesignSystem.Success.copy(alpha = 0.12f)
    val IconGreen = TempusDesignSystem.Success
    
    val IconBgTeal = TempusDesignSystem.Secondary.copy(alpha = 0.12f)
    val IconTeal = TempusDesignSystem.Secondary
    
    val IconBgOrange = TempusDesignSystem.Warning.copy(alpha = 0.12f)
    val IconOrange = TempusDesignSystem.Warning
    
    val IconBgPink = TempusDesignSystem.Error.copy(alpha = 0.12f)
    val IconPink = TempusDesignSystem.Error
    
    val IconBgYellow = Color(0xFFFFCC00).copy(alpha = 0.12f)  
    val IconYellow = Color(0xFFFFCC00)
    
    val IconBgGray = TempusDesignSystem.Slate400.copy(alpha = 0.12f)
    val IconGray = TempusDesignSystem.Slate400
    
    val IconBgRed = TempusDesignSystem.Error.copy(alpha = 0.12f)
    val IconRed = TempusDesignSystem.Error
    
    val IconBgCyan = TempusDesignSystem.Secondary.copy(alpha = 0.12f)
    val IconCyan = TempusDesignSystem.Secondary
    
    
    val TextPrimary = TempusDesignSystem.TextPrimary
    val TextSecondary = TempusDesignSystem.TextSecondary
    val TextMuted = TempusDesignSystem.Slate300
    
    
    val Divider = TempusDesignSystem.Slate200
    
    
    val LogoutBackground = TempusDesignSystem.Slate900
}


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

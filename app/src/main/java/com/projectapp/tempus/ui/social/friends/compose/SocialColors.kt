package com.projectapp.tempus.ui.social.friends.compose

import androidx.compose.ui.graphics.Color
import com.projectapp.tempus.ui.theme.TempusDesignSystem

/**
 * Social-specific color palette based on TempusDesignSystem
 */
object SocialColors {
    // Primary colors
    val Primary = TempusDesignSystem.Primary
    val PrimaryLight = TempusDesignSystem.PrimaryLight
    val Secondary = TempusDesignSystem.Secondary
    
    // Gradient colors
    val GradientStart = TempusDesignSystem.Primary
    val GradientMid = TempusDesignSystem.Accent
    val GradientEnd = TempusDesignSystem.Secondary
    
    // Background colors
    val Background = TempusDesignSystem.BackgroundLight
    val CardBackground = TempusDesignSystem.SurfaceLight
    
    // Text colors
    val TextPrimary = TempusDesignSystem.TextPrimary
    val TextSecondary = TempusDesignSystem.TextSecondary
    val TextMuted = TempusDesignSystem.TextMuted
    
    // Functional colors
    val Success = TempusDesignSystem.Success
    val Error = TempusDesignSystem.Error
    val Warning = TempusDesignSystem.Warning
}

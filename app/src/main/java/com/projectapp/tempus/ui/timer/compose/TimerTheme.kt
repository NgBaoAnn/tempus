package com.projectapp.tempus.ui.timer.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R

/**
 * Timer Screen Color Palette
 * Based on Flat Design system - clean, bold colors
 */
object TimerColors {
    // Primary palette
    val Primary = Color(0xFF3B82F6)       // Blue
    val Secondary = Color(0xFF60A5FA)     // Light Blue
    val Accent = Color(0xFFF97316)        // Orange CTA
    
    // Background
    val Background = Color(0xFFF8FAFC)    // Light gray
    val Surface = Color(0xFFFFFFFF)       // White
    val SurfaceVariant = Color(0xFFF2F2F7) // Light gray for buttons
    
    // Text
    val TextPrimary = Color(0xFF1E293B)   // Dark slate
    val TextSecondary = Color(0xFF64748B) // Slate
    val TextMuted = Color(0xFF8E8E93)     // Gray
    
    // Timer colors
    val TimerGreen = Color(0xFF34C759)
    val TimerBlue = Color(0xFF007AFF)
    val TimerOrange = Color(0xFFFF9500)
    val TimerRed = Color(0xFFFF3B30)
    val TimerPurple = Color(0xFFAF52DE)
    val TimerTeal = Color(0xFF00AF91)
    
    // Track
    val TrackBackground = Color(0xFFEBEBF5)
    
    // Status
    val Running = Color(0xFF34C759)
    val Paused = Color(0xFFFF9500)
}

/**
 * Timer screen dimensions
 */
object TimerDimens {
    // Progress ring
    val ProgressSize = 280.dp
    val ProgressTrackWidth = 16.dp
    
    // Time display
    val TimeTextSize = 64.sp
    val TimeTextSizeSmall = 48.sp
    
    // Buttons
    val ControlButtonSize = 80.dp
    val QuickSelectHeight = 44.dp
    val StartButtonHeight = 60.dp
    
    // Card
    val CardCornerRadius = 24.dp
    val CardPadding = 20.dp
    val CardElevation = 2.dp
    
    // Color selector
    val ColorCircleSize = 40.dp
    val ColorCircleSpacing = 12.dp
    
    // Spacing
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 24.dp
    val SpacingXLarge = 40.dp
}

/**
 * Inter font family
 */
val InterFontFamily = FontFamily(
    Font(R.font.inter_medium, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

/**
 * Timer typography styles
 */
object TimerTypography {
    val HeaderLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = TimerColors.TextPrimary
    )
    
    val TimeDisplay = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        color = TimerColors.TextPrimary
    )
    
    val StatusText = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )
    
    val BodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = TimerColors.TextSecondary
    )
    
    val ButtonText = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
    
    val LabelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = TimerColors.TextMuted
    )
}

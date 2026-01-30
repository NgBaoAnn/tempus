package com.projectapp.tempus.ui.timer.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R


object TimerColors {
    
    val Primary = Color(0xFF3B82F6)       
    val Secondary = Color(0xFF60A5FA)     
    val Accent = Color(0xFFF97316)        
    
    
    val Background = Color(0xFFF8FAFC)    
    val Surface = Color(0xFFFFFFFF)       
    val SurfaceVariant = Color(0xFFF2F2F7) 
    
    
    val TextPrimary = Color(0xFF1E293B)   
    val TextSecondary = Color(0xFF64748B) 
    val TextMuted = Color(0xFF8E8E93)     
    
    
    val TimerGreen = Color(0xFF34C759)
    val TimerBlue = Color(0xFF007AFF)
    val TimerOrange = Color(0xFFFF9500)
    val TimerRed = Color(0xFFFF3B30)
    val TimerPurple = Color(0xFFAF52DE)
    val TimerTeal = Color(0xFF00AF91)
    
    
    val TrackBackground = Color(0xFFEBEBF5)
    
    
    val Running = Color(0xFF34C759)
    val Paused = Color(0xFFFF9500)
}


object TimerDimens {
    
    val ProgressSize = 280.dp
    val ProgressTrackWidth = 16.dp
    
    
    val TimeTextSize = 64.sp
    val TimeTextSizeSmall = 48.sp
    
    
    val ControlButtonSize = 80.dp
    val QuickSelectHeight = 44.dp
    val StartButtonHeight = 60.dp
    
    
    val CardCornerRadius = 24.dp
    val CardPadding = 20.dp
    val CardElevation = 2.dp
    
    
    val ColorCircleSize = 40.dp
    val ColorCircleSpacing = 12.dp
    
    
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 24.dp
    val SpacingXLarge = 40.dp
}


val InterFontFamily = FontFamily(
    Font(R.font.inter_medium, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)


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

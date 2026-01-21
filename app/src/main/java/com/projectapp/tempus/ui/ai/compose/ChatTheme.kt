package com.projectapp.tempus.ui.ai.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R

/**
 * Chat Color Palette from UI/UX Pro Max Design System
 * Style: Vibrant & Block-based, Professional
 */
object ChatColors {
    // Primary Palette (Slate)
    val Primary = Color(0xFF0F172A)        // Slate 900
    val Secondary = Color(0xFF334155)      // Slate 700
    val Tertiary = Color(0xFF64748B)       // Slate 500
    
    // Accent (Sky)
    val Accent = Color(0xFF0369A1)         // Sky 700
    val AccentLight = Color(0xFF0EA5E9)    // Sky 500
    
    // Background & Surface
    val Background = Color(0xFFF8FAFC)     // Slate 50
    val Surface = Color(0xFFFFFFFF)        // White
    val SurfaceVariant = Color(0xFFF1F5F9) // Slate 100
    
    // Text
    val TextPrimary = Color(0xFF020617)    // Slate 950
    val TextSecondary = Color(0xFF475569)  // Slate 600
    val TextMuted = Color(0xFF94A3B8)      // Slate 400
    
    // Chat Bubbles
    val UserBubble = Color(0xFF0369A1)     // Sky 700
    val UserBubbleText = Color(0xFFFFFFFF) // White
    val AIBubble = Color(0xFFF1F5F9)       // Slate 100
    val AIBubbleText = Color(0xFF0F172A)   // Slate 900
    
    // Status
    val Online = Color(0xFF22C55E)         // Green 500
    val Typing = Color(0xFFF59E0B)         // Amber 500
    val Error = Color(0xFFEF4444)          // Red 500
    
    // Dark Mode
    val DarkBackground = Color(0xFF0F172A)    // Slate 900
    val DarkSurface = Color(0xFF1E293B)       // Slate 800
    val DarkSurfaceVariant = Color(0xFF334155) // Slate 700
    val DarkUserBubble = Color(0xFF0EA5E9)    // Sky 500
    val DarkAIBubble = Color(0xFF1E293B)      // Slate 800
}

/**
 * Chat specific dimensions and spacing
 */
object ChatDimens {
    val BubbleCornerRadius = 16.dp
    val BubbleSmallCorner = 4.dp
    val BubblePadding = 12.dp
    val MessageSpacing = 8.dp
    val AvatarSize = 36.dp
    val InputHeight = 56.dp
    val SendButtonSize = 44.dp
}

/**
 * Light color scheme for Chat
 */
val ChatLightColorScheme = lightColorScheme(
    primary = ChatColors.Accent,
    onPrimary = Color.White,
    secondary = ChatColors.Secondary,
    onSecondary = Color.White,
    tertiary = ChatColors.Tertiary,
    background = ChatColors.Background,
    onBackground = ChatColors.TextPrimary,
    surface = ChatColors.Surface,
    onSurface = ChatColors.TextPrimary,
    surfaceVariant = ChatColors.SurfaceVariant,
    onSurfaceVariant = ChatColors.TextSecondary,
    error = ChatColors.Error,
    onError = Color.White
)

/**
 * Dark color scheme for Chat
 */
val ChatDarkColorScheme = darkColorScheme(
    primary = ChatColors.AccentLight,
    onPrimary = Color.White,
    secondary = ChatColors.Tertiary,
    onSecondary = Color.White,
    tertiary = ChatColors.TextMuted,
    background = ChatColors.DarkBackground,
    onBackground = Color.White,
    surface = ChatColors.DarkSurface,
    onSurface = Color.White,
    surfaceVariant = ChatColors.DarkSurfaceVariant,
    onSurfaceVariant = ChatColors.TextMuted,
    error = ChatColors.Error,
    onError = Color.White
)

/**
 * Chat Typography
 * Fonts: Poppins for headings, System default for body
 */
val ChatTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

/**
 * Chat Shapes
 */
val ChatShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Chat Theme composable
 */
@Composable
fun ChatTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ChatDarkColorScheme else ChatLightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChatTypography,
        shapes = ChatShapes,
        content = content
    )
}

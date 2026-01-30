package com.projectapp.tempus.ui.ai.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


object ChatColors {
    
    val Primary = Color(0xFF3B82F6)         
    val PrimaryLight = Color(0xFF60A5FA)    
    val PrimaryDark = Color(0xFF1D4ED8)     
    
    
    val Accent = Color(0xFF06B6D4)          
    val AccentLight = Color(0xFF22D3EE)     
    val AccentGlow = Color(0xFF67E8F9)      
    val OnAccent = Color(0xFFFFFFFF)
    
    
    val Background = Color(0xFFF8FAFC)      
    val BackgroundGradientStart = Color(0xFFFFFFFF)
    val BackgroundGradientEnd = Color(0xFFF1F5F9)
    val Surface = Color(0xFFFFFFFF)         
    val SurfaceVariant = Color(0xFFF1F5F9)  
    val SurfaceGlass = Color(0xFF0F172A).copy(alpha = 0.03f)
    val BorderGlass = Color(0xFF0F172A).copy(alpha = 0.08f)
    
    
    val TextPrimary = Color(0xFF0F172A)     
    val TextSecondary = Color(0xFF475569)   
    val TextMuted = Color(0xFF94A3B8)       
    val TextDim = Color(0xFFCBD5E1)         
    
    
    val UserBubble = Color(0xFF3B82F6)      
    val UserBubbleEnd = Color(0xFF60A5FA)   
    val UserBubbleText = Color(0xFFFFFFFF)
    
    val AIBubble = Color(0xFFF1F5F9)        
    val AIBubbleLight = Color(0xFFF8FAFC)   
    val AIBubbleText = Color(0xFF0F172A)    
    val AIBubbleBorder = Color(0xFF0F172A).copy(alpha = 0.06f)
    
    
    val Online = Color(0xFF10B981)          
    val OnlineGlow = Color(0xFF34D399)      
    val Typing = Color(0xFF06B6D4)          
    val TypingGlow = Color(0xFF22D3EE)      
    val Error = Color(0xFFEF4444)           
    val Success = Color(0xFF22C55E)         
    
    
    val GlowPurple = Color(0xFF3B82F6).copy(alpha = 0.15f)  
    val GlowCyan = Color(0xFF06B6D4).copy(alpha = 0.15f)
    val Shimmer = Color(0xFF0F172A).copy(alpha = 0.05f)
    
    
    val InputBackground = Color(0xFFF1F5F9) 
    val InputBorder = Color(0xFFE2E8F0)     
    val InputBorderFocused = Color(0xFF3B82F6)  
    val InputPlaceholder = Color(0xFF94A3B8) 
    
    
    val ModeActive = Color(0xFF3B82F6)      
    val ModeInactive = Color(0xFFE2E8F0)    
    val ModeText = Color(0xFFFFFFFF)
    val ModeTextInactive = Color(0xFF64748B) 
}


object ChatDimens {
    val BubbleCornerRadius = 20.dp
    val BubbleSmallCorner = 6.dp
    val BubblePadding = 14.dp
    val BubblePaddingHorizontal = 16.dp
    val MessageSpacing = 12.dp
    val AvatarSize = 40.dp
    val AvatarSizeSmall = 32.dp
    val InputHeight = 52.dp
    val InputCornerRadius = 26.dp
    val SendButtonSize = 44.dp
    val TopBarHeight = 72.dp
    val ModeToggleHeight = 40.dp
}


val ChatLightColorScheme = lightColorScheme(
    primary = ChatColors.Primary,
    onPrimary = Color.White,
    secondary = ChatColors.Accent,
    onSecondary = Color.White,
    tertiary = ChatColors.PrimaryLight,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = ChatColors.Error,
    onError = Color.White
)


val ChatDarkColorScheme = darkColorScheme(
    primary = ChatColors.Primary,
    onPrimary = Color.White,
    secondary = ChatColors.Accent,
    onSecondary = Color.White,
    tertiary = ChatColors.PrimaryLight,
    background = ChatColors.Background,
    onBackground = ChatColors.TextPrimary,
    surface = ChatColors.Surface,
    onSurface = ChatColors.TextPrimary,
    surfaceVariant = ChatColors.SurfaceVariant,
    onSurfaceVariant = ChatColors.TextSecondary,
    error = ChatColors.Error,
    onError = Color.White
)


val ChatTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
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
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)


val ChatShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)


@Composable
fun ChatTheme(
    darkTheme: Boolean = true,  
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

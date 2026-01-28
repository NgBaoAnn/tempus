package com.projectapp.tempus.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Tempus Design System - Unified Color Palette
 * Based on "Modern Clean" aesthetic: Electric Blue, Slate, White.
 */
object TempusDesignSystem {
    // Brand Colors
    val Primary = Color(0xFF3B82F6)        // Electric Blue
    val PrimaryDark = Color(0xFF1D4ED8)    // Darker Blue for states
    val PrimaryLight = Color(0xFFDBEAFE)   // Light Blue for backgrounds
    
    val Secondary = Color(0xFF14B8A6)      // Teal - Growth/Focus
    val SecondaryLight = Color(0xFFCCFBF1) // Light Teal
    
    val Accent = Color(0xFF60A5FA)         // Light Blue - Premium/AI (was Violet)
    val AccentLight = Color(0xFFDBEAFE)    // Light Blue background
    
    // Functional Colors
    val Success = Color(0xFF10B981)        // Emerald
    val Warning = Color(0xFFF59E0B)        // Amber
    val Error = Color(0xFFF43F5E)          // Rose
    
    // Light variants for functional colors
    val SuccessLight = Color(0xFFD1FAE5)   // Emerald-100
    val WarningLight = Color(0xFFFEF3C7)   // Amber-100
    val ErrorLight = Color(0xFFFEE2E2)     // Rose-100
    
    // Neutral Scale (Slate)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    
    val Slate50 = Color(0xFFF8FAFC)        // App Background (Light)
    val Slate100 = Color(0xFFF1F5F9)       // Cards/Elevated
    val Slate200 = Color(0xFFE2E8F0)       // Borders
    val Slate300 = Color(0xFFCBD5E1)       // Disabled/Placeholders
    val Slate400 = Color(0xFF94A3B8)       // Muted Text
    val Slate500 = Color(0xFF64748B)       // Secondary Text
    val Slate600 = Color(0xFF475569)
    val Slate700 = Color(0xFF334155)
    val Slate800 = Color(0xFF1E293B)
    val Slate900 = Color(0xFF0F172A)       // Primary Text (Light) / Background (Dark)
    
    // Semantic Roles
    val TextPrimary = Slate900
    val TextSecondary = Slate500
    val TextMuted = Slate400
    
    val BackgroundLight = Slate50
    val SurfaceLight = White
    
    val BackgroundDark = Slate900
    val SurfaceDark = Slate800
    
    // ═══════════════════════════════════════════════════════════════════
    // PREMIUM DESIGN TOKENS (Pro Max Upgrade)
    // ═══════════════════════════════════════════════════════════════════
    
    // Gradient Colors
    object Gradients {
        val Primary: List<Color> = listOf(TempusDesignSystem.Primary, Color(0xFF60A5FA))      // Blue gradient
        val Accent: List<Color> = listOf(TempusDesignSystem.Primary, Color(0xFF93C5FD))        // Blue gradient (was Purple)
        val Success: List<Color> = listOf(TempusDesignSystem.Success, Color(0xFF34D399))      // Green gradient
        val Sunset = listOf(Color(0xFFF97316), Color(0xFFFBBF24)) // Orange-Yellow
        val Ocean = listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))  // Sky-Teal
    }
    
    // Glassmorphism
    object Glass {
        val Background = Color.White.copy(alpha = 0.75f)
        val BackgroundDark = Color(0xFF1E293B).copy(alpha = 0.85f)
        val Border = Color.White.copy(alpha = 0.2f)
        val BorderDark = Color.White.copy(alpha = 0.1f)
    }
    
    // Shadows & Elevation
    object Elevation {
        val None = 0f
        val Small = 2f
        val Medium = 6f
        val Large = 12f
        val XLarge = 24f
    }
    
    // Border Radius
    object Radius {
        val Small = 8f
        val Medium = 12f
        val Large = 16f
        val XLarge = 24f
        val Full = 9999f
    }
    
    // Animation Durations (ms)
    object Animation {
        const val Fast = 150
        const val Normal = 300
        const val Slow = 500
        const val VerySlow = 800
    }
}

// Material 3 Color Schemes based on our token system
val TempusLightScheme = lightColorScheme(
    primary = TempusDesignSystem.Primary,
    onPrimary = TempusDesignSystem.White,
    primaryContainer = TempusDesignSystem.PrimaryLight,
    onPrimaryContainer = TempusDesignSystem.PrimaryDark,
    
    secondary = TempusDesignSystem.Secondary,
    onSecondary = TempusDesignSystem.White,
    secondaryContainer = TempusDesignSystem.SecondaryLight,
    onSecondaryContainer = Color(0xFF0F766E),
    
    tertiary = TempusDesignSystem.Accent,
    onTertiary = TempusDesignSystem.White,
    tertiaryContainer = TempusDesignSystem.AccentLight,
    onTertiaryContainer = Color(0xFF1D4ED8),  // Blue 700
    
    background = TempusDesignSystem.BackgroundLight,
    onBackground = TempusDesignSystem.TextPrimary,
    
    surface = TempusDesignSystem.SurfaceLight,
    onSurface = TempusDesignSystem.TextPrimary,
    surfaceVariant = TempusDesignSystem.Slate100,
    onSurfaceVariant = TempusDesignSystem.TextSecondary,
    
    error = TempusDesignSystem.Error,
    onError = TempusDesignSystem.White,
    
    outline = TempusDesignSystem.Slate200
)

val TempusDarkScheme = darkColorScheme(
    primary = TempusDesignSystem.Primary,
    onPrimary = TempusDesignSystem.White,
    primaryContainer = Color(0xFF1E3A8A), // Darker blue container
    onPrimaryContainer = TempusDesignSystem.PrimaryLight,
    
    secondary = TempusDesignSystem.Secondary,
    onSecondary = TempusDesignSystem.White,
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = TempusDesignSystem.SecondaryLight,
    
    tertiary = TempusDesignSystem.Accent,
    onTertiary = TempusDesignSystem.White,
    tertiaryContainer = Color(0xFF1E3A8A),  // Blue 800
    onTertiaryContainer = TempusDesignSystem.AccentLight,
    
    background = TempusDesignSystem.BackgroundDark,
    onBackground = TempusDesignSystem.Slate50,
    
    surface = TempusDesignSystem.SurfaceDark,
    onSurface = TempusDesignSystem.Slate50,
    surfaceVariant = TempusDesignSystem.Slate700,
    onSurfaceVariant = TempusDesignSystem.Slate300,
    
    error = TempusDesignSystem.Error,
    onError = TempusDesignSystem.White,
    
    outline = TempusDesignSystem.Slate600
)

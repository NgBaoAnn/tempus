package com.projectapp.tempus.ui.onboarding

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * LIGHT Onboarding Color System
 * Bright, clean, soft pastel palette
 */
object OnboardingColors {
    // ============ BACKGROUND ============
    val BackgroundLight = Color(0xFFF8FAFC)    // Very light gray
    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FAFC),   // Light
            Color(0xFFEEF2FF),   // Very soft indigo
            Color(0xFFF5F3FF)    // Soft violet tint
        )
    )
    
    // ============ ACCENT GRADIENTS ============
    val Page1Gradient = listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))  // Blue
    val Page2Gradient = listOf(Color(0xFFA78BFA), Color(0xFF8B5CF6))  // Purple
    val Page3Gradient = listOf(Color(0xFF34D399), Color(0xFF10B981))  // Green
    val Page4Gradient = listOf(Color(0xFFFB923C), Color(0xFFF97316))  // Orange
    
    // ============ TEXT ============
    val TextPrimary = Color(0xFF1E293B)        // Slate-800 - Dark
    val TextSecondary = Color(0xFF64748B)      // Slate-500 - Muted
    val TextMuted = Color(0xFF94A3B8)          // Slate-400
    
    // ============ BUTTON ============
    val ButtonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF6366F1),   // Indigo
            Color(0xFF8B5CF6)    // Violet
        )
    )
    val ButtonText = Color.White
    val ButtonSecondary = Color(0xFF6366F1)    // For skip button
    
    // ============ INDICATORS ============
    val IndicatorActive = Color(0xFF6366F1)    // Indigo
    val IndicatorInactive = Color(0xFFCBD5E1)  // Slate-300
    
    // For theme
    val GradientStart = Color(0xFF6366F1)
    val GradientEnd = Color(0xFF8B5CF6)
    val BackgroundDark = Color(0xFF0F172A)
}

package com.projectapp.tempus.ui.onboarding

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.projectapp.tempus.ui.theme.TempusDesignSystem


object OnboardingColors {
    
    val BackgroundLight = TempusDesignSystem.Slate50
    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            TempusDesignSystem.Slate50,
            TempusDesignSystem.PrimaryLight.copy(alpha = 0.3f),
            TempusDesignSystem.Slate50
        )
    )
    
    
    val Page1Gradient = listOf(TempusDesignSystem.PrimaryLight, TempusDesignSystem.Primary)  
    val Page2Gradient = listOf(TempusDesignSystem.Secondary, Color(0xFF0D9488))              
    val Page3Gradient = listOf(Color(0xFF34D399), TempusDesignSystem.Success)                
    val Page4Gradient = listOf(Color(0xFFFB923C), Color(0xFFF97316))                         
    
    
    val TextPrimary = TempusDesignSystem.TextPrimary
    val TextSecondary = TempusDesignSystem.TextSecondary
    val TextMuted = TempusDesignSystem.TextMuted
    
    
    val ButtonGradient = Brush.horizontalGradient(
        colors = listOf(
            TempusDesignSystem.Primary,
            TempusDesignSystem.PrimaryLight
        )
    )
    val ButtonText = Color.White
    val ButtonSecondary = TempusDesignSystem.Primary
    
    
    val IndicatorActive = TempusDesignSystem.Primary
    val IndicatorInactive = TempusDesignSystem.Slate300
    
    
    val GradientStart = TempusDesignSystem.Primary
    val GradientEnd = TempusDesignSystem.PrimaryLight
    val BackgroundDark = TempusDesignSystem.Slate900
}

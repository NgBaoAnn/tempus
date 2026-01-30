package com.projectapp.tempus.ui.heatmap.compose

import androidx.compose.ui.graphics.Color
import com.projectapp.tempus.domain.usecase.HeatLevel
import com.projectapp.tempus.ui.theme.TempusDesignSystem


object HeatmapColors {
    
    
    val NoneLightBg = Color(0xFFEBEDF0)      
    val LowLightBg = Color(0xFFFEE2E2)       
    val MediumLightBg = Color(0xFFFEF3C7)    
    val HighLightBg = Color(0xFFD1FAE5)      
    val ExcellentLightBg = Color(0xFF10B981) 
    
    
    val NoneDarkBg = Color(0xFF374151)       
    val LowDarkBg = Color(0xFF7F1D1D)        
    val MediumDarkBg = Color(0xFF78350F)     
    val HighDarkBg = Color(0xFF064E3B)       
    val ExcellentDarkBg = Color(0xFF059669)  
    
    
    val TextOnNone = Color(0xFF64748B)       
    val TextOnLow = Color(0xFFDC2626)        
    val TextOnMedium = Color(0xFFD97706)     
    val TextOnHigh = Color(0xFF059669)       
    val TextOnExcellent = Color.White
    
    
    fun getBackgroundColor(level: HeatLevel, isDarkMode: Boolean = false): Color {
        return if (isDarkMode) {
            when (level) {
                HeatLevel.NONE -> NoneDarkBg
                HeatLevel.LOW -> LowDarkBg
                HeatLevel.MEDIUM -> MediumDarkBg
                HeatLevel.HIGH -> HighDarkBg
                HeatLevel.EXCELLENT -> ExcellentDarkBg
            }
        } else {
            when (level) {
                HeatLevel.NONE -> NoneLightBg
                HeatLevel.LOW -> LowLightBg
                HeatLevel.MEDIUM -> MediumLightBg
                HeatLevel.HIGH -> HighLightBg
                HeatLevel.EXCELLENT -> ExcellentLightBg
            }
        }
    }
    
    
    fun getTextColor(level: HeatLevel, isDarkMode: Boolean = false): Color {
        
        if (isDarkMode) {
            return when (level) {
                HeatLevel.NONE -> Color(0xFF9CA3AF)      
                HeatLevel.LOW -> Color(0xFFFCA5A5)       
                HeatLevel.MEDIUM -> Color(0xFFFCD34D)    
                HeatLevel.HIGH -> Color(0xFF6EE7B7)      
                HeatLevel.EXCELLENT -> Color.White
            }
        }
        
        return when (level) {
            HeatLevel.NONE -> TextOnNone
            HeatLevel.LOW -> TextOnLow
            HeatLevel.MEDIUM -> TextOnMedium
            HeatLevel.HIGH -> TextOnHigh
            HeatLevel.EXCELLENT -> TextOnExcellent
        }
    }
    
    
    fun getIndicatorColor(level: HeatLevel): Color {
        return when (level) {
            HeatLevel.NONE -> Color(0xFFCBD5E1)      
            HeatLevel.LOW -> Color(0xFFEF4444)       
            HeatLevel.MEDIUM -> Color(0xFFF59E0B)    
            HeatLevel.HIGH -> Color(0xFF10B981)      
            HeatLevel.EXCELLENT -> Color(0xFF059669) 
        }
    }
    
    
    fun getEmoji(level: HeatLevel): String {
        return when (level) {
            HeatLevel.NONE -> "⬜"
            HeatLevel.LOW -> "🔴"
            HeatLevel.MEDIUM -> "🟡"
            HeatLevel.HIGH -> "🟢"
            HeatLevel.EXCELLENT -> "⭐"
        }
    }
    
    
    fun getDescription(level: HeatLevel): String {
        return when (level) {
            HeatLevel.NONE -> "Không có task"
            HeatLevel.LOW -> "Dưới 30%"
            HeatLevel.MEDIUM -> "30-60%"
            HeatLevel.HIGH -> "60-80%"
            HeatLevel.EXCELLENT -> "Trên 80%"
        }
    }
}

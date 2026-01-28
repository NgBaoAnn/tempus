package com.projectapp.tempus.ui.heatmap.compose

import androidx.compose.ui.graphics.Color
import com.projectapp.tempus.domain.usecase.HeatLevel
import com.projectapp.tempus.ui.theme.TempusDesignSystem

/**
 * Color mapping for Productivity Heatmap.
 * Uses a gradient approach: Gray (no tasks) → Red (poor) → Yellow (medium) → Green (excellent)
 */
object HeatmapColors {
    
    // ======================== BACKGROUND COLORS ========================
    
    // Light mode colors
    val NoneLightBg = Color(0xFFEBEDF0)      // Gray - no tasks
    val LowLightBg = Color(0xFFFEE2E2)       // Light Red - poor (<30%)
    val MediumLightBg = Color(0xFFFEF3C7)    // Light Yellow - medium (30-60%)
    val HighLightBg = Color(0xFFD1FAE5)      // Light Green - good (60-80%)
    val ExcellentLightBg = Color(0xFF10B981) // Emerald - excellent (>80%)
    
    // Dark mode colors
    val NoneDarkBg = Color(0xFF374151)       // Dark Gray
    val LowDarkBg = Color(0xFF7F1D1D)        // Dark Red
    val MediumDarkBg = Color(0xFF78350F)     // Dark Amber
    val HighDarkBg = Color(0xFF064E3B)       // Dark Green
    val ExcellentDarkBg = Color(0xFF059669)  // Emerald-600
    
    // ======================== TEXT COLORS ========================
    
    val TextOnNone = Color(0xFF64748B)       // Slate-500
    val TextOnLow = Color(0xFFDC2626)        // Red-600
    val TextOnMedium = Color(0xFFD97706)     // Amber-600
    val TextOnHigh = Color(0xFF059669)       // Emerald-600
    val TextOnExcellent = Color.White
    
    // ======================== HELPER FUNCTIONS ========================
    
    /**
     * Get background color for a heat level.
     * 
     * @param level The heat level
     * @param isDarkMode Whether dark mode is active
     */
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
    
    /**
     * Get text color suitable for the heat level background.
     */
    fun getTextColor(level: HeatLevel, isDarkMode: Boolean = false): Color {
        // In dark mode, use lighter text colors
        if (isDarkMode) {
            return when (level) {
                HeatLevel.NONE -> Color(0xFF9CA3AF)      // Gray-400
                HeatLevel.LOW -> Color(0xFFFCA5A5)       // Red-300
                HeatLevel.MEDIUM -> Color(0xFFFCD34D)    // Amber-300
                HeatLevel.HIGH -> Color(0xFF6EE7B7)      // Emerald-300
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
    
    /**
     * Get indicator dot color for completion status.
     */
    fun getIndicatorColor(level: HeatLevel): Color {
        return when (level) {
            HeatLevel.NONE -> Color(0xFFCBD5E1)      // Slate-300
            HeatLevel.LOW -> Color(0xFFEF4444)       // Red-500
            HeatLevel.MEDIUM -> Color(0xFFF59E0B)    // Amber-500
            HeatLevel.HIGH -> Color(0xFF10B981)      // Emerald-500
            HeatLevel.EXCELLENT -> Color(0xFF059669) // Emerald-600
        }
    }
    
    /**
     * Get emoji indicator for heat level.
     */
    fun getEmoji(level: HeatLevel): String {
        return when (level) {
            HeatLevel.NONE -> "⬜"
            HeatLevel.LOW -> "🔴"
            HeatLevel.MEDIUM -> "🟡"
            HeatLevel.HIGH -> "🟢"
            HeatLevel.EXCELLENT -> "⭐"
        }
    }
    
    /**
     * Get description for heat level.
     */
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

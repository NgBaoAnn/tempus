package com.projectapp.tempus.ui.garden.compose.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tree Drawing Utilities
 * Common utility functions and extensions for tree rendering
 */

// ========== Color Extension Functions ==========

/**
 * Lighten a color by factor (0-1)
 * Example: color.lighten(0.2f) makes it 20% lighter
 */
fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Darken a color by factor (0-1)
 * Example: color.darken(0.2f) makes it 20% darker
 */
fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// ========== Size enum ==========

enum class ProceduralTreeSize(val dp: Dp) {
    SMALL(60.dp),
    MEDIUM(100.dp),
    LARGE(150.dp),
    XLARGE(200.dp)
}

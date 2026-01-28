package com.projectapp.tempus.ui.garden.compose.trees

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.projectapp.tempus.ui.garden.compose.drawing.darken
import com.projectapp.tempus.ui.garden.compose.drawing.lighten
import com.projectapp.tempus.ui.garden.compose.stableRandom

/**
 * Apple Tree Renderer
 * Renders apple trees with large, shiny red fruits
 */

/**
 * Draw a single apple with 3D shine effect
 */
fun DrawScope.drawApple(
    center: Offset,
    size: Float,
    color: Color,
    opacity: Float,
    seed: Int
) {
    val appleColor = color.copy(alpha = opacity)
    val darkColor = color.darken(0.25f).copy(alpha = opacity)
    val highlightColor = color.lighten(0.2f).copy(alpha = opacity)
    
    // Apple shadow
    drawOval(
        color = Color.Black.copy(alpha = opacity * 0.15f),
        topLeft = Offset(center.x - size * 0.4f, center.y + size * 0.25f),
        size = Size(size * 0.8f, size * 0.2f)
    )
    
    // Apple body with gradient
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(highlightColor, appleColor, darkColor),
            center = Offset(center.x - size * 0.15f, center.y - size * 0.15f),
            radius = size * 0.7f
        ),
        topLeft = Offset(center.x - size * 0.45f, center.y - size * 0.45f),
        size = Size(size * 0.9f, size * 0.9f)
    )
    
    // Top indent (apple characteristic shape)
    drawOval(
        color = darkColor,
        topLeft = Offset(center.x - size * 0.12f, center.y - size * 0.48f),
        size = Size(size * 0.24f, size * 0.12f)
    )
    
    // Stem
    drawLine(
        color = Color(0xFF5D4037).copy(alpha = opacity),
        start = Offset(center.x, center.y - size * 0.38f),
        end = Offset(center.x + size * 0.08f, center.y - size * 0.55f),
        strokeWidth = size * 0.06f
    )
    
    // Small leaf on stem
    val leafCenterX = center.x + size * 0.15f
    val leafCenterY = center.y - size * 0.52f
    drawOval(
        color = Color(0xFF4CAF50).copy(alpha = opacity),
        topLeft = Offset(leafCenterX - size * 0.12f, leafCenterY - size * 0.05f),
        size = Size(size * 0.25f, size * 0.1f)
    )
    
    // Shine highlight
    drawOval(
        color = Color.White.copy(alpha = opacity * 0.5f),
        topLeft = Offset(center.x - size * 0.3f, center.y - size * 0.35f),
        size = Size(size * 0.2f, size * 0.15f)
    )
}

/**
 * Draw multiple apples on tree canopy
 */
fun DrawScope.drawApples(
    canopyCenter: Offset,
    canopyRadius: Float,
    count: Int,
    appleSize: Float,
    opacity: Float,
    seed: Int
) {
    // Mix of red and green apples
    val redColor = Color(0xFFD32F2F)
    val greenColor = Color(0xFF689F38)
    
    for (i in 0 until count) {
        val angle = (360f / count) * i + stableRandom(seed, i) * 40f - 20f
        val angleRad = Math.toRadians(angle.toDouble()).toFloat()
        val dist = canopyRadius * (0.3f + stableRandom(seed, i + 10) * 0.5f)
        
        val appleX = canopyCenter.x + kotlin.math.cos(angleRad) * dist
        val appleY = canopyCenter.y + kotlin.math.sin(angleRad) * dist * 0.7f
        
        // 70% red, 30% green
        val isRed = stableRandom(seed, i + 100) > 0.3f
        val color = if (isRed) redColor else greenColor
        
        drawApple(
            center = Offset(appleX, appleY),
            size = appleSize * (0.8f + stableRandom(seed, i + 20) * 0.4f),
            color = color,
            opacity = opacity,
            seed = seed + i
        )
    }
}

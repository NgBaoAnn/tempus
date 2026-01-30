package com.projectapp.tempus.ui.garden.compose.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin


fun DrawScope.drawGroundShadow(
    centerX: Float,
    baseY: Float,
    radius: Float
) {
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = Offset(centerX, baseY + radius * 0.1f),
            radius = radius
        ),
        topLeft = Offset(centerX - radius, baseY - radius * 0.15f),
        size = Size(radius * 2, radius * 0.35f)
    )
}


fun DrawScope.drawCanopyBase(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    color: Color,
    opacity: Float
) {
    
    val baseColor = color.copy(alpha = opacity)
    
    
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                baseColor,
                baseColor.copy(alpha = opacity * 0.8f),
                Color.Transparent
            ),
            center = center,
            radius = radiusX
        ),
        topLeft = Offset(center.x - radiusX, center.y - radiusY),
        size = Size(radiusX * 2, radiusY * 2)
    )
    
    
    drawOval(
        color = baseColor.copy(alpha = opacity * 0.6f),
        topLeft = Offset(center.x - radiusX * 1.1f, center.y - radiusY * 0.6f),
        size = Size(radiusX * 0.8f, radiusY * 0.9f)
    )
    
    
    drawOval(
        color = baseColor.copy(alpha = opacity * 0.6f),
        topLeft = Offset(center.x + radiusX * 0.3f, center.y - radiusY * 0.5f),
        size = Size(radiusX * 0.8f, radiusY * 0.85f)
    )
    
    
    drawOval(
        color = color.lighten(0.15f).copy(alpha = opacity * 0.4f),
        topLeft = Offset(center.x - radiusX * 0.5f, center.y - radiusY * 0.9f),
        size = Size(radiusX * 0.7f, radiusY * 0.4f)
    )
}


fun DrawScope.drawLeafShadow(center: Offset, size: Float) {
    drawOval(
        color = Color.Black.copy(alpha = 0.05f),
        topLeft = Offset(center.x - size * 0.8f, center.y + size * 0.1f),
        size = Size(size * 1.6f, size * 0.4f)
    )
}


fun generateTightLeafPositions(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    count: Int,
    seed: Int
): List<Offset> {
    val positions = mutableListOf<Offset>()
    
    for (i in 0 until count) {
        
        val ring = i / 6  
        val indexInRing = i % 6
        
        val ringRadius = 0.3f + ring * 0.25f  
        val angleOffset = ring * 30f  
        val angle = (indexInRing * 60f + angleOffset + stableRandom(seed, i) * 25f)
        
        val angleRad = Math.toRadians(angle.toDouble()).toFloat()
        val distX = radiusX * ringRadius * (0.7f + stableRandom(seed, i + 100) * 0.3f)
        val distY = radiusY * ringRadius * (0.6f + stableRandom(seed, i + 200) * 0.4f)
        
        positions.add(
            Offset(
                center.x + cos(angleRad) * distX,
                center.y + sin(angleRad) * distY * 0.8f - radiusY * 0.1f
            )
        )
    }
    
    return positions
}


fun stableRandom(seed: Int, index: Int): Float {
    val combined = seed * 31 + index
    return ((combined * 1103515245 + 12345) and 0x7fffffff) / 2147483647f
}


fun stablePhaseOffset(seed: Int, index: Int): Float {
    return stableRandom(seed, index + 1000) * 6.28f 
}

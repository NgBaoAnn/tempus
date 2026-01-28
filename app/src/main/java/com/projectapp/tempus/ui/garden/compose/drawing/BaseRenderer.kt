package com.projectapp.tempus.ui.garden.compose.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin

/**
 * Base Rendering Utilities
 * Common drawing functions for shadows, canopy base, and leaf positioning
 */

/**
 * Ground shadow ellipse dưới cây
 */
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

/**
 * Draw canopy base - solid mass creating cohesive look
 */
fun DrawScope.drawCanopyBase(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    color: Color,
    opacity: Float
) {
    // Multiple overlapping ovals for organic canopy shape
    val baseColor = color.copy(alpha = opacity)
    
    // Main canopy mass
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
    
    // Left bulge
    drawOval(
        color = baseColor.copy(alpha = opacity * 0.6f),
        topLeft = Offset(center.x - radiusX * 1.1f, center.y - radiusY * 0.6f),
        size = Size(radiusX * 0.8f, radiusY * 0.9f)
    )
    
    // Right bulge
    drawOval(
        color = baseColor.copy(alpha = opacity * 0.6f),
        topLeft = Offset(center.x + radiusX * 0.3f, center.y - radiusY * 0.5f),
        size = Size(radiusX * 0.8f, radiusY * 0.85f)
    )
    
    // Top highlight
    drawOval(
        color = color.lighten(0.15f).copy(alpha = opacity * 0.4f),
        topLeft = Offset(center.x - radiusX * 0.5f, center.y - radiusY * 0.9f),
        size = Size(radiusX * 0.7f, radiusY * 0.4f)
    )
}

/**
 * Leaf shadow
 */
fun DrawScope.drawLeafShadow(center: Offset, size: Float) {
    drawOval(
        color = Color.Black.copy(alpha = 0.05f),
        topLeft = Offset(center.x - size * 0.8f, center.y + size * 0.1f),
        size = Size(size * 1.6f, size * 0.4f)
    )
}

/**
 * Generate tightly clustered leaf positions within canopy bounds
 */
fun generateTightLeafPositions(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    count: Int,
    seed: Int
): List<Offset> {
    val positions = mutableListOf<Offset>()
    
    for (i in 0 until count) {
        // Distribute in concentric rings for tight clustering
        val ring = i / 6  // 6 leaves per ring
        val indexInRing = i % 6
        
        val ringRadius = 0.3f + ring * 0.25f  // Start from center, expand outward
        val angleOffset = ring * 30f  // Offset each ring
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

/**
 * Stable random for procedural generation - using seed for deterministic results
 */
fun stableRandom(seed: Int, index: Int): Float {
    val combined = seed * 31 + index
    return ((combined * 1103515245 + 12345) and 0x7fffffff) / 2147483647f
}

/**
 * Stable phase offset for animations
 */
fun stablePhaseOffset(seed: Int, index: Int): Float {
    return stableRandom(seed, index + 1000) * 6.28f // 0 to 2π
}

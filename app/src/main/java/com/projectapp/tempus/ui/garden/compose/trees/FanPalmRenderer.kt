package com.projectapp.tempus.ui.garden.compose.trees

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.projectapp.tempus.ui.garden.compose.drawing.darken
import com.projectapp.tempus.ui.garden.compose.drawing.lighten
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fan Palm Renderer (Cây Cọ)
 * Renders fan-shaped palm leaves with multiple radiating segments
 * Used for PALM tree type with LeafShape.LONG
 */

/**
 * Draw a fan palm leaf cluster - multiple leaves radiating from a single point
 * Creates the distinctive "fan" shape of fan palms with filled leaves
 */
fun DrawScope.drawFanPalmLeaf(
    center: Offset,
    size: Float,
    baseColor: Color,
    opacity: Float,
    sway: Float,  // Wind sway angle
    seed: Int
) {
    val lightColor = baseColor.lighten(0.15f).copy(alpha = opacity)
    val midColor = baseColor.copy(alpha = opacity)
    val darkColor = baseColor.darken(0.2f).copy(alpha = opacity)
    
    val fanCount = 11  // More segments for fuller fan
    val fanSpread = 140f  // Góc tỏa rộng hơn (độ)
    val fanLength = size * 1.5f
    val leafWidth = size * 0.12f  // Width of each segment
    
    // Draw all fan segments as filled shapes
    for (i in 0 until fanCount) {
        val fanProgress = i.toFloat() / (fanCount - 1)
        // Góc từ -70 đến +70 độ (tổng 140 độ) + sway
        val fanAngle = -fanSpread / 2f + fanSpread * fanProgress + sway * 0.5f
        val fanAngleRad = Math.toRadians(fanAngle.toDouble()).toFloat()
        
        // Độ dài dao động - lá giữa dài nhất
        val centerFactor = 1f - abs(fanProgress - 0.5f) * 2f
        val thisLength = fanLength * (0.8f + 0.2f * centerFactor)
        val thisWidth = leafWidth * (0.7f + 0.3f * centerFactor)
        
        // Calculate leaf endpoints going UPWARD
        val endX = center.x + sin(fanAngleRad) * thisLength
        val endY = center.y - cos(fanAngleRad) * thisLength
        
        // Control points for nice curve - arching upward and outward
        val ctrlDist = thisLength * 0.5f
        val ctrlX = center.x + sin(fanAngleRad) * ctrlDist
        val ctrlY = center.y - cos(fanAngleRad) * ctrlDist * 0.7f
        
        // Create filled leaf segment with bezier curves
        val leafPath = Path().apply {
            // Start at center
            moveTo(center.x, center.y)
            
            // Left edge of leaf
            val leftAngle = fanAngleRad - 0.08f
            val leftCtrlX = center.x + sin(leftAngle) * ctrlDist - thisWidth * 0.3f
            val leftCtrlY = center.y - cos(leftAngle) * ctrlDist * 0.6f
            quadraticBezierTo(leftCtrlX, leftCtrlY, endX - thisWidth * 0.2f, endY)
            
            // Leaf tip 
            lineTo(endX + thisWidth * 0.2f, endY)
            
            // Right edge back to center
            val rightAngle = fanAngleRad + 0.08f
            val rightCtrlX = center.x + sin(rightAngle) * ctrlDist + thisWidth * 0.3f
            val rightCtrlY = center.y - cos(rightAngle) * ctrlDist * 0.6f
            quadraticBezierTo(rightCtrlX, rightCtrlY, center.x, center.y)
            
            close()
        }
        
        // Color varies by position for depth
        val leafColor = when {
            i < fanCount / 3 -> darkColor  // Left side darker
            i > fanCount * 2 / 3 -> darkColor  // Right side darker
            i == fanCount / 2 -> lightColor  // Center brightest
            else -> midColor  // Mid sections
        }
        
        drawPath(
            path = leafPath,
            color = leafColor
        )
        
        // Draw center vein line for detail
        drawLine(
            color = darkColor.copy(alpha = opacity * 0.3f),
            start = Offset(center.x, center.y),
            end = Offset(endX, endY),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
    }
    
    // Gốc lá (cuống) - điểm tụ tại đỉnh cây
    drawCircle(
        color = baseColor.darken(0.3f).copy(alpha = opacity),
        radius = size * 0.08f,
        center = center
    )
}

/**
 * Draw the full fan palm crown with multiple fan leaf clusters
 * Creates a layered, natural-looking crown
 */
fun DrawScope.drawFanPalmCrown(
    trunkTop: Offset,
    canvasHeight: Float,
    baseColor: Color,
    opacity: Float,
    scale: Float,
    sway: Float,
    seed: Int
) {
    val crownSize = canvasHeight * 0.22f * scale
    
    // Draw 5 overlapping fan clusters arranged in layers for depth
    // Back layer first (darker, smaller), front layer last (brighter, larger)
    val clusters = listOf(
        // Back layer - darker, offset left/right
        Triple(-0.12f, 0.9f, -8f),   // offsetX ratio, size ratio, extra sway
        Triple(0.12f, 0.9f, 8f),
        // Middle layer
        Triple(-0.05f, 0.95f, -3f),
        Triple(0.05f, 0.95f, 3f),
        // Front layer - main, centered
        Triple(0f, 1f, 0f)
    )
    
    clusters.forEachIndexed { index, (offsetRatio, sizeRatio, extraSway) ->
        val clusterX = trunkTop.x + crownSize * offsetRatio
        val clusterY = trunkTop.y - crownSize * 0.05f * (4 - index)  // Slight vertical offset
        
        // Color varies by layer - back darker, front lighter
        val clusterColor = when {
            index < 2 -> baseColor.darken(0.15f)  // Back layer
            index < 4 -> baseColor                 // Middle layer  
            else -> baseColor.lighten(0.05f)       // Front layer
        }
        
        drawFanPalmLeaf(
            center = Offset(clusterX, clusterY),
            size = crownSize * sizeRatio,
            baseColor = clusterColor,
            opacity = opacity * (0.85f + index * 0.03f),  // Front more opaque
            sway = sway + extraSway,
            seed = seed + index
        )
    }
}

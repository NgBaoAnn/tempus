package com.projectapp.tempus.ui.garden.compose.trees

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.projectapp.tempus.ui.garden.compose.drawing.darken
import com.projectapp.tempus.ui.garden.compose.drawing.lighten
import com.projectapp.tempus.ui.garden.compose.stableRandom
import kotlin.math.cos
import kotlin.math.sin

/**
 * Bamboo Tree Renderer
 * Renders bamboo with segmented culms and shooting leaves
 */

/**
 * Draw a single bamboo stalk with segments (like HTML drawBambooStalk)
 * Used by ProceduralTree for single stalk rendering
 * Returns the top position for attaching leaves
 */
fun DrawScope.drawBambooSingleStalk(
    centerX: Float,
    baseY: Float,
    width: Float,
    height: Float,
    segmentCount: Int,
    baseColor: Color,
    opacity: Float,
    sway: Float,
    seed: Int
): Offset {
    val lightColor = baseColor.lighten(0.20f)
    val darkColor = baseColor.darken(0.15f)
    val segmentHeight = height / segmentCount
    
    rotate(sway, pivot = Offset(centerX, baseY)) {
        for (segIndex in 0 until segmentCount) {
            val segY = baseY - segmentHeight * (segIndex + 1)
            val segWidth = width * (1f - segIndex * 0.05f)
            
            // Segment body with gradient (like HTML demo)
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        darkColor.copy(alpha = opacity),
                        lightColor.copy(alpha = opacity),
                        baseColor.copy(alpha = opacity),
                        baseColor.copy(alpha = opacity),
                        darkColor.copy(alpha = opacity)
                    ),
                    startX = centerX - segWidth / 2,
                    endX = centerX + segWidth / 2
                ),
                topLeft = Offset(centerX - segWidth / 2, segY),
                size = Size(segWidth, segmentHeight * 0.90f),
                cornerRadius = CornerRadius(segWidth / 3)
            )
            
            // Joint ring (node) between segments - like HTML ellipse
            if (segIndex < segmentCount - 1) {
                drawOval(
                    color = darkColor.copy(alpha = opacity),
                    topLeft = Offset(
                        centerX - segWidth * 0.55f / 2,
                        segY + segmentHeight * 0.02f - segmentHeight * 0.06f / 2
                    ),
                    size = Size(segWidth * 0.55f, segmentHeight * 0.06f)
                )
            }
        }
    }
    
    // Return top position (affected by sway)
    val swayRad = Math.toRadians(sway.toDouble()).toFloat()
    val topX = centerX + sin(swayRad) * height
    val topY = baseY - height
    return Offset(topX, topY)
}

/**
 * Draw bamboo culms (main stalks) with segments and nodes
 */
fun DrawScope.drawBambooCulms(
    baseCenter: Offset,
    height: Float,
    width: Float,
    culmCount: Int,
    segmentCount: Int,
    baseColor: Color,
    opacity: Float,
    sway: Float,
    seed: Int
) {
    val culms = listOf(-0.6f, -0.2f, 0.2f, 0.6f).take(culmCount)
    
    culms.forEachIndexed { culmIndex, offsetX ->
        val culmX = baseCenter.x + width * offsetX
        val culmHeight = height * (0.85f + stableRandom(seed, culmIndex) * 0.3f)
        val culmWidth = width * 0.12f * (0.9f + stableRandom(seed, culmIndex + 10) * 0.2f)
        val culmSway = sway * (0.8f + culmIndex * 0.1f)
        
        rotate(culmSway, pivot = Offset(culmX, baseCenter.y)) {
            val segmentHeight = culmHeight / segmentCount
            
            for (segIndex in 0 until segmentCount) {
                val segY = baseCenter.y - segmentHeight * (segIndex + 1)
                val segWidth = culmWidth * (1f - segIndex * 0.05f)
                
                // Segment body
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            baseColor.darken(0.15f).copy(alpha = opacity),
                            baseColor.lighten(0.1f).copy(alpha = opacity),
                            baseColor.copy(alpha = opacity),
                            baseColor.darken(0.1f).copy(alpha = opacity)
                        )
                    ),
                    topLeft = Offset(culmX - segWidth / 2, segY),
                    size = Size(segWidth, segmentHeight * 0.92f),
                    cornerRadius = CornerRadius(segWidth / 4)
                )
                
                // Node ring (between segments)
                if (segIndex < segmentCount - 1) {
                    drawOval(
                        color = baseColor.darken(0.2f).copy(alpha = opacity),
                        topLeft = Offset(culmX - segWidth * 0.6f / 2, segY - segmentHeight * 0.03f),
                        size = Size(segWidth * 0.6f, segmentHeight * 0.06f)
                    )
                    
                    // Leaves shooting from nodes (every 2nd node)
                    if (segIndex % 2 == 0) {
                        val leafSide = if (culmIndex % 2 == 0) 1f else -1f
                        drawBambooLeaf(
                            nodePoint = Offset(culmX + leafSide * segWidth * 0.3f, segY),
                            length = culmWidth * 4f,
                            width = culmWidth * 1.2f,
                            angle = leafSide * (30f + stableRandom(seed, culmIndex + segIndex) * 20f),
                            color = baseColor.lighten(0.1f),
                            opacity = opacity,
                            seed = seed + culmIndex * 10 + segIndex
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draw a single bamboo leaf
 */
fun DrawScope.drawBambooLeaf(
    nodePoint: Offset,
    length: Float,
    width: Float,
    angle: Float,
    color: Color,
    opacity: Float,
    seed: Int
) {
    val angleRad = Math.toRadians(angle.toDouble()).toFloat()
    
    val endX = nodePoint.x + cos(angleRad) * length
    val endY = nodePoint.y - sin(angleRad) * length * 0.5f
    
    val midX = nodePoint.x + cos(angleRad) * length * 0.5f
    val midY = nodePoint.y - sin(angleRad) * length * 0.25f
    
    val leafPath = Path().apply {
        moveTo(nodePoint.x, nodePoint.y)
        quadraticBezierTo(
            midX + sin(angleRad) * width * 0.5f,
            midY - cos(angleRad) * width * 0.5f,
            endX, endY
        )
        quadraticBezierTo(
            midX - sin(angleRad) * width * 0.3f,
            midY + cos(angleRad) * width * 0.3f,
            nodePoint.x, nodePoint.y
        )
        close()
    }
    
    drawPath(
        path = leafPath,
        brush = Brush.linearGradient(
            colors = listOf(
                color.lighten(0.1f).copy(alpha = opacity),
                color.copy(alpha = opacity),
                color.darken(0.1f).copy(alpha = opacity)
            ),
            start = nodePoint,
            end = Offset(endX, endY)
        )
    )
    
    // Center vein
    drawLine(
        color = color.darken(0.15f).copy(alpha = opacity * 0.5f),
        start = nodePoint,
        end = Offset(endX, endY),
        strokeWidth = width * 0.05f,
        cap = StrokeCap.Round
    )
}

/**
 * Draw fan-shaped bamboo leaves at the top of culm (like HTML demo)
 * 7 leaves spreading from -70° to +70° using rotate/translate like HTML
 */
fun DrawScope.drawBambooTopLeaves(
    origin: Offset,
    size: Float,
    baseColor: Color,
    opacity: Float,
    sway: Float,
    seed: Int
) {
    val leafCount = 7
    val lightColor = baseColor.lighten(0.15f)
    val darkColor = baseColor.darken(0.1f)
    
    // Wind sway rotation
    rotate(sway, pivot = origin) {
        for (i in 0 until leafCount) {
            // Spread from -70° to +70° like HTML demo
            val angle = -70f + (140f / (leafCount - 1)) * i
            val leafLength = size * (0.8f + stableRandom(seed, i) * 0.4f)
            val leafWidth = leafLength * 0.15f
            
            // Convert angle to radians (negative because going UP from origin)
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()
            
            // Leaf path in local coordinates (going UP from origin, then rotate)
            // Using bezier curves exactly like HTML demo:
            // ctx.moveTo(0, 0);
            // ctx.bezierCurveTo(leafWidth, -leafLength * 0.3, leafWidth * 0.5, -leafLength * 0.8, 0, -leafLength);
            // ctx.bezierCurveTo(-leafWidth * 0.5, -leafLength * 0.8, -leafWidth, -leafLength * 0.3, 0, 0);
            
            val leafPath = Path().apply {
                // Start at origin
                moveTo(origin.x, origin.y)
                
                // First bezier curve (right side going up)
                // Transform: rotate by angle, then translate by origin
                val cp1X = origin.x + leafWidth * cos(angleRad) - (-leafLength * 0.3f) * sin(angleRad)
                val cp1Y = origin.y + leafWidth * sin(angleRad) + (-leafLength * 0.3f) * cos(angleRad)
                
                val cp2X = origin.x + (leafWidth * 0.5f) * cos(angleRad) - (-leafLength * 0.8f) * sin(angleRad)
                val cp2Y = origin.y + (leafWidth * 0.5f) * sin(angleRad) + (-leafLength * 0.8f) * cos(angleRad)
                
                val endX = origin.x + 0f * cos(angleRad) - (-leafLength) * sin(angleRad)
                val endY = origin.y + 0f * sin(angleRad) + (-leafLength) * cos(angleRad)
                
                cubicTo(cp1X, cp1Y, cp2X, cp2Y, endX, endY)
                
                // Second bezier curve (left side coming down)
                val cp3X = origin.x + (-leafWidth * 0.5f) * cos(angleRad) - (-leafLength * 0.8f) * sin(angleRad)
                val cp3Y = origin.y + (-leafWidth * 0.5f) * sin(angleRad) + (-leafLength * 0.8f) * cos(angleRad)
                
                val cp4X = origin.x + (-leafWidth) * cos(angleRad) - (-leafLength * 0.3f) * sin(angleRad)
                val cp4Y = origin.y + (-leafWidth) * sin(angleRad) + (-leafLength * 0.3f) * cos(angleRad)
                
                cubicTo(cp3X, cp3Y, cp4X, cp4Y, origin.x, origin.y)
                
                close()
            }
            
            // Choose color based on position for depth effect (like HTML: i % 2, i % 3)
            val leafColor = when {
                i % 2 == 0 -> baseColor
                i % 3 == 0 -> lightColor
                else -> darkColor
            }
            
            // Fill leaf
            drawPath(
                path = leafPath,
                color = leafColor.copy(alpha = opacity * (0.85f + stableRandom(seed, i + 100) * 0.15f))
            )
        }
    }
}

package com.projectapp.tempus.ui.garden.compose.drawing

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.projectapp.tempus.ui.garden.compose.TrunkConfig
import com.projectapp.tempus.ui.garden.compose.stableRandom
import kotlin.math.cos
import kotlin.math.sin

/**
 * Trunk Renderer
 * Contains functions for rendering tree trunks with various styles
 */

/**
 * Draws an organic trunk with bezier curves and 3D gradient effect
 * Returns the position of the trunk top for attaching branches
 */
fun DrawScope.drawOrganicTrunk(
    trunk: TrunkConfig,
    centerX: Float,
    baseY: Float,
    canvasHeight: Float,
    rotation: Float,
    growthFactor: Float,
    scale: Float,
    seed: Int
): Offset {
    val trunkHeight = canvasHeight * trunk.height * growthFactor * scale
    val baseWidth = canvasHeight * trunk.width * scale
    val topWidth = baseWidth * trunk.taperRatio
    
    rotate(rotation, pivot = Offset(centerX, baseY)) {
        if (trunk.segments > 1) {
            // Segmented trunk (bamboo style)
            drawBambooTrunk(trunk, centerX, baseY, canvasHeight, growthFactor, scale)
        } else {
            // Organic bezier trunk
            val trunkPath = Path().apply {
                // Left edge - slight curve outward then inward
                moveTo(centerX - baseWidth / 2, baseY)
                val curveAmount = baseWidth * 0.08f * (stableRandom(seed, 0) - 0.5f)
                cubicTo(
                    centerX - baseWidth / 2 + curveAmount, baseY - trunkHeight * 0.3f,
                    centerX - topWidth / 2 - curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                    centerX - topWidth / 2, baseY - trunkHeight
                )
                
                // Top edge
                lineTo(centerX + topWidth / 2, baseY - trunkHeight)
                
                // Right edge - mirror curve
                cubicTo(
                    centerX + topWidth / 2 + curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                    centerX + baseWidth / 2 - curveAmount, baseY - trunkHeight * 0.3f,
                    centerX + baseWidth / 2, baseY
                )
                close()
            }
            
            // Trunk gradient (3D effect)
            drawPath(
                path = trunkPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        trunk.color.darken(0.15f),
                        trunk.color.lighten(0.05f),
                        trunk.color,
                        trunk.color.darken(0.2f)
                    ),
                    startX = centerX - baseWidth / 2,
                    endX = centerX + baseWidth / 2
                )
            )
            
            // Subtle bark texture lines
            for (i in 1..3) {
                val lineY = baseY - trunkHeight * (0.2f + i * 0.2f)
                val lineWidth = baseWidth - (baseWidth - topWidth) * (0.2f + i * 0.2f)
                drawLine(
                    color = trunk.color.darken(0.1f).copy(alpha = 0.3f),
                    start = Offset(centerX - lineWidth * 0.3f, lineY),
                    end = Offset(centerX + lineWidth * 0.2f, lineY + trunkHeight * 0.03f),
                    strokeWidth = baseWidth * 0.03f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
    
    val topY = baseY - trunkHeight
    val rotRad = Math.toRadians(rotation.toDouble()).toFloat()
    return Offset(
        centerX + sin(rotRad) * trunkHeight * 0.1f,
        topY
    )
}

/**
 * Draws a Palm trunk with distinct ring texture (leaf scars)
 */
fun DrawScope.drawPalmTrunk(
    trunk: TrunkConfig,
    centerX: Float,
    baseY: Float,
    canvasHeight: Float,
    rotation: Float,
    growthFactor: Float,
    scale: Float,
    seed: Int
): Offset {
    val trunkHeight = canvasHeight * trunk.height * growthFactor * scale
    val baseWidth = canvasHeight * trunk.width * scale
    val topWidth = baseWidth * trunk.taperRatio
    
    rotate(rotation, pivot = Offset(centerX, baseY)) {
        // Organic bezier trunk shape
        val trunkPath = Path().apply {
            // Left edge
            moveTo(centerX - baseWidth / 2, baseY)
            val curveAmount = baseWidth * 0.05f * (stableRandom(seed, 0) - 0.5f)
            cubicTo(
                centerX - baseWidth / 2 + curveAmount, baseY - trunkHeight * 0.3f,
                centerX - topWidth / 2 - curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                centerX - topWidth / 2, baseY - trunkHeight
            )
            
            // Top edge
            lineTo(centerX + topWidth / 2, baseY - trunkHeight)
            
            // Right edge
            cubicTo(
                centerX + topWidth / 2 + curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                centerX + baseWidth / 2 - curveAmount, baseY - trunkHeight * 0.3f,
                centerX + baseWidth / 2, baseY
            )
            close()
        }
        
        // Trunk gradient
        drawPath(
            path = trunkPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    trunk.color.darken(0.2f),
                    trunk.color,
                    trunk.color.lighten(0.1f),
                    trunk.color.darken(0.25f)
                ),
                startX = centerX - baseWidth / 2,
                endX = centerX + baseWidth / 2
            )
        )
        
        // Palm leaf scars (rings) - Strong visual texture
        val ringCount = 12
        for (i in 1..ringCount) {
            val progress = i.toFloat() / (ringCount + 1)
            // Slightly irregular spacing
            val adjustedProgress = progress + (stableRandom(seed, i) - 0.5f) * 0.02f
            
            val lineY = baseY - trunkHeight * adjustedProgress
            val lineWidth = baseWidth - (baseWidth - topWidth) * adjustedProgress
            
            // Ring line (dark scar)
            drawLine(
                color = trunk.color.darken(0.3f).copy(alpha = 0.6f),
                start = Offset(centerX - lineWidth * 0.48f, lineY),
                end = Offset(centerX + lineWidth * 0.48f, lineY),
                strokeWidth = baseWidth * 0.04f,
                cap = StrokeCap.Round
            )
            
            // Highlight above ring
            drawLine(
                color = trunk.color.lighten(0.15f).copy(alpha = 0.4f),
                start = Offset(centerX - lineWidth * 0.45f, lineY - baseWidth * 0.015f),
                end = Offset(centerX + lineWidth * 0.45f, lineY - baseWidth * 0.015f),
                strokeWidth = baseWidth * 0.02f,
                cap = StrokeCap.Round
            )
        }
    }
    
    // Tính vị trí đỉnh thân sau rotation
    // Điểm gốc trước rotation: (centerX, baseY - trunkHeight)
    // Pivot point của rotation: (centerX, baseY)
    // Sau rotation, cần xoay điểm gốc quanh pivot
    val rotRad = Math.toRadians(rotation.toDouble()).toFloat()
    val originalTopX = centerX
    val originalTopY = baseY - trunkHeight
    
    // Xoay điểm (originalTopX, originalTopY) quanh pivot (centerX, baseY)
    // Công thức: 
    // x' = cos(θ) * (x - px) - sin(θ) * (y - py) + px
    // y' = sin(θ) * (x - px) + cos(θ) * (y - py) + py
    val dx = originalTopX - centerX  // = 0
    val dy = originalTopY - baseY    // = -trunkHeight
    
    val rotatedX = cos(rotRad) * dx - sin(rotRad) * dy + centerX
    val rotatedY = sin(rotRad) * dx + cos(rotRad) * dy + baseY
    
    return Offset(rotatedX, rotatedY)
}

/**
 * Draws a bamboo-style segmented trunk with joint rings
 */
fun DrawScope.drawBambooTrunk(
    trunk: TrunkConfig,
    centerX: Float,
    baseY: Float,
    canvasHeight: Float,
    growthFactor: Float,
    scale: Float
) {
    val trunkHeight = canvasHeight * trunk.height * growthFactor * scale
    val baseWidth = canvasHeight * trunk.width * scale
    val topWidth = baseWidth * trunk.taperRatio
    val segmentCount = trunk.segments
    val segmentHeight = trunkHeight / segmentCount
    
    for (i in 0 until segmentCount) {
        val segY = baseY - segmentHeight * (i + 1)
        val segWidth = baseWidth - (baseWidth - topWidth) * (i.toFloat() / segmentCount)
        
        // Segment body với gradient
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    trunk.color.darken(0.1f),
                    trunk.color.lighten(0.1f),
                    trunk.color.darken(0.05f)
                )
            ),
            topLeft = Offset(centerX - segWidth / 2, segY),
            size = Size(segWidth, segmentHeight * 0.88f),
            cornerRadius = CornerRadius(segWidth / 3)
        )
        
        // Joint ring
        if (i < segmentCount - 1) {
            drawOval(
                color = trunk.color.darken(0.15f),
                topLeft = Offset(centerX - segWidth * 0.55f / 2, segY - segmentHeight * 0.04f),
                size = Size(segWidth * 0.55f, segmentHeight * 0.08f)
            )
        }
    }
}

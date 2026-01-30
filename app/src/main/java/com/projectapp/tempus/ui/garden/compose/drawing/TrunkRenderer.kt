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
            
            drawBambooTrunk(trunk, centerX, baseY, canvasHeight, growthFactor, scale)
        } else {
            
            val trunkPath = Path().apply {
                
                moveTo(centerX - baseWidth / 2, baseY)
                val curveAmount = baseWidth * 0.08f * (stableRandom(seed, 0) - 0.5f)
                cubicTo(
                    centerX - baseWidth / 2 + curveAmount, baseY - trunkHeight * 0.3f,
                    centerX - topWidth / 2 - curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                    centerX - topWidth / 2, baseY - trunkHeight
                )
                
                
                lineTo(centerX + topWidth / 2, baseY - trunkHeight)
                
                
                cubicTo(
                    centerX + topWidth / 2 + curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                    centerX + baseWidth / 2 - curveAmount, baseY - trunkHeight * 0.3f,
                    centerX + baseWidth / 2, baseY
                )
                close()
            }
            
            
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
        
        val trunkPath = Path().apply {
            
            moveTo(centerX - baseWidth / 2, baseY)
            val curveAmount = baseWidth * 0.05f * (stableRandom(seed, 0) - 0.5f)
            cubicTo(
                centerX - baseWidth / 2 + curveAmount, baseY - trunkHeight * 0.3f,
                centerX - topWidth / 2 - curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                centerX - topWidth / 2, baseY - trunkHeight
            )
            
            
            lineTo(centerX + topWidth / 2, baseY - trunkHeight)
            
            
            cubicTo(
                centerX + topWidth / 2 + curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                centerX + baseWidth / 2 - curveAmount, baseY - trunkHeight * 0.3f,
                centerX + baseWidth / 2, baseY
            )
            close()
        }
        
        
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
        
        
        val ringCount = 12
        for (i in 1..ringCount) {
            val progress = i.toFloat() / (ringCount + 1)
            
            val adjustedProgress = progress + (stableRandom(seed, i) - 0.5f) * 0.02f
            
            val lineY = baseY - trunkHeight * adjustedProgress
            val lineWidth = baseWidth - (baseWidth - topWidth) * adjustedProgress
            
            
            drawLine(
                color = trunk.color.darken(0.3f).copy(alpha = 0.6f),
                start = Offset(centerX - lineWidth * 0.48f, lineY),
                end = Offset(centerX + lineWidth * 0.48f, lineY),
                strokeWidth = baseWidth * 0.04f,
                cap = StrokeCap.Round
            )
            
            
            drawLine(
                color = trunk.color.lighten(0.15f).copy(alpha = 0.4f),
                start = Offset(centerX - lineWidth * 0.45f, lineY - baseWidth * 0.015f),
                end = Offset(centerX + lineWidth * 0.45f, lineY - baseWidth * 0.015f),
                strokeWidth = baseWidth * 0.02f,
                cap = StrokeCap.Round
            )
        }
    }
    
    
    val rotRad = Math.toRadians(rotation.toDouble()).toFloat()
    val originalTopX = centerX
    val originalTopY = baseY - trunkHeight
    
    
    val dx = originalTopX - centerX  
    val dy = originalTopY - baseY    
    
    val rotatedX = cos(rotRad) * dx - sin(rotRad) * dy + centerX
    val rotatedY = sin(rotRad) * dx + cos(rotRad) * dy + baseY
    
    return Offset(rotatedX, rotatedY)
}


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
        
        
        if (i < segmentCount - 1) {
            drawOval(
                color = trunk.color.darken(0.15f),
                topLeft = Offset(centerX - segWidth * 0.55f / 2, segY - segmentHeight * 0.04f),
                size = Size(segWidth * 0.55f, segmentHeight * 0.08f)
            )
        }
    }
}

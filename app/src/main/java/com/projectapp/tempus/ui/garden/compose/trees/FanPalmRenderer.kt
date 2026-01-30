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


fun DrawScope.drawFanPalmLeaf(
    center: Offset,
    size: Float,
    baseColor: Color,
    opacity: Float,
    sway: Float,  
    seed: Int
) {
    val lightColor = baseColor.lighten(0.15f).copy(alpha = opacity)
    val midColor = baseColor.copy(alpha = opacity)
    val darkColor = baseColor.darken(0.2f).copy(alpha = opacity)
    
    val fanCount = 11  
    val fanSpread = 140f  
    val fanLength = size * 1.5f
    val leafWidth = size * 0.12f  
    
    
    for (i in 0 until fanCount) {
        val fanProgress = i.toFloat() / (fanCount - 1)
        
        val fanAngle = -fanSpread / 2f + fanSpread * fanProgress + sway * 0.5f
        val fanAngleRad = Math.toRadians(fanAngle.toDouble()).toFloat()
        
        
        val centerFactor = 1f - abs(fanProgress - 0.5f) * 2f
        val thisLength = fanLength * (0.8f + 0.2f * centerFactor)
        val thisWidth = leafWidth * (0.7f + 0.3f * centerFactor)
        
        
        val endX = center.x + sin(fanAngleRad) * thisLength
        val endY = center.y - cos(fanAngleRad) * thisLength
        
        
        val ctrlDist = thisLength * 0.5f
        val ctrlX = center.x + sin(fanAngleRad) * ctrlDist
        val ctrlY = center.y - cos(fanAngleRad) * ctrlDist * 0.7f
        
        
        val leafPath = Path().apply {
            
            moveTo(center.x, center.y)
            
            
            val leftAngle = fanAngleRad - 0.08f
            val leftCtrlX = center.x + sin(leftAngle) * ctrlDist - thisWidth * 0.3f
            val leftCtrlY = center.y - cos(leftAngle) * ctrlDist * 0.6f
            quadraticBezierTo(leftCtrlX, leftCtrlY, endX - thisWidth * 0.2f, endY)
            
            
            lineTo(endX + thisWidth * 0.2f, endY)
            
            
            val rightAngle = fanAngleRad + 0.08f
            val rightCtrlX = center.x + sin(rightAngle) * ctrlDist + thisWidth * 0.3f
            val rightCtrlY = center.y - cos(rightAngle) * ctrlDist * 0.6f
            quadraticBezierTo(rightCtrlX, rightCtrlY, center.x, center.y)
            
            close()
        }
        
        
        val leafColor = when {
            i < fanCount / 3 -> darkColor  
            i > fanCount * 2 / 3 -> darkColor  
            i == fanCount / 2 -> lightColor  
            else -> midColor  
        }
        
        drawPath(
            path = leafPath,
            color = leafColor
        )
        
        
        drawLine(
            color = darkColor.copy(alpha = opacity * 0.3f),
            start = Offset(center.x, center.y),
            end = Offset(endX, endY),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
    }
    
    
    drawCircle(
        color = baseColor.darken(0.3f).copy(alpha = opacity),
        radius = size * 0.08f,
        center = center
    )
}


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
    
    
    val clusters = listOf(
        
        Triple(-0.12f, 0.9f, -8f),   
        Triple(0.12f, 0.9f, 8f),
        
        Triple(-0.05f, 0.95f, -3f),
        Triple(0.05f, 0.95f, 3f),
        
        Triple(0f, 1f, 0f)
    )
    
    clusters.forEachIndexed { index, (offsetRatio, sizeRatio, extraSway) ->
        val clusterX = trunkTop.x + crownSize * offsetRatio
        val clusterY = trunkTop.y - crownSize * 0.05f * (4 - index)  
        
        
        val clusterColor = when {
            index < 2 -> baseColor.darken(0.15f)  
            index < 4 -> baseColor                 
            else -> baseColor.lighten(0.05f)       
        }
        
        drawFanPalmLeaf(
            center = Offset(clusterX, clusterY),
            size = crownSize * sizeRatio,
            baseColor = clusterColor,
            opacity = opacity * (0.85f + index * 0.03f),  
            sway = sway + extraSway,
            seed = seed + index
        )
    }
}

package com.projectapp.tempus.ui.garden.compose.trees

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import com.projectapp.tempus.ui.garden.compose.drawing.darken
import com.projectapp.tempus.ui.garden.compose.drawing.lighten
import com.projectapp.tempus.ui.garden.compose.stableRandom
import kotlin.math.cos
import kotlin.math.sin


fun DrawScope.drawConiferTree(
    trunkTop: Offset,
    canvasHeight: Float,
    tierCount: Int,
    baseColor: Color,
    opacity: Float,
    scale: Float,
    sway: Float,
    seed: Int
) {
    if (tierCount <= 0) return
    
    val darkGreen = baseColor.darken(0.25f).copy(alpha = opacity)
    val midGreen = baseColor.copy(alpha = opacity)
    val lightGreen = baseColor.lighten(0.2f).copy(alpha = opacity)
    val snowHighlight = baseColor.lighten(0.35f).copy(alpha = opacity * 0.6f)
    
    
    val totalHeight = canvasHeight * 0.55f * scale
    
    val baseWidth = canvasHeight * 0.4f * scale
    
    
    val actualTiers = (tierCount * 1.5f).toInt().coerceAtLeast(4)
    
    rotate(sway, pivot = trunkTop) {
        
        for (tier in (actualTiers - 1) downTo 0) {
            val tierProgress = tier.toFloat() / actualTiers
            
            
            val overlapFactor = 0.55f
            val tierY = trunkTop.y - totalHeight * tierProgress * overlapFactor
            
            
            val tierHeight = totalHeight * (0.35f - tierProgress * 0.08f) / actualTiers * 2.5f
            
            
            val tierWidth = baseWidth * (1f - tierProgress * 0.7f)
            
            val tierTop = tierY - tierHeight * 0.7f
            val tierBottom = tierY + tierHeight * 0.3f
            
            
            val spikeCount = 4 + (actualTiers - tier)
            
            val tierPath = Path().apply {
                
                moveTo(trunkTop.x, tierTop)
                
                
                for (i in 0 until spikeCount) {
                    val t = (i + 1).toFloat() / spikeCount
                    val x = trunkTop.x + tierWidth * 0.5f * t
                    val y = tierTop + tierHeight * t * 0.9f
                    
                    
                    val spikeDepth = tierWidth * 0.12f * (1 - t * 0.5f)
                    val spikeOutX = x + spikeDepth
                    val spikeOutY = y - tierHeight * 0.04f
                    lineTo(spikeOutX, spikeOutY)
                    
                    
                    lineTo(x, y)
                }
                
                
                lineTo(trunkTop.x + tierWidth * 0.5f, tierBottom)
                lineTo(trunkTop.x - tierWidth * 0.5f, tierBottom)
                
                
                for (i in spikeCount - 1 downTo 0) {
                    val t = (i + 1).toFloat() / spikeCount
                    val x = trunkTop.x - tierWidth * 0.5f * t
                    val y = tierTop + tierHeight * t * 0.9f
                    
                    lineTo(x, y)
                    
                    val spikeDepth = tierWidth * 0.12f * (1 - t * 0.5f)
                    val spikeOutX = x - spikeDepth
                    val spikeOutY = y - tierHeight * 0.04f
                    lineTo(spikeOutX, spikeOutY)
                }
                
                close()
            }
            
            
            val tierColors = when {
                tier < actualTiers / 3 -> listOf(lightGreen, midGreen, darkGreen)
                tier < actualTiers * 2 / 3 -> listOf(midGreen, midGreen, darkGreen)
                else -> listOf(snowHighlight, lightGreen, midGreen)
            }
            
            drawPath(
                path = tierPath,
                brush = Brush.verticalGradient(
                    colors = tierColors,
                    startY = tierTop,
                    endY = tierBottom
                )
            )
            
            
            drawPath(
                path = tierPath,
                color = darkGreen.copy(alpha = opacity * 0.25f),
                style = Stroke(width = 1f)
            )
        }
        
        
        val tipY = trunkTop.y - totalHeight * 0.65f
        drawCircle(
            color = lightGreen,
            radius = baseWidth * 0.04f,
            center = Offset(trunkTop.x, tipY)
        )
    }
}

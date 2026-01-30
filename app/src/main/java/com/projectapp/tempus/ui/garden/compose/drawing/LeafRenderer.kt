package com.projectapp.tempus.ui.garden.compose.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.projectapp.tempus.ui.garden.compose.LeafShape
import kotlin.math.cos
import kotlin.math.sin


fun DrawScope.drawIllustrationLeaf(
    center: Offset,
    size: Float,
    rotation: Float,
    baseColor: Color,
    opacity: Float,
    shape: LeafShape,
    seed: Int
) {
    
    
    if (shape == LeafShape.FAN_PALM) {
        drawFanPalmFronds(center, size, rotation, baseColor, opacity, seed)
        return
    }
    
    rotate(rotation, pivot = center) {
        val color = baseColor.copy(alpha = opacity)
        val highlightColor = baseColor.lighten(0.2f).copy(alpha = opacity * 0.6f)
        val shadowColor = baseColor.darken(0.15f).copy(alpha = opacity)
        
        when (shape) {
            LeafShape.ROUND -> {
                
                val leafPath = Path().apply {
                    
                    moveTo(center.x, center.y - size * 0.8f) 
                    cubicTo(
                        center.x + size * 0.7f, center.y - size * 0.5f,
                        center.x + size * 0.6f, center.y + size * 0.3f,
                        center.x, center.y + size * 0.4f
                    )
                    cubicTo(
                        center.x - size * 0.6f, center.y + size * 0.3f,
                        center.x - size * 0.7f, center.y - size * 0.5f,
                        center.x, center.y - size * 0.8f
                    )
                    close()
                }
                
                
                translate(left = size * 0.05f, top = size * 0.05f) {
                    drawPath(leafPath, shadowColor)
                }
                
                
                drawPath(
                    path = leafPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(highlightColor, color, shadowColor),
                        startY = center.y - size,
                        endY = center.y + size * 0.5f
                    )
                )
                
                
                drawCircle(
                    color = Color.White.copy(alpha = opacity * 0.15f),
                    radius = size * 0.15f,
                    center = Offset(center.x - size * 0.2f, center.y - size * 0.4f)
                )
            }
            
            LeafShape.NEEDLE -> {
                
                val needleCount = 5
                for (i in 0 until needleCount) {
                    val angle = -25f + (50f / (needleCount - 1)) * i
                    val needleAngleRad = Math.toRadians(angle.toDouble()).toFloat()
                    val needleLength = size * (0.9f + stableRandom(seed, i) * 0.2f)
                    
                    val endX = center.x + sin(needleAngleRad) * needleLength
                    val endY = center.y - cos(needleAngleRad) * needleLength
                    
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(color, shadowColor),
                            start = center,
                            end = Offset(endX, endY)
                        ),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = size * 0.12f,
                        cap = StrokeCap.Round
                    )
                }
            }
            
            LeafShape.LONG -> {
                
                val leafPath = Path().apply {
                    moveTo(center.x, center.y)
                    val droopAngle = stableRandom(seed, 0) * 30f - 15f
                    val droopRad = Math.toRadians(droopAngle.toDouble()).toFloat()
                    val endX = center.x + sin(droopRad) * size * 1.5f
                    val endY = center.y + size * 0.8f
                    quadraticBezierTo(
                        center.x + sin(droopRad) * size * 0.7f,
                        center.y - size * 0.1f,
                        endX, endY
                    )
                }
                
                drawPath(
                    path = leafPath,
                    brush = Brush.linearGradient(
                        colors = listOf(color, shadowColor),
                        start = center,
                        end = Offset(center.x, center.y + size)
                    ),
                    style = Stroke(width = size * 0.25f, cap = StrokeCap.Round)
                )
            }
            
            LeafShape.BAMBOO -> {
                
                val leafCount = 3 + (stableRandom(seed, 0) * 2).toInt()
                
                for (i in 0 until leafCount) {
                    val spreadAngle = -30f + (60f / (leafCount - 1).coerceAtLeast(1)) * i
                    val spreadRad = Math.toRadians(spreadAngle.toDouble()).toFloat()
                    
                    
                    val leafLength = size * (1.2f + stableRandom(seed, i + 1) * 0.4f)
                    
                    
                    val droopFactor = 0.3f + stableRandom(seed, i + 10) * 0.2f
                    val endX = center.x + sin(spreadRad) * leafLength * 0.6f
                    val endY = center.y + leafLength * droopFactor
                    
                    
                    val ctrlX = center.x + sin(spreadRad) * leafLength * 0.4f
                    val ctrlY = center.y - leafLength * 0.2f
                    
                    
                    val leafPath = Path().apply {
                        moveTo(center.x, center.y)
                        quadraticBezierTo(ctrlX, ctrlY, endX, endY)
                    }
                    
                    
                    val leafColor = if (i % 2 == 0) highlightColor else color
                    
                    drawPath(
                        path = leafPath,
                        brush = Brush.linearGradient(
                            colors = listOf(leafColor, shadowColor),
                            start = center,
                            end = Offset(endX, endY)
                        ),
                        style = Stroke(
                            width = size * 0.12f,  
                            cap = StrokeCap.Round
                        )
                    )
                }
            }
            
            LeafShape.FAN_PALM -> {
                
                
                val frondCount = 11  
                
                
                val darkGreen = baseColor.darken(0.15f).copy(alpha = opacity)
                val midGreen = baseColor.copy(alpha = opacity)
                val lightGreen = baseColor.lighten(0.20f).copy(alpha = opacity)
                val stemColor = baseColor.darken(0.30f).copy(alpha = opacity)
                val oldLeafColor = Color(0xFF9E9D24).copy(alpha = opacity * 0.8f)  
                
                
                for (i in 0 until frondCount) {
                    
                    val baseSpreadAngle = -80f + (160f / (frondCount - 1)) * i
                    
                    
                    val frondPosition = (i.toFloat() / (frondCount - 1)) - 0.5f  
                    val perFrondSway = rotation * (1f + kotlin.math.abs(frondPosition) * 1.5f) * 
                        (if (frondPosition < 0) -1f else 1f)  
                    
                    val spreadAngle = baseSpreadAngle + perFrondSway
                    val angleRad = Math.toRadians(spreadAngle.toDouble()).toFloat()
                    
                    
                    val frondLength = size * 1.5f * (0.85f + stableRandom(seed, i) * 0.25f)
                    
                    
                    val droopFactor = 0.2f + kotlin.math.abs(spreadAngle) / 80f * 0.4f
                    
                    
                    val midX = center.x + sin(angleRad) * frondLength * 0.4f
                    val midY = center.y - frondLength * 0.5f + frondLength * droopFactor * 0.3f
                    val endX = center.x + sin(angleRad) * frondLength * 0.8f
                    val endY = center.y - frondLength * (1f - droopFactor) + frondLength * droopFactor * 0.5f
                    
                    
                    val isOldLeaf = (i == 0 || i == frondCount - 1) && stableRandom(seed, i + 100) > 0.5f
                    
                    
                    val stemPath = Path().apply {
                        moveTo(center.x, center.y)
                        cubicTo(
                            center.x + sin(angleRad) * frondLength * 0.15f, center.y - frondLength * 0.25f,
                            midX, midY,
                            endX, endY
                        )
                    }
                    drawPath(
                        path = stemPath,
                        color = if (isOldLeaf) oldLeafColor.darken(0.2f) else stemColor,
                        style = Stroke(width = size * 0.025f, cap = StrokeCap.Round)
                    )
                    
                    
                    val leafletCount = 12  
                    for (j in 1..leafletCount) {
                        val t = j.toFloat() / (leafletCount + 1)
                        
                        
                        val invT = 1f - t
                        val t2 = t * t
                        val t3 = t2 * t
                        val invT2 = invT * invT
                        val invT3 = invT2 * invT
                        
                        
                        val ctrl1X = center.x + sin(angleRad) * frondLength * 0.15f
                        val ctrl1Y = center.y - frondLength * 0.25f
                        val ptX = invT3 * center.x + 3 * invT2 * t * ctrl1X + 3 * invT * t2 * midX + t3 * endX
                        val ptY = invT3 * center.y + 3 * invT2 * t * ctrl1Y + 3 * invT * t2 * midY + t3 * endY
                        
                        
                        val positionFactor = 1f - kotlin.math.abs(t - 0.5f) * 1.5f
                        val leafletLength = frondLength * 0.22f * positionFactor.coerceIn(0.4f, 1f)
                        
                        
                        val tangentAngle = angleRad + (t - 0.5f) * 0.3f
                        val leafletBaseAngle = 55f + t * 25f  
                        
                        
                        val leafletBaseColor = when {
                            isOldLeaf -> oldLeafColor
                            t < 0.4f -> darkGreen
                            t < 0.7f -> midGreen
                            else -> lightGreen
                        }
                        val leafletTipColor = leafletBaseColor.lighten(0.15f)
                        
                        
                        val leftAngle = tangentAngle - Math.toRadians(leafletBaseAngle.toDouble()).toFloat()
                        val leftEndX = ptX + sin(leftAngle) * leafletLength
                        val leftEndY = ptY - cos(leftAngle) * leafletLength * 0.7f + leafletLength * 0.3f  
                        
                        
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(leafletBaseColor, leafletTipColor),
                                start = Offset(ptX, ptY),
                                end = Offset(leftEndX, leftEndY)
                            ),
                            start = Offset(ptX, ptY),
                            end = Offset(leftEndX, leftEndY),
                            strokeWidth = size * 0.018f * (1f - t * 0.3f),  
                            cap = StrokeCap.Round
                        )
                        
                        
                        val rightAngle = tangentAngle + Math.toRadians(leafletBaseAngle.toDouble()).toFloat()
                        val rightEndX = ptX + sin(rightAngle) * leafletLength
                        val rightEndY = ptY - cos(rightAngle) * leafletLength * 0.7f + leafletLength * 0.3f
                        
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(leafletBaseColor, leafletTipColor),
                                start = Offset(ptX, ptY),
                                end = Offset(rightEndX, rightEndY)
                            ),
                            start = Offset(ptX, ptY),
                            end = Offset(rightEndX, rightEndY),
                            strokeWidth = size * 0.018f * (1f - t * 0.3f),
                            cap = StrokeCap.Round
                        )
                    }
                }
                
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(darkGreen, stemColor),
                        center = center,
                        radius = size * 0.1f
                    ),
                    radius = size * 0.08f,
                    center = center
                )
            }
            
            LeafShape.PETAL -> {
                
                val petalCount = 5
                for (i in 0 until petalCount) {
                    val angle = 360f / petalCount * i + stableRandom(seed, i) * 15f
                    val angleRad = Math.toRadians(angle.toDouble()).toFloat()
                    val petalDist = size * 0.35f
                    val petalCenterX = center.x + cos(angleRad) * petalDist
                    val petalCenterY = center.y + sin(angleRad) * petalDist - size * 0.2f
                    
                    
                    val petalPath = Path().apply {
                        val petalSize = size * 0.4f
                        moveTo(petalCenterX, petalCenterY - petalSize * 0.6f)
                        cubicTo(
                            petalCenterX + petalSize * 0.5f, petalCenterY - petalSize * 0.3f,
                            petalCenterX + petalSize * 0.4f, petalCenterY + petalSize * 0.4f,
                            petalCenterX, petalCenterY + petalSize * 0.5f
                        )
                        cubicTo(
                            petalCenterX - petalSize * 0.4f, petalCenterY + petalSize * 0.4f,
                            petalCenterX - petalSize * 0.5f, petalCenterY - petalSize * 0.3f,
                            petalCenterX, petalCenterY - petalSize * 0.6f
                        )
                        close()
                    }
                    
                    drawPath(
                        path = petalPath,
                        brush = Brush.radialGradient(
                            colors = listOf(highlightColor, color),
                            center = Offset(petalCenterX, petalCenterY),
                            radius = size * 0.4f
                        )
                    )
                }
                
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFEB3B), Color(0xFFFFC107)),
                        center = Offset(center.x, center.y - size * 0.2f),
                        radius = size * 0.15f
                    ),
                    radius = size * 0.12f,
                    center = Offset(center.x, center.y - size * 0.2f)
                )
            }
            
            LeafShape.FROND -> {
                
                val frondLength = size * 2.2f
                val frondWidth = size * 0.6f
                
                
                val curveDirection = if (stableRandom(seed, 0) > 0.5f) 1f else -1f
                val curveAmount = size * 0.4f * curveDirection
                
                
                val frondPath = Path().apply {
                    
                    moveTo(center.x, center.y)
                    
                    
                    val midX = center.x + curveAmount * 0.5f
                    val midY = center.y + frondLength * 0.5f
                    val endX = center.x + curveAmount
                    val endY = center.y + frondLength
                    
                    
                    cubicTo(
                        center.x - frondWidth * 0.3f, center.y + frondLength * 0.2f,
                        midX - frondWidth * 0.5f, midY,
                        endX - frondWidth * 0.15f, endY - frondLength * 0.1f
                    )
                    
                    
                    lineTo(endX, endY)
                    
                    
                    cubicTo(
                        midX + frondWidth * 0.5f, midY,
                        center.x + frondWidth * 0.3f, center.y + frondLength * 0.2f,
                        center.x, center.y
                    )
                    close()
                }
                
                
                drawPath(
                    path = frondPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.darken(0.1f),
                            color,
                            highlightColor
                        ),
                        start = center,
                        end = Offset(center.x + curveAmount, center.y + frondLength)
                    )
                )
                
                
                val stemPath = Path().apply {
                    moveTo(center.x, center.y)
                    quadraticBezierTo(
                        center.x + curveAmount * 0.5f, center.y + frondLength * 0.5f,
                        center.x + curveAmount, center.y + frondLength
                    )
                }
                drawPath(
                    path = stemPath,
                    color = color.darken(0.2f),
                    style = Stroke(width = size * 0.06f, cap = StrokeCap.Round)
                )
                
                
                val veinCount = 6
                for (i in 1..veinCount) {
                    val t = i.toFloat() / (veinCount + 1)
                    val stemX = center.x + curveAmount * t
                    val stemY = center.y + frondLength * t
                    
                    
                    val leftEndX = stemX - frondWidth * 0.4f * (1f - t * 0.5f)
                    val leftEndY = stemY + frondLength * 0.08f
                    drawLine(
                        color = color.darken(0.15f).copy(alpha = opacity * 0.6f),
                        start = Offset(stemX, stemY),
                        end = Offset(leftEndX, leftEndY),
                        strokeWidth = size * 0.02f
                    )
                    
                    
                    val rightEndX = stemX + frondWidth * 0.4f * (1f - t * 0.5f)
                    val rightEndY = stemY + frondLength * 0.08f
                    drawLine(
                        color = color.darken(0.15f).copy(alpha = opacity * 0.6f),
                        start = Offset(stemX, stemY),
                        end = Offset(rightEndX, rightEndY),
                        strokeWidth = size * 0.02f
                    )
                }
            }
            
            LeafShape.CONIFER -> {
                
                
            }
            
            LeafShape.OAK_CLOUD -> {
                
                
            }
        }
    }
}


private fun DrawScope.drawFanPalmFronds(
    center: Offset,
    size: Float,
    windSway: Float,
    baseColor: Color,
    opacity: Float,
    seed: Int
) {
    val frondCount = 13  
    
    
    val darkGreen = baseColor.darken(0.15f).copy(alpha = opacity)
    val midGreen = baseColor.copy(alpha = opacity)
    val lightGreen = baseColor.lighten(0.20f).copy(alpha = opacity)
    val stemColor = baseColor.darken(0.30f).copy(alpha = opacity)
    val oldLeafColor = Color(0xFF9E9D24).copy(alpha = opacity * 0.8f)
    
    
    for (i in 0 until frondCount) {
        
        val baseSpreadAngle = -80f + (160f / (frondCount - 1)) * i
        
        
        val frondPosition = (i.toFloat() / (frondCount - 1)) - 0.5f
        val perFrondSway = windSway * (1f + kotlin.math.abs(frondPosition) * 1.5f) * 
            (if (frondPosition < 0) -1f else 1f)
        
        val spreadAngle = baseSpreadAngle + perFrondSway
        val angleRad = Math.toRadians(spreadAngle.toDouble()).toFloat()
        
        
        val frondLength = size * 2.0f * (0.85f + stableRandom(seed, i) * 0.25f)
        
        
        val droopFactor = 0.2f + kotlin.math.abs(spreadAngle) / 80f * 0.4f
        
        
        val midX = center.x + sin(angleRad) * frondLength * 0.4f
        val midY = center.y - frondLength * 0.5f + frondLength * droopFactor * 0.3f
        val endX = center.x + sin(angleRad) * frondLength * 0.8f
        val endY = center.y - frondLength * (1f - droopFactor) + frondLength * droopFactor * 0.5f
        
        
        val isOldLeaf = (i == 0 || i == frondCount - 1) && stableRandom(seed, i + 100) > 0.5f
        
        
        val stemPath = Path().apply {
            moveTo(center.x, center.y)
            cubicTo(
                center.x + sin(angleRad) * frondLength * 0.15f, center.y - frondLength * 0.25f,
                midX, midY,
                endX, endY
            )
        }
        drawPath(
            path = stemPath,
            color = if (isOldLeaf) oldLeafColor.darken(0.2f) else stemColor,
            style = Stroke(width = size * 0.035f, cap = StrokeCap.Round)  
        )
        
        
        val leafletCount = 16  
        for (j in 1..leafletCount) {
            val t = j.toFloat() / (leafletCount + 1)
            
            
            val invT = 1f - t
            val t2 = t * t
            val t3 = t2 * t
            val invT2 = invT * invT
            val invT3 = invT2 * invT
            
            val ctrl1X = center.x + sin(angleRad) * frondLength * 0.15f
            val ctrl1Y = center.y - frondLength * 0.25f
            val ptX = invT3 * center.x + 3 * invT2 * t * ctrl1X + 3 * invT * t2 * midX + t3 * endX
            val ptY = invT3 * center.y + 3 * invT2 * t * ctrl1Y + 3 * invT * t2 * midY + t3 * endY
            
            
            val positionFactor = 1f - kotlin.math.abs(t - 0.5f) * 1.5f
            val leafletLength = frondLength * 0.28f * positionFactor.coerceIn(0.4f, 1f)  
            
            val tangentAngle = angleRad + (t - 0.5f) * 0.3f
            val leafletBaseAngle = 55f + t * 25f
            
            val leafletBaseColor = when {
                isOldLeaf -> oldLeafColor
                t < 0.4f -> darkGreen
                t < 0.7f -> midGreen
                else -> lightGreen
            }
            val leafletTipColor = leafletBaseColor.lighten(0.15f)
            
            
            val leftAngle = tangentAngle - Math.toRadians(leafletBaseAngle.toDouble()).toFloat()
            val leftEndX = ptX + sin(leftAngle) * leafletLength
            val leftEndY = ptY - cos(leftAngle) * leafletLength * 0.7f + leafletLength * 0.3f
            
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(leafletBaseColor, leafletTipColor),
                    start = Offset(ptX, ptY),
                    end = Offset(leftEndX, leftEndY)
                ),
                start = Offset(ptX, ptY),
                end = Offset(leftEndX, leftEndY),
                strokeWidth = size * 0.018f * (1f - t * 0.3f),
                cap = StrokeCap.Round
            )
            
            
            val rightAngle = tangentAngle + Math.toRadians(leafletBaseAngle.toDouble()).toFloat()
            val rightEndX = ptX + sin(rightAngle) * leafletLength
            val rightEndY = ptY - cos(rightAngle) * leafletLength * 0.7f + leafletLength * 0.3f
            
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(leafletBaseColor, leafletTipColor),
                    start = Offset(ptX, ptY),
                    end = Offset(rightEndX, rightEndY)
                ),
                start = Offset(ptX, ptY),
                end = Offset(rightEndX, rightEndY),
                strokeWidth = size * 0.018f * (1f - t * 0.3f),
                cap = StrokeCap.Round
            )
        }
    }
    
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(darkGreen, stemColor),
            center = center,
            radius = size * 0.1f
        ),
        radius = size * 0.08f,
        center = center
    )
}

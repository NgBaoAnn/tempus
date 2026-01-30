package com.projectapp.tempus.ui.garden.compose.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.projectapp.tempus.ui.garden.compose.FruitConfig
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


fun DrawScope.drawIllustrationFruit(
    fruit: FruitConfig,
    attachPoint: Offset,
    canvasHeight: Float,
    sway: Float,
    opacity: Float,
    scale: Float
) {
    val fruitSize = canvasHeight * fruit.size * scale
    val swayOffset = sin(Math.toRadians(sway.toDouble())).toFloat() * fruitSize * 0.3f
    
    
    val spreadOffset = sin(fruit.phaseOffset) * fruitSize * 1.5f
    
    val fruitCenter = Offset(
        attachPoint.x + swayOffset + spreadOffset,
        attachPoint.y + fruitSize * 0.6f + abs(cos(fruit.phaseOffset)) * fruitSize * 0.3f
    )
    
    
    drawOval(
        color = Color.Black.copy(alpha = 0.1f),
        topLeft = Offset(fruitCenter.x - fruitSize * 0.8f, fruitCenter.y + fruitSize * 0.8f),
        size = Size(fruitSize * 1.6f, fruitSize * 0.3f)
    )
    
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                fruit.color.lighten(0.15f).copy(alpha = opacity),
                fruit.color.copy(alpha = opacity),
                fruit.color.darken(0.2f).copy(alpha = opacity)
            ),
            center = Offset(fruitCenter.x - fruitSize * 0.2f, fruitCenter.y - fruitSize * 0.2f),
            radius = fruitSize * 1.2f
        ),
        radius = fruitSize,
        center = fruitCenter
    )
    
    
    drawCircle(
        color = Color.White.copy(alpha = opacity * 0.35f),
        radius = fruitSize * 0.25f,
        center = Offset(fruitCenter.x - fruitSize * 0.35f, fruitCenter.y - fruitSize * 0.35f)
    )
    
    
    val stemPath = Path().apply {
        moveTo(fruitCenter.x, fruitCenter.y - fruitSize)
        quadraticBezierTo(
            fruitCenter.x + fruitSize * 0.15f, fruitCenter.y - fruitSize * 1.15f,
            fruitCenter.x + fruitSize * 0.25f, fruitCenter.y - fruitSize * 1.35f
        )
    }
    drawPath(
        path = stemPath,
        color = Color(0xFF5D4037).copy(alpha = opacity),
        style = Stroke(width = fruitSize * 0.12f, cap = StrokeCap.Round)
    )
    
    
    val leafX = fruitCenter.x + fruitSize * 0.15f
    val leafY = fruitCenter.y - fruitSize * 1.1f
    drawOval(
        color = Color(0xFF4CAF50).copy(alpha = opacity),
        topLeft = Offset(leafX, leafY - fruitSize * 0.12f),
        size = Size(fruitSize * 0.25f, fruitSize * 0.15f)
    )
}

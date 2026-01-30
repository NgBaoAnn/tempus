package com.projectapp.tempus.ui.garden.compose.drawing

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.projectapp.tempus.ui.garden.compose.PotConfig


fun DrawScope.drawIllustrationPot(
    pot: PotConfig,
    centerX: Float,
    baseY: Float,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val potWidth = canvasWidth * pot.width
    val potHeight = canvasHeight * pot.height
    
    
    drawOval(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(centerX - potWidth * 0.4f, baseY - potHeight * 0.08f),
        size = Size(potWidth * 0.8f, potHeight * 0.16f)
    )
    
    
    val potPath = Path().apply {
        val topWidth = potWidth * 0.9f
        val bottomWidth = potWidth * 0.72f
        
        moveTo(centerX - topWidth / 2, baseY - potHeight)
        cubicTo(
            centerX - topWidth / 2 - potWidth * 0.02f, baseY - potHeight * 0.5f,
            centerX - bottomWidth / 2 - potWidth * 0.01f, baseY - potHeight * 0.2f,
            centerX - bottomWidth / 2, baseY
        )
        lineTo(centerX + bottomWidth / 2, baseY)
        cubicTo(
            centerX + bottomWidth / 2 + potWidth * 0.01f, baseY - potHeight * 0.2f,
            centerX + topWidth / 2 + potWidth * 0.02f, baseY - potHeight * 0.5f,
            centerX + topWidth / 2, baseY - potHeight
        )
        close()
    }
    
    
    drawPath(path = potPath, color = pot.color)
    
    
    drawPath(
        path = potPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.15f),  
                Color.Transparent,                 
                Color.Black.copy(alpha = 0.2f)    
            ),
            startX = centerX - potWidth / 2,
            endX = centerX + potWidth / 2
        )
    )
    
    
    val rimHeight = potHeight * 0.12f
    drawRoundRect(
        color = pot.color,  
        topLeft = Offset(centerX - potWidth * 0.48f, baseY - potHeight - rimHeight * 0.5f),
        size = Size(potWidth * 0.96f, rimHeight),
        cornerRadius = CornerRadius(rimHeight / 2)
    )
    
    
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.2f),
                Color.Transparent
            )
        ),
        topLeft = Offset(centerX - potWidth * 0.48f, baseY - potHeight - rimHeight * 0.5f),
        size = Size(potWidth * 0.96f, rimHeight * 0.5f),
        cornerRadius = CornerRadius(rimHeight / 2)
    )
    
    
    drawOval(
        color = Color(0xFF3D2817),
        topLeft = Offset(centerX - potWidth * 0.38f, baseY - potHeight - rimHeight * 0.15f),
        size = Size(potWidth * 0.76f, potHeight * 0.08f)
    )
}


fun DrawScope.drawSoilMound(
    color: Color,
    centerX: Float,
    y: Float,
    radiusX: Float,
    radiusY: Float
) {
    drawOval(
        brush = Brush.verticalGradient(
            colors = listOf(color.lighten(0.1f), color.darken(0.1f)),
            startY = y - radiusY,
            endY = y + radiusY
        ),
        topLeft = Offset(centerX - radiusX, y - radiusY),
        size = Size(radiusX * 2, radiusY * 2.2f)
    )
}

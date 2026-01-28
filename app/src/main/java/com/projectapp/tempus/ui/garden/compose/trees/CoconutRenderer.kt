package com.projectapp.tempus.ui.garden.compose.trees

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.projectapp.tempus.ui.garden.compose.drawing.darken
import com.projectapp.tempus.ui.garden.compose.drawing.lighten
import com.projectapp.tempus.ui.garden.compose.stableRandom
import kotlin.math.cos
import kotlin.math.sin

/**
 * Coconut Tree Renderer (Cây Dừa)
 * Renders tropical coconut palm fronds with natural droop (for COCONUT tree type with LeafShape.FROND)
 */

/**
 * Draw a coconut palm frond from origin, spreading at angle then drooping down
 */
fun DrawScope.drawCoconutFrond(
    origin: Offset,
    spreadAngle: Float,
    frondLength: Float,
    frondWidth: Float,
    baseColor: Color,
    opacity: Float,
    seed: Int
) {
    val color = baseColor.copy(alpha = opacity)
    val highlightColor = baseColor.lighten(0.15f).copy(alpha = opacity)
    val shadowColor = baseColor.darken(0.2f).copy(alpha = opacity)
    
    // Góc ban đầu - xoè ra từ thân
    val spreadRad = Math.toRadians(spreadAngle.toDouble()).toFloat()
    
    // Control points cho bezier curve - tạo hình dáng cong rũ tự nhiên
    val cp1Distance = frondLength * 0.35f
    val cp1X = origin.x + sin(spreadRad) * cp1Distance
    val cp1Y = origin.y - cos(spreadRad) * cp1Distance * 0.8f
    
    val cp2Distance = frondLength * 0.7f
    val droopAmount = frondLength * 0.25f * (1f + stableRandom(seed, 0) * 0.3f)
    val cp2X = origin.x + sin(spreadRad) * cp2Distance * 0.8f
    val cp2Y = origin.y - cos(spreadRad) * cp2Distance * 0.5f + droopAmount
    
    // Điểm cuối - rũ xuống
    val endX = origin.x + sin(spreadRad) * frondLength * 0.6f
    val endY = origin.y + droopAmount * 1.5f
    
    // Path cho tàu lá (thickened bezier)
    val frondPath = Path().apply {
        moveTo(origin.x, origin.y)
        
        // Cạnh ngoài (xa thân cây)
        val outerOffset = frondWidth * 0.5f
        cubicTo(
            cp1X + cos(spreadRad) * outerOffset, cp1Y - sin(spreadRad) * outerOffset,
            cp2X + outerOffset * 0.6f, cp2Y,
            endX + outerOffset * 0.2f, endY
        )
        
        // Đầu lá nhọn
        lineTo(endX - outerOffset * 0.1f, endY + frondWidth * 0.15f)
        
        // Cạnh trong (gần thân cây)
        cubicTo(
            cp2X - outerOffset * 0.4f, cp2Y + outerOffset * 0.2f,
            cp1X - cos(spreadRad) * outerOffset * 0.3f, cp1Y + sin(spreadRad) * outerOffset * 0.3f,
            origin.x, origin.y
        )
        close()
    }
    
    // Fill tàu lá with gradient
    drawPath(
        path = frondPath,
        brush = Brush.linearGradient(
            colors = listOf(shadowColor, color, highlightColor),
            start = origin,
            end = Offset(endX, endY)
        )
    )
    
    // Gân giữa (cuống lá)
    val midRibPath = Path().apply {
        moveTo(origin.x, origin.y)
        cubicTo(cp1X, cp1Y, cp2X, cp2Y, endX, endY)
    }
    drawPath(
        path = midRibPath,
        color = baseColor.darken(0.25f).copy(alpha = opacity * 0.8f),
        style = Stroke(width = frondWidth * 0.08f, cap = StrokeCap.Round)
    )
    
    // Các gân phụ (6 gân mỗi bên)
    for (i in 1..6) {
        val t = i * 0.12f + 0.1f
        val ribT = t * t
        val ribX = origin.x + (cp1X - origin.x) * ribT * 0.5f + (cp2X - cp1X) * ribT + (endX - cp2X) * ribT * 0.5f
        val ribY = origin.y + (cp1Y - origin.y) * ribT * 0.5f + (cp2Y - cp1Y) * ribT + (endY - cp2Y) * ribT * 0.5f
        
        val ribLength = frondWidth * (0.6f + (1 - t) * 0.4f)
        val ribAngle = spreadAngle + (if (i % 2 == 0) 60f else -60f) * (0.5f + t * 0.5f)
        val ribAngleRad = Math.toRadians(ribAngle.toDouble()).toFloat()
        
        drawLine(
            color = baseColor.darken(0.15f).copy(alpha = opacity * 0.5f),
            start = Offset(ribX, ribY),
            end = Offset(ribX + cos(ribAngleRad) * ribLength, ribY + sin(ribAngleRad) * ribLength * 0.7f),
            strokeWidth = frondWidth * 0.025f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draw coconut fruits at palm tree top
 */
fun DrawScope.drawCoconuts(
    origin: Offset,
    count: Int,
    size: Float,
    opacity: Float,
    seed: Int
) {
    val coconutColor = Color(0xFF6D4C41)
    val highlightColor = Color(0xFF8D6E63)
    
    for (i in 0 until count) {
        val angle = (360f / count) * i + stableRandom(seed, i) * 20f
        val angleRad = Math.toRadians(angle.toDouble()).toFloat()
        val dist = size * 0.5f
        
        val x = origin.x + cos(angleRad) * dist
        val y = origin.y + sin(angleRad) * dist * 0.6f + size * 0.3f
        
        // Coconut body
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(highlightColor.copy(alpha = opacity), coconutColor.copy(alpha = opacity)),
                center = Offset(x - size * 0.1f, y - size * 0.1f),
                radius = size * 0.4f
            ),
            topLeft = Offset(x - size * 0.35f, y - size * 0.3f),
            size = Size(size * 0.7f, size * 0.6f)
        )
    }
}

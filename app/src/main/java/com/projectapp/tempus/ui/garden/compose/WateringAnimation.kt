package com.projectapp.tempus.ui.garden.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animation tưới cây với giọt nước rơi xuống
 */
@Composable
fun WateringAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    var isAnimating by remember { mutableStateOf(false) }
    
    // Khi isPlaying chuyển thành true, bắt đầu animation
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            isAnimating = true
            delay(2000) // Animation kéo dài 2 giây
            isAnimating = false
            onAnimationEnd()
        }
    }
    
    if (isAnimating) {
        WaterDropletsCanvas(modifier = modifier)
    }
}

@Composable
private fun WaterDropletsCanvas(modifier: Modifier = Modifier) {
    // Tạo nhiều giọt nước
    val droplets = remember {
        List(15) { 
            WaterDroplet(
                startX = Random.nextFloat() * 0.8f + 0.1f, // 10% - 90% width
                delay = Random.nextInt(0, 500),
                speed = Random.nextFloat() * 0.3f + 0.7f, // 0.7 - 1.0
                size = Random.nextFloat() * 0.5f + 0.75f  // 0.75 - 1.25
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "water")
    
    // Progress cho animation (0 -> 1)
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "water_progress"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val dropletBaseSize = size.minDimension * 0.02f
        
        droplets.forEach { droplet ->
            val dropletProgress = ((progress * 1.5f - droplet.delay / 1000f) * droplet.speed)
                .coerceIn(0f, 1.2f)
            
            if (dropletProgress > 0f && dropletProgress < 1.2f) {
                val x = size.width * droplet.startX
                val startY = size.height * 0.05f
                val endY = size.height * 0.75f
                val y = startY + (endY - startY) * dropletProgress.coerceAtMost(1f)
                
                // Vẽ giọt nước
                if (dropletProgress < 1f) {
                    drawWaterDroplet(
                        center = Offset(x, y),
                        size = dropletBaseSize * droplet.size,
                        alpha = (1f - dropletProgress * 0.5f).coerceIn(0.4f, 1f)
                    )
                } else {
                    // Splash effect khi chạm đất
                    val splashProgress = (dropletProgress - 1f) * 5f
                    drawSplash(
                        center = Offset(x, endY),
                        progress = splashProgress.coerceIn(0f, 1f),
                        baseSize = dropletBaseSize * droplet.size
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawWaterDroplet(
    center: Offset,
    size: Float,
    alpha: Float
) {
    val waterColor = Color(0xFF42A5F5).copy(alpha = alpha * 0.9f)
    val highlightColor = Color(0xFFBBDEFB).copy(alpha = alpha * 0.6f)
    
    // Giọt nước hình giọt lệ
    val dropPath = Path().apply {
        moveTo(center.x, center.y - size * 1.5f) // Đỉnh nhọn
        cubicTo(
            center.x + size * 0.5f, center.y - size * 0.5f,
            center.x + size * 0.8f, center.y + size * 0.3f,
            center.x, center.y + size * 0.8f
        )
        cubicTo(
            center.x - size * 0.8f, center.y + size * 0.3f,
            center.x - size * 0.5f, center.y - size * 0.5f,
            center.x, center.y - size * 1.5f
        )
        close()
    }
    
    // Main drop
    drawPath(dropPath, waterColor)
    
    // Highlight
    drawCircle(
        color = highlightColor,
        radius = size * 0.25f,
        center = Offset(center.x - size * 0.2f, center.y - size * 0.3f)
    )
}

private fun DrawScope.drawSplash(
    center: Offset,
    progress: Float,
    baseSize: Float
) {
    val waterColor = Color(0xFF42A5F5).copy(alpha = (1f - progress) * 0.7f)
    
    // Vẽ các giọt văng ra
    repeat(6) { i ->
        val angle = (i * 60f + 30f) * (Math.PI / 180f).toFloat()
        val distance = baseSize * (2f + progress * 4f)
        val dropSize = baseSize * 0.4f * (1f - progress * 0.5f)
        
        val x = center.x + cos(angle) * distance
        val y = center.y - sin(angle) * distance * 0.5f
        
        drawCircle(
            color = waterColor,
            radius = dropSize,
            center = Offset(x, y)
        )
    }
    
    // Vòng nước chính
    drawCircle(
        color = waterColor.copy(alpha = (1f - progress) * 0.4f),
        radius = baseSize * (1f + progress * 3f),
        center = center
    )
}

private data class WaterDroplet(
    val startX: Float,
    val delay: Int,
    val speed: Float,
    val size: Float
)

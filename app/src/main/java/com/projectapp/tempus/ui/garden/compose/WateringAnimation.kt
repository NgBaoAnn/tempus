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


@Composable
fun WateringAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    var isAnimating by remember { mutableStateOf(false) }
    
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            isAnimating = true
            delay(2000) 
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
    
    val droplets = remember {
        List(15) { 
            WaterDroplet(
                startX = Random.nextFloat() * 0.8f + 0.1f, 
                delay = Random.nextInt(0, 500),
                speed = Random.nextFloat() * 0.3f + 0.7f, 
                size = Random.nextFloat() * 0.5f + 0.75f  
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "water")
    
    
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
                
                
                if (dropletProgress < 1f) {
                    drawWaterDroplet(
                        center = Offset(x, y),
                        size = dropletBaseSize * droplet.size,
                        alpha = (1f - dropletProgress * 0.5f).coerceIn(0.4f, 1f)
                    )
                } else {
                    
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
    
    
    val dropPath = Path().apply {
        moveTo(center.x, center.y - size * 1.5f) 
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
    
    
    drawPath(dropPath, waterColor)
    
    
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

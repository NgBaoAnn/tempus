package com.projectapp.tempus.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput


object TempusAnimations {
    
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
    
    
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val SmoothSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}


fun Modifier.scalePressEffect(
    pressedScale: Float = 0.96f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scalePressEffect"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}


fun Modifier.fadeInSlideUp(
    index: Int,
    visible: Boolean,
    delayPerItem: Int = 50,
    initialOffsetY: Float = 30f
): Modifier = composed {
    val delay = index * delayPerItem
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = TempusAnimations.NORMAL,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        ),
        label = "fadeInAlpha"
    )
    
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else initialOffsetY,
        animationSpec = tween(
            durationMillis = TempusAnimations.NORMAL,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        ),
        label = "fadeInOffsetY"
    )
    
    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = offsetY
    }
}


fun Modifier.pulseEffect(
    enabled: Boolean = true,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f
): Modifier = composed {
    if (!enabled) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}


@Composable
fun rememberShimmerBrush(): androidx.compose.ui.graphics.Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    
    return androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color(0xFFE2E8F0),
            androidx.compose.ui.graphics.Color(0xFFF8FAFC),
            androidx.compose.ui.graphics.Color(0xFFE2E8F0)
        ),
        start = androidx.compose.ui.geometry.Offset(shimmerTranslate - 500f, 0f),
        end = androidx.compose.ui.geometry.Offset(shimmerTranslate, 0f)
    )
}


fun Modifier.glowEffect(
    color: androidx.compose.ui.graphics.Color,
    enabled: Boolean = true,
    blurRadius: Float = 12f
): Modifier = composed {
    if (!enabled) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    this.graphicsLayer {
        shadowElevation = blurRadius
        
        
        this.alpha = 0.9f + (alpha * 0.1f)
    }
}


fun Modifier.rotateOnState(
    rotated: Boolean,
    degrees: Float = 45f
): Modifier = composed {
    val rotation by animateFloatAsState(
        targetValue = if (rotated) degrees else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )
    
    this.graphicsLayer {
        rotationZ = rotation
    }
}

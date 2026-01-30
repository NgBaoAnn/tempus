package com.projectapp.tempus.ui.garden.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType


@Composable
fun AnimatedTree(
    state: TreeState,
    type: TreeType,
    modifier: Modifier = Modifier,
    size: TreeSize = TreeSize.MEDIUM,
    enableAnimation: Boolean = true
) {
    
    val infiniteTransition = rememberInfiniteTransition(label = "treeSway")
    
    
    val swayRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    
    val swayTranslation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translation"
    )
    
    
    val swayScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val actualRotation = if (enableAnimation && state != TreeState.DEAD) swayRotation else 0f
    val actualTranslation = if (enableAnimation && state != TreeState.DEAD) swayTranslation else 0f
    val actualScale = if (enableAnimation && state != TreeState.DEAD) swayScale else 1f
    
    val fontSize = when (size) {
        TreeSize.SMALL -> 40.sp
        TreeSize.MEDIUM -> 60.sp
        TreeSize.LARGE -> 120.sp
        TreeSize.XLARGE -> 180.sp
    }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = actualRotation
                translationX = actualTranslation
                scaleX = actualScale
                scaleY = actualScale
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getTreeEmoji(state, type),
            fontSize = fontSize
        )
    }
}

enum class TreeSize {
    SMALL,   
    MEDIUM,  
    LARGE,   
    XLARGE   
}

private fun getTreeEmoji(state: TreeState, type: TreeType): String {
    return when (state) {
        TreeState.SEED -> "🌰"
        TreeState.SPROUT -> "🌱"
        TreeState.SAPLING -> when (type) {
            TreeType.SAKURA -> "🌸"
            TreeType.PINE -> "🌲"
            TreeType.BAMBOO -> "🎋"
            TreeType.PALM -> "🌴"
            else -> "🌿"
        }
        TreeState.TREE -> type.emoji
        TreeState.DEAD -> "🥀"
    }
}

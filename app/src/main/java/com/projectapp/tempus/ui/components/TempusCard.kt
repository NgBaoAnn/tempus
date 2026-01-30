package com.projectapp.tempus.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.ui.theme.TempusDesignSystem


enum class CardVariant {
    Default,    
    Elevated,   
    Glass,      
    Gradient    
}


@Composable
fun TempusCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 2.dp,
    variant: CardVariant = CardVariant.Default,
    isPressable: Boolean = onClick != null,
    gradientColors: List<Color> = TempusDesignSystem.Gradients.Primary,
    cornerRadius: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isPressable) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardScale"
    )
    
    val shape = RoundedCornerShape(cornerRadius)
    
    
    val actualElevation = when (variant) {
        CardVariant.Default -> elevation
        CardVariant.Elevated -> elevation * 3
        CardVariant.Glass -> 0.dp
        CardVariant.Gradient -> elevation
    }
    
    
    val actualBackground = when (variant) {
        CardVariant.Glass -> TempusDesignSystem.Glass.Background
        else -> backgroundColor
    }
    
    
    val baseModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (variant == CardVariant.Gradient) {
                Modifier
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = shape
                    )
                    .clip(shape)
            } else Modifier
        )
        .then(
            if (isPressable) {
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick?.invoke() }
                    )
                }
            } else Modifier
        )
    
    
    val glassModifier = if (variant == CardVariant.Glass) {
        Modifier.border(
            width = 1.dp,
            color = TempusDesignSystem.Glass.Border,
            shape = shape
        )
    } else Modifier
    
    
    val gradientInnerModifier = if (variant == CardVariant.Gradient) {
        Modifier
            .clip(RoundedCornerShape(cornerRadius - 2.dp))
            .background(backgroundColor)
    } else Modifier
    
    if (onClick != null && !isPressable) {
        
        Card(
            onClick = onClick,
            modifier = baseModifier.then(glassModifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = actualBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = actualElevation),
            content = content
        )
    } else {
        
        Card(
            modifier = baseModifier.then(glassModifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = actualBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = actualElevation),
            content = content
        )
    }
}


@Composable
fun TempusGradientCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradientColors: List<Color> = TempusDesignSystem.Gradients.Primary,
    content: @Composable ColumnScope.() -> Unit
) {
    TempusCard(
        modifier = modifier,
        onClick = onClick,
        variant = CardVariant.Gradient,
        gradientColors = gradientColors,
        content = content
    )
}


@Composable
fun TempusGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    TempusCard(
        modifier = modifier,
        onClick = onClick,
        variant = CardVariant.Glass,
        content = content
    )
}


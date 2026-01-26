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

/**
 * Card Variants for different use cases
 */
enum class CardVariant {
    Default,    // Standard elevated card
    Elevated,   // Higher elevation with stronger shadow
    Glass,      // Glassmorphism effect
    Gradient    // Gradient border effect
}

/**
 * Premium TempusCard with multiple variants and animations
 */
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
    // Press animation state
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
    
    // Calculate elevation based on variant
    val actualElevation = when (variant) {
        CardVariant.Default -> elevation
        CardVariant.Elevated -> elevation * 3
        CardVariant.Glass -> 0.dp
        CardVariant.Gradient -> elevation
    }
    
    // Calculate background based on variant
    val actualBackground = when (variant) {
        CardVariant.Glass -> TempusDesignSystem.Glass.Background
        else -> backgroundColor
    }
    
    // Base modifier with press animation
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
    
    // Glass variant border
    val glassModifier = if (variant == CardVariant.Glass) {
        Modifier.border(
            width = 1.dp,
            color = TempusDesignSystem.Glass.Border,
            shape = shape
        )
    } else Modifier
    
    // Gradient variant inner padding
    val gradientInnerModifier = if (variant == CardVariant.Gradient) {
        Modifier
            .clip(RoundedCornerShape(cornerRadius - 2.dp))
            .background(backgroundColor)
    } else Modifier
    
    if (onClick != null && !isPressable) {
        // Clickable without press animation (legacy support)
        Card(
            onClick = onClick,
            modifier = baseModifier.then(glassModifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = actualBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = actualElevation),
            content = content
        )
    } else {
        // New premium card with animations
        Card(
            modifier = baseModifier.then(glassModifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = actualBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = actualElevation),
            content = content
        )
    }
}

/**
 * Gradient Border Card - Convenience wrapper
 */
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

/**
 * Glass Card - Convenience wrapper for glassmorphism effect
 */
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


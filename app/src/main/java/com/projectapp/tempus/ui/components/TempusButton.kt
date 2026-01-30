package com.projectapp.tempus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.ui.theme.TempusDesignSystem

enum class ButtonVariant {
    Solid,
    Outline,
    Ghost,
    Gradient
}

@Composable
fun TempusButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Solid,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    gradientColors: List<Color> = TempusDesignSystem.Gradients.Primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "buttonScale"
    )

    val shape = RoundedCornerShape(14.dp)
    
    
    val buttonModifier = modifier
        .height(52.dp)
        .scale(scale)
        .clip(shape)
        .then(
            when (variant) {
                ButtonVariant.Solid -> Modifier.background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f))
                ButtonVariant.Gradient -> Modifier.background(
                    brush = Brush.horizontalGradient(
                        colors = if (enabled) gradientColors else gradientColors.map { it.copy(alpha = 0.5f) }
                    )
                )
                ButtonVariant.Outline -> Modifier
                    .border(1.5.dp, if (enabled) containerColor else containerColor.copy(alpha = 0.5f), shape)
                    .background(Color.Transparent)
                ButtonVariant.Ghost -> Modifier.background(Color.Transparent)
            }
        )
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(color = contentColor),
            enabled = enabled && !isLoading,
            onClick = onClick
        )
        .padding(horizontal = 24.dp)

    
    val actualContentColor = when (variant) {
        ButtonVariant.Solid, ButtonVariant.Gradient -> contentColor
        ButtonVariant.Outline, ButtonVariant.Ghost -> containerColor
    }

    Box(
        modifier = buttonModifier,
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = actualContentColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) actualContentColor else actualContentColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) actualContentColor else actualContentColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}


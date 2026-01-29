package com.projectapp.tempus.ui.ai.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.domain.model.ChatMode
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

/**
 * Toggle switch for Ask/Agent modes
 */
@Composable
fun ModeToggle(
    currentMode: ChatMode,
    onModeChange: (ChatMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeButton(
                text = "Ask",
                icon = "💬",
                isSelected = currentMode == ChatMode.ASK,
                onClick = { if (enabled) onModeChange(ChatMode.ASK) },
                enabled = enabled
            )
            
            ModeButton(
                text = "Agent",
                icon = "🤖",
                isSelected = currentMode == ChatMode.AGENT,
                onClick = { if (enabled) onModeChange(ChatMode.AGENT) },
                enabled = enabled
            )
            
            ModeButton(
                text = "Planner",
                icon = "🎯",
                isSelected = currentMode == ChatMode.LIFE_PLANNER,
                onClick = { if (enabled) onModeChange(ChatMode.LIFE_PLANNER) },
                enabled = enabled
            )
        }
    }
}

@Composable
private fun ModeButton(
    text: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(200),
        label = "bgColor"
    )
    
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onSecondary
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "textColor"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = " $text",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

/**
 * Compact mode indicator for input area
 */
@Composable
fun ModeIndicator(
    mode: ChatMode,
    modifier: Modifier = Modifier
) {
    val (icon, text, color) = when (mode) {
        ChatMode.ASK -> Triple("💬", "Ask", MaterialTheme.colorScheme.onSurfaceVariant)
        ChatMode.AGENT -> Triple("🤖", "Agent", MaterialTheme.colorScheme.secondary)
        ChatMode.LIFE_PLANNER -> Triple("🎯", "Planner", MaterialTheme.colorScheme.primary)
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Text(
            text = "$icon $text",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

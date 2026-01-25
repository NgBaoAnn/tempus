package com.projectapp.tempus.ui.ai.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.R

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * PREMIUM CHAT INPUT - AI-Native Design
 * Features: Glass morphism, gradient send button, focus glow effect
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * Premium chat input with glass morphism design
 */
@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    placeholder: String = "Nhập tin nhắn...",
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ChatColors.Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text input field with glass morphism
            ChatTextField(
                value = value,
                onValueChange = onValueChange,
                onSend = {
                    if (value.isNotBlank() && enabled) {
                        onSend()
                    }
                },
                enabled = enabled,
                placeholder = placeholder,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
            
            // Premium send button
            PremiumSendButton(
                onClick = {
                    if (value.isNotBlank() && enabled) {
                        onSend()
                        focusManager.clearFocus()
                    }
                },
                enabled = value.isNotBlank() && enabled,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/**
 * Glass morphism text field
 */
@Composable
private fun ChatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    placeholder: String = "Nhập tin nhắn...",
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (value.isNotEmpty()) 
            ChatColors.Primary.copy(alpha = 0.5f) 
        else 
            ChatColors.InputBorder,
        animationSpec = tween(200),
        label = "borderColor"
    )
    
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = ChatColors.TextPrimary
        ),
        cursorBrush = SolidColor(ChatColors.Primary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(
            onSend = { onSend() }
        ),
        maxLines = 4,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(ChatDimens.InputCornerRadius))
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(ChatDimens.InputCornerRadius)
                    )
                    .background(ChatColors.InputBackground)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ChatColors.InputPlaceholder
                    )
                }
                innerTextField()
            }
        },
        modifier = modifier
    )
}

/**
 * Premium gradient send button with glow effect
 */
@Composable
private fun PremiumSendButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val backgroundBrush = if (enabled) {
        Brush.linearGradient(
            colors = listOf(
                ChatColors.Primary,
                ChatColors.PrimaryLight
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                ChatColors.TextDim,
                ChatColors.TextMuted
            )
        )
    }
    
    Box(contentAlignment = Alignment.Center) {
        // Glow effect when enabled
        if (enabled) {
            Box(
                modifier = Modifier
                    .size(ChatDimens.SendButtonSize + 8.dp)
                    .blur(12.dp)
                    .clip(CircleShape)
                    .background(ChatColors.GlowPurple)
            )
        }
        
        // Button
        Box(
            modifier = modifier
                .size(ChatDimens.SendButtonSize)
                .clip(CircleShape)
                .background(backgroundBrush)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_send_message),
                contentDescription = "Gửi tin nhắn",
                tint = ChatColors.OnAccent,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

package com.projectapp.tempus.ui.ai.compose

import androidx.compose.foundation.background
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.R

/**
 * Chat input composable with text field and send button
 */
@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChatColors.Surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text input field
            ChatTextField(
                value = value,
                onValueChange = onValueChange,
                onSend = {
                    if (value.isNotBlank() && enabled) {
                        onSend()
                    }
                },
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
            
            // Send button
            SendButton(
                onClick = {
                    if (value.isNotBlank() && enabled) {
                        onSend()
                        focusManager.clearFocus()
                    }
                },
                enabled = value.isNotBlank() && enabled,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Custom text field for chat input
 */
@Composable
private fun ChatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = ChatColors.TextPrimary
        ),
        cursorBrush = SolidColor(ChatColors.Accent),
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
                    .background(
                        color = ChatColors.SurfaceVariant,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Nhập tin nhắn...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ChatColors.TextMuted
                    )
                }
                innerTextField()
            }
        },
        modifier = modifier
    )
}

/**
 * Circular send button with icon
 */
@Composable
private fun SendButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (enabled) ChatColors.Accent else ChatColors.TextMuted
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .size(ChatDimens.SendButtonSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = ChatColors.Surface),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_send_message),
            contentDescription = "Gửi tin nhắn",
            tint = ChatColors.Surface,
            modifier = Modifier.size(24.dp)
        )
    }
}

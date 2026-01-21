package com.projectapp.tempus.ui.ai.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.R
import com.projectapp.tempus.data.ai.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * User message bubble - aligned right with accent color
 */
@Composable
fun UserMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.75f
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = ChatDimens.BubbleCornerRadius,
                topEnd = ChatDimens.BubbleCornerRadius,
                bottomStart = ChatDimens.BubbleCornerRadius,
                bottomEnd = ChatDimens.BubbleSmallCorner
            ),
            color = ChatColors.UserBubble,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = maxBubbleWidth)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = ChatColors.UserBubbleText,
                modifier = Modifier.padding(ChatDimens.BubblePadding)
            )
        }
        
        // Timestamp
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = ChatColors.TextMuted,
            modifier = Modifier.padding(top = 4.dp, end = 4.dp)
        )
    }
}

/**
 * AI message bubble - aligned left with avatar
 */
@Composable
fun AIMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.75f
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // AI Avatar
        Image(
            painter = painterResource(id = R.drawable.ic_ai),
            contentDescription = "AI Avatar",
            modifier = Modifier
                .size(ChatDimens.AvatarSize)
                .clip(CircleShape)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Surface(
                shape = RoundedCornerShape(
                    topStart = ChatDimens.BubbleSmallCorner,
                    topEnd = ChatDimens.BubbleCornerRadius,
                    bottomStart = ChatDimens.BubbleCornerRadius,
                    bottomEnd = ChatDimens.BubbleCornerRadius
                ),
                color = ChatColors.AIBubble,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = maxBubbleWidth)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ChatColors.AIBubbleText,
                    modifier = Modifier.padding(ChatDimens.BubblePadding)
                )
            }
            
            // Timestamp
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = ChatColors.TextMuted,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

/**
 * Typing indicator with animated dots
 */
@Composable
fun TypingIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)) + slideInVertically { it },
        exit = fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            // AI Avatar
            Image(
                painter = painterResource(id = R.drawable.ic_ai),
                contentDescription = "AI Avatar",
                modifier = Modifier
                    .size(ChatDimens.AvatarSize)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                shape = RoundedCornerShape(
                    topStart = ChatDimens.BubbleSmallCorner,
                    topEnd = ChatDimens.BubbleCornerRadius,
                    bottomStart = ChatDimens.BubbleCornerRadius,
                    bottomEnd = ChatDimens.BubbleCornerRadius
                ),
                color = ChatColors.AIBubble,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypingDot(delayMillis = 0)
                    TypingDot(delayMillis = 150)
                    TypingDot(delayMillis = 300)
                }
            }
        }
    }
}

/**
 * Single animated dot for typing indicator
 */
@Composable
private fun TypingDot(
    delayMillis: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    
    Box(
        modifier = modifier
            .size(8.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(ChatColors.TextMuted)
    )
}

/**
 * Message bubble wrapper that displays the correct type
 */
@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    if (message.isFromUser) {
        UserMessageBubble(message = message, modifier = modifier)
    } else {
        AIMessageBubble(message = message, modifier = modifier)
    }
}

/**
 * Format timestamp to HH:mm
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

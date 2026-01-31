package com.projectapp.tempus.ui.ai.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.R
import com.projectapp.tempus.data.ai.repo.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun UserMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.78f
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.End
    ) {
        
        Box {
            
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 2.dp, y = 4.dp)
                    .blur(16.dp)
                    .clip(RoundedCornerShape(
                        topStart = ChatDimens.BubbleCornerRadius,
                        topEnd = ChatDimens.BubbleCornerRadius,
                        bottomStart = ChatDimens.BubbleCornerRadius,
                        bottomEnd = ChatDimens.BubbleSmallCorner
                    ))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            )
            
            
            Box(
                modifier = Modifier
                    .widthIn(max = maxBubbleWidth)
                    .clip(RoundedCornerShape(
                        topStart = ChatDimens.BubbleCornerRadius,
                        topEnd = ChatDimens.BubbleCornerRadius,
                        bottomStart = ChatDimens.BubbleCornerRadius,
                        bottomEnd = ChatDimens.BubbleSmallCorner
                    ))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .padding(
                        horizontal = ChatDimens.BubblePaddingHorizontal,
                        vertical = ChatDimens.BubblePadding
                    )
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, end = 4.dp)
        )
    }
}


@Composable
fun AIMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.78f
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        
        Box {
            
            Box(
                modifier = Modifier
                    .size(ChatDimens.AvatarSize + 4.dp)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .blur(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            )
            
            
            Box(
                modifier = Modifier
                    .size(ChatDimens.AvatarSize)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ai),
                    contentDescription = "AI Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Column {
            
            Box(
                modifier = Modifier
                    .widthIn(max = maxBubbleWidth)
                    .clip(RoundedCornerShape(
                        topStart = ChatDimens.BubbleSmallCorner,
                        topEnd = ChatDimens.BubbleCornerRadius,
                        bottomStart = ChatDimens.BubbleCornerRadius,
                        bottomEnd = ChatDimens.BubbleCornerRadius
                    ))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(
                            topStart = ChatDimens.BubbleSmallCorner,
                            topEnd = ChatDimens.BubbleCornerRadius,
                            bottomStart = ChatDimens.BubbleCornerRadius,
                            bottomEnd = ChatDimens.BubbleCornerRadius
                        )
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(
                        horizontal = ChatDimens.BubblePaddingHorizontal,
                        vertical = ChatDimens.BubblePadding
                    )
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }
            
            
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}


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
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            
            Box {
                
                val infiniteTransition = rememberInfiniteTransition(label = "avatarGlow")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )
                
                Box(
                    modifier = Modifier
                        .size(ChatDimens.AvatarSize + 8.dp)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .blur(12.dp)
                        .clip(CircleShape)
                        .background(ChatColors.Typing.copy(alpha = glowAlpha))
                )
                
                Box(
                    modifier = Modifier
                        .size(ChatDimens.AvatarSize)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ChatColors.Typing,
                                    ChatColors.TypingGlow
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_ai),
                        contentDescription = "AI Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(
                        topStart = ChatDimens.BubbleSmallCorner,
                        topEnd = ChatDimens.BubbleCornerRadius,
                        bottomStart = ChatDimens.BubbleCornerRadius,
                        bottomEnd = ChatDimens.BubbleCornerRadius
                    ))
                    .border(
                        width = 1.dp,
                        color = ChatColors.Typing.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(
                            topStart = ChatDimens.BubbleSmallCorner,
                            topEnd = ChatDimens.BubbleCornerRadius,
                            bottomStart = ChatDimens.BubbleCornerRadius,
                            bottomEnd = ChatDimens.BubbleCornerRadius
                        )
                    )
                    .background(ChatColors.AIBubble)
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
                    TypingDot(delayMillis = 200)
                    TypingDot(delayMillis = 400)
                }
            }
        }
    }
}


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
            animation = tween(600, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )
    
    Box(
        modifier = modifier
            .size((8 * scale).dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ChatColors.Typing,
                        ChatColors.TypingGlow
                    )
                )
            )
    )
}


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


private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

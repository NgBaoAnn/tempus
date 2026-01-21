package com.projectapp.tempus.ui.timer.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.R
import java.util.Locale

/**
 * Animated circular progress indicator for timer
 */
@Composable
fun CircularCountdown(
    progress: Float, // 0f to 1f
    timerColor: Color,
    timeText: String,
    statusText: String,
    isRunning: Boolean,
    endTimeText: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "progress"
    )
    
    val statusColor by animateColorAsState(
        targetValue = if (isRunning) TimerColors.Running else TimerColors.Paused,
        animationSpec = tween(200),
        label = "statusColor"
    )
    
    Box(
        modifier = modifier.size(TimerDimens.ProgressSize),
        contentAlignment = Alignment.Center
    ) {
        // Background track
        Canvas(modifier = Modifier.size(TimerDimens.ProgressSize)) {
            drawArc(
                color = TimerColors.TrackBackground,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(
                    width = TimerDimens.ProgressTrackWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
        
        // Progress arc
        Canvas(modifier = Modifier.size(TimerDimens.ProgressSize)) {
            drawArc(
                color = timerColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(
                    width = TimerDimens.ProgressTrackWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time display
            Text(
                text = timeText,
                style = TimerTypography.TimeDisplay
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isRunning) R.drawable.ic_play else R.drawable.ic_pause
                    ),
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    style = TimerTypography.StatusText,
                    color = statusColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // End time
            Text(
                text = endTimeText,
                style = TimerTypography.BodyMedium
            )
        }
    }
}

/**
 * Quick time selection buttons
 */
@Composable
fun QuickSelectButtons(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("1min", "5min", "30min", "1h", "Tùy chỉnh")
    val interactionSource = remember { MutableInteractionSource() }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = TimerColors.SurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEachIndexed { index, label ->
                val isSelected = selectedIndex == index
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) TimerColors.Surface else Color.Transparent,
                    animationSpec = tween(200),
                    label = "bgColor"
                )
                
                Box(
                    modifier = Modifier
                        .weight(if (index == 4) 1.5f else 1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onSelect(index) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = TimerTypography.ButtonText,
                        color = if (isSelected) TimerColors.TextPrimary else TimerColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Simple time display for setup mode
 */
@Composable
fun TimePickerDisplay(
    hours: Int,
    minutes: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hours display
        TimeUnitPicker(
            value = hours,
            label = "giờ",
            onIncrease = { onHoursChange((hours + 1).coerceAtMost(23)) },
            onDecrease = { onHoursChange((hours - 1).coerceAtLeast(0)) }
        )
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Minutes display  
        TimeUnitPicker(
            value = minutes,
            label = "phút",
            onIncrease = { onMinutesChange((minutes + 1) % 60) },
            onDecrease = { onMinutesChange((minutes - 1 + 60) % 60) }
        )
    }
}

@Composable
private fun TimeUnitPicker(
    value: Int,
    label: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up arrow
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onIncrease
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_up),
                contentDescription = "Increase",
                tint = TimerColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Value
        Text(
            text = String.format(Locale.getDefault(), "%02d", value),
            style = TimerTypography.TimeDisplay.copy(fontSize = TimerDimens.TimeTextSizeSmall)
        )
        
        // Label
        Text(
            text = label,
            style = TimerTypography.LabelSmall
        )
        
        // Down arrow
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onDecrease
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_down),
                contentDescription = "Decrease",
                tint = TimerColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Color selector for timer theme
 */
@Composable
fun ColorSelector(
    selectedColor: Color,
    onColorSelect: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        TimerColors.TimerRed,
        TimerColors.TimerOrange,
        TimerColors.TimerGreen,
        TimerColors.TimerBlue,
        TimerColors.TimerPurple,
        TimerColors.TimerTeal
    )
    val interactionSource = remember { MutableInteractionSource() }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        colors.forEach { color ->
            val isSelected = color == selectedColor
            
            Box(
                modifier = Modifier
                    .padding(horizontal = TimerDimens.ColorCircleSpacing / 2)
                    .size(TimerDimens.ColorCircleSize)
                    .clip(CircleShape)
                    .background(color)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onColorSelect(color) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Control button (circular)
 */
@Composable
fun ControlButton(
    iconResId: Int,
    backgroundColor: Color,
    iconTint: Color = Color.White,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .size(TimerDimens.ControlButtonSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Action button (rectangular)
 */
@Composable
fun ActionButton(
    iconResId: Int,
    backgroundColor: Color,
    iconTint: Color = Color.White,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .height(TimerDimens.StartButtonHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
    }
}

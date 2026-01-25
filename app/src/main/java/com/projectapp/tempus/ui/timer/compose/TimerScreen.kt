package com.projectapp.tempus.ui.timer.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.projectapp.tempus.R
import java.util.Calendar
import java.util.Locale

/**
 * Timer states
 */
enum class TimerState {
    SETUP,
    RUNNING,
    PAUSED
}

/**
 * Main Timer Screen composable
 */
@Composable
fun TimerScreen(
    timerState: TimerState,
    hours: Int,
    minutes: Int,
    secondsRemaining: Long,
    totalSeconds: Long,
    selectedQuickIndex: Int,
    selectedColor: Color,
    focusModeEnabled: Boolean = false,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onQuickSelect: (Int) -> Unit,
    onColorSelect: (Color) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onReset: () -> Unit,
    onNotesClick: () -> Unit = {},
    onFocusSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TimerColors.Background)
            .padding(20.dp)
    ) {
        // Header
        Text(
            text = if (timerState == TimerState.SETUP) "Hẹn giờ" else "Đếm ngược",
            style = TimerTypography.HeaderLarge
        )
        
        Spacer(modifier = Modifier.height(TimerDimens.SpacingLarge))
        
        // Animated content switch between setup and running
        AnimatedContent(
            targetState = timerState,
            transitionSpec = {
                (fadeIn() + slideInVertically { it / 2 }) togetherWith
                        (fadeOut() + slideOutVertically { -it / 2 })
            },
            label = "timerContent"
        ) { state ->
            when (state) {
                TimerState.SETUP -> SetupContent(
                    hours = hours,
                    minutes = minutes,
                    selectedQuickIndex = selectedQuickIndex,
                    selectedColor = selectedColor,
                    focusModeEnabled = focusModeEnabled,
                    onHoursChange = onHoursChange,
                    onMinutesChange = onMinutesChange,
                    onQuickSelect = onQuickSelect,
                    onColorSelect = onColorSelect,
                    onStart = onStart,
                    onReset = onReset,
                    onNotesClick = onNotesClick,
                    onFocusSettingsClick = onFocusSettingsClick
                )
                TimerState.RUNNING, TimerState.PAUSED -> RunningContent(
                    isRunning = state == TimerState.RUNNING,
                    secondsRemaining = secondsRemaining,
                    totalSeconds = totalSeconds,
                    timerColor = selectedColor,
                    onPauseResume = if (state == TimerState.RUNNING) onPause else onResume,
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
private fun SetupContent(
    hours: Int,
    minutes: Int,
    selectedQuickIndex: Int,
    selectedColor: Color,
    focusModeEnabled: Boolean,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onQuickSelect: (Int) -> Unit,
    onColorSelect: (Color) -> Unit,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onNotesClick: () -> Unit,
    onFocusSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        // Setup Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TimerDimens.CardCornerRadius),
            color = TimerColors.Surface,
            shadowElevation = TimerDimens.CardElevation
        ) {
            Column(
                modifier = Modifier.padding(TimerDimens.CardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quick select buttons
                QuickSelectButtons(
                    selectedIndex = selectedQuickIndex,
                    onSelect = onQuickSelect
                )
                
                Spacer(modifier = Modifier.height(TimerDimens.SpacingXLarge))
                
                // Time picker
                TimePickerDisplay(
                    hours = hours,
                    minutes = minutes,
                    onHoursChange = onHoursChange,
                    onMinutesChange = onMinutesChange
                )
                
                Spacer(modifier = Modifier.height(TimerDimens.SpacingXLarge))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reset button
                    ActionButton(
                        iconResId = R.drawable.ic_close,
                        backgroundColor = TimerColors.SurfaceVariant,
                        iconTint = TimerColors.TextMuted,
                        onClick = onReset,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Start button
                    ActionButton(
                        iconResId = R.drawable.ic_play,
                        backgroundColor = selectedColor,
                        iconTint = Color.White,
                        onClick = onStart,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(TimerDimens.SpacingMedium))
                
                HorizontalDivider(
                    color = TimerColors.SurfaceVariant,
                    thickness = 1.dp
                )
                
                Spacer(modifier = Modifier.height(TimerDimens.SpacingMedium))
                
                // Quick Notes button - prominent position
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNotesClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F9FF) // Light blue background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📝",
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ghi chú nhanh",
                                    style = TimerTypography.BodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Ghi lại ý tưởng khi tập trung",
                                    fontSize = 12.sp,
                                    color = TimerColors.TextMuted
                                )
                            }
                        }
                        Text(
                            text = "→",
                            fontSize = 20.sp,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(TimerDimens.SpacingSmall))
                
                // Focus Mode button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFocusSettingsClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = if (focusModeEnabled) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎯",
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Focus Mode",
                                    style = TimerTypography.BodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (focusModeEnabled) "Đang bật - Chặn app phân tâm" else "Chặn app phân tâm khi tập trung",
                                    fontSize = 12.sp,
                                    color = TimerColors.TextMuted
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (focusModeEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (focusModeEnabled) "ON" else "OFF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(TimerDimens.SpacingLarge))
        
        // Settings Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TimerDimens.CardCornerRadius),
            color = TimerColors.Surface,
            shadowElevation = TimerDimens.CardElevation
        ) {
            Column(
                modifier = Modifier.padding(TimerDimens.CardPadding)
            ) {
                // Tag row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nhãn",
                        style = TimerTypography.BodyMedium
                    )
                    Text(
                        text = "Hẹn giờ",
                        style = TimerTypography.BodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(TimerDimens.SpacingMedium))
                
                HorizontalDivider(
                    color = TimerColors.SurfaceVariant,
                    thickness = 1.dp
                )
                
                Spacer(modifier = Modifier.height(TimerDimens.SpacingMedium))
                
                // Color selector
                ColorSelector(
                    selectedColor = selectedColor,
                    onColorSelect = onColorSelect
                )
            }
        }
    }
}

@Composable
private fun RunningContent(
    isRunning: Boolean,
    secondsRemaining: Long,
    totalSeconds: Long,
    timerColor: Color,
    onPauseResume: () -> Unit,
    onCancel: () -> Unit
) {
    val progress = if (totalSeconds > 0) {
        secondsRemaining.toFloat() / totalSeconds.toFloat()
    } else 0f
    
    val timeText = formatTime(secondsRemaining)
    val statusText = if (isRunning) "Đang chạy" else "Đã tạm dừng"
    val endTimeText = calculateEndTime(secondsRemaining)
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(TimerDimens.CardCornerRadius),
        color = TimerColors.Surface,
        shadowElevation = TimerDimens.CardElevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(TimerDimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Circular countdown
            CircularCountdown(
                progress = progress,
                timerColor = timerColor,
                timeText = timeText,
                statusText = statusText,
                isRunning = isRunning,
                endTimeText = endTimeText
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cancel button
                ControlButton(
                    iconResId = R.drawable.ic_close,
                    backgroundColor = TimerColors.SurfaceVariant,
                    iconTint = TimerColors.TextPrimary,
                    onClick = onCancel
                )
                
                // Pause/Resume button
                ControlButton(
                    iconResId = if (isRunning) R.drawable.ic_pause else R.drawable.ic_play,
                    backgroundColor = timerColor,
                    iconTint = Color.White,
                    onClick = onPauseResume
                )
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    
    return if (h > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}

private fun calculateEndTime(secondsRemaining: Long): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.SECOND, secondsRemaining.toInt())
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return String.format(Locale.getDefault(), "Kết thúc lúc %02d:%02d", hour, minute)
}

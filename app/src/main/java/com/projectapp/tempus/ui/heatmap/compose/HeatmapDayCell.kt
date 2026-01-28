package com.projectapp.tempus.ui.heatmap.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.domain.usecase.DayHeatmapData
import com.projectapp.tempus.domain.usecase.HeatLevel
import com.projectapp.tempus.ui.theme.TempusDesignSystem
import java.time.LocalDate

/**
 * Single day cell in the heatmap calendar grid.
 * Displays day number, completion indicator, and task count.
 * Background color represents completion rate.
 */
@Composable
fun HeatmapDayCell(
    day: DayHeatmapData?,  // null = padding cell (empty)
    isToday: Boolean = false,
    isSelected: Boolean = false,
    isDarkMode: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Handle padding cells (null days)
    if (day == null) {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .padding(2.dp)
        )
        return
    }
    
    // Animate background color changes
    val backgroundColor by animateColorAsState(
        targetValue = HeatmapColors.getBackgroundColor(day.heatLevel, isDarkMode),
        animationSpec = tween(durationMillis = 300),
        label = "bgColor"
    )
    
    val textColor = HeatmapColors.getTextColor(day.heatLevel, isDarkMode)
    val indicatorColor = HeatmapColors.getIndicatorColor(day.heatLevel)
    
    // Border for today/selected state
    val borderModifier = when {
        isSelected -> Modifier.border(
            width = 2.dp,
            color = TempusDesignSystem.Primary,
            shape = RoundedCornerShape(8.dp)
        )
        isToday -> Modifier.border(
            width = 2.dp,
            color = TempusDesignSystem.Primary.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        )
        else -> Modifier
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(borderModifier)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            // Day number
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center
            )
            
            // Completion indicator (only show if has tasks)
            if (day.totalTasks > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                    )
                    
                    Spacer(modifier = Modifier.width(3.dp))
                    
                    // Task count
                    Text(
                        text = "${day.completedTasks}/${day.totalTasks}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Empty/Padding cell for calendar grid alignment.
 */
@Composable
fun EmptyDayCell(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
    )
}

/**
 * Preview-style cell for skeleton loading.
 */
@Composable
fun SkeletonDayCell(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TempusDesignSystem.Slate200)
    )
}

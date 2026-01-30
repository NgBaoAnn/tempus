package com.projectapp.tempus.ui.statistics.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.domain.usecase.CategoryStats
import com.projectapp.tempus.domain.usecase.DayStats
import com.projectapp.tempus.domain.usecase.InsightsData
import com.projectapp.tempus.domain.usecase.TrendType

import androidx.compose.ui.res.stringResource
import com.projectapp.tempus.R

// StatsDesign object removed. Using MaterialTheme.colorScheme directly.
private val PomodoroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
)
private val SuccessGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF00C853), Color(0xFF69F0AE))
)

// ======================== DATA CLASSES ========================

data class StatisticsUiData(
    val rangeLabel: String = "",
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val dailyStats: List<DayStats> = emptyList(),
    val topCategories: List<CategoryStats> = emptyList(),
    val insights: InsightsData? = null,
    val isWeekMode: Boolean = true,
    val pomodoroCount: Int = 0,
    val pomodoroMinutes: Int = 0
)

// ======================== MAIN SCREEN ========================

@Composable
fun StatisticsScreen(
    uiData: StatisticsUiData,
    isLoading: Boolean,
    onModeChange: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenHeatmap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header
        HeaderSection()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Mode Toggle
        ModeToggle(
            isWeekMode = uiData.isWeekMode,
            onModeChange = onModeChange
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Heatmap Preview Card
        HeatmapPreviewCard(onClick = onOpenHeatmap)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Stats Overview Cards
        StatsOverviewRow(
            pomodoroCount = uiData.pomodoroCount,
            pomodoroMinutes = uiData.pomodoroMinutes,
            completedTasks = uiData.completedTasks,
            totalTasks = uiData.totalTasks
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Progress Card
        ProgressCard(
            rangeLabel = uiData.rangeLabel,
            totalTasks = uiData.totalTasks,
            completedTasks = uiData.completedTasks
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Chart Section
        ChartCard(
            dailyStats = uiData.dailyStats,
            isWeekMode = uiData.isWeekMode,
            rangeLabel = uiData.rangeLabel,
            onPrevious = onPrevious,
            onNext = onNext
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Insights Card
        uiData.insights?.let { insights ->
            InsightsCard(insights = insights)
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Categories Section
        if (uiData.topCategories.isNotEmpty()) {
            CategoriesCard(categories = uiData.topCategories)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ======================== HEADER ========================

@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Analytics,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.stats_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ======================== MODE TOGGLE ========================

@Composable
private fun ModeToggle(
    isWeekMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ToggleChip(
                text = stringResource(R.string.stats_week),
                icon = Icons.Outlined.DateRange,
                selected = isWeekMode,
                onClick = { onModeChange(true) },
                modifier = Modifier.weight(1f)
            )
            ToggleChip(
                text = stringResource(R.string.stats_month),
                icon = Icons.Outlined.CalendarMonth,
                selected = !isWeekMode,
                onClick = { onModeChange(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToggleChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (selected) 4.dp else 0.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

// ======================== STATS OVERVIEW ROW ========================

@Composable
private fun StatsOverviewRow(
    pomodoroCount: Int,
    pomodoroMinutes: Int,
    completedTasks: Int,
    totalTasks: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pomodoro Card
        StatCard(
            title = stringResource(R.string.stats_pomodoro),
            value = "$pomodoroCount",
            subtitle = "$pomodoroMinutes " + stringResource(R.string.stats_minutes),
            icon = Icons.Filled.Timer,
            iconTint = Color(0xFFFF6B6B),
            gradient = PomodoroGradient,
            modifier = Modifier.weight(1f)
        )
        
        // Tasks Card
        StatCard(
            title = stringResource(R.string.stats_completed),
            value = "$completedTasks",
            subtitle = "/ $totalTasks " + stringResource(R.string.stats_tasks),
            icon = Icons.Filled.CheckCircle,
            iconTint = Color(0xFF4CAF50),
            gradient = SuccessGradient,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = iconTint.copy(alpha = 0.15f),
                spotColor = iconTint.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ======================== PROGRESS CARD ========================

@Composable
private fun ProgressCard(
    rangeLabel: String,
    totalTasks: Int,
    completedTasks: Int
) {
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.stats_progress),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        text = rangeLabel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Circular Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 10.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.stats_completed),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                Column {
                    ProgressStatRow(
                        icon = Icons.Filled.Task,
                        label = stringResource(R.string.stats_total),
                        value = "$totalTasks",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressStatRow(
                        icon = Icons.Filled.CheckCircle,
                        label = stringResource(R.string.stats_completed),
                        value = "$completedTasks",
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressStatRow(
                        icon = Icons.Outlined.RadioButtonUnchecked,
                        label = stringResource(R.string.stats_remaining),
                        value = "${totalTasks - completedTasks}",
                        color = Color(0xFFFFC107)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressStatRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ======================== CHART CARD ========================

@Composable
private fun ChartCard(
    dailyStats: List<DayStats>,
    isWeekMode: Boolean,
    rangeLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.stats_chart),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bar Chart
            BarChart(
                dailyStats = dailyStats,
                isWeekMode = isWeekMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = stringResource(R.string.stats_prev),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        text = rangeLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = stringResource(R.string.stats_next),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun BarChart(
    dailyStats: List<DayStats>,
    isWeekMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (dailyStats.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.InsertChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.stats_no_data),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                    fontSize = 14.sp
                )
            }
        }
        return
    }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("100%", "75%", "50%", "25%", "0%").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
            
            // Colors for Canvas (captured outside)
            val gridLineColor = MaterialTheme.colorScheme.outlineVariant
            val barColor = MaterialTheme.colorScheme.primary
            
            // Bar chart canvas
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                
                // Draw horizontal grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = (chartHeight / gridLines) * i
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }
                
                // Draw bars
                val barWidthRatio = if (isWeekMode) 0.6f else 0.8f
                val barWidth = (chartWidth / dailyStats.size) * barWidthRatio
                val barSpacing = chartWidth / dailyStats.size
                
                dailyStats.forEachIndexed { index, dayStats ->
                    val barHeight = (dayStats.completionPercentage / 100f) * chartHeight
                    val x = (barSpacing * index) + (barSpacing - barWidth) / 2
                    val y = chartHeight - barHeight
                    
                    if (barHeight > 0) {
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // X-axis labels
        if (isWeekMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp)
            ) {
                dailyStats.forEach { stats ->
                    val label = when (stats.date.dayOfWeek.value) {
                        1 -> stringResource(R.string.day_mon)
                        2 -> stringResource(R.string.day_tue)
                        3 -> stringResource(R.string.day_wed)
                        4 -> stringResource(R.string.day_thu)
                        5 -> stringResource(R.string.day_fri)
                        6 -> stringResource(R.string.day_sat)
                        7 -> stringResource(R.string.day_sun)
                        else -> ""
                    }
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            val totalDays = dailyStats.size
            val milestoneDays = listOf(1, 5, 10, 15, 20, 25, totalDays)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                milestoneDays.filter { it <= totalDays }.forEach { day ->
                    Text(
                        text = day.toString(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ======================== INSIGHTS CARD ========================

@Composable
private fun InsightsCard(insights: InsightsData) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.stats_insight),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Average Rate
            InsightItem(
                icon = Icons.Outlined.Speed,
                label = stringResource(R.string.stats_completion_rate),
                value = String.format("%.1f%%", insights.avgCompletionRate),
                iconTint = MaterialTheme.colorScheme.primary
            )
            
            // Best Day
            insights.bestDay?.let { (day, rate) ->
                InsightItem(
                    icon = Icons.Filled.TrendingUp,
                    label = stringResource(R.string.stats_best_day),
                    value = "$day (${rate.toInt()}%)",
                    iconTint = Color(0xFF4CAF50)
                )
            }
            
            // Worst Day
            insights.worstDay?.let { (day, rate) ->
                InsightItem(
                    icon = Icons.Filled.TrendingDown,
                    label = stringResource(R.string.stats_improve),
                    value = "$day (${rate.toInt()}%)",
                    iconTint = MaterialTheme.colorScheme.error
                )
            }
            
            // Trend
            val (trendIcon, trendText, trendColor) = when (insights.trend) {
                TrendType.UP -> Triple(Icons.Filled.TrendingUp, stringResource(R.string.stats_trend_up), Color(0xFF4CAF50))
                TrendType.DOWN -> Triple(Icons.Filled.TrendingDown, stringResource(R.string.stats_trend_down), MaterialTheme.colorScheme.error)
                TrendType.STABLE -> Triple(Icons.Default.TrendingFlat, stringResource(R.string.stats_trend_stable), MaterialTheme.colorScheme.onSurfaceVariant)
            }
            InsightItem(
                icon = trendIcon,
                label = stringResource(R.string.stats_trend_label),
                value = trendText,
                iconTint = trendColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Suggestion Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TipsAndUpdates,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = insights.suggestion,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ======================== CATEGORIES CARD ========================

// Category icon mapping
private fun getCategoryIcon(label: String): ImageVector = when (label) {
    "wakeup" -> Icons.Outlined.WbSunny
    "eat" -> Icons.Outlined.Restaurant
    "exercise" -> Icons.Outlined.FitnessCenter
    "rest" -> Icons.Outlined.Weekend
    "water" -> Icons.Outlined.WaterDrop
    "book" -> Icons.Outlined.MenuBook
    "sleep" -> Icons.Outlined.Bedtime
    "clean" -> Icons.Outlined.CleaningServices
    "cook" -> Icons.Outlined.SoupKitchen
    "garden" -> Icons.Outlined.Yard
    else -> Icons.Outlined.Label
}

private fun getCategoryNameResId(label: String): Int? = when (label) {
    "wakeup" -> R.string.cat_wakeup
    "eat" -> R.string.cat_eat
    "exercise" -> R.string.cat_exercise
    "rest" -> R.string.cat_rest
    "water" -> R.string.cat_water
    "book" -> R.string.cat_study
    "sleep" -> R.string.cat_sleep
    "clean" -> R.string.cat_clean
    "cook" -> R.string.cat_cook
    "garden" -> R.string.cat_garden
    else -> null
}

@Composable
private fun CategoriesCard(categories: List<CategoryStats>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.stats_by_category),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            categories.take(5).forEach { category ->
                CategoryItem(category = category)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CategoryItem(category: CategoryStats) {
    val progress = category.percentage / 100f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "category_progress"
    )
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getCategoryIcon(category.label),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                
                val nameResId = getCategoryNameResId(category.label)
                Text(
                    text = if (nameResId != null) stringResource(nameResId) else category.label.replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${category.completedCount}/${category.totalCount} (${category.percentage}%)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
            )
        }
    }
}

// ======================== HEATMAP PREVIEW CARD ========================

@Composable
private fun HeatmapPreviewCard(
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = stringResource(R.string.heatmap_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.heatmap_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.heatmap_open_desc),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

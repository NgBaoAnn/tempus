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

// ======================== MODERN DESIGN SYSTEM ========================

private object StatsDesign {
    // Primary Palette
    val Primary = Color(0xFF3B82F6)        // Blue 500
    val PrimaryLight = Color(0xFF60A5FA)    // Blue 400
    val PrimaryDark = Color(0xFF1D4ED8)     // Blue 700
    
    // Accent Colors
    val Accent = Color(0xFF8B5CF6)          // Violet 500
    val Success = Color(0xFF10B981)         // Emerald 500
    val Warning = Color(0xFFF59E0B)         // Amber 500
    val Error = Color(0xFFEF4444)           // Red 500
    
    // Backgrounds
    val Background = Color(0xFFF8FAFC)      // Slate 50
    val Surface = Color(0xFFFFFFFF)
    val SurfaceElevated = Color(0xFFF1F5F9) // Slate 100
    
    // Text Colors
    val TextPrimary = Color(0xFF0F172A)     // Slate 900
    val TextSecondary = Color(0xFF475569)   // Slate 600
    val TextMuted = Color(0xFF94A3B8)       // Slate 400
    
    // Chart Colors
    val ChartBar = Color(0xFF3B82F6)
    val ChartBarLight = Color(0xFF60A5FA)
    val GridLine = Color(0xFFE2E8F0)        // Slate 200
    
    // Gradients
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
    )
    val SuccessGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF10B981), Color(0xFF34D399))
    )
    val PomodoroGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
    )
}

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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StatsDesign.Background)
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
            tint = StatsDesign.Primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Thống kê",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = StatsDesign.TextPrimary
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
        color = StatsDesign.SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ToggleChip(
                text = "Tuần",
                icon = Icons.Outlined.DateRange,
                selected = isWeekMode,
                onClick = { onModeChange(true) },
                modifier = Modifier.weight(1f)
            )
            ToggleChip(
                text = "Tháng",
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
            containerColor = if (selected) StatsDesign.Primary else Color.Transparent,
            contentColor = if (selected) Color.White else StatsDesign.TextSecondary
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
            title = "Pomodoro",
            value = "$pomodoroCount",
            subtitle = "$pomodoroMinutes phút",
            icon = Icons.Filled.Timer,
            iconTint = Color(0xFFFF6B6B),
            gradient = StatsDesign.PomodoroGradient,
            modifier = Modifier.weight(1f)
        )
        
        // Tasks Card
        StatCard(
            title = "Hoàn thành",
            value = "$completedTasks",
            subtitle = "/ $totalTasks tác vụ",
            icon = Icons.Filled.CheckCircle,
            iconTint = StatsDesign.Success,
            gradient = StatsDesign.SuccessGradient,
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
        color = StatsDesign.Surface
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
                    color = StatsDesign.TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = StatsDesign.TextPrimary
            )
            
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = StatsDesign.TextMuted
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
                ambientColor = StatsDesign.Primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = StatsDesign.Surface
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
                        tint = StatsDesign.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tiến độ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatsDesign.TextPrimary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatsDesign.SurfaceElevated
                ) {
                    Text(
                        text = rangeLabel,
                        fontSize = 12.sp,
                        color = StatsDesign.TextSecondary,
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
                        color = StatsDesign.Primary,
                        trackColor = StatsDesign.GridLine
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatsDesign.TextPrimary
                        )
                        Text(
                            text = "Hoàn thành",
                            fontSize = 11.sp,
                            color = StatsDesign.TextMuted
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                Column {
                    ProgressStatRow(
                        icon = Icons.Filled.Task,
                        label = "Tổng cộng",
                        value = "$totalTasks",
                        color = StatsDesign.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressStatRow(
                        icon = Icons.Filled.CheckCircle,
                        label = "Đã hoàn thành",
                        value = "$completedTasks",
                        color = StatsDesign.Success
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressStatRow(
                        icon = Icons.Outlined.RadioButtonUnchecked,
                        label = "Còn lại",
                        value = "${totalTasks - completedTasks}",
                        color = StatsDesign.Warning
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
            color = StatsDesign.TextSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = StatsDesign.TextPrimary
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
                ambientColor = StatsDesign.Primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = StatsDesign.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = StatsDesign.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Biểu đồ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatsDesign.TextPrimary
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
                        contentDescription = "Trước",
                        tint = StatsDesign.Primary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatsDesign.SurfaceElevated
                ) {
                    Text(
                        text = rangeLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatsDesign.TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Sau",
                        tint = StatsDesign.Primary
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
                    tint = StatsDesign.TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chưa có dữ liệu",
                    color = StatsDesign.TextMuted,
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
                        color = StatsDesign.TextMuted,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
            
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
                        color = StatsDesign.GridLine,
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
                            color = StatsDesign.ChartBar,
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
                        1 -> "T2"
                        2 -> "T3"
                        3 -> "T4"
                        4 -> "T5"
                        5 -> "T6"
                        6 -> "T7"
                        7 -> "CN"
                        else -> ""
                    }
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = StatsDesign.TextSecondary,
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
                        color = StatsDesign.TextSecondary
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
                ambientColor = StatsDesign.Accent.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = StatsDesign.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = StatsDesign.Warning,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Phân tích",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatsDesign.TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Average Rate
            InsightItem(
                icon = Icons.Outlined.Speed,
                label = "Tỷ lệ hoàn thành TB",
                value = String.format("%.1f%%", insights.avgCompletionRate),
                iconTint = StatsDesign.Primary
            )
            
            // Best Day
            insights.bestDay?.let { (day, rate) ->
                InsightItem(
                    icon = Icons.Filled.TrendingUp,
                    label = "Ngày tốt nhất",
                    value = "$day (${rate.toInt()}%)",
                    iconTint = StatsDesign.Success
                )
            }
            
            // Worst Day
            insights.worstDay?.let { (day, rate) ->
                InsightItem(
                    icon = Icons.Filled.TrendingDown,
                    label = "Cần cải thiện",
                    value = "$day (${rate.toInt()}%)",
                    iconTint = StatsDesign.Error
                )
            }
            
            // Trend
            val (trendIcon, trendText, trendColor) = when (insights.trend) {
                TrendType.UP -> Triple(Icons.Filled.TrendingUp, "Đang tăng", StatsDesign.Success)
                TrendType.DOWN -> Triple(Icons.Filled.TrendingDown, "Đang giảm", StatsDesign.Error)
                TrendType.STABLE -> Triple(Icons.Filled.TrendingFlat, "Ổn định", StatsDesign.TextSecondary)
            }
            InsightItem(
                icon = trendIcon,
                label = "Xu hướng",
                value = trendText,
                iconTint = trendColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Suggestion Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = StatsDesign.Primary.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TipsAndUpdates,
                        contentDescription = null,
                        tint = StatsDesign.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = insights.suggestion,
                        fontSize = 14.sp,
                        color = StatsDesign.TextPrimary,
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
                color = StatsDesign.TextSecondary
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = StatsDesign.TextPrimary
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

private fun getCategoryName(label: String): String = when (label) {
    "wakeup" -> "Thức dậy"
    "eat" -> "Ăn uống"
    "exercise" -> "Tập luyện"
    "rest" -> "Nghỉ ngơi"
    "water" -> "Uống nước"
    "book" -> "Học tập"
    "sleep" -> "Ngủ"
    "clean" -> "Dọn dẹp"
    "cook" -> "Nấu ăn"
    "garden" -> "Làm vườn"
    else -> label.replaceFirstChar { it.uppercase() }
}

@Composable
private fun CategoriesCard(categories: List<CategoryStats>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = StatsDesign.Accent.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = StatsDesign.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Category,
                    contentDescription = null,
                    tint = StatsDesign.Accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Theo danh mục",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatsDesign.TextPrimary
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
                    tint = StatsDesign.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = getCategoryName(category.label),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = StatsDesign.TextPrimary
                )
            }
            Text(
                text = "${category.completedCount}/${category.totalCount} (${category.percentage}%)",
                fontSize = 13.sp,
                color = StatsDesign.TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StatsDesign.GridLine)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(StatsDesign.Primary, StatsDesign.Accent)
                        )
                    )
            )
        }
    }
}

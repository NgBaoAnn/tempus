package com.projectapp.tempus.ui.statistics.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.domain.usecase.CategoryStats
import com.projectapp.tempus.domain.usecase.DayStats
import com.projectapp.tempus.domain.usecase.InsightsData
import com.projectapp.tempus.domain.usecase.TrendType

// ======================== COLORS ========================

private object StatsColors {
    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFF8F9FA)
    val CardBg = Color(0xFFF5F5F5)
    val Primary = Color(0xFF3CDAEF)
    val PrimaryDark = Color(0xFF00B4D8)
    val TextPrimary = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF6B7280)
    val Green = Color(0xFF4CAF50)
    val Red = Color(0xFFFF5722)
    val Orange = Color(0xFFFF9800)
    val Purple = Color(0xFF9C27B0)
    val GridLine = Color(0xFFE0E0E0)
    val ChartBar = Color(0xFF3CDAEF)
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
    onModeChange: (Boolean) -> Unit, // true = Week, false = Month
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StatsColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Thống kê",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = StatsColors.TextPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toggle Week/Month
        ModeToggle(
            isWeekMode = uiData.isWeekMode,
            onModeChange = onModeChange
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Pomodoro Stats Card
        PomodoroStatsCard(
            pomodoroCount = uiData.pomodoroCount,
            pomodoroMinutes = uiData.pomodoroMinutes
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summary Card
        SummaryCard(
            rangeLabel = uiData.rangeLabel,
            totalTasks = uiData.totalTasks,
            completedTasks = uiData.completedTasks
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chart Section
        ChartSection(
            dailyStats = uiData.dailyStats,
            isWeekMode = uiData.isWeekMode,
            rangeLabel = uiData.rangeLabel,
            onPrevious = onPrevious,
            onNext = onNext
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Insights Card
        uiData.insights?.let { insights ->
            InsightsCard(insights = insights)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Categories Section
        if (uiData.topCategories.isNotEmpty()) {
            CategoriesSection(categories = uiData.topCategories)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ======================== MODE TOGGLE ========================

@Composable
private fun ModeToggle(
    isWeekMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StatsColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ToggleButton(
                text = "Tuần",
                selected = isWeekMode,
                onClick = { onModeChange(true) },
                modifier = Modifier.weight(1f)
            )
            ToggleButton(
                text = "Tháng",
                selected = !isWeekMode,
                onClick = { onModeChange(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) StatsColors.Primary else Color.Transparent,
            contentColor = if (selected) Color.White else StatsColors.TextSecondary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ======================== POMODORO STATS CARD ========================

@Composable
private fun PomodoroStatsCard(
    pomodoroCount: Int,
    pomodoroMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECB3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍅",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pomodoro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatsColors.TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "$pomodoroCount",
                    label = "Phiên",
                    emoji = "✅"
                )
                StatItem(
                    value = "$pomodoroMinutes",
                    label = "Phút tập trung",
                    emoji = "⏱️"
                )
                StatItem(
                    value = if (pomodoroCount > 0) "${pomodoroMinutes / pomodoroCount}" else "0",
                    label = "Phút/phiên",
                    emoji = "📊"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = StatsColors.TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = StatsColors.TextSecondary
        )
    }
}

// ======================== SUMMARY CARD ========================

@Composable
private fun SummaryCard(
    rangeLabel: String,
    totalTasks: Int,
    completedTasks: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatsColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tiến độ hoàn thành",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StatsColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = rangeLabel,
                fontSize = 14.sp,
                color = StatsColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 800),
                label = "progress"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StatsColors.GridLine)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatsColors.Primary)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$completedTasks / $totalTasks tác vụ đã hoàn thành (${(progress * 100).toInt()}%)",
                fontSize = 14.sp,
                color = StatsColors.TextSecondary
            )
        }
    }
}

// ======================== CHART SECTION ========================

@Composable
private fun ChartSection(
    dailyStats: List<DayStats>,
    isWeekMode: Boolean,
    rangeLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatsColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Biểu đồ hoàn thành",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StatsColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bar Chart using Canvas
            BarChart(
                dailyStats = dailyStats,
                isWeekMode = isWeekMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPrevious) {
                    Text("◀", fontSize = 18.sp)
                }
                
                Text(
                    text = rangeLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = StatsColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                TextButton(onClick = onNext) {
                    Text("▶", fontSize = 18.sp)
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
            Text(
                text = "Chưa có dữ liệu",
                color = StatsColors.TextSecondary,
                fontSize = 14.sp
            )
        }
        return
    }
    
    Column(modifier = modifier) {
        // Chart area with Y-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("100%", "75%", "50%", "25%", "0%").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = StatsColors.TextSecondary,
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
                        color = StatsColors.GridLine,
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
                    
                    // Draw bar with rounded corners
                    if (barHeight > 0) {
                        drawRoundRect(
                            color = StatsColors.ChartBar,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // X-axis labels
        if (isWeekMode) {
            // Week mode: show all 7 day labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp)
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
                        color = StatsColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Month mode: show only milestone days (1, 5, 10, 15, 20, 25, last day)
            val totalDays = dailyStats.size
            val milestoneDays = listOf(1, 5, 10, 15, 20, 25, totalDays)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                milestoneDays.filter { it <= totalDays }.forEach { day ->
                    Text(
                        text = day.toString(),
                        fontSize = 11.sp,
                        color = StatsColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ======================== INSIGHTS CARD ========================

@Composable
private fun InsightsCard(insights: InsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatsColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💡 Phân tích",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StatsColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Average Rate
            InsightRow(
                label = "Tỷ lệ hoàn thành TB",
                value = String.format("%.1f%%", insights.avgCompletionRate),
                valueColor = StatsColors.Primary
            )
            
            // Best Day
            insights.bestDay?.let { (day, rate) ->
                InsightRow(
                    label = "📈 Ngày tốt nhất",
                    value = "$day (${rate.toInt()}%)",
                    valueColor = StatsColors.Green
                )
            }
            
            // Worst Day
            insights.worstDay?.let { (day, rate) ->
                InsightRow(
                    label = "📉 Cần cải thiện",
                    value = "$day (${rate.toInt()}%)",
                    valueColor = StatsColors.Red
                )
            }
            
            // Trend
            InsightRow(
                label = "📊 Xu hướng",
                value = when (insights.trend) {
                    TrendType.UP -> "📈 Đang tăng"
                    TrendType.DOWN -> "📉 Đang giảm"
                    TrendType.STABLE -> "➡️ Ổn định"
                },
                valueColor = when (insights.trend) {
                    TrendType.UP -> StatsColors.Green
                    TrendType.DOWN -> StatsColors.Red
                    TrendType.STABLE -> StatsColors.TextSecondary
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Suggestion
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "💡 ${insights.suggestion}",
                    fontSize = 14.sp,
                    color = StatsColors.TextPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = StatsColors.TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

// ======================== CATEGORIES SECTION ========================

@Composable
private fun CategoriesSection(categories: List<CategoryStats>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatsColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🏷️ Theo danh mục",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StatsColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            categories.take(5).forEach { category ->
                CategoryItem(category = category)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryItem(category: CategoryStats) {
    val labelDisplayName = when (category.label) {
        "wakeup" -> "🌅 Thức dậy"
        "eat" -> "🍽️ Ăn uống"
        "exercise" -> "💪 Tập luyện"
        "rest" -> "😴 Nghỉ ngơi"
        "water" -> "💧 Uống nước"
        "book" -> "📚 Học tập"
        "sleep" -> "🌙 Ngủ"
        "clean" -> "🧹 Dọn dẹp"
        "cook" -> "🍳 Nấu ăn"
        "garden" -> "🌱 Làm vườn"
        else -> "📋 ${category.label}"
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = labelDisplayName,
                fontSize = 14.sp,
                color = StatsColors.TextPrimary
            )
            Text(
                text = "${category.completedCount}/${category.totalCount} (${category.percentage}%)",
                fontSize = 14.sp,
                color = StatsColors.TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Progress bar
        val progress = category.percentage / 100f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StatsColors.GridLine)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(StatsColors.Primary)
            )
        }
    }
}

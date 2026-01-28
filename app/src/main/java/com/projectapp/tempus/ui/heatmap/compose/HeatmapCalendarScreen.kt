package com.projectapp.tempus.ui.heatmap.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.domain.usecase.DayHeatmapData
import com.projectapp.tempus.domain.usecase.HeatLevel
import com.projectapp.tempus.domain.usecase.MonthHeatmapData
import com.projectapp.tempus.domain.usecase.MonthStats
import com.projectapp.tempus.ui.theme.TempusDesignSystem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Main Heatmap Calendar Screen.
 * Displays a monthly grid with color-coded completion rates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatmapCalendarScreen(
    monthData: MonthHeatmapData?,
    selectedDate: LocalDate?,
    isLoading: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onNavigatePrevMonth: () -> Unit,
    onNavigateNextMonth: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkMode = isSystemInDarkTheme()
    
    Scaffold(
        topBar = {
            HeatmapTopBar(
                yearMonth = monthData?.yearMonth ?: YearMonth.now(),
                onNavigateBack = onNavigateBack,
                onPrevMonth = onNavigatePrevMonth,
                onNextMonth = onNavigateNextMonth
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Month Stats Summary Card
            if (monthData != null) {
                MonthStatsCard(stats = monthData.monthStats)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Legend
            HeatmapLegend()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Calendar Grid
            if (isLoading) {
                HeatmapLoadingSkeleton()
            } else if (monthData != null) {
                HeatmapGrid(
                    monthData = monthData,
                    selectedDate = selectedDate,
                    isDarkMode = isDarkMode,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}

/**
 * Top App Bar with month navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeatmapTopBar(
    yearMonth: YearMonth,
    onNavigateBack: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))
    val monthText = yearMonth.atDay(1).format(formatter)
    
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Tháng trước",
                        tint = TempusDesignSystem.Primary
                    )
                }
                
                Text(
                    text = monthText.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Tháng sau",
                        tint = TempusDesignSystem.Primary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

/**
 * Month statistics summary card.
 */
@Composable
private fun MonthStatsCard(stats: MonthStats) {
    val completionPercent = (stats.avgCompletionRate * 100).toInt()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tổng quan tháng",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Completion Rate
                StatItem(
                    value = "$completionPercent%",
                    label = "Hoàn thành",
                    color = when {
                        completionPercent >= 80 -> TempusDesignSystem.Success
                        completionPercent >= 50 -> TempusDesignSystem.Warning
                        else -> TempusDesignSystem.Error
                    }
                )
                
                // Total Tasks
                StatItem(
                    value = "${stats.completedTasks}/${stats.totalTasks}",
                    label = "Tasks",
                    color = TempusDesignSystem.Primary
                )
                
                // Days with Tasks
                StatItem(
                    value = "${stats.daysWithTasks}",
                    label = "Ngày hoạt động",
                    color = TempusDesignSystem.Secondary
                )
            }
            
            // Best/Worst day info
            if (stats.bestDay != null || stats.worstDay != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    stats.bestDay?.let { best ->
                        val dayFormatter = DateTimeFormatter.ofPattern("d/M")
                        Text(
                            text = " Ngày tốt nhất: ${best.format(dayFormatter)}",
                            fontSize = 13.sp,
                            color = TempusDesignSystem.Success
                        )
                    }
                    
                    stats.worstDay?.let { worst ->
                        val dayFormatter = DateTimeFormatter.ofPattern("d/M")
                        Text(
                            text = " Cần cải thiện: ${worst.format(dayFormatter)}",
                            fontSize = 13.sp,
                            color = TempusDesignSystem.Warning
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Color legend for heatmap.
 */
@Composable
private fun HeatmapLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ít",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        // Color boxes
        listOf(
            HeatLevel.NONE,
            HeatLevel.LOW,
            HeatLevel.MEDIUM,
            HeatLevel.HIGH,
            HeatLevel.EXCELLENT
        ).forEach { level ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(HeatmapColors.getBackgroundColor(level, false))
            )
            Spacer(modifier = Modifier.width(3.dp))
        }
        
        Text(
            text = "Nhiều",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Calendar grid with Monday-Sunday layout.
 */
@Composable
private fun HeatmapGrid(
    monthData: MonthHeatmapData,
    selectedDate: LocalDate?,
    isDarkMode: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    
    // Build calendar cells with padding for alignment
    val cells = buildCalendarCells(monthData)
    
    Column {
        // Weekday headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid - 7 columns
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            userScrollEnabled = false
        ) {
            items(cells) { day ->
                HeatmapDayCell(
                    day = day,
                    isToday = day?.date == today,
                    isSelected = day?.date == selectedDate,
                    isDarkMode = isDarkMode,
                    onClick = {
                        day?.let { onDateSelected(it.date) }
                    }
                )
            }
        }
    }
}

/**
 * Build calendar cells with padding for month alignment.
 * Ensures first day of month aligns with correct weekday.
 */
private fun buildCalendarCells(monthData: MonthHeatmapData): List<DayHeatmapData?> {
    val cells = mutableListOf<DayHeatmapData?>()
    
    val firstDayOfMonth = monthData.yearMonth.atDay(1)
    val startOffset = (firstDayOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    
    // Add empty padding cells before first day
    repeat(startOffset) {
        cells.add(null)
    }
    
    // Add actual days
    cells.addAll(monthData.days)
    
    // Add padding at end to complete the last week
    while (cells.size % 7 != 0) {
        cells.add(null)
    }
    
    return cells
}

/**
 * Loading skeleton for calendar.
 */
@Composable
private fun HeatmapLoadingSkeleton() {
    Column {
        // Weekday headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Skeleton grid - 6 rows × 7 columns
        repeat(6) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { col ->
                    SkeletonDayCell(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

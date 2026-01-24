package com.projectapp.tempus.ui.timeline.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R
import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.domain.model.TimelineBlock
import com.projectapp.tempus.ui.timeline.SortOption
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Color definitions
object TimelineColors {
    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val Primary = Color(0xFF3B82F6)
    val TextPrimary = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)
    val TextMuted = Color(0xFF94A3B8)
    val PriorityHigh = Color(0xFFEF4444)
    val PriorityMedium = Color(0xFFF59E0B)
    val PriorityLow = Color(0xFF22C55E)
    val TimelineGray = Color(0xFFE2E8F0)
}

@Composable
fun TimelineScreen(
    blocks: List<TimelineBlock>,
    selectedDate: LocalDate,
    monthYear: String,
    weeks: List<List<LocalDate>>,
    // Search/Sort/Filter state
    searchQuery: String = "",
    sortBy: SortOption = SortOption.START_TIME,
    filterLabels: Set<ScheduleLabel> = emptySet(),
    filterPriorities: Set<PriorityType> = emptySet(),
    filterStatus: StatusType? = null,
    isFilterActive: Boolean = false,
    // Callbacks
    onDateSelected: (LocalDate) -> Unit,
    onMonthPickerClick: () -> Unit,
    onAddClick: () -> Unit,
    onTaskClick: (TimelineBlock) -> Unit,
    onStatusToggle: (TimelineBlock) -> Unit,
    // Search/Sort/Filter callbacks
    onSearchQueryChanged: (String) -> Unit = {},
    onSortChanged: (SortOption) -> Unit = {},
    onFilterLabelToggle: (ScheduleLabel) -> Unit = {},
    onFilterPriorityToggle: (PriorityType) -> Unit = {},
    onFilterStatusChanged: (StatusType?) -> Unit = {},
    onClearAllFilters: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.background(TimelineColors.Background),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = TimelineColors.Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = TimelineColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar
            TimelineTopBar(
                monthYear = monthYear,
                onMonthPickerClick = onMonthPickerClick
            )
            
            // Week Calendar Strip
            WeekCalendarStrip(
                weeks = weeks,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
            
            // Filter/Sort Bar
            FilterSortBar(
                searchQuery = searchQuery,
                sortBy = sortBy,
                filterLabels = filterLabels,
                filterPriorities = filterPriorities,
                filterStatus = filterStatus,
                isFilterActive = isFilterActive,
                onSearchQueryChanged = onSearchQueryChanged,
                onSortChanged = onSortChanged,
                onFilterLabelToggle = onFilterLabelToggle,
                onFilterPriorityToggle = onFilterPriorityToggle,
                onFilterStatusChanged = onFilterStatusChanged,
                onClearAllFilters = onClearAllFilters
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Timeline List
            if (blocks.isEmpty()) {
                EmptyState(onAddClick = onAddClick)
            } else {
                TimelineList(
                    blocks = blocks,
                    onTaskClick = onTaskClick,
                    onStatusToggle = onStatusToggle
                )
            }
        }
    }
}


@Composable
private fun TimelineTopBar(
    monthYear: String,
    onMonthPickerClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month Picker
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(TimelineColors.Primary.copy(alpha = 0.1f))
                .clickable { onMonthPickerClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthYear,
                color = TimelineColors.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_unfold_more),
                contentDescription = null,
                tint = TimelineColors.Primary,
                modifier = Modifier.size(16.dp)
            )
        }
        
        // Inbox icon
        IconButton(onClick = { }) {
            Icon(
                painter = painterResource(id = R.drawable.inbox),
                contentDescription = "Inbox",
                tint = TimelineColors.Primary
            )
        }
    }
}

@Composable
fun WeekCalendarStrip(
    weeks: List<List<LocalDate>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentWeek = weeks.find { week -> 
        week.any { it == selectedDate } 
    } ?: weeks.firstOrNull() ?: return
    
    Column {
        // Day labels (T2, T3, T4, T5, T6, T7, CN)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                Text(
                    text = day,
                    color = TimelineColors.TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Day numbers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            currentWeek.forEach { date ->
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> TimelineColors.Primary
                                else -> Color.Transparent
                            }
                        )
                        .then(
                            if (isToday && !isSelected) {
                                Modifier
                                    .background(Color.Transparent)
                                    .clip(CircleShape)
                            } else Modifier
                        )
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = when {
                            isSelected -> Color.White
                            isToday -> TimelineColors.Primary
                            else -> TimelineColors.TextPrimary
                        },
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineList(
    blocks: List<TimelineBlock>,
    onTaskClick: (TimelineBlock) -> Unit,
    onStatusToggle: (TimelineBlock) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        itemsIndexed(
            items = blocks,
            key = { _, block -> block.taskId }
        ) { index, block ->
            val nextBlock = blocks.getOrNull(index + 1)
            val showFreeTime = nextBlock != null && 
                block.startTime.plus(block.duration).isBefore(nextBlock.startTime)
            
            TimelineItem(
                block = block,
                isLast = index == blocks.lastIndex,
                onTaskClick = { onTaskClick(block) },
                onStatusToggle = { onStatusToggle(block) }
            )
            
            if (showFreeTime && nextBlock != null) {
                val freeStart = block.startTime.plus(block.duration)
                val freeEnd = nextBlock.startTime
                val freeMinutes = java.time.Duration.between(freeStart, freeEnd).toMinutes()
                
                FreeTimeBlock(
                    startTime = freeStart.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = freeEnd.format(DateTimeFormatter.ofPattern("HH:mm")),
                    durationMinutes = freeMinutes.toInt()
                )
            }
        }
    }
}

@Composable
fun TimelineItem(
    block: TimelineBlock,
    isLast: Boolean,
    onTaskClick: () -> Unit,
    onStatusToggle: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = block.startTime.format(timeFormatter)
    val endTime = block.startTime.plus(block.duration).format(timeFormatter)
    val taskColor = try {
        Color(android.graphics.Color.parseColor(block.color))
    } catch (e: Exception) {
        TimelineColors.Primary
    }
    
    val isDone = block.status == StatusType.done
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick() }
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Time Column
        Column(
            modifier = Modifier.width(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = startTime,
                fontSize = 14.sp,
                color = TimelineColors.TextSecondary
            )
            Text(
                text = "↓",
                fontSize = 12.sp,
                color = TimelineColors.TextMuted
            )
            Text(
                text = endTime,
                fontSize = 14.sp,
                color = TimelineColors.TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Vertical Line + Dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            // Colored Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(taskColor)
            )
            
            // Vertical Line (if not last)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(60.dp)
                        .background(TimelineColors.TimelineGray)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Task Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TimelineColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(taskColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = taskColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Title & Time
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = block.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDone) TimelineColors.TextMuted else TimelineColors.TextPrimary,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        // Priority Badge
                        if (block.priority != PriorityType.medium) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (block.priority) {
                                            PriorityType.high -> TimelineColors.PriorityHigh
                                            PriorityType.low -> TimelineColors.PriorityLow
                                            else -> Color.Transparent
                                        }
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val hours = block.duration.toHours()
                    val minutes = block.duration.toMinutes() % 60
                    val durationText = when {
                        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                        hours > 0 -> "${hours}h"
                        else -> "${minutes}m"
                    }
                    
                    Text(
                        text = "$startTime - $endTime ($durationText)",
                        fontSize = 13.sp,
                        color = TimelineColors.TextMuted
                    )
                }
                
                // Checkbox
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isDone) taskColor else Color.Transparent)
                        .then(
                            if (!isDone) {
                                Modifier
                                    .background(Color.Transparent)
                                    .clip(CircleShape)
                            } else Modifier
                        )
                        .clickable { onStatusToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FreeTimeBlock(
    startTime: String,
    endTime: String,
    durationMinutes: Int
) {
    val durationText = when {
        durationMinutes >= 60 -> {
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            if (mins > 0) "${hours}g ${mins}p" else "${hours}g"
        }
        else -> "${durationMinutes} phút"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Time Column
        Column(
            modifier = Modifier.width(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = startTime,
                fontSize = 14.sp,
                color = TimelineColors.TextMuted
            )
            Text(
                text = "↓",
                fontSize = 12.sp,
                color = TimelineColors.TextMuted.copy(alpha = 0.5f)
            )
            Text(
                text = endTime,
                fontSize = 14.sp,
                color = TimelineColors.TextMuted
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Vertical Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(50.dp)
                    .background(TimelineColors.TimelineGray.copy(alpha = 0.5f))
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Free Time Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⏱️",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$durationText thời gian rảnh!",
                    fontSize = 14.sp,
                    color = TimelineColors.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Khung giờ đó đã qua—hãy nắm bắt cái tiếp theo.",
                fontSize = 13.sp,
                color = TimelineColors.TextMuted
            )
        }
    }
}

@Composable
fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.inbox),
            contentDescription = null,
            tint = TimelineColors.TextMuted.copy(alpha = 0.3f),
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Không có task nào",
            fontSize = 18.sp,
            color = TimelineColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Nhấn + để thêm task mới",
            fontSize = 14.sp,
            color = TimelineColors.TextMuted
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = TimelineColors.Primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thêm Task")
        }
    }
}

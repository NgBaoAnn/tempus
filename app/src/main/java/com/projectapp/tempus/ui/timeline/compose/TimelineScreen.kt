package com.projectapp.tempus.ui.timeline.compose

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R
import com.projectapp.tempus.data.quote.dto.QuoteDto
import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.domain.model.TimelineBlock
import com.projectapp.tempus.ui.timeline.SortOption
import com.projectapp.tempus.domain.model.SubtaskInfo
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


import com.projectapp.tempus.ui.theme.TempusDesignSystem
import com.projectapp.tempus.ui.components.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple


fun getLabelIconResId(label: String): Int = when (label.lowercase()) {
    "wakeup" -> R.drawable.wakeup
    "eat" -> R.drawable.eat
    "cook" -> R.drawable.cook
    "exercise" -> R.drawable.exercise
    "rest" -> R.drawable.rest
    "water" -> R.drawable.water
    "book" -> R.drawable.book
    "sleep" -> R.drawable.sleep
    "clean" -> R.drawable.clean
    "garden" -> R.drawable.ic_garden
    else -> R.drawable.book  
}

@Composable
fun TimelineScreen(
    blocks: List<TimelineBlock>,
    selectedDate: LocalDate,
    monthYear: String,
    weeks: List<List<LocalDate>>,
    
    searchQuery: String = "",
    sortBy: SortOption = SortOption.START_TIME,
    filterLabels: Set<ScheduleLabel> = emptySet(),
    filterPriorities: Set<PriorityType> = emptySet(),
    filterStatus: StatusType? = null,
    isFilterActive: Boolean = false,
    dailyQuote: QuoteDto? = null,
    isLoading: Boolean = false,
    onDateSelected: (LocalDate) -> Unit,
    onMonthPickerClick: () -> Unit,
    onAddClick: () -> Unit,
    onVoiceClick: () -> Unit = {},
    onTaskClick: (TimelineBlock) -> Unit,
    onStatusToggle: (TimelineBlock) -> Unit,
    
    onSearchQueryChanged: (String) -> Unit = {},
    onSortChanged: (SortOption) -> Unit = {},
    onFilterLabelToggle: (ScheduleLabel) -> Unit = {},
    onFilterPriorityToggle: (PriorityType) -> Unit = {},
    onFilterStatusChanged: (StatusType?) -> Unit = {},
    onClearAllFilters: () -> Unit = {},
    onSubtaskToggle: (subtaskId: String, isDone: Boolean) -> Unit = { _, _ -> },
    onGardenClick: () -> Unit = {},
    currentStreak: Int = 0,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scalePressEffect()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(TempusDesignSystem.Gradients.Success)
                        )
                        .clickable(
                            onClick = { onVoiceClick() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = stringResource(R.string.timeline_voice_command),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scalePressEffect()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(TempusDesignSystem.Gradients.Primary)
                        )
                        .clickable(
                            onClick = { onAddClick() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = stringResource(R.string.timeline_add_task),
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            item(key = "topbar") {
                TimelineTopBar(
                    monthYear = monthYear,
                    onMonthPickerClick = onMonthPickerClick,
                    onGardenClick = onGardenClick,
                    currentStreak = currentStreak
                )
            }
            
            
            item(key = "quote") {
                DailyQuoteCard(quote = dailyQuote)
            }
            
            
            stickyHeader(key = "week_calendar") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    SwipeableWeekCalendarStrip(
                        weeks = weeks,
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            
            item(key = "filter_bar") {
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
            }
            
            
            when {
                isLoading -> {
                    
                    items(5, key = { "skeleton_$it" }) {
                        SkeletonTaskItemInline()
                    }
                }
                blocks.isEmpty() -> {
                    item(key = "empty") {
                        EmptyState(onAddClick = onAddClick)
                    }
                }
                else -> {
                    
                    itemsIndexed(
                        items = blocks,
                        key = { _, block -> block.taskId }
                    ) { index, block ->
                        val nextBlock = blocks.getOrNull(index + 1)
                        val blockEndTime = block.startTime.plus(block.duration)
                        
                        
                        val hasOverlap = nextBlock != null && 
                            !blockEndTime.isBefore(nextBlock.startTime) &&
                            !blockEndTime.isEqual(nextBlock.startTime)
                        
                        
                        val showFreeTime = nextBlock != null && 
                            blockEndTime.isBefore(nextBlock.startTime)
                        
                        TimelineItem(
                            block = block,
                            isLast = index == blocks.lastIndex,
                            onTaskClick = { onTaskClick(block) },
                            onStatusToggle = { onStatusToggle(block) },
                            onSubtaskToggle = onSubtaskToggle
                        )
                        
                        
                        if (hasOverlap && nextBlock != null) {
                            OverlapWarning()
                        }
                        
                        
                        if (showFreeTime && nextBlock != null) {
                            val freeStart = blockEndTime
                            val freeEnd = nextBlock.startTime
                            val freeMinutes = java.time.Duration.between(freeStart, freeEnd).toMinutes()
                            
                            FreeTimeBlock(
                                startTime = freeStart.format(DateTimeFormatter.ofPattern("HH:mm")),
                                endTime = freeEnd.format(DateTimeFormatter.ofPattern("HH:mm")),
                                durationMinutes = freeMinutes.toInt()
                            )
                        }
                    }
                    
                    
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun SkeletonTaskItemInline() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.surfaceContainerHigh),
        start = Offset(shimmerTranslateAnim - 500f, 0f),
        end = Offset(shimmerTranslateAnim, 0f)
    )
    
    SkeletonTaskItem(shimmerBrush)
}


@Composable
private fun TimelineLoadingSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        start = Offset(shimmerTranslateAnim - 500f, 0f),
        end = Offset(shimmerTranslateAnim, 0f)
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            SkeletonTaskItem(shimmerBrush)
        }
    }
}

@Composable
private fun SkeletonTaskItem(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        
        
        Column(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SwipeableWeekCalendarStrip(
    weeks: List<List<LocalDate>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    if (weeks.isEmpty()) return
    
    
    val currentWeekIndex = weeks.indexOfFirst { week ->
        week.any { it == selectedDate }
    }.coerceAtLeast(0)
    
    val pagerState = rememberPagerState(
        initialPage = currentWeekIndex,
        pageCount = { weeks.size }
    )
    
    
    LaunchedEffect(selectedDate) {
        val newIndex = weeks.indexOfFirst { week -> week.any { it == selectedDate } }
        if (newIndex >= 0 && newIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(newIndex)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) 
            .padding(bottom = 8.dp)
    ) {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                stringResource(R.string.day_mon),
                stringResource(R.string.day_tue),
                stringResource(R.string.day_wed),
                stringResource(R.string.day_thu),
                stringResource(R.string.day_fri),
                stringResource(R.string.day_sat),
                stringResource(R.string.day_sun)
            ).forEach { day ->
                Text(
                    text = day,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            val week = weeks.getOrNull(pageIndex) ?: return@HorizontalPager
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
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
                                    isSelected -> MaterialTheme.colorScheme.primary 
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
                                isToday -> MaterialTheme.colorScheme.primary 
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun TimelineTopBar(
    monthYear: String,
    onMonthPickerClick: () -> Unit,
    onGardenClick: () -> Unit = {},
    currentStreak: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) 
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) 
                .clickable { onMonthPickerClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthYear,
                color = MaterialTheme.colorScheme.primary, 
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_unfold_more),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            
            if (currentStreak > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF5722).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = currentStreak.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                    }
                }
            }
            
            
            IconButton(onClick = onGardenClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_garden),
                    contentDescription = stringResource(R.string.timeline_garden),
                    tint = Color.Unspecified, 
                    modifier = Modifier.size(34.dp)
                )
            }
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
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) 
            .padding(bottom = 8.dp)
    ) {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                stringResource(R.string.day_mon),
                stringResource(R.string.day_tue),
                stringResource(R.string.day_wed),
                stringResource(R.string.day_thu),
                stringResource(R.string.day_fri),
                stringResource(R.string.day_sat),
                stringResource(R.string.day_sun)
            ).forEach { day ->
                Text(
                    text = day,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        
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
                                isSelected -> TempusDesignSystem.Primary 
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
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = when {
                            isSelected -> Color.White
                            isToday -> MaterialTheme.colorScheme.primary 
                            else -> MaterialTheme.colorScheme.onSurface
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
    onStatusToggle: (TimelineBlock) -> Unit,
    onSubtaskToggle: (subtaskId: String, isDone: Boolean) -> Unit = { _, _ -> }
) {
    
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(blocks) {
        visible = true
    }

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
            
            Box(
                modifier = Modifier.fadeInSlideUp(
                    index = index,
                    visible = visible,
                    delayPerItem = 40 
                )
            ) {
                TimelineItem(
                    block = block,
                    isLast = index == blocks.lastIndex,
                    onTaskClick = { onTaskClick(block) },
                    onStatusToggle = { onStatusToggle(block) },
                    onSubtaskToggle = onSubtaskToggle
                )
            }
            
            if (showFreeTime && nextBlock != null) {
                val freeStart = block.startTime.plus(block.duration)
                val freeEnd = nextBlock.startTime
                val freeMinutes = java.time.Duration.between(freeStart, freeEnd).toMinutes()
                
                Box(
                    modifier = Modifier.fadeInSlideUp(
                        index = index, 
                        visible = visible,
                        delayPerItem = 40,
                        initialOffsetY = 10f
                    )
                ) {
                    FreeTimeBlock(
                        startTime = freeStart.format(DateTimeFormatter.ofPattern("HH:mm")),
                        endTime = freeEnd.format(DateTimeFormatter.ofPattern("HH:mm")),
                        durationMinutes = freeMinutes.toInt()
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    block: TimelineBlock,
    isLast: Boolean,
    onTaskClick: () -> Unit,
    onStatusToggle: () -> Unit,
    onSubtaskToggle: (subtaskId: String, isDone: Boolean) -> Unit = { _, _ -> }
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = block.startTime.format(timeFormatter)
    val endTime = block.startTime.plus(block.duration).format(timeFormatter)
    val taskColor = try {
        Color(android.graphics.Color.parseColor(block.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    
    val isDone = block.status == StatusType.done
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDone) { onTaskClick() } 
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        
        Column(
            modifier = Modifier.width(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = startTime,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "↓",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = endTime,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(taskColor)
            )
            
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        
        val isHighPriority = block.priority == PriorityType.high
        
        TempusCard(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
            backgroundColor = MaterialTheme.colorScheme.surface,
            elevation = if (isHighPriority) 4.dp else 2.dp,
            variant = if (isHighPriority) CardVariant.Gradient else CardVariant.Default,
            gradientColors = if (isHighPriority) 
                listOf(TempusDesignSystem.Error.copy(alpha = 0.1f), MaterialTheme.colorScheme.surface)
            else TempusDesignSystem.Gradients.Primary,
            onClick = if (isDone) null else onTaskClick 
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp)) 
                        .background(taskColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = getLabelIconResId(block.label)),
                        contentDescription = null,
                        tint = taskColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = block.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDone) TempusDesignSystem.TextMuted else MaterialTheme.colorScheme.onBackground,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        
                        if (block.priority != PriorityType.medium) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .glowEffect(
                                        color = if (block.priority == PriorityType.high) TempusDesignSystem.Error else TempusDesignSystem.Success,
                                        enabled = block.priority == PriorityType.high
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        when (block.priority) {
                                            PriorityType.high -> TempusDesignSystem.Error
                                            PriorityType.low -> TempusDesignSystem.Success
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
                        text = "$startTime - $endTime • $durationText",
                        fontSize = 13.sp,
                        color = TempusDesignSystem.TextMuted
                    )
                }
                
                
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isDone) taskColor else Color.Transparent)
                        .scalePressEffect() 
                        .then(
                            if (!isDone) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = TempusDesignSystem.TextMuted.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                            } else Modifier
                        )
                        .clickable { onStatusToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isDone,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            
            if (block.subtasks.isNotEmpty()) {
                Column(modifier = Modifier.padding(start = 64.dp, end = 12.dp, bottom = 12.dp)) {
                    block.subtasks.take(3).forEach { subtask ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (subtask.isDone) taskColor.copy(alpha = 0.7f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (subtask.isDone) Color.Transparent else TempusDesignSystem.TextMuted.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { onSubtaskToggle(subtask.id, !subtask.isDone) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (subtask.isDone) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subtask.title,
                                fontSize = 12.sp,
                                color = if (subtask.isDone) TempusDesignSystem.TextMuted else MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (block.subtasks.size > 3) {
                        Text(
                            text = "+${block.subtasks.size - 3} more",
                            fontSize = 11.sp,
                            color = TempusDesignSystem.TextMuted,
                             modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun OverlapWarning() {
    val warningColor = Color(0xFFFF9800) 
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Spacer(modifier = Modifier.width(50.dp))
        
        Spacer(modifier = Modifier.width(8.dp))
        
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .background(warningColor.copy(alpha = 0.5f))
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        
        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = warningColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_warning),
                contentDescription = "Overlap Warning",
                tint = warningColor,
                modifier = Modifier.size(14.dp)
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = stringResource(R.string.timeline_overlap_warning),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = warningColor
            )
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
        
        Column(
            modifier = Modifier.width(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = startTime,
                fontSize = 14.sp,
                color = TempusDesignSystem.TextMuted
            )
            Text(
                text = "↓",
                fontSize = 12.sp,
                color = TempusDesignSystem.TextMuted.copy(alpha = 0.5f)
            )
            Text(
                text = endTime,
                fontSize = 14.sp,
                color = TempusDesignSystem.TextMuted
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(50.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        
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
                val suffix = stringResource(R.string.timeline_free_time_title)
                Text(
                    text = "$durationText $suffix",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.timeline_free_time_desc),
                fontSize = 13.sp,
                color = TempusDesignSystem.TextMuted
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
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = TempusDesignSystem.TextMuted.copy(alpha = 0.3f),
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.timeline_empty_title),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = stringResource(R.string.timeline_empty_desc),
            fontSize = 14.sp,
            color = TempusDesignSystem.TextMuted
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.timeline_add_task_label))
        }
    }
}

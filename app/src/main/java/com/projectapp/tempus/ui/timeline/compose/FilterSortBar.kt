package com.projectapp.tempus.ui.timeline.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.ui.theme.TempusDesignSystem
import com.projectapp.tempus.ui.timeline.SortOption

// Color definitions for filter UI - aligned with TempusDesignSystem
// FilterColors object removed - using MaterialTheme directly

/**
 * Filter and Sort Bar for Timeline
 */
@Composable
fun FilterSortBar(
    searchQuery: String,
    sortBy: SortOption,
    filterLabels: Set<ScheduleLabel>,
    filterPriorities: Set<PriorityType>,
    filterStatus: StatusType?,
    isFilterActive: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onSortChanged: (SortOption) -> Unit,
    onFilterLabelToggle: (ScheduleLabel) -> Unit,
    onFilterPriorityToggle: (PriorityType) -> Unit,
    onFilterStatusChanged: (StatusType?) -> Unit,
    onClearAllFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Search Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search TextField
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Tìm kiếm...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onSearchQueryChanged("") }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Sort Button
            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Sort",
                        tint = if (sortBy != SortOption.START_TIME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (option) {
                                        SortOption.START_TIME -> "Theo giờ bắt đầu"
                                        SortOption.PRIORITY -> "Theo mức ưu tiên"
                                        SortOption.CREATED_AT -> "Theo ngày tạo"
                                    },
                                    color = if (sortBy == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSortChanged(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Filter Toggle Button
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isFilterActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Filter",
                    tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Expandable Filter Section
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                // Priority Chips
                Text(
                    text = "Mức ưu tiên",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    PriorityType.entries.forEach { priority ->
                        FilterChip(
                            selected = priority in filterPriorities,
                            onClick = { onFilterPriorityToggle(priority) },
                            label = {
                                Text(
                                    text = when (priority) {
                                        PriorityType.high -> "🔴 Cao"
                                        PriorityType.medium -> "🟡 Trung bình"
                                        PriorityType.low -> "🟢 Thấp"
                                    },
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (priority) {
                                    PriorityType.high -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    PriorityType.medium -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    PriorityType.low -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                }
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Label Chips
                Text(
                    text = "Danh mục",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // Show common labels
                    val commonLabels = listOf(
                        ScheduleLabel.wakeup to "⏰ Thức dậy",
                        ScheduleLabel.eat to "🍽️ Ăn uống",
                        ScheduleLabel.exercise to "💪 Tập luyện",
                        ScheduleLabel.book to "📚 Học tập",
                        ScheduleLabel.sleep to "😴 Ngủ",
                        ScheduleLabel.rest to "☕ Nghỉ ngơi"
                    )
                    
                    commonLabels.forEach { (label, displayName) ->
                        FilterChip(
                            selected = label in filterLabels,
                            onClick = { onFilterLabelToggle(label) },
                            label = { Text(displayName, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Status Chips
                Text(
                    text = "Trạng thái",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterStatus == StatusType.planned,
                        onClick = { 
                            onFilterStatusChanged(
                                if (filterStatus == StatusType.planned) null else StatusType.planned
                            )
                        },
                        label = { Text("📋 Chưa xong", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                    FilterChip(
                        selected = filterStatus == StatusType.done,
                        onClick = { 
                            onFilterStatusChanged(
                                if (filterStatus == StatusType.done) null else StatusType.done
                            )
                        },
                        label = { Text("✅ Hoàn thành", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        )
                    )
                }
                
                // Clear All Button
                if (isFilterActive) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = onClearAllFilters,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xóa tất cả bộ lọc", fontSize = 13.sp)
                    }
                }
            }
        }
        
        // Divider
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            thickness = 1.dp
        )
    }
}

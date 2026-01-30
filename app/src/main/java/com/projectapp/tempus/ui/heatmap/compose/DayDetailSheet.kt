package com.projectapp.tempus.ui.heatmap.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.domain.model.TimelineBlock
import com.projectapp.tempus.domain.usecase.DayHeatmapData
import com.projectapp.tempus.domain.usecase.HeatLevel
import com.projectapp.tempus.ui.theme.TempusDesignSystem
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun DayDetailSheet(
    dayData: DayHeatmapData?,
    tasks: List<TimelineBlock>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onViewTimeline: () -> Unit,
    onToggleTaskStatus: (TimelineBlock) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        
        if (dayData != null) {
            DayHeader(dayData = dayData)
            Spacer(modifier = Modifier.height(20.dp))
            
            
            CompletionSummary(dayData = dayData)
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        
        Text(
            text = "Tasks",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = TempusDesignSystem.Primary
                )
            }
        } else if (tasks.isEmpty()) {
            EmptyTasksPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TaskListItem(
                        task = task,
                        onToggleStatus = { onToggleTaskStatus(task) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            OutlinedButton(
                onClick = onViewTimeline,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TempusDesignSystem.Primary
                )
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Xem Timeline")
            }
            
            
            Button(
                onClick = onAddTask,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TempusDesignSystem.Primary
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Thêm task")
            }
        }
    }
}


@Composable
private fun DayHeader(dayData: DayHeatmapData) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("vi"))
    val dateText = dayData.date.format(formatter)
        .replaceFirstChar { it.uppercase() }
    
    Text(
        text = "📅 $dateText",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
}


@Composable
private fun CompletionSummary(dayData: DayHeatmapData) {
    val percent = (dayData.completionRate * 100).toInt()
    val statusColor = HeatmapColors.getIndicatorColor(dayData.heatLevel)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (dayData.totalTasks > 0) {
                        "✅ ${dayData.completedTasks}/${dayData.totalTasks} tasks hoàn thành"
                    } else {
                        "Không có task nào"
                    },
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (dayData.totalTasks > 0) {
                    Text(
                        text = "$percent%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = statusColor
                    )
                }
            }
            
            if (dayData.totalTasks > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                
                LinearProgressIndicator(
                    progress = { dayData.completionRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}


@Composable
private fun TaskListItem(
    task: TimelineBlock,
    onToggleStatus: () -> Unit
) {
    val isDone = task.status == StatusType.done
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleStatus() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) {
                TempusDesignSystem.SuccessLight.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            IconButton(
                onClick = onToggleStatus,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isDone) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = if (isDone) "Hoàn thành" else "Chưa hoàn thành",
                    tint = if (isDone) TempusDesignSystem.Success else TempusDesignSystem.Slate400,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = task.startTime.format(timeFormatter),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TempusDesignSystem.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = task.label.take(1).uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TempusDesignSystem.Primary
                )
            }
        }
    }
}


@Composable
private fun EmptyTasksPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📭",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Không có task nào",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

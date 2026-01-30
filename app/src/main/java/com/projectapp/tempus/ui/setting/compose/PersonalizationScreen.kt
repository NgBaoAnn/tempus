package com.projectapp.tempus.ui.setting.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.projectapp.tempus.R
import com.projectapp.tempus.data.personalization.CustomTimePeriod
import com.projectapp.tempus.data.personalization.LifestylePreset
import com.projectapp.tempus.data.personalization.PersonalizationTask
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.ui.setting.PersonalizationUiState
import com.projectapp.tempus.ui.setting.SchedulePreviewItem
import com.projectapp.tempus.ui.setting.TimePickerTarget


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    uiState: PersonalizationUiState,
    activeDaysLabel: String,
    onBackClick: () -> Unit,
    onShowTimePicker: (TimePickerTarget) -> Unit,
    onDismissTimePicker: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onShowLifestyleSheet: () -> Unit,
    onDismissLifestyleSheet: () -> Unit,
    onSelectLifestyle: (LifestylePreset) -> Unit,
    onToggleDay: (Int) -> Unit,
    onShowAddCustomPeriod: () -> Unit,
    onDismissAddCustomPeriod: () -> Unit,
    onUpdateNewPeriodName: (String) -> Unit,
    onUpdateNewPeriodDescription: (String) -> Unit,
    onUpdateNewPeriodColor: (String) -> Unit,
    onSaveNewCustomPeriod: () -> Unit,
    onRemoveCustomPeriod: (String) -> Unit,
    onShowResetConfirmation: () -> Unit,
    onDismissResetConfirmation: () -> Unit,
    onConfirmReset: () -> Unit,
    onShowLabelSheet: () -> Unit,
    onDismissLabelSheet: () -> Unit,
    onSelectLabel: (ScheduleLabel) -> Unit,
    getLabelDisplayName: (ScheduleLabel) -> String,
    // Task management callbacks
    onShowAddTaskDialog: () -> Unit,
    onDismissAddTaskDialog: () -> Unit,
    onUpdateNewTaskName: (String) -> Unit,
    onUpdateNewTaskDescription: (String) -> Unit,
    onUpdateNewTaskMinutes: (Int) -> Unit,
    onUpdateNewTaskPriority: (String) -> Unit,
    onSaveNewTask: () -> Unit,
    onRemoveTask: (String) -> Unit,
    onGenerateScheduleWithAI: () -> Unit,
    onConfirmSchedule: () -> Unit,
    onDismissSchedulePreview: () -> Unit,
    onDeletePreviewItem: (String) -> Unit,
    onUpdatePreviewItem: (String, String?, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    
    if (uiState.showAddCustomPeriodScreen) {
        AddCustomPeriodScreen(
            name = uiState.newPeriodName,
            description = uiState.newPeriodDescription,
            startTime = uiState.newPeriodStartTime,
            endTime = uiState.newPeriodEndTime,
            color = uiState.newPeriodColor,
            label = uiState.newPeriodLabel,
            showTimePickerFor = uiState.showTimePickerFor,
            showLabelSheet = uiState.showLabelSheet,
            onNameChange = onUpdateNewPeriodName,
            onDescriptionChange = onUpdateNewPeriodDescription,
            onColorChange = onUpdateNewPeriodColor,
            onShowTimePicker = onShowTimePicker,
            onDismissTimePicker = onDismissTimePicker,
            onTimeSelected = onTimeSelected,
            onShowLabelSheet = onShowLabelSheet,
            onDismissLabelSheet = onDismissLabelSheet,
            onSelectLabel = onSelectLabel,
            getLabelDisplayName = getLabelDisplayName,
            onSave = onSaveNewCustomPeriod,
            onClose = onDismissAddCustomPeriod
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PersonalizationColors.Background)
    ) {
        
        PersonalizationHeader(onBackClick = onBackClick)

        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            
            item {
                SectionTitle(text = stringResource(R.string.pers_section_lifestyle))
            }

            item {
                LifestyleCard(
                    lifestyle = uiState.lifestyle,
                    onClick = onShowLifestyleSheet
                )
            }

            
            item {
                SectionTitle(text = stringResource(R.string.pers_section_days))
            }

            item {
                DaysCard(
                    activeDays = uiState.activeDays,
                    activeDaysLabel = activeDaysLabel,
                    onToggleDay = onToggleDay
                )
            }

            
            item {
                SectionTitle(text = stringResource(R.string.pers_section_sleep))
            }

            item {
                TimeSettingsCard(
                    items = listOf(
                        TimeSettingData(stringResource(R.string.pers_wake_up), uiState.wakeUpTime, PersonalizationColors.Orange, TimePickerTarget.WAKE_UP),
                        TimeSettingData(stringResource(R.string.pers_sleep), uiState.sleepTime, PersonalizationColors.Purple, TimePickerTarget.SLEEP)
                    ),
                    onShowTimePicker = onShowTimePicker
                )
            }

            // Section: CÔNG VIỆC CÁ NHÂN (replaced GIỜ LÀM VIỆC)
            item {
                SectionTitle(text = "CÔNG VIỆC CÁ NHÂN")
            }

            // Task list
            if (uiState.pendingTasks.isNotEmpty()) {
                item {
                    TasksListCard(
                        tasks = uiState.pendingTasks,
                        onRemove = onRemoveTask
                    )
                }
            }

            // Add task button
            item {
                AddCustomPeriodCard(
                    onClick = onShowAddTaskDialog,
                    text = "+ Thêm công việc"
                )
            }

            // Generate schedule with AI button
            if (uiState.pendingTasks.isNotEmpty()) {
                item {
                    GenerateScheduleButton(
                        isLoading = uiState.isGeneratingSchedule,
                        onClick = onGenerateScheduleWithAI
                    )
                }
            }

            // Bottom spacer
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    
    if (uiState.showTimePickerFor != null && 
        uiState.showTimePickerFor != TimePickerTarget.NEW_PERIOD_START &&
        uiState.showTimePickerFor != TimePickerTarget.NEW_PERIOD_END) {
        TimePickerDialog(
            initialTime = when (uiState.showTimePickerFor) {
                TimePickerTarget.WAKE_UP -> uiState.wakeUpTime
                TimePickerTarget.SLEEP -> uiState.sleepTime
                TimePickerTarget.WORK_START -> uiState.workStartTime
                TimePickerTarget.WORK_END -> uiState.workEndTime
                else -> "08:00"
            },
            title = when (uiState.showTimePickerFor) {
                TimePickerTarget.WAKE_UP -> stringResource(R.string.picker_title_wake)
                TimePickerTarget.SLEEP -> stringResource(R.string.picker_title_sleep)
                TimePickerTarget.WORK_START -> stringResource(R.string.picker_title_work_start)
                TimePickerTarget.WORK_END -> stringResource(R.string.picker_title_work_end)
                else -> stringResource(R.string.picker_title_default)
            },
            onDismiss = onDismissTimePicker,
            onConfirm = onTimeSelected
        )
    }

    
    if (uiState.showLifestyleSheet) {
        LifestylePickerSheet(
            currentLifestyle = uiState.lifestyle,
            onLifestyleSelected = onSelectLifestyle,
            onDismiss = onDismissLifestyleSheet
        )
    }

    
    if (uiState.showResetConfirmation) {
        ResetConfirmationDialog(
            wakeUpTime = uiState.wakeUpTime,
            sleepTime = uiState.sleepTime,
            workStartTime = uiState.workStartTime,
            workEndTime = uiState.workEndTime,
            customPeriods = uiState.customTimePeriods,
            activeDaysLabel = activeDaysLabel,
            onDismiss = onDismissResetConfirmation,
            onConfirm = onConfirmReset
        )
    }
    
    // Add Task Dialog
    if (uiState.showAddTaskDialog) {
        AddTaskDialog(
            taskName = uiState.newTaskName,
            taskDescription = uiState.newTaskDescription,
            estimatedMinutes = uiState.newTaskEstimatedMinutes,
            priority = uiState.newTaskPriority,
            onNameChange = onUpdateNewTaskName,
            onDescriptionChange = onUpdateNewTaskDescription,
            onMinutesChange = onUpdateNewTaskMinutes,
            onPriorityChange = onUpdateNewTaskPriority,
            onDismiss = onDismissAddTaskDialog,
            onSave = onSaveNewTask
        )
    }
    
    // Schedule Preview Dialog
    if (uiState.showSchedulePreview) {
        SchedulePreviewDialog(
            previewItems = uiState.generatedSchedulePreview,
            isLoading = uiState.isGeneratingSchedule,
            onConfirm = onConfirmSchedule,
            onDismiss = onDismissSchedulePreview,
            onDeleteItem = onDeletePreviewItem,
            onUpdateItem = onUpdatePreviewItem
        )
    }
}


private data class TimeSettingData(
    val label: String,
    val time: String,
    val accentColor: Color,
    val target: TimePickerTarget
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomPeriodScreen(
    name: String,
    description: String,
    startTime: String,
    endTime: String,
    color: String,
    label: ScheduleLabel,
    showTimePickerFor: TimePickerTarget?,
    showLabelSheet: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onShowTimePicker: (TimePickerTarget) -> Unit,
    onDismissTimePicker: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onShowLabelSheet: () -> Unit,
    onDismissLabelSheet: () -> Unit,
    onSelectLabel: (ScheduleLabel) -> Unit,
    getLabelDisplayName: (ScheduleLabel) -> String,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PersonalizationColors.Background)
    ) {
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClose) {
                    Text(stringResource(R.string.cancel), color = PersonalizationColors.Blue, fontSize = 17.sp)
                }
                
                Text(
                    text = stringResource(R.string.custom_period_title),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PersonalizationColors.TextPrimary
                )
                
                TextButton(
                    onClick = onSave,
                    enabled = name.isNotBlank()
                ) {
                    Text(
                        stringResource(R.string.save),
                        color = if (name.isNotBlank()) PersonalizationColors.Blue else PersonalizationColors.TextHint,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val colorValue = try {
                            Color(android.graphics.Color.parseColor(color))
                        } catch (e: Exception) { PersonalizationColors.Blue }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colorValue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colorValue)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        BasicTextField(
                            value = name,
                            onValueChange = onNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = PersonalizationColors.TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(PersonalizationColors.Blue),
                            decorationBox = { innerTextField ->
                                if (name.isEmpty()) {
                                    Text(
                                        stringResource(R.string.custom_period_name_hint),
                                        color = PersonalizationColors.TextHint,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    
                    BasicTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp),
                        textStyle = TextStyle(
                            color = PersonalizationColors.TextSecondary,
                            fontSize = 15.sp
                        ),
                        cursorBrush = SolidColor(PersonalizationColors.Blue),
                        decorationBox = { innerTextField ->
                            if (description.isEmpty()) {
                                Text(
                                    stringResource(R.string.custom_period_desc_hint),
                                    color = PersonalizationColors.TextHint,
                                    fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = PersonalizationColors.Divider
                    )

                    
                    ColorPaletteRow(
                        selectedColor = color,
                        onColorSelected = onColorChange
                    )
                }
            }

            
            Card(
                onClick = onShowLabelSheet,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PersonalizationColors.Purple)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = stringResource(R.string.custom_period_label),
                        color = PersonalizationColors.TextPrimary,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = getLabelDisplayName(label),
                        color = PersonalizationColors.TextSecondary,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "›",
                        color = PersonalizationColors.TextHint,
                        fontSize = 20.sp
                    )
                }
            }

            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    
                    Surface(
                        onClick = { onShowTimePicker(TimePickerTarget.NEW_PERIOD_START) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.custom_period_start),
                                fontSize = 16.sp,
                                color = PersonalizationColors.TextPrimary
                            )
                            Text(
                                text = startTime,
                                fontSize = 16.sp,
                                color = PersonalizationColors.TextSecondary
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        thickness = 1.dp,
                        color = PersonalizationColors.Divider
                    )

                    
                    Surface(
                        onClick = { onShowTimePicker(TimePickerTarget.NEW_PERIOD_END) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.custom_period_end),
                                fontSize = 16.sp,
                                color = PersonalizationColors.TextPrimary
                            )
                            Text(
                                text = endTime,
                                fontSize = 16.sp,
                                color = PersonalizationColors.TextSecondary
                            )
                        }
                    }
                }
            }

            
            val duration = calculateDurationDisplay(startTime, endTime)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.custom_period_duration),
                        fontSize = 16.sp,
                        color = PersonalizationColors.TextPrimary
                    )
                    Text(
                        text = duration,
                        fontSize = 16.sp,
                        color = PersonalizationColors.Green,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    
    if (showTimePickerFor == TimePickerTarget.NEW_PERIOD_START || 
        showTimePickerFor == TimePickerTarget.NEW_PERIOD_END) {
        TimePickerDialog(
            initialTime = if (showTimePickerFor == TimePickerTarget.NEW_PERIOD_START) startTime else endTime,
            title = if (showTimePickerFor == TimePickerTarget.NEW_PERIOD_START) 
                stringResource(R.string.picker_title_start) else stringResource(R.string.picker_title_end),
            onDismiss = onDismissTimePicker,
            onConfirm = onTimeSelected
        )
    }

    
    if (showLabelSheet) {
        LabelPickerSheet(
            currentLabel = label,
            onLabelSelected = onSelectLabel,
            onDismiss = onDismissLabelSheet,
            getLabelDisplayName = getLabelDisplayName
        )
    }
}

@Composable
private fun calculateDurationDisplay(startTime: String, endTime: String): String {
    val hourStr = stringResource(R.string.custom_period_hour)
    val minuteStr = stringResource(R.string.custom_period_minute)
    return try {
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")
        
        val startMinutes = (startParts[0].toInt() * 60) + startParts[1].toInt()
        var endMinutes = (endParts[0].toInt() * 60) + endParts[1].toInt()
        
        if (endMinutes < startMinutes) {
            endMinutes += 24 * 60
        }
        
        val durationMinutes = endMinutes - startMinutes
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        
        when {
            hours == 0 -> "$minutes $minuteStr"
            minutes == 0 -> "$hours $hourStr"
            else -> "$hours $hourStr $minutes $minuteStr"
        }
    } catch (e: Exception) {
        "1 $hourStr"
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabelPickerSheet(
    currentLabel: ScheduleLabel,
    onLabelSelected: (ScheduleLabel) -> Unit,
    onDismiss: () -> Unit,
    getLabelDisplayName: (ScheduleLabel) -> String
) {
    val labels = listOf(
        ScheduleLabel.wakeup,
        ScheduleLabel.eat,
        ScheduleLabel.exercise,
        ScheduleLabel.rest,
        ScheduleLabel.water,
        ScheduleLabel.book,
        ScheduleLabel.sleep,
        ScheduleLabel.clean,
        ScheduleLabel.cook,
        ScheduleLabel.garden
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = PersonalizationColors.Background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.custom_period_sheet_label),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PersonalizationColors.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            labels.forEach { labelItem ->
                val isSelected = labelItem == currentLabel
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) PersonalizationColors.Blue.copy(alpha = 0.1f) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "labelBgColor"
                )

                Surface(
                    onClick = { onLabelSelected(labelItem) },
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getLabelDisplayName(labelItem),
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) PersonalizationColors.Blue else PersonalizationColors.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = PersonalizationColors.Blue,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ======================== ADD TASK DIALOG ========================

@Composable
private fun AddTaskDialog(
    taskName: String,
    taskDescription: String,
    estimatedMinutes: Int,
    priority: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onPriorityChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PersonalizationColors.SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Thêm công việc",
                    color = PersonalizationColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Task name input
                OutlinedTextField(
                    value = taskName,
                    onValueChange = onNameChange,
                    label = { Text("Tên công việc *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PersonalizationColors.AccentPrimary,
                        unfocusedBorderColor = PersonalizationColors.Divider,
                        focusedLabelColor = PersonalizationColors.AccentPrimary,
                        unfocusedLabelColor = PersonalizationColors.TextSecondary,
                        focusedTextColor = PersonalizationColors.TextPrimary,
                        unfocusedTextColor = PersonalizationColors.TextPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Description input (optional)
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Mô tả (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PersonalizationColors.AccentPrimary,
                        unfocusedBorderColor = PersonalizationColors.Divider,
                        focusedLabelColor = PersonalizationColors.AccentPrimary,
                        unfocusedLabelColor = PersonalizationColors.TextSecondary,
                        focusedTextColor = PersonalizationColors.TextPrimary,
                        unfocusedTextColor = PersonalizationColors.TextPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time estimation
                Text(
                    text = "Thời gian ước tính",
                    color = PersonalizationColors.TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val timeOptions = listOf(15, 30, 45, 60, 90, 120)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timeOptions) { minutes ->
                        val isSelected = minutes == estimatedMinutes
                        Surface(
                            onClick = { onMinutesChange(minutes) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) PersonalizationColors.AccentPrimary else PersonalizationColors.ChipBackground
                        ) {
                            Text(
                                text = "${minutes}p",
                                color = if (isSelected) Color.White else PersonalizationColors.TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority selection
                Text(
                    text = "Độ ưu tiên",
                    color = PersonalizationColors.TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("high" to "Cao", "medium" to "Trung bình", "low" to "Thấp").forEach { (value, label) ->
                        val isSelected = value == priority
                        val priorityColor = when (value) {
                            "high" -> Color(0xFFE53935)
                            "low" -> Color(0xFF4CAF50)
                            else -> Color(0xFFFFA726)
                        }
                        
                        Surface(
                            onClick = { onPriorityChange(value) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) priorityColor else PersonalizationColors.ChipBackground
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else PersonalizationColors.TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PersonalizationColors.TextSecondary
                        )
                    ) {
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = onSave,
                        enabled = taskName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PersonalizationColors.AccentPrimary
                        )
                    ) {
                        Text("Thêm")
                    }
                }
            }
        }
    }
}

// ======================== SCHEDULE PREVIEW DIALOG ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchedulePreviewDialog(
    previewItems: List<SchedulePreviewItem>,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteItem: (String) -> Unit,
    onUpdateItem: (String, String?, String?, String?) -> Unit
) {
    // State for editing item
    var editingItem by remember { mutableStateOf<SchedulePreviewItem?>(null) }
    var editName by remember { mutableStateOf("") }
    var editStartTime by remember { mutableStateOf("") }
    var editEndTime by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = PersonalizationColors.SurfaceCard,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = "Lịch trình được đề xuất",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PersonalizationColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Nhấn vào lịch để sửa, hoặc vuốt để xóa.",
                    fontSize = 14.sp,
                    color = PersonalizationColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Schedule items list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(previewItems.sortedBy { it.startTime }) { item ->
                        SchedulePreviewItemCard(
                            item = item,
                            onDelete = { onDeleteItem(item.id) },
                            onEdit = {
                                editingItem = item
                                editName = item.name
                                editStartTime = item.startTime
                                editEndTime = item.endTime
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PersonalizationColors.TextSecondary
                        )
                    ) {
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PersonalizationColors.Green
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Xác nhận")
                        }
                    }
                }
            }
        }
    }
    
    // Edit dialog with time pickers
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Chỉnh sửa lịch", color = PersonalizationColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Tên") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PersonalizationColors.AccentPrimary,
                            unfocusedBorderColor = PersonalizationColors.Divider,
                            focusedLabelColor = PersonalizationColors.AccentPrimary,
                            unfocusedLabelColor = PersonalizationColors.TextSecondary,
                            focusedTextColor = PersonalizationColors.TextPrimary,
                            unfocusedTextColor = PersonalizationColors.TextPrimary
                        )
                    )
                    
                    // Time selection buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start time button
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bắt đầu",
                                fontSize = 12.sp,
                                color = PersonalizationColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                onClick = { showStartTimePicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = PersonalizationColors.ChipBackground
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = PersonalizationColors.AccentPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = editStartTime,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PersonalizationColors.TextPrimary
                                    )
                                }
                            }
                        }
                        
                        // End time button
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kết thúc",
                                fontSize = 12.sp,
                                color = PersonalizationColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                onClick = { showEndTimePicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = PersonalizationColors.ChipBackground
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = PersonalizationColors.AccentPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = editEndTime,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PersonalizationColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingItem?.let { item ->
                            onUpdateItem(item.id, editName, editStartTime, editEndTime)
                        }
                        editingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PersonalizationColors.Green)
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("Hủy")
                }
            },
            containerColor = PersonalizationColors.SurfaceCard
        )
    }
    
    // Time picker dialogs for editing
    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = editStartTime,
            title = "Chọn giờ bắt đầu",
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour, minute ->
                editStartTime = String.format("%02d:%02d", hour, minute)
                showStartTimePicker = false
            }
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = editEndTime,
            title = "Chọn giờ kết thúc",
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour, minute ->
                editEndTime = String.format("%02d:%02d", hour, minute)
                showEndTimePicker = false
            }
        )
    }
}

@Composable
private fun SchedulePreviewItemCard(
    item: SchedulePreviewItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val itemColor = try {
        Color(android.graphics.Color.parseColor(item.color))
    } catch (e: Exception) {
        PersonalizationColors.Blue
    }
    
    val priorityColor = when (item.priority) {
        "high" -> Color(0xFFE53935)
        "low" -> Color(0xFF4CAF50)
        else -> Color(0xFFFFA726)
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PersonalizationColors.ChipBackground,
        onClick = onEdit  // Tap to edit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(
                        color = itemColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = PersonalizationColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.startTime} - ${item.endTime}",
                    fontSize = 13.sp,
                    color = PersonalizationColors.TextSecondary
                )
            }
            
            // Priority badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = priorityColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = when (item.priority) {
                        "high" -> "Cao"
                        "low" -> "Thấp"
                        else -> "TB"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = priorityColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ======================== COLOR PALETTE ROW ========================

@Composable
private fun ColorPaletteRow(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#FF3B30", "#FF9500", "#FFCC00", "#34C759", "#00C7BE",
        "#007AFF", "#5856D6", "#AF52DE", "#FF2D55", "#A2845E"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(colors) { colorHex ->
            val color = try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: Exception) { PersonalizationColors.Blue }
            val isSelected = colorHex.equals(selectedColor, ignoreCase = true)

            Surface(
                onClick = { onColorSelected(colorHex) },
                shape = CircleShape,
                color = color,
                modifier = Modifier
                    .size(32.dp)
                    .then(
                        if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                        else Modifier
                    )
            ) {
                if (isSelected) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PersonalizationHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PersonalizationColors.AccentPrimary.copy(alpha = 0.15f),
                        PersonalizationColors.Background
                    )
                )
            )
            .padding(vertical = 20.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBackClick,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PersonalizationColors.AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.back),
                    color = PersonalizationColors.AccentPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
        
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = PersonalizationColors.AccentPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.pers_title),
                color = PersonalizationColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun SectionTitle(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PersonalizationColors.AccentPrimary,
                            PersonalizationColors.AccentSecondary
                        )
                    )
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = PersonalizationColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}


@Composable
private fun LifestyleCard(
    lifestyle: LifestylePreset,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PersonalizationColors.GlowPrimary,
                spotColor = PersonalizationColors.GlowPrimary
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PersonalizationColors.SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            PersonalizationColors.AccentPrimary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Icon(
                imageVector = Icons.Filled.TrackChanges,
                contentDescription = null,
                tint = PersonalizationColors.AccentPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.pers_lifestyle),
                color = PersonalizationColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = lifestyle.displayName,
                color = PersonalizationColors.AccentTertiary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "›",
                color = PersonalizationColors.TextHint,
                fontSize = 24.sp
            )
        }
    }
}


@Composable
private fun DaysCard(
    activeDays: List<Int>,
    activeDaysLabel: String,
    onToggleDay: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PersonalizationColors.GlowSecondary,
                spotColor = PersonalizationColors.GlowSecondary
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PersonalizationColors.SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            PersonalizationColors.AccentSecondary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = PersonalizationColors.AccentSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activeDaysLabel,
                    color = PersonalizationColors.TextSecondary,
                    fontSize = 14.sp
                )
            }
            DayOfWeekSelector(
                activeDays = activeDays,
                onToggleDay = onToggleDay
            )
        }
    }
}

@Composable
private fun DayOfWeekSelector(
    activeDays: List<Int>,
    onToggleDay: (Int) -> Unit
) {
    val days = listOf(
        1 to stringResource(R.string.day_mon_short),
        2 to stringResource(R.string.day_tue_short),
        3 to stringResource(R.string.day_wed_short),
        4 to stringResource(R.string.day_thu_short),
        5 to stringResource(R.string.day_fri_short),
        6 to stringResource(R.string.day_sat_short),
        7 to stringResource(R.string.day_sun_short)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (dayNum, dayLabel) ->
            val isActive = activeDays.contains(dayNum)
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.1f else 1f,
                animationSpec = spring(),
                label = "dayScale"
            )
            val backgroundColor by animateColorAsState(
                targetValue = if (isActive) PersonalizationColors.AccentPrimary else PersonalizationColors.ChipBackground,
                animationSpec = tween(200),
                label = "dayBgColor"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) Color.White else PersonalizationColors.TextSecondary,
                animationSpec = tween(200),
                label = "dayTextColor"
            )

            Surface(
                onClick = { onToggleDay(dayNum) },
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier
                    .size(40.dp)
                    .scale(scale)
                    .then(
                        if (isActive) Modifier.shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            ambientColor = PersonalizationColors.AccentPrimary,
                            spotColor = PersonalizationColors.AccentPrimary
                        ) else Modifier
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = dayLabel,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
private fun TimeSettingsCard(
    items: List<TimeSettingData>,
    onShowTimePicker: (TimePickerTarget) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PersonalizationColors.GlowPrimary,
                spotColor = PersonalizationColors.GlowPrimary
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PersonalizationColors.SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Surface(
                    onClick = { onShowTimePicker(item.target) },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        
                        val icon = when (item.target) {
                            TimePickerTarget.WAKE_UP -> Icons.Outlined.WbSunny
                            TimePickerTarget.SLEEP -> Icons.Outlined.Bedtime
                            TimePickerTarget.WORK_START -> Icons.Outlined.Work
                            TimePickerTarget.WORK_END -> Icons.Outlined.Home
                            else -> Icons.Outlined.Schedule
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = item.accentColor,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = item.label,
                            color = PersonalizationColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            item.accentColor.copy(alpha = 0.3f),
                                            item.accentColor.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = item.accentColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = item.time,
                                color = PersonalizationColors.TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        thickness = 1.dp,
                        color = PersonalizationColors.Divider
                    )
                }
            }
        }
    }
}


@Composable
private fun CustomPeriodsCard(
    periods: List<CustomTimePeriod>,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PersonalizationColors.GlowSecondary,
                spotColor = PersonalizationColors.GlowSecondary
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PersonalizationColors.SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            periods.forEachIndexed { index, period ->
                val periodColor = try {
                    Color(android.graphics.Color.parseColor(period.color))
                } catch (e: Exception) { PersonalizationColors.AccentPrimary }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        periodColor,
                                        periodColor.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(8.dp),
                                ambientColor = periodColor,
                                spotColor = periodColor
                            )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = period.name,
                            color = PersonalizationColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${period.startTime} - ${period.endTime}",
                            color = PersonalizationColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = stringResource(R.string.pers_delete), 
                                    color = Color.Red 
                                ) 
                            },
                            onClick = {
                                onRemove(period.id)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            }
                        )
                    }
                }
                
                if (index < periods.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        thickness = 1.dp,
                        color = PersonalizationColors.Divider
                    )
                }
            }
        }
    }
}


@Composable
private fun AddCustomPeriodCard(
    onClick: () -> Unit,
    text: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PersonalizationColors.AccentPrimary.copy(alpha = 0.5f),
                        PersonalizationColors.AccentSecondary.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PersonalizationColors.SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = PersonalizationColors.AccentTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = PersonalizationColors.AccentTertiary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ======================== TASKS LIST CARD ========================

@Composable
private fun TasksListCard(
    tasks: List<PersonalizationTask>,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PersonalizationColors.SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            tasks.forEachIndexed { index, task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (task.priority) {
                                    "high" -> Color(0xFFE53935)
                                    "low" -> Color(0xFF4CAF50)
                                    else -> Color(0xFFFFA726)
                                }
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.name,
                            color = PersonalizationColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${task.estimatedMinutes} phút • ${
                                when (task.priority) {
                                    "high" -> "Cao"
                                    "low" -> "Thấp"
                                    else -> "Trung bình"
                                }
                            }",
                            color = PersonalizationColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(onClick = { onRemove(task.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Xóa",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
                
                if (index < tasks.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 20.dp),
                        thickness = 1.dp,
                        color = PersonalizationColors.Divider
                    )
                }
            }
        }
    }
}

// ======================== GENERATE SCHEDULE BUTTON ========================

@Composable
private fun GenerateScheduleButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PersonalizationColors.AccentPrimary,
            contentColor = Color.White
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Đang tạo lịch...",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tạo lịch với AI",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ======================== RESET PLAN BUTTON ========================

@Composable
private fun ResetPlanButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PersonalizationColors.AccentPrimary,
                spotColor = PersonalizationColors.AccentPrimary
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = PersonalizationColors.TextPrimary
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            PersonalizationColors.AccentPrimary,
                            PersonalizationColors.AccentSecondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.pers_reset_plan),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: String,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timeParts = initialTime.split(":")
    val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
    val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PersonalizationColors.SurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PersonalizationColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = PersonalizationColors.ChipBackground,
                        selectorColor = PersonalizationColors.Blue,
                        containerColor = PersonalizationColors.SurfaceCard
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = PersonalizationColors.TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }
                    ) {
                        Text(
                            text = stringResource(R.string.pers_confirm),
                            color = PersonalizationColors.Blue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LifestylePickerSheet(
    currentLifestyle: LifestylePreset,
    onLifestyleSelected: (LifestylePreset) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = PersonalizationColors.Background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.pers_select_lifestyle),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PersonalizationColors.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LifestylePreset.entries.forEach { preset ->
                val isSelected = preset == currentLifestyle
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) PersonalizationColors.Blue.copy(alpha = 0.1f) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "lifestyleBgColor"
                )

                Surface(
                    onClick = { onLifestyleSelected(preset) },
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.displayName,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) PersonalizationColors.Blue else PersonalizationColors.TextPrimary
                            )
                            if (preset != LifestylePreset.CUSTOM) {
                                Text(
                                    text = stringResource(R.string.pers_preset_desc, preset.wakeUpTime, preset.sleepTime),
                                    fontSize = 13.sp,
                                    color = PersonalizationColors.TextSecondary
                                )
                            }
                        }

                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = PersonalizationColors.Blue,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
private fun ResetConfirmationDialog(
    wakeUpTime: String,
    sleepTime: String,
    workStartTime: String,
    workEndTime: String,
    customPeriods: List<CustomTimePeriod>,
    activeDaysLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PersonalizationColors.SurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.pers_reset_warning_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PersonalizationColors.Orange
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.pers_reset_warning_content),
                    fontSize = 15.sp,
                    color = PersonalizationColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PersonalizationColors.WarningBackground)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.pers_reset_new_tasks, activeDaysLabel),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PersonalizationColors.TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = stringResource(R.string.pers_reset_wake, wakeUpTime),
                            fontSize = 13.sp,
                            color = PersonalizationColors.TextSecondary
                        )
                        Text(
                            text = stringResource(R.string.pers_reset_sleep, sleepTime),
                            fontSize = 13.sp,
                            color = PersonalizationColors.TextSecondary
                        )
                        Text(
                            text = stringResource(R.string.pers_reset_work, workStartTime, workEndTime),
                            fontSize = 13.sp,
                            color = PersonalizationColors.TextSecondary
                        )
                        
                        customPeriods.forEach { period ->
                            Text(
                                text = stringResource(R.string.pers_reset_custom_period, period.name, period.startTime, period.endTime),
                                fontSize = 13.sp,
                                color = PersonalizationColors.TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.pers_cancel),
                            color = PersonalizationColors.TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PersonalizationColors.Red
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.pers_reset_confirm_action),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}


private object PersonalizationColors {
    
    
    val Background: Color
        @Composable get() = MaterialTheme.colorScheme.background
    val SurfaceCard: Color 
        @Composable get() = MaterialTheme.colorScheme.surface
    val SurfaceDark: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    
    
    val GlassBackground: Color
        @Composable get() = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val GlassBorder: Color
        @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    
    
    val TextPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onBackground
    val TextSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val TextHint: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    
    
    val AccentPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val AccentSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.secondary
    val AccentTertiary: Color
        @Composable get() = MaterialTheme.colorScheme.tertiary
    
    
    val Blue = Color(0xFF3B82F6)
    val Orange = Color(0xFFF59E0B)
    val Purple = Color(0xFF8B5CF6)
    val Green = Color(0xFF10B981)
    val Red = Color(0xFFEF4444)
    val Cyan = Color(0xFF06B6D4)
    val Pink = Color(0xFFEC4899)
    
    
    val Divider: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val ChipBackground: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val ChipBackgroundActive: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val WarningBackground: Color
        @Composable get() = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    
    
    val GlowPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val GlowSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
}

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
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.ui.setting.PersonalizationUiState
import com.projectapp.tempus.ui.setting.TimePickerTarget

/**
 * Personalization Screen - Cá nhân hóa thói quen hằng ngày
 * All UI uses Jetpack Compose without icons as requested
 * Using Surface(onClick=) and Card(onClick=) to avoid clickable modifier issues
 */
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
    modifier: Modifier = Modifier
) {
    // Show Add Custom Period Screen if active
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
        // Header
        PersonalizationHeader(onBackClick = onBackClick)

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Section: LỐI SỐNG
            item {
                SectionTitle(text = stringResource(R.string.pers_section_lifestyle))
            }

            item {
                LifestyleCard(
                    lifestyle = uiState.lifestyle,
                    onClick = onShowLifestyleSheet
                )
            }

            // Section: NGÀY ÁP DỤNG
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

            // Section: NGỦ & THỨC DẬY
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

            // Section: GIỜ LÀM VIỆC
            item {
                SectionTitle(text = stringResource(R.string.pers_section_work))
            }

            item {
                TimeSettingsCard(
                    items = listOf(
                        TimeSettingData(stringResource(R.string.pers_work_start), uiState.workStartTime, PersonalizationColors.Green, TimePickerTarget.WORK_START),
                        TimeSettingData(stringResource(R.string.pers_work_end), uiState.workEndTime, PersonalizationColors.Orange, TimePickerTarget.WORK_END)
                    ),
                    onShowTimePicker = onShowTimePicker
                )
            }

            // Section: THỜI GIAN TÙY CHỈNH
            item {
                SectionTitle(text = stringResource(R.string.pers_section_custom))
            }

            // Custom time periods list
            if (uiState.customTimePeriods.isNotEmpty()) {
                item {
                    CustomPeriodsCard(
                        periods = uiState.customTimePeriods,
                        onRemove = onRemoveCustomPeriod
                    )
                }
            }

            // Add custom period button
            item {
                AddCustomPeriodCard(
                    onClick = onShowAddCustomPeriod,
                    text = stringResource(R.string.pers_add_period)
                )
            }

            // Spacer
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Reset button
            item {
                ResetPlanButton(
                    isLoading = uiState.isLoading,
                    onClick = onShowResetConfirmation
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Time Picker Dialog
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

    // Lifestyle Sheet
    if (uiState.showLifestyleSheet) {
        LifestylePickerSheet(
            currentLifestyle = uiState.lifestyle,
            onLifestyleSelected = onSelectLifestyle,
            onDismiss = onDismissLifestyleSheet
        )
    }

    // Reset Confirmation Dialog
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
}

// ======================== DATA CLASSES ========================

private data class TimeSettingData(
    val label: String,
    val time: String,
    val accentColor: Color,
    val target: TimePickerTarget
)

// ======================== ADD CUSTOM PERIOD SCREEN ========================

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
        // Top Bar
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

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Name + Description + Colors
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Color indicator + Name
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

                    // Description
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

                    // Color Palette
                    ColorPaletteRow(
                        selectedColor = color,
                        onColorSelected = onColorChange
                    )
                }
            }

            // Card 2: Label Selection
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

            // Card 3: Time Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    // Start Time
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

                    // End Time
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

            // Duration preview
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

    // Time Picker Dialog for new period
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

    // Label Picker Sheet
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

// ======================== LABEL PICKER SHEET ========================

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

// ======================== HEADER ========================

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

// ======================== SECTION TITLE ========================

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

// ======================== LIFESTYLE CARD ========================

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
            // Icon indicator
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

// ======================== DAYS CARD ========================

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

// ======================== TIME SETTINGS CARD ========================

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
                        // Icon based on type
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

// ======================== CUSTOM PERIODS CARD ========================

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

// ======================== ADD CUSTOM PERIOD CARD ========================

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

// ======================== TIME PICKER DIALOG ========================

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

// ======================== LIFESTYLE PICKER SHEET ========================

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

// ======================== RESET CONFIRMATION DIALOG ========================

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

// ======================== COLORS ========================

/**
 * Personalization screen colors that adapt to current theme
 * Use PersonalizationColors inside @Composable functions
 */
private object PersonalizationColors {
    // These will be populated by the rememberPersonalizationColors() composable
    // For now, they serve as fallback values matching light theme
    
    // Base Colors - these should be accessed via MaterialTheme.colorScheme
    val Background: Color
        @Composable get() = MaterialTheme.colorScheme.background
    val SurfaceCard: Color 
        @Composable get() = MaterialTheme.colorScheme.surface
    val SurfaceDark: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    
    // Glassmorphism
    val GlassBackground: Color
        @Composable get() = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val GlassBorder: Color
        @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    
    // Text Colors
    val TextPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onBackground
    val TextSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val TextHint: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    
    // Accent Colors - use primary from theme
    val AccentPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val AccentSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.secondary
    val AccentTertiary: Color
        @Composable get() = MaterialTheme.colorScheme.tertiary
    
    // Status Colors (semantic - stay consistent across themes)
    val Blue = Color(0xFF3B82F6)
    val Orange = Color(0xFFF59E0B)
    val Purple = Color(0xFF8B5CF6)
    val Green = Color(0xFF10B981)
    val Red = Color(0xFFEF4444)
    val Cyan = Color(0xFF06B6D4)
    val Pink = Color(0xFFEC4899)
    
    // Utility Colors
    val Divider: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val ChipBackground: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val ChipBackgroundActive: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val WarningBackground: Color
        @Composable get() = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    
    // Glow Effects
    val GlowPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val GlowSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
}

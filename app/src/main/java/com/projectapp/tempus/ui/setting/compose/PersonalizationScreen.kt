package com.projectapp.tempus.ui.setting.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
                SectionTitle(text = "LỐI SỐNG")
            }

            item {
                LifestyleCard(
                    lifestyle = uiState.lifestyle,
                    onClick = onShowLifestyleSheet
                )
            }

            // Section: NGÀY ÁP DỤNG
            item {
                SectionTitle(text = "NGÀY ÁP DỤNG")
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
                SectionTitle(text = "NGỦ & THỨC DẬY")
            }

            item {
                TimeSettingsCard(
                    items = listOf(
                        TimeSettingData("Giờ thức", uiState.wakeUpTime, PersonalizationColors.Orange, TimePickerTarget.WAKE_UP),
                        TimeSettingData("Giờ ngủ", uiState.sleepTime, PersonalizationColors.Purple, TimePickerTarget.SLEEP)
                    ),
                    onShowTimePicker = onShowTimePicker
                )
            }

            // Section: GIỜ LÀM VIỆC
            item {
                SectionTitle(text = "GIỜ LÀM VIỆC")
            }

            item {
                TimeSettingsCard(
                    items = listOf(
                        TimeSettingData("Giờ bắt đầu", uiState.workStartTime, PersonalizationColors.Green, TimePickerTarget.WORK_START),
                        TimeSettingData("Giờ kết thúc", uiState.workEndTime, PersonalizationColors.Orange, TimePickerTarget.WORK_END)
                    ),
                    onShowTimePicker = onShowTimePicker
                )
            }

            // Section: THỜI GIAN TÙY CHỈNH
            item {
                SectionTitle(text = "THỜI GIAN TÙY CHỈNH")
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
                AddCustomPeriodCard(onClick = onShowAddCustomPeriod)
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
                TimePickerTarget.WAKE_UP -> "Chọn giờ thức"
                TimePickerTarget.SLEEP -> "Chọn giờ ngủ"
                TimePickerTarget.WORK_START -> "Chọn giờ bắt đầu làm việc"
                TimePickerTarget.WORK_END -> "Chọn giờ kết thúc làm việc"
                else -> "Chọn thời gian"
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
                    Text("Hủy", color = PersonalizationColors.Blue, fontSize = 17.sp)
                }
                
                Text(
                    text = "Thêm hoạt động",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PersonalizationColors.TextPrimary
                )
                
                TextButton(
                    onClick = onSave,
                    enabled = name.isNotBlank()
                ) {
                    Text(
                        "Lưu",
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
                                        "Tên hoạt động",
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
                                    "Thêm mô tả...",
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
                        text = "Nhãn",
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
                                text = "Thời gian bắt đầu",
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
                                text = "Thời gian kết thúc",
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
                        text = "Thời lượng",
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
                "Chọn giờ bắt đầu" else "Chọn giờ kết thúc",
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

private fun calculateDurationDisplay(startTime: String, endTime: String): String {
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
            hours == 0 -> "$minutes phút"
            minutes == 0 -> "$hours giờ"
            else -> "$hours giờ $minutes phút"
        }
    } catch (e: Exception) {
        "1 giờ"
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
                "Chọn nhãn",
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PersonalizationColors.Background)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onBackClick,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "← Cài đặt",
                color = PersonalizationColors.Blue,
                fontSize = 17.sp
            )
        }

        Text(
            text = "Cá nhân hóa",
            color = PersonalizationColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 60.dp)
        )
    }
}

// ======================== SECTION TITLE ========================

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = PersonalizationColors.TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp)
    )
}

// ======================== LIFESTYLE CARD ========================

@Composable
private fun LifestyleCard(
    lifestyle: LifestylePreset,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                    .background(PersonalizationColors.Blue)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Lối sống",
                color = PersonalizationColors.TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = lifestyle.displayName,
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
}

// ======================== DAYS CARD ========================

@Composable
private fun DaysCard(
    activeDays: List<Int>,
    activeDaysLabel: String,
    onToggleDay: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = activeDaysLabel,
                color = PersonalizationColors.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
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
        1 to "T2",
        2 to "T3",
        3 to "T4",
        4 to "T5",
        5 to "T6",
        6 to "T7",
        7 to "CN"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (dayNum, dayLabel) ->
            val isActive = activeDays.contains(dayNum)
            val backgroundColor by animateColorAsState(
                targetValue = if (isActive) PersonalizationColors.Blue else PersonalizationColors.ChipBackground,
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
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = dayLabel,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(item.accentColor)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = item.label,
                            color = PersonalizationColors.TextPrimary,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PersonalizationColors.ChipBackground)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item.time,
                                color = PersonalizationColors.TextPrimary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            periods.forEachIndexed { index, period ->
                val periodColor = try {
                    Color(android.graphics.Color.parseColor(period.color))
                } catch (e: Exception) { PersonalizationColors.Blue }

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
                            .background(periodColor)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = period.name,
                            color = PersonalizationColors.TextPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${period.startTime} - ${period.endTime}",
                            color = PersonalizationColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    TextButton(
                        onClick = { onRemove(period.id) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Xóa",
                            color = PersonalizationColors.Red,
                            fontSize = 14.sp
                        )
                    }
                }
                
                if (index < periods.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
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
private fun AddCustomPeriodCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "+ Thêm khoảng thời gian",
                color = PersonalizationColors.Blue,
                fontSize = 16.sp,
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
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = PersonalizationColors.Blue
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, PersonalizationColors.Divider)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PersonalizationColors.Blue,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Tạo lại kế hoạch",
                fontSize = 16.sp
            )
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
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        containerColor = Color.White
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
                            text = "Hủy",
                            color = PersonalizationColors.TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }
                    ) {
                        Text(
                            text = "Xác nhận",
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
                "Chọn lối sống",
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
                                    text = "Thức: ${preset.wakeUpTime} • Ngủ: ${preset.sleepTime}",
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
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "⚠️ Xác nhận đặt lại kế hoạch",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PersonalizationColors.Orange
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Hành động này sẽ XÓA TẤT CẢ tác vụ hiện có trên timeline từ hôm nay trở đi và tạo mới các tác vụ cá nhân hóa.",
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
                            text = "Tác vụ mới sẽ được tạo ($activeDaysLabel):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PersonalizationColors.TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• Thức dậy: $wakeUpTime",
                            fontSize = 13.sp,
                            color = PersonalizationColors.TextSecondary
                        )
                        Text(
                            text = "• Đi ngủ: $sleepTime",
                            fontSize = 13.sp,
                            color = PersonalizationColors.TextSecondary
                        )
                        Text(
                            text = "• Làm việc: $workStartTime - $workEndTime",
                            fontSize = 13.sp,
                            color = PersonalizationColors.TextSecondary
                        )
                        
                        customPeriods.forEach { period ->
                            Text(
                                text = "• ${period.name}: ${period.startTime} - ${period.endTime}",
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
                            text = "Hủy",
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
                            text = "Xóa và tạo mới",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ======================== COLORS ========================

private object PersonalizationColors {
    val Background = Color(0xFFF2F2F7)
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF8E8E93)
    val TextHint = Color(0xFFC7C7CC)
    val Blue = Color(0xFF007AFF)
    val Orange = Color(0xFFFF9500)
    val Purple = Color(0xFF5856D6)
    val Green = Color(0xFF34C759)
    val Red = Color(0xFFFF3B30)
    val Divider = Color(0xFFE5E5EA)
    val ChipBackground = Color(0xFFF2F2F7)
    val WarningBackground = Color(0xFFFFF3CD)
}

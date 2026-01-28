package com.projectapp.tempus.ui.timeline.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R
import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.ui.timeline.EditScheduleViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ============================================
// COLORS - Modern iOS Style
// ============================================
private object EditColors {
    val Background = Color(0xFFF2F2F7)
    val Surface = Color(0xFFFFFFFF)
    val Primary = Color(0xFF007AFF)  // iOS Blue
    val PrimaryLight = Color(0xFFE3F2FF)
    val Divider = Color(0xFFE5E5EA)
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF8E8E93)
    val TextHint = Color(0xFFC7C7CC)
    val Delete = Color(0xFFFF3B30)
    val Success = Color(0xFF34C759)
    val Warning = Color(0xFFFF9500)
    val SheetBackground = Color(0xFFF2F2F7)
}

// ============================================
// MAIN SCREEN
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleScreen(
    viewModel: EditScheduleViewModel,
    onClose: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    var titleText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    
    // Subtasks state
    var subtasks by remember { mutableStateOf(listOf<String>()) }
    var newSubtaskText by remember { mutableStateOf("") }
    
    var showIconSheet by remember { mutableStateOf(false) }
    var showDurationSheet by remember { mutableStateOf(false) }
    var showRepeatSheet by remember { mutableStateOf(false) }
    var showPrioritySheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(state.title) {
        if (titleText.isEmpty() && state.title.isNotEmpty()) {
            titleText = state.title
        }
    }
    
    // Sync description from state (for edit mode)
    LaunchedEffect(state.description) {
        if (descriptionText.isEmpty() && state.description.isNotEmpty()) {
            descriptionText = state.description
        }
    }
    
    // Sync subtasks from state (for edit mode)
    LaunchedEffect(state.subtasks) {
        subtasks = state.subtasks
    }
    
    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collect { onSaveSuccess() }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EditColors.Background)
    ) {
        // ============ TOP BAR ============
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = EditColors.Surface,
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
                    Text("Hủy", color = EditColors.Primary, fontSize = 17.sp)
                }
                
                Text(
                    text = if (state.isEditMode) "Sửa tác vụ" else "Thêm tác vụ",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EditColors.TextPrimary
                )
                
                TextButton(
                    onClick = {
                        if (titleText.isNotBlank()) {
                            viewModel.saveTask(titleText, descriptionText, subtasks)
                        }
                    },
                    enabled = titleText.isNotBlank()
                ) {
                    Text(
                        "Lưu",
                        color = if (titleText.isNotBlank()) EditColors.Primary else EditColors.TextHint,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        // ============ SCROLLABLE CONTENT ============
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ============ CARD 1: Title + Description + Colors ============
            ModernCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Icon + Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconColor = try {
                            Color(android.graphics.Color.parseColor(state.color))
                        } catch (e: Exception) { EditColors.Primary }
                        
                        // Clickable Icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iconColor.copy(alpha = 0.15f))
                                .clickable { showIconSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = getLabelIconResId(state.iconLabel.name)),
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        BasicTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = EditColors.TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(EditColors.Primary),
                            decorationBox = { innerTextField ->
                                if (titleText.isEmpty()) {
                                    Text("Tên tác vụ", color = EditColors.TextHint, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                }
                                innerTextField()
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Description
                    BasicTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp),
                        textStyle = TextStyle(color = EditColors.TextSecondary, fontSize = 15.sp),
                        cursorBrush = SolidColor(EditColors.Primary),
                        decorationBox = { innerTextField ->
                            if (descriptionText.isEmpty()) {
                                Text("Thêm mô tả...", color = EditColors.TextHint, fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = EditColors.Divider)
                    
                    // Color Palette
                    ModernColorRow(
                        selectedColor = state.color,
                        onColorSelected = { viewModel.setColor(it) }
                    )
                }
            }
            
            // ============ CARD 2: Settings ============
            ModernCard {
                Column {
                    // Date - In edit mode, show the date user clicked on timeline and disable picker
                    val displayDate = if (state.isEditMode) state.selectedDate else state.date
                    ModernSettingRow(
                        icon = R.drawable.ic_points_star,
                        label = "Ngày",
                        value = displayDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.forLanguageTag("vi-VN"))),
                        enabled = !state.isEditMode, // Disable date change in edit mode
                        onClick = { if (!state.isEditMode) showDatePicker = true }
                    )
                    
                    ModernDivider()
                    
                    // Time
                    ModernSettingRow(
                        icon = R.drawable.ic_points_star,
                        label = "Thời gian",
                        value = state.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                        onClick = { showTimePicker = true }
                    )
                    
                    ModernDivider()
                    
                    // Duration
                    val durationLabel = durationToLabel(state.duration)
                    ModernSettingRow(
                        icon = R.drawable.ic_points_star,
                        label = "Thời lượng",
                        value = durationLabel,
                        onClick = { showDurationSheet = true }
                    )
                    
                    ModernDivider()
                    
                    // Repeat
                    ModernSettingRow(
                        icon = R.drawable.ic_points_star,
                        label = "Lặp lại",
                        value = repeatToLabel(state.repeat),
                        enabled = !state.applyTodayOnly,
                        onClick = { showRepeatSheet = true }
                    )
                    
                    ModernDivider()
                    
                    // Priority
                    val priorityInfo = priorityToInfo(state.priority)
                    ModernSettingRow(
                        icon = R.drawable.ic_points_star,
                        label = "Độ ưu tiên",
                        value = priorityInfo.first,
                        valueColor = priorityInfo.second,
                        onClick = { showPrioritySheet = true }
                    )
                    
                    // Today Only Switch (Edit mode)
                    if (state.isEditMode) {
                        ModernDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Chỉ áp dụng hôm nay", fontSize = 16.sp, color = EditColors.TextPrimary)
                            Switch(
                                checked = state.applyTodayOnly,
                                onCheckedChange = { viewModel.setApplyTodayOnly(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EditColors.Primary
                                )
                            )
                        }
                    }
                }
            }
            
            // ============ SUBTASKS ============
            ModernCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Nhiệm vụ con",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = EditColors.TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // List of subtasks
                    subtasks.forEachIndexed { index, subtask ->
                        SubtaskItem(
                            text = subtask,
                            onDelete = { subtasks = subtasks.toMutableList().also { it.removeAt(index) } }
                        )
                    }
                    
                    // Add new subtask input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clickable Add icon
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(2.dp, EditColors.Primary, CircleShape)
                                .clickable {
                                    if (newSubtaskText.isNotBlank()) {
                                        subtasks = subtasks + newSubtaskText.trim()
                                        newSubtaskText = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Thêm nhiệm vụ con",
                                tint = EditColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        BasicTextField(
                            value = newSubtaskText,
                            onValueChange = { newSubtaskText = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = EditColors.TextPrimary, fontSize = 16.sp),
                            cursorBrush = SolidColor(EditColors.Primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newSubtaskText.isNotBlank()) {
                                        subtasks = subtasks + newSubtaskText.trim()
                                        newSubtaskText = ""
                                    }
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (newSubtaskText.isEmpty()) {
                                    Text("Thêm nhiệm vụ con...", color = EditColors.TextHint, fontSize = 16.sp)
                                }
                                innerTextField()
                            },
                            singleLine = true
                        )
                        
                        // Add button (visible when there's text)
                        if (newSubtaskText.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    subtasks = subtasks + newSubtaskText.trim()
                                    newSubtaskText = ""
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Thêm",
                                    tint = EditColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // ============ DELETE (Edit mode) ============
            if (state.isEditMode) {
                ModernCard {
                    Text(
                        text = "Xóa tác vụ",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                // Nếu là tác vụ lặp lại, hiển thị dialog
                                if (viewModel.isRecurringTask()) {
                                    showDeleteDialog = true
                                } else {
                                    // Tác vụ một lần - xóa trực tiếp
                                    viewModel.deleteTask()
                                }
                            }
                            .padding(16.dp),
                        color = EditColors.Delete,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // ============ BOTTOM SHEETS ============
    
    // Icon Picker Sheet
    if (showIconSheet) {
        IconPickerSheet(
            currentLabel = state.iconLabel,
            onLabelSelected = { viewModel.setIcon(it); showIconSheet = false },
            onDismiss = { showIconSheet = false }
        )
    }
    
    // Duration Picker Sheet
    if (showDurationSheet) {
        DurationPickerSheet(
            currentDuration = state.duration,
            onDurationSelected = { viewModel.setDuration(it); showDurationSheet = false },
            onDismiss = { showDurationSheet = false }
        )
    }
    
    // Repeat Picker Sheet
    if (showRepeatSheet) {
        RepeatPickerSheet(
            currentRepeat = state.repeat,
            currentRepeatDays = state.repeatDays,
            onRepeatSelected = { viewModel.setRepeat(it) },
            onRepeatDayToggle = { viewModel.toggleRepeatDay(it) },
            onDismiss = { showRepeatSheet = false }
        )
    }
    
    // Priority Picker Sheet
    if (showPrioritySheet) {
        PriorityPickerSheet(
            currentPriority = state.priority,
            onPrioritySelected = { viewModel.setPriority(it); showPrioritySheet = false },
            onDismiss = { showPrioritySheet = false }
        )
    }
    
    // Date Picker
    if (showDatePicker) {
        ModernDatePicker(
            currentDate = state.date,
            onDateSelected = { viewModel.setDate(it); showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Time Picker
    if (showTimePicker) {
        ModernTimePicker(
            currentTime = state.time,
            onTimeSelected = { viewModel.setTime(it); showTimePicker = false },
            onDismiss = { showTimePicker = false }
        )
    }
    
    // Delete Options Dialog for recurring tasks
    if (showDeleteDialog) {
        DeleteOptionsDialog(
            onDeleteForToday = {
                viewModel.deleteForToday()
                showDeleteDialog = false
            },
            onDeleteFromToday = {
                viewModel.deleteFromToday()
                showDeleteDialog = false
            },
            onDeleteCompletely = {
                viewModel.deleteTask()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

// ============================================
// MODERN CARD
// ============================================
@Composable
private fun ModernCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = EditColors.Surface,
        shadowElevation = 0.dp
    ) {
        content()
    }
}

@Composable
private fun ModernDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        thickness = 0.5.dp,
        color = EditColors.Divider
    )
}

// ============================================
// MODERN SETTING ROW
// ============================================
@Composable
private fun ModernSettingRow(
    icon: Int,
    label: String,
    value: String,
    valueColor: Color = EditColors.TextSecondary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .then(if (!enabled) Modifier else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = if (enabled) EditColors.TextPrimary else EditColors.TextHint
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = if (enabled) valueColor else EditColors.TextHint
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = EditColors.TextHint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ============================================
// MODERN COLOR ROW
// ============================================
@Composable
private fun ModernColorRow(
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
            } catch (e: Exception) { EditColors.Primary }
            val isSelected = colorHex.equals(selectedColor, ignoreCase = true)
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ============================================
// SUBTASK ITEM
// ============================================
@Composable
private fun SubtaskItem(
    text: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, EditColors.TextHint, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = EditColors.TextPrimary
        )
        
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete",
                tint = EditColors.TextHint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ============================================
// ICON PICKER SHEET
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconPickerSheet(
    currentLabel: ScheduleLabel,
    onLabelSelected: (ScheduleLabel) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val labels = listOf(
        ScheduleLabel.wakeup to "Thức dậy",
        ScheduleLabel.eat to "Ăn uống",
        ScheduleLabel.exercise to "Tập luyện",
        ScheduleLabel.rest to "Nghỉ ngơi",
        ScheduleLabel.water to "Uống nước",
        ScheduleLabel.book to "Học tập",
        ScheduleLabel.sleep to "Ngủ",
        ScheduleLabel.clean to "Dọn dẹp",
        ScheduleLabel.cook to "Nấu ăn",
        ScheduleLabel.garden to "Làm vườn"
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = EditColors.SheetBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Chọn biểu tượng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EditColors.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(labels) { (label, name) ->
                    val isSelected = label == currentLabel
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) EditColors.Primary.copy(alpha = 0.15f) else Color.Transparent,
                        animationSpec = tween(200)
                    )
                    
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) EditColors.Primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onLabelSelected(label) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = getLabelIconResId(label.name)),
                            contentDescription = name,
                            tint = if (isSelected) EditColors.Primary else EditColors.TextSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            color = if (isSelected) EditColors.Primary else EditColors.TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ============================================
// DURATION PICKER SHEET
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationPickerSheet(
    currentDuration: String,
    onDurationSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val durations = listOf(
        "00:00:00" to "Không",
        "00:15:00" to "15 phút",
        "00:30:00" to "30 phút",
        "00:45:00" to "45 phút",
        "01:00:00" to "1 giờ",
        "01:30:00" to "1 giờ 30 phút",
        "02:00:00" to "2 giờ",
        "02:30:00" to "2 giờ 30 phút",
        "03:00:00" to "3 giờ",
        "04:00:00" to "4 giờ",
        "05:00:00" to "5 giờ",
        "06:00:00" to "6 giờ",
        "07:00:00" to "7 giờ",
        "08:00:00" to "8 giờ"
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = EditColors.SheetBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Thời lượng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            durations.forEach { (dur, label) ->
                val isSelected = dur == currentDuration
                SheetOption(
                    label = label,
                    isSelected = isSelected,
                    onClick = { onDurationSelected(dur) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ============================================
// REPEAT PICKER SHEET
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatPickerSheet(
    currentRepeat: RepeatType,
    currentRepeatDays: List<Int> = listOf(1, 2, 3, 4, 5),
    onRepeatSelected: (RepeatType) -> Unit,
    onRepeatDayToggle: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    val repeats = listOf(
        RepeatType.once to "Một lần",
        RepeatType.daily to "Hàng ngày",
        RepeatType.weekly to "Hàng tuần",
        RepeatType.monthly to "Hàng tháng",
        RepeatType.custom to "Tùy chỉnh theo thứ"
    )
    
    val weekDays = listOf(
        1 to "T2", 2 to "T3", 3 to "T4", 4 to "T5", 5 to "T6", 6 to "T7", 7 to "CN"
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = EditColors.SheetBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Lặp lại",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            repeats.forEach { (repeat, label) ->
                val isSelected = repeat == currentRepeat
                SheetOption(
                    label = label,
                    isSelected = isSelected,
                    onClick = { onRepeatSelected(repeat) }
                )
            }
            
            // Hiển thị weekday selector nếu chọn "Tùy chỉnh"
            if (currentRepeat == RepeatType.custom) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Chọn các thứ lặp lại:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = EditColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekDays.forEach { (dayValue, dayLabel) ->
                        val isActive = currentRepeatDays.contains(dayValue)
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onRepeatDayToggle(dayValue) },
                            color = if (isActive) EditColors.Primary else Color.Transparent,
                            shape = CircleShape
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 1.dp,
                                        color = if (isActive) EditColors.Primary else EditColors.Divider,
                                        shape = CircleShape
                                    )
                            ) {
                                Text(
                                    text = dayLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isActive) Color.White else EditColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ============================================
// PRIORITY PICKER SHEET
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityPickerSheet(
    currentPriority: PriorityType,
    onPrioritySelected: (PriorityType) -> Unit,
    onDismiss: () -> Unit
) {
    val priorities = listOf(
        PriorityType.high to ("Cao" to EditColors.Delete),
        PriorityType.medium to ("Trung bình" to EditColors.Warning),
        PriorityType.low to ("Thấp" to EditColors.Success)
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = EditColors.SheetBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Độ ưu tiên",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            priorities.forEach { (priority, labelColor) ->
                val (label, color) = labelColor
                val isSelected = priority == currentPriority
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onPrioritySelected(priority) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, fontSize = 16.sp, color = EditColors.TextPrimary)
                    }
                    
                    if (isSelected) {
                        Icon(Icons.Default.Check, null, tint = color)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ============================================
// SHEET OPTION
// ============================================
@Composable
private fun SheetOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) EditColors.PrimaryLight else Color.Transparent)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 16.sp,
            color = if (isSelected) EditColors.Primary else EditColors.TextPrimary,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
        
        if (isSelected) {
            Icon(Icons.Default.Check, null, tint = EditColors.Primary)
        }
    }
}

// ============================================
// MODERN DATE PICKER
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernDatePicker(
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.toEpochDay() * 86400000L
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val selected = LocalDate.ofEpochDay(it / 86400000L)
                    onDateSelected(selected)
                }
            }) {
                Text("OK", color = EditColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = EditColors.TextSecondary)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// ============================================
// MODERN TIME PICKER
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTimePicker(
    currentTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn giờ") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text("OK", color = EditColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = EditColors.TextSecondary)
            }
        }
    )
}

// ============================================
// HELPER FUNCTIONS
// ============================================
private fun durationToLabel(hhmmss: String): String {
    val parts = hhmmss.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val total = h * 60 + m
    return when (total) {
        0 -> "Không"
        in 1..59 -> "${total} phút"
        else -> {
            val hh = total / 60
            val mm = total % 60
            if (mm == 0) "${hh} giờ" else "${hh}h ${mm}p"
        }
    }
}

private fun repeatToLabel(r: RepeatType): String = when (r) {
    RepeatType.once -> "Một lần"
    RepeatType.daily -> "Hàng ngày"
    RepeatType.weekly -> "Hàng tuần"
    RepeatType.monthly -> "Hàng tháng"
    RepeatType.custom -> "Tùy chỉnh"
}

private fun priorityToInfo(p: PriorityType): Pair<String, Color> = when (p) {
    PriorityType.high -> "Cao" to Color(0xFFFF3B30)
    PriorityType.medium -> "Trung bình" to Color(0xFFFF9500)
    PriorityType.low -> "Thấp" to Color(0xFF34C759)
}

// ============================================
// DELETE OPTIONS DIALOG
// ============================================
@Composable
private fun DeleteOptionsDialog(
    onDeleteForToday: () -> Unit,
    onDeleteFromToday: () -> Unit,
    onDeleteCompletely: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EditColors.Surface,
        title = {
            Text(
                text = "Xóa tác vụ lặp lại",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = EditColors.TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Đây là tác vụ lặp lại. Bạn muốn xóa như thế nào?",
                    fontSize = 14.sp,
                    color = EditColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Option 1: Delete for today only
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDeleteForToday() },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = EditColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Chỉ xóa hôm nay",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = EditColors.TextPrimary
                            )
                            Text(
                                text = "Tác vụ vẫn còn ở các ngày khác",
                                fontSize = 13.sp,
                                color = EditColors.TextSecondary
                            )
                        }
                    }
                }
                
                HorizontalDivider(color = EditColors.Divider)
                
                // Option 2: Delete from today onwards
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDeleteFromToday() },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = EditColors.Warning,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Xóa từ hôm nay trở đi",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = EditColors.TextPrimary
                            )
                            Text(
                                text = "Không còn lặp lại ở các ngày sau",
                                fontSize = 13.sp,
                                color = EditColors.TextSecondary
                            )
                        }
                    }
                }
                
                HorizontalDivider(color = EditColors.Divider)
                
                // Option 3: Delete completely
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDeleteCompletely() },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = EditColors.Delete,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Xóa hoàn toàn",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = EditColors.Delete
                            )
                            Text(
                                text = "Xóa tác vụ và tất cả lịch sử",
                                fontSize = 13.sp,
                                color = EditColors.TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = EditColors.Primary)
            }
        }
    )
}

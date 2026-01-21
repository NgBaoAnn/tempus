package com.projectapp.tempus.ui.ai.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.domain.model.ScheduleSuggestion

/**
 * Bottom sheet for previewing and accepting/rejecting schedule suggestions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSuggestionSheet(
    suggestions: List<ScheduleSuggestion>,
    onAccept: (List<ScheduleSuggestion>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedItems = remember { mutableStateListOf<String>().apply { 
        addAll(suggestions.map { it.id })  // Select all by default
    }}
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ChatColors.Surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lịch trình đề xuất",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ChatColors.TextPrimary
                )
                
                Text(
                    text = "${selectedItems.size}/${suggestions.size} đã chọn",
                    style = MaterialTheme.typography.bodySmall,
                    color = ChatColors.TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Suggestions list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = suggestions,
                    key = { it.id }
                ) { suggestion ->
                    SuggestionItem(
                        suggestion = suggestion,
                        isSelected = selectedItems.contains(suggestion.id),
                        onToggle = {
                            if (selectedItems.contains(suggestion.id)) {
                                selectedItems.remove(suggestion.id)
                            } else {
                                selectedItems.add(suggestion.id)
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reject/Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ChatColors.TextSecondary
                    )
                ) {
                    Text("Hủy")
                }
                
                // Accept button
                Button(
                    onClick = {
                        val accepted = suggestions.filter { selectedItems.contains(it.id) }
                        onAccept(accepted)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChatColors.Accent
                    )
                ) {
                    Text("Thêm vào lịch")
                }
            }
        }
    }
}

/**
 * Single suggestion item with checkbox
 */
@Composable
private fun SuggestionItem(
    suggestion: ScheduleSuggestion,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) ChatColors.AccentLight.copy(alpha = 0.1f)
                     else ChatColors.SurfaceVariant,
        label = "bgColor"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = ChatColors.Accent,
                    uncheckedColor = ChatColors.TextMuted
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Time indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = suggestion.startTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ChatColors.Accent
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(ChatColors.TextMuted.copy(alpha = 0.3f))
                )
                Text(
                    text = suggestion.calculateEndTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = ChatColors.TextMuted
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Task info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ChatColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${suggestion.durationMinutes} phút",
                    style = MaterialTheme.typography.bodySmall,
                    color = ChatColors.TextSecondary
                )
            }
            
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (suggestion.priority) {
                            ScheduleSuggestion.Priority.HIGH -> ChatColors.Error
                            ScheduleSuggestion.Priority.MEDIUM -> ChatColors.Typing
                            ScheduleSuggestion.Priority.LOW -> ChatColors.Online
                        }
                    )
            )
        }
    }
}

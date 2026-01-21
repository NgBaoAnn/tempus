package com.projectapp.tempus.ui.ai.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.domain.model.ExecutionResult

/**
 * Card showing execution result after user accepts a proposal
 */
@Composable
fun ExecutionFeedback(
    result: ExecutionResult,
    modifier: Modifier = Modifier
) {
    val (icon, title, color) = if (result.success) {
        Triple("✅", "Thực hiện thành công!", ChatColors.Online)
    } else {
        Triple("❌", "Thực hiện thất bại", ChatColors.Error)
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ChatColors.Surface,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
            
            if (result.success && result.changesApplied.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ChatColors.SurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Changes applied
                Text(
                    text = "Đã thực hiện:",
                    style = MaterialTheme.typography.labelMedium,
                    color = ChatColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                result.changesApplied.forEach { change ->
                    ChangeItem(text = change)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            if (!result.success && result.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChatColors.Error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = ChatColors.Error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ChatColors.SurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Execution time
            Text(
                text = "⏱️ Thời gian: ${result.executionTimeMs}ms",
                style = MaterialTheme.typography.bodySmall,
                color = ChatColors.TextMuted
            )
        }
    }
}

@Composable
private fun ChangeItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(start = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(ChatColors.Online)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = ChatColors.TextPrimary
        )
    }
}

/**
 * Animated wrapper for execution feedback
 */
@Composable
fun AnimatedExecutionFeedback(
    result: ExecutionResult?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && result != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        result?.let {
            ExecutionFeedback(result = it)
        }
    }
}

package com.projectapp.tempus.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.data.sync.SyncState


@Composable
fun SyncButton(
    syncState: SyncState,
    pendingCount: Int,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSyncing = syncState.isSyncing
    val hasError = syncState.error != null
    
    Box(modifier = modifier) {
        
        FloatingActionButton(
            onClick = { if (!isSyncing) onSyncClick() },
            containerColor = when {
                hasError -> MaterialTheme.colorScheme.errorContainer
                isSyncing -> MaterialTheme.colorScheme.secondaryContainer
                pendingCount > 0 -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                hasError -> MaterialTheme.colorScheme.onErrorContainer
                isSyncing -> MaterialTheme.colorScheme.onSecondaryContainer
                pendingCount > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = when {
                        hasError -> Icons.Default.CloudOff
                        pendingCount == 0 -> Icons.Default.CloudDone
                        else -> Icons.Default.CloudSync
                    },
                    contentDescription = "Sync"
                )
            }
        }
        
        
        AnimatedVisibility(
            visible = pendingCount > 0 && !isSyncing,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Badge(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
            ) {
                Text(
                    text = if (pendingCount > 99) "99+" else pendingCount.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun SyncProgressBar(
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = syncState.isSyncing && syncState.progress != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        syncState.progress?.let { progress ->
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = progress.phase,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${progress.current}/${progress.total}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (progress.current.toFloat() / progress.total.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}


@Composable
fun SyncErrorMessage(
    error: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = error != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        error?.let {
            Snackbar(
                action = {
                    TextButton(onClick = onDismiss) {
                        Text("Đóng")
                    }
                },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Text(it)
            }
        }
    }
}


@Composable
fun LastSyncIndicator(
    lastSyncTime: Long?,
    modifier: Modifier = Modifier
) {
    lastSyncTime?.let { time ->
        val timeAgo = remember(time) {
            val diff = System.currentTimeMillis() - time
            when {
                diff < 60_000 -> "Vừa xong"
                diff < 3600_000 -> "${diff / 60_000} phút trước"
                diff < 86400_000 -> "${diff / 3600_000} giờ trước"
                else -> "${diff / 86400_000} ngày trước"
            }
        }
        
        Text(
            text = "Sync: $timeAgo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    }
}

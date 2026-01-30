package com.projectapp.tempus.ui.garden.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.domain.model.TreeState


@Composable
fun TreeDetailDialog(
    tree: TreeUiModel,
    onDismiss: () -> Unit,
    onWater: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = tree.type.emoji,
                    fontSize = 28.sp
                )
                Text(
                    text = tree.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                
                DetailRow(
                    label = "📊 Trạng thái",
                    value = "${tree.state.emoji} ${tree.state.displayName}"
                )
                
                
                DetailRow(
                    label = "📈 Tiến độ",
                    value = "${tree.progressPercent.toInt()}%"
                )
                
                
                DetailRow(
                    label = "💰 Đã đầu tư",
                    value = "${tree.investedPoints} điểm"
                )
                
                
                if (tree.isAlive && tree.state != TreeState.TREE) {
                    val warningColor = when {
                        tree.daysUntilDeath <= 1 -> Color(0xFFFF3B30)
                        tree.daysUntilDeath <= 2 -> Color(0xFFFF9500)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    
                    DetailRow(
                        label = "⏰ Thời gian còn lại",
                        value = "${tree.daysUntilDeath} ngày",
                        valueColor = warningColor
                    )
                }
                
                
                Column {
                    Text(
                        text = "Tiến độ phát triển",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { tree.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (tree.isAlive && tree.state != TreeState.TREE) {
                Button(
                    onClick = onWater,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("💧 Tưới cây (10 điểm)")
                }
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFF3B30)
                    )
                ) {
                    Text("Xóa cây")
                }
                TextButton(onClick = onDismiss) {
                    Text("Đóng")
                }
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}


@Composable
fun DeleteTreeDialog(
    treeName: String,
    investedPoints: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Text(text = "🗑️", fontSize = 32.sp)
        },
        title = {
            Text(
                text = "Xóa cây?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Bạn có chắc muốn xóa \"$treeName\"?\n\nHành động này không thể hoàn tác và bạn sẽ mất $investedPoints điểm đã đầu tư."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF3B30)
                )
            ) {
                Text("Xóa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

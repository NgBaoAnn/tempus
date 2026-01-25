package com.projectapp.tempus.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Colors for points notification
 */
private object PointsNotificationColors {
    val SurfaceDark = Color(0xFF1A1A2E)
    val SurfaceCard = Color(0xFF16213E)
    val Gold = Color(0xFFFFD700)
    val Green = Color(0xFF00D26A)
    val Red = Color(0xFFFF6B6B)
    val Text = Color.White
    val TextSecondary = Color(0xFFB0B0B0)
}

/**
 * Center overlay notification hiển thị khi user kiếm hoặc mất điểm
 * 
 * @param points Số điểm thay đổi (dương = cộng, âm = trừ)
 * @param reason Lý do (vd: "Hoàn thành Task", "Huỷ hoàn thành Task")
 * @param onDismiss Callback khi notification bị đóng
 */
@Composable
fun PointsNotification(
    points: Int,
    reason: String,
    onDismiss: () -> Unit
) {
    // Auto dismiss after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }
    
    // Determine if earning or losing points
    val isEarning = points > 0
    val displayPoints = abs(points)
    val pointsText = if (isEarning) "+$displayPoints" else "-$displayPoints"
    val pointsColor = if (isEarning) PointsNotificationColors.Green else PointsNotificationColors.Red
    val emoji = if (isEarning) "🎉" else "📉"
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Scale animation
        var scale by remember { mutableFloatStateOf(0.8f) }
        val animatedScale by animateFloatAsState(
            targetValue = scale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "scale"
        )
        
        LaunchedEffect(Unit) {
            scale = 1f
        }
        
        Card(
            modifier = Modifier
                .width(260.dp)
                .scale(animatedScale)
                .clickable { onDismiss() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PointsNotificationColors.SurfaceCard,
                                PointsNotificationColors.SurfaceDark
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Emoji
                    Text(
                        text = emoji,
                        fontSize = 48.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Points with animation
                    Text(
                        text = pointsText,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = pointsColor,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "điểm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = PointsNotificationColors.TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Reason with chip style
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = pointsColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = reason,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = pointsColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tap hint
                    Text(
                        text = "Nhấn để đóng",
                        fontSize = 12.sp,
                        color = PointsNotificationColors.TextSecondary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * State holder for showing points notification
 */
data class PointsNotificationState(
    val show: Boolean = false,
    val points: Int = 0,
    val reason: String = ""
)

package com.projectapp.tempus.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.projectapp.tempus.R
import kotlinx.coroutines.delay

/**
 * Colors for points notification
 */
private object PointsNotificationColors {
    val Background = Color(0xFF1C1C1E)
    val Surface = Color(0xFF2C2C2E)
    val Gold = Color(0xFFFFD700)
    val Green = Color(0xFF34C759)
    val Text = Color.White
    val TextSecondary = Color(0xFFAEAEB2)
}

/**
 * Center overlay notification hiển thị khi user kiếm được điểm
 * 
 * @param points Số điểm được cộng (phải > 0)
 * @param reason Lý do nhận điểm (vd: "Hoàn thành Pomodoro", "Hoàn thành Task")
 * @param onDismiss Callback khi notification bị đóng (auto sau 3s hoặc user tap)
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
                stiffness = Spring.StiffnessLow
            ),
            label = "scale"
        )
        
        LaunchedEffect(Unit) {
            scale = 1f
        }
        
        Surface(
            modifier = Modifier
                .width(280.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .clickable { onDismiss() },
            shape = RoundedCornerShape(24.dp),
            color = PointsNotificationColors.Surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Star icon with animation
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = PointsNotificationColors.Gold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_points_star),
                        contentDescription = "Points",
                        tint = PointsNotificationColors.Gold,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Points earned text
                Text(
                    text = "+$points",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = PointsNotificationColors.Green
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "điểm",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = PointsNotificationColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Reason
                Text(
                    text = reason,
                    fontSize = 14.sp,
                    color = PointsNotificationColors.Text.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close hint
                Text(
                    text = "Nhấn để đóng",
                    fontSize = 12.sp,
                    color = PointsNotificationColors.TextSecondary.copy(alpha = 0.6f)
                )
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

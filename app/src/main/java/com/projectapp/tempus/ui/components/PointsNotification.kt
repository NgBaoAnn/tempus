package com.projectapp.tempus.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
<<<<<<< HEAD
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.TrendingDown
=======
import androidx.compose.foundation.shape.RoundedCornerShape
>>>>>>> master
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
<<<<<<< HEAD
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
=======
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
>>>>>>> master
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
<<<<<<< HEAD
 * Bright, vibrant colors following UI/UX Pro Max guidelines
 */
private object PointsColors {
    // Bright gradient for earning points
    val EarnGradientStart = Color(0xFF00E676)
    val EarnGradientEnd = Color(0xFF00C853)
    
    // Gradient for losing points
    val DeductGradientStart = Color(0xFFFF6B6B)
    val DeductGradientEnd = Color(0xFFEE5A5A)
    
    // Card background
    val CardBackground = Color(0xFFFFFFF8)
    
    // Text colors
    val TextDark = Color(0xFF1A1A2E)
    val TextMuted = Color(0xFF6B7280)
}

/**
 * State holder for showing points notification
 */
data class PointsNotificationState(
    val show: Boolean = false,
    val points: Int = 0,
    val reason: String = ""
)

/**
 * Modern horizontal layout points notification
 * Icon and points are aligned horizontally for better visual flow
=======
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
>>>>>>> master
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
    
<<<<<<< HEAD
    val isEarning = points > 0
    val displayPoints = abs(points)
    val pointsText = if (isEarning) "+$displayPoints" else "-$displayPoints"
    
    val gradientColors = if (isEarning) {
        listOf(PointsColors.EarnGradientStart, PointsColors.EarnGradientEnd)
    } else {
        listOf(PointsColors.DeductGradientStart, PointsColors.DeductGradientEnd)
    }
    val accentColor = if (isEarning) PointsColors.EarnGradientStart else PointsColors.DeductGradientStart
    val icon: ImageVector = if (isEarning) Icons.Rounded.CheckCircle else Icons.Rounded.TrendingDown
=======
    // Determine if earning or losing points
    val isEarning = points > 0
    val displayPoints = abs(points)
    val pointsText = if (isEarning) "+$displayPoints" else "-$displayPoints"
    val pointsColor = if (isEarning) PointsNotificationColors.Green else PointsNotificationColors.Red
    val emoji = if (isEarning) "🎉" else "📉"
>>>>>>> master
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
<<<<<<< HEAD
        // Bounce scale animation
        var scale by remember { mutableFloatStateOf(0.7f) }
=======
        // Scale animation
        var scale by remember { mutableFloatStateOf(0.8f) }
>>>>>>> master
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
<<<<<<< HEAD
                .widthIn(min = 280.dp, max = 340.dp)
                .scale(animatedScale)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = accentColor.copy(alpha = 0.3f),
                    spotColor = accentColor.copy(alpha = 0.3f)
                )
                .clickable { onDismiss() },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = PointsColors.CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            ) {
                // ===== HORIZONTAL LAYOUT: Icon + Points =====
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Icon with gradient circle background
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(gradientColors)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Points value + label stacked
                    Column {
                        Text(
                            text = pointsText,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            lineHeight = 48.sp
                        )
                        Text(
                            text = "điểm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = PointsColors.TextMuted,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Reason with pill/tag style
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEarning) {
                            Text(
                                text = "⭐",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        
                        Text(
                            text = reason,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tap to dismiss hint
                Text(
                    text = "Nhấn để đóng",
                    fontSize = 12.sp,
                    color = PointsColors.TextMuted.copy(alpha = 0.5f)
                )
=======
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
>>>>>>> master
            }
        }
    }
}
<<<<<<< HEAD
=======

/**
 * State holder for showing points notification
 */
data class PointsNotificationState(
    val show: Boolean = false,
    val points: Int = 0,
    val reason: String = ""
)
>>>>>>> master

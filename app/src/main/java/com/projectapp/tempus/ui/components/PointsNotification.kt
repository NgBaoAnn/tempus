package com.projectapp.tempus.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlin.math.abs


private object PointsColors {
    
    val EarnGradientStart = Color(0xFF00E676)
    val EarnGradientEnd = Color(0xFF00C853)
    
    
    val DeductGradientStart = Color(0xFFFF6B6B)
    val DeductGradientEnd = Color(0xFFEE5A5A)
    
    
    val CardBackground = Color(0xFFFFFFF8)
    
    
    val TextDark = Color(0xFF1A1A2E)
    val TextMuted = Color(0xFF6B7280)
}


data class PointsNotificationState(
    val show: Boolean = false,
    val points: Int = 0,
    val reason: String = ""
)


@Composable
fun PointsNotification(
    points: Int,
    reason: String,
    onDismiss: () -> Unit
) {
    
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }
    
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
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        
        var scale by remember { mutableFloatStateOf(0.7f) }
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
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    
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
                
                
                Text(
                    text = "Nhấn để đóng",
                    fontSize = 12.sp,
                    color = PointsColors.TextMuted.copy(alpha = 0.5f)
                )
            }
        }
    }
}

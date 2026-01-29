package com.projectapp.tempus.ui.timeline.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R
import com.projectapp.tempus.data.quote.dto.QuoteDto

/**
 * Premium Daily Quote Card - Aurora UI inspired with collapsible feature
 */
@Composable
fun DailyQuoteCard(
    quote: QuoteDto?,
    modifier: Modifier = Modifier
) {
    if (quote == null) return
    
    // Collapsible state - default expanded to show quote on first load
    var isExpanded by remember { mutableStateOf(true) }
    
    // Subtle shimmer animation
    val infiniteTransition = rememberInfiniteTransition(label = "quote_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        ) {
            // Decorative circles (smaller)
            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .offset(x = (-15).dp, y = (-15).dp)
                        .alpha(shimmerAlpha)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 15.dp, y = 8.dp)
                        .alpha(shimmerAlpha * 0.8f)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp, 
                        vertical = if (isExpanded) 12.dp else 8.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row with expand/collapse indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_points_star),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Câu nói hôm nay",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_points_star),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Collapsed view - show truncated quote
                if (!isExpanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${quote.text}\"",
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Expanded view - show full quote
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Quote text with elegant typography
                        Text(
                            text = "\"${quote.text}\"",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        
                        // Author attribution
                        if (!quote.author.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.6f))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = quote.author,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.95f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

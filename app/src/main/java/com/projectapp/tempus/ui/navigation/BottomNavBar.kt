package com.projectapp.tempus.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R

/**
 * Navigation items for bottom bar
 */
sealed class NavItem(
    val route: String,
    val titleResId: Int, // Use resource ID
    val icon: Int, // Giữ lại để tương thích với code cũ
    val fragmentId: Int,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    data object Timer : NavItem(
        "timer", R.string.nav_timer, R.drawable.ic_timer, R.id.timerFragment,
        Icons.Filled.Timer, Icons.Outlined.Timer
    )
    data object Timeline : NavItem(
        "timeline", R.string.nav_timeline, R.drawable.ic_timeline, R.id.timelineFragment,
        Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth
    )
    data object AI : NavItem(
        "ai", R.string.nav_ai, R.drawable.ic_ai, R.id.aiFragment,
        Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome
    )
    data object Social : NavItem(
        "social", R.string.nav_social, R.drawable.ic_social, R.id.socialFragment,
        Icons.Filled.People, Icons.Outlined.People
    )
    data object Statistics : NavItem(
        "statistics", R.string.nav_stats, R.drawable.ic_statistics, R.id.statisticsFragment,
        Icons.Filled.BarChart, Icons.Outlined.BarChart
    )
    data object Settings : NavItem(
        "settings", R.string.nav_settings, R.drawable.ic_settings, R.id.settingsFragment,
        Icons.Filled.Settings, Icons.Outlined.Settings
    )
}

val navItems = listOf(
    NavItem.Timer,
    NavItem.Timeline,
    NavItem.AI,
    NavItem.Social,
    NavItem.Statistics,
    NavItem.Settings
)

/**
 * Modern Bottom Navigation Colors
 */
/**
 * Modern Premium Bottom Navigation Bar
 * - Floating design with rounded corners
 * - Gradient indicator for active item
 * - Smooth animations
 * - Material 3 icons
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemClick: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                ModernNavItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.ModernNavItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animations
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // Use Primary color for active icon
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(200),
        label = "iconColor"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(200),
        label = "textColor"
    )
    
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            // Icon with indicator
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Active indicator (gradient pill behind icon)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    )
                                )
                            )
                    )
                }
                
                // Icon
                Icon(
                    imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                    contentDescription = stringResource(id = item.titleResId),
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Label with animated visibility
            Text(
                text = stringResource(id = item.titleResId),
                color = textColor,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

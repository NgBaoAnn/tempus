package com.projectapp.tempus.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R

/**
 * Navigation items for bottom bar
 */
sealed class NavItem(
    val route: String,
    val title: String,
    val icon: Int,
    val fragmentId: Int
) {
    data object Timer : NavItem("timer", "Hẹn giờ", R.drawable.ic_timer, R.id.timerFragment)
    data object Timeline : NavItem("timeline", "Lịch", R.drawable.ic_timeline, R.id.timelineFragment)
    data object AI : NavItem("ai", "AI", R.drawable.ic_ai, R.id.aiFragment)
    data object Statistics : NavItem("statistics", "Thống kê", R.drawable.ic_statistics, R.id.statisticsFragment)
    data object Garden : NavItem("garden", "Garden", R.drawable.ic_garden, R.id.gardenFragment)
    data object Settings : NavItem("settings", "Cài đặt", R.drawable.ic_settings, R.id.settingsFragment)
}

val navItems = listOf(
    NavItem.Timer,
    NavItem.Timeline,
    NavItem.AI,
    NavItem.Statistics,
    NavItem.Garden,
    NavItem.Settings
)

/**
 * Bottom Navigation Colors
 */
object BottomNavColors {
    val Background = Color(0xFFFFFFFF)
    val ActivePrimary = Color(0xFF3B82F6)
    val ActiveBackground = Color(0xFFEFF6FF)
    val InactiveText = Color(0xFF64748B)
    val Surface = Color(0xFFF8FAFC)
}

/**
 * Custom Bottom Navigation Bar with modern design
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemClick: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = BottomNavColors.Background,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                BottomNavItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) BottomNavColors.ActivePrimary else BottomNavColors.InactiveText,
        label = "textColor"
    )
    
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) BottomNavColors.ActiveBackground else Color.Transparent,
        label = "bgColor"
    )
    
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            // Use Image instead of Icon to preserve original colors
            Image(
                painter = painterResource(id = item.icon),
                contentDescription = item.title,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.title,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

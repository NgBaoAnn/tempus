package com.projectapp.tempus.ui.setting.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.ui.theme.ThemeMode
import com.projectapp.tempus.ui.theme.TempusDesignSystem
import androidx.compose.ui.res.stringResource
import com.projectapp.tempus.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.theme_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            Text(
                text = stringResource(R.string.theme_select_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            
            
            ThemeOptionCard(
                title = stringResource(R.string.theme_light),
                subtitle = stringResource(R.string.theme_light_desc),
                emoji = "☀️",
                isSelected = currentThemeMode == ThemeMode.LIGHT,
                previewColors = listOf(Color.White, TempusDesignSystem.Slate100),
                onClick = { onThemeSelected(ThemeMode.LIGHT) }
            )
            
            ThemeOptionCard(
                title = stringResource(R.string.theme_dark),
                subtitle = stringResource(R.string.theme_dark_desc),
                emoji = "🌙",
                isSelected = currentThemeMode == ThemeMode.DARK,
                previewColors = listOf(TempusDesignSystem.Slate900, TempusDesignSystem.Slate800),
                onClick = { onThemeSelected(ThemeMode.DARK) }
            )
            
            ThemeOptionCard(
                title = stringResource(R.string.theme_system),
                subtitle = stringResource(R.string.theme_system_desc),
                emoji = "📱",
                isSelected = currentThemeMode == ThemeMode.SYSTEM,
                previewColors = listOf(Color.White, TempusDesignSystem.Slate900),
                isGradientSplit = true,
                onClick = { onThemeSelected(ThemeMode.SYSTEM) }
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    subtitle: String,
    emoji: String,
    isSelected: Boolean,
    previewColors: List<Color>,
    isGradientSplit: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) TempusDesignSystem.Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(200),
        label = "borderColor"
    )
    
    val borderWidth = if (isSelected) 2.dp else 1.dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isGradientSplit) {
                            Modifier.background(Color.Transparent)
                        } else {
                            Modifier.background(
                                Brush.linearGradient(previewColors)
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isGradientSplit) {
                    
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    previewColors[0],
                                    RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    previewColors[1],
                                    RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                                )
                        )
                    }
                }
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TempusDesignSystem.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.theme_selected_content_desc),
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

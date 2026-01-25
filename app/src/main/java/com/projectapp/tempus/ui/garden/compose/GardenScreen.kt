package com.projectapp.tempus.ui.garden.compose

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectapp.tempus.domain.model.TreeType
import com.projectapp.tempus.ui.garden.TreeDetailActivity

/**
 * Main Garden Screen với compact stats và animated tree cards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    viewModel: GardenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Plant tree dialog
    if (uiState.showPlantDialog) {
        PlantTreeDialog(
            currentPoints = uiState.currentPoints,
            affordableTrees = uiState.affordableTrees,
            onDismiss = { viewModel.dismissPlantDialog() },
            onPlant = { treeType ->
                viewModel.plantTree(
                    type = treeType,
                    onSuccess = {
                        Toast.makeText(context, "🌱 Đã trồng ${treeType.displayName}!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "My Garden",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Compact inline stats
                    CompactStats(
                        totalTrees = uiState.totalTrees,
                        matureTrees = uiState.matureTrees,
                        totalPoints = uiState.currentPoints,
                        streak = uiState.currentStreak
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showPlantTreeDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Trồng cây mới")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (uiState.trees.isEmpty()) {
                GardenEmptyState(
                    onPlantClick = { viewModel.showPlantTreeDialog() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 100.dp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    itemsIndexed(
                        items = uiState.trees,
                        key = { _, tree -> tree.id }
                    ) { index, tree ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 50
                                )
                            ) + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 50
                                )
                            )
                        ) {
                            TreeCard(
                                tree = tree,
                                onClick = { 
                                    navigateToTreeDetail(context, tree) 
                                },
                                onLongClick = { 
                                    navigateToTreeDetail(context, tree) 
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact inline stats cho TopAppBar - chỉ hiện thông tin quan trọng nhất
 */
@Composable
private fun CompactStats(
    totalTrees: Int,
    matureTrees: Int,
    totalPoints: Int,
    streak: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        // Chỉ hiển thị điểm và streak để tránh bị wrap
        StatChip(emoji = "💰", value = totalPoints)
        if (streak > 0) {
            StatChip(emoji = "🔥", value = streak)
        }
        // Hiển thị số cây trưởng thành / tổng cây
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌲", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "$matureTrees/$totalTrees",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StatChip(emoji: String, value: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PlantTreeDialog(
    currentPoints: Int,
    affordableTrees: List<TreeType>,
    onDismiss: () -> Unit,
    onPlant: (TreeType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🌱",
                    fontSize = 40.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Trồng cây mới",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bạn có $currentPoints 💰",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (affordableTrees.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "❌ Cần ít nhất ${TreeType.OAK.costToPlant} điểm để trồng cây!",
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    TreeType.entries.forEach { treeType ->
                        val canAfford = currentPoints >= treeType.costToPlant
                        
                        Surface(
                            onClick = { if (canAfford) onPlant(treeType) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (canAfford) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            enabled = canAfford
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Tree emoji/icon
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (canAfford)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                ) {
                                    Text(
                                        text = treeType.emoji,
                                        fontSize = 28.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                
                                // Tree info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = treeType.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (canAfford) 
                                            MaterialTheme.colorScheme.onSurface 
                                        else 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "${treeType.costToPlant} điểm",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (canAfford)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                
                                // Status icon
                                if (canAfford) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Trồng",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

private fun navigateToTreeDetail(context: Context, tree: TreeUiModel) {
    val intent = Intent(context, TreeDetailActivity::class.java).apply {
        putExtra(TreeDetailActivity.EXTRA_TREE_ID, tree.id)
        putExtra(TreeDetailActivity.EXTRA_TREE_NAME, tree.name)
        putExtra(TreeDetailActivity.EXTRA_TREE_STATE, tree.state.name)
        putExtra(TreeDetailActivity.EXTRA_TREE_TYPE, tree.type.name)
        putExtra(TreeDetailActivity.EXTRA_INVESTED_POINTS, tree.investedPoints)
        putExtra(TreeDetailActivity.EXTRA_PROGRESS, tree.progressPercent)
        putExtra(TreeDetailActivity.EXTRA_DAYS_UNTIL_DEATH, tree.daysUntilDeath)
    }
    context.startActivity(intent)
}

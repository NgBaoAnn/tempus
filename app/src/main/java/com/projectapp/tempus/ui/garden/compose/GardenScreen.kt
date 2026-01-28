package com.projectapp.tempus.ui.garden.compose

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.domain.model.TreeGrowthCalculator
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType
import com.projectapp.tempus.ui.garden.GardenViewModel
import com.projectapp.tempus.ui.garden.TreeDetailActivity
import com.projectapp.tempus.ui.garden.compose.drawing.ProceduralTreeSize

// ======================== DESIGN SYSTEM ========================

private object GardenDesign {
    val Primary = Color(0xFF4CAF50)
    val Background = Color(0xFFF1F8E9)
    val CardBg = Color(0xFFFFFFFF)
    val StreakFire = Color(0xFFFF5722)
    val PointsGold = Color(0xFFFFB300)
}

// ======================== SKELETON LOADING ========================

/**
 * Shimmer Loading Skeleton for Garden - displays grid of skeleton tree cards
 */
@Composable
private fun GardenLoadingSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        ),
        start = Offset(shimmerTranslateAnim - 500f, 0f),
        end = Offset(shimmerTranslateAnim, 0f)
    )
    
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
        modifier = modifier.fillMaxSize()
    ) {
        items(6) {
            SkeletonTreeCard(shimmerBrush)
        }
    }
}

@Composable
private fun SkeletonTreeCard(brush: Brush) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = GardenDesign.CardBg
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tree placeholder
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Name placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // State badge placeholder
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush)
            )
        }
    }
}

// ======================== MAIN SCREEN ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    viewModel: GardenViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Local state for dialog
    var showPlantDialog by remember { mutableStateOf(false) }
    
    // Plant tree dialog
    if (showPlantDialog) {
        PlantTreeDialog(
            currentPoints = uiState.userPoints?.totalPoints ?: 0,
            affordableTrees = viewModel.getAffordableTrees(uiState.userPoints?.totalPoints ?: 0),
            onDismiss = { showPlantDialog = false },
            onPlant = { treeType ->
                viewModel.plantTree(
                    type = treeType,
                    onSuccess = {
                        showPlantDialog = false
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Timeline"
                        )
                    }
                },
                actions = {
                    // Compact inline stats
                    CompactStats(
                        totalTrees = uiState.totalTrees,
                        matureTrees = uiState.matureTrees,
                        totalPoints = uiState.userPoints?.totalPoints ?: 0,
                        streak = uiState.userPoints?.currentStreak ?: 0
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refresh() }
                    ) {
                         Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPlantDialog = true },
                containerColor = GardenDesign.Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Trồng cây mới")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            GardenLoadingSkeleton(modifier = Modifier.padding(padding))
        } else {
            if (uiState.trees.isEmpty()) {
                EmptyGardenState(
                    onPlantClick = { showPlantDialog = true }
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
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================== COMPONENTS ========================

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
private fun TreeCard(
    tree: TreeEntity,
    onClick: () -> Unit
) {
    val state = TreeState.fromString(tree.state)
    val type = TreeType.fromString(tree.treeType)
    val calculator = remember { TreeGrowthCalculator() }
    val progress = calculator.getProgressPercent(tree.investedPoints)
    val daysUntilDeath = calculator.getDaysUntilDeath(tree.lastWateredAt)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = GardenDesign.CardBg
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tree with ProceduralTree (same as TreeDetailActivity)
            Box(
                modifier = Modifier.height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                ProceduralTree(
                    treeType = type,
                    growthStage = state,
                    treeId = tree.id,
                    size = ProceduralTreeSize.SMALL.dp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Name
            Text(
                text = tree.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Warning if dying
            if (tree.isAlive && daysUntilDeath <= 2 && state != TreeState.TREE) {
                Text(
                    text = "⚠️ Sắp héo!",
                    fontSize = 10.sp,
                    color = GardenDesign.StreakFire,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // State Badge
                Surface(
                    color = getTreeStateColor(state).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = state.displayName,
                        fontSize = 10.sp,
                        color = getTreeStateColor(state),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GardenDesign.Primary,
                trackColor = GardenDesign.Background
            )
        }
    }
}

@Composable
private fun EmptyGardenState(onPlantClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌱",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Khu vườn trống trơn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hãy trồng cái cây đầu tiên của bạn!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onPlantClick,
            colors = ButtonDefaults.buttonColors(containerColor = GardenDesign.Primary)
        ) {
            Text("Trồng cây ngay")
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

// ======================== HELPERS ========================

private fun getTreeStateColor(state: TreeState): Color {
    return when (state) {
        TreeState.SEED -> Color(0xFF8B4513)
        TreeState.SPROUT -> Color(0xFF32CD32)
        TreeState.SAPLING -> Color(0xFF228B22)
        TreeState.TREE -> Color(0xFF006400)
        TreeState.DEAD -> Color(0xFF696969)
    }
}

private fun navigateToTreeDetail(context: Context, tree: TreeEntity) {
    val calculator = TreeGrowthCalculator()
    val progress = calculator.getProgressPercent(tree.investedPoints)
    val daysUntilDeath = calculator.getDaysUntilDeath(tree.lastWateredAt)
    val type = TreeType.fromString(tree.treeType)
    val state = TreeState.fromString(tree.state)
    
    val intent = Intent(context, TreeDetailActivity::class.java).apply {
        putExtra(TreeDetailActivity.EXTRA_TREE_ID, tree.id)
        putExtra(TreeDetailActivity.EXTRA_TREE_NAME, tree.name)
        putExtra(TreeDetailActivity.EXTRA_TREE_STATE, state.name)
        putExtra(TreeDetailActivity.EXTRA_TREE_TYPE, type.name)
        putExtra(TreeDetailActivity.EXTRA_INVESTED_POINTS, tree.investedPoints)
        putExtra(TreeDetailActivity.EXTRA_PROGRESS, progress)
        putExtra(TreeDetailActivity.EXTRA_DAYS_UNTIL_DEATH, daysUntilDeath)
    }
    context.startActivity(intent)
}

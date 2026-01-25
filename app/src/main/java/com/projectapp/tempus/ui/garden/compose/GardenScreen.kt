package com.projectapp.tempus.ui.garden.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.R
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.domain.model.TreeGrowthCalculator
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType
import com.projectapp.tempus.ui.garden.GardenUiState
import com.projectapp.tempus.ui.garden.GardenViewModel

// ======================== DESIGN SYSTEM ========================

private object GardenDesign {
    val Primary = Color(0xFF4CAF50)        // Green 500
    val PrimaryDark = Color(0xFF1B5E20)    // Green 900
    val Secondary = Color(0xFF8D6E63)      // Brown 400
    val Background = Color(0xFFF1F8E9)     // Light Green 50
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF1B5E20)
    val TextSecondary = Color(0xFF5D4037)
    val CardBg = Color(0xFFFFFFFF)
    
    val StreakFire = Color(0xFFFF5722)
    val PointsGold = Color(0xFFFFB300)
    
    val GradientGreen = Brush.verticalGradient(
        colors = listOf(Color(0xFF66BB6A), Color(0xFF43A047))
    )
}

// ======================== MAIN SCREEN ========================

@Composable
fun GardenScreen(
    viewModel: GardenViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // State for dialogs
    var showPlantDialog by remember { mutableStateOf(false) }
    var selectedTreeForDetails by remember { mutableStateOf<TreeEntity?>(null) }
    
    Scaffold(
        containerColor = GardenDesign.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPlantDialog = true },
                containerColor = GardenDesign.Primary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, "Trồng cây", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header Stats
            GardenHeader(
                userPoints = uiState.userPoints?.totalPoints ?: 0,
                streak = uiState.userPoints?.currentStreak ?: 0,
                totalTrees = uiState.totalTrees,
                matureTrees = uiState.matureTrees
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GardenDesign.Primary)
                }
            } else if (uiState.trees.isEmpty()) {
                EmptyGardenState { showPlantDialog = true }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.trees, key = { it.id }) { tree ->
                        TreeCard(
                            tree = tree,
                            onClick = { selectedTreeForDetails = tree }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // Padding for FAB
                }
            }
        }
    }
    
    // Dialogs
    if (showPlantDialog) {
        PlantTreeDialog(
            currentPoints = uiState.userPoints?.totalPoints ?: 0,
            onDismiss = { showPlantDialog = false },
            onPlant = { type ->
                viewModel.plantTree(
                    type = type,
                    onSuccess = { showPlantDialog = false },
                    onError = { /* Toast logic handled in VM or external */ }
                )
            },
            viewModel = viewModel
        )
    }
    
    selectedTreeForDetails?.let { tree ->
        TreeDetailsDialog(
            tree = tree,
            viewModel = viewModel,
            onDismiss = { selectedTreeForDetails = null },
            onWater = {
                viewModel.waterTree(
                    treeId = tree.id,
                    onSuccess = { selectedTreeForDetails = null }, // Refresh logic auto updates
                    onError = { }
                )
            },
            onDelete = {
                 viewModel.deleteTree(
                     tree = tree,
                     onSuccess = { selectedTreeForDetails = null },
                     onError = { }
                 )
            }
        )
    }
}

// ======================== COMPONENTS ========================

@Composable
private fun GardenHeader(
    userPoints: Int,
    streak: Int,
    totalTrees: Int,
    matureTrees: Int
) {
    Column {
        // Points & Streak Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Points
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = GardenDesign.Surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = null,
                        tint = GardenDesign.PointsGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$userPoints",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GardenDesign.TextPrimary
                    )
                }
            }
            
            // Streak
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = GardenDesign.Surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = GardenDesign.StreakFire,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$streak ngày",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GardenDesign.TextPrimary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tree Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatsItem("Tổng cây", "$totalTrees")
            StatsItem("Trưởng thành", "$matureTrees")
        }
    }
}

@Composable
private fun StatsItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GardenDesign.TextPrimary)
        Text(text = label, fontSize = 12.sp, color = GardenDesign.TextSecondary)
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
            // Tree Image
            Box(
                modifier = Modifier.height(100.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = getTreeDrawable(type, state)),
                    contentDescription = tree.name,
                    modifier = Modifier.size(80.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Name
            Text(
                text = tree.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                color = GardenDesign.TextPrimary
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
        Icon(
            imageVector = Icons.Outlined.Yard,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GardenDesign.Primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Khu vườn trống trơn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GardenDesign.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hãy trồng cái cây đầu tiên của bạn!",
            fontSize = 14.sp,
            color = GardenDesign.TextSecondary.copy(alpha = 0.8f)
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

// ======================== DIALOGS ========================

@Composable
private fun PlantTreeDialog(
    currentPoints: Int,
    onDismiss: () -> Unit,
    onPlant: (TreeType) -> Unit,
    viewModel: GardenViewModel
) {
    val affordableTrees = remember(currentPoints) { viewModel.getAffordableTrees(currentPoints) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Trồng cây mới",
                fontWeight = FontWeight.Bold,
                color = GardenDesign.TextPrimary
            )
        },
        text = {
            Column {
                Text("Bạn có $currentPoints điểm.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                if (affordableTrees.isEmpty()) {
                    Text(
                        "Bạn chưa đủ điểm trồng cây nào (Min: ${TreeType.OAK.costToPlant})",
                        color = GardenDesign.StreakFire
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(affordableTrees) { type ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GardenDesign.Primary.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { onPlant(type) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = type.emoji, fontSize = 24.sp)
                                    Text(text = type.displayName, fontWeight = FontWeight.Bold)
                                    Text(text = "${type.costToPlant} điểm", fontSize = 12.sp, color = GardenDesign.TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
private fun TreeDetailsDialog(
    tree: TreeEntity,
    viewModel: GardenViewModel,
    onDismiss: () -> Unit,
    onWater: () -> Unit,
    onDelete: () -> Unit
) {
    var treeInfo by remember { mutableStateOf<com.projectapp.tempus.domain.usecase.TreeInfo?>(null) }
    
    LaunchedEffect(tree.id) {
        treeInfo = viewModel.getTreeInfo(tree.id)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tree.name, fontWeight = FontWeight.Bold) },
        text = {
            if (treeInfo == null) {
                CircularProgressIndicator()
            } else {
                val info = treeInfo!!
                Column {
                    Text("Loại: ${info.type.displayName} ${info.type.emoji}")
                    Text("Trạng thái: ${info.state.displayName}")
                    Text("Đã đầu tư: ${info.entity.investedPoints} điểm")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (info.entity.isAlive) {
                        Text("Tiến độ: ${info.progressPercent.toInt()}%")
                        LinearProgressIndicator(
                            progress = { info.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        info.pointsToNextLevel?.let {
                            Text("Cần $it điểm nữa để lớn lên", fontSize = 12.sp, color = GardenDesign.TextSecondary)
                        }
                        
                        if (info.state != TreeState.TREE) {
                            Text(
                                "Còn ${info.daysUntilDeath} ngày trước khi héo",
                                color = if (info.daysUntilDeath <= 2) GardenDesign.StreakFire else GardenDesign.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text("Cây đã chết 💀", color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onWater,
                enabled = treeInfo?.entity?.isAlive == true && treeInfo?.state != TreeState.TREE,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Icon(Icons.Outlined.WaterDrop, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tưới (10đ)")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("Chặt cây", color = Color.Red) }
                TextButton(onClick = onDismiss) { Text("Đóng") }
            }
        }
    )
}

// ======================== HELPERS ========================

private fun getTreeDrawable(type: TreeType, state: TreeState): Int {
    return when (state) {
        TreeState.SEED -> R.drawable.ic_seed
        TreeState.SPROUT -> when (type) {
            TreeType.PINE -> R.drawable.ic_pine_sprout
            TreeType.BAMBOO -> R.drawable.ic_bamboo_sprout
            TreeType.PALM -> R.drawable.ic_palm_sprout
            else -> R.drawable.ic_sprout
        }
        TreeState.SAPLING -> when (type) {
            TreeType.OAK -> R.drawable.ic_oak_sapling
            TreeType.PINE -> R.drawable.ic_pine_sapling
            TreeType.SAKURA -> R.drawable.ic_sakura_sapling
            TreeType.BAMBOO -> R.drawable.ic_bamboo_sapling
            TreeType.PALM -> R.drawable.ic_palm_sapling
            TreeType.APPLE -> R.drawable.ic_apple_sapling
        }
        TreeState.TREE -> when (type) {
            TreeType.OAK -> R.drawable.ic_oak_tree
            TreeType.PINE -> R.drawable.ic_pine_tree
            TreeType.SAKURA -> R.drawable.ic_sakura_tree
            TreeType.BAMBOO -> R.drawable.ic_bamboo_tree
            TreeType.PALM -> R.drawable.ic_palm_tree
            TreeType.APPLE -> R.drawable.ic_apple_tree
        }
        TreeState.DEAD -> R.drawable.ic_tree_dead
    }
}

private fun getTreeStateColor(state: TreeState): Color {
    return when (state) {
        TreeState.SEED -> Color(0xFF8B4513)
        TreeState.SPROUT -> Color(0xFF32CD32)
        TreeState.SAPLING -> Color(0xFF228B22)
        TreeState.TREE -> Color(0xFF006400)
        TreeState.DEAD -> Color(0xFF696969)
    }
}

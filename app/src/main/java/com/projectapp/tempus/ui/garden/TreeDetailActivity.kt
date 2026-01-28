package com.projectapp.tempus.ui.garden

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.data.RepositoryProvider
import com.projectapp.tempus.domain.model.TreeGrowthCalculator
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType
import com.projectapp.tempus.domain.usecase.PointsManager
import com.projectapp.tempus.ui.garden.compose.ProceduralTree
import com.projectapp.tempus.ui.garden.compose.ProceduralTreeSize
import com.projectapp.tempus.ui.garden.compose.WateringAnimation
import com.projectapp.tempus.ui.theme.TempusTheme
import kotlinx.coroutines.launch

/**
 * Activity hiển thị chi tiết cây với animation đung đưa
 */
class TreeDetailActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_TREE_ID = "tree_id"
        const val EXTRA_TREE_NAME = "tree_name"
        const val EXTRA_TREE_STATE = "tree_state"
        const val EXTRA_TREE_TYPE = "tree_type"
        const val EXTRA_INVESTED_POINTS = "invested_points"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_DAYS_UNTIL_DEATH = "days_until_death"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val treeId = intent.getLongExtra(EXTRA_TREE_ID, -1)
        val treeName = intent.getStringExtra(EXTRA_TREE_NAME) ?: "Tree"
        val treeStateStr = intent.getStringExtra(EXTRA_TREE_STATE) ?: "SEED"
        val treeTypeStr = intent.getStringExtra(EXTRA_TREE_TYPE) ?: "OAK"
        val investedPoints = intent.getIntExtra(EXTRA_INVESTED_POINTS, 0)
        val progress = intent.getFloatExtra(EXTRA_PROGRESS, 0f)
        val daysUntilDeath = intent.getIntExtra(EXTRA_DAYS_UNTIL_DEATH, 7)
        
        val treeState = TreeState.fromString(treeStateStr)
        val treeType = TreeType.fromString(treeTypeStr)
        
        setContent {
            TempusTheme {
                TreeDetailScreen(
                    treeId = treeId,
                    initialName = treeName,
                    initialState = treeState,
                    treeType = treeType,
                    initialInvestedPoints = investedPoints,
                    initialProgress = progress,
                    initialDaysUntilDeath = daysUntilDeath,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreeDetailScreen(
    treeId: Long,
    initialName: String,
    initialState: TreeState,
    treeType: TreeType,
    initialInvestedPoints: Int,
    initialProgress: Float,
    initialDaysUntilDeath: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Use offline-first repository
    val repository = remember { RepositoryProvider.getGamificationRepository(context) }
    val pointsManager = remember { PointsManager(repository) }
    val treeCalculator = remember { TreeGrowthCalculator() }
    
    // MUTABLE STATE - để UI update real-time
    var currentState by remember { mutableStateOf(initialState) }
    var currentInvestedPoints by remember { mutableIntStateOf(initialInvestedPoints) }
    var currentProgress by remember { mutableFloatStateOf(initialProgress) }
    var currentDaysUntilDeath by remember { mutableIntStateOf(initialDaysUntilDeath) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isWatering by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var showWateringAnimation by remember { mutableStateOf(false) }
    
    val canWater = currentState != TreeState.TREE && currentState != TreeState.DEAD
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Text("🗑️", fontSize = 32.sp) },
            title = { Text("Xóa cây?", fontWeight = FontWeight.Bold) },
            text = { 
                Text("Bạn có chắc muốn xóa \"$initialName\"?\n\nBạn sẽ mất $currentInvestedPoints điểm đã đầu tư.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isDeleting = true
                            try {
                                repository.deleteTree(treeId)
                                Toast.makeText(context, "Đã xóa $initialName", Toast.LENGTH_SHORT).show()
                                onBack()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            isDeleting = false
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                    enabled = !isDeleting
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(initialName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Procedural Tree với glow effect
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                getTreeGlowColor(currentState).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                ProceduralTree(
                    treeType = treeType,
                    growthStage = currentState,
                    treeId = treeId,
                    size = ProceduralTreeSize.XLARGE.dp
                )
            
                // Water animation overlay
                WateringAnimation(
                    isPlaying = showWateringAnimation,
                    onAnimationEnd = { showWateringAnimation = false }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // State Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = getTreeGlowColor(currentState).copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${currentState.emoji} ${currentState.displayName}",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = getTreeGlowColor(currentState)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Tiến độ phát triển",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${currentProgress.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { currentProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // WATER BUTTON - Ngay dưới thanh tiến độ
            if (canWater) {
                Button(
                    onClick = {
                        scope.launch {
                            isWatering = true
                            val newState = pointsManager.waterTree(treeId)
                            isWatering = false
                            
                            if (newState != null) {
                                currentState = newState
                                currentInvestedPoints += 10
                                currentProgress = treeCalculator.getProgressPercent(currentInvestedPoints)
                                currentDaysUntilDeath = 7
                                
                                // Trigger water animation
                                showWateringAnimation = true
                                
                                Toast.makeText(context, "💧 Đã tưới cây thành công!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "❌ Không đủ điểm để tưới cây!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34C759)
                    ),
                    enabled = !isWatering
                ) {
                    if (isWatering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            "💧 Tưới cây (10 điểm)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (currentState == TreeState.TREE) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF34C759).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "🎉 Cây đã trưởng thành!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF34C759)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Thông tin chi tiết",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    HorizontalDivider()
                    
                    InfoRow(label = "Loại cây", value = treeType.displayName)
                    InfoRow(label = "Đã đầu tư", value = "$currentInvestedPoints điểm")
                    InfoRow(label = "Trạng thái", value = currentState.displayName)
                    
                    if (canWater) {
                        val warningColor = when {
                            currentDaysUntilDeath <= 1 -> Color(0xFFFF3B30)
                            currentDaysUntilDeath <= 3 -> Color(0xFFFF9500)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        InfoRow(
                            label = "Thời gian còn lại", 
                            value = "$currentDaysUntilDeath ngày",
                            valueColor = warningColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Delete Button
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF3B30)
                )
            ) {
                Icon(Icons.Default.Delete, "Delete")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xóa cây")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

private fun getTreeGlowColor(state: TreeState): Color {
    return when (state) {
        TreeState.SEED -> Color(0xFF8B4513)
        TreeState.SPROUT -> Color(0xFF32CD32)
        TreeState.SAPLING -> Color(0xFF228B22)
        TreeState.TREE -> Color(0xFF006400)
        TreeState.DEAD -> Color(0xFF696969)
    }
}

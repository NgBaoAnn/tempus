package com.projectapp.tempus.ui.garden.compose

import androidx.compose.ui.graphics.Color
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType

/**
 * TreeConfigs - Config cho từng TreeType và GrowthStage
 * 
 * Mỗi loại cây có shape/motion rules riêng
 */

// ========== Color Palettes ==========

private object TreeColors {
    // Trunk colors
    val brownDark = Color(0xFF4A3728)
    val brownMedium = Color(0xFF6B4423)
    val brownLight = Color(0xFF8B5A2B)
    val brownGray = Color(0xFF5A4A3A)
    
    // Leaf colors
    val greenDark = Color(0xFF1B5E20)
    val greenMedium = Color(0xFF2E7D32)
    val greenLight = Color(0xFF4CAF50)
    val greenBright = Color(0xFF66BB6A)
    val greenPale = Color(0xFF81C784)
    
    // Special colors
    val pink = Color(0xFFE91E63)
    val pinkLight = Color(0xFFF48FB1)
    val bambooGreen = Color(0xFF7CB342)
    val palmGreen = Color(0xFF558B2F)
    val deadBrown = Color(0xFF5D4037)
    val deadGray = Color(0xFF757575)
    
    // Fruits
    val appleRed = Color(0xFFD32F2F)
    val appleGreen = Color(0xFF689F38)
    val coconutBrown = Color(0xFF6D4C41)  // Trái dừa khô
    val coconutGreen = Color(0xFF8BC34A)  // Trái dừa xanh
    
    // Pot
    val potTerracotta = Color(0xFFBF6030)
    val potDark = Color(0xFF8D4E2A)
    val soil = Color(0xFF3E2723)
}

// ========== TreeType Configs ==========

fun getTreeConfig(type: TreeType, state: TreeState, seed: Int): TreeRenderConfig {
    return when (type) {
        TreeType.OAK -> getOakConfig(state, seed)
        TreeType.PINE -> getPineConfig(state, seed)
        TreeType.SAKURA -> getSakuraConfig(state, seed)
        TreeType.BAMBOO -> getBambooConfig(state, seed)
        TreeType.PALM -> getPalmConfig(state, seed)
        TreeType.COCONUT -> getCoconutConfig(state, seed)
        TreeType.APPLE -> getAppleConfig(state, seed)
    }
}

// ========== OAK - Classic rounded tree with visible branches ==========

private fun getOakConfig(state: TreeState, seed: Int): TreeRenderConfig {
    return when (state) {
        TreeState.SEED -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.08f, 0.15f, 0.6f, TreeColors.brownLight),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.12f, 2, TreeColors.greenLight, stablePhaseOffset(seed, 0), LeafShape.OAK_CLOUD)
                ),
                baseColor = TreeColors.greenLight,
                stiffness = 0.3f,
                overallScale = 0.5f
            ),
            pot = null,
            showSoil = true
        )
        
        TreeState.SPROUT -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.06f, 0.35f, 0.5f, TreeColors.greenMedium),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.7f, 0.15f, 3, TreeColors.greenLight, stablePhaseOffset(seed, 0), LeafShape.OAK_CLOUD),
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.9f, 0.12f, 2, TreeColors.greenBright, stablePhaseOffset(seed, 1), LeafShape.OAK_CLOUD)
                ),
                baseColor = TreeColors.greenMedium,
                stiffness = 0.4f,
                overallScale = 0.7f
            ),
            pot = null,
            showSoil = true
        )
        
        TreeState.SAPLING -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.12f, 0.55f, 0.45f, TreeColors.brownMedium),
                branches = listOf(
                    // Visible branches like HTML demo
                    BranchConfig(0.55f, 0.28f, 0.055f, -45f, stablePhaseOffset(seed, 0), TreeColors.brownMedium),
                    BranchConfig(0.65f, 0.25f, 0.05f, 40f, stablePhaseOffset(seed, 1), TreeColors.brownMedium),
                    BranchConfig(0.75f, 0.30f, 0.055f, 5f, stablePhaseOffset(seed, 2), TreeColors.brownMedium)
                ),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.95f, 0.26f, 5, TreeColors.greenMedium, stablePhaseOffset(seed, 3), LeafShape.OAK_CLOUD),
                    LeafClusterConfig(LeafAttachment.BRANCH_0, 0.8f, 0.18f, 4, TreeColors.greenLight, stablePhaseOffset(seed, 4), LeafShape.OAK_CLOUD),
                    LeafClusterConfig(LeafAttachment.BRANCH_1, 0.8f, 0.18f, 4, TreeColors.greenLight, stablePhaseOffset(seed, 5), LeafShape.OAK_CLOUD),
                    LeafClusterConfig(LeafAttachment.BRANCH_2, 0.85f, 0.20f, 4, TreeColors.greenBright, stablePhaseOffset(seed, 6), LeafShape.OAK_CLOUD)
                ),
                baseColor = TreeColors.greenMedium,
                stiffness = 0.6f,
                overallScale = 0.88f
            ),
            pot = null,
            showSoil = true
        )
        
        TreeState.TREE -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.16f, 0.55f, 0.35f, TreeColors.brownDark),
                // More visible branches like HTML demo
                branches = listOf(
                    // Main branches - prominent
                    BranchConfig(0.50f, 0.32f, 0.06f, -50f, stablePhaseOffset(seed, 0), TreeColors.brownMedium),
                    BranchConfig(0.55f, 0.30f, 0.055f, 45f, stablePhaseOffset(seed, 1), TreeColors.brownMedium),
                    BranchConfig(0.65f, 0.35f, 0.06f, 5f, stablePhaseOffset(seed, 2), TreeColors.brownMedium),
                    BranchConfig(0.70f, 0.28f, 0.05f, -30f, stablePhaseOffset(seed, 3), TreeColors.brownMedium),
                    BranchConfig(0.78f, 0.25f, 0.045f, 35f, stablePhaseOffset(seed, 4), TreeColors.brownMedium)
                ),
                leafClusters = listOf(
                    // Crown - large clusters - LOWERED more for better coverage
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.65f, 0.38f, 6, TreeColors.greenDark, stablePhaseOffset(seed, 5), LeafShape.OAK_CLOUD),
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.55f, 0.32f, 5, TreeColors.greenMedium, stablePhaseOffset(seed, 6), LeafShape.OAK_CLOUD),
                    // Branch leaves
                    LeafClusterConfig(LeafAttachment.BRANCH_0, 0.9f, 0.22f, 5, TreeColors.greenLight, stablePhaseOffset(seed, 7), LeafShape.OAK_CLOUD),
                    LeafClusterConfig(LeafAttachment.BRANCH_1, 0.9f, 0.22f, 5, TreeColors.greenLight, stablePhaseOffset(seed, 8), LeafShape.OAK_CLOUD),
                    LeafClusterConfig(LeafAttachment.BRANCH_2, 0.88f, 0.24f, 5, TreeColors.greenMedium, stablePhaseOffset(seed, 9), LeafShape.OAK_CLOUD),
                    LeafClusterConfig(LeafAttachment.BRANCH_3, 0.85f, 0.20f, 4, TreeColors.greenBright, stablePhaseOffset(seed, 10), LeafShape.OAK_CLOUD),
                    LeafClusterConfig(LeafAttachment.BRANCH_4, 0.85f, 0.20f, 4, TreeColors.greenBright, stablePhaseOffset(seed, 11), LeafShape.OAK_CLOUD)
                ),
                baseColor = TreeColors.greenDark,
                stiffness = 0.7f,
                overallScale = 1.05f  // Slightly larger
            ),
            pot = null,
            showSoil = true
        )
        
        TreeState.DEAD -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.12f, 0.50f, 0.4f, TreeColors.deadBrown),
                branches = listOf(
                    BranchConfig(0.55f, 0.22f, 0.045f, -50f, 0f, TreeColors.deadGray),
                    BranchConfig(0.70f, 0.18f, 0.04f, 55f, 0f, TreeColors.deadGray),
                    BranchConfig(0.80f, 0.12f, 0.03f, -25f, 0f, TreeColors.deadGray)
                ),
                leafClusters = emptyList(),
                baseColor = TreeColors.deadGray,
                stiffness = 1f,
                overallScale = 0.9f
            ),
            pot = null,
            showSoil = true,
            soilColor = Color(0xFF5D4037)
        )
    }
}

// ========== PINE - Triangular conifer (LARGER SIZE) ==========

private fun getPineConfig(state: TreeState, seed: Int): TreeRenderConfig {
    return when (state) {
        TreeState.SEED, TreeState.SPROUT -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.06f, 0.20f, 0.5f, TreeColors.brownLight),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.15f, 1, TreeColors.greenDark, 0f, LeafShape.CONIFER)
                ),
                baseColor = TreeColors.greenDark,
                stiffness = 0.35f,
                // Increased scale
                overallScale = if (state == TreeState.SEED) 0.65f else 0.85f
            ),
            pot = null
        )
        
        TreeState.SAPLING -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.07f, 0.35f, 0.35f, TreeColors.brownMedium),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.22f, 1, TreeColors.greenDark, 0f, LeafShape.CONIFER)
                ),
                baseColor = TreeColors.greenDark,
                stiffness = 0.4f,
                overallScale = 1.0f  // Increased
            ),
            pot = null
        )
        
        TreeState.TREE -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.08f, 0.25f, 0.3f, TreeColors.brownDark),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.28f, 1, TreeColors.greenDark, 0f, LeafShape.CONIFER)
                ),
                baseColor = TreeColors.greenDark,
                stiffness = 0.45f,
                overallScale = 1.25f  // INCREASED SIZE (was 1f)
            ),
            pot = null
        )
        
        TreeState.DEAD -> getOakConfig(TreeState.DEAD, seed)
    }
}

// ========== BAMBOO - Segmented, flexible ==========

private fun getBambooConfig(state: TreeState, seed: Int): TreeRenderConfig {
    return when (state) {
        TreeState.SEED, TreeState.SPROUT -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.04f, 0.30f, 0.7f, TreeColors.bambooGreen, segments = 3),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.9f, 0.12f, 2, TreeColors.greenLight, stablePhaseOffset(seed, 0), LeafShape.BAMBOO)
                ),
                baseColor = TreeColors.bambooGreen,
                stiffness = 0.2f, // Very flexible
                overallScale = if (state == TreeState.SEED) 0.4f else 0.65f
            ),
            pot = null
        )
        
        TreeState.SAPLING, TreeState.TREE -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(
                    if (state == TreeState.TREE) 0.06f else 0.05f,
                    if (state == TreeState.TREE) 0.70f else 0.55f,
                    0.8f,
                    TreeColors.bambooGreen,
                    segments = if (state == TreeState.TREE) 10 else 6
                ),
                branches = emptyList(), // Bamboo doesn't have branches
                // Single large fan-shaped leaf cluster at top (like HTML demo)
                leafClusters = listOf(
                    LeafClusterConfig(
                        LeafAttachment.TRUNK,
                        1.0f,  // At the very top
                        if (state == TreeState.TREE) 0.25f else 0.18f,  // Larger size for fan leaves
                        7,  // 7 leaves like HTML demo
                        TreeColors.greenLight,
                        stablePhaseOffset(seed, 0),
                        LeafShape.BAMBOO
                    )
                ),
                baseColor = TreeColors.bambooGreen,
                stiffness = 0.25f,
                overallScale = if (state == TreeState.TREE) 1f else 0.8f
            ),
            pot = null
        )
        
        TreeState.DEAD -> getOakConfig(TreeState.DEAD, seed)
    }
}

// ========== SAKURA - Cherry blossom with complex branching ==========

private fun getSakuraConfig(state: TreeState, seed: Int): TreeRenderConfig {
    return when (state) {
        TreeState.SEED, TreeState.SPROUT -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.05f, 0.28f, 0.5f, TreeColors.brownLight),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.85f, 0.12f, 2, TreeColors.pinkLight, stablePhaseOffset(seed, 0), LeafShape.PETAL)
                ),
                baseColor = TreeColors.pinkLight,
                stiffness = 0.4f,
                overallScale = if (state == TreeState.SEED) 0.45f else 0.65f
            ),
            pot = null
        )
        
        TreeState.SAPLING -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.08f, 0.45f, 0.45f, TreeColors.brownMedium, curve = 0.03f),
                // Main branches
                branches = listOf(
                    BranchConfig(0.60f, 0.25f, 0.045f, -45f, stablePhaseOffset(seed, 0), TreeColors.brownLight),
                    BranchConfig(0.70f, 0.22f, 0.04f, 40f, stablePhaseOffset(seed, 1), TreeColors.brownLight),
                    BranchConfig(0.80f, 0.20f, 0.035f, -25f, stablePhaseOffset(seed, 2), TreeColors.brownLight)
                ),
                leafClusters = listOf(
                    // Cloud-like clusters on each branch
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.90f, 0.22f, 4, TreeColors.pink, stablePhaseOffset(seed, 3), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_0, 0.85f, 0.16f, 3, TreeColors.pinkLight, stablePhaseOffset(seed, 4), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_1, 0.85f, 0.16f, 3, TreeColors.pinkLight, stablePhaseOffset(seed, 5), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_2, 0.80f, 0.14f, 3, TreeColors.pinkLight, stablePhaseOffset(seed, 6), LeafShape.PETAL)
                ),
                baseColor = TreeColors.pink,
                stiffness = 0.45f,
                overallScale = 0.82f
            ),
            pot = null
        )
        
        TreeState.TREE -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.10f, 0.50f, 0.40f, TreeColors.brownMedium, curve = 0.04f),
                // Complex branching: Main branches + sub-branches (twigs)
                branches = listOf(
                    // Main branches (thick, prominent)
                    BranchConfig(0.50f, 0.30f, 0.055f, -50f, stablePhaseOffset(seed, 0), TreeColors.brownMedium),  // Left Low
                    BranchConfig(0.55f, 0.28f, 0.05f, 45f, stablePhaseOffset(seed, 1), TreeColors.brownMedium),   // Right Low
                    BranchConfig(0.65f, 0.32f, 0.05f, -20f, stablePhaseOffset(seed, 2), TreeColors.brownMedium),  // Left Mid
                    BranchConfig(0.70f, 0.30f, 0.045f, 25f, stablePhaseOffset(seed, 3), TreeColors.brownMedium),  // Right Mid
                    BranchConfig(0.80f, 0.25f, 0.04f, -5f, stablePhaseOffset(seed, 4), TreeColors.brownLight),    // Top Center
                    BranchConfig(0.82f, 0.22f, 0.035f, 15f, stablePhaseOffset(seed, 5), TreeColors.brownLight),   // Top Right
                    // Sub-branches (twigs - thinner, longer angles)
                    BranchConfig(0.45f, 0.18f, 0.025f, -70f, stablePhaseOffset(seed, 6), TreeColors.brownLight),  // Left Twig
                    BranchConfig(0.48f, 0.15f, 0.02f, -90f, stablePhaseOffset(seed, 7), TreeColors.brownLight),   // Far Left Twig
                    BranchConfig(0.52f, 0.18f, 0.025f, 60f, stablePhaseOffset(seed, 8), TreeColors.brownLight),   // Right Twig
                    BranchConfig(0.55f, 0.15f, 0.02f, 80f, stablePhaseOffset(seed, 9), TreeColors.brownLight)     // Far Right Twig
                ),
                // Cloud-like blossom clusters distributed across branches
                leafClusters = listOf(
                    // Main crown clusters
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.95f, 0.32f, 6, TreeColors.pink, stablePhaseOffset(seed, 10), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.88f, 0.26f, 5, TreeColors.pinkLight, stablePhaseOffset(seed, 11), LeafShape.PETAL),
                    // Branch end clusters
                    LeafClusterConfig(LeafAttachment.BRANCH_0, 0.90f, 0.20f, 4, TreeColors.pinkLight, stablePhaseOffset(seed, 12), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_1, 0.90f, 0.20f, 4, TreeColors.pinkLight, stablePhaseOffset(seed, 13), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_2, 0.88f, 0.18f, 4, TreeColors.pink, stablePhaseOffset(seed, 14), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_3, 0.88f, 0.18f, 4, TreeColors.pink, stablePhaseOffset(seed, 15), LeafShape.PETAL),
                    // Twig clusters (smaller)
                    LeafClusterConfig(LeafAttachment.BRANCH_4, 0.85f, 0.14f, 3, TreeColors.pinkLight, stablePhaseOffset(seed, 16), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_5, 0.85f, 0.14f, 3, TreeColors.pinkLight, stablePhaseOffset(seed, 17), LeafShape.PETAL),
                    // Far twig clusters (left and right side twigs)
                    LeafClusterConfig(LeafAttachment.BRANCH_6, 0.80f, 0.16f, 3, TreeColors.pink, stablePhaseOffset(seed, 18), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_7, 0.75f, 0.14f, 3, TreeColors.pinkLight, stablePhaseOffset(seed, 19), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_8, 0.80f, 0.16f, 3, TreeColors.pink, stablePhaseOffset(seed, 20), LeafShape.PETAL),
                    LeafClusterConfig(LeafAttachment.BRANCH_9, 0.75f, 0.14f, 3, TreeColors.pinkLight, stablePhaseOffset(seed, 21), LeafShape.PETAL)
                ),
                baseColor = TreeColors.pink,
                stiffness = 0.5f,
                overallScale = 1f
            ),
            pot = null
        )
        
        TreeState.DEAD -> getOakConfig(TreeState.DEAD, seed)
    }
}

// ========== PALM - Tropical fan leaves ==========

private fun getPalmConfig(state: TreeState, seed: Int): TreeRenderConfig {
    return when (state) {
        TreeState.SEED, TreeState.SPROUT -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.06f, 0.22f, 0.6f, TreeColors.brownLight),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.9f, 0.15f, 2, TreeColors.palmGreen, stablePhaseOffset(seed, 0), LeafShape.FAN_PALM)
                ),
                baseColor = TreeColors.palmGreen,
                stiffness = 0.35f,
                overallScale = if (state == TreeState.SEED) 0.4f else 0.6f
            ),
            pot = null
        )
        
        TreeState.SAPLING, TreeState.TREE -> {
            val isTree = state == TreeState.TREE
            TreeRenderConfig(
                parts = TreeParts(
                    trunk = TrunkConfig(
                        if (isTree) 0.08f else 0.06f,
                        if (isTree) 0.55f else 0.40f,
                        0.7f,
                        TreeColors.brownMedium
                    ),
                    branches = emptyList(),  // No branches - fronds are drawn by FAN_PALM
                    // Single leaf cluster at trunk top - FAN_PALM draws all fronds internally
                    leafClusters = listOf(
                        LeafClusterConfig(
                            LeafAttachment.TRUNK,
                            1.0f,  // At very top
                            if (isTree) 0.28f else 0.20f,  // Size
                            1,  // Just 1 cluster
                            TreeColors.palmGreen,
                            stablePhaseOffset(seed, 0),
                            LeafShape.FAN_PALM
                        )
                    ),
                    baseColor = TreeColors.palmGreen,
                    stiffness = 0.3f,
                    overallScale = if (isTree) 1f else 0.8f
                ),
                pot = null
            )
        }
        
        TreeState.DEAD -> getOakConfig(TreeState.DEAD, seed)
    }
}

// ========== APPLE - Fruit tree with more visible fruits ==========

private fun getAppleConfig(state: TreeState, seed: Int): TreeRenderConfig {
    val baseConfig = getOakConfig(state, seed)
    
    return if (state == TreeState.TREE) {
        // Add fruits to mature tree - positioned inside the leaf canopy
        // fruit.position 0.0-1.0: 0=bottom of canopy, 0.5=center, 1=top
        baseConfig.copy(
            parts = baseConfig.parts.copy(
                fruits = listOf(
                    // Táo trải đều trong tán lá
                    FruitConfig(LeafAttachment.TRUNK, 0.35f, 0.05f, TreeColors.appleRed, 0f),      // Dưới
                    FruitConfig(LeafAttachment.TRUNK, 0.55f, 0.048f, TreeColors.appleRed, 2.0f),   // Giữa
                    FruitConfig(LeafAttachment.TRUNK, 0.40f, 0.045f, TreeColors.appleGreen, 4.0f), // Dưới-giữa
                    FruitConfig(LeafAttachment.TRUNK, 0.60f, 0.046f, TreeColors.appleRed, 1.0f),   // Giữa-trên
                    FruitConfig(LeafAttachment.TRUNK, 0.50f, 0.044f, TreeColors.appleRed, 3.0f),   // Chính giữa
                    FruitConfig(LeafAttachment.TRUNK, 0.45f, 0.042f, TreeColors.appleGreen, 5.0f)  // Giữa-dưới
                ),
                baseColor = TreeColors.greenMedium
            )
        )
    } else {
        baseConfig
    }
}

// ========== COCONUT - Cây Dừa ==========

private fun getCoconutConfig(state: TreeState, seed: Int): TreeRenderConfig {
    return when (state) {
        TreeState.SEED, TreeState.SPROUT -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.05f, 0.20f, 0.65f, TreeColors.brownLight),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.9f, 0.12f, 2, TreeColors.palmGreen, stablePhaseOffset(seed, 0), LeafShape.LONG)
                ),
                baseColor = TreeColors.palmGreen,
                stiffness = 0.3f,
                overallScale = if (state == TreeState.SEED) 0.4f else 0.6f
            ),
            pot = null
        )
        
        TreeState.SAPLING -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.07f, 0.50f, 0.55f, TreeColors.brownMedium, curve = 0.03f),
                branches = listOf(
                    BranchConfig(0.92f, 0.30f, 0.015f, -50f, stablePhaseOffset(seed, 0), TreeColors.palmGreen),
                    BranchConfig(0.94f, 0.28f, 0.015f, 0f, stablePhaseOffset(seed, 1), TreeColors.palmGreen),
                    BranchConfig(0.92f, 0.30f, 0.015f, 50f, stablePhaseOffset(seed, 2), TreeColors.palmGreen)
                ),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.BRANCH_0, 0.9f, 0.18f, 4, TreeColors.greenLight, stablePhaseOffset(seed, 3), LeafShape.LONG),
                    LeafClusterConfig(LeafAttachment.BRANCH_1, 0.9f, 0.18f, 4, TreeColors.greenLight, stablePhaseOffset(seed, 4), LeafShape.LONG),
                    LeafClusterConfig(LeafAttachment.BRANCH_2, 0.9f, 0.18f, 4, TreeColors.greenLight, stablePhaseOffset(seed, 5), LeafShape.LONG)
                ),
                baseColor = TreeColors.palmGreen,
                stiffness = 0.35f,
                overallScale = 0.8f
            ),
            pot = null
        )
        
        TreeState.TREE -> TreeRenderConfig(
            parts = TreeParts(
                // Thân cong như hình mẫu
                trunk = TrunkConfig(0.08f, 0.50f, 0.55f, TreeColors.brownMedium, curve = 0.06f),
                // Không có branches - tất cả lá gắn trực tiếp vào đỉnh trunk
                branches = emptyList(),
                // 5 tàu lá gắn vào cùng 1 điểm (đỉnh trunk), xoè ra các hướng
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.30f, 0, TreeColors.greenLight, -70f, LeafShape.FROND),  // Trái xa
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.28f, 1, TreeColors.greenLight, -35f, LeafShape.FROND),  // Trái gần
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.26f, 2, TreeColors.greenMedium, 0f, LeafShape.FROND),   // Giữa
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.28f, 3, TreeColors.greenLight, 35f, LeafShape.FROND),   // Phải gần
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.30f, 4, TreeColors.greenLight, 70f, LeafShape.FROND)    // Phải xa
                ),
                fruits = listOf(
                    // 3 quả dừa nâu ngay dưới tàu lá
                    FruitConfig(LeafAttachment.TRUNK, 0.96f, 0.055f, TreeColors.coconutBrown, stablePhaseOffset(seed, 10)),
                    FruitConfig(LeafAttachment.TRUNK, 0.94f, 0.06f, TreeColors.coconutBrown, stablePhaseOffset(seed, 11)),
                    FruitConfig(LeafAttachment.TRUNK, 0.95f, 0.052f, TreeColors.coconutBrown, stablePhaseOffset(seed, 12))
                ),
                baseColor = TreeColors.palmGreen,
                stiffness = 0.25f,
                overallScale = 1f
            ),
            pot = null
        )
        
        TreeState.DEAD -> getOakConfig(TreeState.DEAD, seed)
    }
}

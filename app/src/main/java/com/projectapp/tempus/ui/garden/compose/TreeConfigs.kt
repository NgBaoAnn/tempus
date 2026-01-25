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

// ========== OAK - Classic rounded tree ==========

private fun getOakConfig(state: TreeState, seed: Int): TreeRenderConfig {
    return when (state) {
        TreeState.SEED -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.08f, 0.15f, 0.6f, TreeColors.brownLight),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.12f, 2, TreeColors.greenLight, stablePhaseOffset(seed, 0))
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
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.7f, 0.15f, 3, TreeColors.greenLight, stablePhaseOffset(seed, 0)),
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.9f, 0.12f, 2, TreeColors.greenBright, stablePhaseOffset(seed, 1))
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
                trunk = TrunkConfig(0.10f, 0.50f, 0.45f, TreeColors.brownMedium),
                branches = listOf(
                    BranchConfig(0.65f, 0.20f, 0.04f, -35f, stablePhaseOffset(seed, 0), TreeColors.brownMedium),
                    BranchConfig(0.75f, 0.18f, 0.035f, 40f, stablePhaseOffset(seed, 1), TreeColors.brownMedium)
                ),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.95f, 0.22f, 4, TreeColors.greenMedium, stablePhaseOffset(seed, 2)),
                    LeafClusterConfig(LeafAttachment.BRANCH_0, 0.8f, 0.16f, 3, TreeColors.greenLight, stablePhaseOffset(seed, 3)),
                    LeafClusterConfig(LeafAttachment.BRANCH_1, 0.8f, 0.16f, 3, TreeColors.greenLight, stablePhaseOffset(seed, 4))
                ),
                baseColor = TreeColors.greenMedium,
                stiffness = 0.6f,
                overallScale = 0.85f
            ),
            pot = null,
            showSoil = true
        )
        
        TreeState.TREE -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.14f, 0.55f, 0.35f, TreeColors.brownDark),
                branches = listOf(
                    BranchConfig(0.55f, 0.28f, 0.05f, -45f, stablePhaseOffset(seed, 0), TreeColors.brownMedium),
                    BranchConfig(0.65f, 0.25f, 0.045f, 50f, stablePhaseOffset(seed, 1), TreeColors.brownMedium),
                    BranchConfig(0.75f, 0.22f, 0.04f, -30f, stablePhaseOffset(seed, 2), TreeColors.brownMedium),
                    BranchConfig(0.85f, 0.20f, 0.035f, 35f, stablePhaseOffset(seed, 3), TreeColors.brownMedium)
                ),
                leafClusters = listOf(
                    // Crown - cụm lá lớn ở đỉnh
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.95f, 0.35f, 5, TreeColors.greenDark, stablePhaseOffset(seed, 4)),
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.85f, 0.28f, 4, TreeColors.greenMedium, stablePhaseOffset(seed, 5)),
                    // Branch leaves
                    LeafClusterConfig(LeafAttachment.BRANCH_0, 0.9f, 0.20f, 4, TreeColors.greenLight, stablePhaseOffset(seed, 6)),
                    LeafClusterConfig(LeafAttachment.BRANCH_1, 0.9f, 0.20f, 4, TreeColors.greenLight, stablePhaseOffset(seed, 7)),
                    LeafClusterConfig(LeafAttachment.BRANCH_2, 0.85f, 0.18f, 3, TreeColors.greenBright, stablePhaseOffset(seed, 8)),
                    LeafClusterConfig(LeafAttachment.BRANCH_3, 0.85f, 0.18f, 3, TreeColors.greenBright, stablePhaseOffset(seed, 9))
                ),
                baseColor = TreeColors.greenDark,
                stiffness = 0.7f,
                overallScale = 1f
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
                leafClusters = emptyList(), // No leaves
                baseColor = TreeColors.deadGray,
                stiffness = 1f, // Stiff, không sway
                overallScale = 0.9f
            ),
            pot = null,
            showSoil = true,
            soilColor = Color(0xFF5D4037)
        )
    }
}

// ========== PINE - Triangular conifer ==========

private fun getPineConfig(state: TreeState, seed: Int): TreeRenderConfig {
    return when (state) {
        TreeState.SEED, TreeState.SPROUT -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.06f, 0.20f, 0.5f, TreeColors.brownLight),
                branches = emptyList(),
                leafClusters = listOf(
                    // Dùng CONIFER để vẽ tán thông tam giác
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.15f, 1, TreeColors.greenDark, 0f, LeafShape.CONIFER)
                ),
                baseColor = TreeColors.greenDark,
                stiffness = 0.35f,
                overallScale = if (state == TreeState.SEED) 0.5f else 0.7f
            ),
            pot = null
        )
        
        TreeState.SAPLING -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.07f, 0.35f, 0.35f, TreeColors.brownMedium),
                branches = emptyList(),
                leafClusters = listOf(
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.20f, 1, TreeColors.greenDark, 0f, LeafShape.CONIFER)
                ),
                baseColor = TreeColors.greenDark,
                stiffness = 0.4f,
                overallScale = 0.85f
            ),
            pot = null
        )
        
        TreeState.TREE -> TreeRenderConfig(
            parts = TreeParts(
                trunk = TrunkConfig(0.08f, 0.25f, 0.3f, TreeColors.brownDark),
                branches = emptyList(),
                leafClusters = listOf(
                    // 1 cluster với CONIFER shape - sẽ vẽ 4 tầng tam giác
                    LeafClusterConfig(LeafAttachment.TRUNK, 1f, 0.25f, 1, TreeColors.greenDark, 0f, LeafShape.CONIFER)
                ),
                baseColor = TreeColors.greenDark,
                stiffness = 0.45f,
                overallScale = 1f
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
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.9f, 0.12f, 2, TreeColors.greenLight, stablePhaseOffset(seed, 0), LeafShape.LONG)
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
                    segments = if (state == TreeState.TREE) 6 else 4
                ),
                branches = emptyList(), // Bamboo doesn't have branches
                leafClusters = (0 until (if (state == TreeState.TREE) 5 else 3)).map { i ->
                    LeafClusterConfig(
                        LeafAttachment.TRUNK,
                        0.5f + i * 0.12f,
                        0.15f,
                        3,
                        TreeColors.greenLight,
                        stablePhaseOffset(seed, i),
                        LeafShape.LONG
                    )
                },
                baseColor = TreeColors.bambooGreen,
                stiffness = 0.25f,
                overallScale = if (state == TreeState.TREE) 1f else 0.8f
            ),
            pot = null
        )
        
        TreeState.DEAD -> getOakConfig(TreeState.DEAD, seed)
    }
}

// ========== SAKURA - Cherry blossom ==========

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
        
        TreeState.SAPLING, TreeState.TREE -> {
            val isTree = state == TreeState.TREE
            TreeRenderConfig(
                parts = TreeParts(
                    trunk = TrunkConfig(
                        if (isTree) 0.11f else 0.08f,
                        if (isTree) 0.50f else 0.42f,
                        0.4f,
                        TreeColors.brownMedium,
                        curve = 0.05f // Slight curve
                    ),
                    branches = listOf(
                        BranchConfig(0.55f, 0.22f, 0.04f, -40f, stablePhaseOffset(seed, 0), TreeColors.brownLight),
                        BranchConfig(0.65f, 0.20f, 0.035f, 45f, stablePhaseOffset(seed, 1), TreeColors.brownLight),
                        BranchConfig(0.75f, 0.18f, 0.03f, -35f, stablePhaseOffset(seed, 2), TreeColors.brownLight)
                    ).take(if (isTree) 3 else 2),
                    leafClusters = listOf(
                        LeafClusterConfig(LeafAttachment.TRUNK, 0.9f, if (isTree) 0.30f else 0.22f, 5, TreeColors.pink, stablePhaseOffset(seed, 3), LeafShape.PETAL),
                        LeafClusterConfig(LeafAttachment.BRANCH_0, 0.85f, 0.18f, 4, TreeColors.pinkLight, stablePhaseOffset(seed, 4), LeafShape.PETAL),
                        LeafClusterConfig(LeafAttachment.BRANCH_1, 0.85f, 0.18f, 4, TreeColors.pinkLight, stablePhaseOffset(seed, 5), LeafShape.PETAL)
                    ),
                    baseColor = TreeColors.pink,
                    stiffness = 0.5f,
                    overallScale = if (isTree) 1f else 0.82f
                ),
                pot = null
            )
        }
        
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
                    LeafClusterConfig(LeafAttachment.TRUNK, 0.9f, 0.15f, 2, TreeColors.palmGreen, stablePhaseOffset(seed, 0), LeafShape.LONG)
                ),
                baseColor = TreeColors.palmGreen,
                stiffness = 0.35f,
                overallScale = if (state == TreeState.SEED) 0.4f else 0.6f
            ),
            pot = null
        )
        
        TreeState.SAPLING, TreeState.TREE -> {
            val isTree = state == TreeState.TREE
            val frondCount = if (isTree) 6 else 4
            TreeRenderConfig(
                parts = TreeParts(
                    trunk = TrunkConfig(
                        if (isTree) 0.10f else 0.07f,
                        if (isTree) 0.60f else 0.45f,
                        0.7f,
                        TreeColors.brownMedium
                    ),
                    branches = (0 until frondCount).map { i ->
                        val angle = -60f + (120f / (frondCount - 1)) * i
                        BranchConfig(0.95f, 0.35f, 0.02f, angle, stablePhaseOffset(seed, i), TreeColors.palmGreen)
                    },
                    leafClusters = (0 until frondCount).map { i ->
                        LeafClusterConfig(
                            LeafAttachment.entries.getOrElse(i + 1) { LeafAttachment.BRANCH_0 },
                            0.9f,
                            0.20f,
                            4,
                            TreeColors.greenLight,
                            stablePhaseOffset(seed, frondCount + i),
                            LeafShape.LONG
                        )
                    },
                    baseColor = TreeColors.palmGreen,
                    stiffness = 0.4f,
                    overallScale = if (isTree) 1f else 0.8f
                ),
                pot = null
            )
        }
        
        TreeState.DEAD -> getOakConfig(TreeState.DEAD, seed)
    }
}

// ========== APPLE - Fruit tree ==========

private fun getAppleConfig(state: TreeState, seed: Int): TreeRenderConfig {
    val baseConfig = getOakConfig(state, seed)
    
    return if (state == TreeState.TREE) {
        // Add fruits to mature tree
        baseConfig.copy(
            parts = baseConfig.parts.copy(
                fruits = listOf(
                    FruitConfig(LeafAttachment.BRANCH_0, 0.7f, 0.08f, TreeColors.appleRed, stablePhaseOffset(seed, 20)),
                    FruitConfig(LeafAttachment.BRANCH_1, 0.75f, 0.07f, TreeColors.appleRed, stablePhaseOffset(seed, 21)),
                    FruitConfig(LeafAttachment.BRANCH_2, 0.65f, 0.075f, TreeColors.appleGreen, stablePhaseOffset(seed, 22)),
                    FruitConfig(LeafAttachment.TRUNK, 0.80f, 0.065f, TreeColors.appleRed, stablePhaseOffset(seed, 23))
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

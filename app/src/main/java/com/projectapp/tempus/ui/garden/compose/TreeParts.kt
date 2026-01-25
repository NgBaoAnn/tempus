package com.projectapp.tempus.ui.garden.compose

import androidx.compose.ui.graphics.Color

/**
 * TreeParts - Data classes cho hierarchical tree structure
 * 
 * Hierarchy: TreeParts → Trunk → Branches → LeafClusters
 */

/**
 * Config cho thân cây
 */
data class TrunkConfig(
    val width: Float,           // Width ở gốc (dp ratio, 0..1)
    val height: Float,          // Height (dp ratio, 0..1)  
    val taperRatio: Float,      // Tỷ lệ thon ở ngọn (0.3 = ngọn = 30% gốc)
    val color: Color,
    val curve: Float = 0f,      // Độ cong (±, 0 = thẳng)
    val segments: Int = 1       // Số đoạn (BAMBOO dùng nhiều)
)

/**
 * Config cho cành
 */
data class BranchConfig(
    val attachHeight: Float,    // Vị trí attach trên trunk (0..1, 0=gốc, 1=ngọn)
    val length: Float,          // Độ dài cành (dp ratio)
    val width: Float,           // Độ dày cành
    val angle: Float,           // Góc so với trunk (degrees, + = phải, - = trái)
    val phaseOffset: Float,     // Phase offset cho animation
    val color: Color,
    val hasSubBranches: Boolean = false,
    val subBranchCount: Int = 0
)

/**
 * Config cho cụm lá
 */
data class LeafClusterConfig(
    val attachTo: LeafAttachment,  // Attach vào đâu
    val position: Float,           // Vị trí trên branch (0..1)
    val size: Float,               // Kích thước cluster
    val density: Int,              // Số lá trong cluster (1-5)
    val color: Color,
    val phaseOffset: Float,        // Animation phase
    val shape: LeafShape = LeafShape.ROUND
)

enum class LeafAttachment {
    TRUNK,      // Attach trực tiếp vào trunk
    BRANCH_0,   // Attach vào branch index 0
    BRANCH_1,
    BRANCH_2,
    BRANCH_3,
    BRANCH_4,
    BRANCH_5
}

enum class LeafShape {
    ROUND,      // Tròn (default, OAK, APPLE)
    NEEDLE,     // Kim (PINE cũ)
    LONG,       // Dài (BAMBOO, PALM)
    PETAL,      // Cánh hoa (SAKURA)
    FROND,      // Tàu lá dừa (COCONUT)
    CONIFER     // Tán thông hình tam giác với viền nhọn (PINE mới)
}

/**
 * Fruit/decoration config
 */
data class FruitConfig(
    val attachTo: LeafAttachment,
    val position: Float,
    val size: Float,
    val color: Color,
    val phaseOffset: Float
)

/**
 * Full tree parts definition
 */
data class TreeParts(
    val trunk: TrunkConfig,
    val branches: List<BranchConfig>,
    val leafClusters: List<LeafClusterConfig>,
    val fruits: List<FruitConfig> = emptyList(),
    
    // Meta
    val baseColor: Color,         // Màu chủ đạo cho background
    val stiffness: Float = 0.5f,  // 0=rất mềm (bamboo), 1=cứng (oak)
    val overallScale: Float = 1f
)

/**
 * Pot config (optional, một số stage có chậu)
 */
data class PotConfig(
    val width: Float,
    val height: Float,
    val color: Color,
    val rimHeight: Float = 0.1f
)

/**
 * Complete tree render config
 */
data class TreeRenderConfig(
    val parts: TreeParts,
    val pot: PotConfig? = null,
    val showSoil: Boolean = true,
    val soilColor: Color = Color(0xFF3D2817)
)

/**
 * Animation state for stage transitions
 */
data class TreeAnimationState(
    val trunkGrowth: Float = 1f,      // 0..1 animate trunk height
    val branchGrowth: Float = 1f,     // 0..1 animate branches emerging
    val leafOpacity: Float = 1f,      // 0..1 fade in leaves
    val leafScale: Float = 1f,        // Scale with overshoot
    val fruitOpacity: Float = 1f      // 0..1 fade in fruits
)

package com.projectapp.tempus.ui.garden.compose

import androidx.compose.ui.graphics.Color


data class TrunkConfig(
    val width: Float,           
    val height: Float,          
    val taperRatio: Float,      
    val color: Color,
    val curve: Float = 0f,      
    val segments: Int = 1       
)


data class BranchConfig(
    val attachHeight: Float,    
    val length: Float,          
    val width: Float,           
    val angle: Float,           
    val phaseOffset: Float,     
    val color: Color,
    val hasSubBranches: Boolean = false,
    val subBranchCount: Int = 0
)


data class LeafClusterConfig(
    val attachTo: LeafAttachment,  
    val position: Float,           
    val size: Float,               
    val density: Int,              
    val color: Color,
    val phaseOffset: Float,        
    val shape: LeafShape = LeafShape.ROUND
)

enum class LeafAttachment {
    TRUNK,      
    BRANCH_0,   
    BRANCH_1,
    BRANCH_2,
    BRANCH_3,
    BRANCH_4,
    BRANCH_5,
    BRANCH_6,
    BRANCH_7,
    BRANCH_8,
    BRANCH_9
}

enum class LeafShape {
    ROUND,      
    NEEDLE,     
    LONG,       
    PETAL,      
    FROND,      
    CONIFER,    
    BAMBOO,     
    FAN_PALM,   
    OAK_CLOUD   
}


data class FruitConfig(
    val attachTo: LeafAttachment,
    val position: Float,
    val size: Float,
    val color: Color,
    val phaseOffset: Float
)


data class TreeParts(
    val trunk: TrunkConfig,
    val branches: List<BranchConfig>,
    val leafClusters: List<LeafClusterConfig>,
    val fruits: List<FruitConfig> = emptyList(),
    
    
    val baseColor: Color,         
    val stiffness: Float = 0.5f,  
    val overallScale: Float = 1f
)


data class PotConfig(
    val width: Float,
    val height: Float,
    val color: Color,
    val rimHeight: Float = 0.1f
)


data class TreeRenderConfig(
    val parts: TreeParts,
    val pot: PotConfig? = null,
    val showSoil: Boolean = true,
    val soilColor: Color = Color(0xFF3D2817)
)


data class TreeAnimationState(
    val trunkGrowth: Float = 1f,      
    val branchGrowth: Float = 1f,     
    val leafOpacity: Float = 1f,      
    val leafScale: Float = 1f,        
    val fruitOpacity: Float = 1f      
)

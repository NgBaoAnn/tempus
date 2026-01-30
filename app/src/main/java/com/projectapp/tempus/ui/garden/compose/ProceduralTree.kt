package com.projectapp.tempus.ui.garden.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType
import kotlin.math.cos
import kotlin.math.sin


import com.projectapp.tempus.ui.garden.compose.drawing.lighten
import com.projectapp.tempus.ui.garden.compose.drawing.darken
import com.projectapp.tempus.ui.garden.compose.drawing.ProceduralTreeSize
import com.projectapp.tempus.ui.garden.compose.drawing.stableRandom
import com.projectapp.tempus.ui.garden.compose.drawing.stablePhaseOffset
import com.projectapp.tempus.ui.garden.compose.drawing.drawOrganicBranch
import com.projectapp.tempus.ui.garden.compose.drawing.drawOrganicTrunk
import com.projectapp.tempus.ui.garden.compose.drawing.drawPalmTrunk
import com.projectapp.tempus.ui.garden.compose.drawing.drawGroundShadow
import com.projectapp.tempus.ui.garden.compose.drawing.drawCanopyBase
import com.projectapp.tempus.ui.garden.compose.drawing.drawLeafShadow
import com.projectapp.tempus.ui.garden.compose.drawing.generateTightLeafPositions
import com.projectapp.tempus.ui.garden.compose.drawing.drawIllustrationPot
import com.projectapp.tempus.ui.garden.compose.drawing.drawSoilMound
import com.projectapp.tempus.ui.garden.compose.drawing.drawIllustrationLeaf
import com.projectapp.tempus.ui.garden.compose.drawing.drawIllustrationFruit
import com.projectapp.tempus.ui.garden.compose.drawing.darken
import com.projectapp.tempus.ui.garden.compose.drawing.lighten


import com.projectapp.tempus.ui.garden.compose.trees.drawSakuraBlossomCluster
import com.projectapp.tempus.ui.garden.compose.trees.drawFallenPetal
import com.projectapp.tempus.ui.garden.compose.trees.drawConiferTree
import com.projectapp.tempus.ui.garden.compose.trees.drawCoconutFrond
import com.projectapp.tempus.ui.garden.compose.trees.drawFanPalmLeaf
import com.projectapp.tempus.ui.garden.compose.trees.drawOakCanopy
import com.projectapp.tempus.ui.garden.compose.trees.drawApples
import com.projectapp.tempus.ui.garden.compose.trees.drawBambooCulms
import com.projectapp.tempus.ui.garden.compose.trees.drawBambooTopLeaves
import com.projectapp.tempus.ui.garden.compose.trees.drawBambooSingleStalk


@Composable
fun ProceduralTree(
    treeType: TreeType,
    growthStage: TreeState,
    modifier: Modifier = Modifier,
    treeId: Long? = null,
    size: Dp = 150.dp
) {
    
    val seed = remember(treeType, treeId) {
        (treeId?.toInt() ?: treeType.name.hashCode()) and 0x7FFFFFFF
    }
    
    
    val windState by rememberWindState(seed)
    
    
    val currentConfig = remember(treeType, growthStage, seed) {
        getTreeConfig(treeType, growthStage, seed)
    }
    
    
    val transitionState = updateTransition(targetState = growthStage, label = "stageTransition")
    
    val trunkGrowth by transitionState.animateFloat(
        transitionSpec = { tween(800, easing = FastOutSlowInEasing) },
        label = "trunkGrowth"
    ) { stage ->
        when (stage) {
            TreeState.SEED -> 0.5f
            TreeState.SPROUT -> 0.7f
            TreeState.SAPLING -> 0.85f
            TreeState.TREE -> 1f
            TreeState.DEAD -> 0.9f
        }
    }
    
    val branchGrowth by transitionState.animateFloat(
        transitionSpec = { tween(600, delayMillis = 200, easing = FastOutSlowInEasing) },
        label = "branchGrowth"
    ) { stage ->
        when (stage) {
            TreeState.SEED, TreeState.SPROUT -> 0f
            TreeState.SAPLING -> 0.8f
            TreeState.TREE -> 1f
            TreeState.DEAD -> 0.7f
        }
    }
    
    val leafOpacity by transitionState.animateFloat(
        transitionSpec = { tween(500, delayMillis = 400, easing = LinearEasing) },
        label = "leafOpacity"
    ) { stage ->
        when (stage) {
            TreeState.DEAD -> 0f
            else -> 1f
        }
    }
    
    val leafScale by transitionState.animateFloat(
        transitionSpec = { 
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "leafScale"
    ) { stage ->
        when (stage) {
            TreeState.SEED -> 0.6f
            TreeState.SPROUT -> 0.8f
            TreeState.SAPLING -> 0.9f
            TreeState.TREE -> 1f
            TreeState.DEAD -> 0f
        }
    }
    
    val animState = TreeAnimationState(
        trunkGrowth = trunkGrowth,
        branchGrowth = branchGrowth,
        leafOpacity = leafOpacity,
        leafScale = leafScale,
        fruitOpacity = if (growthStage == TreeState.TREE) 1f else 0f
    )
    
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        val centerX = canvasWidth / 2
        val baseY = canvasHeight * 0.92f
        
        val config = currentConfig
        val parts = config.parts
        val scale = parts.overallScale
        
        
        val potHeight = (config.pot?.height ?: 0f) * canvasHeight
        val trunkBaseY = baseY - potHeight * 0.35f
        
        
        val trunkRotation = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
            windState.getRotation("trunk", 0f, 0.3f) * (1f - parts.stiffness)
        } else 0f
        
        
        drawGroundShadow(centerX, baseY, canvasWidth * 0.35f * scale)
        
        
        config.pot?.let { pot ->
            drawIllustrationPot(pot, centerX, baseY, canvasWidth, canvasHeight)
        }
        
        
        val trunkHeight = canvasHeight * parts.trunk.height * animState.trunkGrowth * scale
        val trunkTop = Offset(centerX, trunkBaseY - trunkHeight)
        
        
        val branchEnds = mutableMapOf<LeafAttachment, Offset>()
        branchEnds[LeafAttachment.TRUNK] = trunkTop
        
        parts.branches.forEachIndexed { index, branch ->
            if (animState.branchGrowth > 0f) {
                val branchRotation = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
                    windState.getRotation("branch", branch.phaseOffset, 0.5f) * (1f - parts.stiffness * 0.5f)
                } else 0f
                
                val branchEnd = drawOrganicBranch(
                    branch = branch,
                    trunkTop = trunkTop,
                    trunkBaseY = trunkBaseY,
                    canvasHeight = canvasHeight,
                    parentRotation = trunkRotation,
                    selfRotation = branchRotation,
                    growthFactor = animState.branchGrowth,
                    scale = scale
                )
                
                val attachment = LeafAttachment.entries.getOrElse(index + 1) { LeafAttachment.BRANCH_0 }
                branchEnds[attachment] = branchEnd
            }
        }
        
        
        val hasBambooLeaves = parts.leafClusters.any { it.shape == LeafShape.BAMBOO }
        
        if (!hasBambooLeaves) {
            
            val hasPalmLeaves = parts.leafClusters.any { it.shape == LeafShape.FAN_PALM }
            
            if (hasPalmLeaves) {
                 
                 val palmTrunkTop = drawPalmTrunk(
                    trunk = parts.trunk,
                    centerX = centerX,
                    baseY = trunkBaseY,
                    canvasHeight = canvasHeight,
                    rotation = trunkRotation,
                    growthFactor = animState.trunkGrowth,
                    scale = scale,
                    seed = seed
                )
                
                branchEnds[LeafAttachment.TRUNK] = palmTrunkTop
            } else {
                
                drawOrganicTrunk(
                    trunk = parts.trunk,
                    centerX = centerX,
                    baseY = trunkBaseY,
                    canvasHeight = canvasHeight,
                    rotation = trunkRotation,
                    growthFactor = animState.trunkGrowth,
                    scale = scale,
                    seed = seed
                )
            }
        }
        
        
        if (config.showSoil) {
            drawSoilMound(config.soilColor, centerX, trunkBaseY + canvasHeight * 0.01f, canvasWidth * 0.18f * scale, canvasHeight * 0.025f)
        }
        
        
        val leafClusterCount = when (growthStage) {
            TreeState.SEED -> 4
            TreeState.SPROUT -> 8
            TreeState.SAPLING -> 15
            TreeState.TREE -> 25
            TreeState.DEAD -> 0
        }
        
        if (animState.leafOpacity > 0f && leafClusterCount > 0) {
            
            val hasFrondLeaves = parts.leafClusters.any { it.shape == LeafShape.FROND }
            val hasConiferLeaves = parts.leafClusters.any { it.shape == LeafShape.CONIFER }
            
            if (hasFrondLeaves) {
                
                
                parts.fruits.forEach { fruit ->
                    if (animState.fruitOpacity > 0f) {
                        val attachPoint = branchEnds[fruit.attachTo] ?: trunkTop
                        val fruitSway = windState.getRotation("branch", fruit.phaseOffset, 0.4f) * 0.5f
                        
                        drawIllustrationFruit(
                            fruit = fruit,
                            attachPoint = attachPoint,
                            canvasHeight = canvasHeight,
                            sway = fruitSway,
                            opacity = animState.fruitOpacity,
                            scale = scale
                        )
                    }
                }
                
                
                parts.leafClusters.forEach { cluster ->
                    val spreadAngle = cluster.phaseOffset
                    val frondSize = canvasHeight * cluster.size * scale * animState.leafScale
                    
                    val windSway = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
                        windState.getRotation("branch", cluster.density.toFloat(), 0.6f) * (1f - parts.stiffness)
                    } else 0f
                    
                    drawCoconutFrond(
                        origin = trunkTop,
                        spreadAngle = spreadAngle + windSway,
                        frondLength = frondSize * 2.5f,
                        frondWidth = frondSize * 0.7f,
                        baseColor = cluster.color,
                        opacity = animState.leafOpacity,
                        seed = seed + cluster.density
                    )
                }
            } else if (hasConiferLeaves) {
                
                val tierCount = when (growthStage) {
                    TreeState.SEED -> 1
                    TreeState.SPROUT -> 2
                    TreeState.SAPLING -> 3
                    TreeState.TREE -> 4
                    TreeState.DEAD -> 0
                }
                
                
                val coniferSway = trunkRotation * 2f
                
                drawConiferTree(
                    trunkTop = trunkTop,
                    canvasHeight = canvasHeight,
                    tierCount = tierCount,
                    baseColor = parts.baseColor,
                    opacity = animState.leafOpacity,
                    scale = scale * animState.leafScale,
                    sway = coniferSway,
                    seed = seed
                )
            } else if (parts.leafClusters.any { it.shape == LeafShape.PETAL }) {
                
                
                parts.leafClusters.forEach { cluster ->
                    val attachPoint = branchEnds[cluster.attachTo] ?: trunkTop
                    val clusterSize = canvasHeight * cluster.size * scale * animState.leafScale
                    
                    val windSway = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
                        windState.getRotation("leaf", cluster.phaseOffset, 0.6f) * (1f - parts.stiffness * 0.5f)
                    } else 0f
                    
                    
                    drawSakuraBlossomCluster(
                        center = Offset(attachPoint.x, attachPoint.y - clusterSize * 0.3f),
                        radius = clusterSize,
                        color = cluster.color,
                        opacity = animState.leafOpacity,
                        sway = windSway,
                        seed = seed + cluster.density
                    )
                }
                
                
                if (growthStage == TreeState.TREE) {
                    for (i in 0 until 5) {
                        val petalX = trunkTop.x + (stableRandom(seed, i + 100) - 0.5f) * canvasHeight * 0.4f
                        val petalY = trunkBaseY - canvasHeight * 0.05f - stableRandom(seed, i + 200) * canvasHeight * 0.1f
                        val petalSway = windState.getRotation("leaf", stablePhaseOffset(seed, i + 50), 0.8f)
                        
                        drawFallenPetal(
                            center = Offset(petalX, petalY),
                            size = canvasHeight * 0.02f * scale,
                            rotation = petalSway * 30f + stableRandom(seed, i + 300) * 360f,
                            color = parts.baseColor.lighten(0.2f),
                            opacity = animState.leafOpacity * 0.6f
                        )
                    }
                }
            } else if (parts.leafClusters.any { it.shape == LeafShape.OAK_CLOUD }) {
                
                
                val canopyRadius = canvasHeight * when (growthStage) {
                    TreeState.SEED -> 0.10f
                    TreeState.SPROUT -> 0.15f
                    TreeState.SAPLING -> 0.20f
                    TreeState.TREE -> 0.28f
                    TreeState.DEAD -> 0f
                } * scale * animState.leafScale
                
                val windSway = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
                    windState.getRotation("leaf", stablePhaseOffset(seed, 0), 0.5f) * (1f - parts.stiffness * 0.5f)
                } else 0f
                
                
                drawOakCanopy(
                    center = Offset(trunkTop.x, trunkTop.y + canopyRadius * 0.1f),
                    radiusX = canopyRadius * 1.2f,
                    radiusY = canopyRadius * 0.95f,
                    baseColor = parts.baseColor,
                    opacity = animState.leafOpacity,
                    sway = windSway,
                    seed = seed
                )
            } else if (parts.leafClusters.any { it.shape == LeafShape.BAMBOO }) {
                
                
                val stalkConfigs = listOf(
                    Triple(-0.08f, 1.0f, 0),    
                    Triple(0f, 1.15f, 1),       
                    Triple(0.08f, 0.85f, 2)     
                )
                
                val baseHeight = canvasHeight * parts.trunk.height * animState.trunkGrowth * scale
                val baseWidth = canvasHeight * parts.trunk.width * scale * 1.2f
                
                
                val sharedPhase = stablePhaseOffset(seed, 0)
                val baseSway = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
                    windState.getRotation("branch", sharedPhase, 0.7f) * (1f - parts.stiffness) * 1.5f
                } else 0f
                
                stalkConfigs.forEach { (offsetRatio, heightRatio, seedOffset) ->
                    val stalkX = centerX + canvasWidth * offsetRatio
                    val stalkHeight = baseHeight * heightRatio
                    val stalkWidth = baseWidth * (0.9f + stableRandom(seed, seedOffset) * 0.2f)
                    
                    
                    val stalkSway = baseSway * (0.9f + seedOffset * 0.05f)
                    
                    
                    val stalkTop = drawBambooSingleStalk(
                        centerX = stalkX,
                        baseY = trunkBaseY,
                        width = stalkWidth,
                        height = stalkHeight,
                        segmentCount = parts.trunk.segments,
                        baseColor = parts.trunk.color,
                        opacity = 1f,
                        sway = stalkSway,
                        seed = seed + seedOffset
                    )
                    
                    
                    val leafSize = canvasHeight * 0.15f * scale * animState.leafScale * (0.7f + heightRatio * 0.3f)
                    
                    
                    val leafColor = when (seedOffset) {
                        0 -> parts.baseColor.darken(0.1f)    
                        1 -> parts.baseColor                  
                        else -> parts.baseColor.lighten(0.15f) 
                    }
                    
                    drawBambooTopLeaves(
                        origin = stalkTop,
                        size = leafSize,
                        baseColor = leafColor,
                        opacity = animState.leafOpacity,
                        sway = stalkSway,
                        seed = seed + seedOffset * 100
                    )
                }
            } else if (parts.leafClusters.any { it.shape == LeafShape.FAN_PALM }) {
                
                val cluster = parts.leafClusters.first { it.shape == LeafShape.FAN_PALM }
                val crownSize = canvasHeight * cluster.size * scale * animState.leafScale
                
                val windSway = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
                    windState.getRotation("leaf", cluster.phaseOffset, 0.5f) * (1f - parts.stiffness * 0.5f)
                } else 0f
                
                
                val palmLeafCenter = branchEnds[LeafAttachment.TRUNK] ?: trunkTop
                
                
                drawIllustrationLeaf(
                    center = palmLeafCenter,
                    size = crownSize,
                    rotation = windSway,
                    baseColor = cluster.color,
                    opacity = animState.leafOpacity,
                    shape = LeafShape.FAN_PALM,
                    seed = seed
                )
            } else {
                
                val canopyRadius = canvasHeight * when (growthStage) {
                    TreeState.SEED -> 0.08f
                    TreeState.SPROUT -> 0.12f
                    TreeState.SAPLING -> 0.18f
                    TreeState.TREE -> 0.25f
                    TreeState.DEAD -> 0f
                } * scale * animState.leafScale
                
                
                val canopyYOffset = canopyRadius * 0.15f
                
                
                drawCanopyBase(
                    center = Offset(trunkTop.x, trunkTop.y + canopyYOffset),
                    radiusX = canopyRadius * 1.1f,
                    radiusY = canopyRadius * 0.9f,
                    color = parts.baseColor.darken(0.1f),
                    opacity = animState.leafOpacity * 0.7f
                )
                
                
                val leafPositions = generateTightLeafPositions(
                    center = Offset(trunkTop.x, trunkTop.y + canopyYOffset),
                    radiusX = canopyRadius,
                    radiusY = canopyRadius * 0.75f,
                    count = leafClusterCount,
                    seed = seed
                )
                
                
                leafPositions.forEach { pos ->
                    drawLeafShadow(pos, canvasHeight * 0.025f * scale * animState.leafScale)
                }
                
                
                leafPositions.forEachIndexed { index, pos ->
                    val leafRotation = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
                        windState.getRotation("leaf", stablePhaseOffset(seed, index), 0.7f + index * 0.02f) * 
                        (1f - parts.stiffness * 0.3f) + windState.getMicroJitter(index)
                    } else 0f
                    
                    drawIllustrationLeaf(
                        center = pos,
                        size = canvasHeight * (0.06f + stableRandom(seed, index) * 0.03f) * scale * animState.leafScale,
                        rotation = leafRotation,
                        baseColor = parts.baseColor,
                        opacity = animState.leafOpacity * (0.7f + stableRandom(seed, index + 100) * 0.3f),
                        shape = parts.leafClusters.firstOrNull()?.shape ?: LeafShape.ROUND,
                        seed = seed + index
                    )
                }
            }
        }
        
        
        val hasFrondLeaves = parts.leafClusters.any { it.shape == LeafShape.FROND }
        if (!hasFrondLeaves) {
            
            val canopyRadius = canvasHeight * when (growthStage) {
                TreeState.SEED -> 0.08f
                TreeState.SPROUT -> 0.12f
                TreeState.SAPLING -> 0.18f
                TreeState.TREE -> 0.25f
                TreeState.DEAD -> 0f
            } * scale * animState.leafScale
            
            
            val canopyYOffset = canopyRadius * 0.1f
            val canopyCenter = Offset(trunkTop.x, trunkTop.y + canopyYOffset)
            
            
            val canopySway = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
                windState.getRotation("leaf", stablePhaseOffset(seed, 0), 0.5f) * (1f - parts.stiffness * 0.5f)
            } else 0f
            
            parts.fruits.forEach { fruit ->
                if (animState.fruitOpacity > 0f) {
                    
                    val attachPoint = if (fruit.attachTo == LeafAttachment.TRUNK) {
                        
                        
                        val fruitYOffset = (fruit.position - 0.5f) * canopyRadius * 1.2f
                        val fruitY = canopyCenter.y + fruitYOffset
                        
                        
                        val spreadX = sin(fruit.phaseOffset * 1.5f) * canopyRadius * 0.6f
                        
                        
                        val swayX = sin(Math.toRadians(canopySway.toDouble())).toFloat() * canopyRadius * 0.3f
                        
                        Offset(centerX + spreadX + swayX, fruitY)
                    } else {
                        branchEnds[fruit.attachTo] ?: trunkTop
                    }
                    
                    val fruitSway = windState.getRotation("branch", fruit.phaseOffset, 0.4f) * 0.5f
                    
                    drawIllustrationFruit(
                        fruit = fruit,
                        attachPoint = attachPoint,
                        canvasHeight = canvasHeight,
                        sway = fruitSway,
                        opacity = animState.fruitOpacity,
                        scale = scale
                    )
                }
            }
        }
    }
}


private fun DrawScope.drawOrganicTrunk(
    trunk: TrunkConfig,
    centerX: Float,
    baseY: Float,
    canvasHeight: Float,
    rotation: Float,
    growthFactor: Float,
    scale: Float,
    seed: Int
) {
    val trunkHeight = canvasHeight * trunk.height * growthFactor * scale
    val baseWidth = canvasHeight * trunk.width * scale
    val topWidth = canvasHeight * trunk.width * trunk.taperRatio * scale
    
    val color = trunk.color
    val highlightColor = color.lighten(0.15f)
    val shadowColor = color.darken(0.2f)
    
    
    rotate(rotation, pivot = Offset(centerX, baseY)) {
        
        val trunkPath = Path().apply {
            
            moveTo(centerX - baseWidth / 2, baseY)
            cubicTo(
                centerX - baseWidth / 2 + baseWidth * 0.05f, baseY - trunkHeight * 0.3f,
                centerX - topWidth / 2 - topWidth * 0.1f, baseY - trunkHeight * 0.7f,
                centerX - topWidth / 2, baseY - trunkHeight
            )
            
            
            lineTo(centerX + topWidth / 2, baseY - trunkHeight)
            
            
            cubicTo(
                centerX + topWidth / 2 + topWidth * 0.1f, baseY - trunkHeight * 0.7f,
                centerX + baseWidth / 2 - baseWidth * 0.05f, baseY - trunkHeight * 0.3f,
                centerX + baseWidth / 2, baseY
            )
            close()
        }
        
        
        drawPath(
            path = trunkPath,
            brush = Brush.horizontalGradient(
                colors = listOf(shadowColor, color, highlightColor, color, shadowColor),
                startX = centerX - baseWidth / 2,
                endX = centerX + baseWidth / 2
            )
        )
        
        
        val textureCount = 3
        for (i in 0 until textureCount) {
            val yPos = baseY - trunkHeight * (0.2f + i * 0.25f)
            val widthAtY = baseWidth - (baseWidth - topWidth) * (1f - (yPos - (baseY - trunkHeight)) / trunkHeight)
            
            drawLine(
                color = shadowColor.copy(alpha = 0.2f),
                start = Offset(centerX - widthAtY * 0.35f, yPos),
                end = Offset(centerX + widthAtY * 0.25f, yPos - trunkHeight * 0.02f),
                strokeWidth = canvasHeight * 0.003f
            )
        }
    }
}


private fun DrawScope.drawOrganicBranch(
    branch: BranchConfig,
    trunkTop: Offset,
    trunkBaseY: Float,
    canvasHeight: Float,
    parentRotation: Float,
    selfRotation: Float,
    growthFactor: Float,
    scale: Float
): Offset {
    val branchLength = canvasHeight * branch.length * growthFactor * scale
    val branchWidth = canvasHeight * branch.width * scale
    
    
    val branchStartY = trunkBaseY - (trunkBaseY - trunkTop.y) * branch.attachHeight
    val branchStart = Offset(trunkTop.x, branchStartY)
    
    
    val totalRotation = branch.angle + parentRotation + selfRotation
    val rotationRad = Math.toRadians(totalRotation.toDouble()).toFloat()
    
    
    val endX = branchStart.x + sin(rotationRad) * branchLength
    val endY = branchStart.y - cos(rotationRad) * branchLength
    
    
    val ctrlX = branchStart.x + sin(rotationRad) * branchLength * 0.5f + branchLength * 0.1f
    val ctrlY = branchStart.y - cos(rotationRad) * branchLength * 0.5f
    
    val branchPath = Path().apply {
        moveTo(branchStart.x, branchStart.y)
        quadraticBezierTo(ctrlX, ctrlY, endX, endY)
    }
    
    drawPath(
        path = branchPath,
        color = branch.color,
        style = Stroke(
            width = branchWidth,
            cap = StrokeCap.Round
        )
    )
    
    return Offset(endX, endY)
}

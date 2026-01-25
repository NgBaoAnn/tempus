package com.projectapp.tempus.ui.garden.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType
import kotlin.math.*

/**
 * ProceduralTree - Illustration-style tree renderer với hierarchical per-part motion
 * 
 * Visual style: Forest app-like, game illustration, NOT diagram
 * - Organic bezier trunk with gradient
 * - Many small leaf clusters (teardrop/organic shapes)
 * - Shadow under pot/tree
 * - Soft palette, no hard outlines
 */
@Composable
fun ProceduralTree(
    treeType: TreeType,
    growthStage: TreeState,
    modifier: Modifier = Modifier,
    treeId: Long? = null,
    size: Dp = 150.dp
) {
    // Seed cho deterministic randomness
    val seed = remember(treeType, treeId) {
        (treeId?.toInt() ?: treeType.name.hashCode()) and 0x7FFFFFFF
    }
    
    // Wind state
    val windState by rememberWindState(seed)
    
    // Get config cho current stage
    val currentConfig = remember(treeType, growthStage, seed) {
        getTreeConfig(treeType, growthStage, seed)
    }
    
    // Stage transition animation
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
    
    // Canvas rendering
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
        
        // Calculate positions first
        val potHeight = (config.pot?.height ?: 0f) * canvasHeight
        val trunkBaseY = baseY - potHeight * 0.35f
        
        // Calculate trunk sway
        val trunkRotation = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
            windState.getRotation("trunk", 0f, 0.3f) * (1f - parts.stiffness)
        } else 0f
        
        // ====== Z-ORDER: BACK TO FRONT ======
        
        // 1. Ground shadow (very back)
        drawGroundShadow(centerX, baseY, canvasWidth * 0.35f * scale)
        
        // 2. Pot body (behind everything except shadow)
        config.pot?.let { pot ->
            drawIllustrationPot(pot, centerX, baseY, canvasWidth, canvasHeight)
        }
        
        // 3. Calculate trunk top position first (needed for branches)
        val trunkHeight = canvasHeight * parts.trunk.height * animState.trunkGrowth * scale
        val trunkTop = Offset(centerX, trunkBaseY - trunkHeight)
        
        // 4. Draw BRANCHES FIRST (behind trunk)
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
        
        // 5. Draw TRUNK (in front of branches, covers branch attachment points)
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
        
        // 6. Soil mound (on top of pot, around trunk base)
        if (config.showSoil) {
            drawSoilMound(config.soilColor, centerX, trunkBaseY + canvasHeight * 0.01f, canvasWidth * 0.18f * scale, canvasHeight * 0.025f)
        }
        
        // 6. Draw many small leaf clusters (illustration style)
        val leafClusterCount = when (growthStage) {
            TreeState.SEED -> 4
            TreeState.SPROUT -> 8
            TreeState.SAPLING -> 15
            TreeState.TREE -> 25
            TreeState.DEAD -> 0
        }
        
        if (animState.leafOpacity > 0f && leafClusterCount > 0) {
            // Check if this is a palm/coconut tree with FROND leaves
            val hasFrondLeaves = parts.leafClusters.any { it.shape == LeafShape.FROND }
            val hasConiferLeaves = parts.leafClusters.any { it.shape == LeafShape.CONIFER }
            
            if (hasFrondLeaves) {
                // ===== SPECIAL: Vẽ tàu lá dừa từ đỉnh trunk =====
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
                // ===== SPECIAL: Vẽ cây thông với các tầng tam giác nhọn =====
                val tierCount = when (growthStage) {
                    TreeState.SEED -> 1
                    TreeState.SPROUT -> 2
                    TreeState.SAPLING -> 3
                    TreeState.TREE -> 4
                    TreeState.DEAD -> 0
                }
                
                // Dùng "branch" để có amplitude lớn hơn, nhân 3 để đung đưa rõ
                val windSway = if (parts.stiffness < 1f && growthStage != TreeState.DEAD) {
                    windState.getRotation("branch", stablePhaseOffset(seed, 0), 0.6f) * (1f - parts.stiffness) * 3f
                } else 0f
                
                drawConiferTree(
                    trunkTop = trunkTop,
                    canvasHeight = canvasHeight,
                    tierCount = tierCount,
                    baseColor = parts.baseColor,
                    opacity = animState.leafOpacity,
                    scale = scale * animState.leafScale,
                    sway = windSway,
                    seed = seed
                )
            } else {
                // ===== NORMAL: Canopy cho các loại cây khác =====
                val canopyRadius = canvasHeight * when (growthStage) {
                    TreeState.SEED -> 0.08f
                    TreeState.SPROUT -> 0.12f
                    TreeState.SAPLING -> 0.18f
                    TreeState.TREE -> 0.25f
                    TreeState.DEAD -> 0f
                } * scale * animState.leafScale
                
                // Draw canopy base (solid green mass) first for cohesion
                drawCanopyBase(
                    center = Offset(trunkTop.x, trunkTop.y - canopyRadius * 0.3f),
                    radiusX = canopyRadius * 1.1f,
                    radiusY = canopyRadius * 0.9f,
                    color = parts.baseColor.darken(0.1f),
                    opacity = animState.leafOpacity * 0.7f
                )
                
                // Generate tightly clustered leaf positions
                val leafPositions = generateTightLeafPositions(
                    center = Offset(trunkTop.x, trunkTop.y - canopyRadius * 0.35f),
                    radiusX = canopyRadius,
                    radiusY = canopyRadius * 0.75f,
                    count = leafClusterCount,
                    seed = seed
                )
                
                // Draw leaf shadows first
                leafPositions.forEach { pos ->
                    drawLeafShadow(pos, canvasHeight * 0.025f * scale * animState.leafScale)
                }
                
                // Draw leaves with different depths
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
        
        // 7. Draw fruits
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
    }
}

// ========== Illustration Drawing Functions ==========

/**
 * Ground shadow ellipse dưới cây
 */
private fun DrawScope.drawGroundShadow(
    centerX: Float,
    baseY: Float,
    radius: Float
) {
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = Offset(centerX, baseY + radius * 0.1f),
            radius = radius
        ),
        topLeft = Offset(centerX - radius, baseY - radius * 0.15f),
        size = Size(radius * 2, radius * 0.35f)
    )
}

/**
 * Pot với gradient và depth - SOLID không xuyên thấu
 */
private fun DrawScope.drawIllustrationPot(
    pot: PotConfig,
    centerX: Float,
    baseY: Float,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val potWidth = canvasWidth * pot.width
    val potHeight = canvasHeight * pot.height
    
    // Pot shadow (behind pot)
    drawOval(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(centerX - potWidth * 0.4f, baseY - potHeight * 0.08f),
        size = Size(potWidth * 0.8f, potHeight * 0.16f)
    )
    
    // Pot body path
    val potPath = Path().apply {
        val topWidth = potWidth * 0.9f
        val bottomWidth = potWidth * 0.72f
        
        moveTo(centerX - topWidth / 2, baseY - potHeight)
        cubicTo(
            centerX - topWidth / 2 - potWidth * 0.02f, baseY - potHeight * 0.5f,
            centerX - bottomWidth / 2 - potWidth * 0.01f, baseY - potHeight * 0.2f,
            centerX - bottomWidth / 2, baseY
        )
        lineTo(centerX + bottomWidth / 2, baseY)
        cubicTo(
            centerX + bottomWidth / 2 + potWidth * 0.01f, baseY - potHeight * 0.2f,
            centerX + topWidth / 2 + potWidth * 0.02f, baseY - potHeight * 0.5f,
            centerX + topWidth / 2, baseY - potHeight
        )
        close()
    }
    
    // SOLID base fill first (no transparency)
    drawPath(path = potPath, color = pot.color)
    
    // Gradient overlay for 3D effect
    drawPath(
        path = potPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.15f),  // Shadow on left
                Color.Transparent,                 // Center clear
                Color.Black.copy(alpha = 0.2f)    // Shadow on right
            ),
            startX = centerX - potWidth / 2,
            endX = centerX + potWidth / 2
        )
    )
    
    // Pot rim - SOLID
    val rimHeight = potHeight * 0.12f
    drawRoundRect(
        color = pot.color,  // Solid base
        topLeft = Offset(centerX - potWidth * 0.48f, baseY - potHeight - rimHeight * 0.5f),
        size = Size(potWidth * 0.96f, rimHeight),
        cornerRadius = CornerRadius(rimHeight / 2)
    )
    
    // Rim highlight
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.2f),
                Color.Transparent
            )
        ),
        topLeft = Offset(centerX - potWidth * 0.48f, baseY - potHeight - rimHeight * 0.5f),
        size = Size(potWidth * 0.96f, rimHeight * 0.5f),
        cornerRadius = CornerRadius(rimHeight / 2)
    )
    
    // Inner soil visible in pot (dark circle at top)
    drawOval(
        color = Color(0xFF3D2817),
        topLeft = Offset(centerX - potWidth * 0.38f, baseY - potHeight - rimHeight * 0.15f),
        size = Size(potWidth * 0.76f, potHeight * 0.08f)
    )
}

/**
 * Soil mound với gradient
 */
private fun DrawScope.drawSoilMound(
    color: Color,
    centerX: Float,
    y: Float,
    radiusX: Float,
    radiusY: Float
) {
    drawOval(
        brush = Brush.verticalGradient(
            colors = listOf(color.lighten(0.1f), color.darken(0.1f)),
            startY = y - radiusY,
            endY = y + radiusY
        ),
        topLeft = Offset(centerX - radiusX, y - radiusY),
        size = Size(radiusX * 2, radiusY * 2.2f)
    )
}

/**
 * Organic trunk với bezier curves và gradient
 */
private fun DrawScope.drawOrganicTrunk(
    trunk: TrunkConfig,
    centerX: Float,
    baseY: Float,
    canvasHeight: Float,
    rotation: Float,
    growthFactor: Float,
    scale: Float,
    seed: Int
): Offset {
    val trunkHeight = canvasHeight * trunk.height * growthFactor * scale
    val baseWidth = canvasHeight * trunk.width * scale
    val topWidth = baseWidth * trunk.taperRatio
    
    rotate(rotation, pivot = Offset(centerX, baseY)) {
        if (trunk.segments > 1) {
            // Segmented trunk (bamboo style)
            drawBambooTrunk(trunk, centerX, baseY, canvasHeight, growthFactor, scale)
        } else {
            // Organic bezier trunk
            val trunkPath = Path().apply {
                // Left edge - slight curve outward then inward
                moveTo(centerX - baseWidth / 2, baseY)
                val curveAmount = baseWidth * 0.08f * (stableRandom(seed, 0) - 0.5f)
                cubicTo(
                    centerX - baseWidth / 2 + curveAmount, baseY - trunkHeight * 0.3f,
                    centerX - topWidth / 2 - curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                    centerX - topWidth / 2, baseY - trunkHeight
                )
                
                // Top edge
                lineTo(centerX + topWidth / 2, baseY - trunkHeight)
                
                // Right edge - mirror curve
                cubicTo(
                    centerX + topWidth / 2 + curveAmount * 0.5f, baseY - trunkHeight * 0.7f,
                    centerX + baseWidth / 2 - curveAmount, baseY - trunkHeight * 0.3f,
                    centerX + baseWidth / 2, baseY
                )
                close()
            }
            
            // Trunk gradient (3D effect)
            drawPath(
                path = trunkPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        trunk.color.darken(0.15f),
                        trunk.color.lighten(0.05f),
                        trunk.color,
                        trunk.color.darken(0.2f)
                    ),
                    startX = centerX - baseWidth / 2,
                    endX = centerX + baseWidth / 2
                )
            )
            
            // Subtle bark texture lines
            for (i in 1..3) {
                val lineY = baseY - trunkHeight * (0.2f + i * 0.2f)
                val lineWidth = baseWidth - (baseWidth - topWidth) * (0.2f + i * 0.2f)
                drawLine(
                    color = trunk.color.darken(0.1f).copy(alpha = 0.3f),
                    start = Offset(centerX - lineWidth * 0.3f, lineY),
                    end = Offset(centerX + lineWidth * 0.2f, lineY + trunkHeight * 0.03f),
                    strokeWidth = baseWidth * 0.03f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
    
    val topY = baseY - trunkHeight
    val rotRad = Math.toRadians(rotation.toDouble()).toFloat()
    return Offset(
        centerX + sin(rotRad) * trunkHeight * 0.1f,
        topY
    )
}

/**
 * Bamboo-style segmented trunk
 */
private fun DrawScope.drawBambooTrunk(
    trunk: TrunkConfig,
    centerX: Float,
    baseY: Float,
    canvasHeight: Float,
    growthFactor: Float,
    scale: Float
) {
    val trunkHeight = canvasHeight * trunk.height * growthFactor * scale
    val baseWidth = canvasHeight * trunk.width * scale
    val topWidth = baseWidth * trunk.taperRatio
    val segmentCount = trunk.segments
    val segmentHeight = trunkHeight / segmentCount
    
    for (i in 0 until segmentCount) {
        val segY = baseY - segmentHeight * (i + 1)
        val segWidth = baseWidth - (baseWidth - topWidth) * (i.toFloat() / segmentCount)
        
        // Segment body với gradient
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    trunk.color.darken(0.1f),
                    trunk.color.lighten(0.1f),
                    trunk.color.darken(0.05f)
                )
            ),
            topLeft = Offset(centerX - segWidth / 2, segY),
            size = Size(segWidth, segmentHeight * 0.88f),
            cornerRadius = CornerRadius(segWidth / 3)
        )
        
        // Joint ring
        if (i < segmentCount - 1) {
            drawOval(
                color = trunk.color.darken(0.15f),
                topLeft = Offset(centerX - segWidth * 0.55f / 2, segY - segmentHeight * 0.04f),
                size = Size(segWidth * 0.55f, segmentHeight * 0.08f)
            )
        }
    }
}

/**
 * Organic branch với bezier curve
 */
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
    val trunkHeight = trunkBaseY - trunkTop.y
    val attachY = trunkTop.y + trunkHeight * (1 - branch.attachHeight)
    val attachPoint = Offset(trunkTop.x, attachY)
    
    val branchLength = canvasHeight * branch.length * growthFactor * scale
    val branchWidth = canvasHeight * branch.width * scale
    
    val totalAngle = branch.angle + parentRotation + selfRotation
    val angleRad = Math.toRadians(totalAngle.toDouble()).toFloat()
    
    val endX = attachPoint.x + sin(angleRad) * branchLength
    val endY = attachPoint.y - cos(angleRad) * branchLength
    
    // Control point for curve (slight droop)
    val ctrlX = attachPoint.x + sin(angleRad) * branchLength * 0.6f
    val ctrlY = attachPoint.y - cos(angleRad) * branchLength * 0.4f + branchLength * 0.05f
    
    val branchPath = Path().apply {
        moveTo(attachPoint.x, attachPoint.y)
        quadraticBezierTo(ctrlX, ctrlY, endX, endY)
    }
    
    // Branch với gradient stroke
    drawPath(
        path = branchPath,
        brush = Brush.linearGradient(
            colors = listOf(branch.color, branch.color.darken(0.1f)),
            start = attachPoint,
            end = Offset(endX, endY)
        ),
        style = Stroke(
            width = branchWidth,
            cap = StrokeCap.Round,
            pathEffect = null
        )
    )
    
    return Offset(endX, endY)
}

/**
 * Draw canopy base - solid mass creating cohesive look
 */
private fun DrawScope.drawCanopyBase(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    color: Color,
    opacity: Float
) {
    // Multiple overlapping ovals for organic canopy shape
    val baseColor = color.copy(alpha = opacity)
    
    // Main canopy mass
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                baseColor,
                baseColor.copy(alpha = opacity * 0.8f),
                Color.Transparent
            ),
            center = center,
            radius = radiusX
        ),
        topLeft = Offset(center.x - radiusX, center.y - radiusY),
        size = Size(radiusX * 2, radiusY * 2)
    )
    
    // Left bulge
    drawOval(
        color = baseColor.copy(alpha = opacity * 0.6f),
        topLeft = Offset(center.x - radiusX * 1.1f, center.y - radiusY * 0.6f),
        size = Size(radiusX * 0.8f, radiusY * 0.9f)
    )
    
    // Right bulge
    drawOval(
        color = baseColor.copy(alpha = opacity * 0.6f),
        topLeft = Offset(center.x + radiusX * 0.3f, center.y - radiusY * 0.5f),
        size = Size(radiusX * 0.8f, radiusY * 0.85f)
    )
    
    // Top highlight
    drawOval(
        color = color.lighten(0.15f).copy(alpha = opacity * 0.4f),
        topLeft = Offset(center.x - radiusX * 0.5f, center.y - radiusY * 0.9f),
        size = Size(radiusX * 0.7f, radiusY * 0.4f)
    )
}

/**
 * Generate tightly clustered leaf positions within canopy bounds
 */
private fun generateTightLeafPositions(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    count: Int,
    seed: Int
): List<Offset> {
    val positions = mutableListOf<Offset>()
    
    for (i in 0 until count) {
        // Distribute in concentric rings for tight clustering
        val ring = i / 6  // 6 leaves per ring
        val indexInRing = i % 6
        
        val ringRadius = 0.3f + ring * 0.25f  // Start from center, expand outward
        val angleOffset = ring * 30f  // Offset each ring
        val angle = (indexInRing * 60f + angleOffset + stableRandom(seed, i) * 25f)
        
        val angleRad = Math.toRadians(angle.toDouble()).toFloat()
        val distX = radiusX * ringRadius * (0.7f + stableRandom(seed, i + 100) * 0.3f)
        val distY = radiusY * ringRadius * (0.6f + stableRandom(seed, i + 200) * 0.4f)
        
        positions.add(
            Offset(
                center.x + cos(angleRad) * distX,
                center.y + sin(angleRad) * distY * 0.8f - radiusY * 0.1f
            )
        )
    }
    
    return positions
}

/**
 * Leaf shadow
 */
private fun DrawScope.drawLeafShadow(center: Offset, size: Float) {
    drawOval(
        color = Color.Black.copy(alpha = 0.05f),
        topLeft = Offset(center.x - size * 0.8f, center.y + size * 0.1f),
        size = Size(size * 1.6f, size * 0.4f)
    )
}

/**
 * Illustration-style leaf cluster (teardrop/organic shape)
 */
private fun DrawScope.drawIllustrationLeaf(
    center: Offset,
    size: Float,
    rotation: Float,
    baseColor: Color,
    opacity: Float,
    shape: LeafShape,
    seed: Int
) {
    rotate(rotation, pivot = center) {
        val color = baseColor.copy(alpha = opacity)
        val highlightColor = baseColor.lighten(0.2f).copy(alpha = opacity * 0.6f)
        val shadowColor = baseColor.darken(0.15f).copy(alpha = opacity)
        
        when (shape) {
            LeafShape.ROUND -> {
                // Teardrop/organic cluster shape
                val leafPath = Path().apply {
                    // Main teardrop
                    moveTo(center.x, center.y - size * 0.8f) // Top point
                    cubicTo(
                        center.x + size * 0.7f, center.y - size * 0.5f,
                        center.x + size * 0.6f, center.y + size * 0.3f,
                        center.x, center.y + size * 0.4f
                    )
                    cubicTo(
                        center.x - size * 0.6f, center.y + size * 0.3f,
                        center.x - size * 0.7f, center.y - size * 0.5f,
                        center.x, center.y - size * 0.8f
                    )
                    close()
                }
                
                // Shadow layer
                translate(left = size * 0.05f, top = size * 0.05f) {
                    drawPath(leafPath, shadowColor)
                }
                
                // Main leaf với gradient
                drawPath(
                    path = leafPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(highlightColor, color, shadowColor),
                        startY = center.y - size,
                        endY = center.y + size * 0.5f
                    )
                )
                
                // Small highlight spot
                drawCircle(
                    color = Color.White.copy(alpha = opacity * 0.15f),
                    radius = size * 0.15f,
                    center = Offset(center.x - size * 0.2f, center.y - size * 0.4f)
                )
            }
            
            LeafShape.NEEDLE -> {
                // Clustered needles
                val needleCount = 5
                for (i in 0 until needleCount) {
                    val angle = -25f + (50f / (needleCount - 1)) * i
                    val needleAngleRad = Math.toRadians(angle.toDouble()).toFloat()
                    val needleLength = size * (0.9f + stableRandom(seed, i) * 0.2f)
                    
                    val endX = center.x + sin(needleAngleRad) * needleLength
                    val endY = center.y - cos(needleAngleRad) * needleLength
                    
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(color, shadowColor),
                            start = center,
                            end = Offset(endX, endY)
                        ),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = size * 0.12f,
                        cap = StrokeCap.Round
                    )
                }
            }
            
            LeafShape.LONG -> {
                // Long drooping leaf với bezier
                val leafPath = Path().apply {
                    moveTo(center.x, center.y)
                    val droopAngle = stableRandom(seed, 0) * 30f - 15f
                    val droopRad = Math.toRadians(droopAngle.toDouble()).toFloat()
                    val endX = center.x + sin(droopRad) * size * 1.5f
                    val endY = center.y + size * 0.8f
                    quadraticBezierTo(
                        center.x + sin(droopRad) * size * 0.7f,
                        center.y - size * 0.1f,
                        endX, endY
                    )
                }
                
                drawPath(
                    path = leafPath,
                    brush = Brush.linearGradient(
                        colors = listOf(color, shadowColor),
                        start = center,
                        end = Offset(center.x, center.y + size)
                    ),
                    style = Stroke(width = size * 0.25f, cap = StrokeCap.Round)
                )
            }
            
            LeafShape.PETAL -> {
                // Cherry blossom petal cluster
                val petalCount = 5
                for (i in 0 until petalCount) {
                    val angle = 360f / petalCount * i + stableRandom(seed, i) * 15f
                    val angleRad = Math.toRadians(angle.toDouble()).toFloat()
                    val petalDist = size * 0.35f
                    val petalCenterX = center.x + cos(angleRad) * petalDist
                    val petalCenterY = center.y + sin(angleRad) * petalDist - size * 0.2f
                    
                    // Petal shape
                    val petalPath = Path().apply {
                        val petalSize = size * 0.4f
                        moveTo(petalCenterX, petalCenterY - petalSize * 0.6f)
                        cubicTo(
                            petalCenterX + petalSize * 0.5f, petalCenterY - petalSize * 0.3f,
                            petalCenterX + petalSize * 0.4f, petalCenterY + petalSize * 0.4f,
                            petalCenterX, petalCenterY + petalSize * 0.5f
                        )
                        cubicTo(
                            petalCenterX - petalSize * 0.4f, petalCenterY + petalSize * 0.4f,
                            petalCenterX - petalSize * 0.5f, petalCenterY - petalSize * 0.3f,
                            petalCenterX, petalCenterY - petalSize * 0.6f
                        )
                        close()
                    }
                    
                    drawPath(
                        path = petalPath,
                        brush = Brush.radialGradient(
                            colors = listOf(highlightColor, color),
                            center = Offset(petalCenterX, petalCenterY),
                            radius = size * 0.4f
                        )
                    )
                }
                
                // Yellow center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFEB3B), Color(0xFFFFC107)),
                        center = Offset(center.x, center.y - size * 0.2f),
                        radius = size * 0.15f
                    ),
                    radius = size * 0.12f,
                    center = Offset(center.x, center.y - size * 0.2f)
                )
            }
            
            LeafShape.FROND -> {
                // Tàu lá dừa kiểu lá chuối - rộng, cong, có răng cưa
                val frondLength = size * 2.2f
                val frondWidth = size * 0.6f
                
                // Cuống chính - cong rủ xuống
                val curveDirection = if (stableRandom(seed, 0) > 0.5f) 1f else -1f
                val curveAmount = size * 0.4f * curveDirection
                
                // Vẽ tàu lá như hình lá chuối
                val frondPath = Path().apply {
                    // Bắt đầu từ gốc
                    moveTo(center.x, center.y)
                    
                    // Cạnh trái của lá - cong ra ngoài rồi vào
                    val midX = center.x + curveAmount * 0.5f
                    val midY = center.y + frondLength * 0.5f
                    val endX = center.x + curveAmount
                    val endY = center.y + frondLength
                    
                    // Left edge với răng cưa
                    cubicTo(
                        center.x - frondWidth * 0.3f, center.y + frondLength * 0.2f,
                        midX - frondWidth * 0.5f, midY,
                        endX - frondWidth * 0.15f, endY - frondLength * 0.1f
                    )
                    
                    // Đầu lá nhọn
                    lineTo(endX, endY)
                    
                    // Right edge
                    cubicTo(
                        midX + frondWidth * 0.5f, midY,
                        center.x + frondWidth * 0.3f, center.y + frondLength * 0.2f,
                        center.x, center.y
                    )
                    close()
                }
                
                // Fill lá với gradient
                drawPath(
                    path = frondPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.darken(0.1f),
                            color,
                            highlightColor
                        ),
                        start = center,
                        end = Offset(center.x + curveAmount, center.y + frondLength)
                    )
                )
                
                // Gân giữa lá (cuống)
                val stemPath = Path().apply {
                    moveTo(center.x, center.y)
                    quadraticBezierTo(
                        center.x + curveAmount * 0.5f, center.y + frondLength * 0.5f,
                        center.x + curveAmount, center.y + frondLength
                    )
                }
                drawPath(
                    path = stemPath,
                    color = color.darken(0.2f),
                    style = Stroke(width = size * 0.06f, cap = StrokeCap.Round)
                )
                
                // Vẽ các đường gân lá (các nét nhỏ 2 bên cuống)
                val veinCount = 6
                for (i in 1..veinCount) {
                    val t = i.toFloat() / (veinCount + 1)
                    val stemX = center.x + curveAmount * t
                    val stemY = center.y + frondLength * t
                    
                    // Gân bên trái
                    val leftEndX = stemX - frondWidth * 0.4f * (1f - t * 0.5f)
                    val leftEndY = stemY + frondLength * 0.08f
                    drawLine(
                        color = color.darken(0.15f).copy(alpha = opacity * 0.6f),
                        start = Offset(stemX, stemY),
                        end = Offset(leftEndX, leftEndY),
                        strokeWidth = size * 0.02f
                    )
                    
                    // Gân bên phải
                    val rightEndX = stemX + frondWidth * 0.4f * (1f - t * 0.5f)
                    val rightEndY = stemY + frondLength * 0.08f
                    drawLine(
                        color = color.darken(0.15f).copy(alpha = opacity * 0.6f),
                        start = Offset(stemX, stemY),
                        end = Offset(rightEndX, rightEndY),
                        strokeWidth = size * 0.02f
                    )
                }
            }
            
            LeafShape.CONIFER -> {
                // CONIFER được xử lý riêng bởi drawConiferTree
                // Không làm gì ở đây
            }
        }
    }
}

/**
 * Illustration-style fruit
 */
private fun DrawScope.drawIllustrationFruit(
    fruit: FruitConfig,
    attachPoint: Offset,
    canvasHeight: Float,
    sway: Float,
    opacity: Float,
    scale: Float
) {
    val fruitSize = canvasHeight * fruit.size * scale
    val swayOffset = sin(Math.toRadians(sway.toDouble())).toFloat() * fruitSize * 0.3f
    
    // Dùng phaseOffset để tách các quả ra theo chiều ngang
    val spreadOffset = sin(fruit.phaseOffset) * fruitSize * 1.5f
    
    val fruitCenter = Offset(
        attachPoint.x + swayOffset + spreadOffset,
        attachPoint.y + fruitSize * 0.6f + abs(cos(fruit.phaseOffset)) * fruitSize * 0.3f
    )
    
    // Fruit shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.1f),
        topLeft = Offset(fruitCenter.x - fruitSize * 0.8f, fruitCenter.y + fruitSize * 0.8f),
        size = Size(fruitSize * 1.6f, fruitSize * 0.3f)
    )
    
    // Fruit body với gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                fruit.color.lighten(0.15f).copy(alpha = opacity),
                fruit.color.copy(alpha = opacity),
                fruit.color.darken(0.2f).copy(alpha = opacity)
            ),
            center = Offset(fruitCenter.x - fruitSize * 0.2f, fruitCenter.y - fruitSize * 0.2f),
            radius = fruitSize * 1.2f
        ),
        radius = fruitSize,
        center = fruitCenter
    )
    
    // Highlight
    drawCircle(
        color = Color.White.copy(alpha = opacity * 0.35f),
        radius = fruitSize * 0.25f,
        center = Offset(fruitCenter.x - fruitSize * 0.35f, fruitCenter.y - fruitSize * 0.35f)
    )
    
    // Stem
    val stemPath = Path().apply {
        moveTo(fruitCenter.x, fruitCenter.y - fruitSize)
        quadraticBezierTo(
            fruitCenter.x + fruitSize * 0.15f, fruitCenter.y - fruitSize * 1.15f,
            fruitCenter.x + fruitSize * 0.25f, fruitCenter.y - fruitSize * 1.35f
        )
    }
    drawPath(
        path = stemPath,
        color = Color(0xFF5D4037).copy(alpha = opacity),
        style = Stroke(width = fruitSize * 0.12f, cap = StrokeCap.Round)
    )
    
    // Small leaf on stem
    val leafX = fruitCenter.x + fruitSize * 0.15f
    val leafY = fruitCenter.y - fruitSize * 1.1f
    drawOval(
        color = Color(0xFF4CAF50).copy(alpha = opacity),
        topLeft = Offset(leafX, leafY - fruitSize * 0.12f),
        size = Size(fruitSize * 0.25f, fruitSize * 0.15f)
    )
}

/**
 * Vẽ tàu lá dừa - gốc tại origin, xoè ra theo spreadAngle rồi rủ xuống
 */
private fun DrawScope.drawCoconutFrond(
    origin: Offset,
    spreadAngle: Float,  // Góc xoè (độ), 0 = thẳng lên, -70 = trái, +70 = phải
    frondLength: Float,
    frondWidth: Float,
    baseColor: Color,
    opacity: Float,
    seed: Int
) {
    val color = baseColor.copy(alpha = opacity)
    val highlightColor = baseColor.lighten(0.15f).copy(alpha = opacity)
    val shadowColor = baseColor.darken(0.2f).copy(alpha = opacity)
    
    // Convert góc từ degree sang radian
    val angleRad = Math.toRadians(spreadAngle.toDouble()).toFloat()
    
    // Tính điểm cuối của tàu lá (VỂNH LÊN trước rồi mới rủ xuống)
    val upwardDist = frondLength * 0.5f   // Đoạn đầu vểnh lên + xoè ra
    val droopDist = frondLength * 0.5f    // Đoạn rủ xuống
    
    // Control point 1: Vểnh LÊN TRÊN + xoè ra (lên cao hơn)
    val cp1X = origin.x + sin(angleRad) * upwardDist * 0.8f
    val cp1Y = origin.y - cos(angleRad) * upwardDist * 0.6f - upwardDist * 0.3f  // Lên nhiều hơn
    
    // Control point 2: Bắt đầu cong rủ xuống
    val cp2X = origin.x + sin(angleRad) * upwardDist * 1.2f
    val cp2Y = origin.y - upwardDist * 0.1f + droopDist * 0.2f
    
    // End point: Rủ xuống (nhưng vẫn xa hơn)
    val endX = origin.x + sin(angleRad) * upwardDist * 1.1f
    val endY = origin.y + droopDist * 0.6f
    
    // Vẽ hình dạng tàu lá (rộng ở giữa, nhọn 2 đầu)
    val frondPath = Path().apply {
        moveTo(origin.x, origin.y)
        
        // Cạnh ngoài (xa thân cây)
        val outerOffset = frondWidth * 0.5f
        cubicTo(
            cp1X + cos(angleRad) * outerOffset, cp1Y - sin(angleRad) * outerOffset,
            cp2X + outerOffset * 0.6f, cp2Y,
            endX + outerOffset * 0.2f, endY
        )
        
        // Đầu lá nhọn
        lineTo(endX - outerOffset * 0.1f, endY + frondWidth * 0.15f)
        
        // Cạnh trong (gần thân cây)
        cubicTo(
            cp2X - outerOffset * 0.4f, cp2Y + outerOffset * 0.2f,
            cp1X - cos(angleRad) * outerOffset * 0.3f, cp1Y + sin(angleRad) * outerOffset * 0.3f,
            origin.x, origin.y
        )
        close()
    }
    
    // Fill tàu lá with gradient
    drawPath(
        path = frondPath,
        brush = Brush.linearGradient(
            colors = listOf(shadowColor, color, highlightColor),
            start = origin,
            end = Offset(endX, endY)
        )
    )
    
    // Gân giữa (cuống lá)
    val midRibPath = Path().apply {
        moveTo(origin.x, origin.y)
        cubicTo(cp1X, cp1Y, cp2X, cp2Y, endX, endY)
    }
    drawPath(
        path = midRibPath,
        color = baseColor.darken(0.25f).copy(alpha = opacity * 0.8f),
        style = Stroke(width = frondWidth * 0.08f, cap = StrokeCap.Round)
    )
    
    // Các gân phụ (6 gân mỗi bên)
    for (i in 1..6) {
        val t = i * 0.12f + 0.1f  // 0.22 to 0.82 along the frond
        
        // Điểm trên cuống tại t
        val ribT = t * t  // Ease out
        val ribX = origin.x + (cp1X - origin.x) * ribT * 0.5f + (cp2X - cp1X) * ribT + (endX - cp2X) * ribT * 0.5f
        val ribY = origin.y + (cp1Y - origin.y) * ribT * 0.5f + (cp2Y - cp1Y) * ribT + (endY - cp2Y) * ribT * 0.5f
        
        val ribLen = frondWidth * (0.5f - t * 0.3f)
        val ribAngle = angleRad + (if (i % 2 == 0) 0.5f else -0.5f)  // Xen kẽ 2 bên
        
        drawLine(
            color = baseColor.darken(0.15f).copy(alpha = opacity * 0.5f),
            start = Offset(ribX, ribY),
            end = Offset(
                ribX + cos(ribAngle) * ribLen,
                ribY + sin(ribAngle) * ribLen * 0.5f + ribLen * 0.3f
            ),
            strokeWidth = frondWidth * 0.025f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Vẽ cây thông với các tầng tam giác và viền nhọn
 */
private fun DrawScope.drawConiferTree(
    trunkTop: Offset,
    canvasHeight: Float,
    tierCount: Int,
    baseColor: Color,
    opacity: Float,
    scale: Float,
    sway: Float,
    seed: Int
) {
    if (tierCount <= 0) return
    
    val totalHeight = canvasHeight * 0.50f * scale  // Cao hơn
    val baseWidth = canvasHeight * 0.40f * scale    // Rộng hơn
    
    // Màu sắc
    val darkGreen = baseColor.darken(0.15f).copy(alpha = opacity)
    val midGreen = baseColor.copy(alpha = opacity)
    val lightGreen = baseColor.lighten(0.1f).copy(alpha = opacity)
    
    // Vẽ từ dưới lên trên (tầng dưới trước để bị che bởi tầng trên)
    for (tier in 0 until tierCount) {
        val tierProgress = tier.toFloat() / tierCount.coerceAtLeast(1)
        
        // Mỗi tầng nhỏ dần khi lên cao
        val tierWidth = baseWidth * (1f - tierProgress * 0.5f)
        val tierHeight = totalHeight / tierCount * 1.4f  // Overlap nhiều hơn
        
        // Vị trí Y của tầng - bắt đầu từ dưới trunk top
        val tierBottom = trunkTop.y + canvasHeight * 0.02f - totalHeight * tierProgress * 0.80f
        val tierTop = tierBottom - tierHeight
        
        // Sway offset - tầng cao đung đưa nhiều hơn
        val swayOffset = sin(Math.toRadians(sway.toDouble())).toFloat() * tierWidth * 0.15f * (tier + 1)
        val tierCenterX = trunkTop.x + swayOffset
        
        // Vẽ hình tam giác với viền nhọn (spiky)
        val spikeCount = 5 + tier  // Ít spike hơn, dễ nhìn
        
        val tierPath = Path().apply {
            // Bắt đầu từ đỉnh
            moveTo(tierCenterX, tierTop)
            
            // Vẽ cạnh phải với các spike
            val rightEdgeX = tierCenterX + tierWidth / 2
            for (i in 0 until spikeCount) {
                val t = (i + 1).toFloat() / (spikeCount + 1)
                val y = tierTop + tierHeight * t
                val baseX = tierCenterX + tierWidth / 2 * t
                
                // Spike ra ngoài
                val spikeOutX = baseX + tierWidth * 0.08f
                val spikeOutY = y - tierHeight * 0.03f
                lineTo(spikeOutX, spikeOutY)
                
                // Spike vào trong
                val spikeInX = baseX - tierWidth * 0.02f
                val spikeInY = y + tierHeight * 0.02f
                lineTo(spikeInX, spikeInY)
            }
            
            // Góc dưới phải
            lineTo(rightEdgeX, tierBottom)
            
            // Đáy
            lineTo(tierCenterX - tierWidth / 2, tierBottom)
            
            // Vẽ cạnh trái với các spike
            for (i in spikeCount - 1 downTo 0) {
                val t = (i + 1).toFloat() / (spikeCount + 1)
                val y = tierTop + tierHeight * t
                val baseX = tierCenterX - tierWidth / 2 * t
                
                // Spike vào trong trước
                val spikeInX = baseX + tierWidth * 0.02f
                val spikeInY = y + tierHeight * 0.02f
                lineTo(spikeInX, spikeInY)
                
                // Spike ra ngoài
                val spikeOutX = baseX - tierWidth * 0.08f
                val spikeOutY = y - tierHeight * 0.03f
                lineTo(spikeOutX, spikeOutY)
            }
            
            close()
        }
        
        // Fill với gradient
        drawPath(
            path = tierPath,
            brush = Brush.verticalGradient(
                colors = listOf(lightGreen, midGreen, darkGreen),
                startY = tierTop,
                endY = tierBottom
            )
        )
        
        // Viền nhẹ
        drawPath(
            path = tierPath,
            color = darkGreen.copy(alpha = opacity * 0.3f),
            style = Stroke(width = 1.5f)
        )
    }
}

// ========== Color Extension Functions ==========

private fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

private fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// ========== Size enum ==========

enum class ProceduralTreeSize(val dp: Dp) {
    SMALL(60.dp),
    MEDIUM(100.dp),
    LARGE(150.dp),
    XLARGE(200.dp)
}

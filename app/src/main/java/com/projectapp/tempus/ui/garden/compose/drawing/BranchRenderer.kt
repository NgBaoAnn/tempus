package com.projectapp.tempus.ui.garden.compose.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.projectapp.tempus.ui.garden.compose.BranchConfig
import kotlin.math.cos
import kotlin.math.sin

/**
 * Branch Renderer
 * Contains functions for rendering tree branches
 */

/**
 * Draws an organic branch with bezier curve
 * Returns the end point of the branch for attaching leaves/fruits
 */
fun DrawScope.drawOrganicBranch(
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

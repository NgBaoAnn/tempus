package com.projectapp.tempus.ui.garden.compose.trees

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.projectapp.tempus.ui.garden.compose.drawing.darken
import com.projectapp.tempus.ui.garden.compose.drawing.lighten
import com.projectapp.tempus.ui.garden.compose.stableRandom
import kotlin.math.cos
import kotlin.math.sin

/**
 * Oak Tree Renderer - Modern Flat Illustration Style
 * Multi-layer cloud canopy with gradients, highlights, and visible depth
 */

/**
 * Draw an oak-style canopy with 3 layers of cloud clusters for beautiful depth
 */
fun DrawScope.drawOakCanopy(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    baseColor: Color,
    opacity: Float,
    sway: Float,
    seed: Int
) {
    rotate(sway, pivot = center) {
        // Color palette
        val darkGreen = Color(0xFF1B5E20).copy(alpha = opacity)
        val midGreen = Color(0xFF2E7D32).copy(alpha = opacity)
        val lightGreen = Color(0xFF4CAF50).copy(alpha = opacity)
        val highlightGreen = Color(0xFF81C784).copy(alpha = opacity)
        val paleGreen = Color(0xFFC8E6C9).copy(alpha = opacity * 0.8f)
        
        // ============= LAYER 1: Back shadow layer (darkest, largest) =============
        val backClusters = listOf(
            Offset(-0.55f, 0.1f) to 0.55f,
            Offset(0.55f, 0.1f) to 0.52f,
            Offset(-0.35f, -0.25f) to 0.48f,
            Offset(0.35f, -0.25f) to 0.48f,
            Offset(0f, -0.5f) to 0.55f,
        )
        
        backClusters.forEach { (offset, sizeFactor) ->
            val cx = center.x + radiusX * offset.x
            val cy = center.y + radiusY * offset.y
            val r = radiusX * sizeFactor
            
            drawCircle(
                color = darkGreen,
                radius = r,
                center = Offset(cx, cy)
            )
        }
        
        // ============= LAYER 2: Mid layer (medium green, 9 clusters) =============
        val midClusters = listOf(
            Offset(-0.65f, 0.05f) to 0.40f,
            Offset(0.65f, 0.05f) to 0.38f,
            Offset(-0.42f, -0.18f) to 0.45f,
            Offset(0.42f, -0.18f) to 0.45f,
            Offset(-0.18f, -0.42f) to 0.50f,
            Offset(0.18f, -0.42f) to 0.50f,
            Offset(0f, 0.1f) to 0.38f,
            Offset(-0.58f, -0.35f) to 0.35f,
            Offset(0.58f, -0.35f) to 0.35f,
        )
        
        midClusters.forEach { (offset, sizeFactor) ->
            val cx = center.x + radiusX * offset.x
            val cy = center.y + radiusY * offset.y
            val r = radiusX * sizeFactor
            
            drawCircle(
                color = midGreen,
                radius = r,
                center = Offset(cx, cy)
            )
        }
        
        // ============= LAYER 3: Front layer (with radial gradients for 3D) =============
        val frontClusters = listOf(
            Offset(-0.48f, 0f) to 0.35f,
            Offset(0.48f, 0f) to 0.35f,
            Offset(-0.25f, -0.30f) to 0.40f,
            Offset(0.25f, -0.30f) to 0.40f,
            Offset(0f, -0.12f) to 0.38f,
            Offset(-0.65f, -0.22f) to 0.28f,
            Offset(0.65f, -0.22f) to 0.28f,
            Offset(0f, -0.58f) to 0.35f,
        )
        
        frontClusters.forEach { (offset, sizeFactor) ->
            val cx = center.x + radiusX * offset.x
            val cy = center.y + radiusY * offset.y
            val r = radiusX * sizeFactor
            
            // Radial gradient for 3D ball effect
            val gradient = Brush.radialGradient(
                colors = listOf(highlightGreen, lightGreen, midGreen),
                center = Offset(cx - r * 0.3f, cy - r * 0.3f),
                radius = r * 1.2f
            )
            
            drawCircle(
                brush = gradient,
                radius = r,
                center = Offset(cx, cy)
            )
        }
        
        // ============= HIGHLIGHT SPOTS (top light reflections) =============
        val highlights = listOf(
            Offset(-0.35f, -0.48f) to 0.10f,
            Offset(0.30f, -0.55f) to 0.08f,
            Offset(-0.58f, -0.15f) to 0.06f,
            Offset(0.52f, -0.10f) to 0.09f,
            Offset(0.05f, -0.35f) to 0.07f,
        )
        
        highlights.forEach { (offset, sizeFactor) ->
            val cx = center.x + radiusX * offset.x
            val cy = center.y + radiusY * offset.y
            val r = radiusX * sizeFactor
            
            val highlightGradient = Brush.radialGradient(
                colors = listOf(paleGreen, Color.Transparent),
                center = Offset(cx, cy),
                radius = r
            )
            
            drawCircle(
                brush = highlightGradient,
                radius = r,
                center = Offset(cx, cy)
            )
        }
        
        // ============= SMALL LEAF TEXTURE DOTS =============
        for (i in 0 until 12) {
            val angle = (30f * i + stableRandom(seed, i) * 20f)
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()
            val dist = radiusX * (0.25f + stableRandom(seed, i + 20) * 0.35f)
            val dotX = center.x + cos(angleRad) * dist
            val dotY = center.y - radiusY * 0.3f + sin(angleRad) * dist * 0.5f
            val dotRadius = radiusX * 0.025f * (0.6f + stableRandom(seed, i + 40) * 0.4f)
            
            drawCircle(
                color = darkGreen.copy(alpha = opacity * 0.3f),
                radius = dotRadius,
                center = Offset(dotX, dotY)
            )
        }
    }
}

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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sakura (Cherry Blossom) Tree Renderer - Modern Flat Illustration Style
 * Beautiful cloud-like pink blossom clusters with individual 5-petal flowers
 */

/**
 * Draw a single 5-petal sakura flower
 */
private fun DrawScope.drawSingleFlower(
    center: Offset,
    size: Float,
    opacity: Float,
    seed: Int
) {
    val petalCount = 5
    val pinkLight = Color(0xFFFCE4EC).copy(alpha = opacity)
    val pinkMid = Color(0xFFF8BBD9).copy(alpha = opacity)
    val pinkDark = Color(0xFFF48FB1).copy(alpha = opacity)
    
    for (i in 0 until petalCount) {
        val angle = (360f / petalCount) * i - 90f + stableRandom(seed, i) * 10f
        val angleRad = Math.toRadians(angle.toDouble()).toFloat()
        val petalDist = size * 0.55f
        val petalCenterX = center.x + cos(angleRad) * petalDist
        val petalCenterY = center.y + sin(angleRad) * petalDist
        
        // Petal with gradient (white center → pink edge)
        val petalGradient = Brush.radialGradient(
            colors = listOf(pinkLight, pinkMid, pinkDark),
            center = Offset(petalCenterX - size * 0.1f, petalCenterY - size * 0.1f),
            radius = size * 0.5f
        )
        
        // Elliptical petal shape
        rotate(angle, pivot = Offset(petalCenterX, petalCenterY)) {
            drawOval(
                brush = petalGradient,
                topLeft = Offset(petalCenterX - size * 0.35f, petalCenterY - size * 0.45f),
                size = Size(size * 0.7f, size * 0.9f)
            )
        }
    }
    
    // Yellow center
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFEB3B), Color(0xFFFFC107)),
            center = Offset(center.x - size * 0.03f, center.y - size * 0.03f),
            radius = size * 0.18f
        ),
        radius = size * 0.15f,
        center = center
    )
}

/**
 * Draw a cloud-like sakura blossom cluster with multiple layers
 */
fun DrawScope.drawSakuraBlossomCluster(
    center: Offset,
    radius: Float,
    color: Color,
    opacity: Float,
    sway: Float,
    seed: Int
) {
    rotate(sway, pivot = center) {
        // Color palette
        val pinkDark = Color(0xFFF48FB1).copy(alpha = opacity)
        val pinkMid = Color(0xFFF8BBD9).copy(alpha = opacity) 
        val pinkLight = Color(0xFFFCE4EC).copy(alpha = opacity)
        val white = Color.White.copy(alpha = opacity * 0.9f)
        
        // ============= LAYER 1: Back layer (darker pink) =============
        val backCount = 5
        for (i in 0 until backCount) {
            val angle = (360f / backCount) * i + stableRandom(seed, i) * 20f
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()
            val dist = radius * 0.4f
            val cx = center.x + cos(angleRad) * dist
            val cy = center.y + sin(angleRad) * dist * 0.7f
            val r = radius * 0.55f
            
            drawCircle(
                color = pinkDark,
                radius = r,
                center = Offset(cx, cy)
            )
        }
        
        // ============= LAYER 2: Mid layer (gradient pink clouds) =============
        val midCount = 7
        for (i in 0 until midCount) {
            val angle = (360f / midCount) * i + 25f + stableRandom(seed, i + 10) * 15f
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()
            val dist = radius * 0.45f
            val cx = center.x + cos(angleRad) * dist
            val cy = center.y + sin(angleRad) * dist * 0.6f
            val r = radius * (0.4f + stableRandom(seed, i + 20) * 0.15f)
            
            val gradient = Brush.radialGradient(
                colors = listOf(pinkLight, pinkMid, pinkDark),
                center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                radius = r * 1.2f
            )
            
            drawCircle(
                brush = gradient,
                radius = r,
                center = Offset(cx, cy)
            )
        }
        
        // ============= LAYER 3: Individual 5-petal flowers on top =============
        val flowerCount = 5
        for (i in 0 until flowerCount) {
            val angle = (360f / flowerCount) * i + stableRandom(seed, i + 30) * 40f
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()
            val dist = radius * 0.25f * (0.5f + stableRandom(seed, i + 40) * 0.5f)
            val fx = center.x + cos(angleRad) * dist
            val fy = center.y + sin(angleRad) * dist * 0.6f
            val flowerSize = radius * (0.22f + stableRandom(seed, i + 50) * 0.08f)
            
            drawSingleFlower(
                center = Offset(fx, fy),
                size = flowerSize,
                opacity = opacity,
                seed = seed + i * 100
            )
        }
        
        // ============= CENTER BRIGHT SPOT =============
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(white, pinkLight, Color.Transparent),
                center = center,
                radius = radius * 0.35f
            ),
            radius = radius * 0.3f,
            center = center
        )
        
        // ============= HIGHLIGHT REFLECTION =============
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = opacity * 0.4f),
                    Color.Transparent
                ),
                center = Offset(center.x - radius * 0.2f, center.y - radius * 0.25f),
                radius = radius * 0.25f
            ),
            topLeft = Offset(center.x - radius * 0.4f, center.y - radius * 0.45f),
            size = Size(radius * 0.5f, radius * 0.35f)
        )
    }
}

/**
 * Draw a single fallen petal on the ground
 */
fun DrawScope.drawFallenPetal(
    center: Offset,
    size: Float,
    rotation: Float,
    color: Color,
    opacity: Float
) {
    rotate(rotation, pivot = center) {
        val pinkLight = Color(0xFFFCE4EC).copy(alpha = opacity)
        val pinkMid = Color(0xFFF8BBD9).copy(alpha = opacity)
        
        // Elliptical petal shape
        val petalGradient = Brush.radialGradient(
            colors = listOf(pinkLight, pinkMid),
            center = Offset(center.x - size * 0.1f, center.y - size * 0.1f),
            radius = size
        )
        
        drawOval(
            brush = petalGradient,
            topLeft = Offset(center.x - size, center.y - size * 0.6f),
            size = Size(size * 2, size * 1.2f)
        )
    }
}

package com.projectapp.tempus.ui.garden.compose.trees

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import com.projectapp.tempus.ui.garden.compose.drawing.darken
import com.projectapp.tempus.ui.garden.compose.drawing.lighten
import com.projectapp.tempus.ui.garden.compose.stableRandom
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pine (Conifer) Tree Renderer - Professional Christmas Tree Style
 * Creates a dense, overlapping triangular silhouette like a real pine tree
 */

/**
 * Draw a conifer/pine tree with dense, overlapping triangular tiers
 */
fun DrawScope.drawConiferTree(
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
    
    val darkGreen = baseColor.darken(0.25f).copy(alpha = opacity)
    val midGreen = baseColor.copy(alpha = opacity)
    val lightGreen = baseColor.lighten(0.2f).copy(alpha = opacity)
    val snowHighlight = baseColor.lighten(0.35f).copy(alpha = opacity * 0.6f)
    
    // Tổng chiều cao tán cây - dense và compact hơn
    val totalHeight = canvasHeight * 0.55f * scale
    // Độ rộng đáy - tạo hình tam giác cân đẹp
    val baseWidth = canvasHeight * 0.4f * scale
    
    // Số tầng thực tế (nhiều hơn để chồng dày)
    val actualTiers = (tierCount * 1.5f).toInt().coerceAtLeast(4)
    
    rotate(sway, pivot = trunkTop) {
        // Vẽ từ dưới lên trên để các tầng trên đè lên
        for (tier in (actualTiers - 1) downTo 0) {
            val tierProgress = tier.toFloat() / actualTiers
            
            // Mỗi tầng overlap với tầng dưới ~40%
            val overlapFactor = 0.55f
            val tierY = trunkTop.y - totalHeight * tierProgress * overlapFactor
            
            // Chiều cao mỗi tầng - tầng dưới cao hơn
            val tierHeight = totalHeight * (0.35f - tierProgress * 0.08f) / actualTiers * 2.5f
            
            // Độ rộng tầng - tầng dưới rộng hơn (hình tam giác tổng thể)
            val tierWidth = baseWidth * (1f - tierProgress * 0.7f)
            
            val tierTop = tierY - tierHeight * 0.7f
            val tierBottom = tierY + tierHeight * 0.3f
            
            // Số spike cho mỗi cạnh - tăng theo tầng dưới
            val spikeCount = 4 + (actualTiers - tier)
            
            val tierPath = Path().apply {
                // Bắt đầu từ đỉnh
                moveTo(trunkTop.x, tierTop)
                
                // Cạnh phải với spikes nhọn hơn
                for (i in 0 until spikeCount) {
                    val t = (i + 1).toFloat() / spikeCount
                    val x = trunkTop.x + tierWidth * 0.5f * t
                    val y = tierTop + tierHeight * t * 0.9f
                    
                    // Spike nhọn ra ngoài
                    val spikeDepth = tierWidth * 0.12f * (1 - t * 0.5f)
                    val spikeOutX = x + spikeDepth
                    val spikeOutY = y - tierHeight * 0.04f
                    lineTo(spikeOutX, spikeOutY)
                    
                    // Về lại edge
                    lineTo(x, y)
                }
                
                // Đáy
                lineTo(trunkTop.x + tierWidth * 0.5f, tierBottom)
                lineTo(trunkTop.x - tierWidth * 0.5f, tierBottom)
                
                // Cạnh trái đối xứng
                for (i in spikeCount - 1 downTo 0) {
                    val t = (i + 1).toFloat() / spikeCount
                    val x = trunkTop.x - tierWidth * 0.5f * t
                    val y = tierTop + tierHeight * t * 0.9f
                    
                    lineTo(x, y)
                    
                    val spikeDepth = tierWidth * 0.12f * (1 - t * 0.5f)
                    val spikeOutX = x - spikeDepth
                    val spikeOutY = y - tierHeight * 0.04f
                    lineTo(spikeOutX, spikeOutY)
                }
                
                close()
            }
            
            // Gradient từ sáng (trên) xuống tối (dưới) + hiệu ứng 3D
            val tierColors = when {
                tier < actualTiers / 3 -> listOf(lightGreen, midGreen, darkGreen)
                tier < actualTiers * 2 / 3 -> listOf(midGreen, midGreen, darkGreen)
                else -> listOf(snowHighlight, lightGreen, midGreen)
            }
            
            drawPath(
                path = tierPath,
                brush = Brush.verticalGradient(
                    colors = tierColors,
                    startY = tierTop,
                    endY = tierBottom
                )
            )
            
            // Viền tinh tế
            drawPath(
                path = tierPath,
                color = darkGreen.copy(alpha = opacity * 0.25f),
                style = Stroke(width = 1f)
            )
        }
        
        // Ngôi sao/đỉnh nhỏ ở trên cùng
        val tipY = trunkTop.y - totalHeight * 0.65f
        drawCircle(
            color = lightGreen,
            radius = baseWidth * 0.04f,
            center = Offset(trunkTop.x, tipY)
        )
    }
}

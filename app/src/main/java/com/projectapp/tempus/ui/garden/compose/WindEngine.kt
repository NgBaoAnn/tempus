package com.projectapp.tempus.ui.garden.compose

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * WindEngine - Quản lý wind state cho procedural tree animation
 * 
 * Features:
 * - Idle sway: smooth, loop nhưng randomized phase
 * - Gust: burst ngẫu nhiên với easing
 * - Deterministic per-seed
 */
data class WindState(
    val idleValue: Float,     // 0..1 smooth noise
    val gustValue: Float,     // 0..1 gust envelope
    val gustActive: Boolean,
    val timeSeconds: Float
) {
    /**
     * Tính rotation cho từng part type
     * @param partType: "trunk", "branch", "leaf"
     * @param phaseOffset: unique per-part
     * @param heightFactor: 0..1, higher = more sway
     */
    fun getRotation(
        partType: String,
        phaseOffset: Float,
        heightFactor: Float = 0.5f
    ): Float {
        // STRONGER SWAY - tăng amplitude
        val config = when (partType) {
            "trunk" -> PartMotionConfig(frequency = 0.4f, amplitude = 5f)   // was 2f
            "branch" -> PartMotionConfig(frequency = 0.7f, amplitude = 12f)  // was 5f
            "leaf" -> PartMotionConfig(frequency = 1.2f, amplitude = 18f)    // was 8f
            else -> PartMotionConfig(frequency = 0.4f, amplitude = 5f)
        }
        
        val idleComponent = sin(timeSeconds * config.frequency * 2 * Math.PI.toFloat() + phaseOffset)
        val gustMultiplier = 1f + gustValue * 2.5f // Gust tăng amplitude 3.5x (was 1.5f)
        val heightMultiplier = 0.6f + heightFactor * 0.4f
        
        return config.amplitude * idleComponent * (0.6f + idleValue * 0.4f) * gustMultiplier * heightMultiplier
    }
    
    /**
     * Micro jitter cho leaves (high frequency, low amplitude)
     */
    fun getMicroJitter(seed: Int): Float {
        if (!gustActive && idleValue < 0.3f) return 0f
        val jitterPhase = seed * 0.1f + timeSeconds * 8f
        return sin(jitterPhase) * 1.5f * idleValue
    }
}

private data class PartMotionConfig(
    val frequency: Float,  // Hz
    val amplitude: Float   // degrees
)

/**
 * Composable để theo dõi wind state theo thời gian
 */
@Composable
fun rememberWindState(seed: Int = 0): State<WindState> {
    val windState = remember { mutableStateOf(WindState(0f, 0f, false, 0f)) }
    val random = remember { Random(seed) }
    
    LaunchedEffect(seed) {
        var time = 0f
        var nextGustTime = random.nextFloat() * 10f + 5f // 5-15s đầu tiên
        var gustStartTime = -1f
        val gustDuration = 2f
        
        while (true) {
            delay(16L) // ~60fps
            time += 0.016f
            
            // Idle noise: multi-octave sin để tạo organic feel
            val idle = (
                sin(time * 0.3f) * 0.3f +
                sin(time * 0.7f + 1.5f) * 0.3f +
                sin(time * 1.1f + 3.0f) * 0.4f
            ) * 0.5f + 0.5f // Normalize to 0..1
            
            // Gust logic
            var gustValue = 0f
            var gustActive = false
            
            if (time >= nextGustTime && gustStartTime < 0f) {
                gustStartTime = time
            }
            
            if (gustStartTime >= 0f) {
                val gustProgress = (time - gustStartTime) / gustDuration
                if (gustProgress <= 1f) {
                    gustActive = true
                    // Ease in-out envelope
                    gustValue = if (gustProgress < 0.3f) {
                        // Ease in
                        easeInQuad(gustProgress / 0.3f)
                    } else if (gustProgress < 0.7f) {
                        // Hold
                        1f
                    } else {
                        // Ease out
                        1f - easeOutQuad((gustProgress - 0.7f) / 0.3f)
                    }
                } else {
                    gustStartTime = -1f
                    nextGustTime = time + random.nextFloat() * 7f + 8f // 8-15s sau
                }
            }
            
            windState.value = WindState(
                idleValue = idle,
                gustValue = gustValue,
                gustActive = gustActive,
                timeSeconds = time
            )
        }
    }
    
    return windState
}

private fun easeInQuad(t: Float): Float = t * t
private fun easeOutQuad(t: Float): Float = t * (2 - t)

/**
 * Tạo stable random phase offset từ seed
 * Đảm bảo cùng seed sẽ cho cùng kết quả
 */
fun stablePhaseOffset(seed: Int, index: Int): Float {
    val combined = seed * 31 + index * 17
    return (combined % 628) / 100f // 0 to ~2π
}

/**
 * Tạo stable random value 0..1 từ seed
 */
fun stableRandom(seed: Int, index: Int): Float {
    val combined = seed * 31 + index * 17 + 13
    return ((combined % 1000) / 1000f).coerceIn(0f, 1f)
}

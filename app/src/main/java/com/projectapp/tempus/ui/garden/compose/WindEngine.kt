package com.projectapp.tempus.ui.garden.compose

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random


data class WindState(
    val idleValue: Float,     
    val gustValue: Float,     
    val gustActive: Boolean,
    val timeSeconds: Float
) {
    
    fun getRotation(
        partType: String,
        phaseOffset: Float,
        heightFactor: Float = 0.5f
    ): Float {
        
        val config = when (partType) {
            "trunk" -> PartMotionConfig(frequency = 0.4f, amplitude = 5f)   
            "branch" -> PartMotionConfig(frequency = 0.7f, amplitude = 12f)  
            "leaf" -> PartMotionConfig(frequency = 1.2f, amplitude = 18f)    
            else -> PartMotionConfig(frequency = 0.4f, amplitude = 5f)
        }
        
        val idleComponent = sin(timeSeconds * config.frequency * 2 * Math.PI.toFloat() + phaseOffset)
        val gustMultiplier = 1f + gustValue * 2.5f 
        val heightMultiplier = 0.6f + heightFactor * 0.4f
        
        return config.amplitude * idleComponent * (0.6f + idleValue * 0.4f) * gustMultiplier * heightMultiplier
    }
    
    
    fun getMicroJitter(seed: Int): Float {
        if (!gustActive && idleValue < 0.3f) return 0f
        val jitterPhase = seed * 0.1f + timeSeconds * 8f
        return sin(jitterPhase) * 1.5f * idleValue
    }
}

private data class PartMotionConfig(
    val frequency: Float,  
    val amplitude: Float   
)


@Composable
fun rememberWindState(seed: Int = 0): State<WindState> {
    val windState = remember { mutableStateOf(WindState(0f, 0f, false, 0f)) }
    val random = remember { Random(seed) }
    
    LaunchedEffect(seed) {
        var time = 0f
        var nextGustTime = random.nextFloat() * 10f + 5f 
        var gustStartTime = -1f
        val gustDuration = 2f
        
        while (true) {
            delay(16L) 
            time += 0.016f
            
            
            val idle = (
                sin(time * 0.3f) * 0.3f +
                sin(time * 0.7f + 1.5f) * 0.3f +
                sin(time * 1.1f + 3.0f) * 0.4f
            ) * 0.5f + 0.5f 
            
            
            var gustValue = 0f
            var gustActive = false
            
            if (time >= nextGustTime && gustStartTime < 0f) {
                gustStartTime = time
            }
            
            if (gustStartTime >= 0f) {
                val gustProgress = (time - gustStartTime) / gustDuration
                if (gustProgress <= 1f) {
                    gustActive = true
                    
                    gustValue = if (gustProgress < 0.3f) {
                        
                        easeInQuad(gustProgress / 0.3f)
                    } else if (gustProgress < 0.7f) {
                        
                        1f
                    } else {
                        
                        1f - easeOutQuad((gustProgress - 0.7f) / 0.3f)
                    }
                } else {
                    gustStartTime = -1f
                    nextGustTime = time + random.nextFloat() * 7f + 8f 
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


fun stablePhaseOffset(seed: Int, index: Int): Float {
    val combined = seed * 31 + index * 17
    return (combined % 628) / 100f 
}


fun stableRandom(seed: Int, index: Int): Float {
    val combined = seed * 31 + index * 17 + 13
    return ((combined % 1000) / 1000f).coerceIn(0f, 1f)
}

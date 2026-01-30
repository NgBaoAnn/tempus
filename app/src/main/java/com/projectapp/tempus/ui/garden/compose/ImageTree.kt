package com.projectapp.tempus.ui.garden.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.projectapp.tempus.R
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType


@Composable
fun ImageTree(
    state: TreeState,
    type: TreeType,
    modifier: Modifier = Modifier,
    size: TreeImageSize = TreeImageSize.MEDIUM,
    enableAnimation: Boolean = true
) {
    val imageRes = getTreeImageRes(state)
    
    
    val infiniteTransition = rememberInfiniteTransition(label = "treeSway")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val sizeModifier = when (size) {
        TreeImageSize.SMALL -> Modifier.size(70.dp)
        TreeImageSize.MEDIUM -> Modifier.size(100.dp)
        TreeImageSize.LARGE -> Modifier.size(150.dp)
        TreeImageSize.XLARGE -> Modifier.size(180.dp)
    }
    
    Box(
        modifier = modifier.then(sizeModifier),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Tree ${state.displayName}",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (enableAnimation && state != TreeState.DEAD) {
                        
                        transformOrigin = TransformOrigin(0.5f, 1f)
                        rotationZ = rotation
                    }
                }
        )
    }
}

enum class TreeImageSize {
    SMALL,   
    MEDIUM,  
    LARGE,   
    XLARGE   
}


private fun getTreeImageRes(state: TreeState): Int {
    return when (state) {
        TreeState.SEED -> R.drawable.tree_seed
        TreeState.SPROUT -> R.drawable.tree_sprout
        TreeState.SAPLING -> R.drawable.tree_sapling
        TreeState.TREE -> R.drawable.tree_full
        TreeState.DEAD -> R.drawable.tree_dead
    }
}

package com.projectapp.tempus.ui.garden.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.projectapp.tempus.R
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType

/**
 * LottieTree - Hiển thị cây với Lottie animation cute style
 * Sử dụng local resources từ res/raw
 */
@Composable
fun LottieTree(
    state: TreeState,
    type: TreeType,
    modifier: Modifier = Modifier,
    size: LottieTreeSize = LottieTreeSize.MEDIUM
) {
    val animationRes = getTreeAnimationRes(state)
    
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationRes)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = state != TreeState.DEAD
    )
    
    val sizeModifier = when (size) {
        LottieTreeSize.SMALL -> Modifier.size(60.dp)
        LottieTreeSize.MEDIUM -> Modifier.size(100.dp)
        LottieTreeSize.LARGE -> Modifier.size(150.dp)
        LottieTreeSize.XLARGE -> Modifier.size(200.dp)
    }
    
    Box(
        modifier = modifier.then(sizeModifier),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}

enum class LottieTreeSize {
    SMALL,
    MEDIUM,
    LARGE,
    XLARGE
}

/**
 * Lấy animation resource dựa trên state của cây
 */
private fun getTreeAnimationRes(state: TreeState): Int {
    return when (state) {
        TreeState.SEED -> R.raw.tree_seed
        TreeState.SPROUT -> R.raw.tree_sprout
        TreeState.SAPLING -> R.raw.tree_sapling
        TreeState.TREE -> R.raw.tree_full
        TreeState.DEAD -> R.raw.tree_dead
    }
}

package com.projectapp.tempus.ui.garden.lottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.*
import com.projectapp.tempus.R
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType

/**
 * Composable hiển thị cây với Lottie animation
 * 
 * Sử dụng trong Compose UI hoặc thông qua ComposeView trong XML layout
 * 
 * Available animations in res/raw:
 * - tree_seed.json
 * - tree_sprout.json
 * - tree_sapling.json
 * - tree_full.json
 * - tree_dead.json
 */
@Composable
fun LottieTreeView(
    state: TreeState,
    type: TreeType,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    val lottieRes = getLottieResource(state)
    
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(lottieRes)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = when (state) {
            TreeState.TREE -> LottieConstants.IterateForever  // Tree luôn animate
            TreeState.DEAD -> 1  // Dead chỉ play 1 lần
            else -> LottieConstants.IterateForever  // Các state khác loop
        },
        isPlaying = isPlaying
    )
    
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

/**
 * Lấy resource ID của Lottie animation theo state
 */
private fun getLottieResource(state: TreeState): Int {
    return when (state) {
        TreeState.SEED -> R.raw.tree_seed
        TreeState.SPROUT -> R.raw.tree_sprout
        TreeState.SAPLING -> R.raw.tree_sapling
        TreeState.TREE -> R.raw.tree_full
        TreeState.DEAD -> R.raw.tree_dead
    }
}

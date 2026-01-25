package com.projectapp.tempus.ui.garden

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.projectapp.tempus.R
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType

/**
 * Custom View hiển thị cây với hình ảnh drawable
 * 
 * NOTE: Lottie animations disabled do JSON file issues.
 * Using static drawables with scale animations instead.
 */
class TreeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val imageView: ImageView
    private var currentState: TreeState = TreeState.SEED
    private var currentType: TreeType = TreeType.OAK
    private var onGrowthComplete: (() -> Unit)? = null
    
    init {
        imageView = ImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        addView(imageView)
        setState(TreeState.SEED, TreeType.OAK, animate = false)
    }
    
    /**
     * Set trạng thái cây
     * @param state Trạng thái mới
     * @param type Loại cây
     * @param animate True để play animation chuyển đổi
     */
    fun setState(state: TreeState, type: TreeType = currentType, animate: Boolean = true) {
        val isLevelUp = state.ordinal > currentState.ordinal && currentState != TreeState.DEAD
        currentState = state
        currentType = type
        
        val drawableRes = getDrawableRes(type, state)
        imageView.setImageResource(drawableRes)
        
        if (animate && isLevelUp) {
            // Scale animation for level up
            imageView.scaleX = 0.5f
            imageView.scaleY = 0.5f
            imageView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .withEndAction {
                    onGrowthComplete?.invoke()
                }
                .start()
        }
    }
    
    /**
     * Lấy drawable resource theo type và state
     */
    private fun getDrawableRes(type: TreeType, state: TreeState): Int {
        return when (state) {
            TreeState.SEED -> R.drawable.ic_seed
            TreeState.SPROUT -> when (type) {
                TreeType.PINE -> R.drawable.ic_pine_sprout
                TreeType.BAMBOO -> R.drawable.ic_bamboo_sprout
                TreeType.PALM -> R.drawable.ic_palm_sprout
                else -> R.drawable.ic_sprout
            }
            TreeState.SAPLING -> when (type) {
                TreeType.OAK -> R.drawable.ic_oak_sapling
                TreeType.PINE -> R.drawable.ic_pine_sapling
                TreeType.SAKURA -> R.drawable.ic_sakura_sapling
                TreeType.BAMBOO -> R.drawable.ic_bamboo_sapling
                TreeType.PALM -> R.drawable.ic_palm_sapling
                TreeType.COCONUT -> R.drawable.ic_palm_sapling  // Reuse palm
                TreeType.APPLE -> R.drawable.ic_apple_sapling
            }
            TreeState.TREE -> when (type) {
                TreeType.OAK -> R.drawable.ic_oak_tree
                TreeType.PINE -> R.drawable.ic_pine_tree
                TreeType.SAKURA -> R.drawable.ic_sakura_tree
                TreeType.BAMBOO -> R.drawable.ic_bamboo_tree
                TreeType.PALM -> R.drawable.ic_palm_tree
                TreeType.COCONUT -> R.drawable.ic_palm_tree  // Reuse palm
                TreeType.APPLE -> R.drawable.ic_apple_tree
            }
            TreeState.DEAD -> R.drawable.ic_tree_dead
        }
    }
    
    /**
     * Play animation khi cây lên level
     */
    fun playGrowthAnimation() {
        imageView.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                imageView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .withEndAction {
                        onGrowthComplete?.invoke()
                    }
                    .start()
            }
            .start()
    }
    
    /**
     * Play animation khi cây chết
     */
    fun playDeathAnimation() {
        currentState = TreeState.DEAD
        imageView.setImageResource(R.drawable.ic_tree_dead)
        imageView.alpha = 1f
        imageView.animate()
            .alpha(0.5f)
            .setDuration(500)
            .start()
    }
    
    fun setOnGrowthCompleteListener(listener: () -> Unit) {
        onGrowthComplete = listener
    }
    
    fun getCurrentState(): TreeState = currentState
    
    fun pauseAnimation() {}
    
    fun resumeAnimation() {}
}

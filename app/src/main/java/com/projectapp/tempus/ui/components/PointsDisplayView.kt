package com.projectapp.tempus.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.projectapp.tempus.R


class PointsDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val tvPoints: TextView
    private val tvStreak: TextView
    private val ivPointsIcon: ImageView
    
    private var currentPoints: Int = 0
    
    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(16, 8, 16, 8)
        
        
        ivPointsIcon = ImageView(context).apply {
            layoutParams = LayoutParams(48, 48).apply {
                marginEnd = 8
            }
            setImageResource(R.drawable.ic_points_star)
            setColorFilter(Color.parseColor("#FFD700"))  
        }
        addView(ivPointsIcon)
        
        
        tvPoints = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginEnd = 16
            }
            textSize = 18f
            setTextColor(Color.parseColor("#1C1C1E"))
            text = "0"
        }
        addView(tvPoints)
        
        
        tvStreak = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            textSize = 16f
            setTextColor(Color.parseColor("#FF6B35"))  
            visibility = View.GONE
        }
        addView(tvStreak)
    }
    
    
    fun setPoints(points: Int, animate: Boolean = true) {
        if (animate && currentPoints != points) {
            ValueAnimator.ofInt(currentPoints, points).apply {
                duration = 500
                interpolator = OvershootInterpolator()
                addUpdateListener { 
                    tvPoints.text = it.animatedValue.toString()
                }
                start()
            }
        } else {
            tvPoints.text = points.toString()
        }
        currentPoints = points
    }
    
    
    fun setStreak(days: Int) {
        if (days > 0) {
            tvStreak.text = "🔥 $days"
            tvStreak.visibility = View.VISIBLE
        } else {
            tvStreak.visibility = View.GONE
        }
    }
    
    
    fun showEarnedPoints(points: Int) {
        val floatingText = TextView(context).apply {
            text = if (points > 0) "+$points" else "$points"
            textSize = 20f
            setTextColor(if (points > 0) Color.parseColor("#34C759") else Color.parseColor("#FF3B30"))
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
        
        addView(floatingText)
        
        floatingText.animate()
            .translationY(-80f)
            .alpha(0f)
            .setDuration(1000)
            .setInterpolator(OvershootInterpolator())
            .withEndAction { removeView(floatingText) }
            .start()
    }
    
    
    fun playMilestoneAnimation() {
        this.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                this.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
}

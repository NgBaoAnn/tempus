package com.projectapp.tempus.service.focus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.R

/**
 * Overlay window that appears when a blocked app is detected
 * Shows a warning message and option to return to Tempus
 */
class FocusBlockingOverlay(private val context: Context) {
    
    private val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    
    private var overlayView: View? = null
    private var isShowing = false
    
    private val layoutParams: WindowManager.LayoutParams by lazy {
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
    }
    
    /**
     * Show the blocking overlay with app name and remaining time
     */
    @SuppressLint("InflateParams")
    fun show(blockedAppName: String, remainingTimeText: String) {
        if (isShowing) {
            // Update existing overlay
            updateContent(blockedAppName, remainingTimeText)
            return
        }
        
        try {
            overlayView = LayoutInflater.from(context).inflate(R.layout.overlay_focus_blocking, null)
            
            setupOverlayContent(blockedAppName, remainingTimeText)
            
            windowManager.addView(overlayView, layoutParams)
            isShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupOverlayContent(blockedAppName: String, remainingTimeText: String) {
        overlayView?.apply {
            findViewById<TextView>(R.id.tvBlockedAppName)?.text = blockedAppName
            findViewById<TextView>(R.id.tvRemainingTime)?.text = remainingTimeText
            
            findViewById<Button>(R.id.btnReturnToApp)?.setOnClickListener {
                // Launch Tempus app
                val intent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(intent)
                hide()
            }
            
            findViewById<Button>(R.id.btnDismiss)?.setOnClickListener {
                hide()
            }
        }
    }
    
    private fun updateContent(blockedAppName: String, remainingTimeText: String) {
        overlayView?.apply {
            findViewById<TextView>(R.id.tvBlockedAppName)?.text = blockedAppName
            findViewById<TextView>(R.id.tvRemainingTime)?.text = remainingTimeText
        }
    }
    
    /**
     * Hide the overlay
     */
    fun hide() {
        if (!isShowing) return
        
        try {
            overlayView?.let {
                windowManager.removeView(it)
            }
            overlayView = null
            isShowing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Check if overlay is currently visible
     */
    fun isVisible(): Boolean = isShowing
}

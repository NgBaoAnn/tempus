package com.projectapp.tempus.ui.focus

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectapp.tempus.ui.theme.TempusTheme
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ===== DESIGN TOKENS =====
private object FocusDesignTokens {
    // Colors - Dark OLED + Glassmorphism
    val backgroundDark = Color(0xFF000000)
    val backgroundGradientStart = Color(0xFF0A0E1A)
    val backgroundGradientEnd = Color(0xFF0D1B2A)
    
    val surfaceGlass = Color.White.copy(alpha = 0.05f)
    val surfaceGlassElevated = Color.White.copy(alpha = 0.08f)
    val borderGlass = Color.White.copy(alpha = 0.1f)
    
    val primaryBlue = Color(0xFF3B82F6)
    val primaryBlueGlow = Color(0xFF60A5FA)
    val accentOrange = Color(0xFFF97316)
    val accentGreen = Color(0xFF10B981)
    val accentRed = Color(0xFFEF4444)
    
    val textPrimary = Color.White
    val textSecondary = Color.White.copy(alpha = 0.7f)
    val textMuted = Color.White.copy(alpha = 0.4f)
    
    // Typography
    val displayLarge = TextStyle(
        fontSize = 64.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-2).sp
    )
    val headlineMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    )
    val labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 2.sp
    )
    
    // Spacing
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 16.dp
    val spacingLg = 24.dp
    val spacingXl = 32.dp
    val spacing2Xl = 48.dp
    val spacing3Xl = 64.dp
    
    // Animation
    const val animDurationFast = 150
    const val animDurationMedium = 300
    const val animDurationSlow = 600
}

/**
 * Premium Focus Lock Activity with modern design
 */
class FocusLockActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val EXTRA_TIMER_COLOR = "timer_color"
        
        fun start(context: Context, durationSeconds: Long, timerColor: Int = 0xFF3B82F6.toInt()) {
            val intent = Intent(context, FocusLockActivity::class.java).apply {
                putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
                putExtra(EXTRA_TIMER_COLOR, timerColor)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
        
        @Volatile
        private var currentInstance: FocusLockActivity? = null
        
        fun finishIfRunning() {
            currentInstance?.finish()
        }
        
        fun sendTimerUpdate(context: Context, remainingSeconds: Long) {}
        
        fun sendTimerFinish(context: Context) {
            finishIfRunning()
        }
    }
    
    private var remainingSeconds by mutableLongStateOf(0L)
    private var totalSeconds by mutableLongStateOf(0L)
    private var timerColor by mutableStateOf(FocusDesignTokens.primaryBlue)
    private var showUnlockDialog by mutableStateOf(false)
    
    private var countDownTimer: CountDownTimer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        currentInstance = this
        setupWindowFlags()
        
        totalSeconds = intent.getLongExtra(EXTRA_DURATION_SECONDS, 0)
        remainingSeconds = totalSeconds
        val colorInt = intent.getIntExtra(EXTRA_TIMER_COLOR, 0xFF3B82F6.toInt())
        timerColor = Color(colorInt)
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showUnlockDialog = true
            }
        })
        
        startCountdown()
        
        setContent {
            TempusTheme {
                PremiumFocusLockScreen(
                    remainingSeconds = remainingSeconds,
                    totalSeconds = totalSeconds,
                    showUnlockDialog = showUnlockDialog,
                    onDismissDialog = { showUnlockDialog = false },
                    onConfirmUnlock = { finish() }
                )
            }
        }
    }
    
    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(totalSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = millisUntilFinished / 1000
            }
            
            override fun onFinish() {
                remainingSeconds = 0
                finish()
            }
        }.start()
    }
    
    private fun setupWindowFlags() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        currentInstance = null
    }
}

// ===== MAIN SCREEN =====
@Composable
fun PremiumFocusLockScreen(
    remainingSeconds: Long,
    totalSeconds: Long,
    showUnlockDialog: Boolean,
    onDismissDialog: () -> Unit,
    onConfirmUnlock: () -> Unit
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        FocusDesignTokens.backgroundGradientStart,
                        FocusDesignTokens.backgroundGradientEnd,
                        FocusDesignTokens.backgroundDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow effect
        AmbientGlowBackground()
        
        // Floating particles
        FloatingParticles()
        
        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(FocusDesignTokens.spacingXl)
        ) {
            // Status badge
            StatusBadge()
            
            Spacer(modifier = Modifier.height(FocusDesignTokens.spacing2Xl))
            
            // Timer display
            AnimatedTimerRing(
                progress = progress,
                remainingSeconds = remainingSeconds
            )
            
            Spacer(modifier = Modifier.height(FocusDesignTokens.spacing2Xl))
            
            // Motivational message
            MotivationalSection(progress = progress)
            
            Spacer(modifier = Modifier.height(FocusDesignTokens.spacing3Xl))
            
            // Unlock hint
            Text(
                text = "TAP BACK TO UNLOCK",
                style = FocusDesignTokens.labelSmall,
                color = FocusDesignTokens.textMuted
            )
        }
        
        // Unlock dialog
        if (showUnlockDialog) {
            PremiumUnlockDialog(
                onDismiss = onDismissDialog,
                onConfirm = onConfirmUnlock
            )
        }
    }
}

// ===== COMPONENTS =====

@Composable
private fun StatusBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(FocusDesignTokens.surfaceGlass)
            .border(
                width = 1.dp,
                color = FocusDesignTokens.borderGlass,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Live indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(pulseAlpha)
                .background(FocusDesignTokens.accentGreen, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = FocusDesignTokens.textPrimary,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "FOCUS MODE",
            style = FocusDesignTokens.labelSmall,
            color = FocusDesignTokens.textPrimary
        )
    }
}

@Composable
private fun AnimatedTimerRing(
    progress: Float,
    remainingSeconds: Long
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(300.dp)
    ) {
        // Outer glow
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .blur(40.dp)
        ) {
            drawCircle(
                color = FocusDesignTokens.primaryBlue.copy(alpha = glowIntensity * 0.5f),
                radius = size.minDimension / 2.5f
            )
        }
        
        // Decorative outer ring
        Canvas(modifier = Modifier.size(280.dp)) {
            val dotCount = 60
            val radius = size.minDimension / 2 - 8
            
            for (i in 0 until dotCount) {
                val angle = (i * 360f / dotCount + rotationAngle) * (PI / 180f)
                val x = center.x + radius * cos(angle).toFloat()
                val y = center.y + radius * sin(angle).toFloat()
                val dotProgress = i.toFloat() / dotCount
                
                drawCircle(
                    color = if (dotProgress <= progress)
                        FocusDesignTokens.primaryBlue.copy(alpha = 0.6f)
                    else
                        FocusDesignTokens.textMuted.copy(alpha = 0.2f),
                    radius = 2f,
                    center = Offset(x, y)
                )
            }
        }
        
        // Main progress ring
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 6.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Track
            drawCircle(
                color = FocusDesignTokens.surfaceGlass,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc with gradient
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        FocusDesignTokens.primaryBlue,
                        FocusDesignTokens.primaryBlueGlow,
                        FocusDesignTokens.primaryBlue
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(
                    size.width - strokeWidth,
                    size.height - strokeWidth
                )
            )
        }
        
        // Glass inner circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(FocusDesignTokens.surfaceGlassElevated)
                .border(
                    width = 1.dp,
                    color = FocusDesignTokens.borderGlass,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(remainingSeconds),
                    style = FocusDesignTokens.displayLarge,
                    color = FocusDesignTokens.textPrimary
                )
                
                Text(
                    text = "remaining",
                    style = FocusDesignTokens.bodyMedium,
                    color = FocusDesignTokens.textMuted
                )
            }
        }
    }
}

@Composable
private fun MotivationalSection(progress: Float) {
    val (message, subMessage) = getMotivationalMessages(progress)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = message,
            style = FocusDesignTokens.headlineMedium,
            color = FocusDesignTokens.textPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(FocusDesignTokens.spacingSm))
        
        Text(
            text = subMessage,
            style = FocusDesignTokens.bodyMedium,
            color = FocusDesignTokens.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AmbientGlowBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Top-left glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    FocusDesignTokens.primaryBlue.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.2f, size.height * 0.2f + offset1),
                radius = size.width * 0.5f
            ),
            center = Offset(size.width * 0.2f, size.height * 0.2f + offset1),
            radius = size.width * 0.5f
        )
        
        // Bottom-right glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    FocusDesignTokens.accentGreen.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.8f, size.height * 0.7f - offset1 * 0.5f),
                radius = size.width * 0.4f
            ),
            center = Offset(size.width * 0.8f, size.height * 0.7f - offset1 * 0.5f),
            radius = size.width * 0.4f
        )
    }
}

@Composable
private fun FloatingParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    val particles = remember {
        List(30) {
            ParticleData(
                x = (Math.random() * 1000).toFloat(),
                y = (Math.random() * 2000).toFloat(),
                size = (1 + Math.random() * 3).toFloat(),
                speed = (0.5f + Math.random() * 1.5f).toFloat(),
                opacity = (0.1f + Math.random() * 0.3f).toFloat()
            )
        }
    }
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val y = (p.y + time * p.speed) % size.height
            val x = (p.x + sin(time * 0.01f + p.y * 0.01f) * 20) % size.width
            
            drawCircle(
                color = Color.White.copy(alpha = p.opacity),
                radius = p.size,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun PremiumUnlockDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FocusDesignTokens.backgroundGradientEnd,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(FocusDesignTokens.accentOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = FocusDesignTokens.accentOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                "End Focus Session?",
                color = FocusDesignTokens.textPrimary,
                style = FocusDesignTokens.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                "You're making great progress! Are you sure you want to unlock now?",
                color = FocusDesignTokens.textSecondary,
                style = FocusDesignTokens.bodyMedium,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "End Session",
                    color = FocusDesignTokens.accentRed,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FocusDesignTokens.primaryBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Keep Focused", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

// ===== UTILITIES =====

private data class ParticleData(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val opacity: Float
)

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    
    return if (h > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", m, s)
    }
}

private fun getMotivationalMessages(progress: Float): Pair<String, String> {
    return when {
        progress > 0.9f -> "Session Started" to "Deep work begins now"
        progress > 0.75f -> "Building momentum" to "Stay in the zone"
        progress > 0.5f -> "Halfway there" to "You're doing great"
        progress > 0.25f -> "Almost done" to "Push through!"
        progress > 0.1f -> "Final stretch" to "Just a bit more"
        else -> "Excellent work" to "Session complete soon"
    }
}

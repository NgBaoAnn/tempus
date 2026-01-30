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
    private var timerColor by mutableStateOf(Color(0xFF3B82F6)) 
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
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        
        AmbientGlowBackground()
        
        
        FloatingParticles()
        
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            
            StatusBadge()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            
            AnimatedTimerRing(
                progress = progress,
                remainingSeconds = remainingSeconds
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            
            MotivationalSection(progress = progress)
            
            Spacer(modifier = Modifier.height(64.dp))
            
            
            Text(
                text = "NHẤN BACK ĐỂ MỞ KHOÁ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        
        
        if (showUnlockDialog) {
            PremiumUnlockDialog(
                onDismiss = onDismissDialog,
                onConfirm = onConfirmUnlock
            )
        }
    }
}


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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(pulseAlpha)
                .background(Color(0xFF10B981), CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "FOCUS MODE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurface
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
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(300.dp)
    ) {
        
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .blur(40.dp)
        ) {
            drawCircle(
                color = primaryColor.copy(alpha = glowIntensity * 0.5f),
                radius = size.minDimension / 2.5f
            )
        }
        
        
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
                        primaryColor.copy(alpha = 0.6f)
                    else
                        mutedColor.copy(alpha = 0.2f),
                    radius = 2f,
                    center = Offset(x, y)
                )
            }
        }
        
        
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 6.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            
            drawCircle(
                color = surfaceColor.copy(alpha = 0.1f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
            
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor,
                        primaryColor.copy(alpha = 0.8f),
                        primaryColor
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
        
        
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(remainingSeconds),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-2).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "remaining",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = subMessage,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    
    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color1.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.2f, size.height * 0.2f + offset1),
                radius = size.width * 0.5f
            ),
            center = Offset(size.width * 0.2f, size.height * 0.2f + offset1),
            radius = size.width * 0.5f
        )
        
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color2.copy(alpha = 0.1f),
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
    
    val particleColor = MaterialTheme.colorScheme.primary
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val y = (p.y + time * p.speed) % size.height
            val x = (p.x + sin(time * 0.01f + p.y * 0.01f) * 20) % size.width
            
            drawCircle(
                color = particleColor.copy(alpha = p.opacity * 0.5f),
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
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                "Kết thúc phiên tập trung?",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                "Bạn đang làm rất tốt! Bạn có chắc muốn mở khoá ngay bây giờ?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Kết thúc",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tiếp tục tập trung", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}


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
        progress > 0.9f -> "Bắt đầu phiên" to "Làm việc sâu bắt đầu"
        progress > 0.75f -> "Tạo đà tốt" to "Giữ vững trạng thái"
        progress > 0.5f -> "Đã được nửa" to "Bạn đang làm rất tốt"
        progress > 0.25f -> "Sắp xong rồi" to "Cố lên nào!"
        progress > 0.1f -> "Chặng cuối" to "Chỉ còn một chút"
        else -> "Xuất sắc" to "Sắp hoàn thành phiên"
    }
}

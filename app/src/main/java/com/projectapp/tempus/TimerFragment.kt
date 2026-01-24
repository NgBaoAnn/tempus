package com.projectapp.tempus

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.projectapp.tempus.data.gamification.SupabaseGamificationRepository
import com.projectapp.tempus.domain.model.PointAction
import com.projectapp.tempus.domain.usecase.PointsManager
import com.projectapp.tempus.receiver.TimerActionReceiver
import com.projectapp.tempus.ui.timer.compose.TimerColors
import com.projectapp.tempus.ui.timer.compose.TimerScreen
import com.projectapp.tempus.ui.timer.compose.TimerState
import com.projectapp.tempus.util.TimerEventBus
import com.projectapp.tempus.util.TimerNotificationHelper
import com.projectapp.tempus.data.timer.TimerPreferences
import kotlinx.coroutines.launch

class TimerFragment : Fragment() {

    // Timer state
    private var timerState by mutableStateOf(TimerState.SETUP)
    private var hours by mutableIntStateOf(0)
    private var minutes by mutableIntStateOf(15)
    private var selectedQuickIndex by mutableIntStateOf(4) // Custom by default
    private var selectedColor by mutableStateOf(TimerColors.TimerGreen)
    private var secondsRemaining by mutableLongStateOf(0L)
    private var totalSeconds by mutableLongStateOf(0L)
    
    private var countDownTimer: CountDownTimer? = null
    
    // Gamification
    private lateinit var pointsManager: PointsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        
        // Listen to TimerEventBus
        lifecycleScope.launch {
            TimerEventBus.events.collect { event ->
                Log.d("TimerFragment", "Received event: $event")
                when (event) {
                    TimerEventBus.TimerEvent.PAUSE -> {
                        Log.d("TimerFragment", "Calling pauseTimer()")
                        pauseTimer()
                    }
                    TimerEventBus.TimerEvent.RESUME -> {
                        Log.d("TimerFragment", "Calling resumeTimer()")
                        resumeTimer()
                    }
                    TimerEventBus.TimerEvent.STOP -> {
                        Log.d("TimerFragment", "Calling cancelTimer()")
                        cancelTimer()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize gamification
        val repository = SupabaseGamificationRepository()
        pointsManager = PointsManager(repository)
        
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                TimerScreen(
                    timerState = timerState,
                    hours = hours,
                    minutes = minutes,
                    secondsRemaining = secondsRemaining,
                    totalSeconds = totalSeconds,
                    selectedQuickIndex = selectedQuickIndex,
                    selectedColor = selectedColor,
                    onHoursChange = { hours = it },
                    onMinutesChange = { minutes = it },
                    onQuickSelect = { index ->
                        selectedQuickIndex = index
                        when (index) {
                            0 -> { hours = 0; minutes = 1 }
                            1 -> { hours = 0; minutes = 5 }
                            2 -> { hours = 0; minutes = 30 }
                            3 -> { hours = 1; minutes = 0 }
                            // 4 = Custom, keep current values
                        }
                    },
                    onColorSelect = { selectedColor = it },
                    onStart = { startTimer() },
                    onPause = { pauseTimer() },
                    onResume = { resumeTimer() },
                    onCancel = { cancelTimer() },
                    onReset = { 
                        selectedQuickIndex = 4
                        hours = 0
                        minutes = 15
                    }
                )
            }
        }
    }

    private fun startTimer() {
        if (hours == 0 && minutes == 0) {
            Toast.makeText(requireContext(), "Vui lòng chọn thời gian lớn hơn 0", Toast.LENGTH_SHORT).show()
            return
        }
        
        totalSeconds = (hours * 3600 + minutes * 60).toLong()
        secondsRemaining = totalSeconds
        timerState = TimerState.RUNNING
        
        // Show notification
        TimerNotificationHelper.showTimerNotification(
            requireContext(),
            TimerNotificationHelper.formatTime(secondsRemaining),
            false
        )
        
        startCountDown()
    }
    
    private fun startCountDown() {
        countDownTimer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                
                // Update notification
                TimerNotificationHelper.showTimerNotification(
                    requireContext(),
                    TimerNotificationHelper.formatTime(secondsRemaining),
                    false
                )
            }

            override fun onFinish() {
                secondsRemaining = 0
                timerState = TimerState.SETUP
                
                // Cancel notification
                TimerNotificationHelper.cancelNotification(requireContext())
                
                // 🎮 Award Pomodoro points when timer completes
                viewLifecycleOwner.lifecycleScope.launch {
                    val earnedPoints = pointsManager.earnPoints(PointAction.POMODORO_COMPLETE)
                    pointsManager.updateStreak()
                    
                    Toast.makeText(
                        requireContext(),
                        "🎉 Hoàn thành! +$earnedPoints điểm",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerState = TimerState.PAUSED
        
        // Update notification to show Resume button
        TimerNotificationHelper.showTimerNotification(
            requireContext(),
            TimerNotificationHelper.formatTime(secondsRemaining),
            true
        )
    }
    
    private fun resumeTimer() {
        timerState = TimerState.RUNNING
        
        // Update notification to show Pause button
        TimerNotificationHelper.showTimerNotification(
            requireContext(),
            TimerNotificationHelper.formatTime(secondsRemaining),
            false
        )
        
        startCountDown()
    }

    private fun cancelTimer() {
        countDownTimer?.cancel()
        timerState = TimerState.SETUP
        secondsRemaining = 0
        
        // Cancel notification
        TimerNotificationHelper.cancelNotification(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TimerFragment", "onDestroy()")
        countDownTimer?.cancel()
        TimerNotificationHelper.cancelNotification(requireContext())
    }
}


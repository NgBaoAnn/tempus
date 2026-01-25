package com.projectapp.tempus

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.projectapp.tempus.data.gamification.SupabaseGamificationRepository
import com.projectapp.tempus.domain.model.PointAction
import com.projectapp.tempus.domain.usecase.PointsManager
import com.projectapp.tempus.ui.components.PointsNotification
import com.projectapp.tempus.ui.components.PointsNotificationState
import com.projectapp.tempus.ui.timer.compose.TimerColors
import com.projectapp.tempus.ui.timer.compose.TimerScreen
import com.projectapp.tempus.ui.timer.compose.TimerState
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
    
    // Points notification state
    private var pointsNotification by mutableStateOf(PointsNotificationState())

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
                Box(modifier = Modifier.fillMaxSize()) {
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
                        },
                        onNotesClick = {
                            findNavController().navigate(R.id.action_timerFragment_to_notesFragment)
                        }
                    )
                    
                    // Points earned notification overlay
                    if (pointsNotification.show) {
                        PointsNotification(
                            points = pointsNotification.points,
                            reason = pointsNotification.reason,
                            onDismiss = {
                                pointsNotification = PointsNotificationState()
                            }
                        )
                    }
                }
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
        
        startCountDown()
    }
    
    private fun startCountDown() {
        countDownTimer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
            }

            override fun onFinish() {
                secondsRemaining = 0
                timerState = TimerState.SETUP
                
                // 🎮 Award Pomodoro points based on focus duration
                viewLifecycleOwner.lifecycleScope.launch {
                    // Calculate focus minutes from totalSeconds
                    val focusMinutes = (totalSeconds / 60).toInt().coerceAtLeast(1)
                    
                    // Award 1 point per minute
                    val earnedPoints = pointsManager.earnPomodoroPoints(focusMinutes)
                    pointsManager.updateStreak()
                    
                    // Show visual notification with minutes info
                    pointsNotification = PointsNotificationState(
                        show = true,
                        points = earnedPoints,
                        reason = "Tập trung $focusMinutes phút"
                    )
                }
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerState = TimerState.PAUSED
    }
    
    private fun resumeTimer() {
        timerState = TimerState.RUNNING
        startCountDown()
    }

    private fun cancelTimer() {
        countDownTimer?.cancel()
        timerState = TimerState.SETUP
        secondsRemaining = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}

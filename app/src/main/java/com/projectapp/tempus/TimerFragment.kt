package com.projectapp.tempus

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.projectapp.tempus.databinding.FragmentTimerBinding
import java.util.Calendar
import java.util.Locale

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var isTimerRunning = false
    private var totalTimeInMillis: Long = 0
    private var selectedColor: Int = Color.parseColor("#4CD964") // Mặc định xanh lá

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNumberPickers()
        setupToggleGroup()
        setupColorSelection()

        // Nút Start - Chuyển sang màn hình chạy
        binding.btnStart.setOnClickListener {
            val hours = binding.npHour.value
            val minutes = binding.npMinute.value
            
            if (hours == 0 && minutes == 0) {
                Toast.makeText(requireContext(), "Vui lòng chọn thời gian lớn hơn 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            totalTimeInMillis = (hours * 3600 + minutes * 60) * 1000L
            timeLeftInMillis = totalTimeInMillis
            startTimer()
            showRunningCard(true)
        }

        // Nút Pause / Resume
        binding.btnPauseResume.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        // Nút X - Hủy và quay về
        binding.btnCancelTimer.setOnClickListener {
            cancelTimer()
            showRunningCard(false)
        }

        binding.btnReset.setOnClickListener {
            binding.toggleQuickSelect.check(R.id.btnCustom)
            setTimerTime(0, 15) // Reset về 15 phút
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
                updateProgressBar()
            }

            override fun onFinish() {
                isTimerRunning = false
                updateStatusUI()
            }
        }.start()

        isTimerRunning = true
        updateStatusUI()
        updateEndTime()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        updateStatusUI()
    }

    private fun cancelTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = 0
    }

    private fun updateCountDownText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60

        val timeFormatted = if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
        binding.tvTimeRemaining.text = timeFormatted
    }

    private fun updateProgressBar() {
        if (totalTimeInMillis > 0) {
            val progress = (timeLeftInMillis.toFloat() / totalTimeInMillis.toFloat() * 100).toInt()
            binding.timerProgress.progress = progress
        }
    }

    private fun updateStatusUI() {
        if (isTimerRunning) {
            binding.tvStatus.text = "Đang chạy"
            binding.ivStatusIcon.setImageResource(R.drawable.ic_play)
            binding.btnPauseResume.setIconResource(R.drawable.ic_pause)
        } else {
            binding.tvStatus.text = "Đã tạm dừng"
            binding.ivStatusIcon.setImageResource(R.drawable.ic_pause)
            binding.btnPauseResume.setIconResource(R.drawable.ic_play)
        }
        
        // Cập nhật màu sắc theo màu đã chọn
        val colorState = ColorStateList.valueOf(selectedColor)
        binding.timerProgress.setIndicatorColor(selectedColor)
        binding.tvStatus.setTextColor(selectedColor)
        binding.ivStatusIcon.imageTintList = colorState
        binding.btnPauseResume.backgroundTintList = colorState
    }

    private fun updateEndTime() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MILLISECOND, timeLeftInMillis.toInt())
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        binding.tvEndTime.text = String.format("Kết thúc lúc %02d:%02d", hour, minute)
    }

    private fun showRunningCard(isRunning: Boolean) {
        if (isRunning) {
            binding.cardSetup.visibility = View.GONE
            binding.cardSettings.visibility = View.GONE
            binding.cardRunning.visibility = View.VISIBLE
            binding.tvHeader.text = "Đếm ngược"
        } else {
            binding.cardSetup.visibility = View.VISIBLE
            binding.cardSettings.visibility = View.VISIBLE
            binding.cardRunning.visibility = View.GONE
            binding.tvHeader.text = "Hẹn giờ"
        }
    }

    private fun setupToggleGroup() {
        binding.toggleQuickSelect.addOnButtonCheckedListener { group, checkedId, isChecked ->
            val button = group.findViewById<MaterialButton>(checkedId)
            if (button != null) {
                if (isChecked) {
                    button.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    button.elevation = 16f
                    button.scaleX = 1.05f
                    button.scaleY = 1.05f
                    button.z = 16f
                    
                    when (checkedId) {
                        R.id.btn1min -> setTimerTime(0, 1)
                        R.id.btn5min -> setTimerTime(0, 5)
                        R.id.btn30min -> setTimerTime(0, 30)
                        R.id.btn1hour -> setTimerTime(1, 0)
                    }
                } else {
                    button.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_semibold)
                    button.elevation = 0f
                    button.scaleX = 1.0f
                    button.scaleY = 1.0f
                    button.z = 0f
                }
            }
        }
        binding.toggleQuickSelect.check(R.id.btnCustom)
    }

    private fun setupNumberPickers() {
        binding.npHour.apply {
            minValue = 0
            maxValue = 23
            value = 0
            setFormatter { i -> String.format("%02d", i) }
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
        binding.npMinute.apply {
            minValue = 0
            maxValue = 59
            value = 15 // Mặc định 15 phút
            setFormatter { i -> String.format("%02d", i) }
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
    }

    private fun setupColorSelection() {
        val colorViews = mapOf(
            binding.colorRed to Color.parseColor("#FF3B30"),
            binding.colorYellow to Color.parseColor("#FF9500"),
            binding.colorGreen to Color.parseColor("#34C759"),
            binding.colorBlue to Color.parseColor("#007AFF"),
            binding.colorPurple to Color.parseColor("#AF52DE"),
            binding.colorTeal to Color.parseColor("#00AF91")
        )

        colorViews.forEach { (view, color) ->
            view.setOnClickListener {
                colorViews.keys.forEach { it.isSelected = false }
                view.isSelected = true
                selectedColor = color
                binding.btnStart.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        binding.colorGreen.isSelected = true
        selectedColor = Color.parseColor("#34C759")
        binding.btnStart.backgroundTintList = ColorStateList.valueOf(selectedColor)
    }

    private fun setTimerTime(hours: Int, minutes: Int) {
        binding.npHour.value = hours
        binding.npMinute.value = minutes
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}

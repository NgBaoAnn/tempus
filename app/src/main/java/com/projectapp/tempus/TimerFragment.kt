package com.projectapp.tempus

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.projectapp.tempus.databinding.FragmentTimerBinding

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Xử lý các nút chọn thời gian nhanh
        binding.toggleQuickSelect.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn1min -> setTimerTime(0, 1)
                    R.id.btn5min -> setTimerTime(0, 5)
                    R.id.btn30min -> setTimerTime(0, 30)
                    R.id.btn1hour -> setTimerTime(1, 0)
                }
            }
        }

        // 2. Xử lý chọn màu nhãn
        setupColorSelection()

        // 3. Nút Reset
        binding.btnReset.setOnClickListener {
            binding.toggleQuickSelect.clearChecked()
            setTimerTime(0, 0)
        }
    }

    private fun setupColorSelection() {
        val colorViews = listOf(
            binding.colorRed,
            binding.colorYellow,
            binding.colorGreen,
            binding.colorBlue,
            binding.colorPurple,
            binding.colorPink
        )

        colorViews.forEach { view ->
            view.setOnClickListener {
                // Xóa trạng thái chọn của các màu khác
                colorViews.forEach { it.isSelected = false }
                
                // Đánh dấu màu hiện tại là được chọn (hiện dấu tick qua foreground selector)
                view.isSelected = true
                
                // Đổi màu nút Start (btnStart) theo màu đã chọn
                val color = view.backgroundTintList?.defaultColor ?: Color.GREEN
                binding.btnStart.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        // Mặc định chọn màu xanh lá ban đầu
        binding.colorGreen.isSelected = true
        val defaultColor = binding.colorGreen.backgroundTintList?.defaultColor ?: Color.GREEN
        binding.btnStart.backgroundTintList = ColorStateList.valueOf(defaultColor)
    }

    private fun setTimerTime(hours: Int, minutes: Int) {
        binding.tvHour.text = String.format("%02d", hours)
        binding.tvMinute.text = String.format("%02d", minutes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

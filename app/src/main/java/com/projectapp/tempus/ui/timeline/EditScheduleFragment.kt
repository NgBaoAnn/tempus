package com.projectapp.tempus.ui.timeline

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.databinding.FragmentEditScheduleBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.appcompat.app.AlertDialog
import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.ScheduleLabel


class EditScheduleFragment : Fragment() {

    private var _binding: FragmentEditScheduleBinding? = null
    private val binding get() = _binding!!

    // Tạo ViewModel
    private val viewModel: EditScheduleViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val myUserId = "8c7c9fb1-5122-41c1-972f-6dfdcde89109"
                return EditScheduleViewModel(SupabaseScheduleRepository(), myUserId) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskIdArgs = arguments?.getString("taskId")
        viewModel.initialize(taskIdArgs)

        setupEvents()

        // 1. Lắng nghe sự kiện LƯU THÀNH CÔNG -> Tắt màn hình
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveSuccessEvent.collect {
                Toast.makeText(context, "Đã lưu thành công!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        // 3. Quan sát dữ liệu để cập nhật giao diện
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.tvScreenTitle.text = if(state.isEditMode) "Sửa tác vụ" else "Tạo tác vụ"
                binding.btnDelete.visibility = if(state.isEditMode) View.VISIBLE else View.GONE
                binding.switchTodayOnly.isEnabled = state.isEditMode
                binding.tvRepeatValue.text = repeatToVi(state.repeat)
                binding.tvDurationValue.text = durationToVi(state.duration)
                if (binding.switchTodayOnly.isChecked != state.applyTodayOnly) {
                    binding.switchTodayOnly.isChecked = state.applyTodayOnly
                }

                // Điền Title (chỉ điền khi ô đang trống để tránh reset khi user đang gõ)
                if (binding.edtTitle.text.isEmpty() && state.title.isNotEmpty()) {
                    binding.edtTitle.setText(state.title)
                }

                val dateFormatter = DateTimeFormatter.ofPattern("'ngày' dd 'thg' MM, yyyy", Locale("vi", "VN"))
                binding.tvDateValue.text = state.date.format(dateFormatter)
                binding.tvTimeValue.text = state.time.format(DateTimeFormatter.ofPattern("HH:mm"))

                val resId = requireContext().getIconResId(state.iconLabel.name)
                binding.imgIconPreview.setImageResource(resId)

                // Cập nhật màu Icon
                binding.btnPickRepeat.isEnabled = !state.applyTodayOnly
                binding.tvRepeatValue.alpha = if (state.applyTodayOnly) 0.5f else 1.0f
                try {
                    binding.imgIconPreview.setColorFilter(Color.parseColor(state.color))
                } catch (e: Exception) {}
            }
        }
    }

    private fun setupEvents() {
        binding.btnClose.setOnClickListener { findNavController().popBackStack() }

        binding.btnSave.setOnClickListener {
            val title = binding.edtTitle.text.toString()
            if (title.isBlank()) {
                Toast.makeText(context, "Chưa nhập tên", Toast.LENGTH_SHORT).show()
            } else {
                // Gọi lệnh lưu
                viewModel.saveTask(title, binding.edtDescription.text.toString())
            }
        }

        binding.switchTodayOnly.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setApplyTodayOnly(isChecked)
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteTask()
        }

        binding.imgIconPreview.setOnClickListener {
            showIconPicker()
        }

        binding.btnPickDate.setOnClickListener {
            val d = viewModel.state.value.date
            DatePickerDialog(requireContext(), { _, year, month, day ->
                viewModel.setDate(java.time.LocalDate.of(year, month + 1, day))
            }, d.year, d.monthValue - 1, d.dayOfMonth).show()
        }

        binding.btnPickTime.setOnClickListener {
            val t = viewModel.state.value.time
            TimePickerDialog(requireContext(), { _, h, m ->
                viewModel.setTime(java.time.LocalTime.of(h, m))
            }, t.hour, t.minute, true).show()
        }

        binding.btnPickRepeat.setOnClickListener {
            // Nếu đang bật only_today thì bạn có thể chặn luôn:
            if (viewModel.state.value.applyTodayOnly) {
                Toast.makeText(context, "Chế độ 'Chỉ hôm nay' không đổi lặp lại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = arrayOf("Một lần", "Hàng ngày", "Hàng tuần", "Hàng tháng")
            val values = arrayOf(RepeatType.once, RepeatType.daily, RepeatType.weekly, RepeatType.monthly)

            val current = viewModel.state.value.repeat
            val checkedIndex = values.indexOf(current).coerceAtLeast(0)

            AlertDialog.Builder(requireContext())
                .setTitle("Chọn lặp lại")
                .setSingleChoiceItems(options, checkedIndex) { dialog, which ->
                    viewModel.setRepeat(values[which])
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        binding.btnPickDuration.setOnClickListener {
            val mins = arrayOf(0, 15, 30, 45, 60, 90, 120, 180)
            val labels = arrayOf("Không", "15 phút", "30 phút", "45 phút", "1 giờ", "1 giờ 30", "2 giờ", "3 giờ")

            val current = viewModel.state.value.duration
            val currentMin = hhmmssToMinutes(current)
            val checked = mins.indexOf(currentMin).let { if (it >= 0) it else 2 } // default 30p

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Chọn thời lượng")
                .setSingleChoiceItems(labels, checked) { dialog, which ->
                    val dur = minutesToHHMMSS(mins[which])
                    viewModel.setDuration(dur)
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        setupColorClick(binding.colorRed, "#F44336")
        setupColorClick(binding.colorYellow, "#FFEB3B")
        setupColorClick(binding.colorGreen, "#4CAF50")
        setupColorClick(binding.colorBlue, "#2196F3")
        setupColorClick(binding.colorPurple, "#9C27B0")
    }

    private fun setupColorClick(view: View, colorCode: String) {
        view.setOnClickListener { viewModel.setColor(colorCode) }
    }

    private fun repeatToVi(r: RepeatType): String {
        return when (r) {
            RepeatType.once -> "Một lần"
            RepeatType.daily -> "Hàng ngày"
            RepeatType.weekly -> "Hàng tuần"
            RepeatType.monthly -> "Hàng tháng"
        }
    }
    private fun durationToVi(hhmmss: String): String {
        val parts = hhmmss.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val totalMin = h * 60 + m

        return when (totalMin) {
            0 -> "Không"
            in 1..59 -> "${totalMin} phút"
            else -> {
                val hh = totalMin / 60
                val mm = totalMin % 60
                if (mm == 0) "${hh} giờ" else "${hh} giờ ${mm} phút"
            }
        }
    }
    private fun hhmmssToMinutes(s: String): Int {
        val p = s.split(":")
        val h = p.getOrNull(0)?.toIntOrNull() ?: 0
        val m = p.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    private fun minutesToHHMMSS(min: Int): String {
        val h = min / 60
        val m = min % 60
        return String.format("%02d:%02d:00", h, m)
    }
    private fun showIconPicker() {
        val labels = listOf(
            ScheduleLabel.wakeup,
            ScheduleLabel.eat,
            ScheduleLabel.exercise,
            ScheduleLabel.rest,
            ScheduleLabel.water,
            ScheduleLabel.book,
            ScheduleLabel.sleep
        )

        val namesVi = arrayOf(
            "Thức dậy",
            "Ăn uống",
            "Tập luyện",
            "Nghỉ ngơi",
            "Uống nước",
            "Học tập",
            "Ngủ"
        )

        val current = viewModel.state.value.iconLabel
        val checked = labels.indexOf(current).coerceAtLeast(0)

        AlertDialog.Builder(requireContext())
            .setTitle("Chọn biểu tượng")
            .setSingleChoiceItems(namesVi, checked) { dialog, which ->
                val picked = labels[which]
                viewModel.setIcon(picked)

                // update preview ngay
                val resId = requireContext().getIconResId(picked.name)
                binding.imgIconPreview.setImageResource(resId)

                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

}
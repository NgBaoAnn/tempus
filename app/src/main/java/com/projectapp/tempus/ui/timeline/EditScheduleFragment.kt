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

                val iconResId = requireContext().getIconResId(state.iconLabel)
                binding.imgIconPreview.setImageResource(iconResId)

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

}
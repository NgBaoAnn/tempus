package com.projectapp.tempus.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.ui.timeline.compose.EditScheduleScreen
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class EditScheduleFragment : Fragment() {

    private val viewModel: EditScheduleViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val currentUserId = com.projectapp.tempus.core.supabase.SupabaseClientProvider.client
                    .auth.currentSessionOrNull()?.user?.id ?: ""
                return EditScheduleViewModel(SupabaseScheduleRepository(), currentUserId) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                EditScheduleScreen(
                    viewModel = viewModel,
                    onClose = { findNavController().popBackStack() },
                    onSaveSuccess = { 
                        Toast.makeText(context, "Đã lưu thành công!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack() 
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
<<<<<<< HEAD
=======

        val taskIdArgs = arguments?.getString("taskId")
        viewModel.initialize(taskIdArgs)

        setupEvents()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveSuccessEvent.collect {
                Toast.makeText(context, "Đã lưu thành công!", Toast.LENGTH_SHORT).show()
                
                // Refresh widget to show new/updated task
                com.projectapp.tempus.widget.TasksWidgetProvider.refreshAllWidgets(requireContext())
                
                findNavController().popBackStack()
            }
        }

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

                if (binding.edtTitle.text.isEmpty() && state.title.isNotEmpty()) {
                    binding.edtTitle.setText(state.title)
                }

                val dateFormatter = DateTimeFormatter.ofPattern("'ngày' dd 'thg' MM, yyyy", Locale("vi", "VN"))
                binding.tvDateValue.text = state.date.format(dateFormatter)
                binding.tvTimeValue.text = state.time.format(DateTimeFormatter.ofPattern("HH:mm"))

                val resId = requireContext().getIconResId(state.iconLabel.name)
                binding.imgIconPreview.setImageResource(resId)

                binding.btnPickRepeat.isEnabled = !state.applyTodayOnly
                binding.tvRepeatValue.alpha = if (state.applyTodayOnly) 0.5f else 1.0f
                try {
                    binding.imgIconPreview.setColorFilter(Color.parseColor(state.color))
                } catch (e: Exception) {}
                
                // Priority display
                binding.tvPriorityValue.text = priorityToVi(state.priority)
                val priorityColor = when (state.priority) {
                    PriorityType.high -> "#F44336"
                    PriorityType.medium -> "#FF9800"
                    PriorityType.low -> "#4CAF50"
                }
                try {
                    binding.priorityIndicator.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(priorityColor))
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
                // Check alarm permission first
                if (!com.projectapp.tempus.util.PermissionHelper.canScheduleExactAlarms(requireContext())) {
                    com.projectapp.tempus.util.PermissionHelper.showAlarmPermissionDialog(requireContext()) {
                        // Save task after user sees the dialog
                        viewModel.saveTask(title, binding.edtDescription.text.toString())
                    }
                } else {
                    // Has permission, save directly
                    viewModel.saveTask(title, binding.edtDescription.text.toString())
                }
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
            val checked = mins.indexOf(currentMin).let { if (it >= 0) it else 2 }

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
>>>>>>> master
        
        val taskIdArgs = arguments?.getString("taskId")
        val selectedDateArgs = arguments?.getString("selectedDate") // Ngày user đang xem
        viewModel.initialize(taskIdArgs, selectedDateArgs)
        
        // Error handling
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorEvent.collect { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }
}


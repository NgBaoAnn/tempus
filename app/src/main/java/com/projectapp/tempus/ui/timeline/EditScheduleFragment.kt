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

        // 👇 [QUAN TRỌNG] PHẦN NÀY CHỊU TRÁCH NHIỆM TẮT MÀN HÌNH 👇
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveSuccessEvent.collect {
                // Khi code chạy vào đây nghĩa là ViewModel đã báo: "Lưu xong rồi!"
                Toast.makeText(context, "Đã lưu thành công!", Toast.LENGTH_SHORT).show()

                // Lệnh này sẽ đóng màn hình và quay về Timeline
                findNavController().popBackStack()
            }
        }
        // 👆 HẾT PHẦN TẮT MÀN HÌNH 👆

        // Quan sát dữ liệu để cập nhật giao diện
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.tvScreenTitle.text = if(state.isEditMode) "Sửa tác vụ" else "Tạo tác vụ"
                binding.btnDelete.visibility = if(state.isEditMode) View.VISIBLE else View.GONE

                if (binding.edtTitle.text.isEmpty() && state.title.isNotEmpty()) {
                    binding.edtTitle.setText(state.title)
                    binding.edtDescription.setText(state.description)
                }

                val dateFormatter = DateTimeFormatter.ofPattern("'ngày' dd 'thg' MM, yyyy", Locale("vi", "VN"))
                binding.tvDateValue.text = state.date.format(dateFormatter)
                binding.tvTimeValue.text = state.time.format(DateTimeFormatter.ofPattern("HH:mm"))

                try {
                    binding.imgIconPreview.setColorFilter(Color.parseColor(state.color))
                } catch (e: Exception) {}
            }
        }
    }

    private fun setupEvents() {
        // Nút X (Đóng không lưu)
        binding.btnClose.setOnClickListener { findNavController().popBackStack() }

        // Nút Save (Lưu)
        binding.btnSave.setOnClickListener {
            val title = binding.edtTitle.text.toString()
            if (title.isBlank()) {
                Toast.makeText(context, "Chưa nhập tên", Toast.LENGTH_SHORT).show()
            } else {
                // CHỈ GỌI LỆNH LƯU - KHÔNG ĐƯỢC GỌI popBackStack() Ở ĐÂY
                viewModel.saveTask(title, binding.edtDescription.text.toString())
            }
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteTask()
            // Riêng xóa thì có thể đợi event hoặc đóng luôn tùy logic,
            // nhưng tốt nhất là đợi event giống như Save để đảm bảo xóa xong mới đóng.
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

        setupColorClick(binding.colorRed, "#F44336")
        setupColorClick(binding.colorYellow, "#FFEB3B")
        setupColorClick(binding.colorGreen, "#4CAF50")
        setupColorClick(binding.colorBlue, "#2196F3")
        setupColorClick(binding.colorPurple, "#9C27B0")
    }

    private fun setupColorClick(view: View, colorCode: String) {
        view.setOnClickListener { viewModel.setColor(colorCode) }
    }
}
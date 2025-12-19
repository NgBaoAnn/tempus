package com.projectapp.tempus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.databinding.FragmentTimelineBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.projectapp.tempus.ui.timeline.TimelineAdapter
import com.projectapp.tempus.ui.timeline.TimelineViewModel
import com.projectapp.tempus.data.schedule.MockTimelineBlocks

class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Cài đặt RecyclerView
        binding.rvTimeline.layoutManager = LinearLayoutManager(requireContext())

        // -------------------------------------------------------------
        // CÁCH 1: DÙNG DỮ LIỆU MOCK (Để test giao diện)
        // -------------------------------------------------------------

        // Lấy danh sách giả lập
        val mockData = MockTimelineBlocks.getList()

        Toast.makeText(requireContext(), "Số lượng item: ${mockData.size}", Toast.LENGTH_LONG).show()

        // Gán vào Adapter và hiển thị ngay
        val adapter = TimelineAdapter(mockData)
        binding.rvTimeline.adapter = adapter

        // Set ngày tháng giả lập trên header (cho khớp với dữ liệu mock là tháng 12/2025)
        val mockDate = LocalDate.of(2025, 12, 20)
        val formatter = DateTimeFormatter.ofPattern("'thg' MM yyyy")
        binding.tvMonth.text = mockDate.format(formatter)


        // -------------------------------------------------------------
        // CÁCH 2: DÙNG DỮ LIỆU THẬT (Khi nào chạy Server thì mở lại phần này)
        // -------------------------------------------------------------
        /*
        val repo = SupabaseScheduleRepository()
        val vm = TimelineViewModel(userId = "UUID-THAT-CUA-BAN", repo = repo)
        val scope = MainScope()

        scope.launch {
            vm.ui.collect { state ->
                binding.tvMonth.text = state.date.format(formatter)
                val realAdapter = TimelineAdapter(state.blocks)
                binding.rvTimeline.adapter = realAdapter
            }
        }
        vm.onSelectDate(LocalDate.now())
        */

        // Sự kiện click nút Add (Chỉ hiện thông báo test)
        binding.btnAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Đang hiển thị dữ liệu Mock", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.projectapp.tempus

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.databinding.FragmentStatisticsBinding
import com.projectapp.tempus.domain.usecase.GetStatisticsUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val myUserId = "8c7c9fb1-5122-41c1-972f-6dfdcde89109"
                val repo = SupabaseScheduleRepository()
                val useCase = GetStatisticsUseCase()
                return StatisticsViewModel(myUserId, repo, useCase) as T
            }
        }
    }

    private lateinit var topTaskAdapter: TopTaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupBarChart()
        observeViewModel()

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.setMode(checkedId == R.id.btnWeek)
            }
        }

        binding.btnPrev.setOnClickListener { viewModel.navigateRange(-1) }
        binding.btnNext.setOnClickListener { viewModel.navigateRange(1) }

        viewModel.setMode(true)
    }

    private fun setupRecyclerView() {
        topTaskAdapter = TopTaskAdapter(emptyList())
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = topTaskAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is StatisticsUiState.Loading -> { }
                    is StatisticsUiState.Success -> {
                        updateUI(state)
                    }
                    is StatisticsUiState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUI(state: StatisticsUiState.Success) {
        val res = state.result
        // Kiểm tra xem đang ở chế độ tuần hay tháng dựa vào số ngày
        val dayCount = ChronoUnit.DAYS.between(state.startDate, state.endDate) + 1
        val isWeek = dayCount <= 7

        // Cập nhật Header & Range
        val formatterHeader = if (isWeek) DateTimeFormatter.ofPattern("dd/MM") else DateTimeFormatter.ofPattern("MM/yyyy")
        val rangeText = if (isWeek) {
            "${state.startDate.format(formatterHeader)} - ${state.endDate.format(formatterHeader)}"
        } else {
            "Tháng ${state.startDate.format(formatterHeader)}"
        }
        
        binding.tvStatisticDesc.text = rangeText
        binding.tvRange.text = rangeText
        binding.tvStatisticDesc2.text = "Tổng cộng ${res.completedTasksInRange} trong ${res.totalTasksInRange} tác vụ đã hoàn thành"

        // Chuẩn bị dữ liệu biểu đồ
        val entries = res.dailyStats.mapIndexed { index, dayStats ->
            BarEntry(index.toFloat(), dayStats.completionPercentage)
        }
        
        // Nhãn trục X: "Th 2" cho tuần, "1", "2"... cho tháng
        val labels = res.dailyStats.map { 
            if (isWeek) {
                it.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("vi", "VN"))
            } else {
                it.date.dayOfMonth.toString()
            }
        }
        
        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        // Cấu hình lưới và hiển thị cho từng chế độ
        if (isWeek) {
            xAxis.labelCount = 7
            xAxis.setDrawGridLines(true)
        } else {
            // Cho chế độ tháng: Hiện nhãn cách quãng để tránh đè chữ, nhưng lưới dọc vẫn hiện cho mỗi cột
            xAxis.labelCount = 10 
            xAxis.setDrawGridLines(true)
        }
        xAxis.granularity = 1f

        val dataSet = BarDataSet(entries, "")
        dataSet.color = Color.parseColor("#3CDAEF")
        dataSet.setDrawValues(false)
        
        val barData = BarData(dataSet)
        // Điều chỉnh độ rộng cột: Tháng nhiều cột nên để mảnh hơn
        barData.barWidth = if (isWeek) 0.5f else 0.7f 
        
        binding.barChart.data = barData
        binding.barChart.animateY(800)
        binding.barChart.invalidate()

        topTaskAdapter.updateData(res.topCategories)
    }

    private fun setupBarChart() {
        val barChart = binding.barChart
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setTouchEnabled(true)
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)

        val leftAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f
        leftAxis.isEnabled = false

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = true
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 100f
        rightAxis.setLabelCount(5, true)
        rightAxis.setDrawGridLines(true)
        rightAxis.gridColor = Color.parseColor("#BDBDBD")
        rightAxis.enableGridDashedLine(12f, 8f, 0f)
        rightAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
        }

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(true)
        xAxis.gridColor = Color.parseColor("#D3D3D3") // Màu lưới dọc hơi nhạt hơn lưới ngang
        xAxis.enableGridDashedLine(10f, 10f, 0f)
        xAxis.granularity = 1f

        barChart.setExtraOffsets(0f, 0f, 0f, 10f)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

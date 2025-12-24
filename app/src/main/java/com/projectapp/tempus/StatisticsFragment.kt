package com.projectapp.tempus

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.projectapp.tempus.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo biểu đồ
        setupBarChart()

        // Khởi tạo danh sách tác vụ hàng đầu
        setupTopTasks()

        // Đổ dữ liệu mẫu cho biểu đồ
        loadChartDataWeek()

        // Xử lý sự kiện Toggle (Tuần/Tháng)
        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnWeek -> {
                        loadChartDataWeek()
                    }
                    R.id.btnMonth -> {
                        loadChartDataMonth()
                    }
                }
            }
        }
    }

    private fun setupTopTasks() {
        val mockTasks = listOf(
            TopTask("Thư giãn", 0, 7, 0, R.drawable.rest, "#AF52DE"), // Tím
            TopTask("Ăn tối", 0, 7, 0, R.drawable.eat, "#FF2D55"), // Đỏ hồng
            TopTask("Ăn trưa", 0, 7, 0, R.drawable.eat, "#4CD964"), // Xanh lá
            TopTask("Uống nước", 0, 7, 0, R.drawable.water, "#007AFF"), // Xanh dương
            TopTask("Nghỉ giải lao", 0, 5, 0, R.drawable.rest, "#5856D6"), // Indigo
            TopTask("Thức dậy", 0, 7, 0, R.drawable.wakeup, "#FF9500"), // Cam
            TopTask("Học tập", 3, 5, 60, R.drawable.book, "#5AC8FA"), // Xanh trời
            TopTask("Dọn dẹp", 1, 2, 50, R.drawable.clean, "#8E8E93"), // Xám
            TopTask("Tập thể dục", 4, 4, 100, R.drawable.exercise, "#FFCC00"), // Vàng
            TopTask("Ngủ", 7, 7, 100, R.drawable.sleep, "#34C759") // Xanh lá đậm
        )

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TopTaskAdapter(mockTasks)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupBarChart() {
        val barChart = binding.barChart

        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setTouchEnabled(false)
        barChart.setDrawGridBackground(false)
        barChart.setExtraOffsets(0f, 0f, 0f, 10f)

        // --- Trục Y (Bên phải) ---
        val rightAxis = barChart.axisRight
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 100f
        rightAxis.setDrawAxisLine(false)
        rightAxis.setLabelCount(5, true)
        rightAxis.textColor = Color.parseColor("#8E8E93")

        // Lưới ngang đậm hơn (tăng độ đục và kích thước nét)
        rightAxis.setDrawGridLines(true)
        rightAxis.gridColor = Color.parseColor("#BDBDBD") // Đậm hơn màu cũ (#E0E0E0)
        rightAxis.gridLineWidth = 0.8f
        rightAxis.enableGridDashedLine(12f, 8f, 0f)

        rightAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        }

        // --- Trục X ---
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.granularity = 1f
        xAxis.textColor = Color.parseColor("#8E8E93")

        // Lưới dọc đậm hơn
        xAxis.setDrawGridLines(true)
        xAxis.gridColor = Color.parseColor("#BDBDBD")
        xAxis.gridLineWidth = 0.8f
        xAxis.enableGridDashedLine(12f, 8f, 0f)

        val days = arrayOf("Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN")
        xAxis.valueFormatter = IndexAxisValueFormatter(days)

        barChart.axisLeft.isEnabled = false
    }

    private fun loadChartDataWeek() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 30f))
        entries.add(BarEntry(1f, 50f))
        entries.add(BarEntry(2f, 90f))
        entries.add(BarEntry(3f, 10f))
        entries.add(BarEntry(4f, 60f))
        entries.add(BarEntry(5f, 100f))
        entries.add(BarEntry(6f, 45f))

        val dataSet = BarDataSet(entries, "")
        dataSet.color = Color.parseColor("#3CDAEF")
        dataSet.setDrawValues(false)

        val data = BarData(dataSet)
        data.barWidth = 0.5f

        binding.barChart.data = data
        binding.barChart.animateY(800)
        binding.barChart.invalidate()
    }

    private fun loadChartDataMonth() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 10f))
        entries.add(BarEntry(1f, 10f))
        entries.add(BarEntry(2f, 60f))
        entries.add(BarEntry(3f, 50f))
        entries.add(BarEntry(4f, 20f))
        entries.add(BarEntry(5f, 16f))
        entries.add(BarEntry(6f, 75f))

        val dataSet = BarDataSet(entries, "")
        dataSet.color = Color.parseColor("#3CDAEF")
        dataSet.setDrawValues(false)

        val data = BarData(dataSet)
        data.barWidth = 0.5f

        binding.barChart.data = data
        binding.barChart.animateY(800)
        binding.barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

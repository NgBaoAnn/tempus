package com.projectapp.tempus

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

        // Đổ dữ liệu mẫu
        loadChartDataWeek()

        // Xử lý sự kiện Toggle (Tuần/Tháng)
        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnWeek -> {
                        // Xử lý khi chọn Tuần
                        loadChartDataWeek() // Cập nhật lại dữ liệu tuần
                    }
                    R.id.btnMonth -> {
                        // Xử lý khi chọn Tháng
                        loadChartDataMonth() // Cập nhật lại dữ liệu tháng

                    }
                }
            }
        }
    }

    private fun setupBarChart() {
        val barChart = binding.barChart

        // --- Cấu hình chung ---
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setTouchEnabled(false)
        barChart.setDrawGridBackground(false)

        // --- Trục Y (Bên trái) ---
        val leftAxis = barChart.axisRight
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f
        leftAxis.setDrawAxisLine(false)
        leftAxis.setLabelCount(5, true) // Chia 5 vạch: 0, 25, 50, 75, 100

        // Đường kẻ ngang nét đứt giống mẫu
        leftAxis.setDrawGridLines(true)
        leftAxis.enableGridDashedLine(10f, 10f, 0f)
        leftAxis.gridColor = Color.parseColor("#0D0D0D")

        // Định dạng %
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        }

        // --- Trục X (Phía dưới) ---
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.granularity = 1f

        // Nhãn thứ trong tuần
        val days = arrayOf("Mon", "Tus", "Wed", "Thu", "Fri", "Sat", "Sun")
        xAxis.valueFormatter = IndexAxisValueFormatter(days)

        // Tắt trục Y bên phải
        barChart.axisLeft.isEnabled = false
    }

    private fun loadChartDataWeek() {
        val entries = ArrayList<BarEntry>()
        // Dữ liệu mẫu (Vị trí, Giá trị %)
        entries.add(BarEntry(0f, 30f))
        entries.add(BarEntry(1f, 50f))
        entries.add(BarEntry(2f, 90f))
        entries.add(BarEntry(3f, 10f))
        entries.add(BarEntry(4f, 60f))
        entries.add(BarEntry(5f, 100f))
        entries.add(BarEntry(6f, 45f))

        val dataSet = BarDataSet(entries, "")

        // Đổi màu cột sang màu tím (giống màu app của bạn)
        dataSet.color = Color.parseColor("#3CDAEF")
        dataSet.setDrawValues(false) // Ẩn số trên đầu cột

        val data = BarData(dataSet)
        data.barWidth = 0.5f // Độ rộng của cột (0.1 -> 1.0)

        binding.barChart.data = data
        binding.barChart.animateY(800)
        binding.barChart.invalidate()
    }

    private fun loadChartDataMonth() {
        val entries = ArrayList<BarEntry>()
        // Dữ liệu mẫu (Vị trí, Giá trị %)
        entries.add(BarEntry(0f, 10f))
        entries.add(BarEntry(1f, 10f))
        entries.add(BarEntry(2f, 60f))
        entries.add(BarEntry(3f, 50f))
        entries.add(BarEntry(4f, 20f))
        entries.add(BarEntry(5f, 16f))
        entries.add(BarEntry(6f, 75f))

        val dataSet = BarDataSet(entries, "")

        // Đổi màu cột sang màu tím (giống màu app của bạn)
        dataSet.color = Color.parseColor("#3CDAEF")
        dataSet.setDrawValues(false) // Ẩn số trên đầu cột

        val data = BarData(dataSet)
        data.barWidth = 0.5f // Độ rộng của cột (0.1 -> 1.0)

        binding.barChart.data = data
        binding.barChart.animateY(800)
        binding.barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
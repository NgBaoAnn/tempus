package com.projectapp.tempus

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController // Nhớ Import dòng này
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.databinding.FragmentTimelineBinding
import com.projectapp.tempus.ui.timeline.TimelineAdapter
import com.projectapp.tempus.ui.timeline.TimelineViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.projectapp.tempus.ui.timeline.WeekAdapter
import java.time.DayOfWeek
import java.time.LocalDate
import android.app.DatePickerDialog
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import androidx.recyclerview.widget.PagerSnapHelper
import com.projectapp.tempus.ui.timeline.WeekItem
import com.projectapp.tempus.ui.timeline.MonthCalendarDialogFragment
import java.time.YearMonth

class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private lateinit var weekAdapter: WeekAdapter
    private var lastWeekStart: LocalDate? = null
    private var pendingJumpWeekStart: LocalDate? = null

    private val binding get() = _binding!!

    // --- 1. KHỞI TẠO VIEWMODEL ---
    private val viewModel: TimelineViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val myUserId = "8c7c9fb1-5122-41c1-972f-6dfdcde89109"
                val repo = SupabaseScheduleRepository()
                return TimelineViewModel(userId = myUserId, repo = repo) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // --- 1. SETUP CALENDAR (7 ngày trong tuần) ---
        weekAdapter = WeekAdapter { pickedDate ->
            viewModel.onSelectDate(pickedDate)
        }

        binding.rvCalendar.apply {
            adapter = weekAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        PagerSnapHelper().attachToRecyclerView(binding.rvCalendar)
        binding.rvCalendar.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = rv.layoutManager as LinearLayoutManager
                    val pos = lm.findFirstCompletelyVisibleItemPosition()
                    if (pos != RecyclerView.NO_POSITION) {
                        val weekStart = buildWeeksAround(viewModel.ui.value.date)[pos].days.first()
                        // ✅ chỉ đổi header tháng/năm, không chọn ngày
                        // cách đơn giản: set date = giữa tuần để format tháng
                        viewModel.setCurrentWeekForHeader(weekStart.plusDays(3))
                    }
                }
            }
        })

        // --- 2. SETUP ADAPTER ---
        val adapter = TimelineAdapter(
            items = emptyList(),
            onBlockClick = { block ->
                // [SỬA] LOGIC CHUYỂN MÀN HÌNH ĐỂ EDIT
                // Gói ID vào Bundle để màn hình Edit biết cần load task nào
                val bundle = Bundle().apply {
                    putString("taskId", block.taskId) // Đảm bảo block.taskId là ID của task
                }
                // Chuyển màn hình (Ghi đè)
                findNavController().navigate(R.id.action_timelineFragment_to_editScheduleFragment, bundle)
            },
            onStatusClick = { block ->
                // [GIỮ NGUYÊN] Logic đổi trạng thái Checkbox
                val newStatus = if (block.status == StatusType.done) StatusType.planned else StatusType.done
                viewModel.onToggleStatus(block.taskId, newStatus)
            }
        )

        binding.rvTimeline.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTimeline.adapter = adapter

        // --- 3. QUAN SÁT DỮ LIỆU TỪ VIEWMODEL ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ui.collect { state ->

                adapter.submitList(state.blocks)

                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))
                binding.tvMonth.text = state.date.format(formatter)

                val weeks = buildWeeksAround(state.date)
                weekAdapter.submit(weeks, state.date)

                val currentWeekStart = state.date.with(DayOfWeek.MONDAY)

                // ✅ CHỈ scroll khi:
                // - lần đầu vào
                // - đổi tuần (vuốt qua tuần khác)
                // - hoặc user vừa chọn tháng/năm (pendingJumpWeekStart != null)
                val shouldScroll =
                    (lastWeekStart == null) ||
                            (currentWeekStart != lastWeekStart) ||
                            (pendingJumpWeekStart != null)

                if (shouldScroll) {
                    val targetWeekStart = pendingJumpWeekStart ?: currentWeekStart

                    val idx = weeks.indexOfFirst { w ->
                        w.days.first().with(DayOfWeek.MONDAY) == targetWeekStart
                    }

                    if (idx != -1) {
                        binding.rvCalendar.scrollToPosition(idx)
                    }

                    pendingJumpWeekStart = null
                }

                lastWeekStart = currentWeekStart

                if (state.error != null) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }


        // --- 4. XỬ LÝ NÚT ADD (DẤU CỘNG) ---
        binding.btnAdd.setOnClickListener {
            // [SỬA] LOGIC CHUYỂN MÀN HÌNH ĐỂ THÊM MỚI
            // Không truyền bundle -> Màn hình Edit sẽ hiểu là "Thêm mới"
            findNavController().navigate(R.id.action_timelineFragment_to_editScheduleFragment)
        }

        binding.btnMonthPicker.setOnClickListener {
            val init = viewModel.ui.value.date

            lateinit var dialog: MonthCalendarDialogFragment

            dialog = MonthCalendarDialogFragment(
                initialDate = init,
                onPick = { d: LocalDate ->
                    viewModel.onSelectDate(d)
                },
                onMonthChange = { ym: YearMonth ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val map = viewModel.getMonthIcons(ym)
                        dialog.setMonthData(ym, map)
                    }
                }
            )

            dialog.show(parentFragmentManager, "month_calendar")
        }


        // Load dữ liệu khi vào màn hình
        viewModel.onRefresh()
    }

    private fun buildWeeksAround(center: LocalDate): List<WeekItem> {
        val start = center.with(DayOfWeek.MONDAY)

        return (-4..4).map { offset ->
            val weekStart = start.plusWeeks(offset.toLong())
            WeekItem(
                days = (0..6).map { weekStart.plusDays(it.toLong()) }
            )
        }
    }


    // Thêm hàm onResume để khi quay lại từ màn Edit thì reload lại danh sách
    override fun onResume() {
        super.onResume()
        viewModel.onRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
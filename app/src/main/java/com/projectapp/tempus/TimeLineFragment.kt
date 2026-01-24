package com.projectapp.tempus

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.ui.timeline.MonthCalendarDialogFragment
import com.projectapp.tempus.ui.timeline.TimelineViewModel
import com.projectapp.tempus.ui.timeline.WeekItem
import com.projectapp.tempus.ui.timeline.compose.TimelineScreen
import io.github.jan.supabase.gotrue.auth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class TimelineFragment : Fragment() {

    private val viewModel: TimelineViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val supabase = SupabaseClientProvider.client
                val myUserId = supabase.auth.currentUserOrNull()?.id ?: ""
                val repo = SupabaseScheduleRepository()
                return TimelineViewModel(
                    application = requireActivity().application,
                    userId = myUserId,
                    repo = repo
                ) as T
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                val uiState by viewModel.ui.collectAsState()
                val weeks = buildWeeksAround(uiState.date)
                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))
                
                TimelineScreen(
                    blocks = uiState.blocks,
                    selectedDate = uiState.date,
                    monthYear = uiState.date.format(formatter),
                    weeks = weeks.map { it.days },
                    dailyQuote = uiState.dailyQuote,
                    onDateSelected = { date ->
                        viewModel.onSelectDate(date)
                    },
                    onMonthPickerClick = {
                        showMonthPicker()
                    },
                    onAddClick = {
                        val currentDate = viewModel.ui.value.date
                        val bundle = Bundle().apply {
                            putString("selectedDate", currentDate.toString())
                        }
                        findNavController().navigate(R.id.action_timelineFragment_to_editScheduleFragment, bundle)
                    },
                    onTaskClick = { block ->
                        val bundle = Bundle().apply {
                            putString("taskId", block.taskId)
                            putString("selectedDate", viewModel.ui.value.date.toString())
                        }
                        findNavController().navigate(R.id.action_timelineFragment_to_editScheduleFragment, bundle)
                    },
                    onStatusToggle = { block ->
                        val newStatus = if (block.status == StatusType.done) StatusType.planned else StatusType.done
                        viewModel.onToggleStatus(block.taskId, newStatus)
                    },
                    onSubtaskToggle = { subtaskId, isDone ->
                        viewModel.onSubtaskToggle(subtaskId, isDone)
                    }
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel.onRefresh()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showMonthPicker() {
        val currentDate = viewModel.ui.value.date
        val dialog = MonthCalendarDialogFragment(
            initialDate = currentDate,
            onPick = { date ->
                viewModel.onSelectDate(date)
            },
            onMonthChange = { yearMonth ->
                // Can load month data if needed
            }
        )
        dialog.show(parentFragmentManager, "MonthCalendarDialogFragment")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildWeeksAround(anchor: LocalDate, range: Int = 52): List<WeekItem> {
        val weekStart = anchor.with(DayOfWeek.MONDAY)
        
        return (-range..range).map { offset ->
            val monday = weekStart.plusWeeks(offset.toLong())
            val days = (0..6).map { monday.plusDays(it.toLong()) }
            WeekItem(days)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
package com.projectapp.tempus.ui.heatmap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.projectapp.tempus.R
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.ui.heatmap.compose.DayDetailSheet
import com.projectapp.tempus.ui.heatmap.compose.HeatmapCalendarScreen
import com.projectapp.tempus.ui.theme.TempusTheme
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.time.YearMonth

/**
 * Fragment for Productivity Heatmap Calendar.
 * Displays a monthly calendar with color-coded completion rates.
 */
class HeatmapFragment : Fragment() {

    private val viewModel: HeatmapViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: ""
                val repo = SupabaseScheduleRepository()
                @Suppress("UNCHECKED_CAST")
                return HeatmapViewModel(userId, repo) as T
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Handle initial month from arguments
        arguments?.getString("initialMonth")?.let { monthStr ->
            try {
                val yearMonth = YearMonth.parse(monthStr)
                viewModel.loadMonth(yearMonth)
            } catch (e: Exception) {
                // Use default (current month)
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                TempusTheme {
                    val state by viewModel.state.collectAsState()
                    val scope = rememberCoroutineScope()
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    // Main Heatmap Calendar Screen
                    HeatmapCalendarScreen(
                        monthData = state.monthData,
                        selectedDate = state.selectedDate,
                        isLoading = state.isLoading,
                        onDateSelected = { date -> viewModel.selectDate(date) },
                        onNavigatePrevMonth = { viewModel.navigatePrevMonth() },
                        onNavigateNextMonth = { viewModel.navigateNextMonth() },
                        onNavigateBack = { findNavController().popBackStack() },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Day Detail Bottom Sheet
                    if (state.showDayDetailSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { viewModel.dismissDayDetail() },
                            sheetState = sheetState
                        ) {
                            // Find day data for selected date
                            val dayData = state.monthData?.days?.find { 
                                it.date == state.selectedDate 
                            }

                            DayDetailSheet(
                                dayData = dayData,
                                tasks = state.selectedDayTasks,
                                isLoading = state.isLoadingDayDetail,
                                onDismiss = {
                                    scope.launch {
                                        sheetState.hide()
                                        viewModel.dismissDayDetail()
                                    }
                                },
                                onViewTimeline = {
                                    // Navigate to Timeline with selected date
                                    viewModel.dismissDayDetail()
                                    navigateToTimeline(state.selectedDate.toString())
                                },
                                onToggleTaskStatus = { task ->
                                    viewModel.toggleTaskStatus(task)
                                },
                                onAddTask = {
                                    // Navigate to add task
                                    viewModel.dismissDayDetail()
                                    navigateToAddTask(state.selectedDate.toString())
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this screen
        viewModel.refresh()
    }

    /**
     * Navigate to Timeline Fragment with selected date.
     */
    private fun navigateToTimeline(dateStr: String) {
        try {
            // Note: Timeline doesn't have a date argument yet
            // For now, we just navigate back (Timeline is in the bottom nav)
            findNavController().popBackStack()
            // TODO: Add date argument to TimelineFragment if needed
        } catch (e: Exception) {
            findNavController().popBackStack()
        }
    }

    /**
     * Navigate to add task screen with pre-selected date.
     */
    private fun navigateToAddTask(dateStr: String) {
        try {
            findNavController().navigate(
                R.id.editScheduleFragment,
                bundleOf(
                    "taskId" to null,
                    "selectedDate" to dateStr
                )
            )
        } catch (e: Exception) {
            findNavController().popBackStack()
        }
    }
}

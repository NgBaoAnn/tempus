package com.projectapp.tempus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.projectapp.tempus.data.RepositoryProvider
import com.projectapp.tempus.domain.usecase.GetStatisticsUseCase
import com.projectapp.tempus.ui.statistics.compose.StatisticsScreen
import com.projectapp.tempus.ui.theme.TempusTheme
import io.github.jan.supabase.gotrue.auth

class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val myUserId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: ""
                // Use RepositoryProvider for offline-first repositories
                val repo = RepositoryProvider.getScheduleRepository(requireContext())
                val useCase = GetStatisticsUseCase()
                val gamificationRepo = RepositoryProvider.getGamificationRepository(requireContext())
                @Suppress("UNCHECKED_CAST")
                return StatisticsViewModel(myUserId, repo, useCase, gamificationRepo) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                TempusTheme {
                    val uiData by viewModel.composeUiData.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()

                    StatisticsScreen(
                        uiData = uiData,
                        isLoading = isLoading,
                        onModeChange = { isWeek -> viewModel.setMode(isWeek) },
                        onPrevious = { viewModel.navigateRange(-1) },
                        onNext = { viewModel.navigateRange(1) },
                        onOpenHeatmap = {
                            findNavController().navigate(R.id.action_statistics_to_heatmap)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMode(true) // Start with week mode
    }
}


package com.projectapp.tempus.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.RepositoryProvider
import com.projectapp.tempus.ui.ai.compose.ChatScreen
import io.github.jan.supabase.gotrue.auth

/**
 * AI Chat Fragment
 * Provides Jetpack Compose chat interface for interacting with Tiramisu AI
 */
class AIFragment : Fragment() {

    private lateinit var viewModel: AIViewModel

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ViewModel with dependencies - use OfflineFirstScheduleRepository
        val scheduleRepository = RepositoryProvider.getScheduleRepository(requireContext())
        val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
        
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AIViewModel(scheduleRepository, userId) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[AIViewModel::class.java]
        
        return ComposeView(requireContext()).apply {
            // Dispose composition when fragment's view is destroyed
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            
            setContent {
                ChatScreen(viewModel = viewModel)
            }
        }
    }
}

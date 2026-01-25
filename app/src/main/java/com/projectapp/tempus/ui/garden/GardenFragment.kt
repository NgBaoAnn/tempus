package com.projectapp.tempus.ui.garden

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.projectapp.tempus.ui.garden.compose.GardenScreen
import com.projectapp.tempus.ui.garden.compose.GardenViewModel
import com.projectapp.tempus.ui.theme.TempusTheme

/**
 * Fragment hiển thị vườn cây của người dùng
 * Đã migrate sang Jetpack Compose với Modern Glassmorphism Design
 */
class GardenFragment : Fragment() {

    private val gardenViewModel: GardenViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose composition when fragment's view is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                TempusTheme {
                    GardenScreen(viewModel = gardenViewModel)
                }
            }
        }
    }
}

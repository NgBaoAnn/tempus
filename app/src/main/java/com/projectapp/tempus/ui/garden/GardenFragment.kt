package com.projectapp.tempus.ui.garden

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.projectapp.tempus.ui.garden.compose.GardenScreen
import com.projectapp.tempus.ui.theme.TempusTheme

/**
 * Fragment hiển thị vườn cây của người dùng
 * Migrated to Jetpack Compose
 */
class GardenFragment : Fragment() {

    // Use ui.garden.GardenViewModel (the main one that GardenScreen expects)
    private val viewModel: GardenViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GardenViewModel(requireActivity().application) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                TempusTheme {
                    GardenScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}

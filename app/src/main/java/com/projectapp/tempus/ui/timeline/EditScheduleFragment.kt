package com.projectapp.tempus.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.ui.timeline.compose.EditScheduleScreen
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class EditScheduleFragment : Fragment() {

    private val viewModel: EditScheduleViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val currentUserId = com.projectapp.tempus.core.supabase.SupabaseClientProvider.client
                    .auth.currentSessionOrNull()?.user?.id ?: ""
                return EditScheduleViewModel(SupabaseScheduleRepository(), currentUserId) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                EditScheduleScreen(
                    viewModel = viewModel,
                    onClose = { findNavController().popBackStack() },
                    onSaveSuccess = { 
                        Toast.makeText(context, "Đã lưu thành công!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack() 
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val taskIdArgs = arguments?.getString("taskId")
        viewModel.initialize(taskIdArgs)
        
        // Error handling
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorEvent.collect { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }
}


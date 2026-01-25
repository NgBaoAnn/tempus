package com.projectapp.tempus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.data.voice.SpeechRecognitionManager
import com.projectapp.tempus.data.voice.TaskParserService
import com.projectapp.tempus.ui.timeline.MonthCalendarDialogFragment
import com.projectapp.tempus.ui.timeline.TimelineViewModel
import com.projectapp.tempus.ui.timeline.WeekItem
import com.projectapp.tempus.ui.timeline.compose.TimelineScreen
import com.projectapp.tempus.ui.voice.VoiceViewModel
import com.projectapp.tempus.ui.voice.compose.VoiceInputSheet
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
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
    
    private var speechRecognitionManager: SpeechRecognitionManager? = null
    
    // Callback to show voice sheet after permission granted
    private var onPermissionGranted: (() -> Unit)? = null
    
    // Permission launcher for RECORD_AUDIO
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted?.invoke()
        } else {
            Toast.makeText(
                requireContext(),
                "Cần cấp quyền micro để sử dụng Voice Command",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun checkAndRequestMicPermission(onGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            else -> {
                onPermissionGranted = onGranted
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize speech recognition
        speechRecognitionManager = SpeechRecognitionManager(requireContext())
        
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                val uiState by viewModel.ui.collectAsState()
                val weeks = buildWeeksAround(uiState.date)
                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))
                
                // Voice sheet state - controlled by Fragment for permission handling
                var showVoiceSheet by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val scope = rememberCoroutineScope()
                
                // Capture the setter for permission callback
                val openVoiceSheet = { showVoiceSheet = true }
                
                // Voice ViewModel - use correct constructor
                val supabaseForVoice = SupabaseClientProvider.client
                val voiceUserId = supabaseForVoice.auth.currentUserOrNull()?.id ?: ""
                val voiceRepo = SupabaseScheduleRepository()
                
                val voiceViewModel = remember {
                    VoiceViewModel(
                        application = requireActivity().application,
                        scheduleRepository = voiceRepo,
                        userId = voiceUserId
                    )
                }
                val voiceState by voiceViewModel.state.collectAsState()
                
                TimelineScreen(
                    blocks = uiState.blocks,
                    selectedDate = uiState.date,
                    monthYear = uiState.date.format(formatter),
                    weeks = weeks.map { it.days },
                    dailyQuote = uiState.dailyQuote,
                    isLoading = uiState.isLoading,
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
                    onVoiceClick = {
                        // Check permission first, then show voice sheet
                        checkAndRequestMicPermission {
                            openVoiceSheet()
                        }
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
                
                // Voice Input Bottom Sheet
                if (showVoiceSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { 
                            showVoiceSheet = false
                            voiceViewModel.reset()
                        },
                        sheetState = sheetState
                    ) {
                        VoiceInputSheet(
                            state = voiceState,
                            partialText = voiceState.let { 
                                when (it) {
                                    is com.projectapp.tempus.ui.voice.compose.VoiceInputState.Listening -> ""
                                    else -> ""
                                }
                            },
                            onStartListening = { voiceViewModel.startListening() },
                            onStopListening = { voiceViewModel.stopListening() },
                            onConfirmTask = { task ->
                                // Create task directly in database
                                voiceViewModel.createTask(task)
                                
                                // Close sheet and refresh timeline
                                scope.launch {
                                    sheetState.hide()
                                    showVoiceSheet = false
                                    // Refresh timeline to show new task
                                    viewModel.onRefresh()
                                }
                            },
                            onDismiss = {
                                scope.launch {
                                    sheetState.hide()
                                    showVoiceSheet = false
                                    voiceViewModel.reset()
                                }
                            }
                        )
                    }
                }
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
        speechRecognitionManager?.stopListening()
        speechRecognitionManager = null
    }
}
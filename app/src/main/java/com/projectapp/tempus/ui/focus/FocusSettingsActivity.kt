package com.projectapp.tempus.ui.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.projectapp.tempus.ui.focus.compose.BlockedAppsSheet
import com.projectapp.tempus.ui.focus.compose.FocusSettingsScreen
import com.projectapp.tempus.ui.theme.TempusTheme

/**
 * Activity for Focus Mode settings
 */
class FocusSettingsActivity : ComponentActivity() {
    
    private val viewModel: FocusViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TempusTheme {
                val uiState by viewModel.uiState.collectAsState()
                
                Scaffold { paddingValues ->
                    FocusSettingsScreen(
                        uiState = uiState,
                        onBackClick = { finish() },
                        onToggleFocusMode = { viewModel.toggleFocusMode(it) },
                        onToggleAutoStart = { viewModel.toggleAutoStart(it) },
                        onToggleShowOverlay = { viewModel.toggleShowOverlay(it) },
                        onShowAppPicker = { viewModel.showAppPicker() },
                        onUnblockApp = { viewModel.unblockApp(it) },
                        onRequestUsagePermission = { viewModel.requestUsagePermission() },
                        onRequestOverlayPermission = { viewModel.requestOverlayPermission() },
                        modifier = Modifier.padding(paddingValues)
                    )
                    
                    // App picker bottom sheet
                    if (uiState.showAppPicker) {
                        BlockedAppsSheet(
                            installedApps = uiState.installedApps,
                            isLoading = uiState.isLoadingApps,
                            onBlockApp = { viewModel.blockApp(it) },
                            onUnblockApp = { viewModel.unblockApp(it) },
                            onDismiss = { viewModel.hideAppPicker() }
                        )
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh permission status when returning from settings
        viewModel.toggleFocusMode(viewModel.uiState.value.focusModeEnabled)
    }
}

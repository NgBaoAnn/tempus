package com.projectapp.tempus.ui.setting

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
import com.projectapp.tempus.ui.setting.compose.PersonalizationScreen
import com.projectapp.tempus.ui.theme.TempusTheme

class PersonalizationActivity : ComponentActivity() {

    private val viewModel: PersonalizationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TempusTheme {
                val uiState by viewModel.uiState.collectAsState()
                
                Scaffold { paddingValues ->
                    PersonalizationScreen(
                        uiState = uiState,
                        activeDaysLabel = viewModel.getActiveDaysLabel(),
                        onBackClick = { finish() },
                        onShowTimePicker = { target -> viewModel.showTimePicker(target) },
                        onDismissTimePicker = { viewModel.dismissTimePicker() },
                        onTimeSelected = { hour, minute -> viewModel.onTimeSelected(hour, minute) },
                        onShowLifestyleSheet = { viewModel.showLifestyleSheet() },
                        onDismissLifestyleSheet = { viewModel.dismissLifestyleSheet() },
                        onSelectLifestyle = { preset -> viewModel.selectLifestyle(preset) },
                        onToggleDay = { day -> viewModel.toggleDay(day) },
                        onShowAddCustomPeriod = { viewModel.showAddCustomPeriodScreen() },
                        onDismissAddCustomPeriod = { viewModel.dismissAddCustomPeriodScreen() },
                        onUpdateNewPeriodName = { name -> viewModel.updateNewPeriodName(name) },
                        onUpdateNewPeriodDescription = { desc -> viewModel.updateNewPeriodDescription(desc) },
                        onUpdateNewPeriodColor = { color -> viewModel.updateNewPeriodColor(color) },
                        onSaveNewCustomPeriod = { viewModel.saveNewCustomPeriod() },
                        onRemoveCustomPeriod = { id -> viewModel.removeCustomTimePeriod(id) },
                        onShowResetConfirmation = { viewModel.showResetConfirmation() },
                        onDismissResetConfirmation = { viewModel.dismissResetConfirmation() },
                        onConfirmReset = { viewModel.confirmReset() },
                        onShowLabelSheet = { viewModel.showLabelSheet() },
                        onDismissLabelSheet = { viewModel.dismissLabelSheet() },
                        onSelectLabel = { label -> viewModel.selectLabel(label) },
                        getLabelDisplayName = { label -> viewModel.getLabelDisplayName(label) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
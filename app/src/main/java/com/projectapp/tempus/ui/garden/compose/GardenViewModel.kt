package com.projectapp.tempus.ui.garden.compose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.RepositoryProvider
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.domain.model.TreeGrowthCalculator
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType
import com.projectapp.tempus.domain.usecase.PointsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


data class GardenUiState(
    val trees: List<TreeUiModel> = emptyList(),
    val totalTrees: Int = 0,
    val matureTrees: Int = 0,
    val totalInvested: Int = 0,
    val currentPoints: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val showPlantDialog: Boolean = false,
    val affordableTrees: List<TreeType> = emptyList()
)


data class TreeUiModel(
    val id: Long,
    val name: String,
    val state: TreeState,
    val type: TreeType,
    val investedPoints: Int,
    val progressPercent: Float,
    val daysUntilDeath: Int,
    val isAlive: Boolean,
    val entity: TreeEntity
)


class GardenViewModel(application: Application) : AndroidViewModel(application) {
    
    
    private val repository = RepositoryProvider.getGamificationRepository(application)
    private val pointsManager = PointsManager(repository)
    private val treeCalculator = TreeGrowthCalculator()
    
    private val _uiState = MutableStateFlow(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            
            pointsManager.checkAndUpdateDeadTrees()
            
            
            launch {
                pointsManager.getUserPoints().collectLatest { userPoints ->
                    userPoints?.let {
                        _uiState.value = _uiState.value.copy(
                            currentPoints = it.totalPoints,
                            currentStreak = it.currentStreak
                        )
                    }
                }
            }
            
            
            launch {
                pointsManager.getAliveTrees().collectLatest { trees ->
                    val treeModels = trees.map { tree ->
                        val state = TreeState.fromString(tree.state)
                        val type = TreeType.fromString(tree.treeType)
                        TreeUiModel(
                            id = tree.id,
                            name = tree.name,
                            state = state,
                            type = type,
                            investedPoints = tree.investedPoints,
                            progressPercent = treeCalculator.getProgressPercent(tree.investedPoints),
                            daysUntilDeath = treeCalculator.getDaysUntilDeath(tree.lastWateredAt),
                            isAlive = tree.isAlive,
                            entity = tree
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        trees = treeModels,
                        totalTrees = trees.size,
                        matureTrees = trees.count { TreeState.fromString(it.state) == TreeState.TREE },
                        totalInvested = trees.sumOf { it.investedPoints },
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                pointsManager.checkAndUpdateDeadTrees()
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }
    
    fun showPlantTreeDialog() {
        viewModelScope.launch {
            val currentPoints = repository.getUserPointsOnce()?.totalPoints ?: 0
            val affordable = TreeType.getAffordableTrees(currentPoints)
            _uiState.value = _uiState.value.copy(
                showPlantDialog = true,
                affordableTrees = affordable
            )
        }
    }
    
    fun dismissPlantDialog() {
        _uiState.value = _uiState.value.copy(showPlantDialog = false)
    }
    
    fun plantTree(type: TreeType, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val treeId = pointsManager.plantTree(type)
            dismissPlantDialog()
            if (treeId != null) {
                onSuccess()
            } else {
                onError("Không đủ điểm để trồng cây!")
            }
        }
    }
    
    fun waterTree(treeId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val newState = pointsManager.waterTree(treeId)
            if (newState != null) {
                onSuccess()
            } else {
                onError("Không đủ điểm để tưới cây!")
            }
        }
    }
    
    fun deleteTree(treeId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.killTree(treeId)
                onSuccess()
            } catch (e: Exception) {
                onError("Lỗi xóa cây: ${e.message}")
            }
        }
    }
}

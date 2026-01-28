package com.projectapp.tempus.ui.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.gamification.SupabaseGamificationRepository
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity
import com.projectapp.tempus.domain.usecase.TreeInfo
import com.projectapp.tempus.domain.model.TreeState
import com.projectapp.tempus.domain.model.TreeType
import com.projectapp.tempus.domain.usecase.PointsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class GardenUiState(
    val trees: List<TreeEntity> = emptyList(),
    val userPoints: UserPointsEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalTrees: Int = 0,
    val matureTrees: Int = 0,
    val totalInvested: Int = 0
)

class GardenViewModel : ViewModel() {

    private val repository = SupabaseGamificationRepository()
    private val pointsManager = PointsManager(repository)

    private val _uiState = MutableStateFlow(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    init {
        loadData()
        checkDeadTrees()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Observe points
            launch {
                pointsManager.getUserPoints().collectLatest { points ->
                    _uiState.value = _uiState.value.copy(userPoints = points)
                }
            }

            // Observe trees
            launch {
                pointsManager.getAliveTrees().collectLatest { trees ->
                    val mature = trees.count { TreeState.fromString(it.state) == TreeState.TREE }
                    val invested = trees.sumOf { it.investedPoints }
                    
                    _uiState.value = _uiState.value.copy(
                        trees = trees,
                        totalTrees = trees.size,
                        matureTrees = mature,
                        totalInvested = invested,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun checkDeadTrees() {
        viewModelScope.launch {
            pointsManager.checkAndUpdateDeadTrees()
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            checkDeadTrees()
            // isLoading will be set to false when trees flow emits
        }
    }

    fun plantTree(type: TreeType, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val treeId = pointsManager.plantTree(type)
            if (treeId != null) {
                onSuccess()
                // isLoading will be set to false when trees flow emits
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
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
                onError("Không đủ điểm hoặc lỗi khi tưới cây!")
            }
        }
    }

    fun deleteTree(tree: TreeEntity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.killTree(tree.id)
                onSuccess()
                // isLoading will be set to false when trees flow emits
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onError(e.message ?: "Lỗi khi xóa cây")
            }
        }
    }
    
    suspend fun getTreeInfo(treeId: Long): TreeInfo? {
        return pointsManager.getTreeInfo(treeId)
    }
    
    fun getAffordableTrees(currentPoints: Int): List<TreeType> {
        return TreeType.getAffordableTrees(currentPoints)
    }
}

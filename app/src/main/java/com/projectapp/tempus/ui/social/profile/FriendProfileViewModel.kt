package com.projectapp.tempus.ui.social.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.social.dto.UserProfile
import com.projectapp.tempus.data.social.repository.FriendRepository
import com.projectapp.tempus.data.social.repository.RelationshipStatus
import com.projectapp.tempus.data.social.repository.SupabaseFriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FriendProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
    val isActionLoading: Boolean = false
)

class FriendProfileViewModel(
    private val friendRepository: FriendRepository = SupabaseFriendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendProfileUiState())
    val uiState: StateFlow<FriendProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            friendRepository.getUserProfile(userId)
                .onSuccess { profile ->
                    _uiState.update { it.copy(profile = profile, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Không thể tải thông tin profile: ${e.message}"
                        ) 
                    }
                }
        }
    }

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, error = null) }
            
            friendRepository.sendFriendRequest(userId)
                .onSuccess {
                    _uiState.update { it.copy(isActionLoading = false, successMessage = "Đã gửi lời mời!") }
                    
                    _uiState.value.profile?.id?.let { loadProfile(it) }
                }
                .onFailure { e ->
                    
                    val errorMessage = "Kết bạn không thành công"
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false, 
                            error = errorMessage
                        ) 
                    }
                }
        }
    }
    
    fun unfriend(friendshipId: String?) {
        
        
    }
    
    
    fun blockUser(userId: String) {
        performAction {
            friendRepository.blockUser(userId)
        }
    }

    private fun performAction(action: suspend () -> Result<Any>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, error = null) }
            
            action()
                .onSuccess {
                    _uiState.update { it.copy(isActionLoading = false, successMessage = "Thành công!") }
                    
                    _uiState.value.profile?.id?.let { loadProfile(it) }
                }
                .onFailure { e ->
                    
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false, 
                            error = "Thao tác không thành công"
                        ) 
                    }
                }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

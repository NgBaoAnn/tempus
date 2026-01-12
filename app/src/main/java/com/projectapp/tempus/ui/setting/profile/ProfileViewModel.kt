package com.projectapp.tempus.ui.setting.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.gamification.SupabaseGamificationRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

data class ProfileUiState(
    val email: String = "",
    val fullName: String = "",
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val treeCount: Int = 0,
    val memberSince: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class ProfileViewModel : ViewModel() {
    
    private val supabase = SupabaseClientProvider.client
    private val gamificationRepo = SupabaseGamificationRepository()
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Get current user from Supabase Auth
                val user = supabase.auth.currentUserOrNull()
                
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Chưa đăng nhập") }
                    return@launch
                }
                
                // Extract email and full_name from user metadata
                val email = user.email ?: ""
                val fullName = user.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: ""
                val createdAt = user.createdAt?.toString()?.take(10) ?: ""
                
                // Get gamification data
                val userPoints = gamificationRepo.getUserPointsOnce()
                val treeCount = gamificationRepo.getAliveTreeCount()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        email = email,
                        fullName = fullName,
                        totalPoints = userPoints?.totalPoints ?: 0,
                        currentStreak = userPoints?.currentStreak ?: 0,
                        treeCount = treeCount,
                        memberSince = formatDate(createdAt)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Lỗi tải thông tin: ${e.message}") 
                }
            }
        }
    }
    
    fun updateName(newName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }
            
            try {
                // Update user metadata in Supabase using modifyUser
                supabase.auth.modifyUser {
                    data = JsonObject(mapOf("full_name" to JsonPrimitive(newName)))
                }
                
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        fullName = newName,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, error = "Lỗi cập nhật: ${e.message}") 
                }
            }
        }
    }
    
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
    
    private fun formatDate(isoDate: String): String {
        if (isoDate.isEmpty()) return ""
        return try {
            val parts = isoDate.split("-")
            if (parts.size >= 3) {
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }
}


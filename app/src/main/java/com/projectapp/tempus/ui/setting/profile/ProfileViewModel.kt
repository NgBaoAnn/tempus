package com.projectapp.tempus.ui.setting.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.gamification.SupabaseGamificationRepository
import com.projectapp.tempus.data.user.SupabaseUserRepository
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
    val avatarUrl: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class ProfileViewModel : ViewModel() {
    
    private val supabase = SupabaseClientProvider.client
    private val gamificationRepo = SupabaseGamificationRepository()
    private val userRepository = SupabaseUserRepository()
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // First try to load from cache for instant display (offline support)
                val cachedProfile = com.projectapp.tempus.data.user.UserProfileCache.getProfile()
                if (cachedProfile != null) {
                    // Use cached data immediately
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            fullName = cachedProfile.username,
                            email = cachedProfile.email,
                            avatarUrl = cachedProfile.avatarUrl
                        )
                    }
                }
                
                // Get current user from Supabase Auth for auth check
                val authUser = supabase.auth.currentUserOrNull()
                
                if (authUser == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Chưa đăng nhập") }
                    return@launch
                }
                
                val email = authUser.email ?: ""
                
                // Fetch full user details from Repository (public.users table)
                val dbUser = userRepository.getCurrentUser()
                
                val createdAt = authUser.createdAt?.toString()?.take(10) ?: ""
                
                // Get gamification data
                val userPoints = gamificationRepo.getUserPointsOnce()
                val treeCount = gamificationRepo.getAliveTreeCount()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        email = email,
                        fullName = dbUser.username, // Load from DB
                        totalPoints = userPoints?.totalPoints ?: 0,
                        currentStreak = userPoints?.currentStreak ?: 0,
                        treeCount = treeCount,
                        memberSince = formatDate(createdAt),
                        avatarUrl = dbUser.avatar
                    )
                }
            } catch (e: Exception) {
                // If network fetch fails, keep showing cached data (already set above)
                // Only update loading state and error if no cached data was shown
                _uiState.update { currentState ->
                    if (currentState.fullName.isEmpty()) {
                        // No cached data, show error
                        currentState.copy(isLoading = false, error = "Lỗi tải thông tin: ${e.message}")
                    } else {
                        // Has cached data, just stop loading
                        currentState.copy(isLoading = false)
                    }
                }
            }
        }
    }
    
    fun updateName(newName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }
            
            try {
                // Get current DB user first
                val currentUser = userRepository.getCurrentUser()
                // Update username in DB
                val updatedUser = currentUser.copy(username = newName)
                userRepository.updateUser(updatedUser)
                
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
    fun uploadAvatar(byteArray: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }
            
            try {
                // Upload avatar and update user profile
                userRepository.uploadAvatar(byteArray)
                
                // Refresh profile to show new avatar
                loadProfile()
                
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, error = "Lỗi upload ảnh: ${e.message}") 
                }
            }
        }
    }
}


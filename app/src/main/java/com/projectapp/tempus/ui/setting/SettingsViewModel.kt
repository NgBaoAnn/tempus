package com.projectapp.tempus.ui.setting

import androidx.lifecycle.*
import com.projectapp.tempus.data.user.SupabaseUserRepository
import com.projectapp.tempus.domain.model.User
import com.projectapp.tempus.domain.usecase.UserUseCases
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val userRepo = SupabaseUserRepository()
    private val userUseCases = UserUseCases(userRepo)

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    fun loadUser() {
        viewModelScope.launch {
            try {
                // First try to load from cache for instant display (offline support)
                val cachedProfile = com.projectapp.tempus.data.user.UserProfileCache.getProfile()
                if (cachedProfile != null) {
                    // Use cached data immediately
                    _user.value = User(
                        id = "", // ID not needed for display
                        username = cachedProfile.username,
                        email = cachedProfile.email,
                        avatar = cachedProfile.avatarUrl,
                        themeColor = "",
                        appColor = ""
                    )
                }
                
                // Then try to fetch fresh data from network
                // This will update the cache and refresh the UI if successful
                _user.value = userUseCases.getCurrentUser()
            } catch (e: Exception) {
                // If network fetch fails, keep showing cached data (already set above)
                // Only log error, don't crash
                e.printStackTrace()
            }
        }
    }

    fun updateTheme(theme: String, appColor: String) {
        viewModelScope.launch {
            val current = userUseCases.getCurrentUser()
            userUseCases.updateUser(
                current.copy(
                    themeColor = theme,
                    appColor = appColor
                )
            )
        }
    }
}
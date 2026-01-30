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
                
                val cachedProfile = com.projectapp.tempus.data.user.UserProfileCache.getProfile()
                if (cachedProfile != null) {
                    
                    _user.value = User(
                        id = "", 
                        username = cachedProfile.username,
                        email = cachedProfile.email,
                        avatar = cachedProfile.avatarUrl,
                        themeColor = "",
                        appColor = ""
                    )
                }
                
                
                val fetchedUser = userUseCases.getCurrentUser()
                _user.value = fetchedUser
                
                
                fetchedUser.themeColor?.takeIf { it.isNotEmpty() }?.let { themeColor ->
                    val mode = com.projectapp.tempus.ui.theme.ThemeMode.fromValue(themeColor)
                    com.projectapp.tempus.ui.theme.ThemeManager.updateThemeLocally(mode)
                }
            } catch (e: Exception) {
                
                
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
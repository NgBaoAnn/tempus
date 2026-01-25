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
                _user.value = userUseCases.getCurrentUser()
            } catch (e: Exception) {
                // Log error or set error state
                // For now, prevent crash
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
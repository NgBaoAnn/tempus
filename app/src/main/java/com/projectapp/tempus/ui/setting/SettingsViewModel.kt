package com.projectapp.tempus.ui.setting

import androidx.lifecycle.*
import com.projectapp.tempus.data.user.SupabaseUserRepository
import com.projectapp.tempus.domain.user.model.User
import com.projectapp.tempus.domain.user.usecase.GetCurrentUserUseCase
import com.projectapp.tempus.domain.user.usecase.UpdateUserUseCase
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val userRepo = SupabaseUserRepository()
    private val getUser = GetCurrentUserUseCase(userRepo)
    private val updateUser = UpdateUserUseCase(userRepo)

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    fun loadUser() {
        viewModelScope.launch {
            _user.value = getUser()
        }
    }

    fun updateTheme(theme: String, appColor: String) {
        viewModelScope.launch {
            val current = getUser()
            updateUser(
                current.copy(
                    themeColor = theme,
                    appColor = appColor
                )
            )
        }
    }
}
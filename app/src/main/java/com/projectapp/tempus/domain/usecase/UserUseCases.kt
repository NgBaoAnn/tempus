package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.data.user.UserRepository
import com.projectapp.tempus.domain.model.User

class UserUseCases(
    private val repository: UserRepository
) {
    suspend fun getCurrentUser(): User {
        return repository.getCurrentUser()
    }

    suspend fun updateUser(user: User) {
        repository.updateUser(user)
    }
}

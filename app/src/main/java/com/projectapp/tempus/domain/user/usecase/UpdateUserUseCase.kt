package com.projectapp.tempus.domain.user.usecase

import com.projectapp.tempus.data.user.UserRepository
import com.projectapp.tempus.domain.user.model.User

class UpdateUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User) {
        repository.updateUser(user)
    }
}
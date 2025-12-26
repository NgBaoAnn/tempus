package com.projectapp.tempus.data.user

import com.projectapp.tempus.domain.user.model.User

interface UserRepository {
    suspend fun getCurrentUser(): User
    suspend fun updateUser(user: User)
}
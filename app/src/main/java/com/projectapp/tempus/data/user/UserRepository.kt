package com.projectapp.tempus.data.user

import com.projectapp.tempus.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): User
    suspend fun updateUser(user: User)
    suspend fun uploadAvatar(byteArray: ByteArray): String
    suspend fun updateThemeColor(themeColor: String)
}
package com.projectapp.tempus.data.user

import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.user.dto.UserDto
import com.projectapp.tempus.data.user.dto.toDomain
import com.projectapp.tempus.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class SupabaseUserRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : UserRepository {

    override suspend fun getCurrentUser(): User {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

        val list = supabase.from("users")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeList<UserDto>()

        val userDto = list.firstOrNull() ?: throw IllegalStateException("User data not found in database")
        return userDto.toDomain()
    }

    override suspend fun updateUser(user: User) {
        supabase.from("users")
            .update(
                mapOf(
                    "username" to user.username,
                    "avatar" to user.avatar,
                    "theme_color" to user.themeColor,
                    "app_color" to user.appColor
                )
            ) {
                filter {
                    eq("id", user.id)
                }
            }
    }

    override suspend fun uploadAvatar(byteArray: ByteArray): String {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

        // Use a unique name to avoid caching issues or collisions
        val fileName = "$userId/${System.currentTimeMillis()}.jpg"
        val bucket = supabase.storage.from("avatars")

        // Upload file
        bucket.upload(fileName, byteArray)

        // Get public URL
        val publicUrl = bucket.publicUrl(fileName)

        // Update user profile with new avatar URL
        val user = getCurrentUser()
        val updatedUser = user.copy(avatar = publicUrl)
        updateUser(updatedUser)

        return publicUrl
    }
}
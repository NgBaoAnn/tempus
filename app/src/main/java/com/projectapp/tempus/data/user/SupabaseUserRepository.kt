package com.projectapp.tempus.data.user

import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.user.dto.UserDto
import com.projectapp.tempus.data.user.dto.toDomain
import com.projectapp.tempus.domain.user.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from

class SupabaseUserRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : UserRepository {

    override suspend fun getCurrentUser(): User {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")
        //val userId = "cc3b9e4a-dce2-454c-905f-4324134de55f"

        val list = supabase.from("users")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeList<UserDto>()

        return list.first().toDomain()
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
}
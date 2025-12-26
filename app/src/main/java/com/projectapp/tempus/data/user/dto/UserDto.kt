package com.projectapp.tempus.data.user.dto

import com.projectapp.tempus.domain.user.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val created_at: String? = null,
    val avatar: String?,
    val theme_color: String?,
    val app_color: String?
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        avatar = avatar,
        themeColor = theme_color,
        appColor = app_color
    )
}

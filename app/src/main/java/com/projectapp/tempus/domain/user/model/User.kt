package com.projectapp.tempus.domain.user.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val avatar: String?,
    val themeColor: String?,
    val appColor: String?
)
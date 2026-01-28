package com.projectapp.tempus.data.notes.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO cho Notes trên Supabase
 */
@Serializable
data class NoteRow(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val title: String = "",
    val content: String,
    @SerialName("is_pinned")
    val isPinned: Boolean = false,
    val color: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * DTO để insert note mới (không cần id vì DB tự generate)
 */
@Serializable
data class NoteInsert(
    @SerialName("user_id")
    val userId: String,
    val title: String = "",
    val content: String,
    @SerialName("is_pinned")
    val isPinned: Boolean = false,
    val color: String? = null
)

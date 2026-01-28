package com.projectapp.tempus.data.notes.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.projectapp.tempus.data.notes.dto.NoteRow
import java.time.Instant

/**
 * Entity lưu trữ ghi chú của người dùng
 * Hỗ trợ sync với Supabase
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(), // UUID for Supabase sync
    val userId: String = "",  // User ID for multi-user support
    val title: String = "",
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val color: String? = null,  // Optional color for note cards
    
    // Sync fields
    val syncStatus: String = "PENDING_CREATE",  // SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE
    val localUpdatedAt: Long = System.currentTimeMillis(),
    val serverUpdatedAt: Long? = null
) {
    fun toRow(): NoteRow = NoteRow(
        id = id,
        userId = userId,
        title = title,
        content = content,
        isPinned = isPinned,
        color = color,
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        updatedAt = Instant.ofEpochMilli(updatedAt).toString()
    )
    
    companion object {
        fun fromRow(row: NoteRow): NoteEntity {
            val createdAtMillis = try {
                Instant.parse(row.createdAt).toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
            val updatedAtMillis = try {
                Instant.parse(row.updatedAt).toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
            
            return NoteEntity(
                id = row.id,
                userId = row.userId,
                title = row.title,
                content = row.content,
                isPinned = row.isPinned,
                color = row.color,
                createdAt = createdAtMillis,
                updatedAt = updatedAtMillis,
                syncStatus = "SYNCED",
                localUpdatedAt = updatedAtMillis,
                serverUpdatedAt = updatedAtMillis
            )
        }
    }
}


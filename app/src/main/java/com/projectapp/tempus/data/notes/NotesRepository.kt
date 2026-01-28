package com.projectapp.tempus.data.notes

import android.content.Context
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.notes.entity.NoteEntity
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository cho Notes - cung cấp interface duy nhất để truy cập dữ liệu
 * Hỗ trợ offline-first với sync Supabase
 */
class NotesRepository(context: Context) {
    
    private val noteDao: NoteDao = NotesDatabase.getDatabase(context).noteDao()
    
    /**
     * Lấy user ID hiện tại
     */
    private fun getCurrentUserId(): String {
        return SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: ""
    }
    
    /**
     * Lấy tất cả ghi chú của user hiện tại
     */
    fun getAllNotes(): Flow<List<NoteEntity>> {
        val userId = getCurrentUserId()
        return if (userId.isNotEmpty()) {
            noteDao.getAllNotes(userId)
        } else {
            noteDao.getAllNotes()
        }
    }
    
    /**
     * Lấy ghi chú theo ID
     */
    suspend fun getNoteById(noteId: String): NoteEntity? = noteDao.getNoteById(noteId)
    
    /**
     * Tìm kiếm ghi chú theo nội dung
     */
    fun searchNotes(query: String): Flow<List<NoteEntity>> {
        val userId = getCurrentUserId()
        return if (userId.isNotEmpty()) {
            noteDao.searchNotes(userId, query)
        } else {
            noteDao.searchNotes(query)
        }
    }
    
    /**
     * Tạo ghi chú mới
     * @return ID của ghi chú vừa tạo
     */
    suspend fun createNote(title: String, content: String): String {
        val userId = getCurrentUserId()
        val noteId = UUID.randomUUID().toString()
        val note = NoteEntity(
            id = noteId,
            userId = userId,
            title = title.trim(),
            content = content.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING_CREATE",
            localUpdatedAt = System.currentTimeMillis()
        )
        noteDao.insertNote(note)
        return noteId
    }
    
    /**
     * Cập nhật ghi chú
     */
    suspend fun updateNote(noteId: String, title: String, content: String) {
        val existing = noteDao.getNoteById(noteId) ?: return
        val updated = existing.copy(
            title = title.trim(),
            content = content.trim(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = if (existing.syncStatus == "SYNCED") "PENDING_UPDATE" else existing.syncStatus,
            localUpdatedAt = System.currentTimeMillis()
        )
        noteDao.updateNote(updated)
    }
    
    /**
     * Xóa ghi chú (soft delete cho sync, hard delete nếu chưa sync)
     */
    suspend fun deleteNote(noteId: String) {
        val existing = noteDao.getNoteById(noteId) ?: return
        if (existing.syncStatus == "PENDING_CREATE") {
            // Chưa sync lên server -> xóa luôn
            noteDao.deleteNoteById(noteId)
        } else {
            // Đã sync -> đánh dấu pending delete
            val updated = existing.copy(
                syncStatus = "PENDING_DELETE",
                localUpdatedAt = System.currentTimeMillis()
            )
            noteDao.updateNote(updated)
        }
    }
    
    /**
     * Toggle pin/unpin ghi chú
     */
    suspend fun togglePin(noteId: String) {
        noteDao.togglePin(noteId)
    }
    
    /**
     * Cập nhật màu ghi chú
     */
    suspend fun updateNoteColor(noteId: String, color: String?) {
        val existing = noteDao.getNoteById(noteId) ?: return
        val updated = existing.copy(
            color = color,
            updatedAt = System.currentTimeMillis(),
            syncStatus = if (existing.syncStatus == "SYNCED") "PENDING_UPDATE" else existing.syncStatus,
            localUpdatedAt = System.currentTimeMillis()
        )
        noteDao.updateNote(updated)
    }
    
    // ==================== SYNC OPERATIONS ====================
    
    /**
     * Lấy các notes cần sync
     */
    suspend fun getPendingNotes(): List<NoteEntity> = noteDao.getPendingNotes()
    
    /**
     * Đánh dấu note đã sync
     */
    suspend fun markNoteSynced(noteId: String, serverTime: Long) = noteDao.markNoteSynced(noteId, serverTime)
    
    /**
     * Xóa tất cả notes (dùng khi logout)
     */
    suspend fun clearAllNotes() = noteDao.deleteAllNotes()
    
    /**
     * Insert nhiều notes cùng lúc (khi pull từ server)
     */
    suspend fun insertAll(notes: List<NoteEntity>) = noteDao.insertAll(notes)
    
    /**
     * Xóa note đã sync khỏi local
     */
    suspend fun hardDeleteNote(noteId: String) = noteDao.deleteNoteById(noteId)
}


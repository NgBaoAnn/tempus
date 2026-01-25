package com.projectapp.tempus.data.notes

import android.content.Context
import com.projectapp.tempus.data.notes.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository cho Notes - cung cấp interface duy nhất để truy cập dữ liệu
 */
class NotesRepository(context: Context) {
    
    private val noteDao: NoteDao = NotesDatabase.getDatabase(context).noteDao()
    
    /**
     * Lấy tất cả ghi chú
     */
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    
    /**
     * Lấy ghi chú theo ID
     */
    suspend fun getNoteById(noteId: Long): NoteEntity? = noteDao.getNoteById(noteId)
    
    /**
     * Tìm kiếm ghi chú theo nội dung
     */
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)
    
    /**
     * Tạo ghi chú mới
     * @return ID của ghi chú vừa tạo
     */
    suspend fun createNote(title: String, content: String): Long {
        val note = NoteEntity(
            title = title.trim(),
            content = content.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return noteDao.insertNote(note)
    }
    
    /**
     * Cập nhật ghi chú
     */
    suspend fun updateNote(noteId: Long, title: String, content: String) {
        val existing = noteDao.getNoteById(noteId) ?: return
        val updated = existing.copy(
            title = title.trim(),
            content = content.trim(),
            updatedAt = System.currentTimeMillis()
        )
        noteDao.updateNote(updated)
    }
    
    /**
     * Xóa ghi chú
     */
    suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNoteById(noteId)
    }
    
    /**
     * Toggle pin/unpin ghi chú
     */
    suspend fun togglePin(noteId: Long) {
        noteDao.togglePin(noteId)
    }
    
    /**
     * Cập nhật màu ghi chú
     */
    suspend fun updateNoteColor(noteId: Long, color: String?) {
        val existing = noteDao.getNoteById(noteId) ?: return
        val updated = existing.copy(
            color = color,
            updatedAt = System.currentTimeMillis()
        )
        noteDao.updateNote(updated)
    }
}

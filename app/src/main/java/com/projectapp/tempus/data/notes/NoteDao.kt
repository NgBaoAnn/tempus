package com.projectapp.tempus.data.notes

import androidx.room.*
import com.projectapp.tempus.data.notes.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho các operations liên quan đến Notes
 */
@Dao
interface NoteDao {
    
    /**
     * Lấy tất cả ghi chú, sắp xếp theo pinned và thời gian cập nhật
     */
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    /**
     * Lấy ghi chú theo ID
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NoteEntity?
    
    /**
     * Tìm kiếm ghi chú theo title hoặc content
     */
    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%' 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    /**
     * Thêm ghi chú mới
     */
    @Insert
    suspend fun insertNote(note: NoteEntity): Long
    
    /**
     * Cập nhật ghi chú
     */
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    /**
     * Xóa ghi chú
     */
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    /**
     * Xóa ghi chú theo ID
     */
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)
    
    /**
     * Toggle pin status
     */
    @Query("UPDATE notes SET isPinned = NOT isPinned, updatedAt = :timestamp WHERE id = :noteId")
    suspend fun togglePin(noteId: Long, timestamp: Long = System.currentTimeMillis())
}

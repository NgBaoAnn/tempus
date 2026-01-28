package com.projectapp.tempus.data.notes

import androidx.room.*
import com.projectapp.tempus.data.notes.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho các operations liên quan đến Notes
 * Hỗ trợ sync với Supabase
 */
@Dao
interface NoteDao {
    
    /**
     * Lấy tất cả ghi chú của user, sắp xếp theo pinned và thời gian cập nhật
     */
    @Query("SELECT * FROM notes WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(userId: String): Flow<List<NoteEntity>>
    
    /**
     * Lấy tất cả ghi chú (legacy - không filter userId)
     */
    @Query("SELECT * FROM notes WHERE syncStatus != 'PENDING_DELETE' ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    /**
     * Lấy ghi chú theo ID
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?
    
    /**
     * Tìm kiếm ghi chú theo title hoặc content
     */
    @Query("""
        SELECT * FROM notes 
        WHERE userId = :userId 
          AND syncStatus != 'PENDING_DELETE'
          AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(userId: String, query: String): Flow<List<NoteEntity>>
    
    /**
     * Legacy search (không filter userId)
     */
    @Query("""
        SELECT * FROM notes 
        WHERE syncStatus != 'PENDING_DELETE'
          AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    /**
     * Thêm ghi chú mới
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)
    
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
    suspend fun deleteNoteById(noteId: String)
    
    /**
     * Toggle pin status
     */
    @Query("UPDATE notes SET isPinned = NOT isPinned, updatedAt = :timestamp, syncStatus = 'PENDING_UPDATE', localUpdatedAt = :timestamp WHERE id = :noteId")
    suspend fun togglePin(noteId: String, timestamp: Long = System.currentTimeMillis())
    
    // ==================== SYNC OPERATIONS ====================
    
    /**
     * Lấy các notes cần sync lên server
     */
    @Query("SELECT * FROM notes WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingNotes(): List<NoteEntity>
    
    /**
     * Đánh dấu note đã sync
     */
    @Query("UPDATE notes SET syncStatus = 'SYNCED', serverUpdatedAt = :serverTime WHERE id = :noteId")
    suspend fun markNoteSynced(noteId: String, serverTime: Long)
    
    /**
     * Xóa tất cả notes của user (dùng khi logout)
     */
    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllNotesByUser(userId: String)
    
    /**
     * Xóa tất cả notes (dùng khi logout - clear all)
     */
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
    
    /**
     * Insert nhiều notes cùng lúc (dùng khi pull từ server)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)
}


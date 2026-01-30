package com.projectapp.tempus.data.notes

import androidx.room.*
import com.projectapp.tempus.data.notes.entity.NoteEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDao {
    
    
    @Query("SELECT * FROM notes WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(userId: String): Flow<List<NoteEntity>>
    
    
    @Query("SELECT * FROM notes WHERE syncStatus != 'PENDING_DELETE' ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?
    
    
    @Query("""
        SELECT * FROM notes 
        WHERE userId = :userId 
          AND syncStatus != 'PENDING_DELETE'
          AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(userId: String, query: String): Flow<List<NoteEntity>>
    
    
    @Query("""
        SELECT * FROM notes 
        WHERE syncStatus != 'PENDING_DELETE'
          AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)
    
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)
    
    
    @Query("UPDATE notes SET isPinned = NOT isPinned, updatedAt = :timestamp, syncStatus = 'PENDING_UPDATE', localUpdatedAt = :timestamp WHERE id = :noteId")
    suspend fun togglePin(noteId: String, timestamp: Long = System.currentTimeMillis())
    
    
    @Query("SELECT * FROM notes WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingNotes(): List<NoteEntity>
    
    
    @Query("UPDATE notes SET syncStatus = 'SYNCED', serverUpdatedAt = :serverTime WHERE id = :noteId")
    suspend fun markNoteSynced(noteId: String, serverTime: Long)
    
    
    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllNotesByUser(userId: String)
    
    
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
    
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)
}


package com.projectapp.tempus.data.notes

import android.content.Context
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.notes.entity.NoteEntity
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.Flow
import java.util.UUID


class NotesRepository(context: Context) {
    
    private val noteDao: NoteDao = NotesDatabase.getDatabase(context).noteDao()
    
    
    private fun getCurrentUserId(): String {
        return SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: ""
    }
    
    
    fun getAllNotes(): Flow<List<NoteEntity>> {
        val userId = getCurrentUserId()
        return if (userId.isNotEmpty()) {
            noteDao.getAllNotes(userId)
        } else {
            noteDao.getAllNotes()
        }
    }
    
    
    suspend fun getNoteById(noteId: String): NoteEntity? = noteDao.getNoteById(noteId)
    
    
    fun searchNotes(query: String): Flow<List<NoteEntity>> {
        val userId = getCurrentUserId()
        return if (userId.isNotEmpty()) {
            noteDao.searchNotes(userId, query)
        } else {
            noteDao.searchNotes(query)
        }
    }
    
    
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
    
    
    suspend fun deleteNote(noteId: String) {
        val existing = noteDao.getNoteById(noteId) ?: return
        if (existing.syncStatus == "PENDING_CREATE") {
            
            noteDao.deleteNoteById(noteId)
        } else {
            
            val updated = existing.copy(
                syncStatus = "PENDING_DELETE",
                localUpdatedAt = System.currentTimeMillis()
            )
            noteDao.updateNote(updated)
        }
    }
    
    
    suspend fun togglePin(noteId: String) {
        noteDao.togglePin(noteId)
    }
    
    
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
    
    
    suspend fun getPendingNotes(): List<NoteEntity> = noteDao.getPendingNotes()
    
    
    suspend fun getPendingSyncNotes(): List<NoteEntity> = getPendingNotes()
    
    
    suspend fun markNoteSynced(noteId: String, serverTime: Long) = noteDao.markNoteSynced(noteId, serverTime)
    
    
    suspend fun markAsSynced(noteId: String) = noteDao.markNoteSynced(noteId, System.currentTimeMillis())
    
    
    suspend fun clearAllNotes() = noteDao.deleteAllNotes()
    
    
    suspend fun insertAll(notes: List<NoteEntity>) = noteDao.insertAll(notes)
    
    
    suspend fun insertOrUpdate(note: NoteEntity) {
        val existing = noteDao.getNoteById(note.id)
        if (existing == null) {
            noteDao.insertNote(note)
        } else {
            noteDao.updateNote(note)
        }
    }
    
    
    suspend fun hardDeleteNote(noteId: String) = noteDao.deleteNoteById(noteId)
}


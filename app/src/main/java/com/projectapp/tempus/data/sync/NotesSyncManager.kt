package com.projectapp.tempus.data.sync

import android.util.Log
import com.projectapp.tempus.data.notes.NotesRepository
import com.projectapp.tempus.data.notes.SupabaseNotesRepository
import com.projectapp.tempus.data.notes.entity.NoteEntity

/**
 * Sync Manager cho Notes data
 * Push local Room data lên Supabase khi logout
 */
class NotesSyncManager(
    private val localRepo: NotesRepository,
    private val remoteRepo: SupabaseNotesRepository
) {
    companion object {
        private const val TAG = "NotesSync"
        
        // Sync status constants
        const val SYNCED = "SYNCED"
        const val PENDING_CREATE = "PENDING_CREATE"
        const val PENDING_UPDATE = "PENDING_UPDATE"
        const val PENDING_DELETE = "PENDING_DELETE"
    }
    
    /**
     * Push tất cả notes data từ Room lên Supabase
     * Gọi trước khi logout
     */
    suspend fun pushToServer(): Result<NotesSyncResult> {
        return try {
            var inserted = 0
            var updated = 0
            var deleted = 0
            var failed = 0
            
            // Get all notes that need sync
            val pendingNotes = localRepo.getPendingSyncNotes()
            Log.d(TAG, "=== NOTES SYNC START ===")
            Log.d(TAG, "Found ${pendingNotes.size} pending notes to sync")
            
            for (note in pendingNotes) {
                Log.d(TAG, "Processing note: id=${note.id}, status=${note.syncStatus}, title=${note.title}")
                try {
                    when (note.syncStatus) {
                        PENDING_CREATE -> {
                            Log.d(TAG, "Attempting INSERT for note ${note.id}")
                            val success = remoteRepo.insertNote(note)
                            if (success) {
                                localRepo.markAsSynced(note.id)
                                inserted++
                                Log.d(TAG, "INSERT SUCCESS: ${note.id}")
                            } else {
                                failed++
                                Log.e(TAG, "INSERT FAILED: ${note.id}")
                            }
                        }
                        PENDING_UPDATE -> {
                            Log.d(TAG, "Attempting UPDATE for note ${note.id}")
                            val success = remoteRepo.updateNote(note)
                            if (success) {
                                localRepo.markAsSynced(note.id)
                                updated++
                                Log.d(TAG, "UPDATE SUCCESS: ${note.id}")
                            } else {
                                failed++
                                Log.e(TAG, "UPDATE FAILED: ${note.id}")
                            }
                        }
                        PENDING_DELETE -> {
                            Log.d(TAG, "Attempting DELETE for note ${note.id}")
                            val success = remoteRepo.deleteNote(note.id)
                            if (success) {
                                // Actually delete from local after syncing delete
                                localRepo.hardDeleteNote(note.id)
                                deleted++
                                Log.d(TAG, "DELETE SUCCESS: ${note.id}")
                            } else {
                                failed++
                                Log.e(TAG, "DELETE FAILED: ${note.id}")
                            }
                        }
                        else -> {
                            Log.d(TAG, "Skipping note ${note.id} - status: ${note.syncStatus}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync note ${note.id}: ${e.message}", e)
                    failed++
                }
            }
            
            val result = NotesSyncResult(
                inserted = inserted,
                updated = updated,
                deleted = deleted,
                failed = failed
            )
            
            Log.d(TAG, "=== NOTES SYNC COMPLETED: $result ===")
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Pull notes từ Supabase về Room
     * Gọi sau khi login
     */
    suspend fun pullFromServer(userId: String): Result<NotesSyncResult> {
        return try {
            var inserted = 0
            
            // Get all notes from server
            val serverNotes = remoteRepo.getAllNotes()
            
            for (noteRow in serverNotes) {
                try {
                    // Convert to entity using fromRow
                    val entity = NoteEntity.fromRow(noteRow)
                    
                    // Insert or update in local
                    localRepo.insertOrUpdate(entity)
                    inserted++
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to pull note ${noteRow.id}", e)
                }
            }
            
            val result = NotesSyncResult(
                inserted = inserted,
                updated = 0,
                deleted = 0,
                failed = 0
            )
            
            Log.d(TAG, "Pull completed: $result")
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Pull failed", e)
            Result.failure(e)
        }
    }
}

/**
 * Kết quả sync notes
 */
data class NotesSyncResult(
    val inserted: Int,
    val updated: Int,
    val deleted: Int,
    val failed: Int
) {
    fun summary(): String {
        return "Inserted: $inserted, Updated: $updated, Deleted: $deleted, Failed: $failed"
    }
}


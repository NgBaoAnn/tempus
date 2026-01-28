package com.projectapp.tempus.data.notes

import android.util.Log
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.notes.dto.NoteInsert
import com.projectapp.tempus.data.notes.dto.NoteRow
import com.projectapp.tempus.data.notes.entity.NoteEntity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from

/**
 * Repository để sync Notes với Supabase
 */
class SupabaseNotesRepository {
    
    private val supabase = SupabaseClientProvider.client
    private val TABLE_NAME = "notes"
    
    private fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }
    
    /**
     * Lấy tất cả notes của user từ Supabase
     */
    suspend fun getAllNotes(): List<NoteRow> {
        val userId = getCurrentUserId() ?: return emptyList()
        
        return try {
            supabase.from(TABLE_NAME)
                .select()
                .decodeList<NoteRow>()
                .filter { it.userId == userId }
        } catch (e: Exception) {
            Log.e("SupabaseNotesRepo", "Error fetching notes: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Insert note mới lên Supabase
     */
    suspend fun insertNote(entity: NoteEntity): Boolean {
        val userId = getCurrentUserId()
        Log.d("SupabaseNotesRepo", "=== INSERT NOTE START ===")
        Log.d("SupabaseNotesRepo", "Entity id: ${entity.id}")
        Log.d("SupabaseNotesRepo", "Entity userId: ${entity.userId}")
        Log.d("SupabaseNotesRepo", "Current auth userId: $userId")
        Log.d("SupabaseNotesRepo", "Title: ${entity.title}")
        Log.d("SupabaseNotesRepo", "Content length: ${entity.content.length}")
        
        if (userId == null) {
            Log.e("SupabaseNotesRepo", "INSERT FAILED: User not authenticated")
            return false
        }
        
        return try {
            // Use authenticated user's ID for RLS compliance
            val insertData = mapOf(
                "id" to entity.id,
                "user_id" to userId,  // Use current auth user ID, not entity.userId
                "title" to entity.title,
                "content" to entity.content,
                "is_pinned" to entity.isPinned,
                "color" to entity.color
            )
            
            Log.d("SupabaseNotesRepo", "Inserting with data: $insertData")
            
            supabase.from(TABLE_NAME).insert(insertData)
            
            Log.d("SupabaseNotesRepo", "=== INSERT SUCCESS: ${entity.id} ===")
            true
        } catch (e: Exception) {
            Log.e("SupabaseNotesRepo", "=== INSERT FAILED ===")
            Log.e("SupabaseNotesRepo", "Error type: ${e.javaClass.simpleName}")
            Log.e("SupabaseNotesRepo", "Error message: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Update note trên Supabase
     */
    suspend fun updateNote(entity: NoteEntity): Boolean {
        return try {
            supabase.from(TABLE_NAME)
                .update(mapOf(
                    "title" to entity.title,
                    "content" to entity.content,
                    "is_pinned" to entity.isPinned,
                    "color" to entity.color
                )) {
                    filter {
                        eq("id", entity.id)
                    }
                }
            
            Log.d("SupabaseNotesRepo", "Update note success: ${entity.id}")
            true
        } catch (e: Exception) {
            Log.e("SupabaseNotesRepo", "Update note failed: ${e.message}")
            false
        }
    }
    
    /**
     * Xóa note khỏi Supabase
     */
    suspend fun deleteNote(noteId: String): Boolean {
        return try {
            supabase.from(TABLE_NAME)
                .delete {
                    filter {
                        eq("id", noteId)
                    }
                }
            
            Log.d("SupabaseNotesRepo", "Delete note success: $noteId")
            true
        } catch (e: Exception) {
            Log.e("SupabaseNotesRepo", "Delete note failed: ${e.message}")
            false
        }
    }
}

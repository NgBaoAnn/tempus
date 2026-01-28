package com.projectapp.tempus.data.notes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.projectapp.tempus.data.notes.entity.NoteEntity

/**
 * Room Database cho hệ thống Notes
 * Version 2: Added userId, syncStatus, UUID primary key for Supabase sync
 */
@Database(
    entities = [NoteEntity::class],
    version = 2,
    exportSchema = false
)
abstract class NotesDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null
        
        fun getDatabase(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                )
                    .fallbackToDestructiveMigration() // Cho phép xóa data cũ khi upgrade
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


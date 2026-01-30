package com.projectapp.tempus.data.focus

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [BlockedAppEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FocusModeDatabase : RoomDatabase() {
    
    abstract fun blockedAppDao(): BlockedAppDao
    
    companion object {
        @Volatile
        private var INSTANCE: FocusModeDatabase? = null
        
        fun getInstance(context: Context): FocusModeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusModeDatabase::class.java,
                    "focus_mode_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.projectapp.tempus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.projectapp.tempus.data.local.dao.ScheduleDao
import com.projectapp.tempus.data.local.entity.*
import com.projectapp.tempus.data.gamification.GamificationDao
import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity

/**
 * Unified Room Database cho Tempus App
 * 
 * Bao gồm:
 * - Schedule entities (mới cho offline-first)
 * - Gamification entities (migrate từ GamificationDatabase)
 */
@Database(
    entities = [
        // Schedule entities
        ScheduleEntity::class,
        ScheduleItemEntity::class,
        SubTaskEntity::class,
        CategoryEntity::class,
        EditedVersionEntity::class,
        
        // Gamification entities (migrate từ GamificationDatabase)
        UserPointsEntity::class,
        PointHistoryEntity::class,
        TreeEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TempusDatabase : RoomDatabase() {
    
    abstract fun scheduleDao(): ScheduleDao
    abstract fun gamificationDao(): GamificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: TempusDatabase? = null
        
        fun getDatabase(context: Context): TempusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TempusDatabase::class.java,
                    "tempus_database"
                )
                    // Cho development, production cần proper migration
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Clear instance (for testing or logout)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}

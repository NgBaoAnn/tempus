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


@Database(
    entities = [
        
        ScheduleEntity::class,
        ScheduleItemEntity::class,
        SubTaskEntity::class,
        CategoryEntity::class,
        EditedVersionEntity::class,
        
        
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
                    
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        
        fun clearInstance() {
            INSTANCE = null
        }
    }
}

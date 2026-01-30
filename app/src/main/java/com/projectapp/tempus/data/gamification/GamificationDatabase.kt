package com.projectapp.tempus.data.gamification

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.projectapp.tempus.data.gamification.entity.PointHistoryEntity
import com.projectapp.tempus.data.gamification.entity.TreeEntity
import com.projectapp.tempus.data.gamification.entity.UserPointsEntity


@Database(
    entities = [
        UserPointsEntity::class,
        PointHistoryEntity::class,
        TreeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GamificationDatabase : RoomDatabase() {
    
    abstract fun gamificationDao(): GamificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: GamificationDatabase? = null
        
        fun getDatabase(context: Context): GamificationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GamificationDatabase::class.java,
                    "gamification_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

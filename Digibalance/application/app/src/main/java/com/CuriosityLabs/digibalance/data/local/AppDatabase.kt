package com.CuriosityLabs.digibalance.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.CuriosityLabs.digibalance.data.local.dao.ParentalRuleDao
import com.CuriosityLabs.digibalance.data.local.dao.AppUsageDao
import com.CuriosityLabs.digibalance.data.local.dao.FeedbackQueueDao
import com.CuriosityLabs.digibalance.data.local.entity.ParentalRuleEntity
import com.CuriosityLabs.digibalance.data.local.entity.AppUsageEntity
import com.CuriosityLabs.digibalance.data.local.entity.FeedbackQueueEntity

@Database(
    entities = [
        ParentalRuleEntity::class,
        AppUsageEntity::class,
        FeedbackQueueEntity::class,
        com.CuriosityLabs.digibalance.data.local.entity.UserSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun parentalRuleDao(): ParentalRuleDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun feedbackQueueDao(): FeedbackQueueDao
    abstract fun userSessionDao(): com.CuriosityLabs.digibalance.data.local.dao.UserSessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "digibalance_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

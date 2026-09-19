package com.example.digitaldetox.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.digitaldetox.data.local.dao.*

@Database(
    entities = [
        TrackedAppEntity::class,
        RestrictionRuleEntity::class,
        ScheduleEntity::class,
        UsageSessionEntity::class,
        DailyUsageSummaryEntity::class,
        InterventionEventEntity::class,
        GardenStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun trackedAppDao(): TrackedAppDao
    abstract fun usageDao(): UsageDao
    abstract fun interventionDao(): InterventionDao
    abstract fun gardenDao(): GardenDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "digital_detox_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.digitaldetox.data.local.dao

import androidx.room.*
import com.example.digitaldetox.data.local.database.TrackedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedAppDao {
    @Query("SELECT * FROM tracked_apps ORDER BY displayName ASC")
    fun getAllTrackedApps(): Flow<List<TrackedAppEntity>>

    @Query("SELECT * FROM tracked_apps WHERE isMonitored = 1 ORDER BY displayName ASC")
    fun getMonitoredApps(): Flow<List<TrackedAppEntity>>

    @Query("SELECT * FROM tracked_apps WHERE isRestricted = 1 ORDER BY displayName ASC")
    fun getRestrictedApps(): Flow<List<TrackedAppEntity>>

    @Query("SELECT * FROM tracked_apps WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): TrackedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: TrackedAppEntity)

    @Update
    suspend fun updateApp(app: TrackedAppEntity)

    @Delete
    suspend fun deleteApp(app: TrackedAppEntity)
}

package com.example.digitaldetox.data.repository

import com.example.digitaldetox.data.local.dao.GardenDao
import com.example.digitaldetox.data.local.dao.InterventionDao
import com.example.digitaldetox.data.local.dao.ScheduleDao
import com.example.digitaldetox.data.local.dao.TrackedAppDao
import com.example.digitaldetox.data.local.dao.UsageDao
import com.example.digitaldetox.data.local.database.TrackedAppEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val trackedAppDao: TrackedAppDao,
    private val usageDao: UsageDao,
    private val interventionDao: InterventionDao,
    private val gardenDao: GardenDao,
    private val scheduleDao: ScheduleDao
) {
    fun getAllTrackedApps(): Flow<List<TrackedAppEntity>> = trackedAppDao.getAllTrackedApps()
    
    suspend fun addTrackedApp(app: TrackedAppEntity) {
        trackedAppDao.insertApp(app)
    }

    suspend fun updateTrackedApp(app: TrackedAppEntity) {
        trackedAppDao.updateApp(app)
    }

    suspend fun deleteTrackedApp(app: TrackedAppEntity) {
        trackedAppDao.deleteApp(app)
    }
}

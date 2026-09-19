package com.example.digitaldetox.data.local.dao

import androidx.room.*
import com.example.digitaldetox.data.local.database.InterventionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterventionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: InterventionEventEntity)

    @Query("SELECT * FROM intervention_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int): Flow<List<InterventionEventEntity>>
}

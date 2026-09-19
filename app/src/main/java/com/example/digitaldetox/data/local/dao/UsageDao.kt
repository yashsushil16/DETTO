package com.example.digitaldetox.data.local.dao

import androidx.room.*
import com.example.digitaldetox.data.local.database.DailyUsageSummaryEntity
import com.example.digitaldetox.data.local.database.UsageSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UsageSessionEntity)

    @Query("SELECT * FROM usage_sessions WHERE dateString = :dateString ORDER BY startTime DESC")
    fun getSessionsForDate(dateString: String): Flow<List<UsageSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(summary: DailyUsageSummaryEntity)

    @Query("SELECT * FROM daily_usage_summaries WHERE dateString = :dateString")
    fun getDailySummary(dateString: String): Flow<DailyUsageSummaryEntity?>
    
    @Query("SELECT * FROM daily_usage_summaries ORDER BY dateString DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int): Flow<List<DailyUsageSummaryEntity>>
}

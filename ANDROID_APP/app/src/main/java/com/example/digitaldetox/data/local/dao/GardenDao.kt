package com.example.digitaldetox.data.local.dao

import androidx.room.*
import com.example.digitaldetox.data.local.database.GardenStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenDao {
    @Query("SELECT * FROM garden_state WHERE id = 1")
    fun getGardenState(): Flow<GardenStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGardenState(state: GardenStateEntity)
}

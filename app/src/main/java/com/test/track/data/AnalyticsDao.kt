package com.test.track.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<AnalyticsEvent>>

    @Insert
    suspend fun insertEvent(event: AnalyticsEvent)

    @Query("DELETE FROM analytics_events")
    suspend fun deleteAllEvents()
}

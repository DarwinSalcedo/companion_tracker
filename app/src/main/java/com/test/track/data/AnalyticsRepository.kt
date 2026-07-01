package com.test.track.data

import kotlinx.coroutines.flow.Flow

class AnalyticsRepository(private val analyticsDao: AnalyticsDao) {
    val events: Flow<List<AnalyticsEvent>> = analyticsDao.getAllEvents()

    suspend fun addEvent(event: AnalyticsEvent) {
        analyticsDao.insertEvent(event)
    }

    suspend fun clearEvents() {
        analyticsDao.deleteAllEvents()
    }
}

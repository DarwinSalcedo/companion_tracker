package com.test.track

import android.app.Application
import com.test.track.data.AnalyticsRepository
import com.test.track.data.AppDatabase

class TrackerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AnalyticsRepository(database.analyticsDao()) }
}

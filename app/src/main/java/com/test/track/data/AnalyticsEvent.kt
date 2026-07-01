package com.test.track.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analytics_events")
data class AnalyticsEvent(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val eventName: String,
    val eventData: String,
    val timestamp: Long = System.currentTimeMillis()
)

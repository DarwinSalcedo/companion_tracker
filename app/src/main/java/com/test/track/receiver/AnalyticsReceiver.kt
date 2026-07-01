package com.test.track.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.test.track.MainActivity
import com.test.track.TrackerApplication
import com.test.track.data.AnalyticsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class AnalyticsReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ANALYTICS_EVENT = "com.track.ACTION_ANALYTICS_EVENT"
        private const val CHANNEL_ID = "analytics_tracker_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ANALYTICS_EVENT) {
            val eventName = intent.getStringExtra("event_name") ?: "Unknown Event"
            val eventDataBundle = intent.getBundleExtra("event_data")
            
            val paramsCount = eventDataBundle?.keySet()?.size ?: 0
            val eventDataJson = bundleToJsonString(eventDataBundle)

            val event = AnalyticsEvent(
                eventName = eventName,
                eventData = eventDataJson
            )
            
            val repository = (context.applicationContext as TrackerApplication).repository
            CoroutineScope(Dispatchers.IO).launch {
                repository.addEvent(event)
            }
            
            showNotification(context, eventName, paramsCount)
        }
    }

    private fun bundleToJsonString(bundle: Bundle?): String {
        if (bundle == null) return "{}"
        return try {
            val json = JSONObject()
            for (key in bundle.keySet()) {
                val value = bundle.get(key)
                if (value != null) {
                    json.put(key, JSONObject.wrap(value))
                }
            }
            json.toString(4) // 4 spaces for indentation
        } catch (e: Exception) {
            "Error parsing bundle: ${e.message}"
        }
    }

    private fun showNotification(context: Context, eventName: String, paramsCount: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Analytics Events",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for intercepted analytics events"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Event: $eventName")
            .setContentText("Params: $paramsCount - Toca para ver detalle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(NOTIFICATION_ID + System.currentTimeMillis().toInt(), builder.build())
    }
}

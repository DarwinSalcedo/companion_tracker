package com.test.track.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.provider.Settings
import android.content.Context
import android.content.SharedPreferences
import com.test.track.MainActivity
import com.test.track.TrackerApplication
import com.test.track.ui.FloatingBubbleManager

class AnalyticsService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var analyticsReceiver: AnalyticsReceiver
    private var floatingBubbleManager: FloatingBubbleManager? = null
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        
        analyticsReceiver = AnalyticsReceiver()
        val filter = IntentFilter(AnalyticsReceiver.ACTION_ANALYTICS_EVENT)
        ContextCompat.registerReceiver(
            this,
            analyticsReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        prefs = getSharedPreferences("CompanionPrefs", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)

        val repository = (application as TrackerApplication).repository
        floatingBubbleManager = FloatingBubbleManager(this, repository.events)
        
        checkAndShowBubble()
    }

    private fun checkAndShowBubble() {
        val enabled = prefs.getBoolean("bubble_enabled", true)
        if (enabled && Settings.canDrawOverlays(this)) {
            floatingBubbleManager?.show()
        } else {
            floatingBubbleManager?.hide()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "bubble_enabled") {
            checkAndShowBubble()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkAndShowBubble()
        if (intent?.action == "com.track.ACTION_EXPAND_BUBBLE") {
            floatingBubbleManager?.expandState?.value = true
        } else if (intent?.action == "com.track.ACTION_COLLAPSE_BUBBLE") {
            floatingBubbleManager?.expandState?.value = false
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        try {
            unregisterReceiver(analyticsReceiver)
            floatingBubbleManager?.hide()
        } catch (e: Exception) {
            // Ignored
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "analytics_service_channel",
                "Analytics Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the Companion Tracker alive to listen for events"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags)

        return NotificationCompat.Builder(this, "analytics_service_channel")
            .setContentTitle("Companion Tracker Activo")
            .setContentText("Escuchando eventos en segundo plano...")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}

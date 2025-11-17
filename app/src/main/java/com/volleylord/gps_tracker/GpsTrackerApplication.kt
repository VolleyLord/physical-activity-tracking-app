package com.volleylord.gps_tracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.volleylord.gps_tracker.data.service.notification.TrackingNotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GpsTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Create notification channel early to ensure it exists before service starts
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            val existingChannel = notificationManager.getNotificationChannel(
                TrackingNotificationHelper.CHANNEL_ID
            )
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    TrackingNotificationHelper.CHANNEL_ID,
                    TrackingNotificationHelper.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows notification while tracking activity"
                    setShowBadge(false)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}


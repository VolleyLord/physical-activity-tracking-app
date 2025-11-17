package com.volleylord.gps_tracker.data.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.volleylord.gps_tracker.data.service.TrackingForegroundService
import com.volleylord.gps_tracker.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing tracking notifications.
 * Provides dynamic notifications with pause/resume actions and real-time updates.
 */
@Singleton
class TrackingNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "tracking_service_channel"
        const val CHANNEL_NAME = "Activity Tracking Status"
        const val NOTIFICATION_ID = 1
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
        as NotificationManager
    
    private val baseNotificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("Tracking Activity")
            .setContentIntent(createContentIntent())
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    
    /**
     * Updates the tracking notification with current duration and tracking state.
     */
    fun updateTrackingNotification(durationInMillis: Long, isTracking: Boolean) {
        val formattedTime = formatDuration(durationInMillis)
        val notification = baseNotificationBuilder
            .setContentText(formattedTime)
            .clearActions()
            .addAction(getTrackingNotificationAction(isTracking))
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun getTrackingNotificationAction(isTracking: Boolean): NotificationCompat.Action {
        val icon = if (isTracking) 
            android.R.drawable.ic_media_pause 
        else 
            android.R.drawable.ic_media_play
        
        val text = if (isTracking) "Pause" else "Resume"
        val action = if (isTracking) 
            TrackingForegroundService.ACTION_PAUSE_TRACKING 
        else 
            TrackingForegroundService.ACTION_RESUME_TRACKING
        
        val pendingIntent = PendingIntent.getService(
            context,
            0,
            Intent(context, TrackingForegroundService::class.java).apply {
                this.action = action
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Action(icon, text, pendingIntent)
    }
    
    /**
     * Gets default notification for initial foreground service start.
     */
    fun getDefaultNotification(): Notification {
        createNotificationChannel()
        return baseNotificationBuilder
            .setContentText("00:00:00")
            .build()
    }
    
    /**
     * Removes the tracking notification.
     */
    fun removeTrackingNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    
    /**
     * Creates the notification channel for tracking notifications.
     * Should be called early in Application.onCreate() to ensure channel exists.
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows notification while tracking activity"
                    setShowBadge(false)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

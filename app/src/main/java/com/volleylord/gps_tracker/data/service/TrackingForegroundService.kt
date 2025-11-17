package com.volleylord.gps_tracker.data.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.volleylord.gps_tracker.data.service.notification.TrackingNotificationHelper
import com.volleylord.gps_tracker.data.util.TimeTracker
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.ObserveCurrentSessionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for continuous location tracking with reactive notifications.
 * Uses LifecycleService to manage lifecycle-aware coroutines.
 * Updates notification in real-time with duration and pause/resume actions.
 */
@AndroidEntryPoint
class TrackingForegroundService : LifecycleService() {

    companion object {
        const val ACTION_PAUSE_TRACKING = "action_pause_tracking"
        const val ACTION_RESUME_TRACKING = "action_resume_tracking"
        const val ACTION_START_SERVICE = "action_start_service"

        fun start(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_PAUSE_TRACKING
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_RESUME_TRACKING
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TrackingForegroundService::class.java))
        }
    }

    @Inject
    lateinit var activitySessionRepository: ActivitySessionRepository

    @Inject
    lateinit var timeTracker: TimeTracker

    @Inject
    lateinit var observeCurrentSessionUseCase: ObserveCurrentSessionUseCase

    @Inject
    lateinit var notificationHelper: TrackingNotificationHelper

    private var notificationJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_PAUSE_TRACKING -> {
                lifecycleScope.launch {
                    activitySessionRepository.pauseSession()
                }
            }
            ACTION_RESUME_TRACKING -> {
                lifecycleScope.launch {
                    activitySessionRepository.resumeSession()
                }
            }
            ACTION_START_SERVICE -> {
                startForeground(
                    TrackingNotificationHelper.NOTIFICATION_ID,
                    notificationHelper.getDefaultNotification()
                )

                // Subscribe to reactive updates for notification
                if (notificationJob == null) {
                    notificationJob = combine(
                        timeTracker.durationInMillis,
                        observeCurrentSessionUseCase(),
                        activitySessionRepository.isPaused
                    ) { duration, session, isPaused ->
                        val isTracking = session != null && !isPaused
                        notificationHelper.updateTrackingNotification(
                            durationInMillis = duration,
                            isTracking = isTracking
                        )
                    }.launchIn(lifecycleScope)
                }
            }
        }

        return START_STICKY // Restart if killed by system
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationHelper.removeTrackingNotification()
        notificationJob?.cancel()
        notificationJob = null
    }
}


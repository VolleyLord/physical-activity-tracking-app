package com.volleylord.gps_tracker.data.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages tracking duration with pause/resume functionality.
 * Tracks elapsed time and provides StateFlow for reactive updates.
 */
@Singleton
class TimeTracker @Inject constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private var timeElapsedInMillis = 0L
    private var isRunning = false
    private var callback: ((Long) -> Unit)? = null
    private var job: Job? = null
    private var sessionStartTime: Long? = null
    private var pausedAtTime: Long? = null
    
    private val _durationInMillis = MutableStateFlow(0L)
    val durationInMillis: StateFlow<Long> = _durationInMillis.asStateFlow()
    
    /**
     * Starts or resumes the timer from the given session start time.
     * If paused, resumes from the accumulated elapsed time.
     */
    fun startResumeTimer(initialStartTime: Long, onTick: (Long) -> Unit) {
        if (isRunning) return
        
        this.callback = onTick
        
        // If first start, initialize
        if (sessionStartTime == null) {
            sessionStartTime = initialStartTime
            timeElapsedInMillis = 0L
            _durationInMillis.value = 0L
        }
        // If resuming from pause, keep accumulated elapsed time
        // timeElapsedInMillis already has the elapsed time before pause
        
        isRunning = true
        pausedAtTime = null
        start()
    }
    
    private fun start() {
        if (job != null) return
        
        val resumeTime = System.currentTimeMillis()
        val elapsedAtResume = timeElapsedInMillis
        
        // Update immediately to avoid returning 0 on first call
        if (elapsedAtResume == 0L && sessionStartTime != null) {
            // First start - calculate initial elapsed time
            val initialElapsed = resumeTime - sessionStartTime!!
            timeElapsedInMillis = initialElapsed
            _durationInMillis.value = initialElapsed
        }
        
        job = scope.launch(Dispatchers.Default) {
            while (isRunning && isActive) {
                val currentTime = System.currentTimeMillis()
                timeElapsedInMillis = elapsedAtResume + (currentTime - resumeTime)
                _durationInMillis.value = timeElapsedInMillis
                callback?.invoke(timeElapsedInMillis)
                delay(1000)
            }
        }
    }
    
    /**
     * Pauses the timer, preserving elapsed time.
     */
    fun pauseTimer() {
        if (!isRunning) return
        isRunning = false
        pausedAtTime = System.currentTimeMillis()
        job?.cancel()
        job = null
        callback = null
    }
    
    /**
     * Stops the timer and resets all state.
     */
    fun stopTimer() {
        pauseTimer()
        timeElapsedInMillis = 0L
        _durationInMillis.value = 0L
        sessionStartTime = null
        pausedAtTime = null
        callback = null
    }
    
    /**
     * Resets the timer to zero while keeping it running.
     */
    fun reset() {
        val wasRunning = isRunning
        pauseTimer()
        timeElapsedInMillis = 0L
        _durationInMillis.value = 0L
        pausedAtTime = null
        if (wasRunning && sessionStartTime != null) {
            startResumeTimer(sessionStartTime!!, callback ?: {})
        }
    }
    
    /**
     * Gets current elapsed time in milliseconds.
     * If timer is running, returns the latest value from StateFlow (updated every second).
     * If timer is paused, returns the accumulated elapsed time.
     * This ensures we always return a valid elapsed time, even if called before first update.
     */
    fun getCurrentElapsedTime(): Long {
        // Return the StateFlow value which is updated every second when running
        // This ensures we get the most recent value even if called between updates
        return _durationInMillis.value
    }
}

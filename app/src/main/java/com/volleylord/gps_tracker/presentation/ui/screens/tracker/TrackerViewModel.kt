package com.volleylord.gps_tracker.presentation.ui.screens.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import com.volleylord.gps_tracker.domain.usecase.ObserveCurrentSessionUseCase
import com.volleylord.gps_tracker.domain.usecase.PauseActivityTrackingUseCase
import com.volleylord.gps_tracker.domain.usecase.ResumeActivityTrackingUseCase
import com.volleylord.gps_tracker.domain.usecase.StartActivityTrackingUseCase
import com.volleylord.gps_tracker.domain.usecase.StopActivityTrackingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackerUiState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val isLoading: Boolean = false,
    val distanceMeters: Double = 0.0,
    val stepCount: Long = 0L,
    val elapsedTime: String = "00:00",
    val route: List<com.volleylord.gps_tracker.domain.model.TrackingPoint> = emptyList(),
    val currentLocation: com.volleylord.gps_tracker.domain.model.TrackingPoint? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val startTrackingUseCase: StartActivityTrackingUseCase,
    private val stopTrackingUseCase: StopActivityTrackingUseCase,
    private val pauseTrackingUseCase: PauseActivityTrackingUseCase,
    private val resumeTrackingUseCase: ResumeActivityTrackingUseCase,
    private val observeCurrentSessionUseCase: ObserveCurrentSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private var currentSessionId: String? = null

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            observeCurrentSessionUseCase()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message
                    )
                }
                .collect { session ->
                    if (session != null) {
                        currentSessionId = session.id
                        val minutes = session.stats.elapsed.inWholeMinutes
                        val seconds = (session.stats.elapsed.inWholeSeconds % 60)
                        val isPaused = session.status is ActivityStatus.Paused
                        val currentLocation = session.route.lastOrNull()
                        _uiState.value = _uiState.value.copy(
                            isTracking = true,
                            isPaused = isPaused,
                            distanceMeters = session.stats.distanceMeters,
                            stepCount = session.stats.stepCount,
                            elapsedTime = String.format("%02d:%02d", minutes, seconds),
                            route = session.route,
                            currentLocation = currentLocation
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isTracking = false,
                            isPaused = false,
                            distanceMeters = 0.0,
                            stepCount = 0L,
                            elapsedTime = "00:00",
                            route = emptyList(),
                            currentLocation = null
                        )
                    }
                }
        }
    }

    fun startTracking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            startTrackingUseCase().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to start tracking"
                    )
                }
            )
        }
    }

    fun pauseTracking() {
        if (!_uiState.value.isTracking || _uiState.value.isPaused) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            pauseTrackingUseCase()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun resumeTracking() {
        if (!_uiState.value.isTracking || !_uiState.value.isPaused) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            resumeTrackingUseCase()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun stopTracking() {
        viewModelScope.launch {
            val sessionId = currentSessionId ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            stopTrackingUseCase(sessionId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    currentSessionId = null
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to stop tracking"
                    )
                }
            )
        }
    }
}


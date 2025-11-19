package com.volleylord.gps_tracker.presentation.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.usecase.ObserveActivityHistoryUseCase
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val activeSession: ActivitySession? = null,
    val recentSessions: List<ActivitySession> = emptyList(),
    val errorMessage: String? = null,
    val isActionInProgress: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeCurrentSessionUseCase: ObserveCurrentSessionUseCase,
    private val observeActivityHistoryUseCase: ObserveActivityHistoryUseCase,
    private val startActivityTrackingUseCase: StartActivityTrackingUseCase,
    private val stopActivityTrackingUseCase: StopActivityTrackingUseCase,
    private val pauseActivityTrackingUseCase: PauseActivityTrackingUseCase,
    private val resumeActivityTrackingUseCase: ResumeActivityTrackingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentSessionId: String? = null

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                observeCurrentSessionUseCase(),
                observeActivityHistoryUseCase(HISTORY_LIMIT)
            ) { activeSession, history ->
                currentSessionId = activeSession?.id
                DashboardUiState(
                    isLoading = false,
                    activeSession = activeSession,
                    recentSessions = history,
                    errorMessage = null,
                    isActionInProgress = _uiState.value.isActionInProgress
                )
            }
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
                .collect { state ->
                    _uiState.value = state.copy(isActionInProgress = false)
                }
        }
    }

    fun onStartClicked() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true, errorMessage = null)
            startActivityTrackingUseCase().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isActionInProgress = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = error.message ?: "Failed to start tracking"
                    )
                }
            )
        }
    }

    fun onStopClicked() {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true, errorMessage = null)
            stopActivityTrackingUseCase(sessionId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isActionInProgress = false)
                    currentSessionId = null
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isActionInProgress = false,
                        errorMessage = error.message ?: "Failed to stop tracking"
                    )
                }
            )
        }
    }

    fun onPauseClicked() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true, errorMessage = null)
            runCatching { pauseActivityTrackingUseCase() }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Failed to pause tracking"
                    )
                }
            _uiState.value = _uiState.value.copy(isActionInProgress = false)
        }
    }

    fun onResumeClicked() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionInProgress = true, errorMessage = null)
            runCatching { resumeActivityTrackingUseCase() }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Failed to resume tracking"
                    )
                }
            _uiState.value = _uiState.value.copy(isActionInProgress = false)
        }
    }

    companion object {
        private const val HISTORY_LIMIT = 5
    }
}


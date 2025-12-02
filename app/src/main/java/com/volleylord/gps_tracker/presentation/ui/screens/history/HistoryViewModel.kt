package com.volleylord.gps_tracker.presentation.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.usecase.DeleteActivitySessionUseCase
import com.volleylord.gps_tracker.domain.usecase.ObserveActivityHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val sessions: List<ActivitySession> = emptyList(),
    val errorMessage: String? = null,
    val isSelectionMode: Boolean = false,
    val selectedSessionIds: Set<String> = emptySet()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeActivityHistoryUseCase: ObserveActivityHistoryUseCase,
    private val deleteActivitySessionUseCase: DeleteActivitySessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            observeActivityHistoryUseCase(HISTORY_LIMIT)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
                .collect { sessions ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sessions = sessions.sortedByDescending { it.startedAtEpochMillis }
                    )
                }
        }
    }

    fun enterSelectionMode() {
        _uiState.value = _uiState.value.copy(isSelectionMode = true)
    }

    fun exitSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = false,
            selectedSessionIds = emptySet()
        )
    }

    fun toggleSessionSelection(sessionId: String) {
        val currentSelected = _uiState.value.selectedSessionIds
        val newSelected = if (currentSelected.contains(sessionId)) {
            currentSelected - sessionId
        } else {
            currentSelected + sessionId
        }
        
        _uiState.value = _uiState.value.copy(
            selectedSessionIds = newSelected,
            isSelectionMode = newSelected.isNotEmpty() || _uiState.value.isSelectionMode
        )

        if (newSelected.isEmpty() && _uiState.value.isSelectionMode) {
            exitSelectionMode()
        }
    }

    fun deleteSelectedSessions() {
        val selectedIds = _uiState.value.selectedSessionIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            selectedIds.forEach { sessionId ->
                deleteActivitySessionUseCase(sessionId).fold(
                    onSuccess = { },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message ?: "Failed to delete session"
                        )
                    }
                )
            }
            exitSelectionMode()
        }
    }

    companion object {
        private const val HISTORY_LIMIT = 25
    }
}


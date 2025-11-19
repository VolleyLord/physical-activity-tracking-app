package com.volleylord.gps_tracker.presentation.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volleylord.gps_tracker.domain.model.ActivitySession
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
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeActivityHistoryUseCase: ObserveActivityHistoryUseCase
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
                    _uiState.value = HistoryUiState(
                        isLoading = false,
                        sessions = sessions
                    )
                }
        }
    }

    companion object {
        private const val HISTORY_LIMIT = 25
    }
}


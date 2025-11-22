package com.volleylord.gps_tracker.presentation.ui.screens.history.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.usecase.ObserveSessionDetailsUseCase
import com.volleylord.gps_tracker.domain.usecase.UpdateSessionNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val session: ActivitySession? = null,
    val notesInput: String = "",
    val message: String? = null
)

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeSessionDetailsUseCase: ObserveSessionDetailsUseCase,
    private val updateSessionNotesUseCase: UpdateSessionNotesUseCase
) : ViewModel() {

    private val sessionId: String =
        savedStateHandle["sessionId"] ?: throw IllegalArgumentException("sessionId required")

    private val _uiState = MutableStateFlow(HistoryDetailUiState())
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            observeSessionDetailsUseCase(sessionId).collect { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        session = session,
                        notesInput = session?.notes.orEmpty()
                    )
                }
            }
        }
    }

    fun onNotesChanged(value: String) {
        _uiState.update { it.copy(notesInput = value, message = null) }
    }

    fun saveNotes() {
        val notes = _uiState.value.notesInput
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            runCatching { updateSessionNotesUseCase(sessionId, notes) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, message = "Saved") }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = error.message ?: "Failed to save"
                        )
                    }
                }
        }
    }
}


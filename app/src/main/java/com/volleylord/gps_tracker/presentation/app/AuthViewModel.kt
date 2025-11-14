package com.volleylord.gps_tracker.presentation.app

import androidx.lifecycle.ViewModel
import com.volleylord.gps_tracker.domain.usecase.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Simple ViewModel to provide ObserveAuthStateUseCase to Composable.
 * This is needed because we can't inject use cases directly into Composables.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    val observeAuthStateUseCase: ObserveAuthStateUseCase
) : ViewModel()


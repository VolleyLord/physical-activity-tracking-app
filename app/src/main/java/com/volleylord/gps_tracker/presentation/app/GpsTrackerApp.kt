package com.volleylord.gps_tracker.presentation.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.volleylord.gps_tracker.presentation.ui.login.LoginScreen
import com.volleylord.gps_tracker.presentation.ui.theme.GpsTrackerTheme

@Composable
fun GpsTrackerApp() {
    GpsTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val currentUser by authViewModel.observeAuthStateUseCase().collectAsState(initial = null)
            
            if (currentUser == null) {
                // Show login screen when not authenticated
                LoginScreen(
                    onLoginSuccess = {
                        // Login success is handled by auth state flow
                        // The screen will automatically switch when user becomes authenticated
                    }
                )
            } else {
                // Show tracker screen when authenticated
                TrackerScreen()
            }
        }
    }
}

@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Activity Tracker",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isTracking) {
            Text("Distance: ${String.format("%.2f", uiState.distanceMeters)}m")
            Text("Steps: ${uiState.stepCount}")
            Text("Time: ${uiState.elapsedTime}")
        } else {
            Text("Not tracking")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (uiState.isTracking) {
                    viewModel.stopTracking()
                } else {
                    viewModel.startTracking()
                }
            },
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text(if (uiState.isTracking) "Stop" else "Start")
            }
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}
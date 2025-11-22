package com.volleylord.gps_tracker.presentation.ui.screens.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.volleylord.gps_tracker.presentation.ui.components.TrackingPermissionDialog
import com.volleylord.gps_tracker.presentation.ui.permissions.TrackingPermissionState
import com.volleylord.gps_tracker.presentation.ui.permissions.rememberTrackingPermissionState
import com.volleylord.gps_tracker.presentation.ui.theme.GpsTrackerTheme

@Composable
fun TrackerRoute(
    viewModel: TrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionState = rememberTrackingPermissionState()
    var showPermissionDialog by rememberSaveable { mutableStateOf(false) }

    TrackerScreen(
        uiState = uiState,
        permissionState = permissionState,
        onShowPermissionDialog = { showPermissionDialog = true },
        onStartTracking = { viewModel.startTracking() },
        onPauseTracking = { viewModel.pauseTracking() },
        onResumeTracking = { viewModel.resumeTracking() },
        onStopTracking = { viewModel.stopTracking() }
    )

    TrackingPermissionDialog(
        show = showPermissionDialog,
        shouldDirectToSettings = permissionState.shouldDirectToSettings,
        onDismiss = { showPermissionDialog = false },
        onRequestPermission = {
            permissionState.requestPermissions()
        },
        onOpenSettings = {
            permissionState.openAppSettings()
        }
    )
}

@Composable
fun TrackerScreen(
    uiState: TrackerUiState,
    permissionState: TrackingPermissionState,
    onShowPermissionDialog: () -> Unit,
    onStartTracking: () -> Unit,
    onPauseTracking: () -> Unit,
    onResumeTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Live Tracker",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!permissionState.hasRequiredTrackingPermissions) {
                PermissionRationale(
                    onRequestPermissions = onShowPermissionDialog
                )
            } else {
                TrackerStats(uiState)
                Spacer(modifier = Modifier.height(32.dp))
                TrackerControls(
                    uiState = uiState,
                    hasRequiredPermissions = permissionState.hasRequiredTrackingPermissions,
                    onShowPermissionDialog = onShowPermissionDialog,
                    onStartTracking = onStartTracking,
                    onPauseTracking = onPauseTracking,
                    onResumeTracking = onResumeTracking,
                    onStopTracking = onStopTracking
                )
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    onRequestPermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Location permission required",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We need location and activity recognition permissions to track your training route and steps.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRequestPermissions) {
                Text("Review permissions")
            }
        }
    }
}

@Composable
private fun TrackerStats(uiState: TrackerUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (uiState.isTracking) {
                    if (uiState.isPaused) "Tracking paused" else "Tracking in progress"
                } else {
                    "Not tracking"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(title = "Distance", value = String.format("%.2f km", uiState.distanceMeters / 1000.0))
                StatItem(title = "Steps", value = uiState.stepCount.toString())
                StatItem(title = "Time", value = uiState.elapsedTime)
            }
        }
    }
}

@Composable
private fun TrackerControls(
    uiState: TrackerUiState,
    hasRequiredPermissions: Boolean,
    onShowPermissionDialog: () -> Unit,
    onStartTracking: () -> Unit,
    onPauseTracking: () -> Unit,
    onResumeTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                when {
                    !hasRequiredPermissions -> onShowPermissionDialog()
                    !uiState.isTracking -> onStartTracking()
                    uiState.isPaused -> onResumeTracking()
                    else -> onPauseTracking()
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                val label = when {
                    !uiState.isTracking -> "Start"
                    uiState.isPaused -> "Resume"
                    else -> "Pause"
                }
                Text(label)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onStopTracking,
            enabled = uiState.isTracking && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Stop")
        }
    }
}

@Composable
private fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.titleLarge)
    }
}

@Preview
@Composable
private fun TrackerScreenPreview() {
    GpsTrackerTheme {
        TrackerScreen(
            uiState = TrackerUiState(
                isTracking = true,
                isPaused = false,
                distanceMeters = 1200.0,
                stepCount = 1500,
                elapsedTime = "12:45"
            ),
            permissionState = TrackingPermissionState(
                hasLocationPermission = true,
                hasActivityRecognitionPermission = true,
                hasNotificationPermission = true,
                canRequestDirectly = true,
                requestPermissions = {},
                openAppSettings = {}
            ),
            onShowPermissionDialog = {},
            onStartTracking = {},
            onPauseTracking = {},
            onResumeTracking = {},
            onStopTracking = {}
        )
    }
}


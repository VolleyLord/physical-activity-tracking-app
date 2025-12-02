package com.volleylord.gps_tracker.presentation.ui.screens.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.volleylord.gps_tracker.presentation.ui.components.RouteMap
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Map takes full screen
            if (permissionState.hasRequiredTrackingPermissions) {
                RouteMap(
                    route = uiState.route,
                    currentLocation = uiState.currentLocation,
                    modifier = Modifier.fillMaxSize(),
                    isTrackingEnabled = true,
                    enableMapInteractions = true,  // Always enable interactions
                    showMyLocation = true  // Always show location
                )
            }

            // Overlay UI elements
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Top section: Stats card
                if (permissionState.hasRequiredTrackingPermissions) {
                    TrackerStats(
                        uiState = uiState,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PermissionRationale(
                        onRequestPermissions = onShowPermissionDialog
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom section: Controls
                if (permissionState.hasRequiredTrackingPermissions) {
                    TrackerActionButtons(
                        uiState = uiState,
                        hasRequiredPermissions = permissionState.hasRequiredTrackingPermissions,
                        onShowPermissionDialog = onShowPermissionDialog,
                        onStartTracking = onStartTracking,
                        onPauseTracking = onPauseTracking,
                        onResumeTracking = onResumeTracking,
                        onStopTracking = onStopTracking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )
                }

                // Error message
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
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
private fun TrackerStats(
    uiState: TrackerUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
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
private fun TrackerActionButtons(
    uiState: TrackerUiState,
    hasRequiredPermissions: Boolean,
    onShowPermissionDialog: () -> Unit,
    onStartTracking: () -> Unit,
    onPauseTracking: () -> Unit,
    onResumeTracking: () -> Unit,
    onStopTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        when {
            !uiState.isTracking -> {
                SingleActionButton(
                    text = "Start",
                    color = Color(0xFF2196F3),
                    onClick = {
                        if (!hasRequiredPermissions) onShowPermissionDialog() else onStartTracking()
                    },
                    enabled = !uiState.isLoading
                )
            }

            uiState.isPaused -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrackActionButton(
                        text = "Resume",
                        color = Color(0xFF2E7D32),
                        onClick = onResumeTracking,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                    )
                    TrackActionButton(
                        text = "Finish",
                        color = Color(0xFFC62828),
                        onClick = onStopTracking,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }

            else -> {
                SingleActionButton(
                    text = "Pause",
                    color = Color(0xFF2196F3),
                    onClick = onPauseTracking,
                    enabled = !uiState.isLoading
                )
            }
        }
    }
}

@Composable
private fun SingleActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean
) {
    TrackActionButton(
        text = text,
        color = color,
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(56.dp)
    )
}

@Composable
private fun TrackActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = color.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        if (!enabled) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
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


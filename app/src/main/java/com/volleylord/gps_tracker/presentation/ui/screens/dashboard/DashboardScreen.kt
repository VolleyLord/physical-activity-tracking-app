package com.volleylord.gps_tracker.presentation.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.ActivityStats
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import com.volleylord.gps_tracker.presentation.ui.components.TrackingPermissionDialog
import com.volleylord.gps_tracker.presentation.ui.permissions.TrackingPermissionState
import com.volleylord.gps_tracker.presentation.ui.permissions.rememberTrackingPermissionState
import com.volleylord.gps_tracker.presentation.ui.theme.GpsTrackerTheme
import kotlin.time.Duration.Companion.minutes

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionState = rememberTrackingPermissionState()
    var showPermissionDialog by rememberSaveable { mutableStateOf(false) }

    DashboardScreen(
        uiState = uiState,
        permissionState = permissionState,
        onStartWorkout = viewModel::onStartClicked,
        onPauseWorkout = viewModel::onPauseClicked,
        onResumeWorkout = viewModel::onResumeClicked,
        onStopWorkout = viewModel::onStopClicked,
        onShowPermissionDialog = { showPermissionDialog = true },
        onNavigateToHistory = onNavigateToHistory
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
fun DashboardScreen(
    uiState: DashboardUiState,
    permissionState: TrackingPermissionState,
    onStartWorkout: () -> Unit,
    onPauseWorkout: () -> Unit,
    onResumeWorkout: () -> Unit,
    onStopWorkout: () -> Unit,
    onShowPermissionDialog: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )

            ActiveSessionCard(
                uiState = uiState,
                permissionState = permissionState,
                onStartWorkout = onStartWorkout,
                onPauseWorkout = onPauseWorkout,
                onResumeWorkout = onResumeWorkout,
                onStopWorkout = onStopWorkout,
                onShowPermissionDialog = onShowPermissionDialog
            )

            HistoryPreviewCard(
                sessions = uiState.recentSessions,
                onNavigateToHistory = onNavigateToHistory
            )

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ActiveSessionCard(
    uiState: DashboardUiState,
    permissionState: TrackingPermissionState,
    onStartWorkout: () -> Unit,
    onPauseWorkout: () -> Unit,
    onResumeWorkout: () -> Unit,
    onStopWorkout: () -> Unit,
    onShowPermissionDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (uiState.activeSession != null) "Current training" else "Get moving",
                style = MaterialTheme.typography.titleMedium
            )
            if (uiState.activeSession != null) {
                ActiveSessionStats(uiState.activeSession)
            } else {
                Text(
                    text = "No active training session. Start a new workout to begin tracking.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (uiState.activeSession == null) {
                Button(
                    onClick = {
                        if (permissionState.hasRequiredTrackingPermissions) {
                            onStartWorkout()
                        } else {
                            onShowPermissionDialog()
                        }
                    },
                    enabled = !uiState.isActionInProgress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start")
                }
            } else {
                val isPaused = uiState.activeSession.status is ActivityStatus.Paused
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isPaused) onResumeWorkout() else onPauseWorkout()
                        },
                        enabled = !uiState.isActionInProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isPaused) "Resume" else "Pause")
                    }
                    Button(
                        onClick = onStopWorkout,
                        enabled = !uiState.isActionInProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Finish")
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveSessionStats(session: ActivitySession) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(
            text = "Distance: ${String.format("%.2f m", session.stats.distanceMeters)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Steps: ${session.stats.stepCount}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Time: ${session.stats.elapsed.inWholeMinutes} min",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun HistoryPreviewCard(
    sessions: List<ActivitySession>,
    onNavigateToHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent history",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(onClick = onNavigateToHistory) {
                    Text("See all")
                }
            }

            if (sessions.isEmpty()) {
                Text(
                    text = "No completed sessions yet. Your recent runs will appear here.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions) { session ->
                        HistoryRow(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(session: ActivitySession) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            val minutes = session.stats.elapsed.inWholeMinutes
            val seconds = session.stats.elapsed.inWholeSeconds % 60
            Text(
                text = "${minutes}m ${seconds}s",
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = String.format("%.2f m", session.stats.distanceMeters),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = "${session.stats.stepCount} steps",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    val sampleSession = ActivitySession(
        id = "1",
        userId = "preview",
        startedAtEpochMillis = System.currentTimeMillis() - 15.minutes.inWholeMilliseconds,
        endedAtEpochMillis = System.currentTimeMillis(),
        route = listOf(
            TrackingPoint(0.0, 0.0, null, 0L, 0.minutes),
        ),
        stats = ActivityStats(
            distanceMeters = 1250.0,
            stepCount = 1800,
            elapsed = 15.minutes
        ),
        status = ActivityStatus.Completed
    )

    GpsTrackerTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                isLoading = false,
                activeSession = sampleSession.copy(status = ActivityStatus.Active),
                recentSessions = listOf(sampleSession.copy(id = "2"))
            ),
            permissionState = TrackingPermissionState(
                hasLocationPermission = true,
                hasActivityRecognitionPermission = true,
                hasNotificationPermission = true,
                canRequestDirectly = true,
                requestPermissions = {},
                openAppSettings = {}
            ),
            onStartWorkout = {},
            onPauseWorkout = {},
            onResumeWorkout = {},
            onStopWorkout = {},
            onShowPermissionDialog = {},
            onNavigateToHistory = {}
        )
    }
}


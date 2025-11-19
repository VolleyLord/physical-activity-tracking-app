package com.volleylord.gps_tracker.presentation.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.ActivityStats
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import com.volleylord.gps_tracker.presentation.ui.theme.GpsTrackerTheme
import kotlin.time.Duration.Companion.minutes

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HistoryScreen(uiState = uiState)
}

@Composable
fun HistoryScreen(uiState: HistoryUiState) {
    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.sessions.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No sessions yet. Start a workout to populate history.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.sessions) { session ->
                        HistoryItem(session = session)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(session: ActivitySession) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        val minutes = session.stats.elapsed.inWholeMinutes
        val seconds = session.stats.elapsed.inWholeSeconds % 60
        Text(
            text = session.status::class.simpleName ?: "Session",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Duration: ${minutes}m ${seconds}s",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Distance: ${String.format("%.2f m", session.stats.distanceMeters)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Steps: ${session.stats.stepCount}",
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    val sample = ActivitySession(
        id = "sample",
        userId = "preview",
        startedAtEpochMillis = System.currentTimeMillis(),
        endedAtEpochMillis = System.currentTimeMillis(),
        route = listOf(
            TrackingPoint(0.0, 0.0, null, 0L, 0.minutes)
        ),
        stats = ActivityStats(
            distanceMeters = 2300.0,
            stepCount = 3200,
            elapsed = 32.minutes
        ),
        status = ActivityStatus.Completed
    )
    GpsTrackerTheme {
        HistoryScreen(
            uiState = HistoryUiState(
                isLoading = false,
                sessions = listOf(sample)
            )
        )
    }
}


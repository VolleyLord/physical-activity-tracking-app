package com.volleylord.gps_tracker.presentation.ui.screens.history.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.volleylord.gps_tracker.presentation.util.formatSessionDateTime
import kotlin.time.Duration.Companion.minutes

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Suppress("UNUSED_PARAMETER")
fun HistoryDetailRoute(
    sessionId: String,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HistoryDetailScreen(
        uiState = uiState,
        onNotesChanged = viewModel::onNotesChanged,
        onSave = viewModel::saveNotes
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryDetailScreen(
    uiState: HistoryDetailUiState,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val session = uiState.session
            if (session == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Session not found.")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Training details",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text("Started: ${formatSessionDateTime(session.startedAtEpochMillis)}")
                    val minutes = session.stats.elapsed.inWholeMinutes
                    val seconds = session.stats.elapsed.inWholeSeconds % 60
                    Text("Duration: ${minutes}m ${seconds}s")
                    Text(
                        text = "Distance: ${String.format("%.2f km", session.stats.distanceMeters / 1000.0)}"
                    )
                    Text("Steps: ${session.stats.stepCount}")

                    OutlinedTextField(
                        value = uiState.notesInput,
                        onValueChange = onNotesChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Comments") },
                        minLines = 4
                    )

                    Button(
                        onClick = onSave,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        } else {
                            Text("Save comment")
                        }
                    }

                    uiState.message?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun HistoryDetailScreenPreview() {
    val session = ActivitySession(
        id = "preview",
        userId = "user",
        startedAtEpochMillis = System.currentTimeMillis(),
        endedAtEpochMillis = System.currentTimeMillis(),
        route = listOf(TrackingPoint(0.0, 0.0, null, 0L, 0.minutes)),
        stats = ActivityStats(1500.0, 2000, 15.minutes),
        status = ActivityStatus.Completed,
        notes = "Felt great!"
    )
    GpsTrackerTheme {
        HistoryDetailScreen(
            uiState = HistoryDetailUiState(
                isLoading = false,
                session = session,
                notesInput = session.notes.orEmpty()
            ),
            onNotesChanged = {},
            onSave = {}
        )
    }
}


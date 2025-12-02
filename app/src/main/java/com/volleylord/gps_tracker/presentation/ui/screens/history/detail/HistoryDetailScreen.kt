package com.volleylord.gps_tracker.presentation.ui.screens.history.detail

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.volleylord.gps_tracker.data.util.GpxExporter
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.ActivityStats
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import com.volleylord.gps_tracker.presentation.ui.components.RouteMap
import com.volleylord.gps_tracker.presentation.ui.theme.GpsTrackerTheme
import kotlin.time.Duration.Companion.minutes

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Suppress("UNUSED_PARAMETER")
fun HistoryDetailRoute(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HistoryDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onNotesChanged = viewModel::onNotesChanged,
        onSave = viewModel::saveNotes
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryDetailScreen(
    uiState: HistoryDetailUiState,
    onBack: () -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current

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
                // Track if user is editing comments
                val existingComment = session.notes ?: ""
                val hasExistingComment = existingComment.isNotEmpty()
                var isEditing by remember { mutableStateOf(!hasExistingComment) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp)
                    ) {
                        RouteMap(
                            route = session.route,
                            currentLocation = null, // show only route, no current location
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(16.dp)),
                            isTrackingEnabled = false,
                            enableMapInteractions = true,  // Enable interactions for history view
                            showMyLocation = false  // Don't show location
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Distance - big centered
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format("%.2f", session.stats.distanceMeters / 1000.0),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 48.sp
                                )
                            )
                            Text(
                                text = "km",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Duration and Steps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val totalSeconds = session.stats.elapsed.inWholeSeconds
                            val hours = totalSeconds / 3600
                            val minutes = (totalSeconds % 3600) / 60
                            val seconds = totalSeconds % 60
                            val durationText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = durationText,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Duration",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = session.stats.stepCount.toString(),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Steps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (hasExistingComment && !isEditing) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Comments",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = existingComment,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Edit button
                                Button(
                                    onClick = {
                                        isEditing = true
                                        // Pre-fill the text field with existing comment
                                        onNotesChanged(existingComment)
                                    },
                                    modifier = Modifier
                                        .height(32.dp)
                                        .padding(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp
                                    )
                                ) {
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Comments",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = uiState.notesInput,
                                    onValueChange = onNotesChanged,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = {
                                        if (hasExistingComment) {
                                            Text("Edit your comment")
                                        } else {
                                            Text("Add a comment")
                                        }
                                    },
                                    minLines = 4,
                                    maxLines = 6
                                )

                                val hasChanges = if (hasExistingComment) {
                                    uiState.notesInput != existingComment
                                } else {
                                    uiState.notesInput.isNotEmpty()
                                }

                                if (hasChanges) {
                                    Button(
                                        onClick = {
                                            onSave()
                                            isEditing = false
                                        },
                                        enabled = !uiState.isSaving,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (uiState.isSaving) {
                                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                                        } else {
                                            Text("Save comment")
                                        }
                                    }

                                    if (hasExistingComment && uiState.notesInput.isNotEmpty()) {
                                        // Cancel edit button
                                        TextButton(
                                            onClick = {
                                                isEditing = false
                                                // Reset to original comment
                                                onNotesChanged(existingComment)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                }
                            }
                        }

                        // Export to GPX button
                        Button(
                            onClick = {
                                val exportResult = GpxExporter.exportSessionToGpx(context, session)
                                exportResult.fold(
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "GPX exported to Downloads",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            context,
                                            "Failed to export GPX: ${it.message ?: "Unknown error"}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Export to GPX")
                        }

                        uiState.message?.let { message ->
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Add some bottom padding for scrolling
                        Spacer(modifier = Modifier.height(32.dp))
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
            onBack = {},
            onNotesChanged = {},
            onSave = {}
        )
    }
}


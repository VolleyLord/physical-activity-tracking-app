package com.volleylord.gps_tracker.presentation.ui.screens.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.ActivityStats
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import com.volleylord.gps_tracker.presentation.ui.theme.GpsTrackerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import com.volleylord.gps_tracker.BuildConfig


fun buildStaticMapUrl(
    routePoints: List<LatLng>,
    apiKey: String,
    mapId: String
): String {
    val encoded = PolyUtil.encode(routePoints)
    return "https://maps.googleapis.com/maps/api/staticmap?" +
            "size=400x400&" + // square preview
            "map_id=$mapId&" + // your custom style
            "path=color:0x2196F3|weight:5|enc:$encoded&" +
            "key=$apiKey"
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel(),
    onSessionSelected: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    HistoryScreen(
        uiState = uiState,
        viewModel = viewModel,
        onSessionSelected = onSessionSelected
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    viewModel: HistoryViewModel,
    onSessionSelected: (String) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with Delete and Cancel buttons when in selection mode
            if (uiState.isSelectionMode && uiState.selectedSessionIds.isNotEmpty()) {
                HistoryTopBar(
                    onDeleteClick = { viewModel.deleteSelectedSessions() },
                    onCancelClick = { viewModel.exitSelectionMode() },
                    selectedCount = uiState.selectedSessionIds.size
                )
            }

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
                    HistoryList(
                        sessions = uiState.sessions,
                        isSelectionMode = uiState.isSelectionMode,
                        selectedSessionIds = uiState.selectedSessionIds,
                        onSessionSelected = onSessionSelected,
                        onSessionLongClick = { viewModel.enterSelectionMode() },
                        onSessionToggleSelection = { sessionId ->
                            viewModel.toggleSessionSelection(sessionId)
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HistoryList(
    sessions: List<ActivitySession>,
    isSelectionMode: Boolean,
    selectedSessionIds: Set<String>,
    onSessionSelected: (String) -> Unit,
    onSessionLongClick: () -> Unit,
    onSessionToggleSelection: (String) -> Unit
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val groupedSessions = remember(sessions) {
        sessions
            .sortedByDescending { it.startedAtEpochMillis }
            .groupBy { Instant.ofEpochMilli(it.startedAtEpochMillis).atZone(zoneId).toLocalDate() }
            .toSortedMap(compareByDescending { it })
    }

    val dates = remember(groupedSessions) {
        groupedSessions.keys.toList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        dates.forEachIndexed { dateIndex, date ->
            val daySessions = groupedSessions[date] ?: emptyList()

            item(key = "header_$date") {
                DateHeader(date)
                Spacer(modifier = Modifier.height(8.dp))
            }

            itemsIndexed(
                items = daySessions,
                key = { index, session -> "${session.id}_$index" }
            ) { itemIndex, session ->
                HistoryItem(
                    session = session,
                    isSelected = selectedSessionIds.contains(session.id),
                    isSelectionMode = isSelectionMode,
                    onClick = {
                        if (isSelectionMode) {
                            onSessionToggleSelection(session.id)
                        } else {
                            onSessionSelected(session.id)
                        }
                    },
                    onLongClick = {
                        if (!isSelectionMode) {
                            onSessionLongClick()
                            onSessionToggleSelection(session.id)
                        }
                    },
                    isFirstInGroup = itemIndex == 0,                    // First in this day
                    isLastInGroup = itemIndex == daySessions.lastIndex, // Last in this day
                    isOnlyInGroup = daySessions.size == 1              // Single item in this day
                )
                if (itemIndex != daySessions.lastIndex) {
                    Spacer(modifier = Modifier.height(0.dp)) // Remove spacing between items
                }
            }

            if (dateIndex != dates.lastIndex) {
                item(key = "spacer_$dateIndex") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateHeader(date: LocalDate) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
    ) {
        Text(
            text = date.format(formatter),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HistoryItem(
    session: ActivitySession,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    isOnlyInGroup: Boolean
) {
    // Calculate corner shape based on position
    val cornerShape = when {
        isOnlyInGroup -> RoundedCornerShape(20.dp)
        isFirstInGroup -> RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
        isLastInGroup -> RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        )
        else -> RoundedCornerShape(0.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cornerShape)
            .background(
                if (isSelected && isSelectionMode) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .then(
                if (isSelectionMode) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
            )
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection checkbox
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
                    .then(
                        if (!isSelected) {
                            Modifier.border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        RouteThumbnail(
            route = session.route,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Walk", // no activity type detection implemented yet
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatSessionTime(session.startedAtEpochMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = String.format("%.2f km", session.stats.distanceMeters / 1000.0),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            val totalSeconds = session.stats.elapsed.inWholeSeconds
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            val durationText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            Text(
                text = durationText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${session.stats.stepCount} Steps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun formatSessionTime(epochMillis: Long): String {
    val formatter = remember {
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    }
    val instant = Instant.ofEpochMilli(epochMillis)
    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    return zonedDateTime.format(formatter)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTopBar(
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit,
    selectedCount: Int
) {
    TopAppBar(
        title = {
            Text(
                text = if (selectedCount == 1) {
                    "$selectedCount item selected"
                } else {
                    "$selectedCount items selected"
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onCancelClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel"
                )
            }
        },
        actions = {
            TextButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        windowInsets = WindowInsets(0.dp),
    )
}

@Composable
fun RouteThumbnail(
    route: List<TrackingPoint>,
    modifier: Modifier = Modifier
) {
    val routePoints = route.map { LatLng(it.latitude, it.longitude) }
    val url = buildStaticMapUrl(
        routePoints = routePoints,
        apiKey = BuildConfig.MAPS_API_KEY,
        mapId = BuildConfig.STATIC_MAP_ID
    )

    AsyncImage(
        model = url,
        contentDescription = "Route preview",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}




@RequiresApi(Build.VERSION_CODES.O)
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
        // Preview doesn't need viewModel - just show the UI state
        Surface(modifier = Modifier.fillMaxSize()) {
            HistoryList(
                sessions = listOf(sample),
                isSelectionMode = false,
                selectedSessionIds = emptySet(),
                onSessionSelected = {},
                onSessionLongClick = {},
                onSessionToggleSelection = {}
            )
        }
    }
}


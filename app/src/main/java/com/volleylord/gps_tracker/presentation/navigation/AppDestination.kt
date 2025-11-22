package com.volleylord.gps_tracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector?
) {
    data object Login : AppDestination("login", "Login", null)
    data object Dashboard : AppDestination("dashboard", "Main", Icons.Outlined.Home)
    data object Tracker : AppDestination("tracker", "Tracker", Icons.Outlined.Map)
    data object History : AppDestination("history", "History", Icons.Outlined.History)
    data object HistoryDetail : AppDestination("historyDetail/{sessionId}", "History Detail", null)

    companion object {
        val bottomBarDestinations = listOf(Dashboard, Tracker, History)
    }
}


package com.volleylord.gps_tracker.presentation.app

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.volleylord.gps_tracker.presentation.navigation.AppDestination
import com.volleylord.gps_tracker.presentation.navigation.NavigationCommand
import com.volleylord.gps_tracker.presentation.navigation.NavigationManager
import com.volleylord.gps_tracker.presentation.ui.screens.dashboard.DashboardRoute
import com.volleylord.gps_tracker.presentation.ui.screens.history.HistoryRoute
import com.volleylord.gps_tracker.presentation.ui.screens.history.detail.HistoryDetailRoute
import com.volleylord.gps_tracker.presentation.ui.screens.login.LoginScreen
import com.volleylord.gps_tracker.presentation.ui.screens.tracker.TrackerRoute
import com.volleylord.gps_tracker.presentation.ui.theme.GpsTrackerTheme
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GpsTrackerApp(
    navigationManager: NavigationManager
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.observeAuthStateUseCase().collectAsState(initial = null)
    val isAuthenticated = currentUser != null

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val context = LocalContext.current

    var hasNotificationPermission by rememberSaveable {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var showNotificationPermissionDialog by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    LaunchedEffect(navigationManager, navController) {
        navigationManager.commands.collectLatest { command ->
            when (command) {
                is NavigationCommand.NavigateTo -> {
                    navController.navigate(command.route, command.builder)
                }
                NavigationCommand.NavigateUp -> navController.navigateUp()
            }
        }
    }

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            navController.navigate(AppDestination.Login.route) {
                popUpTo(AppDestination.Login.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else {
            navController.navigate(AppDestination.Dashboard.route) {
                popUpTo(AppDestination.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(isAuthenticated, currentRoute) {
        if (isAuthenticated && (currentRoute == null || currentRoute == AppDestination.Login.route)) {
            navController.navigate(AppDestination.Dashboard.route) {
                popUpTo(AppDestination.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(isAuthenticated) {
        if (
            isAuthenticated &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission
        ) {
            showNotificationPermissionDialog = true
        }
    }

    GpsTrackerTheme {
        Surface {
            Scaffold(
                bottomBar = {
                    if (isAuthenticated) {
                        TrackerBottomBar(
                            currentRoute = currentRoute,
                            onDestinationSelected = { destination ->
                                if (destination.route != currentRoute) {
                                    navigationManager.navigateTo(destination.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(AppDestination.Login.route) {
                                            saveState = true
                                            inclusive = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = AppDestination.Login.route,
                    modifier = Modifier.padding(padding)
                ) {
                    composable(AppDestination.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                navigationManager.navigateTo(AppDestination.Dashboard.route) {
                                    popUpTo(AppDestination.Login.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(AppDestination.Dashboard.route) {
                        DashboardRoute(
                            onNavigateToHistory = {
                                navigationManager.navigateTo(AppDestination.History.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(AppDestination.Tracker.route) {
                        TrackerRoute()
                    }
                    composable(AppDestination.History.route) {
                        HistoryRoute(
                            onSessionSelected = { sessionId ->
                                navigationManager.navigateTo(
                                    AppDestination.HistoryDetail.route.replace(
                                        "{sessionId}",
                                        sessionId
                                    )
                                )
                            }
                        )
                    }
                    composable(
                        route = AppDestination.HistoryDetail.route,
                        arguments = listOf(
                            navArgument("sessionId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
                        HistoryDetailRoute(
                            sessionId = sessionId,
                            onBack = { navController.navigateUp() }
                        )
                    }
                }
            }
        }

        if (showNotificationPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationPermissionDialog = false },
                title = { Text("Enable notifications") },
                text = {
                    Text("Allow notifications so we can keep you informed when tracking runs in the background.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showNotificationPermissionDialog = false
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    ) {
                        Text("Allow")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationPermissionDialog = false }) {
                        Text("Not now")
                    }
                }
            )
        }
    }
}

@Composable
private fun TrackerBottomBar(
    currentRoute: String?,
    onDestinationSelected: (AppDestination) -> Unit
) {
    NavigationBar {
        AppDestination.bottomBarDestinations.forEach { destination ->
            NavigationBarItem(
                selected = destination.route == currentRoute,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = requireNotNull(destination.icon),
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}
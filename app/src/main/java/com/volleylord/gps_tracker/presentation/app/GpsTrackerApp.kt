package com.volleylord.gps_tracker.presentation.app

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current
    
    // Permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    
    var hasActivityRecognitionPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasActivityRecognitionPermission = 
                permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = 
                permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        }
    }
    
    // Request permissions when needed
    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        
        if (!hasLocationPermission) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasActivityRecognitionPermission) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

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

        if (!hasLocationPermission) {
            Text(
                "Location permission required",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Please grant location permission in settings",
                style = MaterialTheme.typography.bodySmall
            )
        } else if (uiState.isTracking) {
            Text("Distance: ${String.format("%.2f", uiState.distanceMeters)}m")
            Text("Steps: ${uiState.stepCount}")
            Text("Time: ${uiState.elapsedTime}")
        } else {
            Text("Not tracking")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isTracking) {
            // Show pause/resume and finish buttons when tracking
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (uiState.isPaused) {
                            viewModel.resumeTracking()
                        } else {
                            viewModel.pauseTracking()
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text(if (uiState.isPaused) "Resume" else "Pause")
                    }
                }
                Button(
                    onClick = {
                        viewModel.stopTracking()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Finish")
                    }
                }
            }
        } else {
            // Show start button when not tracking
            Button(
                onClick = {
                    if (hasLocationPermission) {
                        viewModel.startTracking()
                    } else {
                        // Request permissions again if denied
                        val permissionsToRequest = mutableListOf<String>()
                        if (!hasLocationPermission) {
                            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasActivityRecognitionPermission) {
                            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        if (permissionsToRequest.isNotEmpty()) {
                            permissionLauncher.launch(permissionsToRequest.toTypedArray())
                        }
                    }
                },
                enabled = !uiState.isLoading && hasLocationPermission
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Start")
                }
            }
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}
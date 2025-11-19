package com.volleylord.gps_tracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.volleylord.gps_tracker.presentation.app.GpsTrackerApp
import com.volleylord.gps_tracker.presentation.navigation.NavigationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationManager: NavigationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GpsTrackerApp(navigationManager = navigationManager)
        }
    }
}


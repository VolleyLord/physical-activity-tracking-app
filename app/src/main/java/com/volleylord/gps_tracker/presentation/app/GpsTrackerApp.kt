package com.volleylord.gps_tracker.presentation.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.volleylord.gps_tracker.presentation.ui.theme.GpsTrackerTheme

@Composable
fun GpsTrackerApp() {
    GpsTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder content until navigation shell is introduced
                Text(text = "GpsTrackerApp is running")
            }
        }
    }
}

@Preview
@Composable
private fun GpsTrackerAppPreview() {
    GpsTrackerApp()
}


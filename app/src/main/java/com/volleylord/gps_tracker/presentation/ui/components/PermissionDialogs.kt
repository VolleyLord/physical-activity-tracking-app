package com.volleylord.gps_tracker.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun TrackingPermissionDialog(
    show: Boolean,
    shouldDirectToSettings: Boolean,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions required") },
        text = {
            Text(
                if (shouldDirectToSettings) {
                    "It looks like you previously denied location or activity permissions. Please open settings and enable them to start tracking."
                } else {
                    "We need access to your location and physical activity recognition to measure distance, steps, and show your position on the map."
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (shouldDirectToSettings) {
                        onOpenSettings()
                    } else {
                        onRequestPermission()
                    }
                    onDismiss()
                }
            ) {
                Text(if (shouldDirectToSettings) "Open settings" else "Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now")
            }
        }
    )
}


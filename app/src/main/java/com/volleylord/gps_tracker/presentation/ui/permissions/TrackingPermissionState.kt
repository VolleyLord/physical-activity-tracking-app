package com.volleylord.gps_tracker.presentation.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

data class TrackingPermissionState(
    val hasLocationPermission: Boolean,
    val hasActivityRecognitionPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val canRequestDirectly: Boolean,
    val requestPermissions: () -> Unit,
    val openAppSettings: () -> Unit
) {
    val hasRequiredTrackingPermissions: Boolean
        get() = hasLocationPermission && hasActivityRecognitionPermission

    val shouldDirectToSettings: Boolean
        get() = !hasRequiredTrackingPermissions && !canRequestDirectly
}

@Composable
fun rememberTrackingPermissionState(): TrackingPermissionState {
    val context = LocalContext.current
    val activity = context as? Activity

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var hasActivityPermission by remember {
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
    val hasNotificationPermission = remember {
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
    }.value
    var hasRequestedPermissions by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: hasLocationPermission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasActivityPermission =
                permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: hasActivityPermission
        }
    }

    val shouldShowLocationRationale = activity?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(
            it,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } ?: false

    val shouldShowActivityRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } ?: false
    } else {
        false
    }

    val canRequestDirectly =
        !hasRequestedPermissions || shouldShowLocationRationale || shouldShowActivityRationale

    val requestPermissions = {
        hasRequestedPermissions = true
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        launcher.launch(permissions.toTypedArray())
    }

    val openSettings = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    return TrackingPermissionState(
        hasLocationPermission = hasLocationPermission,
        hasActivityRecognitionPermission = hasActivityPermission,
        hasNotificationPermission = hasNotificationPermission,
        canRequestDirectly = canRequestDirectly,
        requestPermissions = requestPermissions,
        openAppSettings = openSettings
    )
}


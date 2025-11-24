package com.volleylord.gps_tracker.presentation.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import kotlinx.coroutines.tasks.await

@Composable
fun RouteMap(
    route: List<TrackingPoint>,
    currentLocation: TrackingPoint?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isMapLoaded by remember { mutableStateOf(false) }
    var cameraInitialized by remember { mutableStateOf(false) }

    val routePoints = remember(route) { route.map { LatLng(it.latitude, it.longitude) } }
    val currentLocationLatLng = remember(currentLocation) { currentLocation?.let { LatLng(it.latitude, it.longitude) } }

    val cameraPositionState = rememberCameraPositionState {
        position = DEFAULT_CAMERA_POSITION
    }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun animateCamera(target: LatLng, zoom: Float) {
        runCatching {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(target, zoom)
                )
            )
        }.onFailure {
            cameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(target, zoom)
                )
            )
        }
    }

    LaunchedEffect(isMapLoaded, currentLocationLatLng) {
        if (isMapLoaded && currentLocationLatLng != null) {
            animateCamera(currentLocationLatLng, 18f)
            cameraInitialized = true
        }
    }

    LaunchedEffect(isMapLoaded, routePoints) {
        if (isMapLoaded && currentLocationLatLng == null && routePoints.size > 1) {
            val bounds = LatLngBounds.builder()
            routePoints.forEach(bounds::include)
            runCatching {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(), 100)
                )
            }
            cameraInitialized = true
        }
    }

    LaunchedEffect(isMapLoaded, cameraInitialized, hasLocationPermission) {
        if (!isMapLoaded || cameraInitialized) return@LaunchedEffect

        val fallbackLocation = if (hasLocationPermission) {
            runCatching {
                fusedLocationClient.lastLocation.await()
                    ?: fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        null
                    ).await()
            }.getOrNull()
        } else {
            null
        }

        val target = fallbackLocation?.let { LatLng(it.latitude, it.longitude) }
            ?: DEFAULT_CAMERA_POSITION.target
        val zoom = fallbackLocation?.let { 16f } ?: DEFAULT_CAMERA_POSITION.zoom

        animateCamera(target, zoom)
        cameraInitialized = true
    }

    val mapUiSettings = remember(hasLocationPermission) {
        MapUiSettings(
            mapToolbarEnabled = false,
            compassEnabled = true,
            zoomControlsEnabled = true,
            myLocationButtonEnabled = hasLocationPermission,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true
        )
    }

    val mapProperties = remember(hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        uiSettings = mapUiSettings,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        onMapLoaded = { isMapLoaded = true },
        googleMapOptionsFactory = {
            GoogleMapOptions().mapId(MAP_ID)
        }
    ) {
        if (routePoints.size > 1) {
            Polyline(
                points = routePoints,
                color = Color(0xFF2196F3),
                width = 8f
            )
        }

        currentLocationLatLng?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Current Location"
            )
        }
    }
}

private val DEFAULT_CAMERA_POSITION = CameraPosition.fromLatLngZoom(
    LatLng(55.7558, 37.6173),
    11f
)

private const val MAP_ID = "9ddcdaed646814b4220318d0"
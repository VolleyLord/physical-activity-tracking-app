package com.volleylord.gps_tracker.presentation.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val sessionDateFormatter: DateTimeFormatter by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter.ofPattern("MMM d • HH:mm", Locale.getDefault())
    } else {
        TODO("VERSION.SDK_INT < O")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatSessionDateTime(epochMillis: Long): String {
    return sessionDateFormatter.format(
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
    )
}


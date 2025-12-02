package com.volleylord.gps_tracker.data.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.volleylord.gps_tracker.domain.model.ActivitySession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for exporting activity sessions to GPX files.
 * GPX files are saved into the public Downloads collection via MediaStore.
 */
object GpxExporter {

    /**
     * Exports the given [session] as a GPX file into the Downloads folder.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportSessionToGpx(context: Context, session: ActivitySession): Result<Unit> = runCatching {
        val time = Date(session.startedAtEpochMillis)
        val nameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "track_${nameFormat.format(time)}.gpx"

        val xml = buildGpx(session)

        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/gpx+xml")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Failed to create GPX file")

        resolver.openOutputStream(uri).use { out ->
            requireNotNull(out) { "OutputStream is null" }
            out.write(xml.toByteArray())
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }

    /**
     * Builds a simple GPX 1.1 document from the session's route points.
     */
    private fun buildGpx(session: ActivitySession): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val startTimeIso = dateFormat.format(Date(session.startedAtEpochMillis))

        val trkpts = session.route.joinToString("\n") { point ->
            val timeIso = dateFormat.format(Date(point.timestampEpochMillis))
            """
            <trkpt lat="${point.latitude}" lon="${point.longitude}">
                <time>$timeIso</time>
            </trkpt>
            """.trimIndent()
        }

        return """
        <?xml version="1.0" encoding="UTF-8"?>
        <gpx version="1.1" creator="GpsTrackerApp"
             xmlns="http://www.topografix.com/GPX/1/1">
          <metadata>
            <time>$startTimeIso</time>
          </metadata>
          <trk>
            <name>Activity ${session.id}</name>
            <trkseg>
            $trkpts
            </trkseg>
          </trk>
        </gpx>
        """.trimIndent()
    }
}



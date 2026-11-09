package com.tsutsen.platformplayer.feature.player.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay

/** Pure formatting so it can be unit tested without Android or ExoPlayer. */
internal object StatsFormat {
    fun bitrate(bps: Int): String =
        when {
            bps <= 0 -> "?"
            bps >= 1_000_000 -> String.format(java.util.Locale.US, "%.1f Mbps", bps / 1_000_000.0)
            else -> "${bps / 1000} kbps"
        }

    fun resolution(width: Int, height: Int, fps: Float): String {
        if (width <= 0 || height <= 0) return "?"
        val rate = if (fps > 0f) "@${Math.round(fps)}" else ""
        return "${width}x$height$rate"
    }

    fun seconds(ms: Long): String = String.format(java.util.Locale.US, "%.1f s", ms.coerceAtLeast(0L) / 1000.0)

    /** Share of frames dropped, as "12 (0.4%)"; "0" when nothing was dropped or rendered. */
    fun dropped(dropped: Int, rendered: Int): String {
        if (dropped <= 0) return "0"
        val total = dropped + rendered.coerceAtLeast(0)
        val pct = dropped * 100.0 / total
        return String.format(java.util.Locale.US, "%d (%.1f%%)", dropped, pct)
    }

    fun codec(mime: String?, codecs: String?): String {
        val m = mime?.substringAfter('/')?.uppercase(java.util.Locale.US)
        return listOfNotNull(m, codecs?.takeIf { it.isNotBlank() }).joinToString(" ").ifBlank { "?" }
    }
}

/** "Stats for nerds": what the player is really doing right now. */
@Composable
internal fun StatsOverlay(player: ExoPlayer?, selectedQuality: String, modifier: Modifier = Modifier) {
    var lowestBuffer by remember { mutableStateOf(Long.MAX_VALUE) }
    var lines by remember { mutableStateOf(listOf("Waiting for the player...")) }
    LaunchedEffect(player) {
        while (true) {
            val p = player
            lines =
                if (p == null) {
                    listOf("No player")
                } else {
                    val v = p.videoFormat
                    val a = p.audioFormat
                    val ahead = p.bufferedPosition - p.currentPosition
                    if (p.isPlaying && ahead >= 0) lowestBuffer = minOf(lowestBuffer, ahead)
                    val counters = p.videoDecoderCounters?.also { it.ensureUpdated() }
                    listOf(
                        "Quality    ${selectedQuality}",
                        "Video      ${StatsFormat.resolution(v?.width ?: 0, v?.height ?: 0, v?.frameRate ?: 0f)}",
                        "V codec    ${StatsFormat.codec(v?.sampleMimeType, v?.codecs)}",
                        "V bitrate  ${StatsFormat.bitrate(v?.bitrate ?: 0)}",
                        "A codec    ${StatsFormat.codec(a?.sampleMimeType, a?.codecs)}",
                        "A bitrate  ${StatsFormat.bitrate(a?.bitrate ?: 0)}",
                        "Buffer     ${StatsFormat.seconds(p.bufferedPosition - p.currentPosition)}",
                        "Lowest buf ${if (lowestBuffer == Long.MAX_VALUE) "?" else StatsFormat.seconds(lowestBuffer)}",
                        "Dropped    ${StatsFormat.dropped(counters?.droppedBufferCount ?: 0, counters?.renderedOutputBufferCount ?: 0)}",
                        "Speed      ${p.playbackParameters.speed}x",
                    )
                }
            delay(500)
        }
    }
    Box(modifier = modifier.fillMaxSize().padding(8.dp)) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp),
        ) {
            lines.forEach {
                Text(it, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

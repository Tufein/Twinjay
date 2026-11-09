package com.tsutsen.platformplayer.compose

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tsutsen.platformplayer.core.data.log.AppLogShare
import com.tsutsen.platformplayer.core.data.log.PlaybackLogShare
import com.tsutsen.platformplayer.core.model.PlayerControllerActions
import com.tsutsen.platformplayer.states.StatePlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * One page with what is needed when something is wrong (version, device,
 * which sources are on, share logs) plus the controller cheat sheet.
 */
@Composable
fun DiagnosticsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val version =
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Throwable) {
            "?"
        }
    val enabled = remember0 { StatePlatform.instance.getEnabledClients() }
    val available = remember0 { StatePlatform.instance.getAvailableClients() }

    val usage =
        remember0 {
            com.tsutsen.platformplayer.states.StateDownloads.instance.getDownloadedVideos()
                .groupBy { it.author.name }
                .map { (name, vids) ->
                    name to vids.sumOf { v -> v.videoSource.sumOf { it.fileSize } + v.audioSource.sumOf { it.fileSize } }
                }.sortedByDescending { it.second }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Text("Diagnostics", style = MaterialTheme.typography.headlineSmall) }
        item {
            Text(
                "TwinJay $version\n${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (sdk ${Build.VERSION.SDK_INT})",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    scope.launch {
                        val uri = withContext(Dispatchers.IO) { AppLogShare.save(context) }
                        if (uri == null) {
                            Toast.makeText(context, "Could not save the log", Toast.LENGTH_LONG).show()
                        } else {
                            context.startActivity(AppLogShare.shareIntent(uri))
                        }
                    }
                }) { Text("Share app log") }
                Button(onClick = {
                    val intent = PlaybackLogShare.shareIntent(context)
                    if (intent == null) {
                        Toast.makeText(context, "No playback log yet", Toast.LENGTH_LONG).show()
                    } else {
                        context.startActivity(intent)
                    }
                }) { Text("Share playback log") }
            }
        }
        item {
            Button(onClick = {
                scope.launch {
                    val uri = withContext(Dispatchers.IO) { AppLogShare.save(context) }
                    if (uri == null) {
                        Toast.makeText(context, "Could not save the log", Toast.LENGTH_LONG).show()
                    } else {
                        val summary =
                            "TwinJay $version\n${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}\n" +
                                "Battery: ${com.tsutsen.platformplayer.di.BatteryLog.read(context).lastOrNull()?.percent ?: "?"}%\n" +
                                "Sources on: ${enabled.joinToString { it.name }}\n\nWhat went wrong:\n"
                        context.startActivity(
                            AppLogShare.shareIntent(uri).apply {
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "TwinJay bug report")
                                putExtra(android.content.Intent.EXTRA_TEXT, summary)
                            },
                        )
                    }
                }
            }) { Text("Send bug report") }
        }
        item { BiggestDownloads() }
        item { CleanupSuggestion() }
        item { HorizontalDivider() }
        item { Text("Sources (${enabled.size} on of ${available.size})", style = MaterialTheme.typography.titleMedium) }
        items0(available.sortedBy { it.name.lowercase() }) { client ->
            val on = enabled.any { it.id == client.id }
            Text(
                "${if (on) "On " else "Off"}  ${client.name}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { HorizontalDivider() }
        item { Text("Battery (last 24 h)", style = MaterialTheme.typography.titleMedium) }
        item { BatteryGraph(com.tsutsen.platformplayer.di.BatteryLog.read(context)) }
        item { HorizontalDivider() }
        item {
            val total = usage.sumOf { it.second }
            Text("Downloads: ${formatBytes(total)}", style = MaterialTheme.typography.titleMedium)
        }
        items0(usage) { (name, bytes) ->
            Row(Modifier.fillMaxWidth()) {
                Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text(formatBytes(bytes), style = MaterialTheme.typography.bodySmall)
            }
        }
        item { HorizontalDivider() }
        item { Text("Controller buttons (defaults)", style = MaterialTheme.typography.titleMedium) }
        items0(PlayerControllerActions.ALL) { action ->
            Row(Modifier.fillMaxWidth()) {
                Text(action.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text(
                    PlayerControllerActions.labelFor(action.defaultKeyCode),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        item {
            Text(
                "You can change these in Settings > Controller.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun <T> remember0(block: () -> T): T = androidx.compose.runtime.remember { block() }

private fun <T> androidx.compose.foundation.lazy.LazyListScope.items0(
    list: List<T>,
    content: @Composable (T) -> Unit,
) {
    items(list.size) { content(list[it]) }
}

internal fun formatBytes(bytes: Long): String =
    when {
        bytes >= 1L shl 30 -> String.format(java.util.Locale.US, "%.1f GB", bytes / (1L shl 30).toDouble())
        bytes >= 1L shl 20 -> String.format(java.util.Locale.US, "%.0f MB", bytes / (1L shl 20).toDouble())
        else -> "${bytes / 1024} KB"
    }

@Composable
private fun BatteryGraph(samples: List<com.tsutsen.platformplayer.di.BatteryLog.Sample>) {
    val cutoff = System.currentTimeMillis() - 24 * 3_600_000L
    val pts = samples.filter { it.time >= cutoff }
    if (pts.size < 2) {
        Text("Not enough data yet. The app records the battery every 5 minutes while it is open.", style = MaterialTheme.typography.bodySmall)
        return
    }
    val line = MaterialTheme.colorScheme.primary
    val charge = MaterialTheme.colorScheme.tertiary
    val grid = MaterialTheme.colorScheme.outlineVariant
    androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(120.dp)) {
        val t0 = pts.first().time
        val span = (pts.last().time - t0).coerceAtLeast(1L).toFloat()
        listOf(0f, 0.5f, 1f).forEach { f ->
            drawLine(grid, androidx.compose.ui.geometry.Offset(0f, size.height * f), androidx.compose.ui.geometry.Offset(size.width, size.height * f))
        }
        for (i in 1 until pts.size) {
            val a = pts[i - 1]
            val b = pts[i]
            drawLine(
                if (b.charging) charge else line,
                androidx.compose.ui.geometry.Offset((a.time - t0) / span * size.width, size.height * (1f - a.percent / 100f)),
                androidx.compose.ui.geometry.Offset((b.time - t0) / span * size.width, size.height * (1f - b.percent / 100f)),
                strokeWidth = 4f,
            )
        }
    }
    Text("Now ${pts.last().percent}% · lowest ${pts.minOf { it.percent }}%", style = MaterialTheme.typography.bodySmall)
}


/** The biggest downloaded videos with a delete button, plus what the oldest ones would free. */
@Composable
private fun BiggestDownloads() {
    val tick = androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val downloads = com.tsutsen.platformplayer.states.StateDownloads.instance
    val videos =
        androidx.compose.runtime.remember(tick.intValue) {
            downloads.getDownloadedVideos()
                .map { v -> v to (v.videoSource.sumOf { it.fileSize } + v.audioSource.sumOf { it.fileSize }) }
                .sortedByDescending { it.second }
        }
    if (videos.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Biggest downloads", style = MaterialTheme.typography.titleMedium)
        val top = videos.take(8)
        Text(
            "The ${top.size} biggest take ${formatBytes(top.sumOf { it.second })} of ${formatBytes(videos.sumOf { it.second })}.",
            style = MaterialTheme.typography.bodySmall,
        )
        top.forEach { (v, bytes) ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    v.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(formatBytes(bytes), style = MaterialTheme.typography.bodySmall)
                androidx.compose.material3.TextButton(onClick = {
                    runCatching { downloads.deleteCachedVideo(v.id) }
                    tick.intValue++
                }) { Text("Delete") }
            }
        }
    }
}


/** One confirm step to remove downloads older than 30 days. */
@Composable
private fun CleanupSuggestion() {
    val tick = androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val downloads = com.tsutsen.platformplayer.states.StateDownloads.instance
    val stale =
        androidx.compose.runtime.remember(tick.intValue) {
            val all = downloads.getDownloadedVideos()
            val entries =
                all.map { v ->
                    com.tsutsen.platformplayer.core.data.downloads.StorageLimit.Entry(
                        v.id.value ?: v.url,
                        v.downloadDate?.toEpochSecond() ?: 0L,
                        v.videoSource.sumOf { it.fileSize } + v.audioSource.sumOf { it.fileSize },
                    )
                }
            val old =
                com.tsutsen.platformplayer.core.data.downloads.StorageLimit
                    .staleEntries(entries, System.currentTimeMillis() / 1000, 30)
                    .map { it.key }
                    .toSet()
            all.filter { (it.id.value ?: it.url) in old }
        }
    if (stale.isEmpty()) return
    val bytes = stale.sumOf { v -> v.videoSource.sumOf { it.fileSize } + v.audioSource.sumOf { it.fileSize } }
    var confirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Cleanup suggestion", style = MaterialTheme.typography.titleMedium)
        Text(
            "${stale.size} downloads are older than 30 days and take ${formatBytes(bytes)}.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = { confirm = true }) { Text("Review and delete") }
    }
    if (confirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Delete ${stale.size} old downloads?") },
            text = { Text("This frees ${formatBytes(bytes)}. The videos stay available online.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    stale.forEach { runCatching { downloads.deleteCachedVideo(it.id) } }
                    confirm = false
                    tick.intValue++
                }) { Text("Delete") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { confirm = false }) { Text("Cancel") } },
        )
    }
}

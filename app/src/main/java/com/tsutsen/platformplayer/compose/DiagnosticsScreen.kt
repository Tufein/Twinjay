package com.tsutsen.platformplayer.compose

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

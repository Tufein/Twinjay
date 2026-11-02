package com.tsutsen.platformplayer.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tsutsen.platformplayer.logging.Logger
import com.tsutsen.platformplayer.states.StatePlatform
import com.tsutsen.platformplayer.states.StatePlaylists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Import playlists by pasting their links (one per line). Creates a local copy of each. */
@Composable
fun ImportPlaylistsScreen() {
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var running by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Import playlists", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Paste one playlist link per line. Each playlist is copied into your library. Needs the matching source (for example YouTube).",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            label = { Text("Playlist links") },
        )
        Button(
            enabled = !running && text.isNotBlank(),
            onClick = {
                val urls = text.lines().map { it.trim() }.filter { it.startsWith("http") }.distinct()
                scope.launch {
                    running = true
                    var ok = 0
                    var failed = 0
                    withContext(Dispatchers.IO) {
                        urls.forEachIndexed { i, url ->
                            status = "Importing ${i + 1} / ${urls.size}..."
                            try {
                                if (!StatePlatform.instance.hasEnabledPlaylistClient(url)) {
                                    failed++
                                } else {
                                    val playlist = StatePlatform.instance.getPlaylist(url).toPlaylist()
                                    StatePlaylists.instance.createOrUpdatePlaylist(playlist)
                                    ok++
                                }
                            } catch (e: Throwable) {
                                Logger.w("ImportPlaylists", "Failed $url: ${e.message}")
                                failed++
                            }
                        }
                    }
                    status = "Done: $ok imported, $failed failed."
                    running = false
                }
            },
        ) { Text(if (running) "Importing..." else "Import") }
        status?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

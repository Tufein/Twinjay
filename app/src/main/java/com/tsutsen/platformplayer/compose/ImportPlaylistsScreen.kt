package com.tsutsen.platformplayer.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableIntStateOf
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
        modifier = Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(16.dp),
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

        HorizontalDivider()
        Text("Merge playlists", style = MaterialTheme.typography.titleLarge)
        var version by remember { mutableIntStateOf(0) }
        val playlists = remember(version) { StatePlaylists.instance.getPlaylists() }
        val picked = remember { mutableStateOf(setOf<String>()) }
        var mergedName by remember { mutableStateOf("") }
        playlists.forEach { pl ->
            Row(
                Modifier.fillMaxWidth().clickable {
                    picked.value = if (pl.id in picked.value) picked.value - pl.id else picked.value + pl.id
                },
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Checkbox(checked = pl.id in picked.value, onCheckedChange = null)
                Text("${pl.name} (${pl.videos.size})", modifier = Modifier.padding(start = 8.dp))
            }
        }
        OutlinedTextField(
            value = mergedName,
            onValueChange = { mergedName = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Name of the new playlist") },
        )
        Button(
            enabled = picked.value.size >= 2 && mergedName.isNotBlank(),
            onClick = {
                val chosen = playlists.filter { it.id in picked.value }
                val seen = HashSet<String>()
                val videos = chosen.flatMap { it.videos }.filter { seen.add(it.url) }
                StatePlaylists.instance.createOrUpdatePlaylist(
                    com.tsutsen.platformplayer.models.Playlist(mergedName.trim(), videos),
                )
                status = "Merged into \"${mergedName.trim()}\" (${videos.size} videos)."
                picked.value = emptySet()
                mergedName = ""
                version++
            },
        ) { Text("Merge") }

        HorizontalDivider()
        Text("Offline playlists", style = MaterialTheme.typography.titleLarge)
        Text(
            "A playlist kept offline downloads all its videos (up to 1080p) and picks up videos added later.",
            style = MaterialTheme.typography.bodySmall,
        )
        val downloads = com.tsutsen.platformplayer.states.StateDownloads.instance
        playlists.forEach { pl ->
            val offline = remember(version, pl.id) { downloads.hasCachedPlaylist(pl.id) }
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("${pl.name} (${pl.videos.size})", modifier = Modifier.weight(1f))
                androidx.compose.material3.TextButton(
                    enabled = offline || pl.videos.isNotEmpty(),
                    onClick = {
                        try {
                            if (offline) {
                                downloads.deleteCachedPlaylist(pl.id)
                                status = "\"${pl.name}\" is no longer kept offline."
                            } else {
                                downloads.download(pl, 1920L * 1080L, null)
                                status = "Downloading \"${pl.name}\" (${pl.videos.size} videos)."
                            }
                        } catch (e: Throwable) {
                            Logger.w("ImportPlaylists", "Offline playlist failed: ${e.message}")
                            status = "That did not work: ${e.message}"
                        }
                        version++
                    },
                ) { Text(if (offline) "Remove offline copy" else "Keep offline") }
            }
        }
    }
}

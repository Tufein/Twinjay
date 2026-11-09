package com.tsutsen.platformplayer.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tsutsen.platformplayer.core.data.util.EverythingSearch
import com.tsutsen.platformplayer.core.data.util.SubtitleIndex
import com.tsutsen.platformplayer.core.data.util.VideoBookmarks
import com.tsutsen.platformplayer.feature.player.impl.PlayerViewModel
import com.tsutsen.platformplayer.logging.Logger
import com.tsutsen.platformplayer.states.StateDownloads
import com.tsutsen.platformplayer.states.StateHistory
import com.tsutsen.platformplayer.states.StatePlaylists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

/** Search across history, downloads (with subtitles), bookmarks and playlists; a tap plays the video. */
@Composable
fun SearchEverythingScreen(playerViewModel: PlayerViewModel = hiltViewModel()) {
    SearchEverythingContent(onPlay = { playerViewModel.play(it) })
}

/** The search box and results; also used as a page on the second screen. */
@Composable
internal fun SearchEverythingContent(onPlay: (String) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<EverythingSearch.Item>?>(null) }
    var hits by remember { mutableStateOf(emptyList<EverythingSearch.Hit>()) }

    LaunchedEffect(Unit) {
        items = withContext(Dispatchers.IO) { collectEverythingItems(context) }
    }
    LaunchedEffect(query, items) {
        delay(250)
        val all = items ?: return@LaunchedEffect
        hits = withContext(Dispatchers.Default) { EverythingSearch.search(all, query) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Search everything", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("History, downloads, subtitles, bookmarks, playlists") },
        )
        when {
            items == null -> Text("Loading...", style = MaterialTheme.typography.bodySmall)
            query.trim().length < EverythingSearch.MIN_QUERY ->
                Text("Type at least ${EverythingSearch.MIN_QUERY} letters.", style = MaterialTheme.typography.bodySmall)
            hits.isEmpty() -> Text("Nothing found.", style = MaterialTheme.typography.bodySmall)
            else -> Text("${hits.size} results", style = MaterialTheme.typography.bodySmall)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(hits) { hit ->
                Column(Modifier.fillMaxWidth().clickable { onPlay(hit.url) }.padding(vertical = 4.dp)) {
                    Text(hit.title, maxLines = 2, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        listOf(hit.kind, hit.detail).filter { it.isNotBlank() }.joinToString(" · "),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

internal fun collectEverythingItems(context: android.content.Context): List<EverythingSearch.Item> {
    val out = ArrayList<EverythingSearch.Item>()
    val titles = HashMap<String, String>()
    try {
        StateDownloads.instance.getDownloadedVideos().forEach { v ->
            titles[v.url] = v.name
            out.add(EverythingSearch.Item("Download", v.name, v.author.name, v.url, SubtitleIndex.textFor(v.url).orEmpty()))
        }
    } catch (e: Throwable) {
        Logger.w("SearchEverything", "downloads: ${e.message}")
    }
    try {
        StateHistory.instance.getRecentHistory(OffsetDateTime.MIN, 2000).forEach { h ->
            titles.putIfAbsent(h.contentUrl, h.title)
            out.add(EverythingSearch.Item("History", h.title, h.author.orEmpty(), h.contentUrl))
        }
    } catch (e: Throwable) {
        Logger.w("SearchEverything", "history: ${e.message}")
    }
    try {
        StatePlaylists.instance.getPlaylists().forEach { pl ->
            pl.videos.forEach { v ->
                titles.putIfAbsent(v.url, v.name)
                out.add(EverythingSearch.Item("Playlist ${pl.name}", v.name, v.author.name, v.url))
            }
        }
    } catch (e: Throwable) {
        Logger.w("SearchEverything", "playlists: ${e.message}")
    }
    try {
        VideoBookmarks.all(context).forEach { (url, marks) ->
            val notes = marks.joinToString("; ") { it.note }.trim()
            out.add(EverythingSearch.Item("Bookmark", titles[url] ?: url, notes, url))
        }
    } catch (e: Throwable) {
        Logger.w("SearchEverything", "bookmarks: ${e.message}")
    }
    return out
}

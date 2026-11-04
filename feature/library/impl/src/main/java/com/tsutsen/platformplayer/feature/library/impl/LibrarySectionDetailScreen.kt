package com.tsutsen.platformplayer.feature.library.impl

import com.tsutsen.platformplayer.core.designsystem.theme.Tokens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tsutsen.platformplayer.core.data.repository.impl.LibraryRepositoryImpl
import com.tsutsen.platformplayer.core.designsystem.layout.AppHeader
import com.tsutsen.platformplayer.core.designsystem.component.ContentCard
import com.tsutsen.platformplayer.core.designsystem.component.ContainerLayout
import com.tsutsen.platformplayer.core.designsystem.component.PlaylistOptionsSheet
import com.tsutsen.platformplayer.core.designsystem.component.VideoCard
import com.tsutsen.platformplayer.core.designsystem.component.VideoContainer
import com.tsutsen.platformplayer.core.designsystem.component.rememberIsWide
import com.tsutsen.platformplayer.core.model.Card
import com.tsutsen.platformplayer.core.model.PlaylistCard
import com.tsutsen.platformplayer.core.navigation.Navigator
import com.tsutsen.platformplayer.feature.library.impl.VideoOptionsSheetHost
import com.tsutsen.platformplayer.feature.player.impl.PlayerViewModel
import com.tsutsen.platformplayer.core.model.VideoCard as CoreVideoCard

/**
 * Detail screen for a library section.
 * List layout in portrait, 3-column grid on wide windows.
 * Video cards play on click; playlist cards open the (placeholder) playlist.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySectionDetailScreen(
    sectionId: String,
    onBack: () -> Unit,
    navigator: Navigator,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    viewModel: LibrarySectionDetailViewModel = hiltViewModel(),
) {
    val gridColumns by viewModel.gridColumns.collectAsState()
    val section by viewModel.section.collectAsState()
    val items by viewModel.items.collectAsState()
    val isWide = rememberIsWide()
    var query by remember { mutableStateOf("") }
    // Time budget: show only what fits in N minutes, shortest first.
    var budgetMinutes by remember { mutableStateOf(0) }
    // History filter: only entries added within the last N days (0 = all).
    var sinceDays by remember { mutableStateOf(0) }
    val shown =
        remember(items, query, budgetMinutes, sinceDays) {
            val cutoff = if (sinceDays > 0) System.currentTimeMillis() - sinceDays * 86_400_000L else 0L
            val dated =
                if (cutoff == 0L) {
                    items
                } else {
                    items.filter { c -> ((c as? CoreVideoCard)?.addedAt ?: 0L) >= cutoff }
                }
            val searched =
                if (query.isBlank()) {
                    dated
                } else {
                    dated.filter { c ->
                        c.title.contains(query, ignoreCase = true) ||
                            (c as? CoreVideoCard)?.author?.contains(query, ignoreCase = true) == true
                    }
                }
            if (budgetMinutes <= 0) {
                searched
            } else {
                searched
                    .filter { c ->
                        val d = (c as? CoreVideoCard)?.durationMs
                        d != null && d > 0 && d <= budgetMinutes * 60_000L
                    }.sortedBy { (it as CoreVideoCard).durationMs }
            }
        }
    var optionsCard by remember { mutableStateOf<CoreVideoCard?>(null) }
    var optionsPlaylist by remember { mutableStateOf<PlaylistCard?>(null) }

    LaunchedEffect(sectionId) {
        viewModel.loadSection(sectionId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            AppHeader(
                title = {
                    Text(
                        text = section?.title ?: "Library",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                leading = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (sectionId == LibraryRepositoryImpl.WATCH_LATER_ID) {
                        TextButton(onClick = { viewModel.removeWatched() }) {
                            Text(
                                text = "Remove watched",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        TextButton(onClick = {
                            viewModel.removeDuplicates { n ->
                                android.widget.Toast.makeText(ctx, if (n == 0) "No duplicates" else "Removed $n duplicates", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text(
                                text = "Remove duplicates",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                },
            )

            if (items.size > 5) {
                androidx.compose.material3.OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text("Search in this list") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (sectionId == LibraryRepositoryImpl.HISTORY_ID) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                ) {
                    listOf(0 to "All time", 1 to "Today", 7 to "This week", 30 to "This month").forEach { (d, label) ->
                        androidx.compose.material3.FilterChip(
                            selected = sinceDays == d,
                            onClick = { sinceDays = d },
                            label = { Text(label) },
                        )
                    }
                }
            }

            if (items.size > 5 && items.any { it is CoreVideoCard && (it.durationMs ?: 0) > 0 }) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                ) {
                    listOf(0 to "All", 10 to "10 min", 20 to "20 min", 40 to "40 min").forEach { (m, label) ->
                        androidx.compose.material3.FilterChip(
                            selected = budgetMinutes == m,
                            onClick = { budgetMinutes = m },
                            label = { Text(if (m == 0) label else "I have $label") },
                        )
                    }
                }
            }

            ContentCard(modifier = Modifier.fillMaxSize()) {
                when {
                    section == null -> {
                        Box(modifier = Modifier.fillMaxSize())
                    }

                    items.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Nothing yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    else -> {
                    VideoContainer(
                        items = shown,
                        layout = if (isWide) ContainerLayout.Grid(gridColumns) else ContainerLayout.List,
                        isLoading = false,
                        hasMorePages = false,
                        onCardClick = { card ->
                            when (card) {
                                is CoreVideoCard -> playerViewModel.play(card)
                                is PlaylistCard -> navigator.navigateToPlaylist(card.url)
                                else -> Unit
                            }
                        },
                        onLoadMore = {},
                    ) { card ->
                        LibraryCard(
                            card = card,
                            onClick = { card ->
                                when (card) {
                                    is CoreVideoCard -> playerViewModel.play(card)
                                    is PlaylistCard -> navigator.navigateToPlaylist(card.url)
                                    else -> Unit
                                }
                            },
                            onVideoLongClick = { optionsCard = it },
                            onPlaylistLongClick = { optionsPlaylist = it },
                        )
                    }
                }
            }
            }
        }
    }

    optionsCard?.let { card ->
        VideoOptionsSheetHost(
            video = card,
            onDismiss = { optionsCard = null },
            onPlay = { playerViewModel.play(card) },
            onGoToChannel = { navigator.navigateToChannel(it) },
            // Every item on the history page is a history entry.
            onRemoveFromHistory =
                if (sectionId == LibraryRepositoryImpl.HISTORY_ID) {
                    { viewModel.deleteFromHistory(card.url) }
                } else {
                    null
                },
        )
    }

    optionsPlaylist?.let { playlist ->
        PlaylistOptionsSheetHost(
            playlist = playlist,
            onDismiss = { optionsPlaylist = null },
        )
    }
}


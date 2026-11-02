package com.tsutsen.platformplayer.feature.settings.impl

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** "What's new" notes shown once after an update. Add an entry per release. */
object WhatsNew {
    private const val FILE = "whats_new"
    private const val KEY = "last_seen_version"

    private val notes: Map<String, String> =
        mapOf(
            "0.15.0-thor.1" to
                "- Gesture sensitivity (Settings > Playback)\n" +
                "- Downloads only between chosen hours, shortest first\n" +
                "- History filters: today, this week, this month\n" +
                "- Subtitle and audio track remembered per channel\n" +
                "- Battery graph in Diagnostics\n" +
                "- Backup also holds quiet channels and channel settings",
            "0.14.0-thor.1" to
                "- Second-screen preset follows the network (Settings > Dual screen)\n" +
                "- Big clock on the dimmed second screen",
            "0.13.0-thor.1" to
                "- Swap the two screens (Settings > Dual screen)\n" +
                "- Choose which SponsorBlock parts to skip\n" +
                "- Merge playlists\n" +
                "- Download space per channel in Diagnostics",
            "0.12.0-thor.1" to
                "- Group chips in the subscriptions feed\n" +
                "- Quiet channels and offline mode\n" +
                "- Speed buttons on the second screen\n" +
                "- Time budget in library lists (10/20/40 min)\n" +
                "- Diagnostics page in Settings",
            "0.11.0-thor.1" to
                "- Subscription groups (Settings)\n" +
                "- Import playlists by link (Settings)\n" +
                "- Picture-in-picture buttons: back, play/pause, forward",
            "0.10.0-thor.1" to
                "- Options sheet: sleep timer, repeat a part (A-B), audio only\n" +
                "- Playback speed is remembered per channel\n" +
                "- Search in your history and other lists\n" +
                "- Hide videos by word (Settings > Playback)\n" +
                "- Presets for the second screen (Settings > Dual screen)\n" +
                "- Old downloads can be deleted automatically\n" +
                "- Playback problems show a Share log button",
        )

    fun notesFor(version: String): String? = notes[version]

    /** Decides what to show: nothing on a first install or when already seen. */
    fun pending(lastSeen: String?, installed: String): String? =
        if (lastSeen == null || lastSeen == installed) null else notesFor(installed)

    fun lastSeen(context: Context): String? =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null)

    fun markSeen(context: Context, version: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, version).apply()
    }
}

@Composable
fun WhatsNewHost() {
    val context = LocalContext.current
    val installed = UpdatePrompt.installedVersion(context)
    var text by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(installed) {
        text = WhatsNew.pending(WhatsNew.lastSeen(context), installed)
        WhatsNew.markSeen(context, installed)
    }
    text?.let { notes ->
        AlertDialog(
            onDismissRequest = { text = null },
            title = { Text("What's new in $installed") },
            text = { Text(notes) },
            confirmButton = { TextButton(onClick = { text = null }) { Text("OK") } },
        )
    }
}

package com.tsutsen.platformplayer.compose

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tsutsen.platformplayer.importer.TakeoutCsv
import com.tsutsen.platformplayer.logging.Logger
import com.tsutsen.platformplayer.states.StatePlatform
import com.tsutsen.platformplayer.states.StateSubscriptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.async
import java.util.concurrent.atomic.AtomicInteger

/**
 * Settings > Import subscriptions: pick the subscriptions.csv from Google
 * Takeout (or a plain list of channel links) and subscribe to every channel.
 */
@Composable
fun ImportSubscriptionsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var total by remember { mutableIntStateOf(0) }
    var done by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<String?>(null) }

    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                running = true
                summary = null
                done = 0
                try {
                    val text =
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                        }.orEmpty()
                    val entries = TakeoutCsv.parse(text)
                    total = entries.size
                    if (entries.isEmpty()) {
                        summary = "No channels found in that file. Pick subscriptions.csv from Google Takeout."
                        return@launch
                    }
                    val added = AtomicInteger()
                    val skipped = AtomicInteger()
                    val failed = AtomicInteger()
                    val gate = Semaphore(3)
                    withContext(Dispatchers.IO) {
                        entries
                            .map { entry ->
                                async {
                                    gate.withPermit {
                                        try {
                                            if (StateSubscriptions.instance.isSubscribed(entry.url)) {
                                                skipped.incrementAndGet()
                                            } else if (!StatePlatform.instance.hasEnabledChannelClient(entry.url)) {
                                                failed.incrementAndGet()
                                            } else {
                                                val channel = StatePlatform.instance.getChannel(entry.url).await()
                                                StateSubscriptions.instance.addSubscription(channel)
                                                added.incrementAndGet()
                                            }
                                        } catch (e: Throwable) {
                                            Logger.w("ImportSubscriptions", "Failed ${entry.url}: ${e.message}")
                                            failed.incrementAndGet()
                                        }
                                        done = done + 1
                                    }
                                }
                            }.awaitAll()
                    }
                    summary =
                        "Done: ${added.get()} added, ${skipped.get()} were already subscribed, ${failed.get()} failed " +
                        "(channels that no longer exist, or no enabled source for them)."
                } catch (e: Throwable) {
                    Logger.e("ImportSubscriptions", "Import failed", e)
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    running = false
                }
            }
        }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Import subscriptions", style = MaterialTheme.typography.headlineSmall)
        Text(
            "1. Go to takeout.google.com, choose only YouTube > Subscriptions and export.\n" +
                "2. Open the downloaded file and pick subscriptions.csv here.\n" +
                "A plain text file with one channel link per line works too. Needs the YouTube source.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = { picker.launch(arrayOf("*/*")) }, enabled = !running) {
            Text(if (running) "Importing..." else "Choose file")
        }
        if (running && total > 0) {
            LinearProgressIndicator(progress = { done.toFloat() / total }, modifier = Modifier.fillMaxWidth())
            Text("$done / $total", style = MaterialTheme.typography.bodySmall)
        }
        summary?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

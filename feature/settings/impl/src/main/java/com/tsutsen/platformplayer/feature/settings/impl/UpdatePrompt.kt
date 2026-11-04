package com.tsutsen.platformplayer.feature.settings.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tsutsen.platformplayer.core.data.update.GithubReleaseUpdate
import com.tsutsen.platformplayer.core.data.update.ReleaseInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/** Helpers shared by the startup prompt and the About screen. */
internal object UpdatePrompt {
    private const val PREFS = "twinjay_update"
    private const val KEY_DISMISSED = "dismissed_tag"

    /** Only the first prompt per app process, so rotating or recomposing never re-asks. */
    @Volatile
    var checkedThisProcess = false

    fun installedVersion(context: Context): String =
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "unknown"

    fun dismissedTag(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DISMISSED, null)

    fun dismiss(context: Context, tag: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_DISMISSED, tag).apply()
    }

    fun sizeLabel(bytes: Long?): String? =
        bytes?.takeIf { it > 0 }?.let { String.format(Locale.US, "%.1f MB", it / 1_048_576.0) }

    fun openUrl(context: Context, url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

/**
 * Checks GitHub once per app start and offers a newer release in a dialog.
 * "Later" remembers that tag, so the same version is not offered again at
 * the next start (the About screen still shows it).
 */
@Composable
fun UpdatePromptHost() {
    val context = LocalContext.current
    if (context.packageName != "com.tufein.bluejay.thor") return
    var release by remember { mutableStateOf<ReleaseInfo?>(null) }
    LaunchedEffect(Unit) {
        if (UpdatePrompt.checkedThisProcess) return@LaunchedEffect
        UpdatePrompt.checkedThisProcess = true
        val latest = withContext(Dispatchers.IO) { runCatching { GithubReleaseUpdate.fetchLatest() }.getOrNull() }
        if (latest != null &&
            GithubReleaseUpdate.isNewer(latest.tag, UpdatePrompt.installedVersion(context)) &&
            UpdatePrompt.dismissedTag(context) != latest.tag
        ) {
            release = latest
        }
    }
    release?.let { r ->
        UpdateScreen(
            release = r,
            currentVersion = UpdatePrompt.installedVersion(context),
            onLater = {
                UpdatePrompt.dismiss(context, r.tag)
                release = null
            },
        )
    }
}

private enum class Phase { Idle, Downloading, NeedsPermission, Installing, Confirming, Failed }

/**
 * Full-screen, in-app update: shows what is new, downloads the APK inside the app with a
 * progress bar and installs it through the system installer, with no browser or
 * notification detour. "Later" remembers the tag so it is not offered again at the next start.
 */
@Composable
internal fun UpdateScreen(
    release: ReleaseInfo,
    currentVersion: String,
    onLater: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    var phase by remember { mutableStateOf(Phase.Idle) }
    var done by remember { mutableStateOf(0L) }
    var total by remember { mutableStateOf<Long?>(release.apkSizeBytes) }
    var error by remember { mutableStateOf<String?>(null) }
    var apk by remember { mutableStateOf<File?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }

    fun startInstall(file: File) {
        if (!InAppUpdate.canInstall(context)) {
            phase = Phase.NeedsPermission
            return
        }
        phase = Phase.Installing
        scope.launch {
            InAppUpdate.install(context, file) { r ->
                when (r) {
                    InAppUpdate.InstallResult.Success -> Unit // the app is replaced and restarts
                    InAppUpdate.InstallResult.WaitingForUser -> phase = Phase.Confirming
                    is InAppUpdate.InstallResult.Failed -> {
                        error = r.message
                        phase = Phase.Failed
                    }
                }
            }
        }
    }

    fun startDownload() {
        val url = release.apkUrl
        if (url == null) {
            UpdatePrompt.openUrl(context, release.pageUrl)
            return
        }
        error = null
        done = 0L
        phase = Phase.Downloading
        job =
            scope.launch {
                try {
                    val file =
                        InAppUpdate.download(context, release.tag, url, release.apkSizeBytes) { d, t ->
                            done = d
                            if (t != null) total = t
                        }
                    apk = file
                    startInstall(file)
                } catch (e: CancellationException) {
                    phase = Phase.Idle
                    throw e
                } catch (e: Throwable) {
                    error = "Download failed: ${e.message ?: "unknown error"}"
                    phase = Phase.Failed
                }
            }
    }

    // Back from the system's "allow installs" screen: carry on by itself.
    LaunchedEffect(phase) {
        if (phase == Phase.NeedsPermission) {
            while (!InAppUpdate.canInstall(context)) delay(600)
            apk?.let { startInstall(it) }
        }
    }

    val busy = phase == Phase.Downloading || phase == Phase.Installing
    Dialog(
        onDismissRequest = { if (!busy) onLater() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = !busy, dismissOnClickOutside = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = scheme.background) {
            Column(
                modifier = Modifier.systemBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(shape = CircleShape, color = scheme.primaryContainer, modifier = Modifier.fillMaxSize()) {}
                    Icon(
                        Icons.Filled.SystemUpdateAlt,
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Update available", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    release.tag,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.primary,
                )
                Text(
                    "You have $currentVersion" + (UpdatePrompt.sizeLabel(release.apkSizeBytes)?.let { "  ·  $it download" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
                ) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                        Text("What's new", style = MaterialTheme.typography.titleSmall, color = scheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            release.notes.ifBlank { "Fixes and improvements." },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your logins, subscriptions and settings are kept.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))

                when (phase) {
                    Phase.Downloading -> {
                        val t = total
                        if (t != null && t > 0) {
                            LinearProgressIndicator(progress = { (done.toFloat() / t).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Downloading  ·  ${InAppUpdate.progressLabel(done, total)}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { job?.cancel() }, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
                    }
                    Phase.Installing -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Text("Preparing the install…", style = MaterialTheme.typography.bodyMedium)
                    }
                    Phase.Confirming -> {
                        Text(
                            "Confirm the install in the system window. TwinJay restarts when it is done.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { apk?.let { startInstall(it) } }, modifier = Modifier.fillMaxWidth()) {
                            Text("Show the confirmation again")
                        }
                    }
                    Phase.NeedsPermission -> {
                        Text(
                            "Android needs your OK to let TwinJay install updates. Allow it on the next screen and come back; the install continues by itself.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { InAppUpdate.openInstallPermissionSettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Allow installs") }
                        TextButton(onClick = onLater) { Text("Later") }
                    }
                    Phase.Failed -> {
                        Text(
                            error ?: "Something went wrong.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { apk?.takeIf { it.exists() }?.let { startInstall(it) } ?: startDownload() },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Try again") }
                        TextButton(onClick = { UpdatePrompt.openUrl(context, release.pageUrl) }) { Text("Open the release page") }
                        TextButton(onClick = onLater) { Text("Later") }
                    }
                    Phase.Idle -> {
                        Button(onClick = { startDownload() }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (release.apkUrl == null) "Open the release page" else "Update now")
                        }
                        TextButton(onClick = onLater) { Text("Later") }
                    }
                }
            }
        }
    }
}

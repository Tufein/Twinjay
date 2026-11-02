package com.tsutsen.platformplayer.feature.settings.impl

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tsutsen.platformplayer.core.data.update.GithubReleaseUpdate
import com.tsutsen.platformplayer.core.data.update.ReleaseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    /**
     * Downloads the APK with the system download manager, which shows
     * progress in the notification shade; tapping the finished download
     * starts the install. Falls back to the browser if that fails.
     */
    fun download(context: Context, release: ReleaseInfo) {
        val apk = release.apkUrl
        if (apk == null) {
            openUrl(context, release.pageUrl)
            return
        }
        try {
            val fileName = apk.substringAfterLast('/')
            val request =
                DownloadManager.Request(Uri.parse(apk))
                    .setTitle("TwinJay ${release.tag}")
                    .setDescription("Tap when finished to install")
                    .setMimeType("application/vnd.android.package-archive")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            Toast.makeText(
                context,
                "Downloading ${release.tag}. Tap the notification when it is done to install.",
                Toast.LENGTH_LONG,
            ).show()
        } catch (e: Throwable) {
            openUrl(context, apk)
        }
    }

    private fun openUrl(context: Context, url: String) {
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
        UpdateDialog(
            release = r,
            currentVersion = UpdatePrompt.installedVersion(context),
            onDownload = {
                UpdatePrompt.download(context, r)
                release = null
            },
            onLater = {
                UpdatePrompt.dismiss(context, r.tag)
                release = null
            },
        )
    }
}

/** "TwinJay v0.9.0 is available": version change, release notes, download / later. */
@Composable
internal fun UpdateDialog(
    release: ReleaseInfo,
    currentVersion: String,
    onDownload: () -> Unit,
    onLater: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onLater,
        icon = { Icon(Icons.Filled.SystemUpdateAlt, contentDescription = null, tint = scheme.primary) },
        title = { Text("Update available") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${release.tag}  ·  you have $currentVersion" +
                        (UpdatePrompt.sizeLabel(release.apkSizeBytes)?.let { "  ·  $it" } ?: ""),
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.primary,
                )
                if (release.notes.isNotBlank()) {
                    Text("What's new", style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                    Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                        Text(release.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(
                    "Your logins, subscriptions and settings are kept when you install over the current version.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDownload) { Text("Download") } },
        dismissButton = { TextButton(onClick = onLater) { Text("Later") } },
    )
}

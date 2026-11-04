package com.tsutsen.platformplayer.feature.settings.impl

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

/**
 * Downloads a release APK into the app's own cache and installs it through the
 * system PackageInstaller, all without leaving the app.
 */
internal object InAppUpdate {
    private const val ACTION = "com.tufein.bluejay.thor.UPDATE_STATUS"

    /** Result of an install attempt reported by the system installer. */
    sealed interface InstallResult {
        object Success : InstallResult

        data class Failed(val message: String) : InstallResult

        /** The system wants a confirmation; the screen opened it. */
        object WaitingForUser : InstallResult
    }

    fun updatesDir(context: Context): File = File(context.cacheDir, "updates").apply { mkdirs() }

    fun canInstall(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    fun openInstallPermissionSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    /** Pure helper (unit tested): "123.4 MB of 218.0 MB". */
    fun progressLabel(done: Long, total: Long?): String {
        fun mb(b: Long) = String.format(java.util.Locale.US, "%.1f MB", b / 1_048_576.0)
        return if (total != null && total > 0) "${mb(done)} of ${mb(total)}" else mb(done)
    }

    /**
     * Downloads [apkUrl] to the updates cache and returns the finished file.
     * Reuses a complete earlier download. [onProgress] gets bytes done and the total (when known).
     */
    suspend fun download(
        context: Context,
        tag: String,
        apkUrl: String,
        expectedSize: Long?,
        onProgress: (Long, Long?) -> Unit,
    ): File =
        withContext(Dispatchers.IO) {
            val dir = updatesDir(context)
            val target = File(dir, "TwinJay-${tag.replace(Regex("[^A-Za-z0-9._-]"), "_")}.apk")
            dir.listFiles()?.filter { it != target }?.forEach { it.delete() }
            if (target.exists() && expectedSize != null && expectedSize > 0 && target.length() == expectedSize) {
                onProgress(target.length(), expectedSize)
                return@withContext target
            }
            val part = File(dir, target.name + ".part")
            var conn: HttpURLConnection? = null
            try {
                conn = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "TwinJay-updater")
                }
                val code = conn.responseCode
                if (code !in 200..299) throw IllegalStateException("Server answered $code")
                val total = conn.contentLengthLong.takeIf { it > 0 } ?: expectedSize
                var done = 0L
                var lastReport = 0L
                conn.inputStream.use { input ->
                    part.outputStream().use { out ->
                        val buf = ByteArray(64 * 1024)
                        while (true) {
                            coroutineContext.ensureActive()
                            val n = input.read(buf)
                            if (n < 0) break
                            out.write(buf, 0, n)
                            done += n
                            val now = System.nanoTime()
                            if (now - lastReport > 150_000_000L) {
                                lastReport = now
                                onProgress(done, total)
                            }
                        }
                    }
                }
                if (total != null && done != total) throw IllegalStateException("Download was cut off ($done of $total bytes)")
                onProgress(done, total)
                target.delete()
                if (!part.renameTo(target)) throw IllegalStateException("Could not save the update")
                target
            } catch (e: CancellationException) {
                part.delete()
                throw e
            } catch (e: Throwable) {
                part.delete()
                throw e
            } finally {
                conn?.disconnect()
            }
        }

    /**
     * Streams [apk] into a PackageInstaller session and commits it. [onResult] is called from the
     * system's status broadcasts; the receiver removes itself after a final result.
     */
    suspend fun install(context: Context, apk: File, onResult: (InstallResult) -> Unit) {
        val app = context.applicationContext
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(c: Context, intent: Intent) {
                    when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
                        PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                            @Suppress("DEPRECATION")
                            val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                            if (confirm != null) {
                                runCatching { c.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                                onResult(InstallResult.WaitingForUser)
                            } else {
                                finish(c, InstallResult.Failed("The system installer did not open"))
                            }
                        }
                        PackageInstaller.STATUS_SUCCESS -> finish(c, InstallResult.Success)
                        else ->
                            finish(
                                c,
                                InstallResult.Failed(
                                    friendly(intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)),
                                ),
                            )
                    }
                }

                fun finish(c: Context, r: InstallResult) {
                    runCatching { c.applicationContext.unregisterReceiver(this) }
                    onResult(r)
                }
            }
        ContextCompat.registerReceiver(app, receiver, IntentFilter(ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
        try {
            withContext(Dispatchers.IO) {
                val installer = app.packageManager.packageInstaller
                val id = installer.createSession(PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL))
                installer.openSession(id).use { session ->
                    apk.inputStream().use { input ->
                        session.openWrite("package", 0, apk.length()).use { out ->
                            input.copyTo(out)
                            session.fsync(out)
                        }
                    }
                    val pending =
                        PendingIntent.getBroadcast(
                            app,
                            id,
                            Intent(ACTION).setPackage(app.packageName),
                            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                        )
                    session.commit(pending.intentSender)
                }
            }
        } catch (e: Throwable) {
            runCatching { app.unregisterReceiver(receiver) }
            onResult(InstallResult.Failed(friendly(e.message)))
        }
    }

    private fun friendly(raw: String?): String {
        val m = raw.orEmpty()
        return when {
            m.contains("allocat", true) || m.contains("ENOSPC", true) || m.contains("storage", true) ->
                "Not enough free storage. Free up some space and try again."
            m.contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE", true) || m.contains("signature", true) ->
                "This update is signed differently from the installed app, so it cannot replace it."
            m.isBlank() -> "The install did not finish."
            else -> m
        }
    }
}

package com.tsutsen.platformplayer.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.StatFs
import java.security.MessageDigest
import java.util.Locale

/** Small facts for the Diagnostics page: free storage and the fingerprint of the key this build is signed with. */
object DeviceInfo {
    /** 1536 -> "1.5 KB", 5_368_709_120 -> "5.0 GB". Negative values count as 0. */
    fun formatBytes(bytes: Long): String {
        var v = bytes.coerceAtLeast(0).toDouble()
        val units = listOf("B", "KB", "MB", "GB", "TB")
        var i = 0
        while (v >= 1024 && i < units.lastIndex) {
            v /= 1024
            i++
        }
        return if (i == 0) "${v.toLong()} B" else String.format(Locale.US, "%.1f %s", v, units[i])
    }

    /** "ab12cd" -> "AB:12:CD", the way `apksigner --print-certs` style tools group it. */
    fun colonHex(bytes: ByteArray): String = bytes.joinToString(":") { "%02X".format(it) }

    /** Free and total bytes of the app's storage, or null when it cannot be read. */
    fun storage(context: Context): Pair<Long, Long>? =
        runCatching {
            val st = StatFs(context.filesDir.absolutePath)
            st.availableBytes to st.totalBytes
        }.getOrNull()

    /** SHA-256 of the certificate this app is signed with (compare with the release's apk-signature.txt). */
    fun signingFingerprint(context: Context): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val sig = info.signingInfo?.apkContentsSigners?.firstOrNull() ?: return null
            colonHex(MessageDigest.getInstance("SHA-256").digest(sig.toByteArray()))
        }.getOrNull()
    }
}

package com.tsutsen.platformplayer.di

import android.content.Context
import com.tsutsen.platformplayer.Settings
import java.io.File

/**
 * Writes the settings as text once a day into the app's own folder on shared
 * storage (Android/data/<app>/files/backups) and keeps the last seven. The
 * file has the same format as "Share settings", so "Import settings" reads it.
 */
object DailySettingsBackup {
    const val KEEP = 7
    private const val PREFIX = "twinjay-settings-"

    fun fileName(day: String): String = "$PREFIX$day.txt"

    /** Names to delete so only the newest [keep] stay; the date in the name sorts by itself. */
    fun toDelete(names: List<String>, keep: Int = KEEP): List<String> =
        names.filter { it.startsWith(PREFIX) && it.endsWith(".txt") }.sortedDescending().drop(keep.coerceAtLeast(0))

    /** Returns the written file, or null when today's file exists already or writing failed. */
    fun run(context: Context, today: String = java.time.LocalDate.now().toString()): File? =
        try {
            val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "backups").apply { mkdirs() }
            val target = File(dir, fileName(today))
            if (target.exists()) {
                null
            } else {
                target.writeText("TWINJAY-SETTINGS-1\n" + Settings.instance.encode())
                toDelete(dir.list()?.toList().orEmpty()).forEach { File(dir, it).delete() }
                target
            }
        } catch (_: Throwable) {
            null
        }
}

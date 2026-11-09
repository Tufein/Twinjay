package com.tsutsen.platformplayer.states

import android.content.Context
import com.tsutsen.platformplayer.api.media.Serializer
import com.tsutsen.platformplayer.api.media.platforms.js.SourcePluginConfig
import com.tsutsen.platformplayer.logging.Logger
import kotlinx.serialization.encodeToString
import java.io.File

/**
 * Keeps the previous version of a source plugin when it is updated, so a
 * broken update can be undone from Diagnostics.
 */
object PluginRollback {
    private const val TAG = "PluginRollback"

    /** Only a real version change is worth keeping; a reinstall of the same version is not. */
    fun shouldKeep(oldVersion: Int, newVersion: Int): Boolean = oldVersion != newVersion

    /** File-safe name for a plugin id. */
    fun safeName(id: String): String = id.replace(Regex("[^A-Za-z0-9_-]"), "_")

    private fun dir(context: Context) = File(context.filesDir, "plugin_rollback").apply { mkdirs() }

    fun keep(context: Context, config: SourcePluginConfig, script: String) {
        try {
            val d = dir(context)
            File(d, safeName(config.id) + ".json").writeText(Serializer.json.encodeToString(config))
            File(d, safeName(config.id) + ".js").writeText(script)
        } catch (e: Throwable) {
            Logger.w(TAG, "Could not keep the previous version of ${config.name}: ${e.message}")
        }
    }

    /** The kept version number, or null when there is nothing to go back to. */
    fun keptVersion(context: Context, id: String): Int? =
        try {
            val f = File(dir(context), safeName(id) + ".json")
            if (f.exists() && File(dir(context), safeName(id) + ".js").exists()) {
                SourcePluginConfig.fromJson(f.readText()).version
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }

    /** Puts the kept version back (the current one becomes the kept one). Returns an error text or null. */
    fun restore(context: Context, id: String): String? =
        try {
            val d = dir(context)
            val config = SourcePluginConfig.fromJson(File(d, safeName(id) + ".json").readText())
            val script = File(d, safeName(id) + ".js").readText()
            StatePlugins.instance.createPlugin(config, script, null, true)?.let { it.message ?: it.javaClass.simpleName }
        } catch (e: Throwable) {
            e.message ?: e.javaClass.simpleName
        }
}

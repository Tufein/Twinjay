package com.tsutsen.platformplayer.di

import android.content.Context

/** Backup of TwinJay's own small stores (quiet channels, per-channel speed/audio/subtitle, download priority). */
object TwinjayExtras {
    const val STORE = "twinjay_extras"
    private val FILES = listOf("quiet_channels", "channel_speed", "channel_tracks", "download_priority")

    fun export(context: Context): List<String> {
        val out = mutableListOf<String>()
        for (file in FILES) {
            val prefs = context.getSharedPreferences(file, Context.MODE_PRIVATE)
            for ((k, v) in prefs.all) {
                when (v) {
                    is Float -> out.add(line(file, "f", k, v.toString()))
                    is Int -> out.add(line(file, "i", k, v.toString()))
                    is String -> out.add(line(file, "s", k, v))
                    is Set<*> -> out.add(line(file, "set", k, v.joinToString("\u0001")))
                }
            }
        }
        return out
    }

    fun import(context: Context, lines: List<String>): Int {
        var n = 0
        for (l in lines) {
            val p = l.split("\u0002")
            if (p.size < 4 || p[0] !in FILES) continue
            val e = context.getSharedPreferences(p[0], Context.MODE_PRIVATE).edit()
            when (p[1]) {
                "f" -> e.putFloat(p[2], p[3].toFloatOrNull() ?: continue)
                "i" -> e.putInt(p[2], p[3].toIntOrNull() ?: continue)
                "s" -> e.putString(p[2], p[3])
                "set" -> e.putStringSet(p[2], p[3].split("\u0001").filter { it.isNotEmpty() }.toSet())
                else -> continue
            }
            e.apply()
            n++
        }
        return n
    }

    private fun line(file: String, type: String, key: String, value: String) =
        listOf(file, type, key, value).joinToString("\u0002")
}

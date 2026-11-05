package com.tsutsen.platformplayer.core.data.util

import android.content.Context

/** Bookmarks inside a video: a position and an optional note. */
object VideoBookmarks {
    private const val FILE = "video_bookmarks"

    data class Bookmark(val positionMs: Long, val note: String)

    fun encode(list: List<Bookmark>): String =
        list.joinToString("\n") { "${it.positionMs}|${it.note.replace('\n', ' ')}" }

    fun decode(text: String?): List<Bookmark> =
        (text ?: "")
            .lines()
            .mapNotNull { line ->
                val i = line.indexOf('|')
                if (i <= 0) return@mapNotNull null
                val ms = line.substring(0, i).toLongOrNull() ?: return@mapNotNull null
                Bookmark(ms, line.substring(i + 1))
            }.sortedBy { it.positionMs }

    fun get(context: Context, url: String): List<Bookmark> =
        decode(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(url, ""))

    fun put(context: Context, url: String, list: List<Bookmark>) {
        val e = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
        if (list.isEmpty()) e.remove(url) else e.putString(url, encode(list))
        e.apply()
    }

    /** Every video that has bookmarks, with its bookmarks. */
    fun all(context: Context): Map<String, List<Bookmark>> =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).all
            .mapNotNull { (k, v) -> (v as? String)?.let { k to decode(it) } }
            .filter { it.second.isNotEmpty() }
            .toMap()
}

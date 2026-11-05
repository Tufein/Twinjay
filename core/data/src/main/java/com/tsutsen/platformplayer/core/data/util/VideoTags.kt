package com.tsutsen.platformplayer.core.data.util

import android.content.Context

/** Own tags per video (url), kept on the device in a small preference file. */
object VideoTags {
    private const val FILE = "video_tags"

    /** "Funny, Cooking ,,funny" -> [funny, cooking] (lower case, no duplicates, no empties). */
    fun parse(text: String): List<String> =
        text.split(',', '\n', '#')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()

    fun get(context: Context, url: String): List<String> =
        parse(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(url, "") ?: "")

    fun set(context: Context, url: String, tags: List<String>) {
        val e = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
        if (tags.isEmpty()) e.remove(url) else e.putString(url, tags.joinToString(","))
        e.apply()
    }

    /** Every tag in use with the urls that carry it. */
    fun index(context: Context): Map<String, Set<String>> {
        val out = HashMap<String, MutableSet<String>>()
        for ((url, v) in context.getSharedPreferences(FILE, Context.MODE_PRIVATE).all) {
            for (t in parse(v as? String ?: "")) out.getOrPut(t) { HashSet() }.add(url)
        }
        return out
    }
}

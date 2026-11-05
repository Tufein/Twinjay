package com.tsutsen.platformplayer.core.data.util

import android.content.Context

/** Pinned search queries, shown above the recent searches. */
object SavedSearches {
    private const val FILE = "saved_searches"
    private const val KEY = "list"

    fun toggle(list: List<String>, query: String): List<String> {
        val q = query.trim()
        if (q.isEmpty()) return list
        return if (list.any { it.equals(q, ignoreCase = true) }) {
            list.filterNot { it.equals(q, ignoreCase = true) }
        } else {
            list + q
        }
    }

    fun get(context: Context): List<String> =
        (context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, "") ?: "")
            .split('\n')
            .filter { it.isNotBlank() }

    fun put(context: Context, list: List<String>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, list.joinToString("\n")).apply()
    }
}

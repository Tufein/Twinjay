package com.tsutsen.platformplayer.core.data.util

/** Finds later copies of the same item: keeps the first of each key, returns the rest. */
object Dedupe {
    fun <T> extras(items: List<T>, key: (T) -> String): List<T> {
        val seen = HashSet<String>()
        return items.filter { !seen.add(key(it)) }
    }

    fun normalizeTitle(title: String, author: String?): String =
        (title.lowercase().filter { it.isLetterOrDigit() } + "|" + author.orEmpty().lowercase().filter { it.isLetterOrDigit() })
}

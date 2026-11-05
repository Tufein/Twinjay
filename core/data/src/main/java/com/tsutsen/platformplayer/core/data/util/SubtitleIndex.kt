package com.tsutsen.platformplayer.core.data.util

/** Lets the library search inside the subtitle text of downloaded videos. */
object SubtitleIndex {
    /** Set by the app: the subtitle text saved with a downloaded video (null = none). */
    @Volatile var textFor: (String) -> String? = { null }

    /** True when the subtitle text contains [query] (case insensitive); short queries never match. */
    fun matches(text: String?, query: String): Boolean {
        val q = query.trim()
        if (text.isNullOrEmpty() || q.length < 3) return false
        return text.contains(q, ignoreCase = true)
    }

    fun matchesVideo(url: String, query: String): Boolean = matches(textFor(url), query)
}

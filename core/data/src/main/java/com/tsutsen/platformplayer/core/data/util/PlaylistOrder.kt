package com.tsutsen.platformplayer.core.data.util

/** Display and play order of a playlist: as saved, oldest first, newest first or shuffled. */
object PlaylistOrder {
    const val DEFAULT = "default"
    const val OLDEST = "oldest"
    const val NEWEST = "newest"
    const val SHUFFLE = "shuffle"

    /** [date] returns the video's posted time (or null); unknown dates sort last. */
    fun <T> apply(items: List<T>, mode: String, seed: Long, date: (T) -> Long?): List<T> =
        when (mode) {
            OLDEST -> items.sortedBy { date(it) ?: Long.MAX_VALUE }
            NEWEST -> items.sortedByDescending { date(it) ?: Long.MIN_VALUE }
            SHUFFLE -> items.shuffled(java.util.Random(seed))
            else -> items
        }
}

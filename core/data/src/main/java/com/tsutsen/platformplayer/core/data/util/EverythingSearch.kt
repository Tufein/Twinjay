package com.tsutsen.platformplayer.core.data.util

/** One search over history, downloads, subtitles, bookmarks and playlists. Pure, so it can be unit tested. */
object EverythingSearch {
    data class Item(val kind: String, val title: String, val detail: String, val url: String, val extraText: String = "")

    data class Hit(val kind: String, val title: String, val detail: String, val url: String)

    const val MIN_QUERY = 2

    /**
     * Items whose title, detail or extra text contain every word of [query]
     * (case insensitive). The same video in the same kind shows once.
     */
    fun search(items: List<Item>, query: String, limit: Int = 100): List<Hit> {
        val words = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty() || query.trim().length < MIN_QUERY) return emptyList()
        val seen = HashSet<String>()
        val out = ArrayList<Hit>()
        for (item in items) {
            val hay = "${item.title}\n${item.detail}\n${item.extraText}".lowercase()
            if (words.all { hay.contains(it) } && seen.add(item.kind + "|" + item.url)) {
                out.add(Hit(item.kind, item.title, item.detail, item.url))
                if (out.size >= limit) break
            }
        }
        return out
    }
}

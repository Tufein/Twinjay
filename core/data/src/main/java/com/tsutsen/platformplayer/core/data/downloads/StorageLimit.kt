package com.tsutsen.platformplayer.core.data.downloads

/** Pure rule for the download size limit: which entries to delete, oldest first, to fit the limit. */
object StorageLimit {
    data class Entry(val key: String, val time: Long, val size: Long)

    fun toDelete(entries: List<Entry>, limitBytes: Long): List<String> {
        if (limitBytes <= 0) return emptyList()
        var total = entries.sumOf { it.size }
        val out = mutableListOf<String>()
        for (e in entries.sortedBy { it.time }) {
            if (total <= limitBytes) break
            out.add(e.key)
            total -= e.size
        }
        return out
    }
}

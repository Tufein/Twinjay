package com.tsutsen.platformplayer.core.data.log

/** Last failure per source, kept in memory so Diagnostics can say why a source is not working. */
object SourceErrors {
    data class Entry(val source: String, val action: String, val message: String, val timeMs: Long, val count: Int)

    private val entries = LinkedHashMap<String, Entry>()

    @Synchronized
    fun record(source: String, action: String, error: Throwable, nowMs: Long = System.currentTimeMillis()) {
        val text = (error.message ?: error.javaClass.simpleName).lineSequence().firstOrNull().orEmpty().take(200)
        val count = (entries[source]?.count ?: 0) + 1
        entries[source] = Entry(source, action, text.ifBlank { error.javaClass.simpleName }, nowMs, count)
    }

    @Synchronized
    fun all(): List<Entry> = entries.values.sortedByDescending { it.timeMs }

    @Synchronized
    fun clear() = entries.clear()

    /** "getHome failed 3 min ago (4x): message". */
    fun describe(e: Entry, nowMs: Long): String {
        val min = ((nowMs - e.timeMs).coerceAtLeast(0) / 60_000).toInt()
        val ago = if (min < 1) "just now" else if (min < 60) "$min min ago" else "${min / 60} h ago"
        val times = if (e.count > 1) " (${e.count}x)" else ""
        return "${e.action} failed $ago$times: ${e.message}"
    }
}

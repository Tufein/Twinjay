package com.tsutsen.platformplayer.importer

/**
 * Parses the subscriptions export of Google Takeout (YouTube > subscriptions.csv:
 * "Channel Id,Channel Url,Channel Title") and, as a fallback, plain lists of
 * channel urls / ids, one per line.
 */
object TakeoutCsv {
    data class Entry(val url: String, val title: String?)

    private val channelId = Regex("^UC[A-Za-z0-9_-]{22}$")

    fun parse(text: String): List<Entry> {
        val lines = text.removePrefix("﻿").lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val first = splitCsv(lines.first()).map { it.trim().lowercase() }
        val hasHeader = first.any { it == "channel id" || it == "channel url" || it == "channel title" }
        val idIdx = first.indexOf("channel id")
        val urlIdx = first.indexOf("channel url")
        val titleIdx = first.indexOf("channel title")

        val rows = if (hasHeader) lines.drop(1) else lines
        val seen = linkedSetOf<String>()
        val out = mutableListOf<Entry>()
        for (line in rows) {
            val fields = splitCsv(line).map { it.trim() }
            var url: String? = null
            var title: String? = null
            if (hasHeader) {
                url = fields.getOrNull(urlIdx)?.takeIf { isUrl(it) }
                    ?: fields.getOrNull(idIdx)?.let { fromId(it) }
                title = fields.getOrNull(titleIdx)?.ifBlank { null }
            } else {
                url = fields.firstOrNull { isUrl(it) } ?: fields.firstNotNullOfOrNull { fromId(it) }
                title = fields.firstOrNull { it.isNotBlank() && it != url && !isUrl(it) && fromId(it) == null }
            }
            if (url == null) continue
            val normalized = url.replace("http://", "https://")
            if (seen.add(normalized)) out.add(Entry(normalized, title))
        }
        return out
    }

    private fun isUrl(s: String) = s.startsWith("http://") || s.startsWith("https://")

    private fun fromId(s: String): String? = if (channelId.matches(s)) "https://www.youtube.com/channel/$s" else null

    /** Minimal CSV line splitter: commas, optional double quotes with "" escapes. */
    internal fun splitCsv(line: String): List<String> {
        val out = mutableListOf<String>()
        val cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    cur.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    out.add(cur.toString())
                    cur.clear()
                }
                else -> cur.append(c)
            }
            i++
        }
        out.add(cur.toString())
        return out
    }
}

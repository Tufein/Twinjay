package com.tsutsen.platformplayer.core.data.update

import java.net.HttpURLConnection
import java.net.URL

/** A published GitHub release of TwinJay. */
data class ReleaseInfo(
    val tag: String,
    val pageUrl: String,
    val apkUrl: String?,
    /** Release notes as plain text (markdown stripped); empty when none. */
    val notes: String = "",
    /** Size of the APK asset in bytes, when GitHub reports it. */
    val apkSizeBytes: Long? = null,
)

/**
 * Update check against the repository's latest non-prerelease GitHub release.
 * Deliberately independent of the upstream Bluejay update feed, which Thor
 * builds must never contact.
 */
object GithubReleaseUpdate {
    const val LATEST_RELEASE_URL = "https://api.github.com/repos/Tufein/Twinjay/releases/latest"

    /** "v0.8.0-thor.1" / "0.7.3-thor.1" -> [0, 8, 0, 1]; non-numbers are ignored. */
    fun versionParts(version: String): List<Int> =
        Regex("\\d+").findAll(version.removePrefix("v")).mapNotNull { it.value.toIntOrNull() }.toList()

    /** True when [tag] is a strictly newer version than [current]. */
    fun isNewer(tag: String, current: String): Boolean {
        val a = versionParts(tag)
        val b = versionParts(current)
        if (a.isEmpty() || b.isEmpty()) return false
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    /**
     * Reads the fields we need from GitHub's release JSON. The release's own
     * `html_url` is the first one in the document; asset URLs end in `.apk`.
     */
    fun parse(json: String): ReleaseInfo? {
        val tag = Regex("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: return null
        val page = Regex("\"html_url\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
            ?: "https://github.com/Tufein/Twinjay/releases/tag/$tag"
        val apk = Regex("\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.apk)\"").findAll(json)
            .map { it.groupValues[1] }
            .let { all -> all.firstOrNull { it.contains("arm64") } ?: all.firstOrNull() }
        val body = Regex("\"body\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(json)?.groupValues?.get(1)
        val notes = body?.let { plainNotes(unescapeJson(it)) } ?: ""
        val size = apk?.let { url ->
            // The asset object holds "size" before its "browser_download_url".
            val idx = json.indexOf(url)
            if (idx < 0) null
            else Regex("\"size\"\\s*:\\s*(\\d+)").findAll(json.substring(0, idx)).lastOrNull()
                ?.groupValues?.get(1)?.toLongOrNull()
        }
        return ReleaseInfo(tag, page, apk, notes, size)
    }

    private fun unescapeJson(s: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (val n = s[i + 1]) {
                    'n' -> out.append('\n')
                    'r' -> {}
                    't' -> out.append(' ')
                    'u' -> if (i + 5 < s.length) {
                        s.substring(i + 2, i + 6).toIntOrNull(16)?.let { out.append(it.toChar()) }
                        i += 4
                    }
                    else -> out.append(n)
                }
                i += 2
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }

    /**
     * Turns GitHub release markdown into short plain text for a dialog:
     * headings and emphasis lose their markers, links keep their label,
     * bullets become "•", and the auto-generated "Full Changelog" line goes.
     */
    fun plainNotes(markdown: String, maxLines: Int = 14): String {
        val lines = markdown.lines()
            .map { line ->
                var l = line.trim()
                l = l.replace(Regex("^#{1,6}\\s*"), "")
                l = l.replace(Regex("^[-*]\\s+"), "• ")
                l = l.replace(Regex("\\[([^\\]]+)]\\([^)]*\\)"), "$1")
                l = l.replace("**", "").replace("__", "").replace("`", "")
                l
            }
            .filterNot { it.startsWith("Full Changelog", ignoreCase = true) }
        // Collapse runs of blank lines and trim the ends.
        val collapsed = mutableListOf<String>()
        for (l in lines) {
            if (l.isEmpty() && (collapsed.isEmpty() || collapsed.last().isEmpty())) continue
            collapsed += l
        }
        while (collapsed.isNotEmpty() && collapsed.last().isEmpty()) collapsed.removeAt(collapsed.size - 1)
        return if (collapsed.size > maxLines) collapsed.take(maxLines).joinToString("\n") + "\n…"
        else collapsed.joinToString("\n")
    }

    /** Blocking; call off the main thread. Returns null when offline or unparseable. */
    fun fetchLatest(): ReleaseInfo? {
        val conn = URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection
        return try {
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "TwinJay")
            if (conn.responseCode != 200) null
            else parse(conn.inputStream.bufferedReader().use { it.readText() })
        } finally {
            conn.disconnect()
        }
    }
}

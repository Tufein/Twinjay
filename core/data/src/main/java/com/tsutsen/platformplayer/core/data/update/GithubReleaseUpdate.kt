package com.tsutsen.platformplayer.core.data.update

import com.tsutsen.platformplayer.core.data.util.MiniJson
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
    /** The release's SHA256SUMS.txt asset, when it has one. */
    val checksumsUrl: String? = null,
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

    private const val TRUSTED_PREFIX = "https://github.com/Tufein/Twinjay/"

    /**
     * Only release assets and pages of this repository over https are trusted. The download itself
     * may redirect to GitHub's CDN; the final file is still checked against SHA256SUMS.txt.
     */
    fun isTrustedUrl(url: String?): Boolean =
        url != null && url.startsWith(TRUSTED_PREFIX) && !url.contains("..") && !url.contains('\\')

    /**
     * Reads the fields we need from GitHub's release JSON with a real JSON parser. Only trusted
     * URLs (see [isTrustedUrl]) are kept; an untrusted APK URL is dropped, so the app falls back
     * to opening the release page.
     */
    fun parse(json: String): ReleaseInfo? {
        val root = MiniJson.asObject(MiniJson.parse(json)) ?: return null
        val tag = (root["tag_name"] as? String)?.takeIf { it.isNotBlank() } ?: return null
        val page = (root["html_url"] as? String)?.takeIf { isTrustedUrl(it) }
            ?: "${TRUSTED_PREFIX}releases/tag/$tag"
        val assets = MiniJson.asArray(root["assets"]).orEmpty().mapNotNull { MiniJson.asObject(it) }
        val apks = assets.filter {
            (it["browser_download_url"] as? String)?.let { u -> u.endsWith(".apk") && isTrustedUrl(u) } == true
        }
        val apk = apks.firstOrNull { (it["browser_download_url"] as String).contains("arm64") } ?: apks.firstOrNull()
        val sums = assets.firstOrNull {
            (it["name"] as? String) == "SHA256SUMS.txt" && isTrustedUrl(it["browser_download_url"] as? String)
        }
        val notes = (root["body"] as? String)?.let { plainNotes(it) } ?: ""
        return ReleaseInfo(
            tag = tag,
            pageUrl = page,
            apkUrl = apk?.get("browser_download_url") as? String,
            notes = notes,
            apkSizeBytes = (apk?.get("size") as? Long)?.takeIf { it > 0 },
            checksumsUrl = sums?.get("browser_download_url") as? String,
        )
    }

    /** Finds the SHA-256 of [fileName] in the text of a `sha256sum` file; null when absent or malformed. */
    fun checksumFor(sumsText: String, fileName: String): String? =
        sumsText.lineSequence()
            .map { it.trim().split(Regex("\\s+"), limit = 2) }
            .firstOrNull { it.size == 2 && it[1].trimStart('*') == fileName }
            ?.get(0)?.lowercase()
            ?.takeIf { Regex("[0-9a-f]{64}").matches(it) }

    fun sha256Hex(bytes: java.io.InputStream): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val buf = ByteArray(64 * 1024)
        while (true) {
            val n = bytes.read(buf)
            if (n < 0) break
            md.update(buf, 0, n)
        }
        return md.digest().joinToString("") { "%02x".format(it) }
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

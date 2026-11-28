package com.tsutsen.platformplayer.core.data.util

import android.content.Context
import com.tsutsen.platformplayer.core.model.Author
import com.tsutsen.platformplayer.core.model.ContentItem
import com.tsutsen.platformplayer.core.model.ContentType
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Keeps the playback queue across app restarts. The text format is one item
 * per line, tab separated, every field URL-encoded (so tabs and newlines in a
 * title cannot break it). `~` stands for "no value": URLEncoder always
 * encodes a real tilde, so the two cannot clash. Only what the queue UI
 * needs is stored; the player fetches the rest again when the video starts.
 */
object QueueStore {
    private const val FILE = "playback_queue"
    private const val KEY = "items"
    private const val NULL = "~"
    private const val FIELDS = 11

    /** More than this is never stored; a runaway queue should not bloat preferences. */
    const val MAX_ITEMS = 200

    fun encode(items: List<ContentItem>): String =
        items.take(MAX_ITEMS).joinToString("\n") { item ->
            listOf(
                item.id,
                item.url,
                item.title,
                item.author?.id,
                item.author?.name,
                item.author?.url,
                item.author?.thumbnailUrl,
                item.thumbnailUrl,
                item.contentType.name,
                item.durationMs?.toString(),
                item.publishedAt?.toString(),
            ).joinToString("\t") { enc(it) }
        }

    /** Reads [text] back; damaged lines are skipped, never thrown on. */
    fun decode(text: String): List<ContentItem> =
        text.lineSequence().mapNotNull { line ->
            if (line.isBlank()) return@mapNotNull null
            val f = line.split('\t')
            if (f.size != FIELDS) return@mapNotNull null
            try {
                val url = dec(f[1]) ?: return@mapNotNull null
                val authorId = dec(f[3])
                ContentItem(
                    id = dec(f[0]) ?: url,
                    url = url,
                    title = dec(f[2]) ?: "",
                    author =
                        authorId?.let {
                            Author(id = it, name = dec(f[4]) ?: "", url = dec(f[5]), thumbnailUrl = dec(f[6]))
                        },
                    thumbnailUrl = dec(f[7]),
                    contentType = runCatching { ContentType.valueOf(dec(f[8]) ?: "") }.getOrDefault(ContentType.VIDEO),
                    publishedAt = dec(f[10])?.toLongOrNull(),
                    durationMs = dec(f[9])?.toLongOrNull(),
                )
            } catch (_: IllegalArgumentException) {
                null
            }
        }.toList()

    fun load(context: Context): List<ContentItem> =
        decode(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, "") ?: "")

    fun save(context: Context, items: List<ContentItem>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, encode(items)).apply()
    }

    private fun enc(value: String?): String = if (value == null) NULL else URLEncoder.encode(value, "UTF-8")

    private fun dec(value: String): String? = if (value == NULL) null else URLDecoder.decode(value, "UTF-8")
}

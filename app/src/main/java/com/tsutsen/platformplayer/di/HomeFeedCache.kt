package com.tsutsen.platformplayer.di

import com.tsutsen.platformplayer.core.model.Card
import com.tsutsen.platformplayer.core.model.VideoCard
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Remembers the last home feed so the app shows videos immediately at start
 * instead of an empty screen while the sources load. Only plain video cards
 * are kept; the live feed replaces them as soon as it arrives.
 */
object HomeFeedCache {
    const val MAX_ITEMS = 40

    @Serializable
    internal data class CachedCard(
        val id: String,
        val title: String,
        val thumbnailUrl: String? = null,
        val author: String? = null,
        val authorUrl: String? = null,
        val durationMs: Long? = null,
        val viewCount: Long? = null,
        val publishedAt: Long? = null,
        val url: String,
        val isLive: Boolean = false,
        val sourceId: String? = null,
    )

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(cards: List<Card>): String =
        json.encodeToString(
            cards
                .filterIsInstance<VideoCard>()
                .filter { !it.isLive }
                .take(MAX_ITEMS)
                .map {
                    CachedCard(
                        it.id, it.title, it.thumbnailUrl, it.author, it.authorUrl, it.durationMs,
                        it.viewCount, it.publishedAt, it.url, it.isLive, it.sourceId,
                    )
                },
        )

    fun decode(text: String): List<Card> =
        try {
            json.decodeFromString<List<CachedCard>>(text).map {
                VideoCard(
                    id = it.id,
                    title = it.title,
                    thumbnailUrl = it.thumbnailUrl,
                    author = it.author,
                    authorUrl = it.authorUrl,
                    durationMs = it.durationMs,
                    viewCount = it.viewCount,
                    publishedAt = it.publishedAt,
                    url = it.url,
                    isLive = it.isLive,
                    sourceId = it.sourceId,
                )
            }
        } catch (e: Exception) {
            emptyList()
        }

    fun load(dir: File): List<Card> =
        try {
            val f = File(dir, FILE)
            if (f.exists()) decode(f.readText()) else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

    fun save(dir: File, cards: List<Card>) {
        try {
            if (cards.none { it is VideoCard }) return
            File(dir, FILE).writeText(encode(cards))
        } catch (e: Exception) {
            // Cache only: ignore.
        }
    }

    private const val FILE = "home_feed_cache.json"
}

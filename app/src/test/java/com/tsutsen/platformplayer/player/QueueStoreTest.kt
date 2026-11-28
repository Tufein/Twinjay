package com.tsutsen.platformplayer.player

import com.tsutsen.platformplayer.core.data.util.QueueStore
import com.tsutsen.platformplayer.core.model.Author
import com.tsutsen.platformplayer.core.model.ContentItem
import com.tsutsen.platformplayer.core.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueStoreTest {
    private fun item(
        url: String,
        title: String = "t",
        author: Author? = null,
    ) = ContentItem(
        id = "id-$url",
        url = url,
        title = title,
        author = author,
        thumbnailUrl = null,
        contentType = ContentType.VIDEO,
    )

    @Test
    fun roundTripKeepsTheFields() {
        val a =
            ContentItem(
                id = "1",
                url = "https://example.com/watch?v=1",
                title = "Tabs\tand\nnewlines ~ and ünïcode",
                author = Author("a1", "Chan nel", "https://example.com/c", "https://example.com/c.png"),
                thumbnailUrl = "https://example.com/t.png",
                contentType = ContentType.LIVE,
                publishedAt = 1_700_000_000L,
                durationMs = 61_000L,
            )
        val back = QueueStore.decode(QueueStore.encode(listOf(a, item("https://example.com/2"))))
        assertEquals(2, back.size)
        assertEquals(a, back[0])
        assertNull(back[1].author)
        assertNull(back[1].durationMs)
    }

    @Test
    fun emptyQueueRoundTrips() {
        assertTrue(QueueStore.decode(QueueStore.encode(emptyList())).isEmpty())
    }

    @Test
    fun damagedLinesAreSkipped() {
        val good = QueueStore.encode(listOf(item("https://example.com/ok")))
        val back = QueueStore.decode("garbage\n\n$good\nonly\ttwo")
        assertEquals(listOf("https://example.com/ok"), back.map { it.url })
    }

    @Test
    fun unknownContentTypeFallsBackToVideo() {
        val line = QueueStore.encode(listOf(item("https://example.com/x"))).replace("VIDEO", "HOLOGRAM")
        assertEquals(ContentType.VIDEO, QueueStore.decode(line).single().contentType)
    }

    @Test
    fun neverStoresMoreThanTheLimit() {
        val many = (1..QueueStore.MAX_ITEMS + 25).map { item("https://example.com/$it") }
        assertEquals(QueueStore.MAX_ITEMS, QueueStore.decode(QueueStore.encode(many)).size)
    }
}

package com.tsutsen.platformplayer.player

import com.tsutsen.platformplayer.core.data.player.PlaybackQueueOrder
import com.tsutsen.platformplayer.core.model.ContentItem
import com.tsutsen.platformplayer.core.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackQueueOrderTest {
    private fun item(url: String) =
        ContentItem(
            id = url,
            url = url,
            title = url,
            author = null,
            thumbnailUrl = null,
            contentType = ContentType.VIDEO,
        )

    private fun urls(queue: List<ContentItem>) = queue.map { it.url }

    @Test
    fun insertsDirectlyAfterCurrent() {
        val queue = listOf(item("a"), item("b"), item("c"))
        assertEquals(
            listOf("a", "x", "b", "c"),
            urls(PlaybackQueueOrder.insertNext(queue, "a", item("x"))),
        )
    }

    @Test
    fun reordersQueuedItemInsteadOfDuplicating() {
        val queue = listOf(item("a"), item("b"), item("c"))
        assertEquals(
            listOf("a", "c", "b"),
            urls(PlaybackQueueOrder.insertNext(queue, "a", item("c"))),
        )
    }

    @Test
    fun goesFirstWithoutACurrentVideo() {
        val queue = listOf(item("b"), item("c"))
        assertEquals(
            listOf("x", "b", "c"),
            urls(PlaybackQueueOrder.insertNext(queue, null, item("x"))),
        )
    }

    @Test
    fun goesFirstWhenCurrentIsNotQueued() {
        val queue = listOf(item("b"), item("c"))
        assertEquals(
            listOf("x", "b", "c"),
            urls(PlaybackQueueOrder.insertNext(queue, "a", item("x"))),
        )
    }

    @Test
    fun keepsAnAlreadyNextItemInPlace() {
        val queue = listOf(item("a"), item("b"))
        assertEquals(
            listOf("a", "b"),
            urls(PlaybackQueueOrder.insertNext(queue, "a", item("b"))),
        )
    }
}

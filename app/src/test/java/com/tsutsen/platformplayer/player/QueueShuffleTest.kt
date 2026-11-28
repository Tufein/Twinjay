package com.tsutsen.platformplayer.player

import com.tsutsen.platformplayer.core.data.player.QueueShuffle
import com.tsutsen.platformplayer.core.data.player.SleepExtend
import com.tsutsen.platformplayer.core.model.ContentItem
import com.tsutsen.platformplayer.core.model.ContentType
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test

class QueueShuffleTest {
    private fun item(n: Int) =
        ContentItem("$n", "u$n", "t$n", null, null, ContentType.VIDEO)

    private val queue = (1..8).map { item(it) }

    @Test
    fun currentVideoStaysFirstAndNothingIsLost() {
        val out = QueueShuffle.shuffleUpcoming(queue, "u1", Random(7))
        assertEquals("u1", out.first().url)
        assertEquals(queue.map { it.url }.sorted(), out.map { it.url }.sorted())
    }

    @Test
    fun itemsBeforeAndIncludingCurrentKeepTheirPlace() {
        val out = QueueShuffle.shuffleUpcoming(queue, "u3", Random(3))
        assertEquals(listOf("u1", "u2", "u3"), out.take(3).map { it.url })
        assertEquals(setOf("u4", "u5", "u6", "u7", "u8"), out.drop(3).map { it.url }.toSet())
    }

    @Test
    fun withoutCurrentTheWholeQueueIsShuffled() {
        val out = QueueShuffle.shuffleUpcoming(queue, null, Random(11))
        assertEquals(queue.map { it.url }.sorted(), out.map { it.url }.sorted())
    }

    @Test
    fun emptyAndSingleQueuesAreUntouched() {
        assertEquals(emptyList<ContentItem>(), QueueShuffle.shuffleUpcoming(emptyList(), "u1"))
        assertEquals(listOf(item(1)), QueueShuffle.shuffleUpcoming(listOf(item(1)), "u1"))
    }

    @Test
    fun sleepExtendAddsToWhatIsLeftAndRoundsUp() {
        assertEquals(25, SleepExtend.totalMinutes(10 * 60_000L, 15))
        assertEquals(26, SleepExtend.totalMinutes(10 * 60_000L + 1L, 15))
        assertEquals(16, SleepExtend.totalMinutes(30_000L, 15))
    }

    @Test
    fun sleepExtendWithoutRunningTimerIsJustTheAmount() {
        assertEquals(15, SleepExtend.totalMinutes(0L, 15))
        assertEquals(15, SleepExtend.totalMinutes(-500L, 15))
        assertEquals(0, SleepExtend.totalMinutes(5 * 60_000L, 0))
    }
}

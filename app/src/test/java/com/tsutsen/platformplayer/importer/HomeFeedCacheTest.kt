package com.tsutsen.platformplayer.importer

import com.tsutsen.platformplayer.core.model.VideoCard
import com.tsutsen.platformplayer.di.HomeFeedCache
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFeedCacheTest {
    private fun card(i: Int, live: Boolean = false) =
        VideoCard(id = "id$i", title = "Title $i", thumbnailUrl = "https://t/$i", author = "A", url = "https://v/$i", isLive = live)

    @Test
    fun roundTripsVideoCardsAndDropsLive() {
        val decoded = HomeFeedCache.decode(HomeFeedCache.encode(listOf(card(1), card(2, live = true), card(3))))
        assertEquals(listOf("https://v/1", "https://v/3"), decoded.map { (it as VideoCard).url })
        assertEquals("Title 1", (decoded[0] as VideoCard).title)
    }

    @Test
    fun limitsSizeAndSurvivesGarbage() {
        assertEquals(HomeFeedCache.MAX_ITEMS, HomeFeedCache.decode(HomeFeedCache.encode((1..100).map { card(it) })).size)
        assertEquals(0, HomeFeedCache.decode("not json").size)
    }
}

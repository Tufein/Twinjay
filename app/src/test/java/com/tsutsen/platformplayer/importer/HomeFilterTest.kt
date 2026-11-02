package com.tsutsen.platformplayer.importer

import com.tsutsen.platformplayer.core.model.VideoCard
import com.tsutsen.platformplayer.di.HomeFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeFilterTest {
    @Test
    fun parsesWordsFromCommasAndLines() {
        assertEquals(listOf("shorts", "drama"), HomeFilter.parseWords(" Shorts, drama\n\nSHORTS "))
    }

    @Test
    fun matchesTitleOrChannelCaseInsensitively() {
        val words = listOf("shorts", "annoying")
        assertTrue(HomeFilter.shouldHide("Best SHORTS ever", "Someone", words))
        assertTrue(HomeFilter.shouldHide("Fine video", "The Annoying Channel", words))
        assertFalse(HomeFilter.shouldHide("Fine video", "Good channel", words))
        assertFalse(HomeFilter.shouldHide("Anything", null, emptyList()))
    }

    @Test
    fun filtersCards() {
        val keep = VideoCard(id = "1", title = "Keep me", thumbnailUrl = null, author = "A", url = "u1")
        val drop = VideoCard(id = "2", title = "Some shorts", thumbnailUrl = null, author = "A", url = "u2")
        assertEquals(listOf(keep), HomeFilter.apply(listOf(keep, drop), listOf("shorts")))
    }

    @Test
    fun hidesQuietChannels() {
        val quietCard = VideoCard(id = "3", title = "Hello", thumbnailUrl = null, author = "A", authorUrl = "chan/q", url = "u3")
        val other = VideoCard(id = "4", title = "Hello", thumbnailUrl = null, author = "B", authorUrl = "chan/o", url = "u4")
        assertEquals(listOf(other), HomeFilter.apply(listOf(quietCard, other), emptyList(), setOf("chan/q")))
    }

    @Test
    fun groupMatching() {
        val g = com.tsutsen.platformplayer.core.data.groups.ChannelGroups.Group("1", "News", setOf("chan/a"))
        val m = com.tsutsen.platformplayer.core.data.groups.ChannelGroups
        assertTrue(m.matches(g, "chan/a"))
        assertFalse(m.matches(g, "chan/b"))
        assertFalse(m.matches(g, null))
        assertTrue(m.matches(null, null))
    }

    @Test
    fun swapWindowExpires() {
        val swap = com.tsutsen.platformplayer.core.data.screens.ScreenSwap
        assertFalse(swap.isExpired(1_000L, 5_000L))
        assertTrue(swap.isExpired(1_000L, 1_000L + swap.WINDOW_MS + 1))
    }

    @Test
    fun networkPresetsDiffer() {
        val home = com.tsutsen.platformplayer.activities.NetworkPreset.forWifi(true).toMap()
        val away = com.tsutsen.platformplayer.activities.NetworkPreset.forWifi(false).toMap()
        assertEquals(false, home["dualScreenSleepWhilePlaying"])
        assertEquals(true, away["dualScreenSleepWhilePlaying"])
        assertEquals(10, away["dualScreenSleepSeconds"])
    }
}

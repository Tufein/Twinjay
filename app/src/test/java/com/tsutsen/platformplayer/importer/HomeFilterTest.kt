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
    fun downloadWindowWrapsMidnight() {
        val w = com.tsutsen.platformplayer.core.data.downloads.DownloadWindow
        assertTrue(w.isOpen(12, 5, 5))
        assertTrue(w.isOpen(3, 1, 7))
        assertFalse(w.isOpen(8, 1, 7))
        assertTrue(w.isOpen(23, 22, 6))
        assertTrue(w.isOpen(2, 22, 6))
        assertFalse(w.isOpen(12, 22, 6))
    }

    @Test
    fun storageLimitDropsOldestFirst() {
        val s = com.tsutsen.platformplayer.core.data.downloads.StorageLimit
        fun E(k: String, t: Long, z: Long) = com.tsutsen.platformplayer.core.data.downloads.StorageLimit.Entry(k, t, z)
        val e = listOf(E("b", 2, 40), E("a", 1, 40), E("c", 3, 40))
        assertEquals(listOf("a"), s.toDelete(e, 100))
        assertEquals(listOf("a", "b"), s.toDelete(e, 50))
        assertEquals(emptyList<String>(), s.toDelete(e, 0))
    }

    @Test
    fun dedupeKeepsFirstCopy() {
        val d = com.tsutsen.platformplayer.core.data.util.Dedupe
        val k = d.normalizeTitle("Big Talk!", "Ann")
        assertEquals(k, d.normalizeTitle("big talk", "ANN"))
        assertEquals(listOf("x2"), d.extras(listOf("x1", "x2", "y")) { if (it.startsWith("x")) "x" else it })
    }

    @Test
    fun networkPresetsDiffer() {
        val home = com.tsutsen.platformplayer.activities.NetworkPreset.forWifi(true).toMap()
        val away = com.tsutsen.platformplayer.activities.NetworkPreset.forWifi(false).toMap()
        assertEquals(false, home["dualScreenSleepWhilePlaying"])
        assertEquals(true, away["dualScreenSleepWhilePlaying"])
        assertEquals(10, away["dualScreenSleepSeconds"])
    }

    @Test
    fun playlistOrderSortsAndKeepsUnknownLast() {
        val o = com.tsutsen.platformplayer.core.data.util.PlaylistOrder
        val items = listOf(5L, null, 1L, 9L)
        assertEquals(listOf(1L, 5L, 9L, null), o.apply(items, o.OLDEST, 0) { it })
        assertEquals(listOf(9L, 5L, 1L, null), o.apply(items, o.NEWEST, 0) { it })
        assertEquals(items, o.apply(items, o.DEFAULT, 0) { it })
        assertEquals(o.apply(items, o.SHUFFLE, 7) { it }, o.apply(items, o.SHUFFLE, 7) { it })
    }

    @Test
    fun tagsParseCleanly() {
        val t = com.tsutsen.platformplayer.core.data.util.VideoTags
        assertEquals(listOf("funny", "cooking"), t.parse(" Funny, #cooking ,,funny"))
    }

    @Test
    fun savedSearchToggles() {
        val s = com.tsutsen.platformplayer.core.data.util.SavedSearches
        val a = s.toggle(emptyList(), " lofi ")
        assertEquals(listOf("lofi"), a)
        assertEquals(emptyList<String>(), s.toggle(a, "LOFI"))
    }

    @Test
    fun downloadQualityCycles() {
        val q = com.tsutsen.platformplayer.core.data.util.ChannelDownloadQuality
        assertEquals("Best", q.next(null))
        assertEquals("720p", q.next("1080p"))
        assertEquals(null, q.next("144p"))
    }

    @Test
    fun bookmarksRoundTripAndSort() {
        val b = com.tsutsen.platformplayer.core.data.util.VideoBookmarks
        val text = b.encode(listOf(b.Bookmark(90_000, "later"), b.Bookmark(5_000, "start | here")))
        val back = b.decode(text)
        assertEquals(listOf(5_000L, 90_000L), back.map { it.positionMs })
        assertEquals("start | here", back.first().note)
    }

    @Test
    fun smartListRulesRoundTrip() {
        val l = com.tsutsen.platformplayer.core.data.util.SmartLists
        val rules = listOf(l.Rule("cooking", 20, ""), l.Rule("", 0, "lofi"))
        assertEquals(rules, l.decode(l.encode(rules)))
        assertEquals("#cooking 20 min", rules[0].label)
    }

    @Test
    fun subtitleSearchNeedsThreeLetters() {
        val i = com.tsutsen.platformplayer.core.data.util.SubtitleIndex
        assertTrue(i.matches("Hello brave new world", "BRAVE"))
        assertFalse(i.matches("Hello brave new world", "he"))
        assertFalse(i.matches(null, "hello"))
    }

    @Test
    fun trendsAndSuggestionsFromHistory() {
        val now = java.time.LocalDate.of(2026, 11, 5)
        fun ms(daysAgo: Long) =
            now.minusDays(daysAgo).atStartOfDay(java.time.ZoneId.systemDefault()).plusHours(12).toInstant().toEpochMilli()
        fun h(url: String, author: String, daysAgo: Long, watched: Long) =
            com.tsutsen.platformplayer.core.database.entity.HistoryEntity(
                contentUrl = url, title = url, author = author, thumbnailUrl = null,
                watchedMs = watched, watchedAt = ms(daysAgo), authorUrl = "https://c/$author",
            )
        val history =
            listOf(
                h("1", "Ann", 1, 600_000), h("2", "Ann", 2, 600_000), h("3", "Ann", 3, 600_000),
                h("4", "Bob", 9, 900_000),
            )
        val stats =
            com.tsutsen.platformplayer.stats.WatchStatsBuilder.build(
                history, now, channelAvatars = mapOf("https://c/Bob" to null),
            )
        val ann = stats.trends.first { it.author == "Ann" }
        assertEquals(1_800_000L, ann.thisWeekMs)
        assertEquals(0L, ann.previousWeekMs)
        assertEquals(900_000L, stats.trends.first { it.author == "Bob" }.previousWeekMs)
        assertEquals(listOf("Ann"), stats.suggestions.map { it.author })
    }
}

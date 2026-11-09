package com.tsutsen.platformplayer.importer

import com.tsutsen.platformplayer.core.data.util.EverythingSearch
import com.tsutsen.platformplayer.core.data.util.EverythingSearch.Item
import org.junit.Assert.assertEquals
import org.junit.Test

class EverythingSearchTest {
    private val items =
        listOf(
            Item("History", "Kotlin coroutines explained", "Dev Channel", "u1"),
            Item("History", "Kotlin coroutines explained", "Dev Channel", "u1"),
            Item("Download", "Cooking pasta", "Chef", "u2", extraText = "add the kotlin... no, the salt"),
            Item("Bookmark", "Travel vlog", "nice bridge", "u3"),
        )

    @Test
    fun matchesAllWordsAcrossFieldsOncePerKind() {
        assertEquals(listOf("u1"), EverythingSearch.search(items, "coroutines dev").map { it.url })
        assertEquals(listOf("u1", "u2"), EverythingSearch.search(items, "KOTLIN").map { it.url })
        assertEquals(listOf("u3"), EverythingSearch.search(items, "bridge").map { it.url })
    }

    @Test
    fun shortOrEmptyQueryFindsNothing() {
        assertEquals(0, EverythingSearch.search(items, "k").size)
        assertEquals(0, EverythingSearch.search(items, "  ").size)
    }
}

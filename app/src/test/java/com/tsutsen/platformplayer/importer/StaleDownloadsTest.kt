package com.tsutsen.platformplayer.importer

import com.tsutsen.platformplayer.core.data.downloads.StorageLimit
import org.junit.Assert.assertEquals
import org.junit.Test

class StaleDownloadsTest {
    private val day = 86_400L

    @Test
    fun picksOnlyOldKnownEntriesOldestFirst() {
        val now = 100 * day
        val entries =
            listOf(
                StorageLimit.Entry("new", now - 2 * day, 10),
                StorageLimit.Entry("older", now - 60 * day, 10),
                StorageLimit.Entry("unknown", 0, 10),
                StorageLimit.Entry("oldest", now - 90 * day, 10),
            )
        assertEquals(listOf("oldest", "older"), StorageLimit.staleEntries(entries, now, 30).map { it.key })
    }

    @Test
    fun zeroDaysSuggestsNothing() {
        assertEquals(emptyList<StorageLimit.Entry>(), StorageLimit.staleEntries(listOf(StorageLimit.Entry("a", 5, 1)), 1000, 0))
    }
}

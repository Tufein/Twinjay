package com.tsutsen.platformplayer.twinjay

import com.tsutsen.platformplayer.core.data.downloads.DownloadWindow
import com.tsutsen.platformplayer.core.data.downloads.StorageLimit
import com.tsutsen.platformplayer.core.data.util.Dedupe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRulesTest {
    @Test fun storageLimitDeletesOldestFirstUntilItFits() {
        val e = listOf(
            StorageLimit.Entry("new", 300, 40),
            StorageLimit.Entry("old", 100, 40),
            StorageLimit.Entry("mid", 200, 40),
        )
        assertEquals(listOf("old"), StorageLimit.toDelete(e, 100))
        assertEquals(listOf("old", "mid"), StorageLimit.toDelete(e, 50))
        assertEquals(emptyList<String>(), StorageLimit.toDelete(e, 200))
        assertEquals(emptyList<String>(), StorageLimit.toDelete(e, 0))
    }

    @Test fun staleEntriesSkipUnknownTimes() {
        val day = 86_400L
        val e = listOf(
            StorageLimit.Entry("unknown", 0, 1),
            StorageLimit.Entry("ancient", 10, 1),
            StorageLimit.Entry("fresh", 100 * day - 5, 1),
        )
        assertEquals(listOf("ancient"), StorageLimit.staleEntries(e, 100 * day, 30).map { it.key })
        assertTrue(StorageLimit.staleEntries(e, 100 * day, 0).isEmpty())
    }

    @Test fun downloadWindowWrapsMidnight() {
        assertTrue(DownloadWindow.isOpen(3, 5, 5))
        assertTrue(DownloadWindow.isOpen(9, 8, 17))
        assertFalse(DownloadWindow.isOpen(17, 8, 17))
        assertTrue(DownloadWindow.isOpen(23, 22, 6))
        assertTrue(DownloadWindow.isOpen(2, 22, 6))
        assertFalse(DownloadWindow.isOpen(12, 22, 6))
    }

    @Test fun dedupeKeepsTheFirstCopy() {
        assertEquals(listOf("a2", "b2"), Dedupe.extras(listOf("a1", "b1", "a2", "b2", "c")) { it.take(1) }.take(2))
        assertEquals(listOf(3), Dedupe.extras(listOf(1, 2, 3)) { (it % 2).toString() })
        assertEquals(
            Dedupe.normalizeTitle("Hello, World!", "The Author"),
            Dedupe.normalizeTitle("hello world", "the author"),
        )
    }
}

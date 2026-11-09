package com.tsutsen.platformplayer.importer

import com.tsutsen.platformplayer.core.data.log.SourceErrors
import org.junit.Assert.assertEquals
import org.junit.Test

class SourceErrorsTest {
    @Test
    fun keepsLastErrorPerSourceAndCounts() {
        SourceErrors.clear()
        SourceErrors.record("YouTube", "getHome", IllegalStateException("first"), nowMs = 1_000)
        SourceErrors.record("YouTube", "search", IllegalStateException("second\nstack"), nowMs = 2_000)
        SourceErrors.record("Other", "getHome", RuntimeException(), nowMs = 1_500)
        val all = SourceErrors.all()
        assertEquals(listOf("YouTube", "Other"), all.map { it.source })
        assertEquals("second", all[0].message)
        assertEquals(2, all[0].count)
        assertEquals("RuntimeException", all[1].message)
        SourceErrors.clear()
    }

    @Test
    fun describesAge() {
        val e = SourceErrors.Entry("YouTube", "getHome", "boom", 0, 3)
        assertEquals("getHome failed just now (3x): boom", SourceErrors.describe(e, 10_000))
        assertEquals("getHome failed 5 min ago (3x): boom", SourceErrors.describe(e, 300_000))
        assertEquals("getHome failed 2 h ago (3x): boom", SourceErrors.describe(e, 7_200_000))
    }
}

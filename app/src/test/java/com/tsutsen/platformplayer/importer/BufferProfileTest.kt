package com.tsutsen.platformplayer.importer

import com.tsutsen.platformplayer.core.data.player.BufferProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BufferProfileTest {
    @Test
    fun wifiKeepsTheOldValues() {
        assertEquals(BufferProfile(30_000, 60_000, 1_500, 5_000), BufferProfile.forWifi(true))
    }

    @Test
    fun mobileWaitsLongerAndStaysValid() {
        val m = BufferProfile.forWifi(false)
        assertTrue(m.resumeMs > BufferProfile.WIFI.resumeMs)
        // media3 requires min >= start and min >= resume, and max >= min.
        assertTrue(m.minMs >= m.startMs && m.minMs >= m.resumeMs && m.maxMs >= m.minMs)
    }
}

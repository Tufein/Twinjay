package com.tsutsen.platformplayer.power

import com.tsutsen.platformplayer.core.data.power.BatterySaver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatterySaverTest {
    @Test
    fun lowOnlyBelowTwentyPercentAndNotCharging() {
        assertTrue(BatterySaver.isLow(19, 100, charging = false))
        assertFalse(BatterySaver.isLow(20, 100, charging = false))
        assertFalse(BatterySaver.isLow(5, 100, charging = true))
    }

    @Test
    fun unknownReadingsAreNeverLow() {
        assertFalse(BatterySaver.isLow(-1, 100, charging = false))
        assertFalse(BatterySaver.isLow(10, 0, charging = false))
    }
}

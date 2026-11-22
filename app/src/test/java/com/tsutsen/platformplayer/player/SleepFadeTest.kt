package com.tsutsen.platformplayer.player

import com.tsutsen.platformplayer.core.data.player.SleepFade
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepFadeTest {
    @Test
    fun fullVolumeBeforeTheFade() {
        assertEquals(1f, SleepFade.volumeAt(-1L, 20_000L), 0f)
        assertEquals(1f, SleepFade.volumeAt(0L, 20_000L), 0f)
    }

    @Test
    fun halfwayThroughIsHalfVolume() {
        assertEquals(0.5f, SleepFade.volumeAt(10_000L, 20_000L), 0.0001f)
    }

    @Test
    fun silentAtAndAfterTheEnd() {
        assertEquals(0f, SleepFade.volumeAt(20_000L, 20_000L), 0f)
        assertEquals(0f, SleepFade.volumeAt(25_000L, 20_000L), 0f)
    }

    @Test
    fun zeroDurationIsSilent() {
        assertEquals(0f, SleepFade.volumeAt(0L, 0L), 0f)
    }
}

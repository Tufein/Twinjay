package com.tsutsen.platformplayer.feature.player.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsFormatTest {
    @Test
    fun formatsBitrate() {
        assertEquals("?", StatsFormat.bitrate(0))
        assertEquals("640 kbps", StatsFormat.bitrate(640_000))
        assertEquals("4.5 Mbps", StatsFormat.bitrate(4_500_000))
    }

    @Test
    fun formatsResolutionAndCodec() {
        assertEquals("1920x1080@30", StatsFormat.resolution(1920, 1080, 29.97f))
        assertEquals("?", StatsFormat.resolution(0, 0, 0f))
        assertEquals("MP4 avc1.640028", StatsFormat.codec("video/mp4", "avc1.640028"))
        assertEquals("?", StatsFormat.codec(null, null))
    }

    @Test
    fun formatsBufferSeconds() {
        assertEquals("12.5 s", StatsFormat.seconds(12_500))
        assertEquals("0.0 s", StatsFormat.seconds(-3))
    }

    @Test
    fun formatsDroppedFrames() {
        assertEquals("0", StatsFormat.dropped(0, 500))
        assertEquals("10 (2.0%)", StatsFormat.dropped(10, 490))
    }
}

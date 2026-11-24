package com.tsutsen.platformplayer.twinjay

import com.tsutsen.platformplayer.activities.NetworkPreset
import com.tsutsen.platformplayer.di.DeviceInfo
import com.tsutsen.platformplayer.di.TwinjayExtras
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceAndPresetTest {
    @Test fun formatsBytes() {
        assertEquals("0 B", DeviceInfo.formatBytes(-5))
        assertEquals("1023 B", DeviceInfo.formatBytes(1023))
        assertEquals("1.5 KB", DeviceInfo.formatBytes(1536))
        assertEquals("5.0 GB", DeviceInfo.formatBytes(5L * 1024 * 1024 * 1024))
    }

    @Test fun groupsFingerprintBytes() {
        assertEquals("AB:01:FF", DeviceInfo.colonHex(byteArrayOf(0xAB.toByte(), 1, 0xFF.toByte())))
    }

    @Test fun networkPresetsDifferForWifiAndMobile() {
        val wifi = NetworkPreset.forWifi(true).toMap()
        val mobile = NetworkPreset.forWifi(false).toMap()
        assertEquals(false, wifi["dualScreenSleepWhilePlaying"])
        assertEquals(true, mobile["dualScreenSleepWhilePlaying"])
        assertEquals("black", mobile["dualScreenLook"])
        assertTrue(wifi.keys.all { it in mobile.keys } || mobile.keys.containsAll(wifi.keys))
    }

    @Test fun extrasLinesKeepSeparatorCharactersInValues() {
        val line = TwinjayExtras.line("video_tags", "s", "k", "a\u0002b")
        val p = line.split("\u0002", limit = 4)
        assertEquals(4, p.size)
        assertEquals("a\u0002b", p[3])
    }
}

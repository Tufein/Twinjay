package com.tsutsen.platformplayer.importer

import com.tsutsen.platformplayer.states.PluginRollback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginRollbackRulesTest {
    @Test
    fun keepsOnlyOnAVersionChange() {
        assertTrue(PluginRollback.shouldKeep(12, 13))
        assertFalse(PluginRollback.shouldKeep(13, 13))
    }

    @Test
    fun idsBecomeSafeFileNames() {
        assertEquals("35ae969a-a7db_x", PluginRollback.safeName("35ae969a-a7db_x"))
        assertEquals("___etc_passwd", PluginRollback.safeName("../etc/passwd"))
    }
}

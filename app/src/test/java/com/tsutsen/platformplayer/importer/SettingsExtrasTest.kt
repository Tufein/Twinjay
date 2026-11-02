package com.tsutsen.platformplayer.importer

import com.tsutsen.platformplayer.feature.settings.impl.SettingsIndex
import com.tsutsen.platformplayer.feature.settings.impl.WhatsNew
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsExtrasTest {
    @Test
    fun searchFindsByTitleAndKeywordButNotForShortQueries() {
        assertEquals("playback", SettingsIndex.search("sponsor").first().section)
        assertEquals("dual", SettingsIndex.search("gaming").first().section)
        assertEquals(0, SettingsIndex.search("a").size)
        assertEquals(0, SettingsIndex.search("zzzz").size)
    }

    @Test
    fun whatsNewIsOnlyShownAfterAnUpdate() {
        assertNull(WhatsNew.pending(null, "0.10.0-thor.1"))
        assertNull(WhatsNew.pending("0.10.0-thor.1", "0.10.0-thor.1"))
        assertNotNull(WhatsNew.pending("0.9.0-thor.1", "0.10.0-thor.1"))
        assertNull(WhatsNew.pending("0.9.0-thor.1", "0.99.0"))
    }
}

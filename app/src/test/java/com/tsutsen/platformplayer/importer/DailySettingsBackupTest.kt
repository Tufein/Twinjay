package com.tsutsen.platformplayer.importer

import com.tsutsen.platformplayer.di.DailySettingsBackup
import org.junit.Assert.assertEquals
import org.junit.Test

class DailySettingsBackupTest {
    @Test
    fun keepsTheNewestAndIgnoresOtherFiles() {
        val names = (1..9).map { DailySettingsBackup.fileName("2026-11-0$it") } + "notes.txt"
        assertEquals(
            listOf("twinjay-settings-2026-11-02.txt", "twinjay-settings-2026-11-01.txt"),
            DailySettingsBackup.toDelete(names),
        )
        assertEquals(emptyList<String>(), DailySettingsBackup.toDelete(names.take(3)))
    }
}

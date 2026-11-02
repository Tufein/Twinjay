package com.tsutsen.platformplayer.feature.settings.impl

/** A small index so the settings search can jump to the right section. */
object SettingsIndex {
    data class Entry(val title: String, val section: String, val keywords: String = "") {
        val sectionName: String
            get() =
                when (section) {
                    "appearance" -> "Appearance"
                    "content" -> "Content"
                    "playback" -> "Playback"
                    "gestures" -> "Gestures"
                    "controller" -> "Controller"
                    "dual" -> "Dual screen"
                    "backup" -> "Backup & restore"
                    else -> "About"
                }
    }

    val entries: List<Entry> =
        listOf(
            Entry("Theme and colors", "appearance", "dark light mode dynamic color pure black text size"),
            Entry("Plugins and sources", "content", "youtube plugin source login"),
            Entry("Default video quality", "playback", "resolution 1080p 720p"),
            Entry("SponsorBlock", "playback", "skip sponsor intro outro"),
            Entry("Battery saver", "playback", "low battery 720p dim"),
            Entry("Auto-download", "playback", "download wifi charging cleanup delete old"),
            Entry("Hide videos with words", "playback", "filter block keyword channel"),
            Entry("Subtitles", "playback", "captions language size"),
            Entry("Gestures", "gestures", "swipe double tap hold"),
            Entry("Controller buttons", "controller", "gamepad bind fullscreen stats seek"),
            Entry("Second screen pages", "dual", "dual screen companion"),
            Entry("Presets: at home, on the go, gaming", "dual", "second screen dim preset"),
            Entry("Dim second screen", "dual", "sleep black brightness clock"),
            Entry("Backup and restore", "backup", "export import"),
            Entry("Check for updates", "about", "version release github"),
            Entry("Share app log", "about", "log bug problem"),
        )

    fun search(query: String): List<Entry> {
        val q = query.trim().lowercase()
        if (q.length < 2) return emptyList()
        return entries.filter { it.title.lowercase().contains(q) || it.keywords.contains(q) }.take(6)
    }
}

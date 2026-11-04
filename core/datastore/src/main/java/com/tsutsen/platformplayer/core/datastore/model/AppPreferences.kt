package com.tsutsen.platformplayer.core.datastore.model

data class AppPreferences(
    val appearance: AppearancePreferences = AppearancePreferences(),
    val playback: PlaybackPreferences = PlaybackPreferences(),
    val subtitle: SubtitlePreferences = SubtitlePreferences(),
    val defaultPlaybackSpeed: Float = 1f,
    val defaultSpeedup: Float = 2f,
    val speedupSensitivity: Float = 1f,
    /** Jump step (seconds) for the back/forward seek gestures. */
    val jumpStepSeconds: Int = 5,
    val playerGestures: PlayerGesturePreferences = PlayerGesturePreferences(),
    val controller: ControllerPreferences = ControllerPreferences(),
    val defaultVideoResolution: String = "1080p",
    val defaultDownloadResolution: String = "1080p",
    val enableDeveloperOptions: Boolean = false,
    val librarySectionOrder: List<String> =
        listOf("watch_later", "liked", "disliked", "favourite", "history", "downloads", "playlists"),
    val librarySectionsEnabled: List<String> =
        listOf("watch_later", "liked", "disliked", "favourite", "history", "downloads", "playlists"),
    val dualScreen: Boolean = false,
    // Dash first: the companion lands on the dash page and flips to the
    // video page when a video opens.
    val dualScreenPages: List<String> = listOf("dash", "video", "library", "home"),
    val dualScreenVideoTabs: List<String> =
        listOf("info", "controls", "comments", "chapters", "recommended", "queue", "dot"),
    /** Display order of the enabled video-page tabs (Settings > Dual screen). */
    val dualScreenVideoTabOrder: List<String> =
        listOf("info", "controls", "comments", "chapters", "recommended", "queue", "dot"),
    /** Order of the elements on the second screen's video page. */
    val dualScreenPageOrder: List<String> = listOf("controls", "video", "tabs"),
    /** Dash page: "Top creators" scope — "week" (this week) or "overall". */
    val dualScreenTopCreatorsScope: String = "week",
    /** Dash page: display order of the widgets. */
    val dualScreenDashPageOrder: List<String> =
        listOf("stats", "top_creators", "continue"),
    val dualScreenFeedSources: List<String> = emptyList(),
    /** Black out the second screen while a video plays and it is not touched. */
    val dualScreenSleepWhilePlaying: Boolean = false,
    /** Seconds without touch before the second screen dims. */
    val dualScreenSleepSeconds: Int = 20,
    /** 0 = black with the backlight off, otherwise the dimmed backlight in percent. */
    val dualScreenSleepLevel: Int = 0,
    /** Second screen look: "same" as the main screen, "dark" or "black" (OLED). */
    val dualScreenLook: String = "same",
    /** Skip sponsor / intro / outro segments marked on sponsor.ajay.app. */
    val sponsorBlock: Boolean = false,
    /** SponsorBlock categories to skip, comma separated. */
    val sponsorCategories: String = "sponsor,selfpromo,interaction,intro,outro",
    /** Download new videos of notified channels on Wi-Fi in the background. */
    val autoDownload: Boolean = false,
    /** Comma separated words: home videos whose title or channel contain one are hidden. */
    val hiddenWords: String = "",
    /** Only auto-download while charging. */
    val downloadOnlyCharging: Boolean = false,
    /** No network feed loading or background work; only saved content. */
    val offlineMode: Boolean = false,
    val autoPresetByNetwork: Boolean = false,
    val bigSleepClock: Boolean = false,
    /** Delete downloads older than this many days (0 = never). */
    val downloadCleanupDays: Int = 0,
    val gestureSensitivity: Float = 1f,
    val resumeRewindSeconds: Int = 5,
    val skipSilence: Boolean = false,
    val batterySaverPercent: Int = 20,
    val storageLimitGb: Int = 0,
    val downloadFromHour: Int = 0,
    val downloadToHour: Int = 0,
    val downloadShortestFirst: Boolean = false,
    /** On low battery: dim the second screen fast and cap video at 720p. */
    val batterySaver: Boolean = false,
    val dualScreenLibrarySlots: List<String> =
        listOf("watch_later", "liked", "favourite", "history"),
    val gridColumns: Int = 3,
    val homeHiddenSources: List<String> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val showRecommendedVideos: Boolean = true,
    val showComments: Boolean = true,
    val autoUpdatePlugins: Boolean = true,
    val gettingStartedCompleted: Boolean = false,
    /** Source ids whose home-feed login notice was declined. */
    val homeLoginPromptsDismissed: List<String> = emptyList(),
)

/**
 * Subtitle appearance for the in-player caption overlay.
 *  - font: "default" | "sans" | "serif" | "mono"
 *  - size: font size in pt
 *  - bottomPadding: gap below the video's bottom edge, in dp
 */
data class SubtitlePreferences(
    val font: String = "default",
    val size: Int = 16,
    val bottomPadding: Int = 20,
    val outline: Int = 3,
)

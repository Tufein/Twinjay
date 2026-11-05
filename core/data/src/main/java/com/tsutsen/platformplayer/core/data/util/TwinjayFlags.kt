package com.tsutsen.platformplayer.core.data.util

/** A few settings the data layer needs without depending on the app's Settings class. */
object TwinjayFlags {
    /** Count down before the next queued video starts. */
    @Volatile var upNext: Boolean = false

    /** Even out the volume of loud and quiet videos. */
    @Volatile var normalizeVolume: Boolean = false
}

package com.tsutsen.platformplayer.core.data.downloads

/** Pure helper for "only download between these hours". Equal hours mean no limit; the window may wrap midnight. */
object DownloadWindow {
    fun isOpen(hour: Int, from: Int, to: Int): Boolean =
        when {
            from == to -> true
            from < to -> hour in from until to
            else -> hour >= from || hour < to
        }
}

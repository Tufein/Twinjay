package com.tsutsen.platformplayer.core.data.screens

/** Hook set by the main activity so Settings can ask for the two screens to be swapped. */
object ScreenSwap {
    @Volatile var action: (() -> Unit)? = null

    /** Pure rule: has a pending swap run out of time? */
    fun isExpired(startedAtMs: Long, nowMs: Long, windowMs: Long = WINDOW_MS): Boolean = nowMs - startedAtMs > windowMs

    const val WINDOW_MS = 15_000L
    const val CONFIRM_MS = 10_000L
}

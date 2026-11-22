package com.tsutsen.platformplayer.core.data.player

/**
 * Volume curve for the sleep timer. Instead of cutting the sound off at the
 * chosen time, the player fades linearly to silence over the last
 * [DURATION_MS], which is much less jarring at night.
 */
object SleepFade {
    /** Length of the fade-out in milliseconds. */
    const val DURATION_MS = 20_000L

    /** How often the player volume is updated during the fade. */
    const val STEP_MS = 500L

    /**
     * Volume for [elapsedMs] into a fade of [durationMs]: 1.0 before the
     * fade starts, 0.0 at the end (and after it).
     */
    fun volumeAt(elapsedMs: Long, durationMs: Long = DURATION_MS): Float {
        if (durationMs <= 0L) return 0f
        if (elapsedMs <= 0L) return 1f
        if (elapsedMs >= durationMs) return 0f
        return 1f - elapsedMs.toFloat() / durationMs.toFloat()
    }
}

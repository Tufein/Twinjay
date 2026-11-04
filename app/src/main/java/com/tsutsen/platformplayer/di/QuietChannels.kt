package com.tsutsen.platformplayer.di

import android.content.Context

/**
 * Channels the user muted in the home feed: they stay subscribed, but vanish from Home.
 * A channel can also be paused until a date, after which it comes back by itself.
 */
object QuietChannels {
    private const val FILE = "quiet_channels"
    private const val KEY = "urls"
    private const val PAUSED_FILE = "paused_channels"

    /** Hidden channels: the permanent ones plus those paused right now. */
    fun get(context: Context, now: Long = System.currentTimeMillis()): Set<String> =
        permanent(context) + pausedUntil(context).filterValues { it > now }.keys

    fun permanent(context: Context): Set<String> =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getStringSet(KEY, emptySet())?.toSet().orEmpty()

    fun toggle(context: Context, url: String): Boolean {
        val now = permanent(context).toMutableSet()
        val added = now.add(url)
        if (!added) now.remove(url)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putStringSet(KEY, now).apply()
        return added
    }

    /** Url to the time (ms) the pause ends. */
    fun pausedUntil(context: Context): Map<String, Long> =
        context.getSharedPreferences(PAUSED_FILE, Context.MODE_PRIVATE).all
            .mapNotNull { (k, v) -> (v as? Long)?.let { k to it } }
            .toMap()

    fun pauseDays(context: Context, url: String, days: Int, now: Long = System.currentTimeMillis()) {
        context.getSharedPreferences(PAUSED_FILE, Context.MODE_PRIVATE).edit()
            .putLong(url, now + days * 86_400_000L).apply()
    }

    fun resume(context: Context, url: String) {
        context.getSharedPreferences(PAUSED_FILE, Context.MODE_PRIVATE).edit().remove(url).apply()
    }
}

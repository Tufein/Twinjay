package com.tsutsen.platformplayer.di

import android.content.Context

/** Channels the user muted in the home feed: they stay subscribed, but vanish from Home. */
object QuietChannels {
    private const val FILE = "quiet_channels"
    private const val KEY = "urls"

    fun get(context: Context): Set<String> =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getStringSet(KEY, emptySet())?.toSet().orEmpty()

    fun toggle(context: Context, url: String): Boolean {
        val now = get(context).toMutableSet()
        val added = now.add(url)
        if (!added) now.remove(url)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putStringSet(KEY, now).apply()
        return added
    }
}

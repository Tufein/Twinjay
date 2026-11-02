package com.tsutsen.platformplayer.core.data.repository.impl

import android.content.Context

/** Remembers the subtitle / audio track the user picked per channel. */
internal object ChannelTrackStore {
    private const val FILE = "channel_tracks"

    fun get(context: Context, channelKey: String?, kind: String): String? {
        if (channelKey.isNullOrBlank()) return null
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString("$channelKey|$kind", null)
    }

    fun put(context: Context, channelKey: String?, kind: String, value: String) {
        if (channelKey.isNullOrBlank()) return
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString("$channelKey|$kind", value).apply()
    }
}

package com.tsutsen.platformplayer.core.data.util

import android.content.Context

/** Seconds to skip at the start of a channel's videos (the intro). */
object ChannelIntroStore {
    private const val FILE = "channel_intro"

    fun key(authorUrl: String?, authorId: String?): String? = authorUrl ?: authorId

    fun get(context: Context, channelKey: String?): Int {
        if (channelKey.isNullOrBlank()) return 0
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getInt(channelKey, 0)
    }

    /** 0 clears the saved intro. */
    fun put(context: Context, channelKey: String?, seconds: Int) {
        if (channelKey.isNullOrBlank()) return
        val e = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
        if (seconds <= 0) e.remove(channelKey) else e.putInt(channelKey, seconds)
        e.apply()
    }
}

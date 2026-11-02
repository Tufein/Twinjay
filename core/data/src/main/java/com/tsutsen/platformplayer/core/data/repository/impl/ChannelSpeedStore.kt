package com.tsutsen.platformplayer.core.data.repository.impl

import android.content.Context

/** Remembers the playback speed per channel (a podcast channel always at 1.5x, and so on). */
internal object ChannelSpeedStore {
    private const val FILE = "channel_speed"

    fun get(context: Context, channelKey: String?): Float? {
        if (channelKey.isNullOrBlank()) return null
        val v = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getFloat(channelKey, 0f)
        return if (v in 0.25f..4f) v else null
    }

    fun put(context: Context, channelKey: String?, speed: Float) {
        if (channelKey.isNullOrBlank()) return
        val edit = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
        if (speed == 1f) edit.remove(channelKey) else edit.putFloat(channelKey, speed)
        edit.apply()
    }
}

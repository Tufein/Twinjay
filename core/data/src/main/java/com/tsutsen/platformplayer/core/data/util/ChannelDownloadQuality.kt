package com.tsutsen.platformplayer.core.data.util

import android.content.Context

/** Download resolution label (like "720p") chosen for one channel; none = the global default. */
object ChannelDownloadQuality {
    private const val FILE = "channel_download_quality"

    val labels = listOf("Best", "1080p", "720p", "480p", "360p", "144p")

    /** default -> Best -> 1080p -> ... -> 144p -> default */
    fun next(current: String?): String? {
        if (current == null) return labels.first()
        val i = labels.indexOf(current)
        return if (i < 0 || i == labels.lastIndex) null else labels[i + 1]
    }

    fun get(context: Context, channelKey: String?): String? {
        if (channelKey.isNullOrBlank()) return null
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(channelKey, null)
    }

    fun put(context: Context, channelKey: String, label: String?) {
        val e = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
        if (label == null) e.remove(channelKey) else e.putString(channelKey, label)
        e.apply()
    }
}

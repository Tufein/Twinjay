package com.tsutsen.platformplayer.core.data.player

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Buffer sizes for the player, picked from the kind of network when the player is created. */
data class BufferProfile(val minMs: Int, val maxMs: Int, val startMs: Int, val resumeMs: Int) {
    companion object {
        /** Wi-Fi: the values used so far. */
        val WIFI = BufferProfile(30_000, 60_000, 1_500, 5_000)

        /** Mobile data: wait a little longer before (re)starting and keep more ahead, so a weak link stalls less often. */
        val MOBILE = BufferProfile(30_000, 90_000, 2_500, 8_000)

        fun forWifi(wifi: Boolean): BufferProfile = if (wifi) WIFI else MOBILE

        /** Unknown or failing network state counts as Wi-Fi, so nothing changes for the usual case. */
        fun current(context: Context): BufferProfile =
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val caps = cm.getNetworkCapabilities(cm.activeNetwork)
                forWifi(caps == null || !caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            } catch (_: Throwable) {
                WIFI
            }
    }
}

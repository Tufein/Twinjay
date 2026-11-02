package com.tsutsen.platformplayer.di

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/** Small rolling log of battery percentage, for the graph in Diagnostics. */
object BatteryLog {
    private const val FILE = "battery_log"
    private const val KEY = "samples"
    private const val MAX = 288 // 24 h at one sample per 5 min

    data class Sample(val time: Long, val percent: Int, val charging: Boolean)

    fun sample(context: Context, now: Long = System.currentTimeMillis()) {
        val intent: Intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val list = (read(context) + Sample(now, level * 100 / scale, charging)).takeLast(MAX)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, encode(list)).apply()
    }

    fun read(context: Context): List<Sample> =
        decode(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, "").orEmpty())

    fun encode(list: List<Sample>): String =
        list.joinToString(";") { "${it.time},${it.percent},${if (it.charging) 1 else 0}" }

    fun decode(s: String): List<Sample> =
        s.split(";").mapNotNull { part ->
            val p = part.split(",")
            if (p.size != 3) return@mapNotNull null
            Sample(p[0].toLongOrNull() ?: return@mapNotNull null, p[1].toIntOrNull() ?: return@mapNotNull null, p[2] == "1")
        }
}

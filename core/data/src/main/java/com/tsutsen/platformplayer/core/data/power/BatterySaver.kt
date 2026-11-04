package com.tsutsen.platformplayer.core.data.power

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/** Low-battery check shared by the player (quality cap) and the second screen (fast dim). */
object BatterySaver {
    /** Below this charge (percent) and not charging, the battery saver kicks in. */
    const val LOW_PERCENT = 20

    /** Current level, set from the settings (default [LOW_PERCENT]). */
    @Volatile var lowPercent: Int = LOW_PERCENT

    /** Quality cap in pixels (height) while the battery saver is active. */
    const val MAX_HEIGHT = 720

    /** Pure rule, so it can be unit tested. [level] and [scale] are as in ACTION_BATTERY_CHANGED. */
    fun isLow(level: Int, scale: Int, charging: Boolean): Boolean {
        if (level < 0 || scale <= 0 || charging) return false
        return level * 100 / scale < lowPercent
    }

    /** True when the device is low on battery and not charging (false when unknown). */
    fun isLow(context: Context): Boolean {
        val intent: Intent =
            context.applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                ?: return false
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        return isLow(
            intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
            intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1),
            charging,
        )
    }

    /** True when charging or plugged in; used to gate background downloads. */
    fun isChargingOrHigh(context: Context, minPercent: Int = 50): Boolean {
        val intent = context.applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return true
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) return true
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return true
        return level * 100 / scale >= minPercent
    }
}

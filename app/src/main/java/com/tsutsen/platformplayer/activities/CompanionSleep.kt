package com.tsutsen.platformplayer.activities

import android.os.SystemClock
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/** When the second screen should go dark while a video plays on the main screen. */
internal object CompanionSleepPolicy {
    const val IDLE_MS = 20_000L

    fun shouldSleep(
        enabled: Boolean,
        isPlaying: Boolean,
        idleMs: Long,
        delayMs: Long = IDLE_MS,
    ): Boolean = enabled && isPlaying && idleMs >= delayMs

    /** Backlight while asleep: 0 for black, else the chosen percentage (capped). */
    fun sleepBrightness(levelPercent: Int): Float = levelPercent.coerceIn(0, 100) / 100f

    /**
     * Brightness to put back on wake. A saved value of 0 (captured while
     * already dimmed) or "unset" goes back to the system brightness, so the
     * screen can never stay stuck dark.
     */
    fun wakeBrightness(saved: Float?, userSet: Float? = null): Float =
        userSet?.takeIf { it > 0.01f }
            ?: if (saved == null || saved <= 0.01f) WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE else saved

    /**
     * Darkness of the overlay on the dimmed screen: fully black at level 0,
     * lighter as the slider goes up (the backlight follows the same level).
     */
    fun scrimAlpha(levelPercent: Int): Float =
        if (levelPercent <= 0) 1f else (0.85f * (1f - levelPercent.coerceIn(0, 100) / 100f)).coerceIn(0f, 0.85f)
}

/**
 * Blacks out the second screen (and drops its backlight) after a few seconds
 * without touch while a video is playing. The first touch only wakes it; it
 * also wakes when playback pauses or stops.
 */
@Composable
internal fun CompanionSleepLayer(
    enabled: Boolean,
    isPlaying: Boolean,
    window: Window?,
    delayMs: Long = CompanionSleepPolicy.IDLE_MS,
    levelPercent: Int = 0,
    content: @Composable () -> Unit,
) {
    var lastTouch by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var asleep by remember { mutableStateOf(false) }

    LaunchedEffect(enabled, isPlaying, delayMs) {
        lastTouch = SystemClock.elapsedRealtime()
        while (true) {
            asleep = CompanionSleepPolicy.shouldSleep(enabled, isPlaying, SystemClock.elapsedRealtime() - lastTouch, delayMs)
            delay(1_000)
        }
    }

    DisposableEffect(asleep, window, levelPercent) {
        val w = window
        val saved = w?.attributes?.screenBrightness
        if (asleep && w != null) {
            w.attributes = w.attributes.apply { screenBrightness = CompanionSleepPolicy.sleepBrightness(levelPercent) }
        }
        onDispose {
            if (asleep && w != null) {
                w.attributes = w.attributes.apply { screenBrightness = CompanionSleepPolicy.wakeBrightness(saved, com.tsutsen.platformplayer.feature.player.impl.SystemControls.brightness.value) }
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                            lastTouch = SystemClock.elapsedRealtime()
                        }
                    }
                },
    ) {
        content()
        if (asleep) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        // Black hides everything; a dim level keeps the
                        // page faintly visible under a translucent scrim.
                        .background(Color.Black.copy(alpha = CompanionSleepPolicy.scrimAlpha(levelPercent)))
                        .pointerInput(Unit) {
                            detectTapGestures {
                                lastTouch = SystemClock.elapsedRealtime()
                                asleep = false
                            }
                        },
                contentAlignment = Alignment.Center,
            ) {
                SleepClock()
            }
        }
    }
}

/** Faint clock and battery level on the dark second screen (only visible when the backlight is not fully off). */
@Composable
private fun SleepClock() {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        while (true) {
            val battery =
                context.applicationContext
                    .registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
                    ?.let {
                        val level = it.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                        val scale = it.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                        if (level >= 0 && scale > 0) " \u00b7 ${level * 100 / scale}%" else ""
                    }.orEmpty()
            text = format.format(java.util.Date()) + battery
            delay(30_000)
        }
    }
    val big = com.tsutsen.platformplayer.Settings.instance.bigSleepClock
    Text(
        text,
        color = Color.White.copy(alpha = if (big) 0.6f else 0.3f),
        fontSize = if (big) 96.sp else 28.sp,
    )
}

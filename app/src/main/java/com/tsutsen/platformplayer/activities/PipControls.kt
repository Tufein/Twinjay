package com.tsutsen.platformplayer.activities

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.util.Rational
import androidx.lifecycle.LifecycleCoroutineScope
import com.tsutsen.platformplayer.core.data.repository.PlayerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Rewind 10 s / play-pause / forward 10 s buttons in the PiP window.
 * Only active while the activity is in PiP, and everything is wrapped so a
 * failure can never break PiP itself. Leaving PiP drops the actions again
 * (the PiP delegate pushes its own params on the next state change).
 */
class PipControls(
    private val activity: android.app.Activity,
    private val player: PlayerRepository,
    private val scope: LifecycleCoroutineScope,
) {
    private var receiver: BroadcastReceiver? = null
    private var job: Job? = null

    fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || receiver != null) return
        try {
            val r =
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val exo = player.exoPlayer ?: return
                        when (intent?.getStringExtra(EXTRA)) {
                            "back" -> exo.seekTo((exo.currentPosition - SKIP_MS).coerceAtLeast(0L))
                            "fwd" -> exo.seekTo(exo.currentPosition + SKIP_MS)
                            "toggle" -> exo.playWhenReady = !exo.playWhenReady
                        }
                    }
                }
            val filter = IntentFilter(ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.registerReceiver(r, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                activity.registerReceiver(r, filter)
            }
            receiver = r
            job =
                scope.launch {
                    player.playerState.map { it.isPlaying }.distinctUntilChanged().collect { push(it) }
                }
        } catch (e: Throwable) {
            Log.w(TAG, "start failed: ${e.message}")
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        receiver?.let {
            try {
                activity.unregisterReceiver(it)
            } catch (_: Throwable) {
            }
        }
        receiver = null
    }

    private fun push(playing: Boolean) {
        try {
            val size = player.exoPlayer?.videoSize
            val builder = PictureInPictureParams.Builder()
            if (size != null && size.width > 0 && size.height > 0) {
                builder.setAspectRatio(Rational(size.width, size.height))
            }
            builder.setActions(
                listOf(
                    action("back", android.R.drawable.ic_media_rew, "Back 10 s", 1),
                    action(
                        "toggle",
                        if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                        if (playing) "Pause" else "Play",
                        2,
                    ),
                    action("fwd", android.R.drawable.ic_media_ff, "Forward 10 s", 3),
                ),
            )
            activity.setPictureInPictureParams(builder.build())
        } catch (e: Throwable) {
            Log.w(TAG, "push failed: ${e.message}")
        }
    }

    private fun action(key: String, icon: Int, title: String, code: Int): RemoteAction {
        val intent = Intent(ACTION).setPackage(activity.packageName).putExtra(EXTRA, key)
        val pending =
            PendingIntent.getBroadcast(
                activity,
                code,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        return RemoteAction(Icon.createWithResource(activity, icon), title, title, pending)
    }

    private companion object {
        const val TAG = "PipControls"
        const val ACTION = "com.tufein.bluejay.thor.PIP_CONTROL"
        const val EXTRA = "key"
        const val SKIP_MS = 10_000L
    }
}

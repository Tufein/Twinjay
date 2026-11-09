package com.tsutsen.platformplayer.activities

import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import com.tsutsen.platformplayer.core.data.screens.ScreenSwap

/**
 * Moves the app to the other screen (the companion then shows on the one it
 * left). A swap can strand the app on a screen nobody can use, so the new
 * screen always asks "keep this?" and moves back by itself after 10 seconds
 * unless someone confirms.
 */
object SwapScreens {
    private const val TAG = "SwapScreens"
    private const val FILE = "swap_screens"

    private fun prefs(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    @Suppress("DEPRECATION")
    private fun displayOf(a: Activity): Int = a.windowManager.defaultDisplay.displayId

    private fun otherDisplayId(a: Activity): Int? {
        val dm = a.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val mine = displayOf(a)
        return dm.displays
            .filter { it.displayId != mine && it.isValid && it.flags and Display.FLAG_PRIVATE == 0 }
            .minByOrNull { it.displayId }
            ?.displayId
    }

    /** Start the swap. Returns false (and changes nothing) when there is no second screen. */
    fun swap(activity: Activity): Boolean {
        val target = otherDisplayId(activity) ?: return false
        prefs(activity).edit()
            .putInt("origin", displayOf(activity))
            .putInt("target", target)
            .putLong("startedAt", System.currentTimeMillis())
            .putBoolean("pending", true)
            .apply()
        return try {
            launchOn(activity, target)
            true
        } catch (e: Throwable) {
            Log.w(TAG, "swap failed: ${e.message}")
            prefs(activity).edit().clear().apply()
            false
        }
    }

    private fun launchOn(activity: Activity, displayId: Int) {
        val options = ActivityOptions.makeBasic().setLaunchDisplayId(displayId)
        val intent =
            Intent(activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent, options.toBundle())
    }

    /** Called from onResume of every MainActivity instance. */
    fun onResume(activity: Activity) {
        val p = prefs(activity)
        if (!p.getBoolean("pending", false)) return
        val startedAt = p.getLong("startedAt", 0L)
        if (ScreenSwap.isExpired(startedAt, System.currentTimeMillis())) {
            // Too late to be this swap (or it never happened): forget it.
            p.edit().clear().apply()
            return
        }
        if (displayOf(activity) != p.getInt("target", -1)) return
        val origin = p.getInt("origin", -1)
        val handler = Handler(Looper.getMainLooper())
        var done = false
        lateinit var dialog: AlertDialog
        val revert = Runnable {
            if (done) return@Runnable
            done = true
            p.edit().clear().apply()
            try {
                dialog.dismiss()
            } catch (_: Throwable) {
            }
            try {
                launchOn(activity, origin)
            } catch (e: Throwable) {
                Log.w(TAG, "revert failed: ${e.message}")
                // Last resort: let the system pick the display (the default one) instead of staying stranded.
                try {
                    activity.startActivity(
                        Intent(activity, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                    )
                } catch (e2: Throwable) {
                    Log.w(TAG, "fallback launch failed: ${e2.message}")
                }
            }
        }
        dialog =
            AlertDialog.Builder(activity)
                .setTitle("Keep the screens swapped?")
                .setMessage("Going back by itself in 10 seconds.")
                .setCancelable(false)
                .setPositiveButton("Keep") { _, _ ->
                    done = true
                    handler.removeCallbacks(revert)
                    p.edit().clear().apply()
                }.setNegativeButton("Go back") { _, _ ->
                    handler.removeCallbacks(revert)
                    revert.run()
                }.create()
        try {
            dialog.show()
        } catch (e: Throwable) {
            // Nobody can confirm without the dialog, so go back right away.
            Log.w(TAG, "confirm dialog failed: ${e.message}")
            revert.run()
            return
        }
        handler.postDelayed(revert, ScreenSwap.CONFIRM_MS)
    }
}

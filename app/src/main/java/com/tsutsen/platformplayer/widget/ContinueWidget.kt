package com.tsutsen.platformplayer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import android.widget.RemoteViews
import com.tsutsen.platformplayer.R

/** Home screen widget: the last video you watched; a tap opens the app. */
class ContinueWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val prefs = context.getSharedPreferences("widget_continue", Context.MODE_PRIVATE)
        val title = prefs.getString("title", null)
        val author = prefs.getString("author", "") ?: ""
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending =
            launch?.let {
                PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_continue)
            views.setTextViewText(R.id.widget_title, title ?: "Nothing watched yet")
            views.setTextViewText(R.id.widget_author, author)
            if (pending != null) views.setOnClickPendingIntent(R.id.widget_root, pending)
            views.setOnClickPendingIntent(R.id.widget_play_pause, mediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
            views.setOnClickPendingIntent(R.id.widget_next, mediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT))
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun mediaKey(context: Context, keyCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            keyCode,
            Intent(context, ContinueWidget::class.java).setAction(ACTION_MEDIA_KEY).putExtra(EXTRA_KEY, keyCode),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /** The buttons send a media key to whatever is playing (TwinJay's own session while a video is open). */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MEDIA_KEY) {
            val keyCode = intent.getIntExtra(EXTRA_KEY, 0)
            if (isAllowedKey(keyCode)) {
                try {
                    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                    audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
                } catch (_: Throwable) {
                }
            }
            return
        }
        super.onReceive(context, intent)
    }

    companion object {
        const val ACTION_MEDIA_KEY = "com.tsutsen.platformplayer.widget.MEDIA_KEY"
        const val EXTRA_KEY = "key"

        fun isAllowedKey(keyCode: Int): Boolean =
            keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT

        const val CLASS_NAME = "com.tsutsen.platformplayer.widget.ContinueWidget"
    }
}

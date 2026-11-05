package com.tsutsen.platformplayer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
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
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    companion object {
        const val CLASS_NAME = "com.tsutsen.platformplayer.widget.ContinueWidget"
    }
}

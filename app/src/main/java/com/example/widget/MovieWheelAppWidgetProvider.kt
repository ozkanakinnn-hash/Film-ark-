package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class MovieWheelAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val state = WidgetMovieStateStore.getOrInitState(context)
        appWidgetIds.forEach { appWidgetId ->
            val views = buildRemoteViews(context, state)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_WIDGET_SPIN -> {
                val nextState = WidgetMovieStateStore.spinNextMovie(context)
                updateAllWidgets(context, nextState)
            }

            ACTION_WIDGET_DAILY -> {
                val dailyState = WidgetMovieStateStore.showDailyRecommendation(context)
                updateAllWidgets(context, dailyState)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_SPIN = "com.example.widget.ACTION_WIDGET_SPIN"
        const val ACTION_WIDGET_DAILY = "com.example.widget.ACTION_WIDGET_DAILY"
        const val EXTRA_WIDGET_MOVIE_ID = "extra_widget_movie_id"

        fun updateAllWidgets(context: Context, state: WidgetDisplayState) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val component = ComponentName(context, MovieWheelAppWidgetProvider::class.java)
            val appWidgetIds = manager.getAppWidgetIds(component)
            if (appWidgetIds.isEmpty()) return

            appWidgetIds.forEach { appWidgetId ->
                val views = buildRemoteViews(context, state)
                manager.updateAppWidget(appWidgetId, views)
            }
        }

        fun buildRemoteViews(context: Context, state: WidgetDisplayState): RemoteViews {
            val movie = state.movie
            val views = RemoteViews(context.packageName, R.layout.widget_movie_wheel)

            val modeLabel = when (state.mode) {
                WidgetDisplayMode.DAILY -> "🎬 GÜNÜN ÖNERİSİ"
                WidgetDisplayMode.SPIN -> "🎡 ÇARK SEÇİMİ • #${state.spinCount.coerceAtLeast(1)}"
            }
            views.setTextViewText(R.id.widget_mode_badge, modeLabel)
            views.setTextViewText(R.id.widget_imdb_badge, "★ ${movie.imdb}")
            views.setTextViewText(R.id.widget_movie_title, movie.title)
            views.setTextViewText(
                R.id.widget_movie_meta,
                "${movie.year} • ${movie.genre} • ${movie.durationMins} dk"
            )
            views.setTextViewText(
                R.id.widget_movie_hook,
                movie.whyWatch.ifBlank { movie.logline }
            )

            // Direct Spin Broadcast (No App Launch Required)
            val spinIntent = Intent(context, MovieWheelAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_SPIN
            }
            val spinPendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                spinIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_spin_button, spinPendingIntent)

            // Switch Back to Today's Daily Pick Broadcast (No App Launch Required)
            val dailyIntent = Intent(context, MovieWheelAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_DAILY
            }
            val dailyPendingIntent = PendingIntent.getBroadcast(
                context,
                102,
                dailyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_daily_button, dailyPendingIntent)

            // Tapping the Movie Info Card opens the recommended/spun movie directly in the app
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_WIDGET_MOVIE_ID, movie.id)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                103,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_movie_card, openAppPendingIntent)

            return views
        }
    }
}

package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Widget düğmelerinin (Çevir / Günün Önerisi) eylemlerini işler.
 *
 * Dışa kapalıdır (android:exported="false"): yalnızca uygulamanın kendi PendingIntent'leri tetikleyebilir,
 * böylece başka uygulamalar widget durumunu değiştiremez. Dışa açık widget sağlayıcısı yalnızca
 * sistemin APPWIDGET_UPDATE yayınını alır.
 */
class WidgetActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = when (intent.action) {
            ACTION_WIDGET_SPIN -> WidgetMovieStateStore.spinNextMovie(context)
            ACTION_WIDGET_DAILY -> WidgetMovieStateStore.showDailyRecommendation(context)
            else -> return
        }
        MovieWheelAppWidgetProvider.updateAllWidgets(context, state)
    }

    companion object {
        const val ACTION_WIDGET_SPIN = "com.ozkanakin.neizlesek.widget.ACTION_WIDGET_SPIN"
        const val ACTION_WIDGET_DAILY = "com.ozkanakin.neizlesek.widget.ACTION_WIDGET_DAILY"
    }
}

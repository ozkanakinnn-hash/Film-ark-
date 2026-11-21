package com.example

import android.app.Application
import com.example.crash.CrashReporter

/** Uygulama süreci başlarken (Activity ve widget dahil) çökme raporlayıcıyı kurar. */
class NeIzlesekApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Ortak modüldeki katalog ilk kez okunmadan önce poster kaynağını ayarla (B planı: posterMode=cards)
        com.example.data.model.TmdbImageConfig.postersEnabled = BuildConfig.TMDB_POSTERS_ENABLED
        CrashReporter.install(this)
    }
}

package com.example

import android.app.Application
import com.example.crash.CrashReporter

/** Uygulama süreci başlarken (Activity ve widget dahil) çökme raporlayıcıyı kurar. */
class NeIzlesekApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
    }
}

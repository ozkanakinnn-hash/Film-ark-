package com.example.data.local

import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * İlk açılıştaki "zevkini seç" ekranının gösterilip gösterilmeyeceği.
 * Robolectric testlerinde otomatik gösterilmez (testler ana ekrandan başlar); Ayarlar'dan her zaman açılabilir.
 */
object OnboardingPreferences {
    private const val PREFS = "ne_izlesek_onboarding_prefs"
    private const val KEY_DONE = "taste_onboarding_done_v1"

    private var prefs: android.content.SharedPreferences? = null
    private val _shouldShow = MutableStateFlow(false)
    val shouldShow: StateFlow<Boolean> = _shouldShow.asStateFlow()

    fun init(context: Context) {
        val sp = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = sp
        _shouldShow.value = !sp.getBoolean(KEY_DONE, false) && Build.FINGERPRINT != "robolectric"
    }

    fun markDone() {
        _shouldShow.value = false
        prefs?.edit()?.putBoolean(KEY_DONE, true)?.apply()
    }
}

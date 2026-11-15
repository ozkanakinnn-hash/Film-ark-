package com.example.data.local

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * "Animasyonları azalt" ayarı. Kullanıcı seçmediyse sistemin animasyon ayarını izler
 * (Geliştirici seçenekleri / Erişilebilirlik'te animasyonlar kapalıysa otomatik açık).
 * Açıkken tür açılış sahnesi, ambiyans parıltısı, otomatik kayan şerit ve ekran geçişleri sadeleşir;
 * çarkın kendisi döner ama daha kısa sürer.
 */
object MotionPreferences {
    private const val PREFS_NAME = "ne_izlesek_motion_prefs"
    private const val KEY_REDUCE = "reduce_motion"

    private var prefs: android.content.SharedPreferences? = null
    private val _reduceMotion = MutableStateFlow(false)
    val reduceMotion: StateFlow<Boolean> = _reduceMotion.asStateFlow()

    /** Kullanıcı ayarı açıkça değiştirdi mi (yoksa sistem ayarı izlenir). */
    var isUserOverride: Boolean = false
        private set

    fun init(context: Context) {
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp
        isUserOverride = sp.contains(KEY_REDUCE)
        _reduceMotion.value = if (isUserOverride) sp.getBoolean(KEY_REDUCE, false) else systemAnimationsOff(context)
    }

    fun setReduceMotion(reduce: Boolean) {
        _reduceMotion.value = reduce
        isUserOverride = true
        prefs?.edit()?.putBoolean(KEY_REDUCE, reduce)?.apply()
    }

    fun systemAnimationsOff(context: Context): Boolean = runCatching {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }.getOrDefault(false)

    fun resetForTests() {
        prefs?.edit()?.clear()?.apply()
        isUserOverride = false
        _reduceMotion.value = false
    }
}

/** Ekranlarda okunur: true ise süsleme animasyonları kapatılır. */
val LocalReduceMotion = staticCompositionLocalOf { false }

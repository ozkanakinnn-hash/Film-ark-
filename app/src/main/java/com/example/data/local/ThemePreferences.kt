package com.example.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemePreferences {
    private const val PREFS_NAME = "ne_izlesek_theme_prefs"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"

    private var prefs: android.content.SharedPreferences? = null

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun init(context: Context) {
        val sharedPrefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sharedPrefs
        _isDarkMode.value = sharedPrefs.getBoolean(KEY_IS_DARK_MODE, true)
    }

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
        prefs?.edit()?.putBoolean(KEY_IS_DARK_MODE, isDark)?.apply()
    }

    fun toggleTheme(): Boolean {
        val next = !_isDarkMode.value
        setDarkMode(next)
        return next
    }
}

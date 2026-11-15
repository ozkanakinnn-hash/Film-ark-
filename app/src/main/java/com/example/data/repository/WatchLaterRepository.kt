package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * "Daha Sonra İzle" listesi. Film kimlikleri eklenme sırasıyla (en yeni başta) cihazda saklanır.
 */
object WatchLaterRepository {
    private const val PREFS = "watch_later_prefs"
    private const val KEY_IDS = "watch_later_ids"

    private var prefs: SharedPreferences? = null
    private val _ids = MutableStateFlow<List<String>>(emptyList())
    val ids: StateFlow<List<String>> = _ids.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _ids.value = prefs?.getString(KEY_IDS, "")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()
    }

    fun contains(movieId: String): Boolean = _ids.value.contains(movieId)

    /** Listeye ekler ya da listeden çıkarır. Eklendiyse true döner. */
    fun toggle(movieId: String): Boolean {
        val current = _ids.value
        val added = !current.contains(movieId)
        _ids.value = if (added) listOf(movieId) + current else current - movieId
        prefs?.edit()?.putString(KEY_IDS, _ids.value.joinToString(","))?.apply()
        return added
    }

    /** Listedeki filmler (katalogda artık olmayanlar atlanır). */
    fun movies(): List<Movie> = _ids.value.mapNotNull { MovieRepository.getMovieById(it) }

    fun clearForTests() {
        _ids.value = emptyList()
        prefs?.edit()?.clear()?.apply()
    }
}

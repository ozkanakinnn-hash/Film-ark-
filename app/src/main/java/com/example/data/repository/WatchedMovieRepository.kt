package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.WatchedMovieDao
import com.example.data.local.WatchedMovieEntity
import com.example.data.model.Movie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object WatchedMovieRepository {

    private const val PREF_NAME = "ne_izlesek_favorites_prefs"
    private const val KEY_FAVORITE_IDS = "favorite_movie_ids"

    // Yazımlar sırayla: art arda yapılan işlemler (ör. izlendi + not) birbirini ezmesin
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    @Volatile
    private var dao: WatchedMovieDao? = null

    @Volatile
    private var prefs: SharedPreferences? = null

    private val _watchedMovies = MutableStateFlow<List<WatchedMovieEntity>>(emptyList())
    val watchedMovies: StateFlow<List<WatchedMovieEntity>> = _watchedMovies.asStateFlow()

    private val _watchedMovieIds = MutableStateFlow<Set<String>>(emptySet())
    val watchedMovieIds: StateFlow<Set<String>> = _watchedMovieIds.asStateFlow()

    // Watched movies are excluded from the wheel unless the user explicitly toggles "Tekrar İzle" ON
    private val _excludedWheelMovieIds = MutableStateFlow<Set<String>>(emptySet())
    val excludedWheelMovieIds: StateFlow<Set<String>> = _excludedWheelMovieIds.asStateFlow()

    private val _favoriteMovieIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteMovieIds: StateFlow<Set<String>> = _favoriteMovieIds.asStateFlow()

    private fun syncDerivedFlows(list: List<WatchedMovieEntity>) {
        _watchedMovies.value = list
        _watchedMovieIds.value = list.map { it.movieId }.toSet()
        _excludedWheelMovieIds.value = list.filterNot { it.wantToRewatch }.map { it.movieId }.toSet()
    }

    fun init(context: Context) {
        if (prefs == null) {
            val sp = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs = sp
            val savedFavs = sp.getStringSet(KEY_FAVORITE_IDS, emptySet())?.toSet() ?: emptySet()
            if (savedFavs.isNotEmpty()) {
                _favoriteMovieIds.value = (_favoriteMovieIds.value + savedFavs).toSet()
            }
        }
        if (dao != null) return
        val localDao = AppDatabase.getInstance(context).watchedMovieDao()
        dao = localDao
        ioScope.launch {
            val persisted = localDao.getAllWatchedMoviesOnce()
            if (persisted.isNotEmpty()) {
                val merged = (_watchedMovies.value + persisted)
                    .distinctBy { it.movieId }
                    .sortedByDescending { it.watchedAtMillis }
                syncDerivedFlows(merged)
                val persistedFavs = persisted.filter { it.isFavorite }.map { it.movieId }.toSet()
                if (persistedFavs.isNotEmpty()) {
                    _favoriteMovieIds.value = (_favoriteMovieIds.value + persistedFavs).toSet()
                    prefs?.edit()?.putStringSet(KEY_FAVORITE_IDS, _favoriteMovieIds.value)?.apply()
                }
            }
        }
    }

    fun markAsWatched(movie: Movie) {
        val existing = _watchedMovies.value.find { it.movieId == movie.id }
        val isFav = existing?.isFavorite ?: _favoriteMovieIds.value.contains(movie.id)
        val entity = WatchedMovieEntity(
            movieId = movie.id,
            title = movie.title,
            year = movie.year,
            durationMins = movie.durationMins,
            genre = movie.genre,
            imdb = movie.imdb,
            watchedAtMillis = System.currentTimeMillis(),
            personalNote = existing?.personalNote ?: "",
            wantToRewatch = false,
            isFavorite = isFav,
            posterPath = movie.posterPath ?: existing?.posterPath
        )
        val updated = listOf(entity) + _watchedMovies.value.filterNot { it.movieId == movie.id }
        syncDerivedFlows(updated)

        dao?.let { localDao ->
            ioScope.launch {
                localDao.insertWatchedMovie(entity)
            }
        }
    }

    fun updateNote(movieId: String, note: String) {
        val trimmed = note.trim()
        val updated = _watchedMovies.value.map { item ->
            if (item.movieId == movieId) item.copy(personalNote = trimmed) else item
        }
        syncDerivedFlows(updated)

        dao?.let { localDao ->
            ioScope.launch {
                localDao.updatePersonalNote(movieId, trimmed)
            }
        }
    }

    fun toggleRewatch(movieId: String): Boolean? {
        val target = _watchedMovies.value.find { it.movieId == movieId } ?: return null
        val newValue = !target.wantToRewatch
        val updated = _watchedMovies.value.map { item ->
            if (item.movieId == movieId) item.copy(wantToRewatch = newValue) else item
        }
        syncDerivedFlows(updated)

        dao?.let { localDao ->
            ioScope.launch {
                localDao.updateWantToRewatch(movieId, newValue)
            }
        }
        return newValue
    }

    fun toggleFavorite(movieId: String): Boolean {
        val currentlyFavorite = _favoriteMovieIds.value.contains(movieId) ||
            (_watchedMovies.value.find { it.movieId == movieId }?.isFavorite == true)
        val newValue = !currentlyFavorite

        val updatedFavs = if (newValue) {
            _favoriteMovieIds.value + movieId
        } else {
            _favoriteMovieIds.value - movieId
        }
        _favoriteMovieIds.value = updatedFavs
        prefs?.edit()?.putStringSet(KEY_FAVORITE_IDS, updatedFavs)?.apply()

        val updatedWatched = _watchedMovies.value.map { item ->
            if (item.movieId == movieId) item.copy(isFavorite = newValue) else item
        }
        syncDerivedFlows(updatedWatched)

        dao?.let { localDao ->
            ioScope.launch {
                localDao.updateFavorite(movieId, newValue)
            }
        }
        return newValue
    }

    fun isFavorite(movieId: String): Boolean {
        return _favoriteMovieIds.value.contains(movieId)
    }

    fun removeWatched(movieId: String) {
        val updated = _watchedMovies.value.filterNot { it.movieId == movieId }
        syncDerivedFlows(updated)

        dao?.let { localDao ->
            ioScope.launch {
                localDao.deleteWatchedMovieById(movieId)
            }
        }
    }

    fun isWatched(movieId: String): Boolean {
        return _watchedMovieIds.value.contains(movieId)
    }

    /** Yedekten geri yüklemeden sonra veritabanını ve favorileri yeniden okur. */
    suspend fun reloadAfterRestore(favoriteIds: Set<String>) {
        val localDao = dao ?: return
        val persisted = localDao.getAllWatchedMoviesOnce().sortedByDescending { it.watchedAtMillis }
        syncDerivedFlows(persisted)
        val favs = _favoriteMovieIds.value + favoriteIds + persisted.filter { it.isFavorite }.map { it.movieId }
        _favoriteMovieIds.value = favs
        prefs?.edit()?.putStringSet(KEY_FAVORITE_IDS, favs)?.apply()
    }

    fun clearAllForTests() {
        syncDerivedFlows(emptyList())
        _favoriteMovieIds.value = emptySet()
        prefs?.edit()?.remove(KEY_FAVORITE_IDS)?.apply()
        dao?.let { localDao ->
            ioScope.launch {
                localDao.clearAll()
            }
        }
    }
}

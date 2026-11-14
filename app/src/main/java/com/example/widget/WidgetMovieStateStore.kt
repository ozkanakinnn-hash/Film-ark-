package com.example.widget

import android.content.Context
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.data.repository.WatchedMovieRepository
import java.util.Calendar
import kotlin.random.Random

enum class WidgetDisplayMode {
    DAILY,
    SPIN
}

data class WidgetDisplayState(
    val movie: Movie,
    val mode: WidgetDisplayMode,
    val spinCount: Int,
    val dayKey: Int
)

/**
 * Persists home-screen widget state independently of Activity lifecycle so users can
 * spin the wheel or inspect today's curated pick without opening the app.
 */
object WidgetMovieStateStore {

    private const val PREF_NAME = "ne_izlesek_widget_prefs"
    private const val KEY_MOVIE_ID = "widget_movie_id"
    private const val KEY_MODE = "widget_mode"
    private const val KEY_SPIN_COUNT = "widget_spin_count"
    private const val KEY_DAY_KEY = "widget_day_key"

    fun currentDayKey(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Deterministically chooses today's featured recommendation from high-rated catalog titles.
     */
    fun getDailyRecommendation(
        dayKey: Int = currentDayKey(),
        excludedIds: Set<String> = WatchedMovieRepository.excludedWheelMovieIds.value
    ): Movie {
        val curated = MovieRepository.allMovies
            .filter { it.imdb >= 7.5 && !excludedIds.contains(it.id) }
            .ifEmpty {
                MovieRepository.allMovies.filterNot { excludedIds.contains(it.id) }
            }
            .ifEmpty { MovieRepository.allMovies }

        val index = kotlin.math.abs(dayKey) % curated.size
        return curated[index]
    }

    fun getOrInitState(
        context: Context,
        dayKey: Int = currentDayKey()
    ): WidgetDisplayState {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedDayKey = prefs.getInt(KEY_DAY_KEY, -1)
        val savedMode = runCatching {
            WidgetDisplayMode.valueOf(prefs.getString(KEY_MODE, WidgetDisplayMode.DAILY.name) ?: WidgetDisplayMode.DAILY.name)
        }.getOrDefault(WidgetDisplayMode.DAILY)
        val savedSpinCount = prefs.getInt(KEY_SPIN_COUNT, 0)
        val savedMovieId = prefs.getString(KEY_MOVIE_ID, null)
        val savedMovie = savedMovieId?.let { MovieRepository.getMovieById(it) }

        // If a new calendar day started while in DAILY mode (or no valid movie yet), refresh today's pick
        if (savedMovie == null || (savedMode == WidgetDisplayMode.DAILY && savedDayKey != dayKey)) {
            val dailyMovie = getDailyRecommendation(dayKey = dayKey)
            val state = WidgetDisplayState(
                movie = dailyMovie,
                mode = WidgetDisplayMode.DAILY,
                spinCount = savedSpinCount,
                dayKey = dayKey
            )
            saveState(context, state)
            return state
        }

        return WidgetDisplayState(
            movie = savedMovie,
            mode = savedMode,
            spinCount = savedSpinCount,
            dayKey = savedDayKey.takeIf { it > 0 } ?: dayKey
        )
    }

    fun spinNextMovie(
        context: Context,
        dayKey: Int = currentDayKey(),
        random: Random = Random.Default
    ): WidgetDisplayState {
        val current = getOrInitState(context, dayKey)
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value

        val pool = MovieRepository.allMovies
            .filterNot { excludedIds.contains(it.id) }
            .filter { it.id != current.movie.id }
            .ifEmpty {
                MovieRepository.allMovies.filter { it.id != current.movie.id }
            }
            .ifEmpty { MovieRepository.allMovies }

        val nextMovie = pool[random.nextInt(pool.size)]
        val nextState = WidgetDisplayState(
            movie = nextMovie,
            mode = WidgetDisplayMode.SPIN,
            spinCount = current.spinCount + 1,
            dayKey = dayKey
        )
        saveState(context, nextState)
        return nextState
    }

    fun showDailyRecommendation(
        context: Context,
        dayKey: Int = currentDayKey()
    ): WidgetDisplayState {
        val current = getOrInitState(context, dayKey)
        val dailyMovie = getDailyRecommendation(dayKey = dayKey)
        val nextState = WidgetDisplayState(
            movie = dailyMovie,
            mode = WidgetDisplayMode.DAILY,
            spinCount = current.spinCount,
            dayKey = dayKey
        )
        saveState(context, nextState)
        return nextState
    }

    private fun saveState(context: Context, state: WidgetDisplayState) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_MOVIE_ID, state.movie.id)
            .putString(KEY_MODE, state.mode.name)
            .putInt(KEY_SPIN_COUNT, state.spinCount)
            .putInt(KEY_DAY_KEY, state.dayKey)
            .apply()
    }

    fun clearForTests(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }
}

package com.example.data.repository

import com.example.data.model.Movie
import com.example.data.model.UpcomingRelease
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Vitrin listeleri: şu an popüler filmler/diziler, "en kötüler" ve yakında vizyona girecekler.
 * Kaynak: haftalık uzaktan katalog (varsa) yoksa APK içindeki [GeneratedHighlights].
 * Listeler TMDB kimliğiyle tutulur ve katalogdaki yapımlara çevrilir (bulunamayanlar atlanır).
 */
object HighlightsRepository {

    private fun remote() = RemoteCatalogRepository.catalog.value

    private fun resolve(tmdbIds: List<Int>, series: Boolean): List<Movie> {
        val byTmdb = MovieRepository.allMovies
            .filter { it.isSeries == series && it.tmdbId != null }
            .associateBy { it.tmdbId!! }
        return tmdbIds.mapNotNull { byTmdb[it] }.distinctBy { it.id }
    }

    fun popularMovies(): List<Movie> =
        resolve(remote()?.popularMovieTmdbIds?.ifEmpty { null } ?: GeneratedHighlights.popularMovieTmdbIds, series = false)

    fun popularSeries(): List<Movie> =
        resolve(remote()?.popularSeriesTmdbIds?.ifEmpty { null } ?: GeneratedHighlights.popularSeriesTmdbIds, series = true)

    /** "Bunu Niye İzledim?" çarkı: çok oy almış ama en düşük puanlı filmler (puana göre artan). */
    fun worstMovies(): List<Movie> {
        val listed = resolve(remote()?.worstMovieTmdbIds?.ifEmpty { null } ?: GeneratedHighlights.worstMovieTmdbIds, series = false)
        val pool = listed.ifEmpty {
            MovieRepository.allMovies.filter { !it.isSeries && it.imdb in 0.1..5.6 }
        }
        return pool.sortedBy { it.imdb }
    }

    /** Yakında: bugünden sonra vizyona girecekler (tarihe göre). [todayIso] yyyy-MM-dd. */
    fun upcoming(todayIso: String = isoDaysFromToday(0)): List<UpcomingRelease> =
        allUpcoming().filter { it.releaseDate > todayIso }.sortedBy { it.releaseDate }

    /** Vizyonda: son 4 haftada vizyona girmiş olanlar (en yeni önce). */
    fun nowPlaying(todayIso: String = isoDaysFromToday(0), fromIso: String = isoDaysFromToday(-28)): List<UpcomingRelease> =
        allUpcoming().filter { it.releaseDate <= todayIso && it.releaseDate > fromIso }
            .sortedByDescending { it.releaseDate }

    private fun allUpcoming(): List<UpcomingRelease> =
        remote()?.upcoming?.ifEmpty { null } ?: GeneratedHighlights.upcoming

    // java.time minSdk 24'te yok: ISO tarihleri (yyyy-MM-dd) metin olarak karşılaştırılır
    fun isoDaysFromToday(days: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
}

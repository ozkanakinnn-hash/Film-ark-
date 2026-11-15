package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Movie

/**
 * Cihazda tutulan basit zevk profili (sunucuya gönderilmez).
 *
 * - Favoriler, "Tekrar İzle" işaretli izlenenler ve "Daha Sonra İzle" listesindeki filmlerin türleri öne çıkar.
 * - Veto edilen filmlerin türleri geri plana düşer.
 * Ağırlıklar yalnızca çarka hangi filmlerin konacağını etkiler; çark dönerken her dilimin şansı eşittir.
 */
object TasteProfileRepository {
    private const val PREFS = "taste_profile_prefs"
    private const val KEY_VETO_GENRES = "veto_genres"

    private var prefs: SharedPreferences? = null
    private val vetoGenreCounts = mutableMapOf<String, Int>()

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        vetoGenreCounts.clear()
        vetoGenreCounts.putAll(decode(prefs?.getString(KEY_VETO_GENRES, "").orEmpty()))
    }

    fun genresOf(genre: String): List<String> =
        genre.split('/', ',', '•').map { it.trim() }.filter { it.isNotEmpty() }

    @Synchronized
    fun recordVeto(movie: Movie) {
        genresOf(movie.genre).forEach { g -> vetoGenreCounts[g] = (vetoGenreCounts[g] ?: 0) + 1 }
        prefs?.edit()?.putString(KEY_VETO_GENRES, encode(vetoGenreCounts))?.apply()
    }

    @Synchronized
    fun vetoGenres(): Map<String, Int> = vetoGenreCounts.toMap()

    @Synchronized
    fun restoreVetoGenres(counts: Map<String, Int>) {
        vetoGenreCounts.clear()
        vetoGenreCounts.putAll(counts.filterValues { it > 0 })
        prefs?.edit()?.putString(KEY_VETO_GENRES, encode(vetoGenreCounts))?.apply()
    }

    /** Beğenilen türlerin sayımı: favoriler (2 puan), tekrar izle (2), daha sonra izle (1), izlenen (1). */
    fun likedGenres(): Map<String, Int> {
        val scores = mutableMapOf<String, Int>()
        fun add(genre: String, points: Int) = genresOf(genre).forEach { scores[it] = (scores[it] ?: 0) + points }
        val favIds = WatchedMovieRepository.favoriteMovieIds.value
        favIds.forEach { id -> MovieRepository.getMovieById(id)?.let { add(it.genre, 2) } }
        WatchedMovieRepository.watchedMovies.value.forEach { w ->
            add(w.genre, if (w.wantToRewatch) 2 else 1)
        }
        WatchLaterRepository.ids.value.forEach { id -> MovieRepository.getMovieById(id)?.let { add(it.genre, 1) } }
        return scores
    }

    /** Snapshot: çark örneklemesi boyunca aynı profili kullanmak için. */
    fun snapshot(): TasteProfile = TasteProfile(likedGenres(), vetoGenres())

    fun clearForTests() {
        vetoGenreCounts.clear()
        prefs?.edit()?.clear()?.apply()
    }

    private fun encode(map: Map<String, Int>) = map.entries.joinToString(";") { "${it.key.replace(";", " ").replace("=", " ")}=${it.value}" }

    private fun decode(raw: String): Map<String, Int> = raw.split(';').mapNotNull { part ->
        val i = part.lastIndexOf('=')
        if (i <= 0) null else part.substring(0, i) to (part.substring(i + 1).toIntOrNull() ?: return@mapNotNull null)
    }.toMap()
}

data class TasteProfile(
    val likedGenres: Map<String, Int>,
    val vetoGenres: Map<String, Int>
) {
    val isEmpty: Boolean get() = likedGenres.isEmpty() && vetoGenres.isEmpty()

    /** 0.25 ile 3.0 arası örnekleme ağırlığı. Profil boşsa herkes 1.0. */
    fun weightOf(movie: Movie): Double {
        if (isEmpty) return 1.0
        val genres = TasteProfileRepository.genresOf(movie.genre)
        val liked = genres.sumOf { likedGenres[it] ?: 0 }.coerceAtMost(6)
        val vetoed = genres.sumOf { vetoGenres[it] ?: 0 }.coerceAtMost(6)
        return (1.0 + 0.35 * liked - 0.2 * vetoed).coerceIn(0.25, 3.0)
    }

    fun topLikedGenre(movie: Movie): String? =
        TasteProfileRepository.genresOf(movie.genre)
            .filter { (likedGenres[it] ?: 0) > 0 }
            .maxByOrNull { likedGenres[it] ?: 0 }
}

/**
 * "Neden bu film?" satırı: en fazla üç kısa gerekçe.
 */
fun buildWhyThisMovie(
    movie: Movie,
    profile: TasteProfile,
    selectedPlatforms: Set<String> = emptySet(),
    selectedGenres: Set<String> = emptySet()
): String {
    val reasons = mutableListOf<String>()
    profile.topLikedGenre(movie)?.let { reasons += "Sevdiğin türden: $it" }
    val matchedGenre = TasteProfileRepository.genresOf(movie.genre).firstOrNull { it in selectedGenres }
    if (matchedGenre != null && reasons.none { it.endsWith(matchedGenre) }) reasons += "Seçtiğin tür: $matchedGenre"
    if (movie.imdb >= 8.0) reasons += "Yüksek puanlı ★ ${movie.imdb}"
    movie.platforms.firstOrNull { it in selectedPlatforms }?.let { reasons += "İzlenebilir: $it" }
    if (movie.isNewRelease) reasons += "Yeni yapım (${movie.year})"
    if (movie.isSeries) {
        movie.seasonCount?.takeIf { it <= 2 }?.let { reasons += "Kısa dizi: $it sezon" }
    } else if (movie.durationMins in 1..100) {
        reasons += "Kısa: ${movie.durationMins} dk"
    }
    if (reasons.isEmpty()) reasons += "Filtrelerine uyan ${movie.genre.substringBefore('/').trim()} ${if (movie.isSeries) "dizisi" else "filmi"}"
    return reasons.take(3).joinToString(" • ")
}

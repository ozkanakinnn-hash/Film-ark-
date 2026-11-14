package com.example.data.model

import com.example.data.local.WatchedMovieEntity
import com.example.data.repository.MovieRepository
import com.example.data.repository.SavedDecision
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

data class GenreDistributionItem(
    val genre: String,
    val spunCount: Int,
    val spunShare: Float,
    val likedCount: Int,
    val likedShare: Float,
    val totalCount: Int
)

data class SpunAndLikedGenreStats(
    val totalSpunMovies: Int,
    val totalLikedMovies: Int,
    val dominantSpunGenre: String?,
    val dominantLikedGenre: String?,
    val alignmentPercent: Int,
    val averageSpunImdb: Double,
    val averageLikedImdb: Double,
    val items: List<GenreDistributionItem>,
    val insightSummary: String
)

private data class DualGenreCounter(
    var spunCount: Int = 0,
    var likedCount: Int = 0
)

/**
 * Resolves all movies liked by the user from explicit favorites and rewatch-marked watched entries.
 */
fun resolveLikedMovies(
    favoriteMovieIds: Set<String>,
    watchedMovies: List<WatchedMovieEntity>
): List<Movie> {
    val byId = linkedMapOf<String, Movie>()

    favoriteMovieIds.forEach { favId ->
        val fromCatalog = MovieRepository.getMovieById(favId)
        if (fromCatalog != null) {
            byId[favId] = fromCatalog
        } else {
            watchedMovies.find { it.movieId == favId }?.let { w ->
                byId[favId] = w.toStubMovie()
            }
        }
    }

    watchedMovies.forEach { w ->
        if ((w.isFavorite || w.wantToRewatch) && !byId.containsKey(w.movieId)) {
            byId[w.movieId] = MovieRepository.getMovieById(w.movieId) ?: w.toStubMovie()
        }
    }

    return byId.values.toList()
}

private fun WatchedMovieEntity.toStubMovie(): Movie = Movie(
    id = movieId,
    title = title,
    originalTitle = title,
    year = year,
    durationMins = durationMins,
    imdb = imdb,
    moodTag = "liked",
    platforms = emptyList(),
    genre = genre,
    logline = personalNote.ifBlank { "$genre • $year" },
    whyWatch = "Beğenilenlerimden",
    posterPath = posterPath ?: "",
    colorHex = 0xFFDB2777
)

fun computeSpunAndLikedGenreStats(
    savedDecisions: List<SavedDecision>,
    favoriteMovieIds: Set<String>,
    watchedMovies: List<WatchedMovieEntity>
): SpunAndLikedGenreStats {
    val spunMovies = savedDecisions.map { it.movie }
    val likedMovies = resolveLikedMovies(favoriteMovieIds, watchedMovies)

    return computeSpunAndLikedGenreStatsFromMovies(
        spunMovies = spunMovies,
        likedMovies = likedMovies
    )
}

fun computeSpunAndLikedGenreStatsFromMovies(
    spunMovies: List<Movie>,
    likedMovies: List<Movie>
): SpunAndLikedGenreStats {
    if (spunMovies.isEmpty() && likedMovies.isEmpty()) {
        return SpunAndLikedGenreStats(
            totalSpunMovies = 0,
            totalLikedMovies = 0,
            dominantSpunGenre = null,
            dominantLikedGenre = null,
            alignmentPercent = 0,
            averageSpunImdb = 0.0,
            averageLikedImdb = 0.0,
            items = emptyList(),
            insightSummary = "Çarkı çevirdikçe ve beğendiğin filmlerde ❤️ simgesine dokundukça tür dağılım grafiklerin burada oluşacak."
        )
    }

    val counters = linkedMapOf<String, DualGenreCounter>()

    fun splitGenres(rawGenre: String): List<String> =
        rawGenre.split("/")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("Genel") }

    spunMovies.forEach { movie ->
        splitGenres(movie.genre).forEach { g ->
            counters.getOrPut(g) { DualGenreCounter() }.spunCount += 1
        }
    }

    likedMovies.forEach { movie ->
        splitGenres(movie.genre).forEach { g ->
            counters.getOrPut(g) { DualGenreCounter() }.likedCount += 1
        }
    }

    val totalSpunHits = counters.values.sumOf { it.spunCount }.coerceAtLeast(1)
    val totalLikedHits = counters.values.sumOf { it.likedCount }.coerceAtLeast(1)

    // Distribution overlap (0..100%) across all genres between spun and liked distributions
    val alignmentPercent = if (spunMovies.isNotEmpty() && likedMovies.isNotEmpty()) {
        val overlap = counters.values.sumOf { c ->
            val sShare = c.spunCount.toDouble() / totalSpunHits.toDouble()
            val lShare = c.likedCount.toDouble() / totalLikedHits.toDouble()
            min(sShare, lShare)
        }
        (overlap * 100.0).roundToInt().coerceIn(0, 100)
    } else {
        0
    }

    val sortedEntries = counters.entries.sortedWith(
        compareByDescending<Map.Entry<String, DualGenreCounter>> { it.value.spunCount + it.value.likedCount }
            .thenByDescending { it.value.likedCount }
            .thenByDescending { it.value.spunCount }
            .thenBy { it.key }
    )

    // Keep top 5 genres and aggregate long tail under "Diğer"
    val displayed = if (sortedEntries.size <= 6) {
        sortedEntries.map { it.key to it.value }
    } else {
        val topFive = sortedEntries.take(5).map { it.key to it.value }
        val other = DualGenreCounter()
        sortedEntries.drop(5).forEach {
            other.spunCount += it.value.spunCount
            other.likedCount += it.value.likedCount
        }
        topFive + ("Diğer" to other)
    }

    val items = displayed.map { (genre, c) ->
        GenreDistributionItem(
            genre = genre,
            spunCount = c.spunCount,
            spunShare = if (spunMovies.isEmpty()) 0f else c.spunCount.toFloat() / totalSpunHits.toFloat(),
            likedCount = c.likedCount,
            likedShare = if (likedMovies.isEmpty()) 0f else c.likedCount.toFloat() / totalLikedHits.toFloat(),
            totalCount = c.spunCount + c.likedCount
        )
    }

    val dominantSpun = counters.entries
        .filter { it.value.spunCount > 0 }
        .maxWithOrNull(compareBy<Map.Entry<String, DualGenreCounter>> { it.value.spunCount }.thenByDescending { -it.key.length })
        ?.key

    val dominantLiked = counters.entries
        .filter { it.value.likedCount > 0 }
        .maxWithOrNull(compareBy<Map.Entry<String, DualGenreCounter>> { it.value.likedCount }.thenByDescending { -it.key.length })
        ?.key

    val avgSpunImdb = if (spunMovies.isEmpty()) 0.0 else spunMovies.sumOf { it.imdb } / spunMovies.size
    val avgLikedImdb = if (likedMovies.isEmpty()) 0.0 else likedMovies.sumOf { it.imdb } / likedMovies.size

    val insightSummary = buildSpunLikedInsightSummary(
        totalSpun = spunMovies.size,
        totalLiked = likedMovies.size,
        dominantSpun = dominantSpun,
        dominantLiked = dominantLiked,
        alignmentPercent = alignmentPercent,
        avgSpunImdb = avgSpunImdb,
        avgLikedImdb = avgLikedImdb
    )

    return SpunAndLikedGenreStats(
        totalSpunMovies = spunMovies.size,
        totalLikedMovies = likedMovies.size,
        dominantSpunGenre = dominantSpun,
        dominantLikedGenre = dominantLiked,
        alignmentPercent = alignmentPercent,
        averageSpunImdb = avgSpunImdb,
        averageLikedImdb = avgLikedImdb,
        items = items,
        insightSummary = insightSummary
    )
}

private fun buildSpunLikedInsightSummary(
    totalSpun: Int,
    totalLiked: Int,
    dominantSpun: String?,
    dominantLiked: String?,
    alignmentPercent: Int,
    avgSpunImdb: Double,
    avgLikedImdb: Double
): String {
    return when {
        totalSpun > 0 && totalLiked > 0 -> {
            val spunPart = dominantSpun?.let { "Çarkta en sık $it türü öne çıktı" } ?: "Çark geçmişin oluştu"
            val likedPart = dominantLiked?.let { "beğenilerinde ise $it lider" } ?: "beğenilerin kaydedildi"
            val imdbDiff = String.format(Locale.US, "%.1f", avgLikedImdb)
            "$spunPart; $likedPart. Çark–beğeni tür uyumun %$alignmentPercent (Beğeni ort. ★$imdbDiff)."
        }
        totalSpun > 0 -> {
            val spunPart = dominantSpun?.let { "Şu ana kadar çarktan en çok $it türü çıktı" } ?: "Çark seçimlerin kaydedildi"
            val spunAvg = String.format(Locale.US, "%.1f", avgSpunImdb)
            "$spunPart (Ort. ★$spunAvg). Sevdiğin filmlerde ❤️ simgesine dokunarak beğeni grafiğini de aktif edebilirsin!"
        }
        else -> {
            val likedPart = dominantLiked?.let { "Beğenilerinde $it türü öne çıkıyor" } ?: "Favorilerin kaydedildi"
            "$likedPart. Çarkı çevirdikçe şans ve zevk karşılaştırma grafiklerin tamamlanacak!"
        }
    }
}

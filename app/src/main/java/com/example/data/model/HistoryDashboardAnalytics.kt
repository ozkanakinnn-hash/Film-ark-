package com.example.data.model

import com.example.data.repository.SavedDecision
import java.util.Locale
import kotlin.math.roundToInt

data class TopSelectedGenreStat(
    val genre: String,
    val selectionCount: Int,
    val uniqueMovieCount: Int,
    val sharePercent: Int,
    val shareFraction: Float
)

data class TopSelectedMovieStat(
    val movie: Movie,
    val selectionCount: Int,
    val lastSelectedDateText: String,
    val lastMode: String,
    val modesUsed: List<String>
)

data class SelectionModeStat(
    val modeCategory: String,
    val selectionCount: Int,
    val totalMinutesSaved: Int,
    val shareFraction: Float
)

data class HistoryDashboardSummary(
    val totalSelections: Int,
    val uniqueMoviesCount: Int,
    val totalMinutesSaved: Int,
    val averageImdb: Double,
    val repeatPickMovieCount: Int,
    val topGenres: List<TopSelectedGenreStat>,
    val topMovies: List<TopSelectedMovieStat>,
    val modeBreakdown: List<SelectionModeStat>,
    val insightText: String
) {
    val dominantGenre: TopSelectedGenreStat?
        get() = topGenres.firstOrNull()

    val dominantMovie: TopSelectedMovieStat?
        get() = topMovies.firstOrNull()
}

private class GenrePickAccumulator {
    var selectionCount: Int = 0
    val uniqueMovieIds = linkedSetOf<String>()
}

private class MoviePickAccumulator(
    var latestDecision: SavedDecision
) {
    var selectionCount: Int = 0
    val modes = linkedSetOf<String>()
}

private class ModeAccumulator {
    var count: Int = 0
    var minutesSaved: Int = 0
}

fun normalizeSelectionModeCategory(rawMode: String): String {
    return when {
        rawMode.contains("Ortak Oda", ignoreCase = true) -> "Ortak Oda"
        rawMode.contains("Özel Çark", ignoreCase = true) -> "Özel Çark"
        rawMode.contains("Karşılaştır", ignoreCase = true) ||
            rawMode.contains("VS", ignoreCase = true) -> "VS Düello"
        rawMode.contains("Kült", ignoreCase = true) ||
            rawMode.contains("Diğer", ignoreCase = true) -> "Kült & Diğer"
        else -> "Tek Başına"
    }
}

fun analyzeDecisionHistory(
    decisions: List<SavedDecision>,
    maxGenres: Int = 6,
    maxMovies: Int = 8
): HistoryDashboardSummary {
    if (decisions.isEmpty()) {
        return HistoryDashboardSummary(
            totalSelections = 0,
            uniqueMoviesCount = 0,
            totalMinutesSaved = 0,
            averageImdb = 0.0,
            repeatPickMovieCount = 0,
            topGenres = emptyList(),
            topMovies = emptyList(),
            modeBreakdown = emptyList(),
            insightText = "Çarkı çevirdikçe veya VS düellosu tamamladıkça yerel veritabanındaki en sık seçilen tür ve filmler burada analiz edilecek."
        )
    }

    val genreCounters = linkedMapOf<String, GenrePickAccumulator>()
    val movieCounters = linkedMapOf<String, MoviePickAccumulator>()
    val modeCounters = linkedMapOf<String, ModeAccumulator>()

    decisions.forEach { decision ->
        val movie = decision.movie

        val tokens = movie.genre
            .split("/", ",", "•")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("Genel") }

        tokens.forEach { genreName ->
            val acc = genreCounters.getOrPut(genreName) { GenrePickAccumulator() }
            acc.selectionCount += 1
            acc.uniqueMovieIds.add(movie.id)
        }

        val movieKey = movie.id.ifBlank { movie.title.lowercase(Locale.ROOT) }
        val existingMovieAcc = movieCounters[movieKey]
        if (existingMovieAcc == null) {
            movieCounters[movieKey] = MoviePickAccumulator(decision).apply {
                selectionCount = 1
                modes.add(normalizeSelectionModeCategory(decision.mode))
            }
        } else {
            existingMovieAcc.selectionCount += 1
            existingMovieAcc.modes.add(normalizeSelectionModeCategory(decision.mode))
            if (decision.selectedAtMillis > existingMovieAcc.latestDecision.selectedAtMillis) {
                existingMovieAcc.latestDecision = decision
            }
        }

        val normalizedMode = normalizeSelectionModeCategory(decision.mode)
        val modeAcc = modeCounters.getOrPut(normalizedMode) { ModeAccumulator() }
        modeAcc.count += 1
        modeAcc.minutesSaved += decision.minutesSaved
    }

    val totalGenreHits = genreCounters.values.sumOf { it.selectionCount }.coerceAtLeast(1)
    val sortedGenres = genreCounters.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, GenrePickAccumulator>> { it.value.selectionCount }
                .thenByDescending { it.value.uniqueMovieIds.size }
                .thenBy { it.key }
        )
        .take(maxGenres)
        .map { (genre, acc) ->
            val fraction = acc.selectionCount.toFloat() / totalGenreHits.toFloat()
            TopSelectedGenreStat(
                genre = genre,
                selectionCount = acc.selectionCount,
                uniqueMovieCount = acc.uniqueMovieIds.size,
                sharePercent = (fraction * 100f).roundToInt(),
                shareFraction = fraction
            )
        }

    val sortedMovies = movieCounters.values
        .sortedWith(
            compareByDescending<MoviePickAccumulator> { it.selectionCount }
                .thenByDescending { it.latestDecision.selectedAtMillis }
                .thenByDescending { it.latestDecision.movie.imdb }
                .thenBy { it.latestDecision.movie.title }
        )
        .take(maxMovies)
        .map { acc ->
            TopSelectedMovieStat(
                movie = acc.latestDecision.movie,
                selectionCount = acc.selectionCount,
                lastSelectedDateText = acc.latestDecision.dateText,
                lastMode = acc.latestDecision.mode,
                modesUsed = acc.modes.toList()
            )
        }

    val totalSelections = decisions.size
    val totalMinutesSaved = decisions.sumOf { it.minutesSaved }
    val avgImdb = decisions.sumOf { it.movie.imdb } / totalSelections.toDouble()
    val uniqueMoviesCount = movieCounters.size
    val repeatPickMovieCount = movieCounters.values.count { it.selectionCount >= 2 }

    val sortedModes = modeCounters.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, ModeAccumulator>> { it.value.count }
                .thenByDescending { it.value.minutesSaved }
                .thenBy { it.key }
        )
        .map { (modeCategory, acc) ->
            SelectionModeStat(
                modeCategory = modeCategory,
                selectionCount = acc.count,
                totalMinutesSaved = acc.minutesSaved,
                shareFraction = acc.count.toFloat() / totalSelections.toFloat()
            )
        }

    val topGenre = sortedGenres.firstOrNull()
    val topMovie = sortedMovies.firstOrNull()
    val topMode = sortedModes.firstOrNull()
    val formattedImdb = String.format(Locale.US, "%.1f", avgImdb)

    val insightText = buildString {
        if (topGenre != null) {
            append("Veritabanındaki $totalSelections seçimin %${topGenre.sharePercent}'inde ${topGenre.genre} türü öne çıktı")
        } else {
            append("Veritabanında $totalSelections film seçimi analiz edildi")
        }
        if (topMovie != null) {
            if (topMovie.selectionCount > 1) {
                append("; en sık seçilen film ${topMovie.selectionCount} kez ile \"${topMovie.movie.title}\" oldu.")
            } else {
                append("; zirvede \"${topMovie.movie.title}\" (★${topMovie.movie.imdb}) yer alıyor.")
            }
        } else {
            append(".")
        }
        if (topMode != null) {
            append(" En çok tercih edilen mod: ${topMode.modeCategory} (Ort. ★$formattedImdb).")
        }
    }

    return HistoryDashboardSummary(
        totalSelections = totalSelections,
        uniqueMoviesCount = uniqueMoviesCount,
        totalMinutesSaved = totalMinutesSaved,
        averageImdb = avgImdb,
        repeatPickMovieCount = repeatPickMovieCount,
        topGenres = sortedGenres,
        topMovies = sortedMovies,
        modeBreakdown = sortedModes,
        insightText = insightText
    )
}

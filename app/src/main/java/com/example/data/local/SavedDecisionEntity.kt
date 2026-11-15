package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Movie
import com.example.data.model.normalizePlatformName
import com.example.data.model.TmdbImageConfig
import com.example.data.repository.SavedDecision

@Entity(tableName = "saved_decisions")
data class SavedDecisionEntity(
    @PrimaryKey val id: String,
    val movieId: String,
    val movieTitle: String,
    val movieOriginalTitle: String = "",
    val movieYear: Int,
    val movieDurationMins: Int,
    val movieImdb: Double,
    val movieMoodTag: String = "",
    val moviePlatformsCsv: String = "",
    val movieGenre: String,
    val movieLogline: String = "",
    val movieWhyWatch: String = "",
    val moviePosterPath: String = "",
    val moviePosterUrl: String = "",
    val movieColorHex: Long = 0xFF8B5CF6L,
    val dateText: String,
    val minutesSaved: Int = 45,
    val mode: String,
    val selectedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(): SavedDecision {
        val parsedPlatforms = moviePlatformsCsv
            .split(",")
            .map { normalizePlatformName(it) }
            .filter { it.isNotEmpty() }
            .distinct()
            .ifEmpty { listOf("Özel Liste") }
        val resolvedUrl = moviePosterUrl.ifBlank {
            TmdbImageConfig.getPosterUrl(moviePosterPath).orEmpty()
        }
        return SavedDecision(
            id = id,
            movie = Movie(
                id = movieId,
                title = movieTitle,
                originalTitle = movieOriginalTitle.ifBlank { movieTitle },
                year = movieYear,
                durationMins = movieDurationMins,
                imdb = movieImdb,
                moodTag = movieMoodTag,
                platforms = parsedPlatforms,
                genre = movieGenre,
                logline = movieLogline,
                whyWatch = movieWhyWatch,
                posterPath = moviePosterPath,
                posterUrl = resolvedUrl,
                colorHex = movieColorHex,
                kind = if (movieId.startsWith("tv")) com.example.data.model.ContentKind.SERIES
                else com.example.data.model.ContentKind.MOVIE,
                seasonCount = com.example.data.repository.MovieRepository.getMovieById(movieId)?.seasonCount
            ),
            dateText = dateText,
            minutesSaved = minutesSaved,
            mode = mode,
            selectedAtMillis = selectedAtMillis
        )
    }
}

fun SavedDecision.toEntity(): SavedDecisionEntity {
    return SavedDecisionEntity(
        id = id,
        movieId = movie.id,
        movieTitle = movie.title,
        movieOriginalTitle = movie.originalTitle,
        movieYear = movie.year,
        movieDurationMins = movie.durationMins,
        movieImdb = movie.imdb,
        movieMoodTag = movie.moodTag,
        moviePlatformsCsv = movie.platforms.joinToString(","),
        movieGenre = movie.genre,
        movieLogline = movie.logline,
        movieWhyWatch = movie.whyWatch,
        moviePosterPath = movie.posterPath,
        moviePosterUrl = movie.posterUrl,
        movieColorHex = movie.colorHex,
        dateText = dateText,
        minutesSaved = minutesSaved,
        mode = mode,
        selectedAtMillis = selectedAtMillis
    )
}

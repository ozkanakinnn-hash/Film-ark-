package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watched_movies")
data class WatchedMovieEntity(
    @PrimaryKey val movieId: String,
    val title: String,
    val year: Int,
    val durationMins: Int,
    val genre: String,
    val imdb: Double,
    val watchedAtMillis: Long = System.currentTimeMillis(),
    val personalNote: String = "",
    val wantToRewatch: Boolean = false,
    val isFavorite: Boolean = false,
    val posterPath: String? = null
)

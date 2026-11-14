package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedMovieDao {
    @Query("SELECT * FROM watched_movies ORDER BY watchedAtMillis DESC")
    fun getAllWatchedMovies(): Flow<List<WatchedMovieEntity>>

    @Query("SELECT * FROM watched_movies ORDER BY watchedAtMillis DESC")
    suspend fun getAllWatchedMoviesOnce(): List<WatchedMovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchedMovie(movie: WatchedMovieEntity)

    @Query("UPDATE watched_movies SET personalNote = :note WHERE movieId = :movieId")
    suspend fun updatePersonalNote(movieId: String, note: String)

    @Query("UPDATE watched_movies SET wantToRewatch = :wantToRewatch WHERE movieId = :movieId")
    suspend fun updateWantToRewatch(movieId: String, wantToRewatch: Boolean)

    @Query("UPDATE watched_movies SET isFavorite = :isFavorite WHERE movieId = :movieId")
    suspend fun updateFavorite(movieId: String, isFavorite: Boolean)

    @Query("DELETE FROM watched_movies WHERE movieId = :movieId")
    suspend fun deleteWatchedMovieById(movieId: String)

    @Query("DELETE FROM watched_movies")
    suspend fun clearAll()
}

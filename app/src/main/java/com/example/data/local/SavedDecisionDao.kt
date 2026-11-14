package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class MovieSelectionCountProjection(
    val movieId: String,
    val movieTitle: String,
    val movieGenre: String,
    val movieYear: Int,
    val movieImdb: Double,
    val moviePosterPath: String,
    val moviePosterUrl: String,
    val movieColorHex: Long,
    val selectionCount: Int,
    val lastSelectedAtMillis: Long
)

data class ModeSelectionCountProjection(
    val modeLabel: String,
    val selectionCount: Int,
    val totalMinutesSaved: Int
)

@Dao
interface SavedDecisionDao {

    @Query("SELECT * FROM saved_decisions ORDER BY selectedAtMillis DESC")
    fun getAllDecisions(): Flow<List<SavedDecisionEntity>>

    @Query("SELECT * FROM saved_decisions ORDER BY selectedAtMillis DESC")
    suspend fun getAllDecisionsOnce(): List<SavedDecisionEntity>

    @Query(
        """
        SELECT movieId, movieTitle, movieGenre, movieYear, movieImdb,
               moviePosterPath, moviePosterUrl, movieColorHex,
               COUNT(*) AS selectionCount,
               MAX(selectedAtMillis) AS lastSelectedAtMillis
        FROM saved_decisions
        GROUP BY movieId
        ORDER BY selectionCount DESC, lastSelectedAtMillis DESC, movieImdb DESC
        """
    )
    fun observeMostSelectedMovies(): Flow<List<MovieSelectionCountProjection>>

    @Query(
        """
        SELECT movieId, movieTitle, movieGenre, movieYear, movieImdb,
               moviePosterPath, moviePosterUrl, movieColorHex,
               COUNT(*) AS selectionCount,
               MAX(selectedAtMillis) AS lastSelectedAtMillis
        FROM saved_decisions
        GROUP BY movieId
        ORDER BY selectionCount DESC, lastSelectedAtMillis DESC, movieImdb DESC
        """
    )
    suspend fun getMostSelectedMoviesOnce(): List<MovieSelectionCountProjection>

    @Query(
        """
        SELECT mode AS modeLabel,
               COUNT(*) AS selectionCount,
               COALESCE(SUM(minutesSaved), 0) AS totalMinutesSaved
        FROM saved_decisions
        GROUP BY mode
        ORDER BY selectionCount DESC
        """
    )
    fun observeModeSelectionStats(): Flow<List<ModeSelectionCountProjection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDecision(entity: SavedDecisionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDecisions(entities: List<SavedDecisionEntity>)

    @Query("DELETE FROM saved_decisions WHERE id = :decisionId")
    suspend fun deleteDecisionById(decisionId: String)

    @Query("DELETE FROM saved_decisions")
    suspend fun clearAll()
}


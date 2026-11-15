package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.repository.CustomWheelListRepository
import com.example.data.repository.MovieRepository
import com.example.data.repository.RoomRepository
import com.example.data.repository.WatchLaterRepository
import com.example.data.repository.WatchedMovieRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupRestoreRobolectricTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        WatchedMovieRepository.init(context)
        RoomRepository.init(context)
        CustomWheelListRepository.init(context)
        WatchLaterRepository.init(context)
        RoomRepository.resetSavedDecisionsForTests()
        WatchedMovieRepository.clearAllForTests()
        WatchLaterRepository.clearForTests()
    }

    @Test
    fun exportThenRestore_bringsBackHistoryWatchedNotesFavoritesAndWatchLater() = runBlocking {
        val a = MovieRepository.allMovies[0]
        val b = MovieRepository.allMovies[5]
        RoomRepository.recordSavedDecision(a, "Karar Çarkı")
        WatchedMovieRepository.markAsWatched(b)
        WatchedMovieRepository.updateNote(b.id, "Harikaydı")
        WatchedMovieRepository.toggleFavorite(a.id)
        WatchLaterRepository.toggle(b.id)
        Thread.sleep(300) // arka plan Room yazımları

        val json = BackupManager.exportJson(context)
        assertTrue(json.contains("\"format\": \"ne-izlesek-backup\""))

        // Verileri sil, yedekten geri yükle
        RoomRepository.resetSavedDecisionsForTests()
        WatchedMovieRepository.clearAllForTests()
        WatchLaterRepository.clearForTests()
        Thread.sleep(300)
        assertTrue(RoomRepository.savedDecisions.value.isEmpty())

        val result = BackupManager.importJson(context, json)
        assertEquals(1, result.decisions)
        assertTrue(RoomRepository.savedDecisions.value.any { it.movie.id == a.id })
        assertTrue(WatchedMovieRepository.watchedMovieIds.value.contains(b.id))
        assertEquals("Harikaydı", WatchedMovieRepository.watchedMovies.value.first { it.movieId == b.id }.personalNote)
        assertTrue(WatchedMovieRepository.favoriteMovieIds.value.contains(a.id))
        assertTrue(WatchLaterRepository.ids.value.contains(b.id))
    }

    @Test
    fun restore_rejectsForeignOrMalformedFilesAndUnsafeValues() = runBlocking {
        for (bad in listOf("not json", "{\"format\":\"other\",\"version\":1}", "{\"format\":\"ne-izlesek-backup\",\"version\":99}")) {
            try {
                BackupManager.importJson(context, bad)
                fail("Reddedilmeliydi: $bad")
            } catch (e: BackupManager.InvalidBackupException) {
                // beklenen
            }
        }
        val evil = """
            {"format":"ne-izlesek-backup","version":1,
             "savedDecisions":[{"id":"../../x","movieId":"m1","movieTitle":"X","movieYear":2020,"movieGenre":"Dram","dateText":"","mode":""},
                               {"id":"ok1","movieId":"m1","movieTitle":"Tamam","movieYear":2020,"movieGenre":"Dram","dateText":"",
                                "mode":"","moviePosterPath":"https://evil.example/x.png"}],
             "watchLaterIds":["m2","javascript:alert(1)"]}
        """.trimIndent()
        val result = BackupManager.importJson(context, evil)
        assertEquals(1, result.decisions)
        val restored = RoomRepository.savedDecisions.value.first { it.id == "ok1" }
        assertEquals("", restored.movie.posterPath)
        assertFalse(WatchLaterRepository.ids.value.any { it.startsWith("javascript") })
    }
}

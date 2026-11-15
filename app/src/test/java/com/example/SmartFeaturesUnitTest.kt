package com.example

import com.example.data.repository.MovieRepository
import com.example.data.repository.TasteProfile
import com.example.data.repository.WheelPoolSampler
import com.example.data.repository.buildWhyThisMovie
import com.example.data.search.MovieSearch
import kotlin.random.Random
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartFeaturesUnitTest {

    @After
    fun tearDown() = WheelPoolSampler.resetForTests()

    @Test
    fun search_isTurkishInsensitiveAndRanksTitleMatchesFirst() {
        assertEquals("yuzuklerin efendisi", MovieSearch.normalize("Yüzüklerin  EFENDİSİ!"))
        assertEquals("isik", MovieSearch.normalize("IŞIK"))
        val movie = MovieRepository.allMovies.first { it.title.length > 6 }
        val query = MovieSearch.normalize(movie.title).take(6)
        val results = MovieSearch.search(MovieRepository.allMovies, query)
        assertTrue("Ad ile arama sonucu bulunmalı", results.any { it.id == movie.id })
        assertTrue(MovieSearch.search(MovieRepository.allMovies, "a").isEmpty())
        assertTrue(MovieSearch.search(MovieRepository.allMovies, "zzzqqqxx").isEmpty())
    }

    @Test
    fun tasteProfile_weightsLikedGenresUpAndVetoedGenresDown() {
        val profile = TasteProfile(likedGenres = mapOf("Korku" to 4), vetoGenres = mapOf("Komedi" to 5))
        val horror = MovieRepository.allMovies.first { it.genre.contains("Korku") && !it.genre.contains("Komedi") }
        val comedy = MovieRepository.allMovies.first { it.genre.contains("Komedi") && !it.genre.contains("Korku") }
        assertTrue(profile.weightOf(horror) > 1.0)
        assertTrue(profile.weightOf(comedy) < 1.0)
        assertEquals(1.0, TasteProfile(emptyMap(), emptyMap()).weightOf(comedy), 0.0001)
    }

    @Test
    fun weightedSampler_prefersHeavyMovies() {
        val pool = MovieRepository.allMovies.take(60)
        val favored = pool.take(12).map { it.id }.toSet()
        var favoredHits = 0
        repeat(30) { i ->
            WheelPoolSampler.resetForTests()
            WheelPoolSampler.random = Random(i)
            val slices = WheelPoolSampler.sample(pool, weightOf = { if (it.id in favored) 3.0 else 0.25 })
            assertEquals(12, slices.size)
            favoredHits += slices.count { it.id in favored }
        }
        // Ağırlıksız beklenti 12*12/60 = 2.4/çevirme; ağırlıklı çok daha yüksek olmalı
        assertTrue("favoredHits=$favoredHits", favoredHits / 30.0 > 6.0)
    }

    @Test
    fun whyThisMovie_mentionsLikedGenreAndPlatform() {
        val movie = MovieRepository.allMovies.first { it.platforms.isNotEmpty() }
        val genre = movie.genre.split('/', ',').first().trim()
        val text = buildWhyThisMovie(
            movie,
            TasteProfile(likedGenres = mapOf(genre to 3), vetoGenres = emptyMap()),
            selectedPlatforms = setOf(movie.platforms.first())
        )
        assertTrue(text, text.contains("Sevdiğin türden: $genre"))
        assertTrue(text.split(" • ").size <= 3)
    }
}

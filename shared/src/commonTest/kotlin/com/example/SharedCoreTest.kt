package com.example

import com.example.data.model.ContentKind
import com.example.data.repository.GeneratedHighlights
import com.example.data.repository.GeneratedMovies
import com.example.data.repository.GeneratedSeries
import com.example.data.repository.WheelPoolSampler
import com.example.data.search.MovieSearch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Ortak çekirdek hem JVM'de hem iOS simülatöründe aynı sonucu vermeli. */
class SharedCoreTest {
    @Test
    fun catalogIsBundled() {
        assertTrue(GeneratedMovies.movies.size >= 300, "film kataloğu")
        assertTrue(GeneratedSeries.series.all { it.kind == ContentKind.SERIES }, "diziler dizi olarak işaretli")
        assertTrue(GeneratedHighlights.popularMovieTmdbIds.isNotEmpty())
    }

    @Test
    fun turkishInsensitiveNormalize() {
        assertEquals("yuzuklerin efendisi", MovieSearch.normalize("Yüzüklerin Efendisi"))
        assertEquals("ic ice", MovieSearch.normalize("İÇ İÇE!"))
        assertEquals("isik", MovieSearch.normalize("IŞIK"))
        assertEquals("amelie", MovieSearch.normalize("Amélie"))
    }

    @Test
    fun searchFindsWithoutTurkishLetters() {
        val all = GeneratedMovies.movies + GeneratedSeries.series
        val target = all.first { it.title.any { c -> c in "ğüşıöçĞÜŞİÖÇ" } }
        val ascii = MovieSearch.normalize(target.title)
        assertTrue(MovieSearch.search(all, ascii).any { it.id == target.id })
    }

    @Test
    fun samplerKeepsAtMostTwelveDistinctSlices() {
        val slices = WheelPoolSampler.sample(GeneratedMovies.movies)
        assertEquals(WheelPoolSampler.MAX_SLICES, slices.size)
        assertEquals(slices.size, slices.distinctBy { it.id }.size)
    }
}

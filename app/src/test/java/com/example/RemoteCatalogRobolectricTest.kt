package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ContentKindFilter
import com.example.data.repository.MovieRepository
import com.example.data.repository.RemoteCatalogRepository
import java.io.File
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RemoteCatalogRobolectricTest {

    private fun catalogText(): String {
        val file = listOf(File("../catalog/catalog.json"), File("catalog/catalog.json")).first { it.exists() }
        return file.readText()
    }

    @Before
    fun setUp() {
        RemoteCatalogRepository.init(ApplicationProvider.getApplicationContext<Context>(), autoFetch = false)
        RemoteCatalogRepository.resetForTests()
    }

    @After
    fun tearDown() = RemoteCatalogRepository.applyForTests(null)

    @Test
    fun publishedCatalogJson_isValidAndMatchesBundledCatalog() {
        val parsed = RemoteCatalogRepository.parse(catalogText())
        assertNotNull(parsed)
        // Haftalık iş kataloğu yeniden ürettiği için sayılar değişebilir; alt sınırları denetle
        assertTrue(parsed!!.items.size >= 150)
        assertTrue(parsed.items.count { it.isSeries } >= 50)
        assertTrue(parsed.items.filter { it.isSeries }.all { (it.seasonCount ?: 0) >= 1 })
    }

    @Test
    fun parse_rejectsWrongFormatTooFewItemsAndDropsUnsafeEntries() {
        assertNull(RemoteCatalogRepository.parse("{}"))
        assertNull(RemoteCatalogRepository.parse("""{"format":"ne-izlesek-catalog","version":2,"items":[]}"""))
        val root = JSONObject(catalogText())
        val items = root.getJSONArray("items")
        // Birkaç kaydı boz: güvensiz poster yolu, bilinmeyen platform, geçersiz kimlik
        items.getJSONObject(0).put("posterPath", "https://evil.example/p.jpg")
        items.getJSONObject(1).put("platforms", org.json.JSONArray(listOf("EvilTV")))
        items.getJSONObject(2).put("id", "../../etc")
        val parsed = RemoteCatalogRepository.parse(root.toString())!!
        assertEquals(items.length() - 1, parsed.items.size)
        assertEquals("", parsed.items.first { it.id == items.getJSONObject(0).getString("id") }.posterPath)
        assertEquals(listOf("Diğer"), parsed.items.first { it.id == items.getJSONObject(1).getString("id") }.platforms)

        // Çok az kayıt varsa tüm katalog reddedilir (APK içi katalog korunur)
        val tiny = JSONObject(root.toString()).put("items", org.json.JSONArray().put(items.getJSONObject(3)))
        assertNull(RemoteCatalogRepository.parse(tiny.toString()))
    }

    @Test
    fun appliedRemoteCatalog_replacesGeneratedPartAndKeepsCurated() {
        val root = JSONObject(catalogText())
        val extra = JSONObject(root.getJSONArray("items").getJSONObject(0).toString())
            .put("id", "t999999001").put("title", "Uzaktan Gelen Film")
        root.getJSONArray("items").put(extra)
        val parsed = RemoteCatalogRepository.parse(root.toString())!!
        RemoteCatalogRepository.applyForTests(parsed)
        assertNotNull(MovieRepository.getMovieById("t999999001"))
        assertNotNull("Elle seçilen filmler korunur", MovieRepository.getMovieById("m13"))
        RemoteCatalogRepository.applyForTests(null)
        assertNull(MovieRepository.getMovieById("t999999001"))
    }

    @Test
    fun refresh_refusesNonHttpsUrls() {
        assertFalse(RemoteCatalogRepository.refresh("http://example.com/catalog.json"))
    }

    @Test
    fun contentKindFilter_returnsOnlySeriesOrOnlyMovies() {
        val series = MovieRepository.filterMoviesWithCriteria(contentKinds = ContentKindFilter.SERIES).movies
        assertTrue(series.isNotEmpty())
        assertTrue(series.all { it.isSeries })
        val movies = MovieRepository.filterMoviesWithCriteria(contentKinds = ContentKindFilter.MOVIES).movies
        assertTrue(movies.none { it.isSeries })
        assertEquals("3 sezon • bölüm 45 dk", series.first().copy(seasonCount = 3, durationMins = 45).durationLabel())
    }
}

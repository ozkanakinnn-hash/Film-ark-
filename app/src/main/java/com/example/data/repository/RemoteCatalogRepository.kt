package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.BuildConfig
import com.example.data.model.AudioLanguageOption
import com.example.data.model.AvailablePlatforms
import com.example.data.model.ContentKind
import com.example.data.model.Mood
import com.example.data.model.Movie
import com.example.data.model.OTHER_PLATFORM_NAME
import com.example.data.model.UpcomingRelease
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/** Doğrulanmış uzaktan katalog. Bölümler TMDB kimlik listeleridir (yoksa boş). */
class RemoteCatalog(
    val generatedAt: String,
    val items: List<Movie>,
    val popularMovieTmdbIds: List<Int> = emptyList(),
    val popularSeriesTmdbIds: List<Int> = emptyList(),
    val worstMovieTmdbIds: List<Int> = emptyList(),
    val upcoming: List<UpcomingRelease> = emptyList(),
    val popularMovieBackdrops: List<String> = emptyList(),
    val popularSeriesBackdrops: List<String> = emptyList()
)

/**
 * Haftalık katalog güncellemesi.
 *
 * Katalog (TMDB'den derlenmiş film + dizi listesi) GitHub deposundaki herkese açık bir JSON dosyasıdır
 * (`catalog/catalog.json`, `tools/export_catalog_json.py` üretir). Uygulama:
 * - haftada en fazla bir kez, yalnızca HTTPS ile indirir (en fazla 3 MB, 15 sn zaman aşımı),
 * - biçimi, alanları ve değer aralıklarını doğrular; tek bir hatalı kayıt atlanır, yapı bozuksa ya da
 *   çok az kayıt varsa dosyanın tamamı reddedilir,
 * - başarılı indirmeyi cihazda saklar; internet yoksa ya da indirme başarısızsa APK içindeki katalog kullanılır.
 * Hiçbir kullanıcı verisi gönderilmez; istek bir dosya indirmekten ibarettir. API anahtarı yoktur.
 */
object RemoteCatalogRepository {
    const val FORMAT = "ne-izlesek-catalog"
    const val VERSION = 1
    private const val MAX_BYTES = 3 * 1024 * 1024
    private const val MIN_ITEMS = 50
    private const val MAX_ITEMS = 3_000
    private const val WEEK_MS = 7L * 24 * 60 * 60 * 1000
    private const val RETRY_MS = 24L * 60 * 60 * 1000
    private const val CACHE_FILE = "remote_catalog.json"
    private const val PREFS = "remote_catalog_prefs"
    private const val KEY_LAST_SUCCESS = "last_success_ms"
    private const val KEY_LAST_ATTEMPT = "last_attempt_ms"
    private const val KEY_ETAG = "etag"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var prefs: SharedPreferences? = null
    private var cacheFile: File? = null

    private val _catalog = MutableStateFlow<RemoteCatalog?>(null)
    val catalog: StateFlow<RemoteCatalog?> = _catalog.asStateFlow()

    fun init(context: Context, autoFetch: Boolean = Build.FINGERPRINT != "robolectric") {
        if (prefs != null) return
        val app = context.applicationContext
        prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        cacheFile = File(app.filesDir, CACHE_FILE)
        scope.launch {
            cacheFile?.takeIf { it.exists() && it.length() in 1..MAX_BYTES }?.let { file ->
                runCatching { parse(file.readText()) }.getOrNull()?.let { _catalog.value = it }
            }
            if (autoFetch && isDue(System.currentTimeMillis())) refresh()
        }
    }

    fun isDue(now: Long): Boolean {
        val p = prefs ?: return false
        val lastSuccess = p.getLong(KEY_LAST_SUCCESS, 0L)
        val lastAttempt = p.getLong(KEY_LAST_ATTEMPT, 0L)
        return now - lastSuccess >= WEEK_MS && now - lastAttempt >= RETRY_MS
    }

    /** İndirir, doğrular ve kaydeder. Başarılıysa true. */
    fun refresh(url: String = BuildConfig.REMOTE_CATALOG_URL): Boolean {
        val p = prefs ?: return false
        p.edit().putLong(KEY_LAST_ATTEMPT, System.currentTimeMillis()).apply()
        if (!url.startsWith("https://")) return false
        val result = runCatching {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 15_000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json")
                p.getString(KEY_ETAG, null)?.takeIf { _catalog.value != null }?.let { setRequestProperty("If-None-Match", it) }
            }
            try {
                when (conn.responseCode) {
                    HttpURLConnection.HTTP_NOT_MODIFIED -> true
                    HttpURLConnection.HTTP_OK -> {
                        if (conn.contentLengthLong > MAX_BYTES) return@runCatching false
                        val bytes = conn.inputStream.use { input ->
                            val out = java.io.ByteArrayOutputStream()
                            val buf = ByteArray(16 * 1024)
                            while (true) {
                                val n = input.read(buf)
                                if (n < 0) break
                                out.write(buf, 0, n)
                                if (out.size() > MAX_BYTES) return@runCatching false
                            }
                            out.toByteArray()
                        }
                        val text = String(bytes, Charsets.UTF_8)
                        val parsed = parse(text) ?: return@runCatching false
                        cacheFile?.writeText(text)
                        conn.getHeaderField("ETag")?.let { p.edit().putString(KEY_ETAG, it.take(200)).apply() }
                        _catalog.value = parsed
                        true
                    }
                    else -> false
                }
            } finally {
                conn.disconnect()
            }
        }.getOrDefault(false)
        if (result) p.edit().putLong(KEY_LAST_SUCCESS, System.currentTimeMillis()).apply()
        return result
    }

    // ---------------- Doğrulama ----------------

    private val ID = Regex("^(t|tv)[0-9]{1,9}$")
    private val POSTER = Regex("^/[A-Za-z0-9_.-]{1,80}\\.(jpg|jpeg|png|webp)$")
    private val TRAILER = Regex("^[A-Za-z0-9_-]{6,20}$")
    private val MOODS = Mood.entries.map { it.id }.toSet()
    private val PLATFORMS = AvailablePlatforms.map { it.name }.toSet() + OTHER_PLATFORM_NAME

    /** Biçim geçersizse null döner. Geçersiz tekil kayıtlar atlanır. */
    fun parse(text: String): RemoteCatalog? {
        if (text.length > MAX_BYTES) return null
        val root = runCatching { JSONObject(text) }.getOrNull() ?: return null
        if (root.optString("format") != FORMAT || root.optInt("version", -1) != VERSION) return null
        val arr = root.optJSONArray("items") ?: return null
        if (arr.length() > MAX_ITEMS) return null
        val items = (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let { runCatching { toMovie(it) }.getOrNull() } }
            .distinctBy { it.id }
        if (items.size < MIN_ITEMS) return null
        val sections = root.optJSONObject("sections")
        fun ids(key: String): List<Int> {
            val a = sections?.optJSONArray(key) ?: return emptyList()
            return (0 until minOf(a.length(), 200)).mapNotNull { a.optInt(it, 0).takeIf { id -> id > 0 } }.distinct()
        }
        fun paths(key: String): List<String> {
            val a = sections?.optJSONArray(key) ?: return emptyList()
            return (0 until minOf(a.length(), 10)).mapNotNull { a.optString(it).takeIf { p -> POSTER.matches(p) } }
        }
        val upcomingArr = root.optJSONArray("upcoming")
        val upcoming = if (upcomingArr == null) emptyList() else (0 until minOf(upcomingArr.length(), 100))
            .mapNotNull { i -> upcomingArr.optJSONObject(i)?.let { runCatching { toUpcoming(it) }.getOrNull() } }
            .distinctBy { it.id }
        return RemoteCatalog(
            generatedAt = root.optString("generatedAt").take(40),
            items = items,
            popularMovieTmdbIds = ids("popularMovies"),
            popularSeriesTmdbIds = ids("popularSeries"),
            worstMovieTmdbIds = ids("worstMovies"),
            upcoming = upcoming,
            popularMovieBackdrops = paths("popularMovieBackdrops"),
            popularSeriesBackdrops = paths("popularSeriesBackdrops")
        )
    }

    private fun JSONObject.text(key: String, max: Int): String? =
        optString(key, "").trim().takeIf { it.length <= max }

    private fun toMovie(o: JSONObject): Movie? {
        val id = o.optString("id").takeIf { ID.matches(it) } ?: return null
        val kind = when (o.optString("kind", "movie")) {
            "movie" -> ContentKind.MOVIE
            "series" -> ContentKind.SERIES
            else -> return null
        }
        val title = o.text("title", 120)?.takeIf { it.isNotEmpty() } ?: return null
        val year = o.optInt("year", 0).takeIf { it in 1888..2100 } ?: return null
        val duration = o.optInt("durationMins", 0).takeIf { it in 1..600 } ?: return null
        val rating = o.optDouble("imdb", -1.0).takeIf { it in 0.0..10.0 } ?: return null
        val mood = o.optString("moodTag").takeIf { it in MOODS } ?: return null
        val platforms = o.optJSONArray("platforms").strings().filter { it in PLATFORMS }.distinct()
            .ifEmpty { listOf(OTHER_PLATFORM_NAME) }
        val audio = o.optJSONArray("audio").strings().mapNotNull { a ->
            when (a) {
                "dubbed" -> AudioLanguageOption.DUBBED.id
                "subtitled" -> AudioLanguageOption.SUBTITLED.id
                else -> null
            }
        }.distinct().ifEmpty { listOf(AudioLanguageOption.SUBTITLED.id) }
        val poster = o.optString("posterPath").takeIf { POSTER.matches(it) }.orEmpty()
        val trailer = o.optString("trailerKey").takeIf { TRAILER.matches(it) }
        val tmdbId = o.optInt("tmdbId", 0).takeIf { it > 0 }
        val color = o.optLong("colorHex", 0xFF8B5CF6L).let { if (it in 0xFF000000L..0xFFFFFFFFL) it else 0xFF8B5CF6L }
        return Movie(
            id = id,
            title = title,
            originalTitle = o.text("originalTitle", 120).orEmpty(),
            year = year,
            durationMins = duration,
            imdb = rating,
            moodTag = mood,
            platforms = platforms,
            genre = o.text("genre", 60)?.ifBlank { null } ?: "Dram",
            logline = o.text("logline", 600).orEmpty(),
            whyWatch = o.text("whyWatch", 300).orEmpty(),
            posterPath = poster,
            tmdbId = tmdbId,
            trailerKey = trailer,
            colorHex = color,
            audioOptions = audio,
            isCultOrMostWatched = o.optBoolean("isCult", false),
            kind = kind,
            seasonCount = if (kind == ContentKind.SERIES) o.optInt("seasonCount", 1).coerceIn(1, 99) else null
        )
    }

    private val UPCOMING_ID = Regex("^u[0-9]{1,9}$")
    private val ISO_DATE = Regex("^20[0-9]{2}-[01][0-9]-[0-3][0-9]$")

    private fun toUpcoming(o: JSONObject): UpcomingRelease? {
        val id = o.optString("id").takeIf { UPCOMING_ID.matches(it) } ?: return null
        val title = o.text("title", 120)?.takeIf { it.isNotEmpty() } ?: return null
        val date = o.optString("releaseDate").takeIf { ISO_DATE.matches(it) } ?: return null
        return UpcomingRelease(
            id = id,
            tmdbId = o.optInt("tmdbId", 0).takeIf { it > 0 } ?: return null,
            title = title,
            originalTitle = o.text("originalTitle", 120).orEmpty(),
            releaseDate = date,
            durationMins = o.optInt("durationMins", 0).coerceIn(0, 600),
            genre = o.text("genre", 60).orEmpty(),
            logline = o.text("logline", 600).orEmpty(),
            posterPath = o.optString("posterPath").takeIf { POSTER.matches(it) }.orEmpty(),
            trailerKey = o.optString("trailerKey").takeIf { TRAILER.matches(it) },
            colorHex = o.optLong("colorHex", 0xFF8B5CF6L).let { if (it in 0xFF000000L..0xFFFFFFFFL) it else 0xFF8B5CF6L }
        )
    }

    private fun JSONArray?.strings(): List<String> =
        if (this == null) emptyList() else (0 until minOf(length(), 20)).mapNotNull { optString(it, null) }

    fun resetForTests() {
        _catalog.value = null
        prefs?.edit()?.clear()?.apply()
        runCatching { cacheFile?.delete() }
    }

    /** Testler için: doğrulanmış kataloğu doğrudan uygular. */
    fun applyForTests(catalog: RemoteCatalog?) {
        _catalog.value = catalog
    }
}

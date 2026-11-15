package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.CustomWheelListEntity
import com.example.data.local.SavedDecisionEntity
import com.example.data.local.WatchedMovieEntity
import com.example.data.repository.CustomWheelListRepository
import com.example.data.repository.RoomRepository
import com.example.data.repository.TasteProfileRepository
import com.example.data.repository.WatchLaterRepository
import com.example.data.repository.WatchedMovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Kullanıcının kendi seçtiği dosyaya yedek alma / geri yükleme (Storage Access Framework).
 *
 * - Yedek yalnızca kullanıcının seçtiği konuma yazılır; uygulama hiçbir sunucuya göndermez.
 * - Geri yükleme BİRLEŞTİRİR: mevcut veriler silinmez, yedekteki kayıtlar eklenir / güncellenir.
 * - Dosya boyutu ve kayıt sayıları sınırlıdır; bozuk ya da başka uygulamaya ait dosyalar reddedilir.
 */
object BackupManager {
    const val FORMAT = "ne-izlesek-backup"
    const val VERSION = 1
    const val MAX_BYTES = 5 * 1024 * 1024
    private const val MAX_ITEMS = 5_000
    private const val MAX_TEXT = 4_000

    data class RestoreResult(
        val decisions: Int,
        val watched: Int,
        val customLists: Int,
        val favorites: Int,
        val watchLater: Int
    ) {
        val total: Int get() = decisions + watched + customLists + favorites + watchLater
        fun summary(): String =
            "$decisions karar, $watched izlenen, $favorites favori, $customLists liste, $watchLater daha sonra izle geri yüklendi"
    }

    class InvalidBackupException(message: String) : Exception(message)

    fun suggestedFileName(nowMillis: Long = System.currentTimeMillis()): String {
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(nowMillis))
        return "ne-izlesek-yedek-$date.json"
    }

    suspend fun exportJson(context: Context): String = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val root = JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("createdAtMillis", System.currentTimeMillis())
        root.put("savedDecisions", JSONArray().apply {
            db.savedDecisionDao().getAllDecisionsOnce().forEach { put(it.toJson()) }
        })
        root.put("watchedMovies", JSONArray().apply {
            db.watchedMovieDao().getAllWatchedMoviesOnce().forEach { put(it.toJson()) }
        })
        root.put("customLists", JSONArray().apply {
            db.customWheelListDao().getAllListsOnce().forEach { put(it.toJson()) }
        })
        root.put("favoriteIds", JSONArray(WatchedMovieRepository.favoriteMovieIds.value.sorted()))
        root.put("watchLaterIds", JSONArray(WatchLaterRepository.ids.value))
        root.put("vetoGenres", JSONObject(TasteProfileRepository.vetoGenres() as Map<*, *>))
        root.toString(2)
    }

    suspend fun exportTo(context: Context, uri: Uri) {
        val json = exportJson(context)
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                ?: throw InvalidBackupException("Dosyaya yazılamadı")
        }
    }

    suspend fun importFrom(context: Context, uri: Uri): RestoreResult {
        val text = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bytes = input.readNBytesCompat(MAX_BYTES + 1)
                if (bytes.size > MAX_BYTES) throw InvalidBackupException("Dosya çok büyük (en fazla 5 MB)")
                String(bytes, Charsets.UTF_8)
            } ?: throw InvalidBackupException("Dosya açılamadı")
        }
        return importJson(context, text)
    }

    suspend fun importJson(context: Context, text: String): RestoreResult = withContext(Dispatchers.IO) {
        if (text.length > MAX_BYTES) throw InvalidBackupException("Dosya çok büyük")
        val root = runCatching { JSONObject(text) }.getOrElse { throw InvalidBackupException("Geçerli bir yedek dosyası değil") }
        if (root.optString("format") != FORMAT) throw InvalidBackupException("Bu dosya bir Ne İzlesek? yedeği değil")
        val version = root.optInt("version", -1)
        if (version < 1 || version > VERSION) throw InvalidBackupException("Bu yedek daha yeni bir sürümle alınmış; uygulamayı güncelle")

        val decisions = root.optJSONArray("savedDecisions").objects().mapNotNull { runCatching { it.toDecisionEntity() }.getOrNull() }
        val watched = root.optJSONArray("watchedMovies").objects().mapNotNull { runCatching { it.toWatchedEntity() }.getOrNull() }
        val lists = root.optJSONArray("customLists").objects().mapNotNull { runCatching { it.toListEntity() }.getOrNull() }
        val favorites = root.optJSONArray("favoriteIds").strings().filter { it.isSafeId() }.toSet()
        val watchLater = root.optJSONArray("watchLaterIds").strings().filter { it.isSafeId() }
        val vetoGenres = root.optJSONObject("vetoGenres")?.let { obj ->
            obj.keys().asSequence().take(200).associateWith { obj.optInt(it, 0).coerceIn(0, 1000) }
        }.orEmpty()

        val db = AppDatabase.getInstance(context)
        if (decisions.isNotEmpty()) db.savedDecisionDao().upsertDecisions(decisions)
        watched.forEach { db.watchedMovieDao().insertWatchedMovie(it) }
        if (lists.isNotEmpty()) db.customWheelListDao().upsertLists(lists)
        favorites.forEach { id -> runCatching { db.watchedMovieDao().updateFavorite(id, true) } }

        RoomRepository.reloadAfterRestore()
        WatchedMovieRepository.reloadAfterRestore(favorites)
        CustomWheelListRepository.reloadAfterRestore()
        WatchLaterRepository.mergeRestored(watchLater)
        if (vetoGenres.isNotEmpty()) {
            val merged = TasteProfileRepository.vetoGenres().toMutableMap()
            vetoGenres.forEach { (g, c) -> merged[g] = maxOf(merged[g] ?: 0, c) }
            TasteProfileRepository.restoreVetoGenres(merged)
        }
        RestoreResult(decisions.size, watched.size, lists.size, favorites.size, watchLater.size)
    }

    // ---- JSON eşlemeleri ----

    private fun SavedDecisionEntity.toJson() = JSONObject()
        .put("id", id).put("movieId", movieId).put("movieTitle", movieTitle)
        .put("movieOriginalTitle", movieOriginalTitle).put("movieYear", movieYear)
        .put("movieDurationMins", movieDurationMins).put("movieImdb", movieImdb)
        .put("movieMoodTag", movieMoodTag).put("moviePlatformsCsv", moviePlatformsCsv)
        .put("movieGenre", movieGenre).put("movieLogline", movieLogline).put("movieWhyWatch", movieWhyWatch)
        .put("moviePosterPath", moviePosterPath).put("movieColorHex", movieColorHex)
        .put("dateText", dateText).put("minutesSaved", minutesSaved).put("mode", mode)
        .put("selectedAtMillis", selectedAtMillis)

    private fun JSONObject.toDecisionEntity(): SavedDecisionEntity? {
        val id = str("id"); val movieId = str("movieId")
        if (!id.isSafeId() || !movieId.isSafeId()) return null
        val posterPath = str("moviePosterPath").takeIf { it.isEmpty() || it.isPosterPath() }.orEmpty()
        return SavedDecisionEntity(
            id = id, movieId = movieId, movieTitle = str("movieTitle").ifBlank { return null },
            movieOriginalTitle = str("movieOriginalTitle"), movieYear = optInt("movieYear", 0).coerceIn(1888, 2100),
            movieDurationMins = optInt("movieDurationMins", 0).coerceIn(0, 1000),
            movieImdb = optDouble("movieImdb", 0.0).takeIf { !it.isNaN() }?.coerceIn(0.0, 10.0) ?: 0.0,
            movieMoodTag = str("movieMoodTag"), moviePlatformsCsv = str("moviePlatformsCsv"),
            movieGenre = str("movieGenre"), movieLogline = str("movieLogline"), movieWhyWatch = str("movieWhyWatch"),
            moviePosterPath = posterPath, moviePosterUrl = "",
            movieColorHex = optLong("movieColorHex", 0xFF8B5CF6L),
            dateText = str("dateText"), minutesSaved = optInt("minutesSaved", 45).coerceIn(0, 600),
            mode = str("mode").ifBlank { "Karar Çarkı" }, selectedAtMillis = optLong("selectedAtMillis", 0L).coerceAtLeast(0L)
        )
    }

    private fun WatchedMovieEntity.toJson() = JSONObject()
        .put("movieId", movieId).put("title", title).put("year", year).put("durationMins", durationMins)
        .put("genre", genre).put("imdb", imdb).put("watchedAtMillis", watchedAtMillis)
        .put("personalNote", personalNote).put("wantToRewatch", wantToRewatch).put("isFavorite", isFavorite)
        .put("posterPath", posterPath ?: "")

    private fun JSONObject.toWatchedEntity(): WatchedMovieEntity? {
        val id = str("movieId")
        if (!id.isSafeId()) return null
        return WatchedMovieEntity(
            movieId = id, title = str("title").ifBlank { return null }, year = optInt("year", 0).coerceIn(1888, 2100),
            durationMins = optInt("durationMins", 0).coerceIn(0, 1000), genre = str("genre"),
            imdb = optDouble("imdb", 0.0).takeIf { !it.isNaN() }?.coerceIn(0.0, 10.0) ?: 0.0,
            watchedAtMillis = optLong("watchedAtMillis", 0L).coerceAtLeast(0L),
            personalNote = str("personalNote"), wantToRewatch = optBoolean("wantToRewatch", false),
            isFavorite = optBoolean("isFavorite", false),
            posterPath = str("posterPath").takeIf { it.isPosterPath() }
        )
    }

    private fun CustomWheelListEntity.toJson() = JSONObject()
        .put("id", id).put("name", name).put("emoji", emoji).put("description", description)
        .put("movieIdsCsv", movieIdsCsv).put("customMoviesJson", customMoviesJson)
        .put("accentColorHex", accentColorHex).put("isPreset", isPreset).put("updatedAtMillis", updatedAtMillis)

    private fun JSONObject.toListEntity(): CustomWheelListEntity? {
        val id = str("id")
        if (!id.isSafeId()) return null
        return CustomWheelListEntity(
            id = id, name = str("name").ifBlank { return null }.take(80), emoji = str("emoji").take(16),
            description = str("description"),
            movieIdsCsv = str("movieIdsCsv", 200_000), customMoviesJson = str("customMoviesJson", 500_000).ifBlank { "[]" },
            accentColorHex = optLong("accentColorHex", 0xFF8B5CF6L), isPreset = optBoolean("isPreset", false),
            updatedAtMillis = optLong("updatedAtMillis", System.currentTimeMillis())
        )
    }

    // ---- yardımcılar ----

    private fun JSONObject.str(key: String, max: Int = MAX_TEXT): String =
        (if (isNull(key)) "" else optString(key, "")).take(max)

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until minOf(length(), MAX_ITEMS)).mapNotNull { optJSONObject(it) }

    private fun JSONArray?.strings(): List<String> =
        if (this == null) emptyList() else (0 until minOf(length(), MAX_ITEMS)).mapNotNull { optString(it, null) }

    private val SAFE_ID = Regex("^[A-Za-z0-9_.:-]{1,80}$")
    private fun String.isSafeId() = SAFE_ID.matches(this)

    /** Yalnızca TMDB görsel yolu ("/abc.jpg") kabul edilir; harici adresler alınmaz. */
    private val POSTER_PATH = Regex("^/[A-Za-z0-9_.-]{1,80}$")
    private fun String.isPosterPath() = POSTER_PATH.matches(this)

    private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(16 * 1024)
        while (out.size() < limit) {
            val n = read(buf, 0, minOf(buf.size, limit - out.size()))
            if (n < 0) break
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }
}

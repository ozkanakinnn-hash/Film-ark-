package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.CustomWheelListDao
import com.example.data.local.CustomWheelListEntity
import com.example.data.model.CustomWheelList
import com.example.data.model.Mood
import com.example.data.model.Movie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

object CustomWheelListRepository {

    private const val PREF_NAME = "ne_izlesek_custom_lists_prefs"
    private const val KEY_PRESETS_SEEDED = "starter_presets_seeded_v1"
    private const val KEY_PRESETS_V2_ADDED = "catalog_presets_added_v2"

    // Yazımlar sırayla: art arda yapılan işlemler (ör. izlendi + not) birbirini ezmesin
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    @Volatile
    private var dao: CustomWheelListDao? = null

    @Volatile
    private var prefs: SharedPreferences? = null

    private val _customLists = MutableStateFlow<List<CustomWheelList>>(buildDefaultStarterPresets())
    val customLists: StateFlow<List<CustomWheelList>> = _customLists.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
        if (dao != null) return
        val localDao = AppDatabase.getInstance(context).customWheelListDao()
        dao = localDao

        ioScope.launch {
            val persisted = localDao.getAllListsOnce()
            val alreadySeeded = prefs?.getBoolean(KEY_PRESETS_SEEDED, false) == true
            if (persisted.isNotEmpty()) {
                val decoded = persisted.mapNotNull { decodeEntity(it) }
                val merged = (decoded + _customLists.value.filter { mem -> decoded.none { it.id == mem.id } })
                    .distinctBy { it.id }
                    .sortedWith(compareByDescending<CustomWheelList> { !it.isPreset }.thenByDescending { it.updatedAtMillis })
                _customLists.value = merged
                prefs?.edit()?.putBoolean(KEY_PRESETS_SEEDED, true)?.apply()
                // v2: yeni hazır listeleri mevcut kullanıcılara bir kez ekle (silinenler geri gelmez)
                if (prefs?.getBoolean(KEY_PRESETS_V2_ADDED, false) != true) {
                    val added = buildCatalogPresets(1_700_000_000_000L).filter { p -> merged.none { it.id == p.id } }
                    if (added.isNotEmpty()) {
                        _customLists.value = (merged + added)
                            .sortedWith(compareByDescending<CustomWheelList> { !it.isPreset }.thenByDescending { it.updatedAtMillis })
                        localDao.upsertLists(added.map { encodeEntity(it) })
                    }
                    prefs?.edit()?.putBoolean(KEY_PRESETS_V2_ADDED, true)?.apply()
                }
            } else if (!alreadySeeded) {
                val starters = buildDefaultStarterPresets()
                _customLists.value = starters
                localDao.upsertLists(starters.map { encodeEntity(it) })
                prefs?.edit()?.putBoolean(KEY_PRESETS_SEEDED, true)?.apply()
            }
        }
    }

    fun getListById(listId: String): CustomWheelList? {
        if (SmartWheelLists.isSmart(listId)) return SmartWheelLists.byId(listId)
        return _customLists.value.find { it.id == listId }
    }

    fun findCustomMovieById(movieId: String): Movie? {
        return _customLists.value
            .asSequence()
            .flatMap { it.movies.asSequence() }
            .firstOrNull { it.id == movieId }
    }

    fun createCustomMovie(
        title: String,
        genre: String = "Özel Seçki",
        year: Int = 2024,
        imdb: Double = 7.8,
        accentColorHex: Long = 0xFF8B5CF6
    ): Movie {
        val cleanTitle = title.trim()
        val id = "custom_mov_${UUID.randomUUID().toString().take(8)}"
        return Movie(
            id = id,
            title = cleanTitle,
            originalTitle = cleanTitle,
            year = year,
            durationMins = 115,
            imdb = imdb,
            moodTag = Mood.CULT_AWARD.id,
            platforms = listOf("Özel Liste"),
            genre = genre.ifBlank { "Özel Seçki" },
            logline = "$cleanTitle — Kullanıcının kendi çark kategorisine eklediği özel film seçimi.",
            whyWatch = "Senin oluşturduğun özel çark listesinden geliyor!",
            posterPath = "",
            posterUrl = "",
            colorHex = accentColorHex
        )
    }

    fun saveCustomList(
        editingListId: String? = null,
        name: String,
        emoji: String,
        description: String,
        movies: List<Movie>,
        accentColorHex: Long = 0xFF8B5CF6
    ): CustomWheelList {
        val cleanName = name.trim()
        val cleanEmoji = emoji.trim().ifBlank { "🎬" }
        val cleanDesc = description.trim()
        val distinctMovies = movies.distinctBy { it.id }
        val existing = editingListId?.let { id -> _customLists.value.find { it.id == id } }

        val saved = CustomWheelList(
            id = existing?.id ?: "custom_list_${UUID.randomUUID().toString().take(8)}",
            name = cleanName,
            emoji = cleanEmoji,
            description = cleanDesc,
            movies = distinctMovies,
            accentColorHex = accentColorHex,
            isPreset = existing?.isPreset ?: false,
            updatedAtMillis = System.currentTimeMillis()
        )

        val currentWithoutSaved = _customLists.value.filterNot { it.id == saved.id }
        _customLists.value = (listOf(saved) + currentWithoutSaved)
            .sortedWith(compareByDescending<CustomWheelList> { !it.isPreset }.thenByDescending { it.updatedAtMillis })

        dao?.let { localDao ->
            ioScope.launch {
                localDao.upsertList(encodeEntity(saved))
            }
        }
        prefs?.edit()?.putBoolean(KEY_PRESETS_SEEDED, true)?.apply()
        return saved
    }

    fun addMovieToList(listId: String, movie: Movie): CustomWheelList? {
        val existing = getListById(listId) ?: return null
        if (existing.movies.any { it.id == movie.id }) return existing
        return saveCustomList(
            editingListId = existing.id,
            name = existing.name,
            emoji = existing.emoji,
            description = existing.description,
            movies = existing.movies + movie,
            accentColorHex = existing.accentColorHex
        )
    }

    fun removeMovieFromList(listId: String, movieId: String): CustomWheelList? {
        val existing = getListById(listId) ?: return null
        val updatedMovies = existing.movies.filterNot { it.id == movieId }
        return saveCustomList(
            editingListId = existing.id,
            name = existing.name,
            emoji = existing.emoji,
            description = existing.description,
            movies = updatedMovies,
            accentColorHex = existing.accentColorHex
        )
    }

    fun deleteList(listId: String) {
        _customLists.value = _customLists.value.filterNot { it.id == listId }
        dao?.let { localDao ->
            ioScope.launch {
                localDao.deleteListById(listId)
            }
        }
    }

    fun restoreStarterPresets() {
        val starters = buildDefaultStarterPresets()
        val existingById = _customLists.value.associateBy { it.id }.toMutableMap()
        starters.forEach { preset ->
            if (!existingById.containsKey(preset.id)) {
                existingById[preset.id] = preset
            }
        }
        val restored = existingById.values
            .sortedWith(compareByDescending<CustomWheelList> { !it.isPreset }.thenByDescending { it.updatedAtMillis })
        _customLists.value = restored

        dao?.let { localDao ->
            ioScope.launch {
                localDao.upsertLists(starters.map { encodeEntity(it) })
            }
        }
    }

    /** Yedekten geri yüklemeden sonra listeleri veritabanından yeniden okur. */
    suspend fun reloadAfterRestore() {
        val localDao = dao ?: return
        val decoded = localDao.getAllListsOnce().mapNotNull { decodeEntity(it) }
        _customLists.value = decoded
            .distinctBy { it.id }
            .sortedWith(compareByDescending<CustomWheelList> { !it.isPreset }.thenByDescending { it.updatedAtMillis })
        prefs?.edit()?.putBoolean(KEY_PRESETS_SEEDED, true)?.apply()
    }

    fun resetForTests() {
        val starters = buildDefaultStarterPresets()
        _customLists.value = starters
        prefs?.edit()?.clear()?.apply()
        dao?.let { localDao ->
            ioScope.launch {
                localDao.clearAll()
                localDao.upsertLists(starters.map { encodeEntity(it) })
            }
        }
    }

    private fun buildDefaultStarterPresets(): List<CustomWheelList> {
        fun moviesByIds(vararg ids: String): List<Movie> =
            ids.mapNotNull { MovieRepository.getMovieById(it) }

        val now = 1_700_000_000_000L
        return listOf(
            CustomWheelList(
                id = "preset_korku_gecesi",
                name = "Korku Gecesi",
                emoji = "👻",
                description = "Işıkları kapatıp nabzı yükselten tüyler ürpertici gece seçkisi",
                movies = moviesByIds("m19", "m20", "m6", "m5", "m4"),
                accentColorHex = 0xFFDC2626,
                isPreset = true,
                updatedAtMillis = now + 3L
            ),
            CustomWheelList(
                id = "preset_aile_dostu",
                name = "Aile Dostu",
                emoji = "👨‍👩‍👧‍👦",
                description = "Her yaşa uygun, sıcacık ve birlikte keyifle izlenen filmler",
                movies = moviesByIds("m23", "m21", "m22", "m9", "m10"),
                accentColorHex = 0xFF10B981,
                isPreset = true,
                updatedAtMillis = now + 2L
            ),
            CustomWheelList(
                id = "preset_klasikler",
                name = "Klasikler",
                emoji = "🎞️",
                description = "Yıllar geçse de eskimeyen, sinema tarihinin kült başyapıtları",
                movies = moviesByIds("m13", "m14", "m15", "m12", "m8", "m17"),
                accentColorHex = 0xFFE9B949,
                isPreset = true,
                updatedAtMillis = now + 1L
            )
        ) + buildCatalogPresets(now)
    }

    /** Katalogdan türetilen ek hazır listeler (her biri en iyi puanlı 10 film, yeniden eskiye). */
    private fun buildCatalogPresets(now: Long): List<CustomWheelList> {
        val films = MovieRepository.allMovies.filter { !it.isSeries && it.imdb >= 6.5 }
        fun pick(predicate: (Movie) -> Boolean): List<Movie> = films.filter(predicate)
            .sortedByDescending { it.imdb }
            .take(10)
            .sortedWith(compareByDescending<Movie> { it.year }.thenByDescending { it.imdb })
        data class Spec(val id: String, val name: String, val emoji: String, val desc: String, val color: Long, val filter: (Movie) -> Boolean)
        val specs = listOf(
            Spec("preset_romantik_aksam", "Romantik Akşam", "❤️", "Battaniye altında izlenecek aşk hikâyeleri", 0xFFBE185D) { it.moodTag == "romance" },
            Spec("preset_kafa_yakan", "Kafa Yakan Filmler", "🧠", "Sonunda 'nasıl yani?' dedirten zihin bükücüler", 0xFF7C3AED) { it.moodTag == "mind_bender" },
            Spec("preset_aksiyon_dolu", "Aksiyon Dolu", "💥", "Nefes nefese kovalamaca ve büyük patlamalar", 0xFFC2410C) { it.genre.contains("Aksiyon") },
            Spec("preset_gerilim_gecesi", "Gerilim Gecesi", "⚡", "Tırnak yedirten gerilim ve suç filmleri", 0xFF6D28D9) { it.moodTag == "thriller" },
            Spec("preset_animasyon_keyfi", "Animasyon Keyfi", "🎨", "Her yaşa görsel şölen sunan animasyonlar", 0xFF059669) { it.moodTag == "animation" },
            Spec("preset_komedi_molasi", "Komedi Molası", "😂", "Günün stresini alacak kahkaha dozu", 0xFFD97706) { it.moodTag == "comedy" }
        )
        return specs.mapIndexedNotNull { idx, spec ->
            val movies = pick(spec.filter)
            if (movies.size < 4) return@mapIndexedNotNull null
            CustomWheelList(
                id = spec.id,
                name = spec.name,
                emoji = spec.emoji,
                description = spec.desc,
                movies = movies,
                accentColorHex = spec.color,
                isPreset = true,
                updatedAtMillis = now - 1L - idx
            )
        }
    }

    private fun encodeEntity(list: CustomWheelList): CustomWheelListEntity {
        val catalogIds = list.movies
            .filter { MovieRepository.getMovieById(it.id) != null }
            .joinToString(",") { it.id }

        val customMoviesEncoded = list.movies
            .filter { MovieRepository.getMovieById(it.id) == null }
            .joinToString("\n") { movie ->
                listOf(
                    escapeField(movie.id),
                    escapeField(movie.title),
                    movie.year.toString(),
                    movie.imdb.toString(),
                    escapeField(movie.genre),
                    movie.colorHex.toString()
                ).joinToString("|")
            }

        return CustomWheelListEntity(
            id = list.id,
            name = list.name,
            emoji = list.emoji,
            description = list.description,
            movieIdsCsv = catalogIds,
            customMoviesJson = customMoviesEncoded,
            accentColorHex = list.accentColorHex,
            isPreset = list.isPreset,
            updatedAtMillis = list.updatedAtMillis
        )
    }

    private fun decodeEntity(entity: CustomWheelListEntity): CustomWheelList? {
        val catalogMovies = entity.movieIdsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { MovieRepository.getMovieById(it) }

        val customMovies = if (entity.customMoviesJson.isBlank() || entity.customMoviesJson == "[]") {
            emptyList()
        } else {
            entity.customMoviesJson.split("\n").mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size < 6) return@mapNotNull null
                val title = unescapeField(parts[1]).trim()
                if (title.isEmpty()) return@mapNotNull null
                val movieId = unescapeField(parts[0]).ifBlank { "custom_mov_${title.hashCode()}" }
                val year = parts[2].toIntOrNull() ?: 2024
                val imdb = parts[3].toDoubleOrNull() ?: 7.5
                val genre = unescapeField(parts[4]).ifBlank { "Özel Seçki" }
                val colorHex = parts[5].toLongOrNull() ?: entity.accentColorHex
                Movie(
                    id = movieId,
                    title = title,
                    originalTitle = title,
                    year = year,
                    durationMins = 115,
                    imdb = imdb,
                    moodTag = Mood.CULT_AWARD.id,
                    platforms = listOf("Özel Liste"),
                    genre = genre,
                    logline = "$title — Kullanıcının kendi çark kategorisine eklediği özel film seçimi.",
                    whyWatch = "Senin oluşturduğun özel çark listesinden geliyor!",
                    posterPath = "",
                    posterUrl = "",
                    colorHex = colorHex
                )
            }
        }

        val combined = (catalogMovies + customMovies).distinctBy { it.id }
        return CustomWheelList(
            id = entity.id,
            name = entity.name,
            emoji = entity.emoji,
            description = entity.description,
            movies = combined,
            accentColorHex = entity.accentColorHex,
            isPreset = entity.isPreset,
            updatedAtMillis = entity.updatedAtMillis
        )
    }

    private fun escapeField(raw: String): String {
        return raw.replace("\\", "\\\\")
            .replace("|", "\\p")
            .replace("\n", "\\n")
    }

    private fun unescapeField(escaped: String): String {
        return escaped.replace("\\n", "\n")
            .replace("\\p", "|")
            .replace("\\\\", "\\")
    }
}

package com.example.data.model

object TmdbImageConfig {
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

    /**
     * TMDB afişleri açık mı? Android'de `NeIzlesekApp` açılışta BuildConfig.TMDB_POSTERS_ENABLED ile ayarlar
     * (katalog ilk kez okunmadan önce). iOS'ta uygulama başlatılırken ayarlanır.
     */
    var postersEnabled: Boolean = true

    /**
     * Builds the full TMDb poster URL for a given [posterPath].
     * Returns null when [posterPath] is null or blank so UI components render the bundled default placeholder.
     */
    fun getPosterUrl(posterPath: String?): String? {
        if (!postersEnabled) return null
        val trimmed = posterPath?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return trimmed
        }
        val normalizedPath = if (trimmed.startsWith("/")) trimmed else "/$trimmed"
        return "$IMAGE_BASE_URL$normalizedPath"
    }
}

enum class AudioLanguageOption(
    val id: String,
    val label: String,
    val shortBadge: String
) {
    DUBBED("dublaj", "Türkçe Dublaj", "TR Dublaj"),
    SUBTITLED("altyazi", "Türkçe Altyazı", "TR Altyazı")
}

data class Movie(
    val id: String,
    val title: String,
    val originalTitle: String = "",
    val year: Int,
    val durationMins: Int,
    val imdb: Double,
    val moodTag: String,
    val platforms: List<String>,
    val genre: String,
    val logline: String,
    val whyWatch: String,
    val posterPath: String = "",
    /** TMDB film kimliği (tools/tmdb_poster_sync.py tarafından doldurulur). */
    val tmdbId: Int? = null,
    /** Resmi YouTube fragmanının video kimliği (TMDB'den). Uygulama fragmanı YouTube'da açar, gömmez. */
    val trailerKey: String? = null,
    val posterUrl: String = TmdbImageConfig.getPosterUrl(posterPath).orEmpty(),
    val colorHex: Long = 0xFF8B5CF6,
    val audioOptions: List<String> = listOf(
        AudioLanguageOption.DUBBED.id,
        AudioLanguageOption.SUBTITLED.id
    ),
    val isCultOrMostWatched: Boolean = false,
    val isNewRelease: Boolean = year >= 2024,
    val addedDateKey: String? = null,
    /** Film mi dizi mi. Dizilerde [durationMins] bölüm süresidir. */
    val kind: ContentKind = ContentKind.MOVIE,
    /** Yalnızca diziler: sezon sayısı. */
    val seasonCount: Int? = null
) {
    val isSeries: Boolean get() = kind == ContentKind.SERIES

    /** "112 dk" ya da "3 sezon • bölüm 45 dk". */
    fun durationLabel(): String = if (durationMins <= 0) {
        "Süre açıklanmadı"
    } else if (isSeries) {
        listOfNotNull(seasonCount?.let { "$it sezon" }, "bölüm $durationMins dk").joinToString(" • ")
    } else {
        "$durationMins dk"
    }

    fun resolvedPosterUrl(): String? =
        TmdbImageConfig.getPosterUrl(posterPath.ifBlank { posterUrl })

    fun supportsAudioOptions(selectedAudioIds: Set<String>): Boolean {
        if (selectedAudioIds.isEmpty()) return true
        return selectedAudioIds.any { audioOptions.contains(it) }
    }

    fun audioBadgesText(): String {
        val hasDub = audioOptions.contains(AudioLanguageOption.DUBBED.id)
        val hasSub = audioOptions.contains(AudioLanguageOption.SUBTITLED.id)
        return when {
            hasDub && hasSub -> "TR Dublaj • TR Altyazı"
            hasDub -> "Türkçe Dublaj"
            hasSub -> "Türkçe Altyazı"
            else -> "Orijinal Dil"
        }
    }
}

enum class ContentKind(val id: String, val label: String) {
    MOVIE("movie", "Film"),
    SERIES("series", "Dizi")
}

/**
 * Türkiye'de yakında vizyona girecek (ya da yeni girmiş) film. Çarka girmez; "Yakında" sekmesinde gösterilir.
 * [releaseDate] ISO biçiminde (yyyy-MM-dd).
 */
data class UpcomingRelease(
    val id: String,
    val tmdbId: Int,
    val title: String,
    val originalTitle: String = "",
    val releaseDate: String,
    val durationMins: Int = 0,
    val genre: String = "",
    val logline: String = "",
    val posterPath: String = "",
    val trailerKey: String? = null,
    val colorHex: Long = 0xFF8B5CF6
) {
    val year: Int get() = releaseDate.take(4).toIntOrNull() ?: 0

    /** Film kartında ve detayda kullanmak için Movie görünümü (çarka eklenmez). */
    fun asMovie(): Movie = Movie(
        id = id,
        title = title,
        originalTitle = originalTitle,
        year = year,
        durationMins = durationMins,
        imdb = 0.0,
        moodTag = "easy_watch",
        platforms = listOf("Sinema"),
        genre = genre.ifBlank { "Dram" },
        logline = logline,
        whyWatch = "Vizyon tarihi: ${formatTurkishDate(releaseDate)}",
        posterPath = posterPath,
        tmdbId = tmdbId,
        trailerKey = trailerKey,
        colorHex = colorHex,
        isNewRelease = true
    )
}

private val TR_MONTHS = listOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
)

/** "2026-11-27" -> "27 Kasım 2026". */
fun formatTurkishDate(iso: String): String {
    val parts = iso.split("-")
    val y = parts.getOrNull(0) ?: return iso
    val m = parts.getOrNull(1)?.toIntOrNull()?.takeIf { it in 1..12 } ?: return iso
    val d = parts.getOrNull(2)?.toIntOrNull() ?: return iso
    return "$d ${TR_MONTHS[m - 1]} $y"
}

/** "Film" ya da "Dizi". */
val Movie.kindNoun: String get() = if (isSeries) "Dizi" else "Film"

/** Liste için sayaç adı: hepsi film → "Film", hepsi dizi → "Dizi", karışık → "Yapım". */
fun List<Movie>.contentNoun(): String = when {
    isEmpty() || none { it.isSeries } -> "Film"
    all { it.isSeries } -> "Dizi"
    else -> "Yapım"
}

/** Filtre ekranındaki "Film / Dizi / İkisi" seçimi. */
enum class ContentKindFilter(val id: String, val label: String) {
    BOTH("both", "İkisi"),
    MOVIES("movies", "Film"),
    SERIES("series", "Dizi");

    fun accepts(movie: Movie): Boolean = when (this) {
        BOTH -> true
        MOVIES -> !movie.isSeries
        SERIES -> movie.isSeries
    }
}

enum class Mood(
    val id: String,
    val label: String,
    val emoji: String,
    val description: String
) {
    EASY_WATCH("easy_watch", "Beyin Yormayan / Çerezlik", "🍿", "Ağır dram istemeyenlere, rahatça arkaya yaslanmalık"),
    THRILLER("thriller", "Soluksuz Gerilim", "⚡", "Tırnak yedirten, nabzı 140'a fırlatan heyecan"),
    COMEDY("comedy", "Gözlerden Yaş Getiren Komedi", "😂", "Günün tüm stresini unutturacak kahkaha dozu"),
    ROMANCE("romance", "Aşk & Romantizm", "❤️", "Battaniye altına girip sarılmalık romantik filmler"),
    MIND_BENDER("mind_bender", "Akıl Oyunları / Zihin Bükücü", "🧠", "Sonunda 'Nasıl yani?!' dedirten şok finaller"),
    CULT_AWARD("cult_award", "Kült & Ödüllü Sinema", "🏆", "Festival ödüllü, sinema sanatının zirvesi"),
    HORROR("horror", "Korku & Adrenalin", "👻", "Işıkları kapattıracak karanlık hikayeler"),
    ANIMATION("animation", "Animasyon & Masalsı", "🎨", "Görsel şölen sunan her yaşa uygun başyapıtlar")
}

data class StreamingPlatform(
    val id: String,
    val name: String,
    val iconName: String,
    val badgeColorHex: Long
)

const val OTHER_PLATFORM_NAME = "Diğer"

/**
 * Eski platform adlarını güncel adlarına çevirir (ör. BluTV, Türkiye'de HBO Max olarak yeniden markalandı).
 * Cihazda kayıtlı eski geçmiş kayıtları okunurken kullanılır.
 */
fun normalizePlatformName(name: String): String = when (name.trim()) {
    "BluTV" -> "HBO Max"
    else -> name.trim()
}

/**
 * Platformlar yalnızca ADLARIYLA gösterilir. Marka logoları, logo karakterleri (ör. Apple simgesi) ve
 * marka renkleri bilerek kullanılmaz; kısaltmalar uygulamanın kendi nötr temasında çizilir.
 */
private const val NEUTRAL_BADGE = 0xFF2E2A35

val AvailablePlatforms = listOf(
    StreamingPlatform("netflix", "Netflix", "N", NEUTRAL_BADGE),
    StreamingPlatform("prime", "Prime Video", "P", NEUTRAL_BADGE),
    StreamingPlatform("disney", "Disney+", "D+", NEUTRAL_BADGE),
    StreamingPlatform("hbomax", "HBO Max", "HBO", NEUTRAL_BADGE),
    StreamingPlatform("mubi", "Mubi", "M", NEUTRAL_BADGE),
    StreamingPlatform("gain", "Gain", "G", NEUTRAL_BADGE),
    StreamingPlatform("tod", "TOD", "TOD", NEUTRAL_BADGE),
    StreamingPlatform("apple", "Apple TV+", "TV+", NEUTRAL_BADGE),
    StreamingPlatform("other", OTHER_PLATFORM_NAME, "+", NEUTRAL_BADGE)
)

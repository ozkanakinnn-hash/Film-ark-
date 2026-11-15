package com.example.data.model

object TmdbImageConfig {
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

    /**
     * Builds the full TMDb poster URL for a given [posterPath].
     * Returns null when [posterPath] is null or blank so UI components render the bundled default placeholder.
     */
    fun getPosterUrl(posterPath: String?): String? {
        if (!com.example.BuildConfig.TMDB_POSTERS_ENABLED) return null
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
    val addedDateKey: String? = null
) {
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

val AvailablePlatforms = listOf(
    StreamingPlatform("netflix", "Netflix", "N", 0xFFE50914),
    StreamingPlatform("prime", "Prime Video", "P", 0xFF00A8E1),
    StreamingPlatform("disney", "Disney+", "D+", 0xFF113CCF),
    StreamingPlatform("hbomax", "HBO Max", "HBO", 0xFF4F46E5),
    StreamingPlatform("mubi", "Mubi", "M", 0xFF192A45),
    StreamingPlatform("gain", "Gain", "G", 0xFF00E676),
    StreamingPlatform("tod", "TOD", "TOD", 0xFF7C3AED),
    StreamingPlatform("apple", "Apple TV+", "", 0xFF475569),
    StreamingPlatform("other", OTHER_PLATFORM_NAME, "+", 0xFF64748B)
)

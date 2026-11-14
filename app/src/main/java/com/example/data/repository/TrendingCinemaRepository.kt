package com.example.data.repository

import com.example.data.model.Movie

data class TrendingCinemaItem(
    val rank: Int,
    val title: String,
    val categoryOrGenre: String,
    val imdbScore: Double,
    val trendReason: String,
    val sourceName: String,
    val sourceUrl: String?,
    val posterPath: String?,
    val accentColorHex: Long,
    val matchedCatalogMovieId: String? = null
) {
    fun toShareableMovie(): Movie {
        val catalogMovie = matchedCatalogMovieId?.let { MovieRepository.getMovieById(it) }
        if (catalogMovie != null) return catalogMovie
        return Movie(
            id = "trend_$rank",
            title = title,
            originalTitle = title,
            year = 2025,
            durationMins = 124,
            imdb = imdbScore,
            moodTag = "trend",
            platforms = listOf(sourceName),
            genre = categoryOrGenre,
            logline = trendReason,
            whyWatch = "Haftanın Trendleri #$rank ($sourceName)",
            posterPath = posterPath ?: "",
            colorHex = accentColorHex
        )
    }
}

data class TrendingCinemaResult(
    val items: List<TrendingCinemaItem>,
    val isPlaceholderData: Boolean,
    val noticeMessage: String,
    val lastUpdatedLabel: String
)

sealed interface TrendingCinemaUiState {
    data object Loading : TrendingCinemaUiState
    data class Success(val result: TrendingCinemaResult) : TrendingCinemaUiState
    data class Error(val message: String) : TrendingCinemaUiState
}

interface TrendingCinemaRepository {
    suspend fun fetchWeeklyTrends(forceRefresh: Boolean = false): TrendingCinemaResult
}

/**
 * Placeholder implementation used when Firebase AI Logic (Google Search Grounding)
 * has not been enabled. Surfaces clearly labeled sample popularity rankings and
 * an explicit in-app notice that live Google Search trends require Firebase.
 */
class PlaceholderTrendingCinemaRepository : TrendingCinemaRepository {

    override suspend fun fetchWeeklyTrends(forceRefresh: Boolean): TrendingCinemaResult {
        val sampleItems = listOf(
            TrendingCinemaItem(
                rank = 1,
                title = "Yıldızlararası",
                categoryOrGenre = "Bilim Kurgu / Macera",
                imdbScore = 8.7,
                trendReason = "IMAX yeniden gösterim haberleriyle sosyal medyada haftanın en çok aranan yapımı.",
                sourceName = "Örnek Liste • IMDb Popüler",
                sourceUrl = null,
                posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                accentColorHex = 0xFF1D4ED8,
                matchedCatalogMovieId = "m13"
            ),
            TrendingCinemaItem(
                rank = 2,
                title = "Parazit",
                categoryOrGenre = "Dram / Gerilim",
                imdbScore = 8.5,
                trendReason = "Haftanın kült film maratonlarında izlenme listelerinin üst sıralarında.",
                sourceName = "Örnek Liste • Box Office",
                sourceUrl = null,
                posterPath = "/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg",
                accentColorHex = 0xFF047857,
                matchedCatalogMovieId = "m16"
            ),
            TrendingCinemaItem(
                rank = 3,
                title = "Örümcek-Adam: Örümcek Evreninde",
                categoryOrGenre = "Animasyon / Aksiyon",
                imdbScore = 8.4,
                trendReason = "Aile ve animasyon kategorisinde haftanın en çok önerilen filmi.",
                sourceName = "Örnek Liste • Sinema Trend",
                sourceUrl = null,
                posterPath = "/iiZZdoQBEYBv6id8su7ImL0oCbD.jpg",
                accentColorHex = 0xFFDC2626,
                matchedCatalogMovieId = "m22"
            ),
            TrendingCinemaItem(
                rank = 4,
                title = "Bıçaklar Çekildi: Gizemli Bir Hikaye",
                categoryOrGenre = "Gizem / Komedi",
                imdbScore = 7.1,
                trendReason = "Hafta sonu arkadaş grubu izlemelerinde en popüler polisiye komedi.",
                sourceName = "Örnek Liste • Dijital Top 10",
                sourceUrl = null,
                posterPath = "/vDGr1YdrlfbU9wxTOdpf3zChmv9.jpg",
                accentColorHex = 0xFFD97706,
                matchedCatalogMovieId = "m1"
            ),
            TrendingCinemaItem(
                rank = 5,
                title = "Korku Seansı",
                categoryOrGenre = "Korku / Gizem",
                imdbScore = 7.5,
                trendReason = "Gece seansı korku listelerinde yükselişini sürdürüyor.",
                sourceName = "Örnek Liste • Korku Seçkisi",
                sourceUrl = null,
                posterPath = "/wVYREutTvI2COPuCm0Yng7IlR.jpg",
                accentColorHex = 0xFF9F1239,
                matchedCatalogMovieId = "m19"
            )
        )

        return TrendingCinemaResult(
            items = sampleItems,
            isPlaceholderData = true,
            noticeMessage = "Canlı Google Search popülerlik verileri için Firebase kurulumu gereklidir. Şu an örnek haftalık sinema listesi gösteriliyor.",
            lastUpdatedLabel = "Örnek Liste • Firebase Gerekli"
        )
    }
}

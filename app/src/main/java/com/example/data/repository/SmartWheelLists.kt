package com.example.data.repository

import com.example.data.model.CustomWheelList

/**
 * Hazır (otomatik güncellenen) çarklar. Haftalık katalogla birlikte yenilenir; kullanıcı düzenleyemez.
 * Kimlikler "smart_" ile başlar. 12'den fazla yapım varsa çarkta 12 dilim örneklenir.
 */
object SmartWheelLists {
    const val POPULAR_MOVIES = "smart_popular_movies"
    const val POPULAR_SERIES = "smart_popular_series"
    const val WORST = "smart_worst"

    fun isSmart(id: String?): Boolean = id?.startsWith("smart_") == true

    fun all(): List<CustomWheelList> = listOfNotNull(
        HighlightsRepository.popularMovies().takeIf { it.size >= 4 }?.let {
            CustomWheelList(
                id = POPULAR_MOVIES,
                name = "Popüler Filmler",
                emoji = "🔥",
                description = "Türkiye'de platformlarda bu hafta en çok izlenen filmler",
                movies = it.sortedWith(compareByDescending<com.example.data.model.Movie> { m -> m.year }.thenByDescending { m -> m.imdb }),
                accentColorHex = 0xFFE9B949,
                isPreset = true
            )
        },
        HighlightsRepository.popularSeries().takeIf { it.size >= 4 }?.let {
            CustomWheelList(
                id = POPULAR_SERIES,
                name = "Popüler Diziler",
                emoji = "📺",
                description = "Türkiye'de platformlarda bu hafta en çok izlenen diziler",
                movies = it.sortedWith(compareByDescending<com.example.data.model.Movie> { m -> m.year }.thenByDescending { m -> m.imdb }),
                accentColorHex = 0xFF2DD4BF,
                isPreset = true
            )
        },
        HighlightsRepository.worstMovies().takeIf { it.size >= 4 }?.let {
            CustomWheelList(
                id = WORST,
                name = "Bunu Niye İzledim?",
                emoji = "🤦",
                description = "Çok izlenmiş ama en düşük puanlı filmler. Kendi isteğinle çeviriyorsun!",
                movies = it.sortedWith(compareByDescending<com.example.data.model.Movie> { m -> m.year }.thenByDescending { m -> m.imdb }),
                accentColorHex = 0xFF94A3B8,
                isPreset = true
            )
        }
    )

    fun byId(id: String): CustomWheelList? = all().firstOrNull { it.id == id }
}

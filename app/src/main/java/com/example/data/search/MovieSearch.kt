package com.example.data.search

import com.example.data.model.Movie
import java.text.Normalizer
import java.util.Locale

/**
 * Katalog içinde Türkçe karakterlere duyarsız arama.
 * "yuzuklerin" -> "Yüzüklerin Efendisi", "inception" -> özgün ada göre eşleşir.
 */
object MovieSearch {
    private val TR = Locale.forLanguageTag("tr")
    private val COMBINING = Regex("\\p{Mn}+")

    fun normalize(text: String): String {
        val lower = text.lowercase(TR)
            .replace('ı', 'i')
            .replace('ğ', 'g')
            .replace('ş', 's')
            .replace('ç', 'c')
            .replace('ö', 'o')
            .replace('ü', 'u')
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(COMBINING, "")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Ad, özgün ad, tür ve yıl içinde arar. En alakalı sonuçlar önce gelir:
     * ad başta eşleşme > kelime başında eşleşme > içinde eşleşme > tür/yıl eşleşmesi.
     */
    fun search(movies: List<Movie>, query: String, limit: Int = 40): List<Movie> {
        val q = normalize(query)
        if (q.length < 2) return emptyList()
        val tokens = q.split(' ').filter { it.isNotBlank() }
        return movies.asSequence()
            .mapNotNull { movie ->
                val title = normalize(movie.title)
                val original = normalize(movie.originalTitle)
                val extra = normalize("${movie.genre} ${movie.year} ${movie.platforms.joinToString(" ")}")
                val haystack = "$title $original $extra"
                if (tokens.any { it !in haystack }) return@mapNotNull null
                val score = when {
                    title.startsWith(q) || original.startsWith(q) -> 0
                    " $title".contains(" $q") || " $original".contains(" $q") -> 1
                    title.contains(q) || original.contains(q) -> 2
                    else -> 3
                }
                Triple(movie, score, -movie.imdb)
            }
            .sortedWith(compareBy({ it.second }, { it.third }))
            .map { it.first }
            .distinctBy { it.id }
            .take(limit)
            .toList()
    }
}

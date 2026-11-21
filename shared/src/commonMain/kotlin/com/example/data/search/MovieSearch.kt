package com.example.data.search

import com.example.data.model.Movie

/**
 * Katalog içinde Türkçe karakterlere duyarsız arama.
 * "yuzuklerin" -> "Yüzüklerin Efendisi", "inception" -> özgün ada göre eşleşir.
 */
object MovieSearch {
    // Türkçe ve sık görülen Latin aksanlı harfler (platformdan bağımsız; java.text.Normalizer iOS'ta yok)
    private val FOLD = mapOf(
        'ı' to 'i', 'i' to 'i', 'ğ' to 'g', 'ş' to 's', 'ç' to 'c', 'ö' to 'o', 'ü' to 'u',
        'â' to 'a', 'à' to 'a', 'á' to 'a', 'ä' to 'a', 'ã' to 'a', 'å' to 'a', 'ā' to 'a',
        'ê' to 'e', 'è' to 'e', 'é' to 'e', 'ë' to 'e', 'ē' to 'e',
        'î' to 'i', 'ì' to 'i', 'í' to 'i', 'ï' to 'i', 'ī' to 'i',
        'ô' to 'o', 'ò' to 'o', 'ó' to 'o', 'õ' to 'o', 'ø' to 'o', 'ō' to 'o',
        'û' to 'u', 'ù' to 'u', 'ú' to 'u', 'ū' to 'u',
        'ñ' to 'n', 'ý' to 'y', 'ÿ' to 'y', 'ž' to 'z', 'š' to 's', 'č' to 'c', 'ć' to 'c', 'ł' to 'l', 'ß' to 's'
    )

    fun normalize(text: String): String {
        // Türkçe büyük harfler: "I" → "ı", "İ" → "i" (lowercase() yerel ayardan bağımsızdır)
        val lower = text.replace('I', 'ı').replace('İ', 'i').lowercase()
        return buildString(lower.length) { for (c in lower) append(FOLD[c] ?: c) }
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

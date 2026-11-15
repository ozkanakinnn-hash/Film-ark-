package com.example.data.repository

import com.example.data.model.Movie
import kotlin.random.Random

/**
 * Çarkta gösterilecek dilimleri büyük film havuzundan seçer.
 *
 * - Çark okunaklı kalsın diye en fazla [MAX_SLICES] dilim gösterilir.
 * - Havuz büyükse seçim rastgeledir; son çarklarda görünmüş filmler geri plana atılır,
 *   böylece her açılışta ve her yeniden çevirişte farklı filmler gelir.
 * - [mustInclude] verilirse (ör. vitrinden ya da widget'tan açılan film) o film mutlaka çarkta kalır.
 * - Havuz zaten küçükse sıra ve içerik olduğu gibi korunur.
 */
object WheelPoolSampler {
    const val MAX_SLICES = 12
    private const val RECENT_MEMORY = 60

    private val recentlyShown = LinkedHashSet<String>()
    var random: Random = Random.Default

    fun sample(pool: List<Movie>, mustInclude: Movie? = null, maxSlices: Int = MAX_SLICES): List<Movie> {
        val distinct = pool.distinctBy { it.id }
        if (distinct.size <= maxSlices) {
            val result = if (mustInclude != null && distinct.none { it.id == mustInclude.id }) {
                (listOf(mustInclude) + distinct).take(maxSlices)
            } else {
                distinct
            }
            remember(result)
            return result
        }

        val others = distinct.filterNot { it.id == mustInclude?.id }
        val (fresh, seen) = others.partition { it.id !in recentlyShown }
        val picks = buildList {
            mustInclude?.let { add(it) }
            addAll(fresh.shuffled(random).take(maxSlices - size))
            if (size < maxSlices) addAll(seen.shuffled(random).take(maxSlices - size))
        }.shuffled(random)
        remember(picks)
        return picks
    }

    private fun remember(movies: List<Movie>) {
        movies.forEach { movie ->
            recentlyShown.remove(movie.id)
            recentlyShown.add(movie.id)
        }
        while (recentlyShown.size > RECENT_MEMORY) {
            recentlyShown.remove(recentlyShown.first())
        }
    }

    fun resetForTests() {
        recentlyShown.clear()
        random = Random.Default
    }
}

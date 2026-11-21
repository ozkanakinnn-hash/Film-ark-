package com.example.data.repository

import com.example.data.model.Movie
import kotlin.math.pow
import kotlin.random.Random

/**
 * Çarkta gösterilecek dilimleri büyük film havuzundan seçer.
 *
 * - Çark okunaklı kalsın diye en fazla [MAX_SLICES] dilim gösterilir.
 * - Havuz büyükse seçim rastgeledir; son çarklarda görünmüş filmler geri plana atılır,
 *   böylece her açılışta ve her yeniden çevirişte farklı filmler gelir.
 * - [mustInclude] verilirse (ör. vitrinden ya da widget'tan açılan film) o film mutlaka çarkta kalır.
 * - Havuz zaten küçükse sıra ve içerik olduğu gibi korunur.
 * - [weightOf] verilirse (zevk profili) seçim ağırlıklıdır: sevilen türler daha sık çarka girer,
 *   veto edilen türler daha seyrek. Çark dönerken dilimlerin şansı yine eşittir.
 */
object WheelPoolSampler {
    const val MAX_SLICES = 12
    private const val RECENT_MEMORY = 60

    private val recentlyShown = LinkedHashSet<String>()
    var random: Random = Random.Default

    fun sample(
        pool: List<Movie>,
        mustInclude: Movie? = null,
        maxSlices: Int = MAX_SLICES,
        weightOf: (Movie) -> Double = { 1.0 }
    ): List<Movie> {
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
            addAll(weightedPick(fresh, maxSlices - size, weightOf))
            if (size < maxSlices) addAll(weightedPick(seen, maxSlices - size, weightOf))
        }.shuffled(random)
        remember(picks)
        return picks
    }

    /** Ağırlıklı, yerine koymadan örnekleme (Efraimidis–Spirakis: anahtar = u^(1/w)). */
    private fun weightedPick(candidates: List<Movie>, count: Int, weightOf: (Movie) -> Double): List<Movie> {
        if (count <= 0 || candidates.isEmpty()) return emptyList()
        return candidates
            .map { movie ->
                val w = weightOf(movie).coerceAtLeast(0.01)
                val u = random.nextDouble().coerceAtLeast(1e-12)
                movie to u.pow(1.0 / w)
            }
            .sortedByDescending { it.second }
            .take(count)
            .map { it.first }
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

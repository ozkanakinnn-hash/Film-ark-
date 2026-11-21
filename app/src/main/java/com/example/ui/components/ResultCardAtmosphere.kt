package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.Movie
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Sonuç kartında türe özel atmosferin dekor tipi (hepsi sonlu zaman çizelgesinden türetilir; sonsuz animasyon yok). */
internal enum class CardFx { STARS, BURSTS, SOFT_LIGHT, CONFETTI, BOKEH, MAGIC, FOG, BEAMS, DUST, BLOBS, CITY, LEAVES, TEXTURE, SPOTLIGHTS }

/**
 * Sonuç kartı türleri. Yerleşim her türde aynı; yalnızca arka plan, vurgu rengi ve dekor değişir.
 * [accent]: rozet, afiş kenarı, düğmeler. [bgTop]/[bgBottom]: arka plan geçişi. [glow]: afiş arkası ışık.
 */
internal enum class CardGenre(
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val glow: Color,
    val bgTop: Color,
    val bgBottom: Color,
    val darkText: Boolean,
    val fx: CardFx
) {
    SCI_FI("BİLİM KURGU", Icons.Filled.RocketLaunch, Color(0xFF38BDF8), Color(0xFF14B8A6), Color(0xFF020B24), Color(0xFF041E2E), false, CardFx.STARS),
    ACTION("AKSİYON", Icons.Filled.Bolt, Color(0xFFFF2D3A), Color(0xFF7C1D9E), Color(0xFF16040A), Color(0xFF1C0623), false, CardFx.BURSTS),
    DRAMA("DRAM", Icons.Filled.TheaterComedy, Color(0xFFE8C07A), Color(0xFFB07A3C), Color(0xFF1A110A), Color(0xFF0E0906), true, CardFx.SOFT_LIGHT),
    COMEDY("KOMEDİ", Icons.Filled.SentimentVerySatisfied, Color(0xFFFFC21A), Color(0xFFFF8A1F), Color(0xFF2A1A04), Color(0xFF1A0E02), true, CardFx.CONFETTI),
    ROMANCE("ROMANTİK", Icons.Filled.Favorite, Color(0xFFFF6FB5), Color(0xFFA855F7), Color(0xFF240A22), Color(0xFF160A2A), false, CardFx.BOKEH),
    FANTASY("FANTASTİK", Icons.Filled.AutoAwesome, Color(0xFFE9C46A), Color(0xFF10B981), Color(0xFF04201A), Color(0xFF071233), true, CardFx.MAGIC),
    HORROR("KORKU", Icons.Filled.Warning, Color(0xFFB14CFF), Color(0xFF8B0000), Color(0xFF0A0306), Color(0xFF120615), false, CardFx.FOG),
    THRILLER("GERİLİM", Icons.Filled.Fingerprint, Color(0xFFFF8A2A), Color(0xFF7F1D1D), Color(0xFF120705), Color(0xFF1A0608), true, CardFx.BEAMS),
    ADVENTURE("MACERA", Icons.Filled.Explore, Color(0xFFD4A853), Color(0xFF2F6B3A), Color(0xFF0C1A0E), Color(0xFF1A1208), true, CardFx.DUST),
    ANIMATION("ANİMASYON", Icons.Filled.Palette, Color(0xFF60A5FA), Color(0xFFFACC15), Color(0xFF0E0A2E), Color(0xFF1B0B33), false, CardFx.BLOBS),
    CRIME("SUÇ", Icons.Filled.Nightlight, Color(0xFFC9CDD6), Color(0xFF8B1E2D), Color(0xFF0B0B0E), Color(0xFF16070B), true, CardFx.CITY),
    DOCUMENTARY("BELGESEL", Icons.Filled.Park, Color(0xFF6EE7B7), Color(0xFF2563EB), Color(0xFF061A14), Color(0xFF061428), true, CardFx.LEAVES),
    HISTORY("TARİH", Icons.Filled.AccountBalance, Color(0xFFCD9B5A), Color(0xFF7C4A1E), Color(0xFF1A0F07), Color(0xFF0F0905), true, CardFx.TEXTURE),
    MUSICAL("MÜZİKAL", Icons.Filled.MusicNote, Color(0xFFE879F9), Color(0xFFE9B949), Color(0xFF1C0730), Color(0xFF14061F), false, CardFx.SPOTLIGHTS),
    CINEMA("SİNEMA", Icons.Filled.LocalMovies, Color(0xFFE9B949), Color(0xFFD63447), Color(0xFF120E08), Color(0xFF0B0A0F), true, CardFx.SOFT_LIGHT)
}

/**
 * Filmin türlerinden kart türü. Katalogdaki tür metni "Bilim Kurgu / Macera" biçimindedir; ayırt edici türler
 * (bilim kurgu, korku, fantastik…) genel türlerin (macera, dram) önüne geçer.
 */
internal fun cardGenreFor(movie: Movie): CardGenre {
    val g = movie.genre.lowercase()
    fun has(vararg k: String) = k.any { g.contains(it) }
    return when {
        has("bilim kurgu", "sci-fi") -> CardGenre.SCI_FI
        has("korku") -> CardGenre.HORROR
        has("fantastik") -> CardGenre.FANTASY
        has("animasyon") -> CardGenre.ANIMATION
        has("müzik") -> CardGenre.MUSICAL
        has("belgesel") -> CardGenre.DOCUMENTARY
        has("tarih", "savaş") -> CardGenre.HISTORY
        has("komedi", "aile") -> CardGenre.COMEDY
        has("romantik", "aşk") -> CardGenre.ROMANCE
        has("aksiyon") -> CardGenre.ACTION
        has("suç") -> CardGenre.CRIME
        has("gerilim", "gizem") -> CardGenre.THRILLER
        has("macera", "western") -> CardGenre.ADVENTURE
        has("dram") -> CardGenre.DRAMA
        else -> CardGenre.CINEMA
    }
}

/** Tohumlu, sabit parçacık konumları (her film için aynı; çizimde yalnızca zaman çizelgesi değişir). */
internal class FxSeeds(seed: Int) {
    private val r = Random(seed)
    val pts: List<FloatArray> = List(60) { floatArrayOf(r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat()) }
}

/**
 * Türe özel atmosfer. [t] 0..1 sahne ilerlemesi (sonlu). Arka plan geçişi + afiş arkası ışık + hafif dekor.
 * Hafif tutuldu: en fazla ~60 küçük şekil, gölge/bulanıklık yok.
 */
internal fun DrawScope.drawCardAtmosphere(g: CardGenre, t: Float, seeds: FxSeeds, intro: Float) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(g.bgTop, g.bgBottom, Color.Black)))
    val center = Offset(w / 2f, h * 0.34f)
    // Afiş arkası tür ışığı
    drawCircle(
        Brush.radialGradient(listOf(g.glow.copy(alpha = 0.42f * intro), Color.Transparent), center = center, radius = w * 0.85f),
        radius = w * 0.85f, center = center
    )
    val drift = t * 0.08f
    when (g.fx) {
        CardFx.STARS -> {
            // Yıldızlar + uzak gezegen
            seeds.pts.forEach { p ->
                val a = (0.25f + 0.75f * p[2]) * intro * (0.6f + 0.4f * sin((t * 6f + p[3] * 6f)))
                drawCircle(Color.White.copy(alpha = a.coerceIn(0f, 1f)), 0.6f + p[2] * 1.8f, Offset(p[0] * w, p[1] * h))
            }
            val planet = Offset(w * 0.86f, h * 0.12f)
            drawCircle(Brush.radialGradient(listOf(g.accent.copy(alpha = 0.55f * intro), g.glow.copy(alpha = 0.15f * intro), Color.Transparent), center = planet.copy(x = planet.x - 12f), radius = w * 0.16f), w * 0.11f, planet)
            drawCircle(g.accent.copy(alpha = 0.25f * intro), w * 0.17f, planet, style = Stroke(width = 1.5f))
        }
        CardFx.BURSTS -> {
            // Enerjik ışık patlamaları (çapraz huzmeler)
            for (i in 0 until 7) {
                val p = seeds.pts[i]
                val start = Offset(p[0] * w, h * (0.05f + p[1] * 0.9f))
                val len = w * (0.4f + p[2] * 0.6f) * intro
                drawLine(
                    Brush.linearGradient(listOf(Color.Transparent, (if (i % 2 == 0) g.accent else g.glow).copy(alpha = 0.45f), Color.Transparent), start, start + Offset(len, -len * 0.55f)),
                    start, start + Offset(len, -len * 0.55f), strokeWidth = 2f + p[3] * 4f
                )
            }
        }
        CardFx.SOFT_LIGHT -> {
            // Yukarıdan süzülen yumuşak sinema ışığı
            drawRect(Brush.verticalGradient(listOf(g.accent.copy(alpha = 0.18f * intro), Color.Transparent), endY = h * 0.5f), size = Size(w, h * 0.5f))
            seeds.pts.take(18).forEach { p ->
                drawCircle(g.accent.copy(alpha = 0.18f * intro * p[2]), 1f + p[3] * 2f, Offset(p[0] * w, (p[1] - drift) * h))
            }
        }
        CardFx.CONFETTI -> {
            seeds.pts.take(36).forEachIndexed { i, p ->
                val c = listOf(g.accent, g.glow, Color(0xFFFDE68A), Color.White)[i % 4]
                val y = ((p[1] + drift * 2f) % 1f) * h
                rotateRect(c.copy(alpha = 0.55f * intro), Offset(p[0] * w, y), 6f + p[2] * 6f, 3f + p[3] * 3f, p[3] * 360f + t * 200f)
            }
        }
        CardFx.BOKEH -> {
            seeds.pts.take(22).forEachIndexed { i, p ->
                val c = if (i % 2 == 0) g.accent else g.glow
                drawCircle(c.copy(alpha = 0.16f * intro), 10f + p[2] * 34f, Offset(p[0] * w, (p[1] - drift) * h))
            }
        }
        CardFx.MAGIC -> {
            seeds.pts.take(40).forEach { p ->
                val y = ((p[1] - drift * 2f + 1f) % 1f) * h
                val a = 0.25f + 0.75f * sin(t * 8f + p[3] * 9f).coerceAtLeast(0f)
                drawCircle(g.accent.copy(alpha = 0.7f * a * intro), 1f + p[2] * 2.2f, Offset(p[0] * w, y))
                if (p[2] > 0.8f) drawCircle(g.glow.copy(alpha = 0.2f * intro), 8f, Offset(p[0] * w, y))
            }
        }
        CardFx.FOG -> {
            // Alttan yükselen sis + kenar gölgeleri
            for (i in 0 until 5) {
                val p = seeds.pts[i]
                val c = Offset(w * p[0], h * (0.75f + p[1] * 0.25f))
                drawCircle(Brush.radialGradient(listOf(Color(0xFFB9A9C9).copy(alpha = 0.12f * intro), Color.Transparent), c, w * 0.6f), w * 0.6f, c)
            }
            drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.7f))))
            drawCircle(Brush.radialGradient(listOf(g.glow.copy(alpha = 0.25f * intro), Color.Transparent), Offset(w * 0.5f, h * 0.05f), w * 0.5f), w * 0.5f, Offset(w * 0.5f, h * 0.05f))
        }
        CardFx.BEAMS -> {
            // Keskin, dar ışık şeritleri (pancur arası ışık)
            for (i in 0 until 6) {
                val x = w * (0.08f + i * 0.17f)
                drawLine(
                    Brush.verticalGradient(listOf(g.accent.copy(alpha = 0.22f * intro), Color.Transparent)),
                    Offset(x, 0f), Offset(x + w * 0.25f, h * 0.7f), strokeWidth = 10f
                )
            }
        }
        CardFx.DUST -> {
            // Güneş huzmesi + uçuşan toz (yolculuk hissi)
            drawCircle(Brush.radialGradient(listOf(g.accent.copy(alpha = 0.35f * intro), Color.Transparent), Offset(w * 0.15f, h * 0.08f), w * 0.7f), w * 0.7f, Offset(w * 0.15f, h * 0.08f))
            seeds.pts.take(30).forEach { p ->
                drawCircle(g.accent.copy(alpha = 0.35f * intro * p[2]), 0.8f + p[3] * 1.8f, Offset(((p[0] + drift) % 1f) * w, p[1] * h))
            }
        }
        CardFx.BLOBS -> {
            val colors = listOf(g.accent, g.glow, Color(0xFFA855F7))
            seeds.pts.take(6).forEachIndexed { i, p ->
                val c = Offset(p[0] * w, p[1] * h)
                drawCircle(Brush.radialGradient(listOf(colors[i % 3].copy(alpha = 0.28f * intro), Color.Transparent), c, w * 0.35f), w * 0.35f, c)
            }
        }
        CardFx.CITY -> {
            // Gece şehri: altta silüet pencereleri, noir ışık
            seeds.pts.take(14).forEachIndexed { i, p ->
                val bw = w / 14f
                val bh = h * (0.08f + p[1] * 0.18f)
                drawRect(Color(0xFF050507), Offset(i * bw, h - bh), Size(bw + 1f, bh))
                for (k in 0 until 4) {
                    if ((p[2] * 10 + k).toInt() % 3 == 0) drawRect(g.accent.copy(alpha = 0.45f * intro), Offset(i * bw + bw * 0.3f, h - bh + 10f + k * 14f), Size(3f, 5f))
                }
            }
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, g.accent.copy(alpha = 0.1f * intro)), startY = h * 0.6f))
        }
        CardFx.LEAVES -> {
            drawCircle(Brush.radialGradient(listOf(g.glow.copy(alpha = 0.22f * intro), Color.Transparent), Offset(w * 0.8f, h * 0.9f), w * 0.7f), w * 0.7f, Offset(w * 0.8f, h * 0.9f))
            seeds.pts.take(16).forEach { p ->
                rotateRect(g.accent.copy(alpha = 0.22f * intro), Offset(p[0] * w, ((p[1] + drift) % 1f) * h), 10f, 5f, p[3] * 360f)
            }
        }
        CardFx.TEXTURE -> {
            // Bronz doku: ince yatay çizgiler + kenar kararması
            var y = 0f
            while (y < h) {
                drawLine(g.accent.copy(alpha = 0.05f * intro), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                y += 9f
            }
            drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)), Offset(w / 2f, h * 0.4f), w))
        }
        CardFx.SPOTLIGHTS -> {
            // Sahne ışıkları + parıltılar
            listOf(0.15f to 18f, 0.85f to -18f).forEach { (x, tilt) ->
                val top = Offset(w * x, 0f)
                val dir = tilt * PI.toFloat() / 180f
                val end = Offset(top.x - sin(dir) * h, top.y + cos(dir) * h)
                drawLine(Brush.linearGradient(listOf(g.accent.copy(alpha = 0.3f * intro), Color.Transparent), top, end), top, end, strokeWidth = w * 0.22f)
            }
            seeds.pts.take(24).forEach { p ->
                val a = 0.3f + 0.7f * sin(t * 10f + p[3] * 7f).coerceAtLeast(0f)
                drawCircle(g.glow.copy(alpha = 0.7f * a * intro), 1f + p[2] * 2f, Offset(p[0] * w, p[1] * h))
            }
        }
    }
    // Ortak sinema vinyeti
    drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.85f))))
}

private fun DrawScope.rotateRect(color: Color, c: Offset, w: Float, h: Float, deg: Float) {
    val rad = deg * PI.toFloat() / 180f
    val dx = cos(rad) * w / 2f
    val dy = sin(rad) * w / 2f
    drawLine(color, Offset(c.x - dx, c.y - dy), Offset(c.x + dx, c.y + dy), strokeWidth = h)
}

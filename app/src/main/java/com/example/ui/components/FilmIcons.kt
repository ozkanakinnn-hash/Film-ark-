package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.Mood
import com.example.ui.theme.CinemaTheme

/**
 * Uygulamaya özel "Neon Tür" film ikon seti (emojilerin yerine).
 * Tüm ikonlar aynı 24 birimlik ızgarada, aynı çizgi kalınlığıyla çizilir.
 * Kaynak çizimler: tools/film_icons.py (branding/film-ikon-secenekleri.png, stil C).
 */
enum class FilmIcon(val label: String, val neon: Color, val paths: List<String>) {
    CEREZLIK("Çerezlik", Color(0xFFFACC15), listOf("M6 10.5H18", "M6.5 10.5L8 20.5H16L17.5 10.5", "M10.2 10.5L10.7 20.5", "M13.8 10.5L13.3 20.5", "M6.6 10.5A2.4 2.4 0 0 1 8.2 6.4A2.7 2.7 0 0 1 12 4.6A2.7 2.7 0 0 1 15.8 6.4A2.4 2.4 0 0 1 17.4 10.5")),
    GERILIM("Gerilim", Color(0xFF8B5CF6), listOf("M2.5 12.5H7L9 7.5L12.2 17.5L14.8 10L16.2 12.5H21.5")),
    KOMEDI("Komedi", Color(0xFFF59E0B), listOf("M5 4.5H19V11A7 7 0 0 1 5 11Z", "M8.2 9.2C8.8 8.3 10 8.3 10.6 9.2", "M13.4 9.2C14 8.3 15.2 8.3 15.8 9.2", "M8.8 13.2A3.4 3.4 0 0 0 15.2 13.2Z")),
    DRAM("Dram", Color(0xFFE9B949), listOf("M5 4.5H19V11A7 7 0 0 1 5 11Z", "M8.2 8.6C8.8 9.5 10 9.5 10.6 8.6", "M13.4 8.6C14 9.5 15.2 9.5 15.8 8.6", "M9 15.4A3.4 3.4 0 0 1 15 15.4")),
    ROMANTIK("Romantik", Color(0xFFF43F5E), listOf("M12 20.2S4.5 15.6 4.5 9.8A4.2 4.2 0 0 1 12 7.1A4.2 4.2 0 0 1 19.5 9.8C19.5 15.6 12 20.2 12 20.2Z")),
    AKIL("Akıl Oyunları", Color(0xFF38BDF8), listOf("M10.75 12A1.25 1.25 0 0 1 13.25 12A2.5 2.5 0 0 1 8.25 12A3.75 3.75 0 0 1 15.75 12A5 5 0 0 1 5.75 12A6.25 6.25 0 0 1 18.25 12")),
    KULT("Kült & Ödüllü", Color(0xFFE9B949), listOf("M8 4.5H16V9.3A4 4 0 0 1 8 9.3Z", "M8 6H5.6A2.6 2.6 0 0 0 8.5 9.9", "M16 6H18.4A2.6 2.6 0 0 1 15.5 9.9", "M12 13.3V16.8", "M9.5 16.8H14.5L15.3 20H8.7Z")),
    KORKU("Korku", Color(0xFFEF4444), listOf("M6 20.5V10.8A6 6 0 0 1 18 10.8V20.5L16 18.9L14 20.5L12 18.9L10 20.5L8 18.9Z", "M10 11V11.2", "M14 11V11.2")),
    ANIMASYON("Animasyon", Color(0xFF10B981), listOf("M11 3.5L12.7 8.4L17.5 10L12.7 11.7L11 16.5L9.3 11.7L4.5 10L9.3 8.4Z", "M18 14.5L18.7 16.3L20.5 17L18.7 17.7L18 19.5L17.3 17.7L15.5 17L17.3 16.3Z")),
    AKSIYON("Aksiyon", Color(0xFFEA580C), listOf("M13.5 2.8L5.5 13.5H11.5L10.5 21.2L18.5 10.5H12.5Z")),
    BILIMKURGU("Bilim Kurgu", Color(0xFF06B6D4), listOf("M17 12A5 5 0 1 1 7 12A5 5 0 1 1 17 12Z", "M3.4 16A9.5 3 -25 1 0 20.6 8A9.5 3 -25 1 0 3.4 16")),
    MACERA("Macera", Color(0xFF22C55E), listOf("M20.5 12A8.5 8.5 0 1 1 3.5 12A8.5 8.5 0 1 1 20.5 12Z", "M15.2 8.8L13.3 13.3L8.8 15.2L10.7 10.7Z")),
    GIZEM("Gizem", Color(0xFF818CF8), listOf("M16 10.5A5.5 5.5 0 1 1 5 10.5A5.5 5.5 0 1 1 16 10.5Z", "M14.5 14.5L20 20")),
    AILE("Aile", Color(0xFF34D399), listOf("M8.5 8.2A2.4 2.4 0 1 1 8.5 3.4A2.4 2.4 0 1 1 8.5 8.2Z", "M4.2 20.5V14.2A4.3 4.3 0 0 1 12.8 14.2V20.5", "M16.2 11.2A1.9 1.9 0 1 1 16.2 7.4A1.9 1.9 0 1 1 16.2 11.2Z", "M13.2 20.5V16.2A3 3 0 0 1 19.2 16.2V20.5")),
    KLASIK("Klasik", Color(0xFFE9B949), listOf("M20.5 12A8.5 8.5 0 1 1 3.5 12A8.5 8.5 0 1 1 20.5 12Z", "M13.4 12A1.4 1.4 0 1 1 10.6 12A1.4 1.4 0 1 1 13.4 12Z", "M13.6 7.4A1.6 1.6 0 1 1 10.4 7.4A1.6 1.6 0 1 1 13.6 7.4Z", "M13.6 16.6A1.6 1.6 0 1 1 10.4 16.6A1.6 1.6 0 1 1 13.6 16.6Z", "M8.2 12A1.6 1.6 0 1 1 5 12A1.6 1.6 0 1 1 8.2 12Z", "M19 12A1.6 1.6 0 1 1 15.8 12A1.6 1.6 0 1 1 19 12Z")),
    SUC("Suç", Color(0xFF94A3B8), listOf("M7.2 7.9A7.9 7.9 0 0 1 19.9 12", "M5.6 16.6C6.1 15.4 6.3 14.1 6.3 12.7A5.7 5.7 0 0 1 17.7 12.7C17.7 14.3 17.6 15.8 17.3 17.3", "M8 19C8.8 17.4 9.2 15.6 9.2 13.7A2.8 2.8 0 0 1 14.8 13.7C14.8 16.1 14.4 18.3 13.6 20.3", "M12 13.7C12 16.3 11.4 18.7 10.3 20.8")),
}

/** Ruh hali → ikon. */
fun Mood.filmIcon(): FilmIcon = when (this) {
    Mood.EASY_WATCH -> FilmIcon.CEREZLIK
    Mood.THRILLER -> FilmIcon.GERILIM
    Mood.COMEDY -> FilmIcon.KOMEDI
    Mood.ROMANCE -> FilmIcon.ROMANTIK
    Mood.MIND_BENDER -> FilmIcon.AKIL
    Mood.CULT_AWARD -> FilmIcon.KULT
    Mood.HORROR -> FilmIcon.KORKU
    Mood.ANIMATION -> FilmIcon.ANIMASYON
}

/**
 * Özel çark listesi → neon ikon. Hazır listeler sabit eşlenir; kullanıcı listeleri,
 * içindeki filmlerin en sık görülen ilk türüne göre ikon alır (emoji yerine).
 */
fun filmIconForList(list: com.example.data.model.CustomWheelList): FilmIcon = when (list.id) {
    "preset_korku_gecesi" -> FilmIcon.KORKU
    "preset_aile_dostu" -> FilmIcon.AILE
    "preset_klasikler" -> FilmIcon.KLASIK
    "preset_romantik_aksam" -> FilmIcon.ROMANTIK
    "preset_kafa_yakan" -> FilmIcon.AKIL
    "preset_aksiyon_dolu" -> FilmIcon.AKSIYON
    "preset_gerilim_gecesi" -> FilmIcon.GERILIM
    "preset_animasyon_keyfi" -> FilmIcon.ANIMASYON
    "preset_komedi_molasi" -> FilmIcon.KOMEDI
    else -> list.movies
        .mapNotNull { it.genre.split('/', ',').firstOrNull()?.trim()?.takeIf { g -> g.isNotEmpty() } }
        .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        ?.let { filmIconForGenre(it) } ?: FilmIcon.CEREZLIK
}

/** Tür adı → ikon (gelişmiş filtre etiketleri). */
fun filmIconForGenre(genre: String): FilmIcon = when (genre.trim().lowercase(java.util.Locale.forLanguageTag("tr-TR"))) {
    "komedi" -> FilmIcon.KOMEDI
    "gerilim" -> FilmIcon.GERILIM
    "aksiyon" -> FilmIcon.AKSIYON
    "dram" -> FilmIcon.DRAM
    "bilim kurgu" -> FilmIcon.BILIMKURGU
    "macera" -> FilmIcon.MACERA
    "romantik" -> FilmIcon.ROMANTIK
    "animasyon" -> FilmIcon.ANIMASYON
    "gizem" -> FilmIcon.GIZEM
    "korku" -> FilmIcon.KORKU
    "suç" -> FilmIcon.SUC
    else -> FilmIcon.CEREZLIK
}

/**
 * Neon Tür ikonu. Koyu temada tür renginde yumuşak parıltı + beyaz çekirdek çizgi;
 * açık temada parıltısız, tür renginde çizgi. [tile] true ise yuvarlatılmış karo içinde çizilir.
 */
@Composable
fun FilmIconView(
    icon: FilmIcon,
    size: Dp,
    modifier: Modifier = Modifier,
    tile: Boolean = true
) {
    val isDark = CinemaTheme.colors.isDark
    val paths = remember(icon) { icon.paths.map { PathParser().parsePathString(it).toPath() } }
    val shape = RoundedCornerShape(size * 0.28f)
    val box = if (tile) {
        modifier
            .size(size)
            .background(if (isDark) Color(0xFF15131A) else Color(0xFFFFF9EE), shape)
            .border(1.dp, if (isDark) icon.neon.copy(alpha = 0.35f) else Color(0x59B45309), shape)
            .padding(size / 6f)
    } else {
        modifier.size(size)
    }
    Box(modifier = box.semantics { contentDescription = icon.label }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = this.size.minDimension / 24f
            scale(s, s, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                paths.forEach { p ->
                    if (isDark) {
                        // Parıltı katmanları
                        drawPath(p, icon.neon.copy(alpha = 0.18f), style = Stroke(5.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(p, icon.neon.copy(alpha = 0.35f), style = Stroke(3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(p, icon.neon, style = Stroke(1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(p, Color.White.copy(alpha = 0.85f), style = Stroke(0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    } else {
                        // Açık temada neon renkleri koyulaştır: fildişi zeminde okunaklı kalsın
                        drawPath(p, lerp(icon.neon, Color(0xFF0B0A0F), 0.3f), style = Stroke(1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }
        }
    }
}

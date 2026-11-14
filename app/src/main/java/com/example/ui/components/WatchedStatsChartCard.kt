package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchedMovieEntity
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.roundToInt

data class GenreWatchStat(
    val genre: String,
    val movieCount: Int,
    val totalMinutes: Int,
    val countShare: Float,
    val durationShare: Float
)

data class WatchedSummaryStats(
    val totalMovies: Int,
    val totalMinutes: Int,
    val averageDurationMins: Int,
    val averageImdb: Double,
    val topGenre: String?,
    val genreStats: List<GenreWatchStat>,
    val tastePersonaTitle: String,
    val tasteSummaryText: String
)

private data class GenreAccumulator(
    var count: Int = 0,
    var minutes: Int = 0
)

private fun resolvePersonaTitle(topGenre: String?): String {
    return when {
        topGenre == null -> "Sinema Yolculuğuna Hazır 🍿"
        topGenre.contains("Komedi", ignoreCase = true) -> "Kahkaha Koleksiyoncusu 😂"
        topGenre.contains("Bilim Kurgu", ignoreCase = true) -> "Evren Gezgini & Zihin Kaşifi 🚀"
        topGenre.contains("Gerilim", ignoreCase = true) -> "Tansiyon & Ters Köşe Avcısı ⚡"
        topGenre.contains("Dram", ignoreCase = true) -> "Derin Hikaye Tutkunu 🎭"
        topGenre.contains("Romantik", ignoreCase = true) -> "Duygu & Aşk Sinemacısı ❤️"
        topGenre.contains("Animasyon", ignoreCase = true) -> "Hayal Dünyası Yolcusu 🎨"
        topGenre.contains("Korku", ignoreCase = true) -> "Gece Yarısı Adrenalin Avcısı 👻"
        topGenre.contains("Aksiyon", ignoreCase = true) || topGenre.contains("Macera", ignoreCase = true) ->
            "Yüksek Tempo Tutkunu 💥"
        topGenre.contains("Gizem", ignoreCase = true) || topGenre.contains("Suç", ignoreCase = true) ->
            "Dedektif Bakışlı İzleyici 🔍"
        else -> "Çok Yönlü Sinema Gurmesi 🍿"
    }
}

private fun buildTasteSummaryText(
    topGenre: String?,
    secondGenre: String?,
    topSharePercent: Int,
    averageDurationMins: Int,
    averageImdb: Double
): String {
    if (topGenre == null) {
        return "İlk filmini izledim olarak işaretlediğinde sana özel 'Film Zevkiniz' profilin burada oluşturulacak."
    }

    val genreSentence = if (secondGenre != null && secondGenre != "Diğer") {
        "En sık $topGenre (%$topSharePercent) ve $secondGenre türlerine yöneliyorsun."
    } else {
        "İzleme geçmişinde $topGenre (%$topSharePercent) türü açık ara öne çıkıyor."
    }

    val paceSentence = when {
        averageDurationMins >= 135 ->
            "Uzun soluklu ve derinlikli anlatıları (ort. $averageDurationMins dk) seviyorsun."
        averageDurationMins <= 110 ->
            "Akıcı, sıkmayan ve tempolu yapımları (ort. $averageDurationMins dk) tercih ediyorsun."
        else ->
            "Dengeli süreli yapımlar (ort. $averageDurationMins dk) izleme ritmine tam uyuyor."
    }

    val formattedImdb = String.format(Locale.US, "%.1f", averageImdb)
    val ratingSentence = when {
        averageImdb >= 8.2 ->
            "Seçiciliğin çok yüksek (★$formattedImdb ort.) — tam bir başyapıt avcısısın!"
        averageImdb >= 7.5 ->
            "Kaliteli ve güçlü puanlı filmleri (★$formattedImdb ort.) radarına alıyorsun."
        else ->
            "Puan kalıplarından bağımsız, anlık keyif odaklı seçimler (★$formattedImdb ort.) yapıyorsun."
    }

    return "$genreSentence $paceSentence $ratingSentence"
}

fun computeWatchedSummaryStats(watchedMovies: List<WatchedMovieEntity>): WatchedSummaryStats {
    if (watchedMovies.isEmpty()) {
        return WatchedSummaryStats(
            totalMovies = 0,
            totalMinutes = 0,
            averageDurationMins = 0,
            averageImdb = 0.0,
            topGenre = null,
            genreStats = emptyList(),
            tastePersonaTitle = resolvePersonaTitle(null),
            tasteSummaryText = buildTasteSummaryText(null, null, 0, 0, 0.0)
        )
    }

    val accumulators = linkedMapOf<String, GenreAccumulator>()
    watchedMovies.forEach { movie ->
        val splitGenres = movie.genre
            .split("/")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("Genel") }

        splitGenres.forEach { g ->
            val acc = accumulators.getOrPut(g) { GenreAccumulator() }
            acc.count += 1
            acc.minutes += movie.durationMins
        }
    }

    val sorted = accumulators.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, GenreAccumulator>> { it.value.count }
                .thenByDescending { it.value.minutes }
                .thenBy { it.key }
        )

    // Keep top 4 genres + group remaining into "Diğer" if more than 5
    val displayedEntries = if (sorted.size <= 5) {
        sorted.map { it.key to it.value }
    } else {
        val topFour = sorted.take(4).map { it.key to it.value }
        val otherAcc = GenreAccumulator()
        sorted.drop(4).forEach {
            otherAcc.count += it.value.count
            otherAcc.minutes += it.value.minutes
        }
        topFour + ("Diğer" to otherAcc)
    }

    val totalCountHits = displayedEntries.sumOf { it.second.count }.coerceAtLeast(1)
    val totalDurationHits = displayedEntries.sumOf { it.second.minutes }.coerceAtLeast(1)

    val stats = displayedEntries.map { (genre, acc) ->
        GenreWatchStat(
            genre = genre,
            movieCount = acc.count,
            totalMinutes = acc.minutes,
            countShare = acc.count.toFloat() / totalCountHits.toFloat(),
            durationShare = acc.minutes.toFloat() / totalDurationHits.toFloat()
        )
    }

    val totalMovies = watchedMovies.size
    val totalMinutes = watchedMovies.sumOf { it.durationMins }
    val avgDuration = (totalMinutes.toDouble() / totalMovies).roundToInt()
    val avgImdb = watchedMovies.sumOf { it.imdb } / totalMovies
    val topGenre = sorted.firstOrNull()?.key
    val secondGenre = sorted.getOrNull(1)?.key
    val topSharePct = ((stats.firstOrNull()?.countShare ?: 0f) * 100f).roundToInt()

    return WatchedSummaryStats(
        totalMovies = totalMovies,
        totalMinutes = totalMinutes,
        averageDurationMins = avgDuration,
        averageImdb = avgImdb,
        topGenre = topGenre,
        genreStats = stats,
        tastePersonaTitle = resolvePersonaTitle(topGenre),
        tasteSummaryText = buildTasteSummaryText(
            topGenre = topGenre,
            secondGenre = secondGenre,
            topSharePercent = topSharePct,
            averageDurationMins = avgDuration,
            averageImdb = avgImdb
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WatchedStatsChartCard(
    watchedMovies: List<WatchedMovieEntity>,
    modifier: Modifier = Modifier
) {
    val gold = PopcornGold
    val teal = ElectricCyan
    val crimson = NeonPurple
    val rose = NeonPink
    val emerald = EmeraldGreen
    val chartPalette = remember(gold, teal, crimson, rose, emerald) {
        listOf(gold, teal, crimson, rose, emerald)
    }

    val summary = remember(watchedMovies) { computeWatchedSummaryStats(watchedMovies) }
    var showByDuration by remember { mutableStateOf(false) }

    val formattedTotalTime = remember(summary.totalMinutes) {
        val hours = summary.totalMinutes / 60
        val mins = summary.totalMinutes % 60
        if (hours > 0) "${hours}s ${mins}dk" else "${mins} dk"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(ElectricCyan.copy(alpha = 0.6f), NeonPurple.copy(alpha = 0.6f))),
                RoundedCornerShape(22.dp)
            )
            .testTag("profile_stats_chart_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0x3306B6D4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = "Tür pasta grafiği",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "En Sık İzlenen Türler & Süre",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "İzleme geçmişinden canlı pasta grafiği analizi",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Toggle: [Tür Dağılımı] vs [İzleme Süresi]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChartModeChip(
                    label = "Tür Dağılımı 🎬",
                    selected = !showByDuration,
                    onClick = { showByDuration = false },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_mode_genre")
                )
                ChartModeChip(
                    label = "İzleme Süresi ⏱️",
                    selected = showByDuration,
                    onClick = { showByDuration = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_mode_duration")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (summary.genreStats.isEmpty()) {
                // Empty Pie Chart Preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("profile_stats_empty"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val emptyPieFill = CinemaSurfaceVariant
                    val emptyPieBorder = CinemaBorder
                    Canvas(
                        modifier = Modifier
                            .size(120.dp)
                            .semantics {
                                contentDescription = "Henüz izlenen film pasta grafiği verisi yok"
                            }
                    ) {
                        val diameter = size.minDimension
                        val topLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )
                        drawArc(
                            color = emptyPieFill,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = true,
                            topLeft = topLeft,
                            size = Size(diameter, diameter)
                        )
                        drawArc(
                            color = emptyPieBorder,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Film izledikçe tür pasta grafiğin ve 'Film Zevkiniz' özetin burada oluşacak.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val animProgress by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 650),
                    label = "pie_chart_sweep"
                )

                // Filled Pie Chart (Pasta Grafiği) + Legend Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pieBaseColor = CinemaBorder
                    val pieBorderColor = CinemaSurface
                    Canvas(
                        modifier = Modifier
                            .size(138.dp)
                            .testTag("genre_pie_chart")
                            .semantics {
                                contentDescription = "En sık izlenen türler pasta grafiği: $formattedTotalTime"
                            }
                    ) {
                        val diameter = size.minDimension - 6.dp.toPx()
                        val topLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )
                        val pieSize = Size(diameter, diameter)

                        // Subtle base circle
                        drawArc(
                            color = pieBaseColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = true,
                            topLeft = topLeft,
                            size = pieSize
                        )

                        var startAngle = -90f
                        summary.genreStats.forEachIndexed { index, stat ->
                            val share = if (showByDuration) stat.durationShare else stat.countShare
                            val sweep = (360f * share * animProgress).coerceAtLeast(1f)
                            val sliceColor = chartPalette[index % chartPalette.size]

                            // Filled Pie Slice
                            drawArc(
                                color = sliceColor,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = topLeft,
                                size = pieSize
                            )
                            // Crisp slice border for clear visual separation
                            if (summary.genreStats.size > 1) {
                                drawArc(
                                    color = pieBorderColor,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = true,
                                    topLeft = topLeft,
                                    size = pieSize,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                            startAngle += sweep
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right-hand Legend + Proportional Bars
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        summary.genreStats.forEachIndexed { index, stat ->
                            val color = chartPalette[index % chartPalette.size]
                            val share = if (showByDuration) stat.durationShare else stat.countShare
                            val pct = (share * 100f).roundToInt()

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .background(color, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stat.genre,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = if (showByDuration) {
                                            "${stat.totalMinutes} dk (%$pct)"
                                        } else {
                                            "${stat.movieCount} film (%$pct)"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = color
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(CinemaBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = (share * animProgress).coerceIn(0.05f, 1f))
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // PERSONALIZED "FİLM ZEVKİNİZ" SUMMARY CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                CinemaTheme.colors.heroGradientStart,
                                CinemaTheme.colors.heroGradientEnd
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(NeonPurple, PopcornGold)),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
                    .testTag("film_zevkiniz_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PopcornGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FİLM ZEVKİNİZ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = PopcornGold,
                                modifier = Modifier.testTag("film_zevkiniz_title")
                            )
                        }

                        if (summary.totalMovies > 0) {
                            Surface(
                                color = Color(0x3306B6D4),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = formattedTotalTime,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = summary.tastePersonaTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.testTag("film_zevkiniz_persona")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = summary.tasteSummaryText,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp,
                        modifier = Modifier.testTag("film_zevkiniz_summary")
                    )

                    if (summary.totalMovies > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.testTag("top_genre_summary")
                        ) {
                            summary.topGenre?.let { top ->
                                TasteMetricPill(text = "🏆 En Sık: $top", color = PopcornGold)
                            }
                            TasteMetricPill(text = "⏱️ Ort. ${summary.averageDurationMins} dk", color = ElectricCyan)
                            TasteMetricPill(
                                text = "★ ${String.format(Locale.US, "%.1f", summary.averageImdb)} ort.",
                                color = EmeraldGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TasteMetricPill(text: String, color: Color) {
    Surface(
        color = CinemaSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ChartModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) CinemaTheme.colors.selectedChipFill else Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) PopcornGold else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) PopcornGold else TextSecondary,
            maxLines = 1
        )
    }
}

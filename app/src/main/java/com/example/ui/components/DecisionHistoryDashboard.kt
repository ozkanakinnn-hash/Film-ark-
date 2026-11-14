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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoryDashboardSummary
import com.example.data.model.Movie
import com.example.data.model.SelectionModeStat
import com.example.data.model.TopSelectedGenreStat
import com.example.data.model.TopSelectedMovieStat
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DecisionHistoryDashboard(
    dashboard: HistoryDashboardSummary,
    onMovieClick: (Movie) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gold = PopcornGold
    val cyan = ElectricCyan
    val pink = NeonPink
    val emerald = EmeraldGreen
    val purple = NeonPurple
    val palette = remember(gold, cyan, pink, emerald, purple) {
        listOf(
            gold,
            cyan,
            pink,
            emerald,
            purple,
            Color(0xFFF97316)
        )
    }

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 650),
        label = "history_dashboard_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(PopcornGold.copy(alpha = 0.55f), NeonPurple.copy(alpha = 0.55f))
                ),
                RoundedCornerShape(22.dp)
            )
            .testTag("decision_history_dashboard"),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.linearGradient(listOf(PopcornGold, NeonPurple)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Veritabanı analiz dashboardu",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Veritabanı Karar Dashboard'u",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Seçim geçmişinden en sık çıkan tür & filmler",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    color = EmeraldGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ROOM DB",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (dashboard.totalSelections == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CinemaSurfaceVariant, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                        .testTag("dashboard_empty_state"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🗃️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Analiz için veritabanı kaydı bekleniyor",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dashboard.insightText,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            } else {
                // 4 KPI Tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardKpiTile(
                        label = "Toplam Seçim",
                        value = "${dashboard.totalSelections}",
                        accent = PopcornGold,
                        tag = "dashboard_kpi_total_selections",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardKpiTile(
                        label = "Farklı Film",
                        value = "${dashboard.uniqueMoviesCount}",
                        accent = ElectricCyan,
                        tag = "dashboard_kpi_unique_movies",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardKpiTile(
                        label = "Ort. IMDb",
                        value = String.format(Locale.US, "%.1f", dashboard.averageImdb),
                        accent = NeonPink,
                        tag = "dashboard_kpi_avg_imdb",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardKpiTile(
                        label = "Kazanç (dk)",
                        value = "${dashboard.totalMinutesSaved}",
                        accent = EmeraldGreen,
                        tag = "dashboard_kpi_minutes_saved",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Top Selected Genres Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CinemaSurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("dashboard_top_genres_section")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "En Sık Seçilen Türler",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        if (dashboard.dominantGenre != null) {
                            Text(
                                text = "#1 ${dashboard.dominantGenre!!.genre}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PopcornGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stacked Genre Spectrum Canvas Bar
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .testTag("dashboard_genres_spectrum_canvas")
                    ) {
                        val totalWidth = size.width
                        val height = size.height
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.08f),
                            size = Size(totalWidth, height),
                            cornerRadius = CornerRadius(height / 2f, height / 2f)
                        )
                        var currentX = 0f
                        val sumFraction = dashboard.topGenres.sumOf { it.shareFraction.toDouble() }
                            .toFloat()
                            .coerceAtLeast(0.01f)
                        dashboard.topGenres.forEachIndexed { idx, genreStat ->
                            val segWidth = (genreStat.shareFraction / sumFraction) * totalWidth * animProgress
                            if (segWidth > 1f) {
                                drawRect(
                                    color = palette[idx % palette.size],
                                    topLeft = Offset(currentX, 0f),
                                    size = Size(segWidth, height)
                                )
                                currentX += segWidth
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val maxGenreHits = (dashboard.topGenres.maxOfOrNull { it.selectionCount } ?: 1)
                        .coerceAtLeast(1)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dashboard.topGenres.forEachIndexed { index, genreStat ->
                            TopGenreProgressRow(
                                index = index,
                                stat = genreStat,
                                maxCount = maxGenreHits,
                                barColor = palette[index % palette.size],
                                progress = animProgress
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Top Selected Movies Leaderboard
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CinemaSurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("dashboard_top_movies_section")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = PopcornGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "En Sık Seçilen Filmler",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        if (dashboard.repeatPickMovieCount > 0) {
                            Surface(
                                color = PopcornGold.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${dashboard.repeatPickMovieCount} tekrar seçim",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PopcornGold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dashboard.topMovies.forEachIndexed { index, movieStat ->
                            TopMovieLeaderboardRow(
                                rank = index + 1,
                                stat = movieStat,
                                accentColor = palette[index % palette.size],
                                onClick = { onMovieClick(movieStat.movie) }
                            )
                        }
                    }
                }

                if (dashboard.modeBreakdown.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_mode_breakdown_section")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalMovies,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Karar Modu Dağılımı",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dashboard.modeBreakdown.forEachIndexed { idx, modeStat ->
                                ModeBreakdownChip(
                                    stat = modeStat,
                                    accent = palette[(idx + 1) % palette.size]
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Automated DB Insight Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            CinemaTheme.colors.selectedChipFill.copy(alpha = 0.7f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            CinemaBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                        .testTag("dashboard_insight_box"),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PopcornGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dashboard.insightText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardKpiTile(
    label: String,
    value: String,
    accent: Color,
    tag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CinemaSurfaceVariant, RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .semantics(mergeDescendants = true) {}
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = accent,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TopGenreProgressRow(
    index: Int,
    stat: TopSelectedGenreStat,
    maxCount: Int,
    barColor: Color,
    progress: Float
) {
    val fillRatio = (stat.selectionCount.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f) * progress
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .testTag("dashboard_genre_row_$index")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(barColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stat.genre,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${stat.selectionCount} seçim • %${stat.sharePercent} (${stat.uniqueMovieCount} film)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.08f),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawRoundRect(
                color = barColor,
                size = Size(size.width * fillRatio, size.height),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
private fun TopMovieLeaderboardRow(
    rank: Int,
    stat: TopSelectedMovieStat,
    accentColor: Color,
    onClick: () -> Unit
) {
    val rankBadge = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CinemaSurface)
            .border(1.dp, CinemaBorder.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag("dashboard_movie_row_${stat.movie.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rankBadge,
                fontSize = if (rank <= 3) 16.sp else 12.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        MoviePosterImage(
            title = stat.movie.title,
            posterPath = stat.movie.posterPath,
            posterUrl = stat.movie.posterUrl,
            accentColorHex = stat.movie.colorHex,
            cornerRadius = 8.dp,
            showFallbackTitleBadge = false,
            modifier = Modifier
                .width(38.dp)
                .height(54.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stat.movie.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${stat.movie.genre} • ${stat.movie.year}",
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = PopcornGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${stat.movie.imdb}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopcornGold
                    )
                }
                Text(
                    text = "• ${stat.lastSelectedDateText}",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            color = accentColor.copy(alpha = 0.18f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "${stat.selectionCount}x seçildi",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                modifier = Modifier
                    .padding(horizontal = 9.dp, vertical = 5.dp)
                    .testTag("dashboard_movie_count_${stat.movie.id}")
            )
        }
    }
}

@Composable
private fun ModeBreakdownChip(
    stat: SelectionModeStat,
    accent: Color
) {
    Surface(
        color = CinemaSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(accent, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${stat.modeCategory}: ${stat.selectionCount}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

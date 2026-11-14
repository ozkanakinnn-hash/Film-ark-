package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchedMovieEntity
import com.example.data.model.GenreDistributionItem
import com.example.data.model.computeSpunAndLikedGenreStats
import com.example.data.repository.SavedDecision
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

private enum class GenreChartFilterMode(val label: String, val tag: String) {
    COMPARE("Karşılaştır", "stats_filter_compare"),
    SPUN("Çarktan Çıkan", "stats_filter_spun"),
    LIKED("Beğenilen ❤️", "stats_filter_liked")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreStatisticsPanel(
    savedDecisions: List<SavedDecision>,
    favoriteMovieIds: Set<String>,
    watchedMovies: List<WatchedMovieEntity>,
    modifier: Modifier = Modifier
) {
    val stats = remember(savedDecisions, favoriteMovieIds, watchedMovies) {
        computeSpunAndLikedGenreStats(
            savedDecisions = savedDecisions,
            favoriteMovieIds = favoriteMovieIds,
            watchedMovies = watchedMovies
        )
    }

    var filterMode by remember { mutableStateOf(GenreChartFilterMode.COMPARE) }

    val gold = PopcornGold
    val cyan = ElectricCyan
    val pink = NeonPink
    val purple = NeonPurple
    val emerald = EmeraldGreen
    val chartPalette = remember(cyan, gold, pink, purple, emerald) {
        listOf(cyan, gold, pink, purple, emerald, Color(0xFFF97316))
    }

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700),
        label = "genre_stats_chart_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(PopcornGold.copy(alpha = 0.65f), ElectricCyan.copy(alpha = 0.65f))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("genre_statistics_panel"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Panel Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(PopcornGold.copy(alpha = 0.25f), NeonPink.copy(alpha = 0.25f))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = "Çark ve beğeni tür grafikleri",
                            tint = PopcornGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Çark & Beğeni Tür İstatistikleri",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Çarktan çıkanlar ile favori türlerinin canlı analizi",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Top KPI Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsKpiTile(
                    value = "${stats.totalSpunMovies}",
                    label = "Çarktan Çıkan",
                    accent = ElectricCyan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_kpi_spun_count")
                )
                StatsKpiTile(
                    value = "${stats.totalLikedMovies}",
                    label = "Beğenilen ❤️",
                    accent = NeonPink,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_kpi_liked_count")
                )
                StatsKpiTile(
                    value = if (stats.totalLikedMovies > 0 && stats.totalSpunMovies > 0) {
                        "%${stats.alignmentPercent}"
                    } else {
                        stats.dominantSpunGenre ?: "-"
                    },
                    label = if (stats.totalLikedMovies > 0 && stats.totalSpunMovies > 0) {
                        "Tür Uyumu"
                    } else {
                        "Baskın Tür"
                    },
                    accent = PopcornGold,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_kpi_alignment")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Mode Segmented Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GenreChartFilterMode.entries.forEach { mode ->
                    val isSelected = filterMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) CinemaTheme.colors.selectedChipFill else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) PopcornGold else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { filterMode = mode }
                            .testTag(mode.tag),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PopcornGold else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val activeItems = remember(stats.items, filterMode) {
                when (filterMode) {
                    GenreChartFilterMode.COMPARE -> stats.items
                    GenreChartFilterMode.SPUN -> stats.items.filter { it.spunCount > 0 }
                    GenreChartFilterMode.LIKED -> stats.items.filter { it.likedCount > 0 }
                }
            }

            if (activeItems.isEmpty()) {
                GenreStatsEmptyPlaceholder(mode = filterMode)
            } else {
                // CHART 1: Donut / Dual Concentric Ring Chart + Center Callout & Palette Legend
                DonutDistributionSection(
                    items = activeItems,
                    filterMode = filterMode,
                    totalSpunMovies = stats.totalSpunMovies,
                    totalLikedMovies = stats.totalLikedMovies,
                    alignmentPercent = stats.alignmentPercent,
                    dominantGenre = when (filterMode) {
                        GenreChartFilterMode.LIKED -> stats.dominantLikedGenre
                        else -> stats.dominantSpunGenre ?: stats.dominantLikedGenre
                    },
                    chartPalette = chartPalette,
                    animProgress = animProgress
                )

                Spacer(modifier = Modifier.height(18.dp))

                // CHART 2: Vertical Grouped Bar Comparison Chart (Canvas)
                VerticalGenreComparisonChart(
                    items = activeItems.take(5),
                    filterMode = filterMode,
                    animProgress = animProgress
                )

                Spacer(modifier = Modifier.height(18.dp))

                // CHART 3: Detailed Proportional Breakdown Bars
                GenreHorizontalBreakdownList(
                    items = activeItems,
                    filterMode = filterMode,
                    chartPalette = chartPalette,
                    animProgress = animProgress
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Insight Callout Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
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
                        Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.6f), PopcornGold.copy(alpha = 0.6f))),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp)
                    .testTag("genre_stats_insight_box")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = PopcornGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KARAR & BEĞENİ ANALİZİ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.9.sp,
                            color = PopcornGold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stats.insightSummary,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 17.sp
                    )

                    if (stats.totalSpunMovies > 0 || stats.totalLikedMovies > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (stats.totalSpunMovies > 0) {
                                StatsSmallBadge(
                                    text = "🎡 Çark Ort: ★ ${String.format(Locale.US, "%.1f", stats.averageSpunImdb)}",
                                    color = ElectricCyan
                                )
                            }
                            if (stats.totalLikedMovies > 0) {
                                StatsSmallBadge(
                                    text = "❤️ Beğeni Ort: ★ ${String.format(Locale.US, "%.1f", stats.averageLikedImdb)}",
                                    color = NeonPink
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsKpiTile(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CinemaSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DonutDistributionSection(
    items: List<GenreDistributionItem>,
    filterMode: GenreChartFilterMode,
    totalSpunMovies: Int,
    totalLikedMovies: Int,
    alignmentPercent: Int,
    dominantGenre: String?,
    chartPalette: List<Color>,
    animProgress: Float
) {
    val ringTrackColor = CinemaBorder
    val outerItems = remember(items) { items.filter { it.spunShare > 0f } }
    val innerItems = remember(items) { items.filter { it.likedShare > 0f } }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(144.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(144.dp)
                    .testTag("genre_distribution_donut_chart")
                    .semantics {
                        contentDescription = "Çark ve beğeni tür dağılım halkası grafiği"
                    }
            ) {
                val outerStroke = if (filterMode == GenreChartFilterMode.COMPARE) 16.dp.toPx() else 22.dp.toPx()
                val outerDiameter = size.minDimension - outerStroke
                val outerTopLeft = Offset(
                    (size.width - outerDiameter) / 2f,
                    (size.height - outerDiameter) / 2f
                )
                val outerSize = Size(outerDiameter, outerDiameter)

                drawArc(
                    color = ringTrackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = outerTopLeft,
                    size = outerSize,
                    style = Stroke(width = outerStroke)
                )

                val primaryList = when (filterMode) {
                    GenreChartFilterMode.LIKED -> innerItems
                    else -> outerItems
                }
                var startAngle = -90f
                primaryList.forEach { item ->
                    val share = if (filterMode == GenreChartFilterMode.LIKED) item.likedShare else item.spunShare
                    val idx = items.indexOfFirst { it.genre == item.genre }.coerceAtLeast(0)
                    val color = chartPalette[idx % chartPalette.size]
                    val gap = if (primaryList.size > 1) 2.5f else 0f
                    val sweep = (360f * share * animProgress - gap).coerceAtLeast(2f)

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = outerTopLeft,
                        size = outerSize,
                        style = Stroke(width = outerStroke, cap = StrokeCap.Butt)
                    )
                    startAngle += 360f * share * animProgress
                }

                // Inner ring for Liked genres in Compare mode
                if (filterMode == GenreChartFilterMode.COMPARE) {
                    val innerStroke = 11.dp.toPx()
                    val innerDiameter = outerDiameter - outerStroke - 14.dp.toPx()
                    if (innerDiameter > 24.dp.toPx()) {
                        val innerTopLeft = Offset(
                            (size.width - innerDiameter) / 2f,
                            (size.height - innerDiameter) / 2f
                        )
                        val innerSize = Size(innerDiameter, innerDiameter)

                        drawArc(
                            color = ringTrackColor.copy(alpha = 0.7f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = innerTopLeft,
                            size = innerSize,
                            style = Stroke(width = innerStroke)
                        )

                        var innerStart = -90f
                        innerItems.forEach { item ->
                            val idx = items.indexOfFirst { it.genre == item.genre }.coerceAtLeast(0)
                            val color = chartPalette[idx % chartPalette.size]
                            val gap = if (innerItems.size > 1) 2.5f else 0f
                            val sweep = (360f * item.likedShare * animProgress - gap).coerceAtLeast(2f)

                            drawArc(
                                color = color.copy(alpha = 0.88f),
                                startAngle = innerStart,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = innerTopLeft,
                                size = innerSize,
                                style = Stroke(width = innerStroke, cap = StrokeCap.Butt)
                            )
                            innerStart += 360f * item.likedShare * animProgress
                        }
                    }
                }
            }

            // Center Donut Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when {
                        filterMode == GenreChartFilterMode.COMPARE && totalLikedMovies > 0 -> "%$alignmentPercent"
                        filterMode == GenreChartFilterMode.LIKED -> "$totalLikedMovies"
                        else -> "$totalSpunMovies"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = PopcornGold
                )
                Text(
                    text = when {
                        filterMode == GenreChartFilterMode.COMPARE && totalLikedMovies > 0 -> "Uyum"
                        filterMode == GenreChartFilterMode.LIKED -> "Beğeni"
                        else -> "Dönüş"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                dominantGenre?.let {
                    Text(
                        text = it,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Compact Palette Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (filterMode == GenreChartFilterMode.COMPARE) {
                Text(
                    text = "Dış Halka: Çark • İç Halka: Beğeni",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }
            items.forEachIndexed { index, item ->
                val color = chartPalette[index % chartPalette.size]
                val share = when (filterMode) {
                    GenreChartFilterMode.LIKED -> item.likedShare
                    GenreChartFilterMode.SPUN -> item.spunShare
                    GenreChartFilterMode.COMPARE -> maxOf(item.spunShare, item.likedShare)
                }
                val pct = (share * 100f).roundToInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.genre,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = when (filterMode) {
                            GenreChartFilterMode.COMPARE -> "${item.spunCount}🎡 / ${item.likedCount}❤️"
                            GenreChartFilterMode.SPUN -> "${item.spunCount} (%$pct)"
                            GenreChartFilterMode.LIKED -> "${item.likedCount} (%$pct)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalGenreComparisonChart(
    items: List<GenreDistributionItem>,
    filterMode: GenreChartFilterMode,
    animProgress: Float
) {
    val gridColor = CinemaBorder
    val spunBarColor = ElectricCyan
    val likedBarColor = NeonPink

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CinemaSurfaceVariant)
            .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tür Bazlı Sütun Grafiği",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filterMode != GenreChartFilterMode.LIKED) {
                    LegendDotLabel(color = spunBarColor, label = "Çark")
                }
                if (filterMode != GenreChartFilterMode.SPUN) {
                    LegendDotLabel(color = likedBarColor, label = "Beğeni")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .testTag("genre_column_comparison_chart")
                .semantics {
                    contentDescription = "Türlere göre çarktan çıkan ve beğenilen film sütun grafiği"
                }
        ) {
            val chartHeight = size.height
            val chartWidth = size.width
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

            // Reference Gridlines (100%, 50%, 0%)
            listOf(0.05f, 0.50f, 0.96f).forEach { yFraction ->
                val y = chartHeight * yFraction
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = if (yFraction < 0.9f) dashEffect else null
                )
            }

            val groupCount = items.size.coerceAtLeast(1)
            val groupWidth = chartWidth / groupCount.toFloat()
            val usableBottom = chartHeight * 0.96f
            val maxBarHeight = chartHeight * 0.88f

            val maxScaleShare = items.maxOfOrNull { item ->
                when (filterMode) {
                    GenreChartFilterMode.COMPARE -> maxOf(item.spunShare, item.likedShare)
                    GenreChartFilterMode.SPUN -> item.spunShare
                    GenreChartFilterMode.LIKED -> item.likedShare
                }
            }?.coerceAtLeast(0.2f) ?: 1f

            items.forEachIndexed { index, item ->
                val centerX = groupWidth * index + groupWidth / 2f

                when (filterMode) {
                    GenreChartFilterMode.COMPARE -> {
                        val barW = (groupWidth * 0.24f).coerceIn(10.dp.toPx(), 20.dp.toPx())
                        val gap = 4.dp.toPx()

                        val spunHeight = ((item.spunShare / maxScaleShare) * maxBarHeight * animProgress)
                            .coerceAtLeast(if (item.spunCount > 0) 6.dp.toPx() else 2.dp.toPx())
                        val likedHeight = ((item.likedShare / maxScaleShare) * maxBarHeight * animProgress)
                            .coerceAtLeast(if (item.likedCount > 0) 6.dp.toPx() else 2.dp.toPx())

                        drawRoundRect(
                            color = if (item.spunCount > 0) spunBarColor else gridColor,
                            topLeft = Offset(centerX - barW - gap / 2f, usableBottom - spunHeight),
                            size = Size(barW, spunHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        drawRoundRect(
                            color = if (item.likedCount > 0) likedBarColor else gridColor,
                            topLeft = Offset(centerX + gap / 2f, usableBottom - likedHeight),
                            size = Size(barW, likedHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    GenreChartFilterMode.SPUN -> {
                        val barW = (groupWidth * 0.36f).coerceIn(14.dp.toPx(), 28.dp.toPx())
                        val barH = ((item.spunShare / maxScaleShare) * maxBarHeight * animProgress)
                            .coerceAtLeast(6.dp.toPx())
                        drawRoundRect(
                            color = spunBarColor,
                            topLeft = Offset(centerX - barW / 2f, usableBottom - barH),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                        )
                    }

                    GenreChartFilterMode.LIKED -> {
                        val barW = (groupWidth * 0.36f).coerceIn(14.dp.toPx(), 28.dp.toPx())
                        val barH = ((item.likedShare / maxScaleShare) * maxBarHeight * animProgress)
                            .coerceAtLeast(6.dp.toPx())
                        drawRoundRect(
                            color = likedBarColor,
                            topLeft = Offset(centerX - barW / 2f, usableBottom - barH),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis Labels aligned below each column group
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEach { item ->
                Text(
                    text = item.genre,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun GenreHorizontalBreakdownList(
    items: List<GenreDistributionItem>,
    filterMode: GenreChartFilterMode,
    chartPalette: List<Color>,
    animProgress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("genre_horizontal_breakdown"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, item ->
            val accentColor = chartPalette[index % chartPalette.size]
            val spunPct = (item.spunShare * 100f).roundToInt()
            val likedPct = (item.likedShare * 100f).roundToInt()

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
                                .size(8.dp)
                                .background(accentColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.genre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = when (filterMode) {
                            GenreChartFilterMode.COMPARE ->
                                "Çark ${item.spunCount} (%$spunPct) • Beğeni ${item.likedCount} (%$likedPct)"
                            GenreChartFilterMode.SPUN ->
                                "${item.spunCount} kez çıktı (%$spunPct)"
                            GenreChartFilterMode.LIKED ->
                                "${item.likedCount} kez beğenildi (%$likedPct)"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                if (filterMode != GenreChartFilterMode.LIKED) {
                    HorizontalProgressTrack(
                        fraction = (item.spunShare * animProgress),
                        hasData = item.spunCount > 0,
                        barColor = ElectricCyan
                    )
                }

                if (filterMode == GenreChartFilterMode.COMPARE) {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (filterMode != GenreChartFilterMode.SPUN) {
                    HorizontalProgressTrack(
                        fraction = (item.likedShare * animProgress),
                        hasData = item.likedCount > 0,
                        barColor = NeonPink
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizontalProgressTrack(
    fraction: Float,
    hasData: Boolean,
    barColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(CinemaBorder)
    ) {
        if (hasData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = fraction.coerceIn(0.06f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun GenreStatsEmptyPlaceholder(mode: GenreChartFilterMode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CinemaSurfaceVariant)
            .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
            .padding(20.dp)
            .testTag("genre_stats_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (mode == GenreChartFilterMode.LIKED) Icons.Default.Favorite else Icons.Default.BarChart,
            contentDescription = null,
            tint = if (mode == GenreChartFilterMode.LIKED) NeonPink else ElectricCyan,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (mode) {
                GenreChartFilterMode.LIKED ->
                    "Henüz beğendiğin bir film yok"
                else ->
                    "Henüz çark veya beğeni verisi oluşmadı"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (mode) {
                GenreChartFilterMode.LIKED ->
                    "Çark sonucunda veya profilinde beğendiğin filmlere ❤️ dokunduğunda beğeni grafiklerin burada canlı çizilecek."
                else ->
                    "Çarkı çevirdikçe ve beğendiğin filmleri favoriledikçe grafiklerin burada güncellenecek."
            },
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun LegendDotLabel(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatsSmallBadge(text: String, color: Color) {
    Surface(
        color = CinemaSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
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

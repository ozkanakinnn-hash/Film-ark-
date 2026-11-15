package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.roundToInt

data class YearFilterOption(
    val id: String,
    val label: String,
    val minYear: Int,
    val maxYear: Int
)

val YearFilterOptions = listOf(
    YearFilterOption("all", "Tüm Yıllar", 1970, 2026),
    YearFilterOption("2020s", "2020+ (Yeni)", 2020, 2026),
    YearFilterOption("2010s", "2010 - 2019", 2010, 2019),
    YearFilterOption("2000s", "2000 - 2009", 2000, 2009),
    YearFilterOption("classic", "2000 Öncesi & Kült", 1970, 1999)
)

data class GenreOption(
    val name: String,
    val emoji: String
)

val AvailableGenreOptions = listOf(
    GenreOption("Komedi", "😂"),
    GenreOption("Gerilim", "⚡"),
    GenreOption("Aksiyon", "💥"),
    GenreOption("Dram", "🎭"),
    GenreOption("Bilim Kurgu", "🚀"),
    GenreOption("Macera", "🗺️"),
    GenreOption("Romantik", "❤️"),
    GenreOption("Animasyon", "🎨"),
    GenreOption("Gizem", "🔍"),
    GenreOption("Korku", "👻"),
    GenreOption("Suç", "🕵️")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieFilterComponent(
    selectedGenres: Set<String>,
    onGenreToggle: (String) -> Unit,
    onClearGenres: () -> Unit,
    selectedYearOptionId: String,
    minYear: Int,
    maxYear: Int,
    onYearOptionSelected: (YearFilterOption) -> Unit,
    onYearRangeChange: (Int, Int) -> Unit,
    minRating: Double,
    onRatingChange: (Double) -> Unit,
    matchedMovies: List<Movie>,
    isExactMatch: Boolean = true,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(NeonPurple, PopcornGold)),
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with Reset button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CinemaTheme.colors.selectedChipFill, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filtre seçenekleri",
                            tint = PopcornGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Film Filtreleme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tür, Yıl ve Puan Kriterleri",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                OutlinedButton(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("reset_filters_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Filtreleri sıfırla",
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sıfırla",
                        fontSize = 12.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. KRİTER: TÜR SEÇİMİ (GENRES)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎬", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Film Türü",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = if (selectedGenres.isEmpty()) "Tümü seçili" else "${selectedGenres.size} tür seçildi",
                    fontSize = 12.sp,
                    color = if (selectedGenres.isEmpty()) TextMuted else PopcornGold,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Genre Chips in FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "Tümü" Chip
                val isAllSelected = selectedGenres.isEmpty()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClearGenres() }
                        .background(if (isAllSelected) CinemaTheme.colors.selectedChipFill else CinemaSurfaceVariant)
                        .border(
                            1.dp,
                            if (isAllSelected) PopcornGold else CinemaBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("genre_chip_all")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✨", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tümü",
                            fontSize = 13.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAllSelected) PopcornGold else TextSecondary
                        )
                    }
                }

                AvailableGenreOptions.forEach { genre ->
                    val isSelected = selectedGenres.contains(genre.name)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onGenreToggle(genre.name) }
                            .background(if (isSelected) CinemaTheme.colors.selectedChipFill else CinemaSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) PopcornGold else CinemaBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("genre_chip_${genre.name.lowercase(Locale.ROOT)}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = genre.emoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = genre.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PopcornGold else TextPrimary
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçili",
                                    tint = PopcornGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 2. KRİTER: YIL ARALIĞI (YEAR PERIOD + RANGE SLIDER)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📅", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Yayın Yılı",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = if (minYear > 1970 || maxYear < 2026) Color(0x3306B6D4) else Color(0x22FFFFFF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (minYear == 1970 && maxYear == 2026) "Tüm Yıllar" else "$minYear - $maxYear",
                        fontSize = 12.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Year preset chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                YearFilterOptions.forEach { yearOpt ->
                    val isSelected = yearOpt.id == selectedYearOptionId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onYearOptionSelected(yearOpt) }
                            .background(if (isSelected) CinemaTheme.colors.selectedChipFill else CinemaSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) ElectricCyan else CinemaBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("year_chip_${yearOpt.id}")
                    ) {
                        Text(
                            text = yearOpt.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ElectricCyan else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fine-grained Year Range Slider
            RangeSlider(
                value = minYear.toFloat()..maxYear.toFloat(),
                onValueChange = { range ->
                    val start = range.start.roundToInt()
                    val end = range.endInclusive.roundToInt().coerceAtLeast(start)
                    onYearRangeChange(start, end)
                },
                valueRange = 1970f..2026f,
                steps = 55,
                colors = SliderDefaults.colors(
                    thumbColor = ElectricCyan,
                    activeTrackColor = ElectricCyan,
                    inactiveTrackColor = CinemaBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("year_range_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "1970", fontSize = 11.sp, color = TextMuted)
                Text(text = "2000", fontSize = 11.sp, color = TextMuted)
                Text(text = "2026", fontSize = 11.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. KRİTER: MİNİMUM PUAN (IMDb RATING)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Minimum Puan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = if (minRating > 0.0) Color(0x33F59E0B) else Color(0x22FFFFFF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (minRating == 0.0) "Tüm Puanlar" else "★ ${String.format(Locale.US, "%.1f", minRating)}+",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (minRating > 0.0) PopcornGold else TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating Slider
            Slider(
                value = minRating.toFloat(),
                onValueChange = { onRatingChange(((it * 10).roundToInt() / 10.0)) },
                valueRange = 0f..8.5f,
                steps = 16, // increments of 0.5
                colors = SliderDefaults.colors(
                    thumbColor = PopcornGold,
                    activeTrackColor = PopcornGold,
                    inactiveTrackColor = CinemaBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rating_slider")
            )

            // Rating threshold quick shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    0.0 to "Hepsi",
                    7.0 to "7.0+",
                    7.5 to "7.5+",
                    8.0 to "8.0+",
                    8.5 to "8.5+"
                ).forEach { (score, label) ->
                    val selected = minRating == score
                    Surface(
                        color = if (selected) Color(0x33F59E0B) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRatingChange(score) }
                            .testTag("rating_chip_${label.replace("+", "_plus")}")
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = if (selected) PopcornGold else TextMuted,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. CANLI EŞLEŞME SAYACI VE FİLM ÖNİZLEMESİ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaTheme.colors.calloutSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎯", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kriterlere Uyan Filmler",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            color = if (isExactMatch) Color(0x3310B981) else Color(0x33F59E0B),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isExactMatch) "${matchedMovies.size} Film Hazır 🎉" else "${matchedMovies.size} Yakın Öneri ✨",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExactMatch) EmeraldGreen else PopcornGold,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("matched_movies_count_badge")
                            )
                        }
                    }

                    if (!isExactMatch) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tam eşleşme bulunamadı; seçimine en yakın filmler havuza alındı.",
                            fontSize = 11.sp,
                            color = PopcornGold
                        )
                    }

                    if (matchedMovies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Horizontal preview list of matched movies
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(matchedMovies.take(6)) { movie ->
                                Surface(
                                    color = CinemaSurfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${movie.title} (${movie.year})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "★${movie.imdb}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PopcornGold
                                        )
                                    }
                                }
                            }

                            if (matchedMovies.size > 6) {
                                item {
                                    Surface(
                                        color = Color(0x22FFFFFF),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "+${matchedMovies.size - 6} daha",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

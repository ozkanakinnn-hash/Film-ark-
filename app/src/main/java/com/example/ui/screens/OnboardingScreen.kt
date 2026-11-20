package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.ui.components.MoviePosterImage
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/** Seçim durumu: 1 dokunuş İzledim, 2 dokunuş Beğendim, 3 dokunuş kaldır. */
enum class TastePick { WATCHED, LIKED }

/**
 * İlk açılış: kullanıcı daha önce izlediği ve beğendiği film/dizileri seçer.
 * İzlenenler çarktan çıkar; beğenilenlerin türleri "Sana Özel" önerilerde ve çarkta öne çıkar.
 */
@Composable
fun OnboardingScreen(
    candidates: List<Movie>,
    onFinish: (watched: List<Movie>, liked: List<Movie>) -> Unit,
    onSkip: () -> Unit
) {
    BackHandler { onSkip() }
    val picks = remember { mutableStateMapOf<String, TastePick>() }
    var showSeries by remember { mutableStateOf(false) }
    val shown = candidates.filter { it.isSeries == showSeries }

    Column(modifier = Modifier.fillMaxSize().testTag("onboarding_screen")) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 16.dp),
            modifier = Modifier.weight(1f).testTag("onboarding_grid")
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(top = 22.dp, bottom = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Seni tanıyalım 🍿",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = PopcornGold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onSkip, modifier = Modifier.testTag("onboarding_skip_button")) {
                            Text("Atla", color = TextSecondary)
                        }
                    }
                    Text(
                        "Daha önce izlediklerini seç; önerileri sana göre ayarlayalım.\n" +
                            "Bir dokunuş: İzledim ✓   •   İki dokunuş: Beğendim ❤   •   Üç: kaldır",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CinemaSurface, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        listOf(false to "Filmler", true to "Diziler").forEach { (series, label) ->
                            val selected = series == showSeries
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (selected) PopcornGold else Color.Transparent)
                                    .clickable { showSeries = series }
                                    .testTag(if (series) "onboarding_tab_series" else "onboarding_tab_movies")
                            ) {
                                Text(
                                    label,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
            items(shown, key = { it.id }) { movie ->
                val pick = picks[movie.id]
                val shape = RoundedCornerShape(14.dp)
                val borderColor = when (pick) {
                    TastePick.LIKED -> NeonPink
                    TastePick.WATCHED -> EmeraldGreen
                    null -> CinemaBorder
                }
                Column(
                    modifier = Modifier
                        .clip(shape)
                        .clickable(onClickLabel = "Seçimi değiştir") {
                            val next = when (pick) {
                                null -> TastePick.WATCHED
                                TastePick.WATCHED -> TastePick.LIKED
                                TastePick.LIKED -> null
                            }
                            if (next == null) picks.remove(movie.id) else picks[movie.id] = next
                        }
                        .semantics {
                            contentDescription = movie.title + when (pick) {
                                TastePick.WATCHED -> ", izledim"
                                TastePick.LIKED -> ", beğendim"
                                null -> ""
                            }
                        }
                        .testTag("onboarding_item_${movie.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(shape)
                            .border(if (pick != null) 3.dp else 1.dp, borderColor, shape)
                    ) {
                        MoviePosterImage(
                            title = movie.title,
                            posterPath = movie.posterPath,
                            posterUrl = movie.posterUrl,
                            accentColorHex = movie.colorHex,
                            cornerRadius = 14.dp,
                            showFallbackTitleBadge = false,
                            genre = movie.genre,
                            year = movie.year,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (pick != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(44.dp)
                                    .background(borderColor, CircleShape)
                            ) {
                                Icon(
                                    if (pick == TastePick.LIKED) Icons.Default.Favorite else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        movie.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        val watchedCount = picks.values.count { it == TastePick.WATCHED }
        val likedCount = picks.values.count { it == TastePick.LIKED }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CinemaSurface)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                "$watchedCount izledim • $likedCount beğendim",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.testTag("onboarding_counter")
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val byId = candidates.associateBy { it.id }
                    onFinish(
                        picks.filterValues { it == TastePick.WATCHED }.keys.mapNotNull { byId[it] },
                        picks.filterValues { it == TastePick.LIKED }.keys.mapNotNull { byId[it] }
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_finish_button")
            ) {
                Text(
                    if (picks.isEmpty()) "Seçmeden Devam Et" else "Önerilerimi Hazırla",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

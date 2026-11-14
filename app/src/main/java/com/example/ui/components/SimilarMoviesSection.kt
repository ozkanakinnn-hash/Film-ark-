package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository.MovieRecommendation
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Displays up to 3 alternative movies similar in genre and IMDb rating to the currently
 * selected wheel movie, allowing the user to immediately switch if they already watched
 * or didn't like the wheel's pick.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimilarMoviesSection(
    targetMovie: Movie,
    isTargetWatched: Boolean,
    recommendations: List<MovieRecommendation>,
    onSelectAlternative: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (recommendations.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(listOf(ElectricCyan, NeonPurple)),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("similar_movies_module"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTargetWatched) {
                            "İzlediysen Bunlara Bak! 🎬"
                        } else {
                            "Beğenmedin mi veya İzledin mi?"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.testTag("similar_movies_header")
                    )
                }

                Surface(
                    color = Color(0x3306B6D4),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ElectricCyan)
                ) {
                    Text(
                        text = "${recommendations.size} Alternatif",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "'${targetMovie.title}' ile tür (${targetMovie.genre}) ve IMDb puanı (★ ${targetMovie.imdb}) bakımından en yakın öneriler:",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            recommendations.forEachIndexed { index, rec ->
                SimilarMovieItemCard(
                    recommendation = rec,
                    rankIndex = index + 1,
                    onSelect = { onSelectAlternative(rec.movie) }
                )
                if (index < recommendations.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimilarMovieItemCard(
    recommendation: MovieRecommendation,
    rankIndex: Int,
    onSelect: () -> Unit
) {
    val movie = recommendation.movie
    Surface(
        color = CinemaSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CinemaBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("similar_movie_card_${movie.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MoviePosterImage(
                title = movie.title,
                posterPath = movie.posterPath,
                posterUrl = movie.posterUrl,
                accentColorHex = movie.colorHex,
                cornerRadius = 10.dp,
                showFallbackTitleBadge = false,
                testTag = "similar_movie_poster_${movie.id}",
                modifier = Modifier
                    .width(58.dp)
                    .height(84.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Match pill + IMDb badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0x3310B981),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "#$rankIndex • %${recommendation.matchPercent} Benzer",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = PopcornGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${movie.imdb}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = movie.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${movie.year} • ${movie.durationMins} dk • ${movie.genre}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recommendation.reasonLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (movie.platforms.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        movie.platforms.take(2).forEach { platform ->
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = platform,
                                    fontSize = 10.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .testTag("select_alternative_button_${movie.id}")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "${movie.title} filmini seç",
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Seç",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

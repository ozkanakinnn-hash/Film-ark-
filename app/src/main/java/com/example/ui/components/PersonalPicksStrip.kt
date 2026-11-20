package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.data.repository.TasteProfileRepository
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/** Ana ekran "Sana Özel" şeridi: zevk profiline göre seçilmiş izlenmemiş yapımlar. */
@Composable
fun PersonalPicksStrip(
    picks: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (picks.isEmpty()) return
    val topGenre = remember(picks) {
        TasteProfileRepository.likedGenres().maxByOrNull { it.value }?.key
    }
    Column(modifier = modifier.fillMaxWidth().testTag("home_personal_picks")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✨ Sana Özel", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Spacer(Modifier.width(8.dp))
            if (topGenre != null) {
                Text(
                    "en sevdiğin tür: $topGenre",
                    fontSize = 11.sp,
                    color = NeonPink,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            "İzlediklerine ve beğendiklerine göre seçtik",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(picks, key = { "pp_${it.id}" }) { movie ->
                val shape = RoundedCornerShape(14.dp)
                Column(
                    modifier = Modifier
                        .width(118.dp)
                        .clip(shape)
                        .clickable(onClickLabel = "${movie.title} kartını aç") { onMovieClick(movie) }
                        .testTag("personal_pick_${movie.id}")
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .border(1.dp, CinemaBorder, shape)
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        movie.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        listOfNotNull(if (movie.isSeries) "Dizi" else null, "★ ${movie.imdb}").joinToString(" • "),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

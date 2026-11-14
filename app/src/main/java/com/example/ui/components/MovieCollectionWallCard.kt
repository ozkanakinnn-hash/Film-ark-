package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchedMovieEntity
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MovieCollectionWallCard(
    watchedMovies: List<WatchedMovieEntity>,
    favoriteMovieIds: Set<String>,
    onShareMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOnlyFavorites by remember { mutableStateOf(false) }

    val displayedItems = remember(watchedMovies, favoriteMovieIds, showOnlyFavorites) {
        val base = if (showOnlyFavorites) {
            watchedMovies.filter { it.isFavorite || favoriteMovieIds.contains(it.movieId) }
        } else {
            watchedMovies
        }
        base.take(12)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CinemaBorder, RoundedCornerShape(20.dp))
            .testTag("profile_collection_wall"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                            .size(34.dp)
                            .background(CinemaTheme.colors.selectedChipFill, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = PopcornGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Film Koleksiyonu Duvarı",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Afişe dokunarak doğrudan paylaşabilirsin",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    color = PopcornGold.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${watchedMovies.size} Afiş",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopcornGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (watchedMovies.isNotEmpty() && favoriteMovieIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WallFilterChip(
                        label = "Tümü (${watchedMovies.size})",
                        selected = !showOnlyFavorites,
                        onClick = { showOnlyFavorites = false },
                        tag = "wall_filter_all"
                    )
                    WallFilterChip(
                        label = "❤️ Favoriler",
                        selected = showOnlyFavorites,
                        onClick = { showOnlyFavorites = true },
                        tag = "wall_filter_favorites"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (displayedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CinemaSurfaceVariant)
                        .padding(18.dp)
                        .testTag("collection_wall_empty"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showOnlyFavorites) {
                            "Koleksiyon duvarında henüz favori işaretli film yok."
                        } else {
                            "İzlediğin filmlerin posterleri burada sinema duvarı olarak sergilenecek! 🎬"
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val rows = displayedItems.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { watched ->
                                val catalogMovie = remember(watched.movieId) {
                                    MovieRepository.getMovieById(watched.movieId)
                                }
                                val isFav = watched.isFavorite || favoriteMovieIds.contains(watched.movieId)
                                val shareableMovie = remember(watched, catalogMovie) {
                                    catalogMovie ?: Movie(
                                        id = watched.movieId,
                                        title = watched.title,
                                        originalTitle = watched.title,
                                        year = watched.year,
                                        durationMins = watched.durationMins,
                                        imdb = watched.imdb,
                                        moodTag = "watched",
                                        platforms = emptyList(),
                                        genre = watched.genre,
                                        logline = watched.personalNote.ifBlank { "${watched.genre} • ${watched.year}" },
                                        whyWatch = "Koleksiyonumdan öneri",
                                        posterPath = watched.posterPath ?: "",
                                        colorHex = 0xFF9F1239
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.68f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isFav) 1.5.dp else 1.dp,
                                            color = if (isFav) NeonPink else CinemaBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onShareMovie(shareableMovie) }
                                        .testTag("collection_wall_tile_${watched.movieId}")
                                ) {
                                    MoviePosterImage(
                                        title = watched.title,
                                        posterPath = watched.posterPath ?: catalogMovie?.posterPath,
                                        posterUrl = catalogMovie?.posterUrl,
                                        accentColorHex = catalogMovie?.colorHex ?: 0xFF9F1239,
                                        cornerRadius = 12.dp,
                                        showFallbackTitleBadge = false,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Bottom gradient scrim with title & share icon
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color(0xDD000000))
                                                )
                                            )
                                            .padding(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = watched.title,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Paylaş",
                                                tint = PopcornGold,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    // Top badges: IMDb + Favorite heart
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color(0xB8000000),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = PopcornGold,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${watched.imdb}",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        if (isFav) {
                                            Surface(
                                                color = NeonPink.copy(alpha = 0.9f),
                                                shape = CircleShape
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Favorite,
                                                    contentDescription = "Favori",
                                                    tint = Color.White,
                                                    modifier = Modifier
                                                        .padding(3.dp)
                                                        .size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WallFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        color = if (selected) CinemaTheme.colors.selectedChipFill else CinemaSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (selected) ElectricCyan else CinemaBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) ElectricCyan else TextSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

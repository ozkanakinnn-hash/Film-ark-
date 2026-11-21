package com.example.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NewReleasesBannerStrip(
    movies: List<Movie>,
    lastUpdatedDate: String,
    onRefreshClick: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val canLoop = movies.size >= 2
    // Sonsuz döngü: liste çok kez tekrarlanır ve ortadan başlanır, böylece iki yöne de "bitmez".
    val itemCount = if (canLoop) movies.size * LOOP_REPEAT else movies.size
    val startIndex = if (canLoop) movies.size * (LOOP_REPEAT / 2) else 0
    val firstMovieId = movies.firstOrNull()?.id
    LaunchedEffect(firstMovieId, movies.size) {
        if (firstMovieId != null) {
            listState.scrollToItem(startIndex)
        }
    }

    // Kendiliğinden kayan şerit: kullanıcı sürüklerken durur, bıraktıktan kısa süre sonra devam eder.
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    val speedPxPerSecond = with(LocalDensity.current) { AUTO_SCROLL_DP_PER_SECOND.dp.toPx() }
    val reduceMotion = com.example.data.local.LocalReduceMotion.current
    LaunchedEffect(firstMovieId, movies.size, isDragged, reduceMotion) {
        if (!canLoop || isDragged || reduceMotion) return@LaunchedEffect
        delay(AUTO_SCROLL_RESUME_DELAY_MS)
        snapshotFlow { listState.isScrollInProgress }.first { !it }
        var lastFrameNanos = -1L
        while (true) {
            // Sonsuz animasyon karesi: testlerde ve "animasyonları kapat" ayarında sistem bunu durdurabilir.
            val frameNanos = withInfiniteAnimationFrameNanos { it }
            if (lastFrameNanos > 0L) {
                val dtSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                listState.scrollBy(speedPxPerSecond * dtSeconds)
            }
            lastFrameNanos = frameNanos
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("new_releases_strip_section")
    ) {
        // Başlık (Sinemaskop konsepti): küçük aralıklı etiket + italik serif başlık + tarih
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NE İZLESEK? • HAFTALIK GÜNCEL",
                    fontSize = 9.sp,
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    maxLines = 1
                )
                Text(
                    text = "Öne Çıkanlar",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
            Text(
                text = lastUpdatedDate,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = PopcornGold,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .testTag("new_releases_date_badge")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                // Kenarlarda yumuşak solma: kartlar şeride "akarak" girip çıkar
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val fade = 22.dp.toPx() / size.width
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to Color.Transparent,
                            fade to Color.Black,
                            1f - fade to Color.Black,
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
                .testTag("new_releases_carousel")
        ) {
            items(
                count = itemCount,
                key = { index -> "new_strip_${movies[index % movies.size].id}_$index" }
            ) { index ->
                val movie = movies[index % movies.size]
                NewReleasePosterCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) }
                )
            }
        }
    }
}

private const val LOOP_REPEAT = 1000
private const val AUTO_SCROLL_DP_PER_SECOND = 32f
private const val AUTO_SCROLL_RESUME_DELAY_MS = 2500L

@Composable
private fun NewReleasePosterCard(
    movie: Movie,
    onClick: () -> Unit
) {
    val isRecentlyAdded = movie.addedDateKey != null
    val badgeColor = if (isRecentlyAdded) EmeraldGreen else NeonPink
    val badgeText = if (isRecentlyAdded) "SON EKLENEN" else "POPÜLER"
    val primaryPlatform = movie.platforms.firstOrNull() ?: "Yeni"

    Card(
        modifier = Modifier
            .width(118.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .semantics(mergeDescendants = true) {}
            .testTag("new_release_card_${movie.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
            ) {
                MoviePosterImage(
                    title = movie.title,
                    posterPath = movie.posterPath,
                    posterUrl = movie.posterUrl,
                    accentColorHex = movie.colorHex,
                    genre = movie.genre,
                    year = movie.year,
                    cornerRadius = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                )

                // Top status pill
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isRecentlyAdded) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Bottom IMDb + platform overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xE6090614))
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xCC1E1B4B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = primaryPlatform,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = Color(0xCC090614),
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
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${movie.imdb}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PopcornGold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 6.dp)
            ) {
                Text(
                    text = movie.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = movie.genre,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

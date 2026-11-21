package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.data.model.UpcomingRelease
import com.example.data.model.formatTurkishDate
import com.example.ui.components.MoviePosterImage
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/*
 * Alt menü sekmeleri: Yakında, Popüler, Diziler, Forum.
 * Hepsi 2 sütunlu kare kartlı ızgara düzeni kullanır; karta dokununca film kartı (MovieDetailSheet) açılır.
 */

@Composable
private fun TabHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = PopcornGold)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Black,
        color = TextPrimary,
        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
    )
}

/** Kare kart: poster üstte kırpılır, alt kısımda koyu geçiş üzerinde ad ve rozet. */
@Composable
private fun SquareTile(
    movie: Movie,
    badge: String?,
    badgeColor: Color,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape)
            .border(1.dp, CinemaBorder, shape)
            .clickable(onClickLabel = "${movie.title} kartını aç", onClick = onClick)
            .testTag(testTag)
    ) {
        MoviePosterImage(
            title = movie.title,
            posterPath = movie.posterPath,
            posterUrl = movie.posterUrl,
            accentColorHex = movie.colorHex,
            cornerRadius = 0.dp,
            showFallbackTitleBadge = false,
            genre = movie.genre,
            year = movie.year,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.92f)
                    )
                )
        )
        if (badge != null) {
            Surface(
                color = badgeColor,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            ) {
                Text(
                    badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
        if (movie.imdb > 0.0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE9B949), modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(2.dp))
                Text("${movie.imdb}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
            Text(
                movie.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(text, fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 24.dp))
}

private val gridPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)

@Composable
fun UpcomingScreen(
    upcoming: List<UpcomingRelease>,
    nowPlaying: List<UpcomingRelease>,
    onItemClick: (UpcomingRelease) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = gridPadding,
        modifier = Modifier.fillMaxSize().testTag("upcoming_screen_grid")
    ) {
        fullWidth { TabHeader("Yakında", "Türkiye'de sinemalarda vizyona girecek filmler") }
        fullWidth { SectionTitle("🎟️ Yakında Vizyonda") }
        if (upcoming.isEmpty()) fullWidth { EmptyNote("Şu an duyurulmuş yeni vizyon tarihi yok.") }
        upcoming.forEachIndexed { index, item ->
            item(key = "up_${item.id}", span = { GridItemSpan(maxLineSpan) }) {
                UpcomingRow(
                    item = item,
                    badge = "🎟 " + formatTurkishDate(item.releaseDate),
                    badgeColor = PopcornGold,
                    posterOnRight = index % 2 == 0,
                    testTag = "upcoming_item_${item.id}",
                    onClick = { onItemClick(item) }
                )
            }
        }
        if (nowPlaying.isNotEmpty()) {
            fullWidth { SectionTitle("🍿 Bu Ay Vizyona Girenler") }
            nowPlaying.forEachIndexed { index, item ->
                item(key = "np_${item.id}", span = { GridItemSpan(maxLineSpan) }) {
                    UpcomingRow(
                        item = item,
                        badge = "VİZYONDA • " + formatTurkishDate(item.releaseDate),
                        badgeColor = ElectricCyan,
                        posterOnRight = index % 2 == 0,
                        testTag = "now_playing_item_${item.id}",
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
        fullWidth {
            Text(
                "Vizyon tarihleri TMDB'den alınır ve değişebilir.",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/** Yakında satırı: poster sırayla sağda / solda, yanında tarih, ad, tür • süre, özet ve fragman. */
@Composable
private fun UpcomingRow(
    item: UpcomingRelease,
    badge: String,
    badgeColor: Color,
    posterOnRight: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val movie = item.asMovie()
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CinemaSurface)
            .border(1.dp, CinemaBorder, shape)
            .clickable(onClickLabel = "${item.title} kartını aç", onClick = onClick)
            .padding(12.dp)
            .testTag(testTag)
    ) {
        val poster: @Composable () -> Unit = {
            MoviePosterImage(
                title = movie.title,
                posterPath = movie.posterPath,
                posterUrl = movie.posterUrl,
                accentColorHex = movie.colorHex,
                cornerRadius = 12.dp,
                showFallbackTitleBadge = false,
                genre = movie.genre,
                year = movie.year,
                modifier = Modifier
                    .size(width = 118.dp, height = 177.dp)
                    .border(1.dp, PopcornGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            )
        }
        if (!posterOnRight) poster()
        Column(modifier = Modifier.weight(1f).height(177.dp)) {
            Text(
                badge,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1408),
                modifier = Modifier
                    .background(badgeColor, RoundedCornerShape(7.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
            Spacer(Modifier.height(7.dp))
            Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 19.sp)
            Text(
                listOfNotNull(item.genre.ifBlank { null }, item.durationMins.takeIf { it > 0 }?.let { "$it dk" }).joinToString(" • "),
                fontSize = 11.5.sp,
                color = PopcornGold,
                maxLines = 1
            )
            Spacer(Modifier.height(5.dp))
            Text(
                item.logline,
                fontSize = 11.5.sp,
                color = TextSecondary,
                lineHeight = 16.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (item.trailerKey != null) {
                Text(
                    "▶ Fragmanı İzle",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF6B7A),
                    modifier = Modifier
                        .border(1.3.dp, Color(0xFFD63447), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        if (posterOnRight) poster()
    }
}

@Composable
fun PopularGridScreen(
    title: String,
    subtitle: String,
    movies: List<Movie>,
    gridTag: String,
    itemTagPrefix: String,
    onMovieClick: (Movie) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = gridPadding,
        modifier = Modifier.fillMaxSize().testTag(gridTag)
    ) {
        fullWidth { TabHeader(title, subtitle) }
        if (movies.isEmpty()) fullWidth { EmptyNote("Liste şu an boş; internet bağlantısıyla haftalık güncelleme gelince dolacak.") }
        items(movies, key = { "${itemTagPrefix}${it.id}" }) { movie ->
            val rank = movies.indexOf(movie) + 1
            SquareTile(
                movie = movie,
                badge = if (rank <= 10) "#$rank POPÜLER" else "POPÜLER",
                badgeColor = PopcornGold,
                subtitle = listOfNotNull(
                    movie.platforms.firstOrNull()?.takeIf { it != "Diğer" },
                    if (movie.isSeries) movie.seasonCount?.let { "$it sezon" } else movie.year.toString()
                ).joinToString(" • ").ifBlank { movie.genre },
                testTag = "$itemTagPrefix${movie.id}",
                onClick = { onMovieClick(movie) }
            )
        }
    }
}

/** Forum: şimdilik yalnızca görünüm. Altyapı (hesap, moderasyon, sunucu) birlikte kurulacak. */
@Composable
fun ForumScreen() {
    val topics = listOf(
        Triple("🎬", "Film Önerileri", "Bu akşam ne izlesek? Birbirinize öneri verin"),
        Triple("📺", "Dizi Sohbetleri", "Bölüm bölüm yorumlar ve sezon tahminleri"),
        Triple("🎡", "Çark Sonuçları", "Çarkın size neyi seçtiğini paylaşın"),
        Triple("⚠️", "Spoiler Odası", "Finali konuşmak isteyenlere özel"),
        Triple("🤦", "Bunu Niye İzledim?", "En kötü film itirafları"),
        Triple("🍿", "Vizyondakiler", "Sinemada izlediklerinize puan verin")
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = gridPadding,
        modifier = Modifier.fillMaxSize().testTag("forum_screen_grid")
    ) {
        fullWidth { TabHeader("Forum", "Film ve dizi severlerle sohbet alanı") }
        fullWidth {
            Surface(
                color = PopcornGold.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PopcornGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .testTag("forum_coming_soon_banner")
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = PopcornGold, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Forum çok yakında açılıyor. Şimdilik bölümlere göz atabilirsin; konu açma ve yorum yazma henüz kapalı.",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
        items(topics, key = { it.second }) { (emoji, title, desc) ->
            val shape = RoundedCornerShape(18.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(shape)
                    .background(CinemaSurface)
                    .border(1.dp, CinemaBorder, shape)
                    .padding(14.dp)
                    .testTag("forum_topic_${title.hashCode()}"),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp).background(PopcornGold.copy(alpha = 0.14f), CircleShape)
                ) { Text(emoji, fontSize = 22.sp) }
                Column {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Black, color = TextPrimary, maxLines = 2)
                    Spacer(Modifier.height(4.dp))
                    Text(desc, fontSize = 11.sp, color = TextSecondary, maxLines = 3, lineHeight = 15.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Yakında", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

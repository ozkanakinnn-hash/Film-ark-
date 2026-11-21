package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Movie
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaMotion
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Bir filmin ayrıntı kartı: afiş, bilgiler, platformlar, özet, fragman ve eylemler.
 * Film bilgileri, fragman, "Daha Sonra İzle" ve "Çarka Ekle".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieDetailSheet(
    movie: Movie,
    onDismiss: () -> Unit,
    /** null ise "Çarka Ekle" gizlenir (ör. henüz vizyona girmemiş filmler). */
    onAddToWheel: (() -> Unit)?,
    isInWatchLater: Boolean = false,
    onToggleWatchLater: (() -> Unit)? = null
) {
    val enter = remember(movie.id) { Animatable(0f) }
    LaunchedEffect(movie.id) { enter.animateTo(1f, tween(CinemaMotion.DurationLong, easing = CinemaMotion.EmphasizedDecelerate)) }
    val shape = RoundedCornerShape(28.dp)
    val gold = CinemaTheme.colors.marqueeGold

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 24.dp)
                .graphicsLayer {
                    val p = enter.value
                    alpha = p
                    scaleX = 0.92f + 0.08f * p
                    scaleY = 0.92f + 0.08f * p
                    translationY = (1f - p) * 40.dp.toPx()
                }
                .clip(shape)
                .background(CinemaSurface)
                .border(1.dp, Brush.verticalGradient(listOf(gold.copy(alpha = 0.7f), CinemaBorder)), shape)
                .testTag("movie_detail_sheet")
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Afiş başlığı
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    MoviePosterImage(
                        title = movie.title,
                        posterPath = movie.posterPath,
                        posterUrl = movie.posterUrl,
                        accentColorHex = movie.colorHex,
                        genre = movie.genre,
                        year = movie.year,
                        cornerRadius = 0.dp,
                        testTag = "movie_detail_poster",
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to CinemaSurface))
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            .testTag("movie_detail_close")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                    }
                    if (movie.imdb > 0.0) Surface(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE9B949), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${movie.imdb}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = movie.title,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        modifier = Modifier.testTag("movie_detail_title")
                    )
                    if (movie.originalTitle.isNotBlank() && movie.originalTitle != movie.title) {
                        Text(text = movie.originalTitle, fontSize = 13.sp, color = TextMuted)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${movie.year} • ${movie.genre} • ${movie.durationLabel()}${if (movie.isSeries) " • Dizi" else ""}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        movie.platforms.forEach { platform ->
                            Surface(color = CinemaSurfaceVariant, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, CinemaBorder)) {
                                Text(platform, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
                            }
                        }
                        Surface(color = gold.copy(alpha = 0.14f), shape = RoundedCornerShape(8.dp)) {
                            Text(movie.audioBadgesText(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = gold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
                        }
                    }

                    if (movie.logline.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Text(text = movie.logline, fontSize = 14.sp, lineHeight = 20.sp, color = TextPrimary)
                    }
                    if (movie.whyWatch.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(text = "“${movie.whyWatch}”", fontSize = 13.sp, lineHeight = 18.sp, color = TextSecondary,
                            modifier = Modifier.heightIn(max = 120.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                    if (movie.trailerKey != null) {
                        TrailerButton(movie = movie)
                        Spacer(Modifier.height(10.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        if (onToggleWatchLater != null) {
                            OutlinedButton(
                                onClick = onToggleWatchLater,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, if (isInWatchLater) gold else CinemaBorder),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .weight(1.15f)
                                    .height(54.dp)
                                    .testTag("movie_detail_watch_later")
                            ) {
                                Icon(
                                    if (isInWatchLater) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (isInWatchLater) gold else TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (isInWatchLater) "İzleneceklerde" else "İzlenecek",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }
                        if (onAddToWheel != null) Button(
                            onClick = onAddToWheel,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(if (onToggleWatchLater != null) 0.85f else 1f)
                                .height(54.dp)
                                .testTag("movie_detail_open_wheel")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Çarka Ekle", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

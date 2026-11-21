package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Movie
import com.example.data.model.TmdbImageConfig

private val SGold = Color(0xFFE9B949)
private val SIvory = Color(0xFFF5EFE6)
private val SNoir = Color(0xFF0B0A0F)

/** TMDB sahne görüntüsü adresi (w780). Poster kapalıysa (kart modu) null. */
internal fun cinemaBackdropUrl(path: String?): String? =
    path?.takeIf { it.isNotBlank() }?.let { TmdbImageConfig.getPosterUrl(it)?.replace("/w500/", "/w780/") }

/**
 * Sinemaskop perde: siyah üst/alt bantlar (letterbox) arasında sahne görüntüsü, soldan karartma.
 * Üst bantta küçük etiket, alt bantta "2.39 : 1". [content] perdenin üstüne çizilir.
 */
@Composable
fun CinemaScopeBackdrop(
    backdropUrl: String?,
    topLabel: String,
    modifier: Modifier = Modifier,
    barHeight: Dp = 22.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    Box(modifier = modifier.background(Color.Black)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = barHeight)
                .background(Brush.linearGradient(listOf(Color(0xFF2A1218), Color(0xFF120A0E))))
        ) {
            if (backdropUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(backdropUrl).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Black.copy(alpha = 0.88f),
                            0.6f to Color.Black.copy(alpha = 0.4f),
                            1f to Color.Black.copy(alpha = 0.12f)
                        )
                    )
            )
        }
        Text(
            topLabel,
            fontSize = 8.5.sp,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold,
            color = SIvory.copy(alpha = 0.5f),
            maxLines = 1,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = (barHeight - 12.dp) / 2)
        )
        Text(
            "2.39 : 1",
            fontSize = 8.5.sp,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold,
            color = SIvory.copy(alpha = 0.45f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = (barHeight - 12.dp) / 2)
        )
        content()
    }
}

/** Altın "▶ ..." hap düğmesi (görsel; tıklama kartın kendisinde). */
@Composable
internal fun GoldPill(text: String, modifier: Modifier = Modifier, red: Boolean = false) {
    Text(
        text,
        fontSize = 11.sp,
        letterSpacing = 1.4.sp,
        fontWeight = FontWeight.Black,
        color = if (red) Color.White else Color(0xFF1A1408),
        maxLines = 1,
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(999.dp))
            .background(
                Brush.verticalGradient(
                    if (red) listOf(Color(0xFFEF5466), Color(0xFFC22A3C)) else listOf(Color(0xFFF3CF74), Color(0xFFD9A23A))
                ),
                RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

/**
 * Hazır çark kutusu (Sinemaskop): tam genişlik, perdede popüler yapımın sahne görüntüsü,
 * italik başlık, sağ altta üst üste küçük afişler ve "▶ ÇARKI ÇEVİR".
 */
@Composable
fun CinemaScopeWheelCard(
    title: String,
    subtitle: String,
    label: String,
    backdropPath: String?,
    posters: List<Movie>,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    CinemaScopeBackdrop(
        backdropUrl = cinemaBackdropUrl(backdropPath),
        topLabel = "NE İZLESEK? • $label",
        modifier = modifier
            .fillMaxWidth()
            .height(224.dp)
            .shadow(12.dp, shape)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
            .clickable(role = Role.Button, onClickLabel = "$title çarkını çevir", onClick = onClick)
            .testTag(testTag)
    ) {
        // Başlık tek başına tam genişlikte: hiçbir telefonda kesilmez
        Text(
            title,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 30.sp,
            color = SIvory,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(start = 16.dp, top = 36.dp, end = 12.dp).testTag("${testTag}_title")
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 36.dp, end = 176.dp)) {
            Text(
                subtitle,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                color = SIvory.copy(alpha = 0.75f),
                maxLines = 3
            )
            Spacer(Modifier.height(12.dp))
            GoldPill("▶ ÇARKI ÇEVİR")
        }
        // Sağ altta üst üste küçük afişler
        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 36.dp)) {
            posters.take(4).forEachIndexed { i, movie ->
                MoviePosterImage(
                    title = movie.title,
                    posterPath = movie.posterPath,
                    posterUrl = movie.posterUrl,
                    accentColorHex = movie.colorHex,
                    cornerRadius = 6.dp,
                    showFallbackTitleBadge = false,
                    genre = movie.genre,
                    year = movie.year,
                    modifier = Modifier
                        .offset(x = (-14 * i).dp)
                        .size(width = 48.dp, height = 72.dp)
                        .shadow(6.dp, RoundedCornerShape(6.dp))
                        .border(1.5.dp, SNoir, RoundedCornerShape(6.dp))
                )
            }
        }
    }
}

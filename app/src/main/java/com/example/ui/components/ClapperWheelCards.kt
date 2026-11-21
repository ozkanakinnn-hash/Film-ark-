package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.LocalReduceMotion
import com.example.data.model.TmdbImageConfig

private val CGold = Color(0xFFE9B949)
private val CCrimson = Color(0xFFD63447)
private val CNoir = Color(0xFF0B0A0F)
private val CIvory = Color(0xFFF5EFE6)
private val CEdge = Color(0xFF2A2530)

/** "Bunu Niye İzledim?" kartı: logo + kesik çizgili çerçeve + eğik "BUNU SEN İSTEDİN" damgası. */
@Composable
fun WorstWheelCard(onClick: () -> Unit, modifier: Modifier = Modifier, posters: List<com.example.data.model.Movie> = emptyList()) {
    val shape = RoundedCornerShape(18.dp)
    // Sinemaskop çerçeve (Popüler kutularıyla aynı dil): siyah bantlar, koyu kırmızı perde, logo + damga
    CinemaScopeBackdrop(
        backdropUrl = null,
        topLabel = "NE İZLESEK? • REZALET GECESİ",
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp)
            .shadow(12.dp, shape)
            .clip(shape)
            .border(1.dp, Color(0x33FF4D5E), shape)
            .clickable(role = Role.Button, onClickLabel = "Bunu Niye İzledim çarkını çevir", onClick = onClick)
            .testTag("home_worst_wheel_card")
    ) {
        // Sağda solgun, eğik "kötü film" afişleri
        Row(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp, top = 6.dp)) {
            posters.take(2).forEachIndexed { i, movie ->
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
                        .offset(x = (-16 * i).dp)
                        .size(width = 46.dp, height = 69.dp)
                        .rotate(-8f + i * 8f)
                        .graphicsLayer { alpha = 0.75f }
                        .border(1.5.dp, CNoir, RoundedCornerShape(6.dp))
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp, end = 92.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.brand_mark),
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.radialGradient(listOf(Color(0xFF1B1822), CNoir)))
                    .border(1.dp, CGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(7.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Bunu Niye İzledim?",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    lineHeight = 23.sp,
                    color = CIvory,
                    maxLines = 2
                )
                Text("Çok izlenen, en düşük puanlı filmler", fontSize = 11.sp, color = CIvory.copy(alpha = 0.65f))
                Spacer(Modifier.height(8.dp))
                Text(
                    "BUNU SEN İSTEDİN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF4D5E),
                    maxLines = 1,
                    modifier = Modifier
                        .rotate(-6f)
                        .border(1.5.dp, Color(0xFFFF4D5E), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/** "Bunu Niye İzledim?" çark ekranı başlığı: logo + açıklama + damga. */
@Composable
fun WorstWheelHeader(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF2A141B), Color(0xFF17121A))))
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Color(0x99FF4D5E),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx()))
                    )
                )
            }
            .padding(14.dp)
            .testTag("worst_wheel_header")
    ) {
        Image(
            painter = painterResource(id = R.drawable.brand_mark),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(CNoir)
                .padding(6.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Bunu Niye İzledim?", fontSize = 17.sp, fontWeight = FontWeight.Black, color = CIvory)
            Text("Çok izlenmiş ama puanı en düşük filmler", fontSize = 11.5.sp, color = CIvory.copy(alpha = 0.65f))
        }
        Text(
            "BUNU SEN İSTEDİN",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFF4D5E),
            maxLines = 1,
            modifier = Modifier
                .rotate(-7f)
                .border(1.5.dp, Color(0xFFFF4D5E), RoundedCornerShape(7.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

/** Rezalet Ölçer: çarktaki puan aralığı ve havuz bilgisi ("Minimum Puan" filtresinin yerine). */
@Composable
fun RezaletMeter(movies: List<com.example.data.model.Movie>, poolTotal: Int, modifier: Modifier = Modifier) {
    val ratings = movies.map { it.imdb }.filter { it > 0 }
    val lo = ratings.minOrNull() ?: 0.0
    val hi = ratings.maxOrNull() ?: 0.0
    val red = Color(0xFFFF4D5E)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF17141B))
            .border(1.dp, CEdge, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("rezalet_meter")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Rezalet Ölçer", fontSize = 13.sp, fontWeight = FontWeight.Black, color = CIvory, modifier = Modifier.weight(1f))
            Text(
                String.format(java.util.Locale.US, "★ %.1f – %.1f", lo, hi),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = red
            )
        }
        Spacer(Modifier.height(9.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.horizontalGradient(listOf(red, Color(0xFFD97706), Color(0xFF3A3340))))
        ) {
            Canvas(Modifier.fillMaxSize()) {
                // 0–10 ölçeğinde çarktaki aralığı işaretle
                listOf(lo, hi).forEach { v ->
                    val x = (v / 10.0 * size.width).toFloat()
                    drawRect(Color.White, topLeft = Offset(x - 1.5f, 0f), size = androidx.compose.ui.geometry.Size(3f, size.height))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text("Felaket", fontSize = 10.sp, color = CIvory.copy(alpha = 0.5f), modifier = Modifier.weight(1f))
            Text("Kötü", fontSize = 10.sp, color = CIvory.copy(alpha = 0.5f), modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("İdare eder", fontSize = 10.sp, color = CIvory.copy(alpha = 0.5f), modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("👥 Çok izlenen", "🎯 $poolTotal filmlik havuz", "🔄 Her çevirişte yeni").forEach { chip ->
                Text(
                    chip,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB3BB),
                    maxLines = 1,
                    modifier = Modifier
                        .background(red.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                        .border(1.dp, red.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

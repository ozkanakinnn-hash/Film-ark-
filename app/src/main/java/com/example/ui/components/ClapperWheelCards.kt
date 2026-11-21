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

/** TMDB sahne görüntüsü adresi (w780). Poster kapalıysa (kart modu) null. */
private fun backdropUrl(path: String): String? =
    TmdbImageConfig.getPosterUrl(path)?.replace("/w500/", "/w780/")

/** Klaket çubuğu: altın / siyah / kırmızı / krem çapraz şeritler. */
@Composable
private fun ClapperStick(modifier: Modifier, mirrored: Boolean) {
    val colors = listOf(CGold, CNoir, CCrimson, CNoir, CIvory, CNoir)
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CNoir)
            .border(1.5.dp, CEdge, RoundedCornerShape(4.dp))
    ) {
        val stripe = 12.dp.toPx()
        val h = size.height
        val slant = h * 0.7f * (if (mirrored) -1f else 1f)
        clipRect {
            var x = -h * 2
            var i = 0
            while (x < size.width + h * 2) {
                val p = Path().apply {
                    moveTo(x, h)
                    lineTo(x + stripe, h)
                    lineTo(x + stripe + slant, 0f)
                    lineTo(x + slant, 0f)
                    close()
                }
                drawPath(p, colors[i % colors.size])
                x += stripe
                i++
            }
        }
        if (!mirrored) {
            // Menteşe
            val r = h * 0.32f
            drawCircle(CNoir, radius = r + 2.dp.toPx(), center = Offset(h * 0.5f, h * 0.5f))
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFFF6D98C), Color(0xFF9C6D1C)), center = Offset(h * 0.42f, h * 0.42f), radius = r),
                radius = r,
                center = Offset(h * 0.5f, h * 0.5f)
            )
        }
    }
}

/** İki görüntüyü ortada flu (yumuşak) geçişle birleştirir. */
@Composable
private fun SoftBlendBackdrops(urls: List<String>, modifier: Modifier) {
    val context = LocalContext.current
    Box(modifier = modifier.background(CNoir)) {
        if (urls.size == 1) {
            // Tek görüntü: kutuyu tamamen kaplar (en popüler yapım)
            AsyncImage(
                model = ImageRequest.Builder(context).data(urls.first()).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            return@Box
        }
        urls.take(2).forEachIndexed { i, url ->
            val isLeft = i == 0
            AsyncImage(
                model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.64f)
                    .align(if (isLeft) Alignment.CenterStart else Alignment.CenterEnd)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        // Kenarı saydamlığa doğru eriterek yumuşak geçiş
                        val fade = if (isLeft) {
                            Brush.horizontalGradient(0.55f to Color.Black, 1f to Color.Transparent)
                        } else {
                            Brush.horizontalGradient(0f to Color.Transparent, 0.45f to Color.Black)
                        }
                        drawRect(fade, blendMode = BlendMode.DstIn)
                    }
            )
        }
    }
}

/**
 * Hazır çark kutusu (klaket): üstte açık klaket çubukları, gövdede son zamanlarda popüler iki yapımın
 * yazısız sahne görüntüsü (flu geçişli), solda renk şeridi, alt bantta logo + yanıp sönen başlık + ÇEVİR.
 */
@Composable
fun ClapperWheelCard(
    title: String,
    subtitle: String,
    backdropPaths: List<String>,
    accent: Color,
    redButton: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalReduceMotion.current
    val glow = if (reduceMotion) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "clapper_neon")
        val v by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 2600
                    1f at 0 using LinearEasing
                    1f at 1700
                    0.25f at 1780
                    1f at 1860
                    0.35f at 1960
                    1f at 2060
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "clapper_neon_value"
        )
        v
    }
    val urls = backdropPaths.mapNotNull { backdropUrl(it) }
    Box(
        modifier = modifier
            .clickable(role = Role.Button, onClickLabel = "$title çarkını çevir", onClick = onClick)
            .testTag(testTag)
    ) {
        // Klaket çubukları
        ClapperStick(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .height(18.dp),
            mirrored = true
        )
        ClapperStick(
            modifier = Modifier
                .padding(top = 2.dp)
                .fillMaxWidth()
                .height(18.dp)
                .graphicsLayer {
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.02f, 1f)
                    rotationZ = -9f
                },
            mirrored = false
        )
        // Gövde
        Box(
            modifier = Modifier
                .padding(top = 42.dp)
                .fillMaxWidth()
                .height(150.dp)
                .shadow(10.dp, RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                .border(1.5.dp, CEdge, RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
        ) {
            if (urls.isNotEmpty()) {
                SoftBlendBackdrops(urls, Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF221C26), CNoir))))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0.25f to CNoir.copy(alpha = 0.05f), 0.8f to CNoir.copy(alpha = 0.93f)))
            )
            Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(accent))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, end = 10.dp, bottom = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.brand_mark),
                        contentDescription = null,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CNoir)
                            .border(1.dp, CGold.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                            .padding(3.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        title,
                        style = TextStyle(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.75f + 0.25f * glow),
                            shadow = Shadow(color = accent.copy(alpha = glow), offset = Offset.Zero, blurRadius = 16f * glow + 2f)
                        ),
                        maxLines = 1,
                        modifier = Modifier.testTag("${testTag}_title")
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        subtitle,
                        fontSize = 10.sp,
                        color = CIvory.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    )
                    Text(
                        "▶ ÇEVİR",
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Black,
                        color = if (redButton) Color.White else Color(0xFF1A1408),
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    if (redButton) listOf(Color(0xFFEF5466), Color(0xFFC22A3C))
                                    else listOf(Color(0xFFF3CF74), Color(0xFFD9A23A))
                                ),
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

/** "Bunu Niye İzledim?" kartı: logo + kesik çizgili çerçeve + eğik "BUNU SEN İSTEDİN" damgası. */
@Composable
fun WorstWheelCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(Color(0xFF1F1A22), Color(0xFF151218))))
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Color(0x8094A3B8),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx()))
                    )
                )
            }
            .clickable(role = Role.Button, onClickLabel = "Bunu Niye İzledim çarkını çevir", onClick = onClick)
            .padding(14.dp)
            .testTag("home_worst_wheel_card")
    ) {
        Image(
            painter = painterResource(id = R.drawable.brand_mark),
            contentDescription = null,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.radialGradient(listOf(Color(0xFF1B1822), CNoir)))
                .padding(7.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Bunu Niye İzledim?", fontSize = 16.sp, fontWeight = FontWeight.Black, color = CIvory)
            Text("En düşük puanlı filmler çarkı", fontSize = 11.5.sp, color = CIvory.copy(alpha = 0.6f))
        }
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

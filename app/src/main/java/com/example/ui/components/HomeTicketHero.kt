package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Movie
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val Gold = Color(0xFFE9B949)
private val GoldLight = Color(0xFFF6D98C)
private val GoldDark = Color(0xFF9C6D1C)
private val Crimson = Color(0xFFD63447)
private val Noir = Color(0xFF0B0A0F)
private val Ivory = Color(0xFFF5EFE6)

private val WheelPartHeight = 318.dp
private val CouponHeight = 168.dp
private val NotchRadius = 14.dp

/**
 * Dikey sinema bileti şekli: köşeler yuvarlak; üst (çark) ile alt kupon arasındaki perforasyon hizasında
 * sol ve sağ kenarda yarım daire çentik, kupon kenarlarında küçük tırtıllar. Çentikler gerçekten kesilir.
 */
private fun verticalTicketShape(perfYdp: Float, totalHdp: Float) = GenericShape { size, _ ->
    val d = size.height / totalHdp
    val corner = 20f * d
    val notch = 14f * d
    val perfY = perfYdp * d
    val base = Path().apply { addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(corner, corner))) }
    val cuts = Path().apply {
        addOval(Rect(Offset(0f, perfY), notch))
        addOval(Rect(Offset(size.width, perfY), notch))
        val step = 14f * d
        var y = perfY + 26f * d
        while (y < size.height - 18f * d) {
            addOval(Rect(Offset(0f, y), 3.6f * d))
            addOval(Rect(Offset(size.width, y), 3.6f * d))
            y += step
        }
    }
    addPath(Path.combine(PathOperation.Difference, base, cuts))
}

/**
 * Ana sayfa bilet kartı (dikey, tek bilet).
 * - Üst bölüm: Sinemaskop perdede popüler bir filmin sahnesi, önünde film makarası Karar Çarkı + ÇEVİR
 *   → Hazır çarklar + Hızlı seçim ekranı.
 * - Perforasyondan sonra koparılan kupon: "Film Karşılaştır" + TINDER → kaydırmalı karşılaştırma.
 */
@Composable
fun HomeTicketHero(
    duelPosters: List<Movie>,
    wheelPosters: List<Movie>,
    onDuelClick: () -> Unit,
    onWheelClick: () -> Unit,
    modifier: Modifier = Modifier,
    wheelBackdropPath: String? = null
) {
    val total = WheelPartHeight + CouponHeight
    val shape = verticalTicketShape(WheelPartHeight.value, total.value)
    Column(modifier = modifier.fillMaxWidth().testTag("home_ticket_hero")) {
        Text(
            "BU AKŞAM NE İZLESEK?",
            fontSize = 11.sp,
            letterSpacing = 2.2.sp,
            fontWeight = FontWeight.Black,
            color = Gold,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(total)
                .shadow(14.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(shape)
                .background(Color(0xFF141117))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                WheelPart(
                    posters = wheelPosters,
                    backdropPath = wheelBackdropPath,
                    onClick = onWheelClick,
                    modifier = Modifier.fillMaxWidth().height(WheelPartHeight)
                )
                CompareCoupon(
                    posters = duelPosters,
                    onClick = onDuelClick,
                    modifier = Modifier.fillMaxWidth().height(CouponHeight)
                )
            }
            // Perforasyon (yatay kesik çizgi)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = WheelPartHeight.toPx()
                drawLine(
                    color = Gold.copy(alpha = 0.6f),
                    start = Offset(NotchRadius.toPx() + 8.dp.toPx(), y),
                    end = Offset(size.width - NotchRadius.toPx() - 8.dp.toPx(), y),
                    strokeWidth = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx()))
                )
            }
        }
    }
}

@Composable
private fun WheelPart(posters: List<Movie>, backdropPath: String?, onClick: () -> Unit, modifier: Modifier) {
    CinemaScopeBackdrop(
        backdropUrl = cinemaBackdropUrl(backdropPath),
        topLabel = "KARAR ÇARKI • SIRA 7 • KOLTUK 12",
        barHeight = 24.dp,
        modifier = modifier
            .clickable(role = Role.Button, onClickLabel = "Hazır çarkları ve hızlı seçimi aç", onClick = onClick)
            .semantics { contentDescription = "Karar çarkı: hazır çarklar ve hızlı seçim" }
            .testTag("home_ticket_wheel")
    ) {
        // Perdeyi biraz daha karart: çark ve yazı okunaklı kalsın
        Box(
            Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp)
                .background(Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top = 36.dp)
        ) {
            FilmReelWheel(posters = posters, size = 184.dp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Modunu seç, çark bu akşamki filmini belirlesin",
                fontSize = 12.sp,
                color = Ivory.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(10.dp))
            GoldPill("▶ ÇEVİR", modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
private fun CompareCoupon(posters: List<Movie>, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .clickable(role = Role.Button, onClickLabel = "Film karşılaştırmayı aç", onClick = onClick)
            .semantics { contentDescription = "Film Karşılaştır: iki film arasından kaydırarak seç" }
            .testTag("home_ticket_duel")
    ) {
        // Sağda iki eğik afiş + VS
        val placements = listOf(Pair(-8f, 0.dp), Pair(8f, 64.dp))
        Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 22.dp).size(width = 150.dp, height = 140.dp)) {
            posters.take(2).forEachIndexed { i, movie ->
                val (rot, x) = placements[i]
                MoviePosterImage(
                    title = movie.title,
                    posterPath = movie.posterPath,
                    posterUrl = movie.posterUrl,
                    accentColorHex = movie.colorHex,
                    cornerRadius = 10.dp,
                    showFallbackTitleBadge = false,
                    genre = movie.genre,
                    year = movie.year,
                    modifier = Modifier
                        .offset(x = x, y = 10.dp)
                        .size(width = 84.dp, height = 124.dp)
                        .rotate(rot)
                        .border(1.5.dp, Ivory.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 4.dp, y = 6.dp)
                    .size(42.dp)
                    .border(2.dp, Gold, CircleShape)
                    .padding(3.dp)
                    .background(Crimson, CircleShape)
            ) {
                Text("VS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
        // Okunabilirlik için soldan karartma
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Noir.copy(alpha = 0.97f),
                        0.5f to Noir.copy(alpha = 0.7f),
                        1f to Noir.copy(alpha = 0.0f)
                    )
                )
        )
        Column(modifier = Modifier.align(Alignment.CenterStart).padding(start = 22.dp, end = 150.dp)) {
            Text(
                "KUPON • 2 FİLM",
                fontSize = 9.5.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ivory.copy(alpha = 0.72f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Film\nKarşılaştır",
                fontSize = 27.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Black,
                color = Gold
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "TINDER",
                    fontSize = 10.5.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier
                        .background(Crimson, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Kaydır & seç", fontSize = 11.sp, color = Ivory.copy(alpha = 0.65f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Dilim şekli: yukarıyı gösteren (–90° merkezli) kama; döndürülerek her dilime yerleştirilir. */
private fun wedgeShape(sweepDeg: Float, radiusFraction: Float) = GenericShape { size, _ ->
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = min(size.width, size.height) / 2f * radiusFraction
    moveTo(c.x, c.y)
    arcTo(Rect(c, r), -90f - sweepDeg / 2f, sweepDeg, forceMoveTo = false)
    close()
}

/** Film makarası çarkı: film şeridi dış halka, afişli dilimler, logolu göbek ve kırmızı ibre. */
@Composable
fun FilmReelWheel(posters: List<Movie>, size: Dp) {
    val slices = posters.take(8)
    val n = slices.size.coerceAtLeast(1)
    val sweep = 360f / n
    val wedgeFraction = 72f / 100f
    val tints = listOf(Crimson, Noir, Gold, Noir)
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        // Afişli dilimler
        slices.forEachIndexed { i, movie ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = i * sweep }
                    .clip(wedgeShape(sweep, wedgeFraction))
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
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = size * 0.13f)
                        .size(width = size * 0.34f, height = size * 0.42f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tints[i % tints.size].copy(alpha = if (i % 2 == 1) 0.3f else 0.36f))
                )
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = center
            val outer = this.size.minDimension / 2f
            val unit = outer / 100f
            val gold = Brush.radialGradient(listOf(GoldLight, Gold, GoldDark), center = c, radius = outer)
            // Dilim ayraçları
            for (i in 0 until n) {
                val a = Math.toRadians((-90.0 - sweep / 2.0) + i * sweep)
                val r = 72f * unit
                drawLine(Gold, c, Offset(c.x + (r * cos(a)).toFloat(), c.y + (r * sin(a)).toFloat()), strokeWidth = 1.6f * unit)
            }
            // Parlaklık
            drawCircle(
                Brush.radialGradient(listOf(Color.White.copy(alpha = 0.2f), Color.Transparent), center = Offset(c.x - 20 * unit, c.y - 28 * unit), radius = 60 * unit),
                radius = 72f * unit, center = c
            )
            // İç altın halka
            drawCircle(gold, radius = 77f * unit, center = c, style = Stroke(width = 5f * unit))
            // Film şeridi halkası
            drawCircle(Color(0xFF17131B), radius = 86f * unit, center = c, style = Stroke(width = 12f * unit))
            for (k in 0 until 28) {
                rotate(degrees = k * 360f / 28f, pivot = c) {
                    drawRoundRect(
                        color = Color(0xFFE9DCC4).copy(alpha = 0.85f),
                        topLeft = Offset(c.x - 2.6f * unit, c.y - 90.5f * unit),
                        size = Size(5.2f * unit, 7f * unit),
                        cornerRadius = CornerRadius(1.4f * unit)
                    )
                }
            }
            // Dış altın çerçeve
            drawCircle(gold, radius = 96f * unit, center = c, style = Stroke(width = 3f * unit))
            // Göbek
            drawCircle(gold, radius = 30f * unit, center = c)
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFF2A2430), Noir), center = Offset(c.x - 6 * unit, c.y - 8 * unit), radius = 30 * unit),
                radius = 26.5f * unit, center = c
            )
            // Kırmızı ibre
            val needle = Path().apply {
                moveTo(c.x - 12f * unit, c.y - outer + 2f * unit)
                lineTo(c.x + 12f * unit, c.y - outer + 2f * unit)
                lineTo(c.x, c.y - outer + 24f * unit)
                close()
            }
            drawPath(needle, Crimson)
            drawCircle(Color(0xFFFFD1D6), radius = 2.6f * unit, center = Offset(c.x, c.y - outer + 7f * unit))
        }
        // Logo (soru işareti + oynat üçgeni)
        Image(
            painter = painterResource(id = R.drawable.brand_mark_hook),
            contentDescription = null,
            modifier = Modifier.size(size * 0.2f)
        )
    }
}

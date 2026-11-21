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

private val TicketHeight = 206.dp
private val StubWidth = 162.dp
private val NotchRadius = 13.dp

/**
 * Sinema bileti şekli: köşeleri yuvarlak, perforasyon hizasında üstte ve altta yarım daire çentik,
 * sol kenarda küçük tırtıllar. Çentikler gerçekten kesilir (arka plan görünür).
 */
private fun ticketShape(stubWidthPx: (Float) -> Float, notchPx: (Float) -> Float, cornerPx: (Float) -> Float) =
    GenericShape { size, _ ->
        val density = size.height / 206f // 206dp yükseklik referansı
        val corner = cornerPx(density)
        val notch = notchPx(density)
        val perfX = size.width - stubWidthPx(density)
        val base = Path().apply {
            addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(corner, corner)))
        }
        val cuts = Path().apply {
            addOval(Rect(Offset(perfX, 0f), notch))
            addOval(Rect(Offset(perfX, size.height), notch))
            // Sol kenarda bilet tırtılları
            val step = 14f * density
            var y = 22f * density
            while (y < size.height - 18f * density) {
                addOval(Rect(Offset(0f, y), 3.6f * density))
                y += step
            }
        }
        addPath(Path.combine(PathOperation.Difference, base, cuts))
    }

/**
 * Ana sayfa bilet kartı.
 * - Gövde (sol): arka planda güncel popüler film afişleri, "DÜELLO" + "TINDER" → Film Düellosu.
 * - Koçan (sağ): film şeridi halkalı, dilimlerinde afiş olan Karar Çarkı → Hazır çarklar + Hızlı seçim ekranı.
 */
@Composable
fun HomeTicketHero(
    duelPosters: List<Movie>,
    wheelPosters: List<Movie>,
    onDuelClick: () -> Unit,
    onWheelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = ticketShape(
        stubWidthPx = { d -> 162f * d },
        notchPx = { d -> 13f * d },
        cornerPx = { d -> 18f * d }
    )
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
                .height(TicketHeight)
                .shadow(14.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(shape)
                .background(Color(0xFF141117))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                DuelBody(
                    posters = duelPosters,
                    onClick = onDuelClick,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                WheelStub(
                    posters = wheelPosters,
                    onClick = onWheelClick,
                    modifier = Modifier.width(StubWidth).fillMaxHeight()
                )
            }
            // Perforasyon (kesik çizgi)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val x = size.width - StubWidth.toPx()
                drawLine(
                    color = Gold.copy(alpha = 0.55f),
                    start = Offset(x, NotchRadius.toPx() + 6.dp.toPx()),
                    end = Offset(x, size.height - NotchRadius.toPx() - 6.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx()))
                )
            }
        }
    }
}

@Composable
private fun DuelBody(posters: List<Movie>, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .clickable(role = Role.Button, onClickLabel = "Film düellosunu aç", onClick = onClick)
            .semantics { contentDescription = "Düello: iki film arasından kaydırarak seç" }
            .testTag("home_ticket_duel")
    ) {
        // Arka plandaki afişler
        val placements = listOf(
            Triple((-9f), 44.dp, 12.dp),
            Triple(7f, 104.dp, 8.dp),
            Triple(-2f, 74.dp, 40.dp)
        )
        posters.take(3).forEachIndexed { i, movie ->
            val (rot, x, y) = placements[i]
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
                    .offset(x = x, y = y)
                    .size(width = 74.dp, height = 110.dp)
                    .rotate(rot)
                    .border(1.5.dp, Ivory.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
            )
        }
        // Okunabilirlik için karartma
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Noir.copy(alpha = 0.95f),
                        0.45f to Noir.copy(alpha = 0.6f),
                        1f to Noir.copy(alpha = 0.35f)
                    )
                )
                .background(
                    Brush.verticalGradient(
                        0.4f to Color.Transparent,
                        1f to Noir.copy(alpha = 0.95f)
                    )
                )
        )
        Text(
            "GİRİŞ BİLETİ • 2 FİLM",
            fontSize = 9.5.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ivory.copy(alpha = 0.72f),
            modifier = Modifier.padding(start = 20.dp, top = 16.dp)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(x = 92.dp, y = 56.dp)
                .size(40.dp)
                .border(2.dp, Gold, CircleShape)
                .padding(3.dp)
                .background(Crimson, CircleShape)
        ) {
            Text("VS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 14.dp)) {
            Text(
                "DÜELLO",
                fontSize = 33.sp,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Black,
                color = Gold
            )
            Spacer(Modifier.height(4.dp))
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

@Composable
private fun WheelStub(posters: List<Movie>, onClick: () -> Unit, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(
                Brush.linearGradient(listOf(Color(0xFF2A1218), Color(0xFF1A0D12), Color(0xFF140A0E)))
            )
            .clickable(role = Role.Button, onClickLabel = "Hazır çarkları ve hızlı seçimi aç", onClick = onClick)
            .semantics { contentDescription = "Karar çarkı: hazır çarklar ve hızlı seçim" }
            .testTag("home_ticket_wheel")
            .padding(top = 12.dp)
    ) {
        Text("KARAR ÇARKI", fontSize = 9.5.sp, letterSpacing = 2.5.sp, fontWeight = FontWeight.Black, color = Gold)
        Spacer(Modifier.height(8.dp))
        FilmReelWheel(posters = posters, size = 112.dp)
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(999.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFF3CF74), Color(0xFFD9A23A))), RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text("▶ ÇEVİR", fontSize = 10.5.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1408))
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

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.theme.CinemaTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import kotlin.math.min

/** Lisans gerektirmeyen, uygulamanın kendi ürettiği afiş kartı tasarımları (B planı). */
enum class PosterCardStyle(val id: String, val label: String) {
    NEON_BILET("neonbilet", "Neon Bilet"),
    AFIS("afis", "Tipografik Afiş"),
    BILET("bilet", "Sinema Bileti"),
    NEON("neon", "Neon Tür İkonu");

    companion object {
        /** app/build.gradle.kts → gradle.properties: neizlesek.posterCardStyle */
        val current: PosterCardStyle
            get() = entries.firstOrNull { it.id == BuildConfig.POSTER_CARD_STYLE } ?: NEON_BILET
    }
}

private val Noir = Color(0xFF0B0A0F)
private val Ivory = Color(0xFFF6F2EA)
private val Gold = Color(0xFFE9B949)
private val Crimson = Color(0xFFD63447)

@Composable
fun GenrePosterCard(
    title: String,
    genre: String,
    year: Int?,
    modifier: Modifier = Modifier,
    style: PosterCardStyle = PosterCardStyle.current
) {
    val genreStyle = remember(genre) { resolveGenreRevealStyle(genre) }
    val isDark = CinemaTheme.colors.isDark
    BoxWithConstraints(modifier = modifier) {
        // Genişlik sınırsız ya da sıfırsa (ör. ölçüsüz bir kapsayıcı) makul bir değere düş; aksi halde
        // boyutlar sonsuz/NaN olur ve Compose çöker.
        val w = maxWidth.value.takeIf { it.isFinite() && it > 0f } ?: 160f
        val compact = w < 90f
        when (style) {
            PosterCardStyle.NEON_BILET -> NeonTicketCard(title, genre, year, genreStyle, w, compact, isDark)
            PosterCardStyle.AFIS -> AfisCard(title, genre, year, genreStyle, w, compact)
            PosterCardStyle.BILET -> BiletCard(title, genre, year, genreStyle, w, compact)
            PosterCardStyle.NEON -> NeonCard(title, genre, year, genreStyle, w, compact)
        }
    }
}

private fun genreLabel(genre: String) = genre.split("/").first().trim().uppercase(java.util.Locale("tr", "TR"))

// 1) Tipografik afiş -------------------------------------------------------------------------------
@Composable
private fun AfisCard(title: String, genre: String, year: Int?, gs: GenreRevealStyle, w: Float, compact: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(lerp(gs.backdropEnd, gs.primaryColor, 0.35f), gs.backdropStart, Noir)))
    ) {
        // Diagonal ışık şeridi
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width * 0.55f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.42f)
                lineTo(0f, size.height * 0.78f)
                lineTo(0f, size.height * 0.5f)
                close()
            }
            drawPath(path, Brush.linearGradient(listOf(gs.primaryColor.copy(alpha = 0.45f), Color.Transparent),
                start = Offset(size.width, 0f), end = Offset(0f, size.height * 0.8f)))
            val inset = min(size.width, size.height) * 0.045f
            drawRect(
                color = Gold.copy(alpha = 0.55f),
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - 2 * inset, size.height - 2 * inset),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        if (compact) {
            Text(
                text = initials(title),
                color = Ivory,
                fontSize = (w * 0.32f).sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding((w * 0.09f).dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = genreLabel(genre),
                color = gs.secondaryColor,
                fontSize = (w * 0.055f).coerceIn(8f, 13f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (w * 0.012f).sp
            )
            Text(
                text = title,
                color = Ivory,
                fontSize = (w * 0.125f).coerceIn(12f, 36f).sp,
                lineHeight = (w * 0.13f).coerceIn(13f, 38f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = year?.toString() ?: "",
                    color = Ivory.copy(alpha = 0.75f),
                    fontSize = (w * 0.06f).coerceIn(8f, 14f).sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                Image(painter = painterResource(R.drawable.brand_mark), contentDescription = null,
                    modifier = Modifier.height((w * 0.11f).coerceIn(10f, 26f).dp))
            }
        }
    }
}

// 2) Sinema bileti -------------------------------------------------------------------------------
@Composable
private fun BiletCard(title: String, genre: String, year: Int?, gs: GenreRevealStyle, w: Float, compact: Boolean) {
    Box(modifier = Modifier.fillMaxSize().background(Ivory)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Üst kızıl bant
            drawRect(Crimson, size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.2f))
            drawRect(gs.primaryColor.copy(alpha = 0.35f), topLeft = Offset(0f, size.height * 0.2f),
                size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.012f))
            // Delikli kenarlar ve kesik çizgi
            val y = size.height * 0.68f
            val r = size.width * 0.065f
            drawCircle(Noir, radius = r, center = Offset(0f, y))
            drawCircle(Noir, radius = r, center = Offset(size.width, y))
            drawLine(Noir.copy(alpha = 0.35f), Offset(r * 1.4f, y), Offset(size.width - r * 1.4f, y),
                strokeWidth = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 7f)))
        }
        if (compact) {
            Text(text = initials(title), color = Noir, fontSize = (w * 0.3f).sp, fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        val pad = (w * 0.08f).dp
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.2f).padding(horizontal = pad), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = "SİNEMA BİLETİ • ${genreLabel(genre)}",
                    color = Ivory,
                    fontSize = (w * 0.05f).coerceIn(7f, 12f).sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(modifier = Modifier.fillMaxWidth().weight(0.48f).padding(horizontal = pad), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = title,
                    color = Noir,
                    fontSize = (w * 0.115f).coerceIn(11f, 32f).sp,
                    lineHeight = (w * 0.125f).coerceIn(12f, 34f).sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(0.32f).padding(horizontal = pad),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "SEANS", color = Noir.copy(alpha = 0.5f), fontSize = (w * 0.04f).coerceIn(6f, 10f).sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(text = year?.toString() ?: "—", color = Noir, fontSize = (w * 0.075f).coerceIn(9f, 18f).sp, fontWeight = FontWeight.Black)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size((w * 0.2f).coerceIn(18f, 48f).dp)
                        .background(Noir, RoundedCornerShape(6.dp))
                ) {
                    Image(painter = painterResource(R.drawable.brand_mark), contentDescription = null,
                        modifier = Modifier.height((w * 0.13f).coerceIn(12f, 32f).dp))
                }
            }
        }
    }
}

// 3) Neon tür ikonu ------------------------------------------------------------------------------
@Composable
private fun NeonCard(title: String, genre: String, year: Int?, gs: GenreRevealStyle, w: Float, compact: Boolean) {
    Box(modifier = Modifier.fillMaxSize().background(Noir)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height * (if (compact) 0.5f else 0.38f))
            drawCircle(
                brush = Brush.radialGradient(listOf(gs.primaryColor.copy(alpha = 0.55f), gs.primaryColor.copy(alpha = 0.12f), Color.Transparent), center = c, radius = size.width * 0.55f),
                radius = size.width * 0.55f, center = c
            )
            drawCircle(color = gs.secondaryColor.copy(alpha = 0.8f), radius = size.width * 0.27f, center = c, style = Stroke(width = 1.5.dp.toPx()))
            drawCircle(color = gs.secondaryColor.copy(alpha = 0.25f), radius = size.width * 0.33f, center = c, style = Stroke(width = 1.dp.toPx()))
        }
        Icon(
            imageVector = gs.icon,
            contentDescription = null,
            tint = Ivory,
            modifier = Modifier
                .align(if (compact) Alignment.Center else Alignment.TopCenter)
                .padding(top = if (compact) 0.dp else (w * 0.38f * 1.45f - w * 0.14f).dp)
                .size((w * 0.28f).dp)
        )
        if (compact) return@Box
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding((w * 0.08f).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = TextStyle(shadow = Shadow(color = gs.primaryColor, blurRadius = 18f)),
                color = Ivory,
                fontSize = (w * 0.1f).coerceIn(11f, 28f).sp,
                lineHeight = (w * 0.11f).coerceIn(12f, 30f).sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height((w * 0.03f).dp))
            Text(
                text = listOfNotNull(genreLabel(genre), year?.toString()).joinToString("  •  "),
                color = gs.secondaryColor,
                fontSize = (w * 0.05f).coerceIn(7f, 12f).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height((w * 0.04f).dp))
            Image(painter = painterResource(R.drawable.brand_mark), contentDescription = null,
                modifier = Modifier.height((w * 0.09f).coerceIn(9f, 22f).dp))
        }
    }
}

// 4) Neon Bilet (2 + 3): neon tür ikonu üst bölümde, bilet koçanı altta; tema duyarlı ----------------

/** Yanlarında gerçek çentikler (kesik) olan bilet şekli. Çentikler [notchAt] yüksekliğindedir. */
private class TicketShape(private val notchAt: Float, private val cornerFraction: Float = 0.07f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = size.width * cornerFraction
        val notch = size.width * 0.075f
        val y = size.height * notchAt
        val path = Path().apply {
            addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(r, r)))
        }
        val holes = Path().apply {
            addOval(Rect(center = Offset(0f, y), radius = notch))
            addOval(Rect(center = Offset(size.width, y), radius = notch))
        }
        return Outline.Generic(Path.combine(PathOperation.Difference, path, holes))
    }
}

@Composable
private fun NeonTicketCard(
    title: String,
    genre: String,
    year: Int?,
    gs: GenreRevealStyle,
    w: Float,
    compact: Boolean,
    isDark: Boolean
) {
    val notchAt = if (compact) 0.66f else 0.68f
    val shape = remember(notchAt) { TicketShape(notchAt) }
    // Açık temada neon renkleri koyulaştırılır ki fildişi zeminde okunaklı kalsın
    val accent = if (isDark) gs.primaryColor else lerp(gs.primaryColor, Noir, 0.25f)
    val accent2 = if (isDark) gs.secondaryColor else lerp(gs.primaryColor, Noir, 0.45f)
    val body = if (isDark) listOf(Color(0xFF1A1720), Noir) else listOf(Color(0xFFFFFBF2), Color(0xFFF1E6D3))
    val ink = if (isDark) Ivory else Noir
    val muted = if (isDark) Ivory.copy(alpha = 0.62f) else Noir.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(Brush.verticalGradient(body))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tearY = size.height * notchAt
            val c = Offset(size.width / 2f, if (compact) tearY * 0.5f else tearY * 0.54f)
            val glowR = size.width * 0.5f
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accent.copy(alpha = if (isDark) 0.55f else 0.22f), accent.copy(alpha = if (isDark) 0.1f else 0.05f), Color.Transparent),
                    center = c, radius = glowR
                ),
                radius = glowR, center = c
            )
            drawCircle(accent2.copy(alpha = if (isDark) 0.85f else 0.7f), radius = size.width * 0.24f, center = c, style = Stroke(1.5.dp.toPx()))
            drawCircle(accent2.copy(alpha = 0.25f), radius = size.width * 0.3f, center = c, style = Stroke(1.dp.toPx()))
            // Koparma çizgisi
            val notch = size.width * 0.075f
            drawLine(
                ink.copy(alpha = if (isDark) 0.28f else 0.3f),
                Offset(notch * 1.5f, tearY), Offset(size.width - notch * 1.5f, tearY),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 6f))
            )
            // Bilet kenarı (çentikleri de izler)
            val outline = shape.createOutline(size, layoutDirection, this)
            if (outline is Outline.Generic) {
                drawPath(outline.path, accent2.copy(alpha = if (isDark) 0.45f else 0.35f), style = Stroke(1.5.dp.toPx()))
            }
        }

        // Neon tür ikonu
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(notchAt * (if (compact) 1f else 1.08f))
        ) {
            Icon(
                imageVector = gs.icon,
                contentDescription = null,
                tint = if (isDark) Ivory else accent,
                modifier = Modifier.size((w * 0.26f).dp)
            )
        }
        if (compact) return@Box

        // Üst etiket
        Text(
            text = if (w >= 130f) "SİNEMA BİLETİ • ${genreLabel(genre)}" else genreLabel(genre),
            color = accent2,
            fontSize = (w * 0.045f).coerceIn(6.5f, 11f).sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = (w * 0.08f).dp, vertical = (w * 0.07f).dp)
        )

        // Alt koçan: ad, tür • yıl, logo mührü
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .fillMaxHeight(1f - notchAt)
                .padding(horizontal = (w * 0.08f).dp, vertical = (w * 0.04f).dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = ink,
                    fontSize = (w * 0.085f).coerceIn(10f, 24f).sp,
                    lineHeight = (w * 0.095f).coerceIn(11f, 26f).sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height((w * 0.015f).dp))
                Text(
                    text = listOfNotNull(genreLabel(genre), year?.toString()).joinToString("  •  "),
                    color = muted,
                    fontSize = (w * 0.042f).coerceIn(6.5f, 11f).sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.size((w * 0.03f).dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size((w * 0.17f).coerceIn(16f, 44f).dp)
                    .background(Noir, RoundedCornerShape(6.dp))
                    .border(1.dp, Gold.copy(alpha = if (isDark) 0.45f else 0f), RoundedCornerShape(6.dp))
            ) {
                Image(painter = painterResource(R.drawable.brand_mark), contentDescription = null,
                    modifier = Modifier.height((w * 0.11f).coerceIn(10f, 28f).dp))
            }
        }
    }
}

private fun initials(title: String): String =
    title.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().toString().uppercase(java.util.Locale("tr", "TR")) }.ifBlank { "?" }

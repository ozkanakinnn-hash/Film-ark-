package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    AFIS("afis", "Tipografik Afiş"),
    BILET("bilet", "Sinema Bileti"),
    NEON("neon", "Neon Tür İkonu");

    companion object {
        /** app/build.gradle.kts → gradle.properties: neizlesek.posterCardStyle */
        val current: PosterCardStyle
            get() = entries.firstOrNull { it.id == BuildConfig.POSTER_CARD_STYLE } ?: AFIS
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
    BoxWithConstraints(modifier = modifier) {
        val w = maxWidth.value
        val compact = w < 90f
        when (style) {
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

private fun initials(title: String): String =
    title.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().toString().uppercase(java.util.Locale("tr", "TR")) }.ifBlank { "?" }

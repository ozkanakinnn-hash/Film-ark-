package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalReduceMotion
import com.example.data.model.Movie

/**
 * Veto mührü: film veto edilince afişin üstüne kırmızı "VETO" damgası vurulur.
 * Mühür büyükten iner, vurduğunda afiş sarsılır ve kararır, mürekkep dalgası yayılır; sonra [onFinished].
 * Animasyonları azalt açıksa mühür animasyonsuz basılı görünür ve kısa süre sonra kapanır.
 */
@Composable
fun VetoStampOverlay(movie: Movie, onFinished: () -> Unit) {
    val reduceMotion = LocalReduceMotion.current
    // Tek, sonlu zaman çizelgesi (0→1): bekleme, mühür iner, sarsıntı + mürekkep, bekleme. Test saatinde de kendiliğinden biter.
    val totalMs = if (reduceMotion) 700 else 1700
    val timeline = remember { Animatable(0f) }
    LaunchedEffect(movie.id) {
        timeline.animateTo(1f, tween(totalMs, easing = LinearEasing))
        onFinished()
    }
    val t = timeline.value * totalMs
    fun phase(startMs: Float, durMs: Float) = ((t - startMs) / durMs).coerceIn(0f, 1f)
    val stampValue = if (reduceMotion) 1f else FastOutSlowInEasing.transform(phase(250f, 260f))
    val inkValue = if (reduceMotion) 1f else phase(510f, 520f)
    val shakeValue = if (reduceMotion) 0f else phase(510f, 240f)
    val red = Color(0xFFE8283C)
    val landed = stampValue >= 0.999f
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
            .testTag("veto_stamp_overlay")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "VETO HAKKI KULLANILDI",
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFE9B949)
            )
            Spacer(Modifier.height(12.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 230.dp, height = 345.dp)
                    .graphicsLayer {
                        val k = if (shakeValue in 0.01f..0.99f) kotlin.math.sin(shakeValue * 30f) * (1f - shakeValue) else 0f
                        translationX = k * 10.dp.toPx()
                        translationY = k * 5.dp.toPx()
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0x59E9B949), RoundedCornerShape(16.dp))
            ) {
                MoviePosterImage(
                    title = movie.title,
                    posterPath = movie.posterPath,
                    posterUrl = movie.posterUrl,
                    accentColorHex = movie.colorHex,
                    cornerRadius = 16.dp,
                    showFallbackTitleBadge = false,
                    genre = movie.genre,
                    year = movie.year,
                    modifier = Modifier.fillMaxSize()
                )
                // Kararma
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f * inkValue)))
                // Mürekkep dalgası
                Box(
                    Modifier
                        .size(260.dp)
                        .graphicsLayer {
                            val sc = 0.2f + inkValue
                            scaleX = sc; scaleY = sc
                            alpha = (1f - inkValue) * 0.9f
                        }
                        .background(Brush.radialGradient(listOf(red.copy(alpha = 0.45f), Color.Transparent)))
                )
                // Mühür
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            val sc = 2.6f - 1.6f * stampValue
                            scaleX = sc; scaleY = sc
                            alpha = (stampValue * 1.4f).coerceAtMost(1f)
                            rotationZ = -14f
                        }
                        .border(6.dp, red, RoundedCornerShape(14.dp))
                        .background(red.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("veto_stamp")
                ) {
                    Text("VETO", fontSize = 52.sp, letterSpacing = 9.sp, fontWeight = FontWeight.Black, color = red)
                    Text(
                        "NE İZLESEK? • REDDEDİLDİ",
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black,
                        color = red,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                if (landed) "Çark bu filmi eler ve son kez döner" else " ",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

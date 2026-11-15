package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Mood
import com.example.data.model.Movie
import com.example.ui.theme.CinemaTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class GenreRevealStyle(
    val id: String,
    val badgeLabel: String,
    val headline: String,
    val subline: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backdropStart: Color,
    val backdropEnd: Color,
    val icon: ImageVector
) {
    HORROR(
        id = "horror",
        badgeLabel = "KORKU",
        headline = "🩸 KORKU SEANSI BAŞLIYOR!",
        subline = "Işıkları kapat, kapıyı kilitle ve arkana bakma! 👻",
        primaryColor = Color(0xFFEF4444),
        secondaryColor = Color(0xFFFCA5A5),
        backdropStart = Color(0xFF0E0306),
        backdropEnd = Color(0xFF2B070D),
        icon = Icons.Default.Warning
    ),
    SCI_FI(
        id = "scifi",
        badgeLabel = "BİLİM KURGU",
        headline = "🚀 BİLİM KURGU EVRENİ AÇILIYOR!",
        subline = "Solucan deliği açıldı, uzay-zaman yolculuğu başlıyor!",
        primaryColor = Color(0xFF06B6D4),
        secondaryColor = Color(0xFF818CF8),
        backdropStart = Color(0xFF050B1F),
        backdropEnd = Color(0xFF131D47),
        icon = Icons.Default.RocketLaunch
    ),
    ROMANCE(
        id = "romance",
        badgeLabel = "ROMANTİK",
        headline = "💖 ROMANTİK ATMOSFER KURULDU!",
        subline = "Kalpler hızlandı, battaniye altı sinema keyfi zamanı!",
        primaryColor = Color(0xFFF43F5E),
        secondaryColor = Color(0xFFFBCFE8),
        backdropStart = Color(0xFF1F0A18),
        backdropEnd = Color(0xFF3E122B),
        icon = Icons.Default.Favorite
    ),
    COMEDY(
        id = "comedy",
        badgeLabel = "KOMEDİ",
        headline = "😂 KAHKAHA TUFANI YAKLAŞIYOR!",
        subline = "Konfetiler patladı, yüz kasların bugünkü mesaiye hazır mı?",
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFF34D399),
        backdropStart = Color(0xFF1E1405),
        backdropEnd = Color(0xFF38230A),
        icon = Icons.Default.Celebration
    ),
    ACTION(
        id = "action",
        badgeLabel = "AKSİYON",
        headline = "🔥 ADRENALİN & AKSİYON ZİRVEDE!",
        subline = "Motorlar çalıştı; nefes kesen tempo tam gaz başlıyor!",
        primaryColor = Color(0xFFEA580C),
        secondaryColor = Color(0xFFFACC15),
        backdropStart = Color(0xFF1E0A04),
        backdropEnd = Color(0xFF3E1608),
        icon = Icons.Default.LocalFireDepartment
    ),
    THRILLER(
        id = "thriller",
        badgeLabel = "GERİLİM",
        headline = "⚡ NEFES KESEN GERİLİM & GİZEM!",
        subline = "Radarlar kilitlendi, ipuçlarını saniye saniye takip et!",
        primaryColor = Color(0xFF8B5CF6),
        secondaryColor = Color(0xFF38BDF8),
        backdropStart = Color(0xFF0B0E1A),
        backdropEnd = Color(0xFF1B1B36),
        icon = Icons.Default.Bolt
    ),
    ANIMATION(
        id = "animation",
        badgeLabel = "ANİMASYON",
        headline = "🎨 MASALSI DÜNYA CANLANIYOR!",
        subline = "Yıldız tozu ve renklerle dolu büyülü bir serüven seni bekliyor!",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFFF472B6),
        backdropStart = Color(0xFF061B1A),
        backdropEnd = Color(0xFF112F2C),
        icon = Icons.Default.AutoAwesome
    ),
    DRAMA(
        id = "drama",
        badgeLabel = "SİNEMA",
        headline = "🎭 SİNEMA SALONU IŞIKLARI KISILDI!",
        subline = "Projektör sahnede; ödüllü ve unutulmaz bir hikaye başlıyor.",
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFE11D48),
        backdropStart = Color(0xFF160B24),
        backdropEnd = Color(0xFF2D1229),
        icon = Icons.Default.TheaterComedy
    )
}

fun Movie.isHorrorMovie(): Boolean =
    moodTag == Mood.HORROR.id || genre.contains("Korku", ignoreCase = true)

fun Movie.resolveGenreRevealStyle(): GenreRevealStyle = resolveGenreRevealStyle(genre, moodTag)

/** Tür metnine (ve varsa ruh hali etiketine) göre görsel stil; film nesnesi olmadan da kullanılabilir. */
fun resolveGenreRevealStyle(genre: String, moodTag: String = ""): GenreRevealStyle {
    if (moodTag == Mood.HORROR.id || genre.contains("Korku", ignoreCase = true)) return GenreRevealStyle.HORROR

    val tokens = genre
        .split("/", ",", "•", "-")
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }

    for (token in tokens) {
        when {
            token.contains("animasyon") || token.contains("aile") -> return GenreRevealStyle.ANIMATION
            token.contains("bilim kurgu") || token.contains("sci-fi") || token.contains("fantastik") ->
                return GenreRevealStyle.SCI_FI
            token.contains("romantik") || token.contains("aşk") -> return GenreRevealStyle.ROMANCE
            token.contains("aksiyon") || token.contains("macera") -> return GenreRevealStyle.ACTION
            token.contains("komedi") -> return GenreRevealStyle.COMEDY
            token.contains("dram") -> return GenreRevealStyle.DRAMA
            token.contains("gerilim") || token.contains("gizem") || token.contains("suç") ||
                token.contains("psikolojik") -> return GenreRevealStyle.THRILLER
            token.contains("müzik") || token.contains("müzikal") -> return GenreRevealStyle.DRAMA
        }
    }

    return when (moodTag) {
        Mood.HORROR.id -> GenreRevealStyle.HORROR
        Mood.ANIMATION.id -> GenreRevealStyle.ANIMATION
        Mood.ROMANCE.id -> GenreRevealStyle.ROMANCE
        Mood.COMEDY.id -> GenreRevealStyle.COMEDY
        Mood.THRILLER.id, Mood.MIND_BENDER.id -> GenreRevealStyle.THRILLER
        else -> GenreRevealStyle.DRAMA
    }
}

/**
 * Renders a distinct, genre-aware animated reveal banner when the decision wheel stops on [movie].
 */
@Composable
fun WheelRevealAnimationBanner(
    movie: Movie,
    modifier: Modifier = Modifier
) {
    val style = remember(movie.id, movie.moodTag, movie.genre) { movie.resolveGenreRevealStyle() }
    val revealScale = remember(movie.id) { Animatable(0.84f) }
    val shakeOffsetX = remember(movie.id) { Animatable(0f) }
    val tiltDegrees = remember(movie.id) { Animatable(0f) }
    val sweepProgress = remember(movie.id) { Animatable(0f) }

    LaunchedEffect(movie.id, style) {
        revealScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = if (style == GenreRevealStyle.COMEDY || style == GenreRevealStyle.ACTION) {
                    Spring.DampingRatioLowBouncy
                } else {
                    Spring.DampingRatioMediumBouncy
                },
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    LaunchedEffect(movie.id) {
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1250, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(movie.id, style) {
        when (style) {
            GenreRevealStyle.HORROR -> {
                val offsets = listOf(-14f, 14f, -10f, 10f, -6f, 6f, 0f)
                offsets.forEach { target ->
                    shakeOffsetX.animateTo(target, animationSpec = tween(durationMillis = 48))
                }
            }
            GenreRevealStyle.ACTION -> {
                val offsets = listOf(10f, -8f, 5f, -3f, 0f)
                offsets.forEach { target ->
                    shakeOffsetX.animateTo(target, animationSpec = tween(durationMillis = 42))
                }
            }
            GenreRevealStyle.COMEDY -> {
                listOf(-2.4f, 2.2f, -1.4f, 0.8f, 0f).forEach { deg ->
                    tiltDegrees.animateTo(deg, animationSpec = tween(durationMillis = 90))
                }
            }
            else -> {
                shakeOffsetX.snapTo(0f)
                tiltDegrees.snapTo(0f)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "genre_reveal_loop")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (style) {
                    GenreRevealStyle.HORROR -> 420
                    GenreRevealStyle.ACTION -> 520
                    GenreRevealStyle.THRILLER -> 700
                    GenreRevealStyle.ROMANCE -> 950
                    else -> 1150
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "genre_reveal_pulse"
    )

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "genre_reveal_phase"
    )

    val outerTag = if (style == GenreRevealStyle.HORROR) {
        "horror_reveal_animation"
    } else {
        "cinematic_reveal_animation"
    }

    val containerStart = if (style == GenreRevealStyle.DRAMA) {
        CinemaTheme.colors.heroGradientStart
    } else {
        style.backdropStart
    }
    val containerEnd = if (style == GenreRevealStyle.DRAMA) {
        CinemaTheme.colors.heroGradientEnd
    } else {
        style.backdropEnd
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = revealScale.value
                scaleY = revealScale.value
                translationX = shakeOffsetX.value
                rotationZ = tiltDegrees.value
            }
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(containerStart, containerEnd, containerStart)))
            .border(
                width = 1.6.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        style.primaryColor.copy(alpha = 0.65f + pulse * 0.35f),
                        style.secondaryColor.copy(alpha = 0.65f + pulse * 0.35f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag(outerTag)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .testTag("genre_reveal_${style.id}")
        ) {
            drawGenreAtmosphericCanvas(
                style = style,
                pulse = pulse,
                sweep = sweepProgress.value,
                phase = phase
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                color = style.primaryColor.copy(alpha = 0.22f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, style.secondaryColor.copy(alpha = 0.65f))
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = "${style.badgeLabel} animasyonu",
                    tint = style.secondaryColor,
                    modifier = Modifier
                        .padding(9.dp)
                        .size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = style.primaryColor.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .semantics(mergeDescendants = true) {}
                            .testTag("genre_reveal_badge_${style.id}")
                    ) {
                        Text(
                            text = style.badgeLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = style.secondaryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = style.headline,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = style.secondaryColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (style == GenreRevealStyle.HORROR) {
                        "'${movie.title}' seçildi • ${style.subline}"
                    } else {
                        "'${movie.title}' • ${style.subline}"
                    },
                    fontSize = 11.sp,
                    color = Color(0xFFE9E7F2),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

private fun DrawScope.drawGenreAtmosphericCanvas(
    style: GenreRevealStyle,
    pulse: Float,
    sweep: Float,
    phase: Float
) {
    val w = size.width
    val h = size.height

    when (style) {
        GenreRevealStyle.HORROR -> {
            val flickerAlpha = 0.25f + (pulse * 0.45f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(style.primaryColor.copy(alpha = flickerAlpha), Color.Transparent),
                    center = Offset(w * 0.16f, h * 0.5f),
                    radius = w * 0.42f
                ),
                radius = w * 0.42f,
                center = Offset(w * 0.16f, h * 0.5f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(style.primaryColor.copy(alpha = flickerAlpha * 0.85f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.45f),
                    radius = w * 0.36f
                ),
                radius = w * 0.36f,
                center = Offset(w * 0.85f, h * 0.45f)
            )
            val crackPath = Path().apply {
                moveTo(w * 0.72f, 0f)
                lineTo(w * 0.68f, h * 0.32f)
                lineTo(w * 0.75f, h * 0.48f)
                lineTo(w * 0.70f, h * 0.78f)
                lineTo(w * 0.74f, h)
            }
            drawPath(
                path = crackPath,
                color = style.secondaryColor.copy(alpha = (0.35f + pulse * 0.55f) * sweep),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        GenreRevealStyle.SCI_FI -> {
            // Wormhole rings on the right + warp speed starlight streaks
            val portalCenter = Offset(w * 0.84f, h * 0.5f)
            for (ring in 1..3) {
                val radius = (12.dp.toPx() * ring) + (pulse * 4.dp.toPx())
                drawCircle(
                    color = style.primaryColor.copy(alpha = (0.45f - ring * 0.1f) * sweep),
                    radius = radius,
                    center = portalCenter,
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
            val streakOffsets = listOf(0.18f, 0.34f, 0.52f, 0.68f, 0.86f)
            streakOffsets.forEachIndexed { index, yRatio ->
                val drift = ((phase + index * 0.19f) % 1f)
                val startX = w * drift * 0.78f
                val len = 26.dp.toPx() + (index % 3) * 14.dp.toPx()
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, style.secondaryColor.copy(alpha = 0.6f))
                    ),
                    start = Offset(startX, h * yRatio),
                    end = Offset(startX + len, h * yRatio),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        GenreRevealStyle.ROMANCE -> {
            // Warm glowing bokeh + floating stylized heart curves
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(style.primaryColor.copy(alpha = 0.32f + pulse * 0.14f), Color.Transparent),
                    center = Offset(w * 0.25f, h * 0.55f),
                    radius = w * 0.35f
                ),
                radius = w * 0.35f,
                center = Offset(w * 0.25f, h * 0.55f)
            )
            val heartPositions = listOf(0.18f, 0.42f, 0.72f, 0.88f)
            heartPositions.forEachIndexed { idx, xRatio ->
                val yTravel = 1f - ((phase + idx * 0.24f) % 1f)
                val cx = w * xRatio
                val cy = h * (0.15f + yTravel * 0.75f)
                val r = (5 + (idx % 2) * 3).dp.toPx()
                val heart = Path().apply {
                    moveTo(cx, cy + r * 0.7f)
                    cubicTo(cx - r * 1.4f, cy - r * 0.2f, cx - r * 0.6f, cy - r * 1.2f, cx, cy - r * 0.35f)
                    cubicTo(cx + r * 0.6f, cy - r * 1.2f, cx + r * 1.4f, cy - r * 0.2f, cx, cy + r * 0.7f)
                    close()
                }
                drawPath(
                    path = heart,
                    color = style.secondaryColor.copy(alpha = 0.38f + pulse * 0.25f)
                )
            }
        }

        GenreRevealStyle.COMEDY -> {
            // Bursting colorful confetti ribbons & cheerful sparkles
            val palette = listOf(
                Color(0xFFFBBF24),
                Color(0xFF34D399),
                Color(0xFFF472B6),
                Color(0xFF38BDF8)
            )
            for (i in 0 until 14) {
                val xRatio = (i + 0.5f) / 14f
                val progress = (phase + i * 0.13f) % 1f
                val cx = w * xRatio + sin((progress * 2f * PI).toFloat()) * 8.dp.toPx()
                val cy = h * progress
                val color = palette[i % palette.size].copy(alpha = 0.55f)
                if (i % 2 == 0) {
                    drawRect(
                        color = color,
                        topLeft = Offset(cx, cy),
                        size = Size(7.dp.toPx(), 4.dp.toPx())
                    )
                } else {
                    drawCircle(
                        color = color,
                        radius = 3.2.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }
        }

        GenreRevealStyle.ACTION -> {
            // Kinetic diagonal slash speed lines & fiery impact flash
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(style.primaryColor.copy(alpha = 0.34f + pulse * 0.22f), Color.Transparent),
                    center = Offset(w * 0.82f, h * 0.5f),
                    radius = w * 0.38f
                ),
                radius = w * 0.38f,
                center = Offset(w * 0.82f, h * 0.5f)
            )
            for (i in 0 until 7) {
                val startX = w * (((phase * 1.3f) + i * 0.16f) % 1.1f - 0.1f)
                val yTop = (i * 11).dp.toPx() % h
                drawLine(
                    color = style.secondaryColor.copy(alpha = 0.48f * sweep),
                    start = Offset(startX, yTop),
                    end = Offset(startX + 42.dp.toPx(), yTop - 16.dp.toPx()),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        GenreRevealStyle.THRILLER -> {
            // Sonar radar arcs + sweeping tension scanline + oscilloscope pulse
            val radarCenter = Offset(w * 0.86f, h * 0.5f)
            for (rStep in 1..3) {
                val radius = (10.dp.toPx() * rStep) + (phase * 18.dp.toPx()) % 22.dp.toPx()
                drawCircle(
                    color = style.secondaryColor.copy(alpha = (0.42f - rStep * 0.08f) * sweep),
                    radius = radius,
                    center = radarCenter,
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }
            val scanX = w * phase
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, style.secondaryColor.copy(alpha = 0.45f), Color.Transparent)
                ),
                start = Offset(scanX, 0f),
                end = Offset(scanX, h),
                strokeWidth = 2.dp.toPx()
            )
        }

        GenreRevealStyle.ANIMATION -> {
            // Floating pastel fairy bubbles & 4-point twinkling stars
            val starCenters = listOf(0.12f to 0.25f, 0.36f to 0.72f, 0.64f to 0.28f, 0.88f to 0.62f)
            starCenters.forEachIndexed { index, (xr, yr) ->
                val cx = w * xr
                val cy = h * yr + sin(((phase + index * 0.2f) * 2f * PI).toFloat()) * 5.dp.toPx()
                val arm = (5 + pulse * 4).dp.toPx()
                drawLine(
                    color = style.secondaryColor.copy(alpha = 0.65f),
                    start = Offset(cx - arm, cy),
                    end = Offset(cx + arm, cy),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = style.primaryColor.copy(alpha = 0.75f),
                    start = Offset(cx, cy - arm),
                    end = Offset(cx, cy + arm),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            for (b in 0 until 6) {
                val bx = w * (0.1f + b * 0.16f)
                val by = h * (0.85f - ((phase + b * 0.17f) % 0.8f))
                drawCircle(
                    color = style.primaryColor.copy(alpha = 0.28f),
                    radius = (6 + (b % 3) * 3).dp.toPx(),
                    center = Offset(bx, by),
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }
        }

        GenreRevealStyle.DRAMA -> {
            // Sweeping theater projector spotlight + 35mm film strip sprocket holes
            val beamX = w * sweep
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(style.primaryColor.copy(alpha = 0.28f + pulse * 0.14f), Color.Transparent),
                    center = Offset(beamX, h * 0.5f),
                    radius = w * 0.36f
                ),
                radius = w * 0.36f,
                center = Offset(beamX, h * 0.5f)
            )
            val sprocketCount = 12
            val step = w / sprocketCount
            for (i in 0 until sprocketCount) {
                val x = (i * step) + step * 0.3f
                drawRect(
                    color = style.primaryColor.copy(alpha = 0.28f),
                    topLeft = Offset(x, 4.dp.toPx()),
                    size = Size(6.dp.toPx(), 3.dp.toPx())
                )
                drawRect(
                    color = style.primaryColor.copy(alpha = 0.28f),
                    topLeft = Offset(x, h - 7.dp.toPx()),
                    size = Size(6.dp.toPx(), 3.dp.toPx())
                )
            }
        }
    }
}

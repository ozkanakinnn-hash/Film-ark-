package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.ui.theme.CinemaMotion
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val REVEAL_DURATION_MS = 3800
private const val SKIP_FROM = 0.88f
/** Eylemli sonuç ekranında sahnenin durduğu nokta (çıkış solması başlamadan önce). */
private const val HOLD_AT = 0.86f
private const val STAMP_MS = 1500

/** Sonuç ekranındaki eylemler. Verilirse ekran kendiliğinden kapanmaz; kullanıcı seçim yapana kadar kalır. */
class RevealActions(
    val canVeto: Boolean,
    val onVeto: () -> Unit,
    val onWatchLater: () -> Unit,
    val onShare: () -> Unit,
    val onClose: () -> Unit
)

internal enum class RevealStamp(val title: String, val sub: String, val color: Color) {
    VETO("VETO", "NE İZLESEK? • REDDEDİLDİ", Color(0xFFE8283C)),
    WATCH("İZLENECEK", "NE İZLESEK? • LİSTEYE EKLENDİ", Color(0xFFE9B949))
}

/**
 * Çark durduktan sonra açılan tam ekran sinematik tür sahnesi.
 *
 * Tüm sahne tek bir sonlu zaman çizelgesiyle (0 → 1) yönetilir; her efekt bu ilerlemeden türetilir.
 * Böylece sahne kendiliğinden biter (sonsuz animasyon yok) ve dokunarak atlanabilir.
 */
@Composable
fun GenreRevealOverlay(
    movie: Movie,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    /** Verilirse tür başlığının yerine büyük, sallanan bir "damga" olarak gösterilir (ör. "Bunu sen istedin!"). */
    stampText: String? = null,
    /** Verilirse sahne sonunda durur ve VETO / İZLENECEK / Paylaş / Kapat düğmeleri çıkar. */
    actions: RevealActions? = null
) {
    if (actions != null) {
        // Sonuç kartı: türe özel sinematik atmosfer, büyük afiş, Veto / İzlenecek / Paylaş (ResultCardScene.kt)
        ResultCardScene(movie = movie, actions = actions, stampText = stampText, modifier = modifier)
        return
    }
    val reduceMotion = com.example.data.local.LocalReduceMotion.current
    val style = remember(movie.id) { movie.resolveGenreRevealStyle() }
    val particles = remember(movie.id) { buildParticles(movie.id.hashCode()) }
    val timeline = remember(movie.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val latestOnFinished by rememberUpdatedState(onFinished)

    val latestActions by rememberUpdatedState<RevealActions?>(actions)
    // Mühür (VETO / İZLENECEK) zaman çizelgesi: 0 → 1, bitince ilgili eylem çağrılır
    var stamp by remember(movie.id) { mutableStateOf<RevealStamp?>(null) }
    val stampT = remember(movie.id) { Animatable(0f) }

    LaunchedEffect(movie.id) {
        if (actions != null) {
            // Sonuç ekranı: sahne oynar ve durur; kullanıcı seçim yapana kadar açık kalır
            if (reduceMotion) timeline.snapTo(HOLD_AT)
            else timeline.animateTo(HOLD_AT, tween((REVEAL_DURATION_MS * HOLD_AT).toInt(), easing = LinearEasing))
            return@LaunchedEffect
        }
        timeline.animateTo(1f, tween(REVEAL_DURATION_MS, easing = LinearEasing))
        latestOnFinished()
    }

    fun playStamp(kind: RevealStamp) {
        if (stamp != null) return
        stamp = kind
        scope.launch {
            if (timeline.value < HOLD_AT) timeline.snapTo(HOLD_AT)
            if (reduceMotion) {
                // Animasyonsuz: mühür basılı halde görünür, kısa süre sonra eylem uygulanır
                stampT.snapTo(0.6f)
                stampT.animateTo(1f, tween(700, easing = LinearEasing))
            } else {
                stampT.animateTo(1f, tween(STAMP_MS, easing = LinearEasing))
            }
            val a = latestActions ?: return@launch
            if (kind == RevealStamp.VETO) a.onVeto() else a.onWatchLater()
        }
    }

    fun skip() {
        if (actions != null) {
            if (timeline.value < HOLD_AT && stamp == null) scope.launch { timeline.snapTo(HOLD_AT) }
            return
        }
        if (timeline.value >= SKIP_FROM) return
        scope.launch {
            timeline.snapTo(SKIP_FROM)
            timeline.animateTo(1f, tween(320, easing = LinearEasing))
            latestOnFinished()
        }
    }

    val t = timeline.value
    val envelope = segment(t, 0f, 0.06f) * (1f - segment(t, 0.9f, 1f))
    val shake = screenShake(style, t)

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = envelope
                val exit = segment(t, 0.9f, 1f)
                scaleX = 1f + exit * 0.05f
                scaleY = 1f + exit * 0.05f
            }
            .pointerInput(movie.id) { detectTapGestures { skip() } }
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "Seçilen film: ${movie.title}, ${style.badgeLabel}"
            }
            .testTag("genre_reveal_overlay")
    ) {
        // Arka plan katmanı: tür atmosferi
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(style.backdropStart, style.backdropEnd, style.backdropStart)))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(style.primaryColor.copy(alpha = 0.38f * segment(t, 0.08f, 0.4f)), Color.Transparent),
                    center = Offset(size.width / 2f, size.height * 0.42f),
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = Offset(size.width / 2f, size.height * 0.42f)
            )
            drawBackgroundFx(style, t, particles)
        }

        // Orta sahne: afiş + bilgiler. Afiş boyu ekran yüksekliğine göre ayarlanır; küçük ekranlarda içerik kayar.
        androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val posterH = if (actions != null) (maxHeight * 0.42f).coerceIn(170.dp, 330.dp) else 260.dp
        val posterW = if (actions != null) posterH * 0.68f else 176.dp
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .then(if (actions != null) Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()) else Modifier)
                .padding(horizontal = 28.dp, vertical = if (actions != null) 56.dp else 0.dp)
                .graphicsLayer {
                    translationX = shake.x * density
                    translationY = shake.y * density
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val badgeIn = segment(t, 0.06f, 0.24f)
            Surface(
                color = style.primaryColor.copy(alpha = 0.22f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, style.secondaryColor.copy(alpha = 0.7f)),
                modifier = Modifier.graphicsLayer {
                    alpha = badgeIn
                    translationY = (1f - CinemaMotion.EmphasizedDecelerate.transform(badgeIn)) * -16.dp.toPx()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(style.icon, contentDescription = null, tint = style.secondaryColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = style.badgeLabel,
                        color = style.secondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            val posterIn = segment(t, 0.12f, 0.42f)
            val posterEase = EaseOutBack.transform(posterIn)
            // Mühür vuruşu: sarsıntı + kararma (afiş kutusunun tamamına uygulanır)
            val sv = stampT.value
            val landPhase = ((sv * STAMP_MS - 260f) / 300f).coerceIn(0f, 1f)
            val stampShake = if (landPhase in 0.01f..0.99f) sin(landPhase * 30f) * (1f - landPhase) else 0f
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer {
                    translationX = stampShake * 10.dp.toPx()
                    translationY = stampShake * 5.dp.toPx()
                }
            ) {
            MoviePosterImage(
                title = movie.title,
                posterPath = movie.posterPath,
                posterUrl = movie.posterUrl,
                accentColorHex = movie.colorHex,
                genre = movie.genre,
                year = movie.year,
                cornerRadius = 18.dp,
                testTag = "genre_reveal_overlay_poster",
                modifier = Modifier
                    .width(posterW)
                    .height(posterH)
                    .graphicsLayer {
                        alpha = segment(t, 0.12f, 0.22f)
                        val s = 0.55f + 0.45f * posterEase
                        scaleX = s
                        scaleY = s
                        rotationY = (1f - CinemaMotion.EmphasizedDecelerate.transform(posterIn)) * -80f
                        cameraDistance = 14f * density
                    }
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = style.primaryColor,
                        spotColor = style.primaryColor
                    )
                    .border(1.5.dp, style.secondaryColor.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            )
            // Işık süzmesi: afiş yerine oturunca soldan sağa geçen parlama
            val shine = segment(t, 0.44f, 0.66f)
            if (shine in 0.001f..0.999f) {
                Canvas(
                    modifier = Modifier
                        .width(posterW)
                        .height(posterH)
                ) {
                    val x = -size.width * 0.6f + size.width * 2.2f * shine
                    drawRect(
                        Brush.linearGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.32f), Color.Transparent),
                            start = Offset(x, 0f),
                            end = Offset(x + size.width * 0.5f, size.height)
                        )
                    )
                }
            }
            stamp?.let { kind ->
                val drop = androidx.compose.animation.core.FastOutSlowInEasing.transform(((sv * STAMP_MS - 260f) / 260f).coerceIn(0f, 1f))
                Box(
                    Modifier
                        .width(posterW)
                        .height(posterH)
                        .background(Color.Black.copy(alpha = 0.5f * landPhase), RoundedCornerShape(18.dp))
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            val sc = 2.6f - 1.6f * drop
                            scaleX = sc; scaleY = sc
                            alpha = (drop * 1.4f).coerceAtMost(1f)
                            rotationZ = -14f
                        }
                        .border(5.dp, kind.color, RoundedCornerShape(14.dp))
                        .background(kind.color.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                        .testTag(if (kind == RevealStamp.VETO) "reveal_veto_stamp" else "reveal_watch_stamp")
                ) {
                    Text(kind.title, fontSize = if (kind == RevealStamp.VETO) 46.sp else 34.sp, letterSpacing = 6.sp, fontWeight = FontWeight.Black, color = kind.color)
                    Text(kind.sub, fontSize = 9.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Black, color = kind.color)
                }
            }
            }

            Spacer(Modifier.height(24.dp))

            val titleIn = segment(t, 0.34f, 0.54f)
            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer {
                    alpha = titleIn
                    translationY = (1f - CinemaMotion.EmphasizedDecelerate.transform(titleIn)) * 28.dp.toPx()
                    if (style == GenreRevealStyle.THRILLER && t < 0.5f) {
                        translationX = glitchOffset(t) * 6.dp.toPx()
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            val metaIn = segment(t, 0.42f, 0.62f)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = metaIn
                    translationY = (1f - CinemaMotion.EmphasizedDecelerate.transform(metaIn)) * 20.dp.toPx()
                }
            ) {
                Text(
                    text = "${movie.year}  •  ${movie.durationLabel()}  •  ",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(3.dp))
                Text(
                    text = String.format(Locale.US, "%.1f", movie.imdb),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            val headlineIn = segment(t, 0.5f, 0.7f)
            if (stampText != null) {
                // "Bunu Niye İzledim?" çarkı: kırmızı mühür gibi çarpıp hafifçe sallanan yazı
                val stampIn = segment(t, 0.46f, 0.6f)
                val wobble = kotlin.math.sin(t * 40f) * (1f - segment(t, 0.6f, 0.8f)) * 4f
                Text(
                    text = stampText,
                    color = Color(0xFFFF4D5E),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = stampIn
                            val sc = 2.2f - 1.2f * CinemaMotion.EmphasizedDecelerate.transform(stampIn)
                            scaleX = sc
                            scaleY = sc
                            rotationZ = -8f + wobble
                        }
                        .border(3.dp, Color(0xFFFF4D5E).copy(alpha = 0.9f * stampIn), RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("reveal_stamp_text")
                )
                Spacer(Modifier.height(10.dp))
            }
            Text(
                text = style.headline,
                color = style.secondaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = headlineIn
                    val s = 0.9f + 0.1f * headlineIn
                    scaleX = s
                    scaleY = s
                }
            )
            if (actions != null) {
                val actionsIn = if (reduceMotion) 1f else segment(t, 0.6f, 0.8f)
                Spacer(Modifier.height(20.dp))
                RevealActionRow(
                    actions = actions,
                    enabled = stamp == null && actionsIn > 0.5f,
                    onVeto = { playStamp(RevealStamp.VETO) },
                    onWatch = { playStamp(RevealStamp.WATCH) },
                    modifier = Modifier.graphicsLayer {
                        alpha = actionsIn
                        translationY = (1f - CinemaMotion.EmphasizedDecelerate.transform(actionsIn)) * 24.dp.toPx()
                    }
                )
            }
        }

        }

        // Ön katman: perde, parlama, karartma gibi sahneyi örten efektler
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawForegroundFx(style, t)
        }

        if (actions != null) {
            Text(
                "✕",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 18.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                    .clickable(onClickLabel = "Sonucu kapat") { if (stamp == null) actions.onClose() }
                    .padding(top = 7.dp)
                    .testTag("reveal_close_button")
            )
        } else Text(
            text = "Geçmek için dokun",
            color = Color.White.copy(alpha = 0.45f * segment(t, 0.6f, 0.75f)),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .background(Color.Black.copy(alpha = 0.18f * segment(t, 0.6f, 0.75f)), RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Zaman çizelgesi yardımcıları
// ---------------------------------------------------------------------------------------------

/** [t] değerini [start, end] aralığında 0..1'e eşler. */
private fun segment(t: Float, start: Float, end: Float): Float =
    ((t - start) / (end - start)).coerceIn(0f, 1f)

private val EaseOutBack: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

private fun glitchOffset(t: Float): Float {
    val frame = (t * 40f).toInt()
    val r = Random(frame * 31 + 7).nextFloat()
    return if (r > 0.72f) (r - 0.86f) * 4f else 0f
}

/** Korku ve aksiyon için sönümlenen ekran sarsıntısı (dp cinsinden). */
private fun screenShake(style: GenreRevealStyle, t: Float): Offset {
    val (start, end, amp) = when (style) {
        GenreRevealStyle.HORROR -> Triple(0.14f, 0.4f, 9f)
        GenreRevealStyle.ACTION -> Triple(0.05f, 0.3f, 12f)
        else -> return Offset.Zero
    }
    if (t < start || t > end) return Offset.Zero
    val p = segment(t, start, end)
    val decay = (1f - p) * (1f - p)
    return Offset(
        sin(p * 70f) * amp * decay,
        cos(p * 53f) * amp * 0.6f * decay
    )
}

private class Particle(
    val x: Float,
    val y: Float,
    val angle: Float,
    val speed: Float,
    val size: Float,
    val spin: Float,
    val delay: Float,
    val colorSeed: Int
)

private fun buildParticles(seed: Int): List<Particle> {
    val rnd = Random(seed)
    return List(140) {
        Particle(
            x = rnd.nextFloat(),
            y = rnd.nextFloat(),
            angle = rnd.nextFloat() * 2f * PI.toFloat(),
            speed = 0.4f + rnd.nextFloat() * 0.6f,
            size = rnd.nextFloat(),
            spin = (rnd.nextFloat() - 0.5f) * 2f,
            delay = rnd.nextFloat(),
            colorSeed = rnd.nextInt(1000)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Arka plan efektleri (afişin arkasında)
// ---------------------------------------------------------------------------------------------

private fun DrawScope.drawBackgroundFx(style: GenreRevealStyle, t: Float, particles: List<Particle>) {
    when (style) {
        GenreRevealStyle.HORROR -> drawHorrorBackground(style, t)
        GenreRevealStyle.SCI_FI -> drawWarpField(style, t, particles)
        GenreRevealStyle.ROMANCE -> drawRisingHearts(style, t, particles)
        GenreRevealStyle.COMEDY -> drawConfettiBurst(t, particles)
        GenreRevealStyle.ACTION -> drawExplosion(style, t, particles)
        GenreRevealStyle.THRILLER -> drawRadar(style, t)
        GenreRevealStyle.ANIMATION -> drawSparkleSwirl(style, t, particles)
        GenreRevealStyle.DRAMA -> drawSpotlight(style, t)
    }
}

private fun DrawScope.drawHorrorBackground(style: GenreRevealStyle, t: Float) {
    val w = size.width
    val h = size.height
    // Nabız gibi atan kızıl vinyet
    val beat = 0.5f + 0.5f * sin(t * 2f * PI.toFloat() * 4f)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, style.primaryColor.copy(alpha = 0.25f + 0.25f * beat)),
            center = Offset(w / 2f, h / 2f),
            radius = maxOf(w, h) * 0.75f
        )
    )
    // Yukarıdan süzülen kan damlaları
    val drips = 16
    for (i in 0 until drips) {
        val rnd = Random(i * 97 + 13)
        val x = w * (i + 0.5f) / drips + (rnd.nextFloat() - 0.5f) * 18.dp.toPx()
        val startAt = 0.12f + rnd.nextFloat() * 0.25f
        val grow = CinemaMotion.EmphasizedDecelerate.transform(segment(t, startAt, startAt + 0.45f))
        val length = h * (0.08f + rnd.nextFloat() * 0.3f) * grow
        val width = (4f + rnd.nextFloat() * 7f).dp.toPx()
        if (length <= 0f) continue
        val blood = Color(0xFF7A0710)
        drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF3A0207), blood), startY = 0f, endY = length),
            topLeft = Offset(x - width / 2f, 0f),
            size = Size(width, length)
        )
        drawCircle(color = blood, radius = width * 0.75f, center = Offset(x, length))
    }
}

private fun DrawScope.drawWarpField(style: GenreRevealStyle, t: Float, particles: List<Particle>) {
    val c = Offset(size.width / 2f, size.height * 0.42f)
    val maxR = maxOf(size.width, size.height) * 0.8f
    val warp = segment(t, 0.02f, 0.7f)
    particles.take(110).forEach { p ->
        val travel = ((p.delay + t * (0.6f + p.speed)) % 1f)
        val dist = travel * travel * maxR
        val streak = (8f + 120f * warp * p.speed).dp.toPx() * travel
        val dx = cos(p.angle)
        val dy = sin(p.angle)
        val color = if (p.colorSeed % 3 == 0) style.secondaryColor else Color.White
        drawLine(
            color = color.copy(alpha = (0.15f + 0.85f * travel) * 0.9f),
            start = Offset(c.x + dx * (dist - streak).coerceAtLeast(0f), c.y + dy * (dist - streak).coerceAtLeast(0f)),
            end = Offset(c.x + dx * dist, c.y + dy * dist),
            strokeWidth = (1f + 1.6f * p.size).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    for (ring in 0 until 3) {
        val p = segment(t, 0.06f + ring * 0.1f, 0.6f + ring * 0.1f)
        if (p <= 0f || p >= 1f) continue
        drawCircle(
            color = style.primaryColor.copy(alpha = (1f - p) * 0.7f),
            radius = 40.dp.toPx() + p * maxR * 0.7f,
            center = c,
            style = Stroke(width = (6f * (1f - p) + 1f).dp.toPx())
        )
    }
}

private fun heartPath(cx: Float, cy: Float, r: Float): Path = Path().apply {
    moveTo(cx, cy + r * 0.9f)
    cubicTo(cx - r * 1.8f, cy - r * 0.3f, cx - r * 0.8f, cy - r * 1.6f, cx, cy - r * 0.45f)
    cubicTo(cx + r * 0.8f, cy - r * 1.6f, cx + r * 1.8f, cy - r * 0.3f, cx, cy + r * 0.9f)
    close()
}

private fun DrawScope.drawRisingHearts(style: GenreRevealStyle, t: Float, particles: List<Particle>) {
    val w = size.width
    val h = size.height
    // Yumuşak bokeh ışıkları
    particles.take(18).forEach { p ->
        val y = h * (1.1f - ((p.delay + t * 0.25f * p.speed) % 1.2f))
        val r = (30f + p.size * 50f).dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(style.secondaryColor.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(w * p.x, y),
                radius = r
            ),
            radius = r,
            center = Offset(w * p.x, y)
        )
    }
    // Yükselen kalpler
    particles.drop(18).take(28).forEach { p ->
        val start = p.delay * 0.45f
        val prog = segment(t, start, start + 0.55f + p.speed * 0.3f)
        if (prog <= 0f) return@forEach
        val x = w * p.x + sin(prog * 6f + p.angle) * 18.dp.toPx()
        val y = h * (1.05f - prog * 1.1f)
        val r = (6f + p.size * 12f).dp.toPx()
        val color = if (p.colorSeed % 2 == 0) style.primaryColor else style.secondaryColor
        rotate(degrees = p.spin * 25f, pivot = Offset(x, y)) {
            drawPath(heartPath(x, y, r), color = color.copy(alpha = 0.85f * (1f - prog * 0.6f)))
        }
    }
}

private val ConfettiColors = listOf(
    Color(0xFFFBBF24), Color(0xFF34D399), Color(0xFFF472B6),
    Color(0xFF38BDF8), Color(0xFFA78BFA), Color(0xFFF87171)
)

private fun DrawScope.drawConfettiBurst(t: Float, particles: List<Particle>) {
    val w = size.width
    val h = size.height
    val gravity = h * 1.5f
    particles.forEachIndexed { i, p ->
        val fromLeft = i % 2 == 0
        val launch = 0.08f + (i % 3) * 0.04f
        val tau = (t - launch).coerceAtLeast(0f) * 2.6f
        if (tau <= 0f) return@forEachIndexed
        val origin = Offset(if (fromLeft) -10f else w + 10f, h * 0.92f)
        // Topun namlusu yukarı-içe doğru (≈ 55°–80°)
        val spread = (55f + p.delay * 28f) * (PI.toFloat() / 180f)
        val v = h * (0.95f + p.speed * 0.75f)
        val vx = cos(spread) * v * (if (fromLeft) 1f else -1f) * (0.45f + p.size * 0.5f)
        val vy = -sin(spread) * v
        val drag = 1f / (1f + tau * 0.9f)
        val x = origin.x + vx * tau * drag
        val y = origin.y + vy * tau * drag + 0.5f * gravity * tau * tau * 0.35f
        if (y > h + 40f) return@forEachIndexed
        val color = ConfettiColors[p.colorSeed % ConfettiColors.size]
        val sz = (5f + p.size * 6f).dp.toPx()
        rotate(degrees = p.spin * 720f * tau, pivot = Offset(x, y)) {
            if (p.colorSeed % 3 == 0) {
                drawCircle(color = color, radius = sz * 0.5f, center = Offset(x, y))
            } else {
                // Dönen kurdele: genişlik sinüsle daralıp genişler (3B takla hissi)
                val flip = kotlin.math.abs(cos(tau * 9f + p.angle))
                drawRect(
                    color = color,
                    topLeft = Offset(x - sz / 2f * flip, y - sz * 0.3f),
                    size = Size(sz * flip.coerceAtLeast(0.15f), sz * 0.6f)
                )
            }
        }
    }
}

private fun DrawScope.drawExplosion(style: GenreRevealStyle, t: Float, particles: List<Particle>) {
    val c = Offset(size.width / 2f, size.height * 0.42f)
    val maxR = maxOf(size.width, size.height) * 0.75f
    // Hız çizgileri
    for (i in 0 until 14) {
        val rnd = Random(i * 41 + 3)
        val y = size.height * rnd.nextFloat()
        val x = size.width * (((t * (1.4f + rnd.nextFloat())) + rnd.nextFloat()) % 1.3f - 0.15f)
        drawLine(
            color = style.secondaryColor.copy(alpha = 0.18f),
            start = Offset(x, y),
            end = Offset(x + 90.dp.toPx(), y - 28.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    // Şok dalgaları
    for (ring in 0 until 3) {
        val p = segment(t, 0.05f + ring * 0.07f, 0.45f + ring * 0.07f)
        if (p <= 0f || p >= 1f) continue
        val e = CinemaMotion.EmphasizedDecelerate.transform(p)
        drawCircle(
            color = (if (ring == 0) Color.White else style.secondaryColor).copy(alpha = (1f - p) * 0.75f),
            radius = 20.dp.toPx() + e * maxR,
            center = c,
            style = Stroke(width = (14f * (1f - p) + 1f).dp.toPx())
        )
    }
    // Ateş topu
    val fire = segment(t, 0.04f, 0.14f) * (1f - segment(t, 0.2f, 0.6f))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF7D6).copy(alpha = fire), style.secondaryColor.copy(alpha = fire * 0.8f), style.primaryColor.copy(alpha = fire * 0.5f), Color.Transparent),
            center = c,
            radius = maxR * 0.45f
        ),
        radius = maxR * 0.45f,
        center = c
    )
    // Kıvılcımlar
    val gravity = size.height * 0.9f
    particles.take(80).forEach { p ->
        val tau = (t - 0.06f).coerceAtLeast(0f) * 1.8f
        if (tau <= 0f) return@forEach
        val v = maxR * (0.6f + p.speed * 1.1f)
        val drag = 1f / (1f + tau * 2.2f)
        val dx = cos(p.angle) * v * tau * drag
        val dy = sin(p.angle) * v * tau * drag + 0.5f * gravity * tau * tau * 0.4f
        val pos = Offset(c.x + dx, c.y + dy)
        val tail = Offset(c.x + dx * 0.86f, c.y + dy * 0.86f)
        val life = (1f - tau / 1.6f).coerceIn(0f, 1f)
        if (life <= 0f) return@forEach
        drawLine(
            color = (if (p.colorSeed % 2 == 0) style.secondaryColor else Color(0xFFFFEDD5)).copy(alpha = life),
            start = tail,
            end = pos,
            strokeWidth = (1.5f + p.size * 2f).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawRadar(style: GenreRevealStyle, t: Float) {
    val c = Offset(size.width / 2f, size.height * 0.42f)
    val maxR = size.width * 0.62f
    val appear = segment(t, 0.02f, 0.2f)
    for (ring in 1..4) {
        drawCircle(
            color = style.secondaryColor.copy(alpha = 0.18f * appear),
            radius = maxR * ring / 4f,
            center = c,
            style = Stroke(width = 1.dp.toPx())
        )
    }
    drawLine(style.secondaryColor.copy(alpha = 0.15f * appear), Offset(c.x - maxR, c.y), Offset(c.x + maxR, c.y), 1.dp.toPx())
    drawLine(style.secondaryColor.copy(alpha = 0.15f * appear), Offset(c.x, c.y - maxR), Offset(c.x, c.y + maxR), 1.dp.toPx())
    // Dönen tarama ışını
    rotate(degrees = t * 720f, pivot = c) {
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Transparent, Color.Transparent, style.primaryColor.copy(alpha = 0.28f * appear)),
                center = c
            ),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true,
            topLeft = Offset(c.x - maxR, c.y - maxR),
            size = Size(maxR * 2f, maxR * 2f)
        )
    }
    // Tarama çizgileri
    val step = 4.dp.toPx()
    var y = 0f
    while (y < size.height) {
        drawLine(Color.Black.copy(alpha = 0.18f), Offset(0f, y), Offset(size.width, y), 1f)
        y += step
    }
    // İlk anlardaki glitch bantları
    if (t < 0.35f) {
        val frame = (t * 40f).toInt()
        val rnd = Random(frame * 17 + 5)
        repeat(4) {
            if (rnd.nextFloat() > 0.45f) {
                val by = size.height * rnd.nextFloat()
                drawRect(
                    color = (if (rnd.nextBoolean()) style.primaryColor else style.secondaryColor).copy(alpha = 0.22f),
                    topLeft = Offset(0f, by),
                    size = Size(size.width, (2f + rnd.nextFloat() * 14f).dp.toPx())
                )
            }
        }
    }
}

private fun DrawScope.drawSparkleSwirl(style: GenreRevealStyle, t: Float, particles: List<Particle>) {
    val c = Offset(size.width / 2f, size.height * 0.42f)
    val maxR = size.width * 0.75f
    val pastel = listOf(style.primaryColor, style.secondaryColor, Color(0xFFFDE68A), Color(0xFF93C5FD))
    // Yükselen baloncuklar
    particles.drop(60).take(20).forEach { p ->
        val y = size.height * (1.1f - ((p.delay + t * 0.45f * p.speed) % 1.2f))
        val x = size.width * p.x + sin(t * 8f + p.angle) * 10.dp.toPx()
        drawCircle(
            color = pastel[p.colorSeed % pastel.size].copy(alpha = 0.35f),
            radius = (6f + p.size * 12f).dp.toPx(),
            center = Offset(x, y),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
    // Spiral yıldız tozu
    particles.take(60).forEach { p ->
        val prog = segment(t, p.delay * 0.3f, p.delay * 0.3f + 0.7f)
        if (prog <= 0f) return@forEach
        val a = p.angle + prog * 4f
        val r = 30.dp.toPx() + prog * maxR * (0.4f + p.speed * 0.6f)
        val pos = Offset(c.x + cos(a) * r, c.y + sin(a) * r)
        val twinkle = 0.5f + 0.5f * sin(t * 30f + p.colorSeed)
        val arm = (3f + p.size * 6f).dp.toPx() * (0.6f + 0.4f * twinkle)
        val color = pastel[p.colorSeed % pastel.size].copy(alpha = (1f - prog * 0.7f))
        drawLine(color, Offset(pos.x - arm, pos.y), Offset(pos.x + arm, pos.y), 1.6.dp.toPx(), StrokeCap.Round)
        drawLine(color, Offset(pos.x, pos.y - arm), Offset(pos.x, pos.y + arm), 1.6.dp.toPx(), StrokeCap.Round)
    }
}

private fun DrawScope.drawSpotlight(style: GenreRevealStyle, t: Float) {
    val w = size.width
    val h = size.height
    val on = segment(t, 0.1f, 0.35f)
    val beam = Path().apply {
        moveTo(w * 0.44f, 0f)
        lineTo(w * 0.56f, 0f)
        lineTo(w * 0.95f, h * 0.78f)
        lineTo(w * 0.05f, h * 0.78f)
        close()
    }
    drawPath(
        path = beam,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF4D6).copy(alpha = 0.32f * on), style.primaryColor.copy(alpha = 0.08f * on), Color.Transparent),
            startY = 0f,
            endY = h * 0.8f
        )
    )
    // Sahne zemini ışık havuzu
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF4D6).copy(alpha = 0.22f * on), Color.Transparent),
            center = Offset(w / 2f, h * 0.78f),
            radius = w * 0.5f
        ),
        topLeft = Offset(w * 0.05f, h * 0.72f),
        size = Size(w * 0.9f, h * 0.12f)
    )
}

// ---------------------------------------------------------------------------------------------
// Ön plan efektleri (afişin önünde)
// ---------------------------------------------------------------------------------------------

private fun DrawScope.drawForegroundFx(style: GenreRevealStyle, t: Float) {
    when (style) {
        GenreRevealStyle.HORROR -> {
            // Işık titremesi: ilk anlarda sahne düzensiz aralıklarla kararır
            if (t < 0.3f) {
                val frame = (t * 50f).toInt()
                val dark = Random(frame * 13 + 1).nextFloat() > 0.55f
                drawRect(Color.Black.copy(alpha = if (dark) 0.88f else 0.25f * (1f - t / 0.3f)))
            }
        }

        GenreRevealStyle.ACTION -> {
            val flash = segment(t, 0.03f, 0.07f) * (1f - segment(t, 0.07f, 0.2f))
            if (flash > 0f) drawRect(Color.White.copy(alpha = flash * 0.9f))
        }

        GenreRevealStyle.DRAMA -> drawCurtains(t)

        else -> Unit
    }
}

/** Kadife sahne perdesi: ortadan iki yana açılır, üstte saçaklı bir pelmeni vardır. */
private fun DrawScope.drawCurtains(t: Float) {
    val w = size.width
    val h = size.height
    // Perde kısa bir an kapalı kalır, sonra ağır ve görkemli biçimde açılır
    val open = CinemaMotion.Emphasized.transform(segment(t, 0.1f, 0.55f))
    val halfW = w / 2f
    val visible = halfW * (1f - open * 0.86f)
    val folds = buildList {
        repeat(7) {
            add(Color(0xFF4A0712))
            add(Color(0xFF9B1B2E))
        }
        add(Color(0xFF4A0712))
    }
    // Sol perde
    drawRect(
        brush = Brush.horizontalGradient(folds, startX = visible - halfW, endX = visible),
        topLeft = Offset(0f, 0f),
        size = Size(visible, h)
    )
    // Sağ perde
    drawRect(
        brush = Brush.horizontalGradient(folds, startX = w - visible, endX = w - visible + halfW),
        topLeft = Offset(w - visible, 0f),
        size = Size(visible, h)
    )
    // Perde kenarlarındaki gölge
    val edgeShadow = 18.dp.toPx()
    drawRect(
        brush = Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent), startX = visible, endX = visible + edgeShadow),
        topLeft = Offset(visible, 0f),
        size = Size(edgeShadow, h)
    )
    drawRect(
        brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)), startX = w - visible - edgeShadow, endX = w - visible),
        topLeft = Offset(w - visible - edgeShadow, 0f),
        size = Size(edgeShadow, h)
    )
    // Pelmen (üst saçak)
    val valanceH = 54.dp.toPx()
    drawRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF5E0A17), Color(0xFF8E1828))),
        size = Size(w, valanceH)
    )
    val scallops = 8
    val sw = w / scallops
    for (i in 0 until scallops) {
        drawArc(
            color = Color(0xFF8E1828),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(i * sw, valanceH - sw * 0.35f),
            size = Size(sw, sw * 0.7f)
        )
    }
    drawLine(Color(0xFFE9B949), Offset(0f, valanceH - 2.dp.toPx()), Offset(w, valanceH - 2.dp.toPx()), 2.dp.toPx())
}


/** Sonuç ekranı düğmeleri: VETO (tek hak), İZLENECEK, Paylaş. */
@Composable
private fun RevealActionRow(
    actions: RevealActions,
    enabled: Boolean,
    onVeto: () -> Unit,
    onWatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pill = RoundedCornerShape(999.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (actions.canVeto) {
                Text(
                    "✕  VETO",
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF4D5E),
                    modifier = Modifier
                        .border(2.dp, Color(0xFFFF4D5E), pill)
                        .background(Color(0x33E8283C), pill)
                        .clickable(enabled = enabled, onClickLabel = "Bu filmi veto et") { onVeto() }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .testTag("reveal_veto_button")
                )
            }
            Text(
                "✓  İZLENECEK",
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1408),
                modifier = Modifier
                    .shadow(10.dp, pill)
                    .background(Brush.verticalGradient(listOf(Color(0xFFF3CF74), Color(0xFFD9A23A))), pill)
                    .clickable(enabled = enabled, onClickLabel = "İzleneceklere ekle") { onWatch() }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("reveal_watch_button")
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "↗  Sosyal Medyada Paylaş",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .border(1.dp, Color.White.copy(alpha = 0.35f), pill)
                .background(Color.Black.copy(alpha = 0.25f), pill)
                .clickable(enabled = enabled, onClickLabel = "Sonucu paylaş") { actions.onShare() }
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .testTag("reveal_share_button")
        )
    }
}

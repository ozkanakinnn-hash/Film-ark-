package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.ui.theme.CinemaMotion
import com.example.ui.theme.pressScale
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

private const val INTRO_MS = 1400
private const val AMBIENT_MS = 9000
private const val STAMP_MS = 1500

private fun seg(t: Float, start: Float, end: Float) = ((t - start) / (end - start)).coerceIn(0f, 1f)

/**
 * Çark sonucu kartı (tüm çarklar). Yerleşim her türde aynı, atmosfer türe göre değişir:
 * 1) tür rozeti  2) büyük afiş  3) film adı + tür  4) yıl • puan • süre  5) Veto | İzlenecek  6) Paylaş.
 * Kullanıcı seçim yapana kadar açık kalır. Tüm animasyonlar sonludur (giriş 1,4 sn, ortam süzülmesi 9 sn, mühür 1,5 sn).
 */
@Composable
internal fun ResultCardScene(
    movie: Movie,
    actions: RevealActions,
    stampText: String?,
    modifier: Modifier = Modifier
) {
    val reduceMotion = com.example.data.local.LocalReduceMotion.current
    val g = remember(movie.id) { cardGenreFor(movie) }
    val seeds = remember(movie.id) { FxSeeds(movie.id.hashCode()) }
    val intro = remember(movie.id) { Animatable(if (reduceMotion) 1f else 0f) }
    val ambient = remember(movie.id) { Animatable(0f) }
    val stampT = remember(movie.id) { Animatable(0f) }
    var stamp by remember(movie.id) { mutableStateOf<RevealStamp?>(null) }
    val scope = rememberCoroutineScope()
    val latest by rememberUpdatedState(actions)

    LaunchedEffect(movie.id) {
        if (reduceMotion) return@LaunchedEffect
        launch { intro.animateTo(1f, tween(INTRO_MS, easing = LinearEasing)) }
        ambient.animateTo(1f, tween(AMBIENT_MS, easing = LinearEasing))
    }

    fun playStamp(kind: RevealStamp) {
        if (stamp != null) return
        stamp = kind
        scope.launch {
            if (intro.value < 1f) intro.snapTo(1f)
            if (reduceMotion) {
                stampT.snapTo(0.6f)
                stampT.animateTo(1f, tween(700, easing = LinearEasing))
            } else {
                stampT.animateTo(1f, tween(STAMP_MS, easing = LinearEasing))
            }
            if (kind == RevealStamp.VETO) latest.onVeto() else latest.onWatchLater()
        }
    }

    val i = intro.value
    val enabled = stamp == null && i > 0.6f
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(movie.id) { detectTapGestures { if (intro.value < 1f) scope.launch { intro.snapTo(1f) } } }
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "Seçilen film: ${movie.title}, ${g.label}"
            }
            .testTag("genre_reveal_overlay")
    ) {
        // 1) Türe özel atmosfer
        Canvas(Modifier.fillMaxSize()) { drawCardAtmosphere(g, ambient.value, seeds, seg(i, 0f, 0.5f)) }
        // Filmin kendi afişi: çok soluk, bulanık doku (atmosferle kaynaşır)
        MoviePosterImage(
            title = movie.title,
            posterPath = movie.posterPath,
            posterUrl = movie.posterUrl,
            accentColorHex = movie.colorHex,
            cornerRadius = 0.dp,
            showFallbackTitleBadge = false,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.16f * seg(i, 0f, 0.4f)
                    val z = 1.3f - 0.1f * ambient.value
                    scaleX = z; scaleY = z
                }
                .blur(36.dp)
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.55f)))))

        BoxWithConstraints(Modifier.fillMaxSize()) {
            // Afiş ekranın odak noktası: yüksekliğin ~%50'si, 2:3 oran korunur, genişlikte en fazla %74
            val maxPosterW = maxWidth * 0.74f
            val posterW = min((maxHeight * 0.5f) * (2f / 3f), maxPosterW).coerceAtLeast(150.dp)
            val posterH = posterW * 1.5f
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 60.dp, bottom = 28.dp)
            ) {
                // Tür rozeti
                val badgeIn = seg(i, 0f, 0.3f)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = badgeIn
                            translationY = (1f - CinemaMotion.EmphasizedDecelerate.transform(badgeIn)) * -14.dp.toPx()
                        }
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, g.accent.copy(alpha = 0.85f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("reveal_genre_badge")
                ) {
                    Icon(g.icon, contentDescription = null, tint = g.accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(g.label, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }

                Spacer(Modifier.height(18.dp))

                // Büyük afiş: ince tür ışığı, yumuşak açılış, ışık süzmesi, mühür
                val posterIn = seg(i, 0.08f, 0.6f)
                val sv = stampT.value
                val land = seg(sv * STAMP_MS, 260f, 560f)
                val shake = if (land in 0.01f..0.99f) sin(land * 30f) * (1f - land) else 0f
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = seg(posterIn, 0f, 0.4f)
                        val s = 0.9f + 0.1f * CinemaMotion.EmphasizedDecelerate.transform(posterIn)
                        scaleX = s; scaleY = s
                        translationX = shake * 10.dp.toPx()
                        translationY = shake * 5.dp.toPx()
                    }
                ) {
                    MoviePosterImage(
                        title = movie.title,
                        posterPath = movie.posterPath,
                        posterUrl = movie.posterUrl,
                        accentColorHex = movie.colorHex,
                        genre = movie.genre,
                        year = movie.year,
                        cornerRadius = 20.dp,
                        showFallbackTitleBadge = false,
                        testTag = "genre_reveal_overlay_poster",
                        modifier = Modifier
                            .size(width = posterW, height = posterH)
                            .shadow(30.dp, RoundedCornerShape(20.dp), ambientColor = g.glow, spotColor = g.accent)
                            .border(1.5.dp, g.accent.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                    )
                    val shine = seg(i, 0.55f, 0.95f)
                    if (shine in 0.001f..0.999f) {
                        Canvas(Modifier.size(width = posterW, height = posterH).clip(RoundedCornerShape(20.dp))) {
                            val x = -size.width * 0.6f + size.width * 2.2f * shine
                            drawRect(
                                Brush.linearGradient(
                                    listOf(Color.Transparent, Color.White.copy(alpha = 0.28f), Color.Transparent),
                                    start = Offset(x, 0f), end = Offset(x + size.width * 0.5f, size.height)
                                )
                            )
                        }
                    }
                    stamp?.let { kind ->
                        val drop = FastOutSlowInEasing.transform(seg(sv * STAMP_MS, 260f, 520f))
                        Box(Modifier.size(width = posterW, height = posterH).background(Color.Black.copy(alpha = 0.5f * land), RoundedCornerShape(20.dp)))
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
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag(if (kind == RevealStamp.VETO) "reveal_veto_stamp" else "reveal_watch_stamp")
                        ) {
                            Text(kind.title, fontSize = if (kind == RevealStamp.VETO) 44.sp else 32.sp, letterSpacing = 5.sp, fontWeight = FontWeight.Black, color = kind.color)
                            Text(kind.sub, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Black, color = kind.color)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Film adı + tür + yıl / puan / süre
                val infoIn = seg(i, 0.35f, 0.75f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = infoIn
                        translationY = (1f - CinemaMotion.EmphasizedDecelerate.transform(infoIn)) * 18.dp.toPx()
                    }
                ) {
                    Text(
                        movie.title,
                        color = Color.White,
                        fontSize = 26.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.3).sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("reveal_card_title")
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        movie.genre.replace(" / ", "  •  "),
                        color = g.accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.testTag("reveal_card_meta")) {
                        Icon(Icons.Filled.CalendarMonth, null, tint = g.accent, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("${movie.year}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        MetaDivider()
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFACC15), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(String.format(Locale.US, "%.1f", movie.imdb), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        MetaDivider()
                        Icon(Icons.Filled.Schedule, null, tint = g.accent, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(movie.durationLabel(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (stampText != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stampText,
                            color = Color(0xFFFF4D5E),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .graphicsLayer { rotationZ = -5f }
                                .border(2.dp, Color(0xFFFF4D5E), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .testTag("reveal_stamp_text")
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                // Veto | İzlenecek (eşit genişlik) + Paylaş
                val btnIn = if (reduceMotion) 1f else seg(i, 0.55f, 0.95f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = btnIn
                        translationY = (1f - CinemaMotion.EmphasizedDecelerate.transform(btnIn)) * 22.dp.toPx()
                    }
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        if (actions.canVeto) {
                            CardButton(
                                text = "Veto",
                                icon = Icons.Filled.Block,
                                filled = false,
                                accent = Color(0xFFFF4A3D),
                                darkText = false,
                                enabled = enabled,
                                clickLabel = "Bu filmi veto et",
                                tag = "reveal_veto_button",
                                modifier = Modifier.weight(1f)
                            ) { playStamp(RevealStamp.VETO) }
                        }
                        CardButton(
                            text = "İzlenecek",
                            icon = Icons.Filled.PlayArrow,
                            filled = true,
                            accent = g.accent,
                            darkText = g.darkText,
                            enabled = enabled,
                            clickLabel = "İzleneceklere ekle",
                            tag = "reveal_watch_button",
                            modifier = Modifier.weight(1f)
                        ) { playStamp(RevealStamp.WATCH) }
                    }
                    Spacer(Modifier.height(14.dp))
                    val shareSource = remember { MutableInteractionSource() }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.62f)
                            .pressScale(shareSource)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.verticalGradient(listOf(g.accent.copy(alpha = 0.22f), g.accent.copy(alpha = 0.1f))))
                            .border(1.2.dp, g.accent.copy(alpha = 0.75f), RoundedCornerShape(999.dp))
                            .clickable(interactionSource = shareSource, indication = null, enabled = stamp == null, onClickLabel = "Sosyal medyada paylaş") { actions.onShare() }
                            .padding(vertical = 12.dp)
                            .testTag("reveal_share_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = g.accent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Paylaş", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Kapat
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClickLabel = "Sonucu kapat") { if (stamp == null) actions.onClose() }
                .testTag("reveal_close_button")
        ) {
            Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun MetaDivider() {
    Box(Modifier.padding(horizontal = 11.dp).size(width = 1.dp, height = 14.dp).background(Color.White.copy(alpha = 0.3f)))
}

@Composable
private fun CardButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    filled: Boolean,
    accent: Color,
    darkText: Boolean,
    enabled: Boolean,
    clickLabel: String,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(999.dp)
    val fg = if (filled) (if (darkText) Color(0xFF14100A) else Color.White) else Color.White
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(52.dp)
            .pressScale(source, pressedScale = 0.94f)
            .then(
                if (filled) Modifier.shadow(18.dp, shape, ambientColor = accent, spotColor = accent)
                else Modifier
            )
            .clip(shape)
            .background(
                if (filled) Brush.verticalGradient(listOf(lighten(accent, 0.3f), accent))
                else Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.4f)))
            )
            .border(if (filled) 0.dp else 1.5.dp, if (filled) Color.Transparent else accent.copy(alpha = 0.85f), shape)
            .clickable(interactionSource = source, indication = null, enabled = enabled, onClickLabel = clickLabel, onClick = onClick)
            .testTag(tag)
    ) {
        Icon(icon, contentDescription = null, tint = if (filled) fg else accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = fg, fontSize = 16.sp, fontWeight = if (filled) FontWeight.Black else FontWeight.Bold)
    }
}

private fun lighten(c: Color, f: Float) = Color(
    red = c.red + (1f - c.red) * f,
    green = c.green + (1f - c.green) * f,
    blue = c.blue + (1f - c.blue) * f,
    alpha = 1f
)

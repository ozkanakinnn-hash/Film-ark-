package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.ui.theme.CinemaBackground
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VetoRed
import com.example.ui.theme.WheelThemeManager
import com.example.ui.theme.WheelVisualTheme
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animasyonlu Karar Çarkı ve Veto Mekanizması (Animated Decision Wheel & Veto Component)
 *
 * Özellikler:
 * - Çarkı Çevir butonuna basıldığında rastgele film seçimi
 * - Çok turlu hızlanma ve yavaşlama (CubicBezierEasing) fiziği
 * - Çark döndükçe ibre titremesi (Needle wobble) ve haptik tık
 * - Seçilen filmden sonra 'Veto Et (1/1)' butonu
 * - Veto butonuna tıklandığında filmi eleyerek çarkı anında YENİDEN ÇEVİREN state mantığı
 * - Veto sonrası kararın kilitlenmesi (Locked Fate: İtiraz yasaktır!)
 */
@Composable
fun AnimatedDecisionWheel(
    movies: List<Movie> = MovieRepository.allMovies.take(8),
    onMovieSelected: (Movie) -> Unit = {},
    onVetoApplied: ((Movie) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    // Active movies list in the wheel (dynamically shrinks when veto is used)
    var activeMoviesList by remember(movies) {
        mutableStateOf(if (movies.isNotEmpty()) movies else MovieRepository.allMovies.take(8))
    }

    // Veto & Lock State
    var hasVetoed by remember { mutableStateOf(false) }
    var isFateLocked by remember { mutableStateOf(false) }

    // Rotation & needle state
    val wheelRotation = remember { Animatable(0f) }
    val needleAngle = remember { Animatable(0f) }

    var isSpinning by remember { mutableStateOf(false) }
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }

    val sliceCount = activeMoviesList.size.coerceAtLeast(1)
    val sweepAngle = 360f / sliceCount

    // Marquee lights infinite blinking animation
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    val marqueePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    var needleJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val wheelTheme: WheelVisualTheme = WheelThemeManager.current
    val textPaint = remember {
        Paint().apply {
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
    }

    // Spin animation handler
    fun triggerSpin(isFinalAfterVeto: Boolean = false) {
        if (isSpinning || activeMoviesList.isEmpty()) return

        coroutineScope.launch {
            isSpinning = true
            selectedMovie = null

            try {
                // 1. Pick a truly random winning movie from current active pool
                val currentSliceCount = activeMoviesList.size
                val currentSweep = 360f / currentSliceCount
                val winnerIndex = Random.nextInt(currentSliceCount)
                val winner = activeMoviesList[winnerIndex]

                // 2. Calculate exact target stopping angle
                val sliceCenterAngle = (winnerIndex * currentSweep) + (currentSweep / 2f)
                val fullSpins = Random.nextInt(5, 8) * 360f
                val currentMod = ((wheelRotation.value % 360f) + 360f) % 360f
                wheelRotation.snapTo(currentMod)

                val targetAngle = fullSpins + (360f - sliceCenterAngle)
                var lastTickDeg = currentMod

                // 3. Animate wheel with dramatic deceleration curve
                wheelRotation.animateTo(
                    targetValue = targetAngle,
                    animationSpec = tween(
                        durationMillis = 4400,
                        easing = CubicBezierEasing(0.12f, 0.8f, 0.2f, 1.0f)
                    )
                ) {
                    // Haptic tick & needle wobble on each slice boundary
                    val currentDeg = this.value
                    if (kotlin.math.abs(currentDeg - lastTickDeg) >= currentSweep) {
                        runCatching { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }
                        lastTickDeg = currentDeg

                        needleJob?.cancel()
                        needleJob = coroutineScope.launch {
                            needleAngle.snapTo(-18f)
                            needleAngle.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = 800f))
                        }
                    }
                }

                // 4. Reveal Winner & Lock fate if veto was used
                selectedMovie = winner
                if (hasVetoed || isFinalAfterVeto) {
                    isFateLocked = true
                }
                onMovieSelected(winner)
                runCatching { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM) }
            } finally {
                isSpinning = false
            }
        }
    }

    // VETO ACTION: Elides the chosen movie and automatically re-spins the wheel
    fun triggerVeto() {
        val rejected = selectedMovie ?: return
        if (!hasVetoed && !isFateLocked && !isSpinning) {
            hasVetoed = true
            val remaining = activeMoviesList.filter { it.id != rejected.id }
            activeMoviesList = remaining.ifEmpty {
                MovieRepository.allMovies.filter { it.id != rejected.id }.take(6)
            }
            selectedMovie = null
            onVetoApplied?.invoke(rejected)
            runCatching { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }

            // Trigger immediate final re-spin that will lock the fate!
            triggerSpin(isFinalAfterVeto = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // VETO & FATE STATUS BANNER
        if (isFateLocked) {
            Surface(
                color = Color(0x3310B981),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KADER KİLİTLENDİ! İtiraz yasaktır, mısırları patlatın! 🍿",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
            }
        } else if (hasVetoed) {
            Surface(
                color = Color(0x33EF4444),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VetoRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "1 Veto Kullanıldı • Bu son karardır! 🔒",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCA5A5)
                    )
                }
            }
        } else {
            Surface(
                color = Color(0x33000000),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎯 Günlük 1 Veto Hakkı Mevcuttur (1/1)",
                    fontSize = 12.sp,
                    color = PopcornGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // THE ROTATING CANVAS WHEEL
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            val rimGold = wheelTheme.outerRimColor
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size
                val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                val outerRadius = min(canvasSize.width, canvasSize.height) / 2f - 18.dp.toPx()
                if (outerRadius <= 16f || center.x <= 0f || center.y <= 0f) return@Canvas

                // Rotate entire wheel by animated angle
                rotate(degrees = wheelRotation.value, pivot = center) {
                    // 1. Draw Slice Arcs & Dividers
                    for (i in 0 until sliceCount) {
                        val startAngle = i * sweepAngle - 90f
                        val color = wheelTheme.sliceColorAt(i)

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                            size = Size(outerRadius * 2f, outerRadius * 2f),
                            style = Fill
                        )

                        // Divider line
                        val rad = Math.toRadians(startAngle.toDouble())
                        val endX = center.x + (outerRadius * cos(rad)).toFloat()
                        val endY = center.y + (outerRadius * sin(rad)).toFloat()
                        drawLine(
                            color = wheelTheme.dividerColor,
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // 2. Draw Text on Slices
                    val computedTextSize = (outerRadius * 0.088f).coerceIn(18f, 38f)
                    textPaint.textSize = computedTextSize
                    val textMargin = 16.dp.toPx().coerceAtMost(outerRadius * 0.25f)

                    for (i in 0 until sliceCount) {
                        val midAngle = (i * sweepAngle + sweepAngle / 2f) - 90f
                        val title = if (i < activeMoviesList.size) activeMoviesList[i].title else "Film #${i + 1}"
                        val label = if (title.length > 15) title.take(13) + ".." else title
                        val sliceColor = wheelTheme.sliceColorAt(i)

                        textPaint.color = wheelTheme.sliceTextColorFor(sliceColor).toArgb()
                        textPaint.setShadowLayer(
                            if (wheelTheme.isDark) 4f else 2f,
                            1.5f,
                            1.5f,
                            wheelTheme.sliceTextShadowArgbFor(sliceColor)
                        )

                        rotate(degrees = midAngle, pivot = center) {
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                center.x + outerRadius - textMargin,
                                center.y + (computedTextSize / 3.2f),
                                textPaint
                            )
                        }
                    }

                    // 3. Inner radial shadow / vignette
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, wheelTheme.innerVignetteColor),
                            center = center,
                            radius = outerRadius
                        ),
                        radius = outerRadius,
                        center = center
                    )

                    // 4. Golden Rim & Marquee Lights
                    drawCircle(
                        color = wheelTheme.outerRimAccentColor,
                        radius = outerRadius - 3.dp.toPx(),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = rimGold,
                        radius = outerRadius,
                        center = center,
                        style = Stroke(width = 8.dp.toPx())
                    )

                    val lightCount = sliceCount * 3
                    for (j in 0 until lightCount) {
                        val dotAngle = j * (360f / lightCount)
                        val dotRad = Math.toRadians(dotAngle.toDouble())
                        val dotDist = outerRadius + 4.dp.toPx()
                        val dotX = center.x + (dotDist * cos(dotRad)).toFloat()
                        val dotY = center.y + (dotDist * sin(dotRad)).toFloat()

                        val isLit = (j % 2 == 0) xor (marqueePhase > 0.5f)
                        drawCircle(
                            color = if (isLit) wheelTheme.marqueeLitBulbColor else wheelTheme.marqueeDimBulbColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                    }

                    // 5. Center Hub Cap
                    val hubRadius = 34.dp.toPx().coerceAtMost(outerRadius * 0.35f).coerceAtLeast(1f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = wheelTheme.hubGradientColors,
                            center = center,
                            radius = hubRadius
                        ),
                        radius = hubRadius,
                        center = center
                    )
                    drawCircle(
                        color = wheelTheme.hubBorderColor,
                        radius = hubRadius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // NEEDLE / POINTER AT TOP (With deflection/wobble)
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 38.dp, height = 46.dp)
            ) {
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@Canvas

                rotate(degrees = needleAngle.value, pivot = Offset(w / 2f, 0f)) {
                    val path = Path().apply {
                        moveTo(w / 2f, h)
                        lineTo(0f, 0f)
                        lineTo(w, 0f)
                        close()
                    }

                    // Shadow
                    val shadowPath = Path().apply {
                        moveTo(w / 2f, h + 3.dp.toPx())
                        lineTo(-2.dp.toPx(), 2.dp.toPx())
                        lineTo(w + 2.dp.toPx(), 2.dp.toPx())
                        close()
                    }
                    drawPath(shadowPath, color = wheelTheme.needleShadowColor)

                    // Needle gradient
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(wheelTheme.needleGradientColors)
                    )
                    drawPath(path = path, color = wheelTheme.needleBorderColor, style = Stroke(width = 2.dp.toPx()))

                    // Pin dot
                    drawCircle(
                        color = wheelTheme.needlePinColor,
                        radius = 4.dp.toPx(),
                        center = Offset(w / 2f, 10.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // "ÇARKI ÇEVİR" BUTTON (Visible when no movie is selected or during spin)
        if (selectedMovie == null || isSpinning) {
            Button(
                onClick = { triggerSpin() },
                enabled = !isSpinning && activeMoviesList.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("animated_spin_wheel_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSpinning) {
                    Text(
                        text = "Kader Karar Veriyor... 🎡",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Çarkı Çevir! 🍿",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // ANIMATED WINNER REVEAL & VETO ACTION SECTION
        AnimatedVisibility(
            visible = selectedMovie != null && !isSpinning,
            enter = fadeIn(tween(400)) + scaleIn(spring(dampingRatio = 0.7f, stiffness = 400f))
        ) {
            selectedMovie?.let { movie ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                ) {
                    // Movie Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                Brush.verticalGradient(listOf(NeonPurple, PopcornGold)),
                                RoundedCornerShape(20.dp)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🎉", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "KAZANAN FİLM",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        color = PopcornGold
                                    )
                                }

                                Surface(
                                    color = Color(0x33F59E0B),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = PopcornGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${movie.imdb}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PopcornGold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = movie.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${movie.year} • ${movie.durationMins} dakika • ${movie.genre}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = movie.logline,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Platforms badges
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                movie.platforms.forEach { platform ->
                                    Surface(
                                        color = CinemaSurfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaBorder)
                                    ) {
                                        Text(
                                            text = platform,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ElectricCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // VETO BUTTON (Re-spins wheel upon click and eliminates current movie)
                    if (!hasVetoed && !isFateLocked) {
                        OutlinedButton(
                            onClick = { triggerVeto() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("animated_wheel_veto_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, VetoRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = VetoRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bu Filmi VETO ET! (1/1 Hak)",
                                color = VetoRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    } else if (isFateLocked) {
                        // Fate is locked - no more vetoes allowed
                        Surface(
                            color = Color(0x2210B981),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🔒 Karar Kilitlendi • Sıfır suçlama, mısırları patlatın!",
                                fontSize = 13.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

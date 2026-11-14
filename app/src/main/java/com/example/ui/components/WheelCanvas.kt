package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Movie
import com.example.ui.theme.WheelThemeManager
import com.example.ui.theme.WheelVisualTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun WheelCanvas(
    movies: List<Movie>,
    rotationProvider: () -> Float,
    needleDeflectionProvider: () -> Float = { 0f },
    isSpinning: Boolean = false,
    selectedMovie: Movie? = null,
    wheelTheme: WheelVisualTheme = WheelThemeManager.current,
    onWheelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorAnimSpec = tween<Color>(durationMillis = 320)
    val rimGold by animateColorAsState(wheelTheme.outerRimColor, animationSpec = colorAnimSpec, label = "wheel_rim")
    val rimAccent by animateColorAsState(wheelTheme.outerRimAccentColor, animationSpec = colorAnimSpec, label = "wheel_rim_accent")
    val dividerColor by animateColorAsState(wheelTheme.dividerColor, animationSpec = colorAnimSpec, label = "wheel_divider")
    val litBulbColor by animateColorAsState(wheelTheme.marqueeLitBulbColor, animationSpec = colorAnimSpec, label = "wheel_bulb_lit")
    val dimBulbColor by animateColorAsState(wheelTheme.marqueeDimBulbColor, animationSpec = colorAnimSpec, label = "wheel_bulb_dim")
    val innerVignetteColor by animateColorAsState(wheelTheme.innerVignetteColor, animationSpec = colorAnimSpec, label = "wheel_vignette")
    val hubBorderColor by animateColorAsState(wheelTheme.hubBorderColor, animationSpec = colorAnimSpec, label = "wheel_hub_border")
    val hubCenterAccent by animateColorAsState(wheelTheme.hubCenterAccentColor, animationSpec = colorAnimSpec, label = "wheel_hub_center")
    val needleBorderColor by animateColorAsState(wheelTheme.needleBorderColor, animationSpec = colorAnimSpec, label = "wheel_needle_border")
    val needleShadowColor by animateColorAsState(wheelTheme.needleShadowColor, animationSpec = colorAnimSpec, label = "wheel_needle_shadow")
    val needlePinColor by animateColorAsState(wheelTheme.needlePinColor, animationSpec = colorAnimSpec, label = "wheel_needle_pin")
    val haloPrimary by animateColorAsState(wheelTheme.haloPrimaryColor, animationSpec = colorAnimSpec, label = "wheel_halo_primary")
    val haloSecondary by animateColorAsState(wheelTheme.haloSecondaryColor, animationSpec = colorAnimSpec, label = "wheel_halo_secondary")
    val projectorRayColor by animateColorAsState(wheelTheme.projectorRayColor, animationSpec = colorAnimSpec, label = "wheel_projector_ray")

    val sliceCount = if (movies.isNotEmpty()) movies.size else 8
    val winningIndex = remember(movies, selectedMovie) {
        if (selectedMovie == null) -1 else movies.indexOfFirst { it.id == selectedMovie.id }
    }
    val hasWinnerHighlight = !isSpinning && winningIndex >= 0

    val winnerRevealAnim = remember { Animatable(0f) }
    LaunchedEffect(selectedMovie?.id, isSpinning, winningIndex) {
        if (!isSpinning && winningIndex >= 0) {
            winnerRevealAnim.snapTo(0f)
            winnerRevealAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 240f)
            )
        } else {
            winnerRevealAnim.snapTo(0f)
        }
    }

    val animatedSliceColors = List(sliceCount) { idx ->
        val target = wheelTheme.sliceColorAt(idx)
        val animated by animateColorAsState(target, animationSpec = colorAnimSpec, label = "slice_color_$idx")
        animated
    }
    val animatedHubGradient = wheelTheme.hubGradientColors.mapIndexed { idx, color ->
        val animated by animateColorAsState(color, animationSpec = colorAnimSpec, label = "hub_grad_$idx")
        animated
    }
    val animatedNeedleGradient = wheelTheme.needleGradientColors.mapIndexed { idx, color ->
        val animated by animateColorAsState(color, animationSpec = colorAnimSpec, label = "needle_grad_$idx")
        animated
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wheel_projector_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_phase"
    )
    val beamRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "projector_beam_rotation"
    )
    val textPaint = remember {
        Paint().apply {
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
    }
    val needlePath = remember { Path() }
    val needleShadowPath = remember { Path() }
    val interactionSource = remember { MutableInteractionSource() }

    val clickModifier = if (onWheelClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onWheelClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(12.dp)
            .then(clickModifier)
            .testTag("wheel_canvas_box"),
        contentAlignment = Alignment.Center
    ) {
        // The Spinning Wheel Body
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val outerRadius = min(canvasSize.width, canvasSize.height) / 2f - 16.dp.toPx()
            if (outerRadius <= 16f || center.x <= 0f || center.y <= 0f) return@Canvas

            val sweepAngle = 360f / sliceCount
            val currentRotation = rotationProvider()
            val revealProgress = if (hasWinnerHighlight) winnerRevealAnim.value.coerceIn(0f, 1.15f) else 0f

            // Cinematic projector halo, rotating spotlight rays & smooth motion trails while spinning
            if (isSpinning) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            haloPrimary.copy(alpha = 0.25f + (pulsePhase * 0.2f)),
                            haloSecondary.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = outerRadius + 16.dp.toPx()
                    ),
                    radius = outerRadius + 16.dp.toPx(),
                    center = center
                )
                rotate(degrees = beamRotation, pivot = center) {
                    for (ray in 0 until 4) {
                        drawArc(
                            color = projectorRayColor.copy(alpha = 0.16f),
                            startAngle = ray * 90f,
                            sweepAngle = 24f,
                            useCenter = true,
                            topLeft = Offset(center.x - outerRadius - 12.dp.toPx(), center.y - outerRadius - 12.dp.toPx()),
                            size = Size((outerRadius + 12.dp.toPx()) * 2f, (outerRadius + 12.dp.toPx()) * 2f)
                        )
                    }
                }
                // Sweeping rotational speed trails outside the rim
                val trailRadius = outerRadius + 9.dp.toPx()
                for (trail in 0 until 3) {
                    val trailStart = (currentRotation * 1.15f) + (trail * 120f)
                    drawArc(
                        color = rimGold.copy(alpha = 0.45f - (trail * 0.1f)),
                        startAngle = trailStart,
                        sweepAngle = 38f,
                        useCenter = false,
                        topLeft = Offset(center.x - trailRadius, center.y - trailRadius),
                        size = Size(trailRadius * 2f, trailRadius * 2f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            } else if (hasWinnerHighlight) {
                // Soft celebratory halo under the stopped wheel
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            rimGold.copy(alpha = 0.22f + pulsePhase * 0.14f),
                            haloPrimary.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = outerRadius + 16.dp.toPx()
                    ),
                    radius = outerRadius + 16.dp.toPx(),
                    center = center
                )
            }

            val dimTargetColor = if (wheelTheme.isDark) Color(0xFF0A0D18) else Color(0xFFDDE2EE)

            // Rotate entire wheel according to current animation angle
            rotate(degrees = currentRotation, pivot = center) {
                // 1. Draw Wheel Slices (non-winners gently dimmed when winner is spotlighted)
                for (i in 0 until sliceCount) {
                    if (hasWinnerHighlight && i == winningIndex) continue
                    val startAngle = i * sweepAngle - 90f // Start from top
                    val baseColor = animatedSliceColors[i]
                    val color = if (hasWinnerHighlight) {
                        lerp(baseColor, dimTargetColor, 0.42f * revealProgress.coerceAtMost(1f))
                    } else {
                        baseColor
                    }

                    // Slice arc
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = Size(outerRadius * 2f, outerRadius * 2f),
                        style = Fill
                    )

                    // Slice divider line
                    val rad = Math.toRadians(startAngle.toDouble())
                    val endX = center.x + (outerRadius * cos(rad)).toFloat()
                    val endY = center.y + (outerRadius * sin(rad)).toFloat()
                    drawLine(
                        color = dividerColor,
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // 1b. Draw Highlighted Winning Slice on top with radial glow + crisp luminous border
                if (hasWinnerHighlight) {
                    val winStartAngle = winningIndex * sweepAngle - 90f
                    val winBaseColor = animatedSliceColors[winningIndex]
                    val winRadius = outerRadius + (4.dp.toPx() * revealProgress.coerceAtMost(1f))
                    val brightWinColor = lerp(winBaseColor, rimGold, 0.24f + pulsePhase * 0.14f)

                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(brightWinColor, winBaseColor),
                            center = center,
                            radius = winRadius
                        ),
                        startAngle = winStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - winRadius, center.y - winRadius),
                        size = Size(winRadius * 2f, winRadius * 2f),
                        style = Fill
                    )

                    // Luminous golden-white winner slice wedge outline
                    drawArc(
                        color = Color.White.copy(alpha = 0.85f),
                        startAngle = winStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - winRadius, center.y - winRadius),
                        size = Size(winRadius * 2f, winRadius * 2f),
                        style = Stroke(width = 3.2.dp.toPx())
                    )

                    // Celebratory rim sparkles hugging the winning slice
                    for (s in 1..4) {
                        val sparkleAngle = winStartAngle + (sweepAngle * (s / 5f))
                        val sparkleRad = Math.toRadians(sparkleAngle.toDouble())
                        val sparkleDist = winRadius + 7.dp.toPx()
                        val sx = center.x + (sparkleDist * cos(sparkleRad)).toFloat()
                        val sy = center.y + (sparkleDist * sin(sparkleRad)).toFloat()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f + pulsePhase * 0.38f),
                            radius = (2.6f + (s % 2) * 1.1f).dp.toPx(),
                            center = Offset(sx, sy)
                        )
                    }
                }

                // 2. Draw Text on each slice with automatic luminance contrast
                val computedTextSize = (outerRadius * 0.085f).coerceIn(18f, 38f)
                val textMargin = 16.dp.toPx().coerceAtMost(outerRadius * 0.25f)

                for (i in 0 until sliceCount) {
                    val isWinnerSlice = hasWinnerHighlight && i == winningIndex
                    val midAngle = (i * sweepAngle + sweepAngle / 2f) - 90f
                    val movieTitle = if (i < movies.size) movies[i].title else "Film #${i + 1}"
                    val truncated = if (movieTitle.length > 16) movieTitle.take(14) + ".." else movieTitle
                    val sliceColor = animatedSliceColors[i]
                    val contrastText = wheelTheme.sliceTextColorFor(sliceColor)

                    textPaint.textSize = if (isWinnerSlice) computedTextSize * 1.07f else computedTextSize
                    textPaint.color = if (hasWinnerHighlight && !isWinnerSlice) {
                        contrastText.copy(alpha = 0.58f).toArgb()
                    } else {
                        contrastText.toArgb()
                    }
                    textPaint.setShadowLayer(
                        if (isWinnerSlice) 6f else if (wheelTheme.isDark) 4f else 2f,
                        1.5f,
                        1.5f,
                        wheelTheme.sliceTextShadowArgbFor(sliceColor)
                    )

                    rotate(degrees = midAngle, pivot = center) {
                        drawContext.canvas.nativeCanvas.drawText(
                            truncated,
                            center.x + outerRadius - textMargin,
                            center.y + (computedTextSize / 3f),
                            textPaint
                        )
                    }
                }

                // 3. Inner shadow / vignette gradient ring (kept clear on winner glow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, innerVignetteColor),
                        center = center,
                        radius = outerRadius
                    ),
                    radius = outerRadius,
                    center = center
                )

                // 4. Outer Rim & Carnival Lights
                drawCircle(
                    color = rimAccent,
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

                val dotCount = sliceCount * 3
                for (j in 0 until dotCount) {
                    val dotAngle = (j * (360f / dotCount))
                    val dotRad = Math.toRadians(dotAngle.toDouble())
                    val dotDistance = outerRadius + 4.dp.toPx()
                    val dotX = center.x + (dotDistance * cos(dotRad)).toFloat()
                    val dotY = center.y + (dotDistance * sin(dotRad)).toFloat()

                    val isLit = when {
                        isSpinning -> ((j % 2 == 0) xor (pulsePhase > 0.5f))
                        hasWinnerHighlight -> true
                        else -> (j % 2 == 0)
                    }
                    drawCircle(
                        color = if (isLit) litBulbColor else dimBulbColor,
                        radius = if (hasWinnerHighlight && j % 2 == 0) 4.dp.toPx() else 3.5.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }

                // 5. Center Hub Cap
                val hubRadius = 32.dp.toPx().coerceAtMost(outerRadius * 0.35f).coerceAtLeast(1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = animatedHubGradient,
                        center = center,
                        radius = hubRadius
                    ),
                    radius = hubRadius,
                    center = center
                )
                drawCircle(
                    color = hubBorderColor,
                    radius = hubRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = hubCenterAccent,
                    radius = (hubRadius * 0.24f).coerceAtLeast(3f),
                    center = center
                )
            }
        }

        // Static Needle / Indicator at Top (Pointing Down)
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 36.dp, height = 44.dp)
        ) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            rotate(degrees = needleDeflectionProvider(), pivot = Offset(width / 2f, 0f)) {
                needlePath.reset()
                needlePath.moveTo(width / 2f, height)
                needlePath.lineTo(0f, 0f)
                needlePath.lineTo(width, 0f)
                needlePath.close()

                needleShadowPath.reset()
                needleShadowPath.moveTo(width / 2f, height + 4.dp.toPx())
                needleShadowPath.lineTo(-2.dp.toPx(), 2.dp.toPx())
                needleShadowPath.lineTo(width + 2.dp.toPx(), 2.dp.toPx())
                needleShadowPath.close()

                drawPath(needleShadowPath, color = needleShadowColor)

                drawPath(
                    path = needlePath,
                    brush = Brush.verticalGradient(colors = animatedNeedleGradient)
                )
                drawPath(
                    path = needlePath,
                    color = needleBorderColor,
                    style = Stroke(width = 2.dp.toPx())
                )

                drawCircle(
                    color = needlePinColor,
                    radius = 4.dp.toPx(),
                    center = Offset(width / 2f, 10.dp.toPx())
                )
            }
        }
    }
}

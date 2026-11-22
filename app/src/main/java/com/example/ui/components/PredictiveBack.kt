package com.example.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalReduceMotion
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * Android 13+ "geri hareketi önizlemesi" (predictive back) için ekran küçülme efekti.
 *
 * Ölçüler Material Components'in `MaterialMainContainerBackHelper` sınıfından alındı:
 * ekran %90'a küçülür, kaydırılan kenarın tersine (8dp kenar boşluğu bırakarak) kayar,
 * parmağın dikey hareketini en fazla 24dp izler ve köşeleri yuvarlanır. İlerleme,
 * Material'in `PathInterpolator(0.1, 0.1, 0, 1)` eğrisiyle yumuşatılır.
 *
 * Kullanım: `MainActivity` her ekranı [PredictiveBackContainer] ile sarar; ekranlar
 * `BackHandler { onBack() }` yerine [ScreenBackHandler] kullanır.
 */
@Stable
class PredictiveBackState {
    internal val progress = Animatable(0f)
    internal var fromLeftEdge by mutableFloatStateOf(1f)
    internal var touchYDelta by mutableFloatStateOf(0f)
}

private val LocalPredictiveBackState = compositionLocalOf<PredictiveBackState?> { null }

private val BackProgressEasing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)
private const val MIN_SCALE = 0.9f
private val MinEdgeGap = 8.dp
private val MaxTranslationY = 24.dp
private val MaxCornerRadius = 28.dp

/** Bir ekranı sarar; içindeki [ScreenBackHandler] geri hareketinde bu kabı küçültür. */
@Composable
fun PredictiveBackContainer(content: @Composable () -> Unit) {
    val state = remember { PredictiveBackState() }
    val reduceMotion = LocalReduceMotion.current
    val density = LocalDensity.current
    val edgeGapPx = with(density) { MinEdgeGap.toPx() }
    val maxTyPx = with(density) { MaxTranslationY.toPx() }
    val cornerPx = with(density) { MaxCornerRadius.toPx() }

    Box(
        modifier = Modifier.graphicsLayer {
            val p = if (reduceMotion) 0f else state.progress.value
            if (p <= 0f) return@graphicsLayer
            val scale = 1f - (1f - MIN_SCALE) * p
            val availableX = max(0f, (size.width - MIN_SCALE * size.width) / 2f - edgeGapPx)
            val availableY = max(0f, (size.height - scale * size.height) / 2f - edgeGapPx)
            val yProgress = if (size.height > 0f) abs(state.touchYDelta) / size.height else 0f
            scaleX = scale
            scaleY = scale
            translationX = availableX * p * state.fromLeftEdge
            translationY = min(availableY, maxTyPx) * yProgress * sign(state.touchYDelta)
            shape = RoundedCornerShape(cornerPx * p)
            clip = true
        }
    ) {
        CompositionLocalProvider(LocalPredictiveBackState provides state) { content() }
    }
}

/**
 * `BackHandler` yerine geçer: geri tuşu/hareketi bittiğinde [onBack] çağrılır. Hareket
 * sürerken çevreleyen [PredictiveBackContainer] küçülür; vazgeçilirse eski haline döner.
 * Kap yoksa (ör. testler) ya da "Animasyonları azalt" açıksa yalnızca [onBack] çalışır.
 */
@Composable
fun ScreenBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val state = LocalPredictiveBackState.current
    val currentOnBack by rememberUpdatedState(onBack)
    val scope = rememberCoroutineScope()

    PredictiveBackHandler(enabled = enabled) { events ->
        var startY = Float.NaN
        try {
            events.collect { event: BackEventCompat ->
                if (state == null) return@collect
                if (startY.isNaN()) startY = event.touchY
                state.fromLeftEdge = if (event.swipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f
                state.touchYDelta = event.touchY - startY
                state.progress.snapTo(BackProgressEasing.transform(event.progress.coerceIn(0f, 1f)))
            }
            // Hareket tamamlandı: Material'deki gibi ekran çıkış geçişi sırasında
            // eski boyutuna döner (ekran değişmezse de küçük kalmaz).
            currentOnBack()
            if (state != null && state.progress.value > 0f) {
                scope.launch {
                    state.progress.animateTo(0f, tween(durationMillis = 300))
                    state.touchYDelta = 0f
                }
            }
        } catch (e: CancellationException) {
            // Vazgeçildi: Material'deki gibi kısa bir animasyonla eski boyuta dön.
            if (state != null) {
                scope.launch {
                    state.progress.animateTo(0f, tween(durationMillis = 150))
                    state.touchYDelta = 0f
                }
            }
            throw e
        }
    }
}

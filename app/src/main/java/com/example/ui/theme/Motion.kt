package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Hareket (motion) tokenları: uygulama genelinde tutarlı süre ve eğriler.
 * Material 3 "emphasized" eğrilerini temel alır.
 */
object CinemaMotion {
    /** Ekrana giren öğeler: hızlı başlar, yumuşak oturur. */
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Ekrandan çıkan öğeler: yavaş başlar, hızla kaybolur. */
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Genel amaçlı vurgulu eğri. */
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    const val DurationShort = 150
    const val DurationMedium = 300
    const val DurationLong = 450
}

/** Yumuşak, modern köşe ölçeği (kart, sayfa ve diyaloglar için). */
val CinemaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

/**
 * Basılınca hafifçe küçülüp yaylanarak geri dönen dokunsal his.
 * Tıklama mantığını değiştirmez; yalnızca görsel geri bildirim ekler.
 * Aynı [interactionSource] öğenin clickable/Button bileşenine de verilmelidir.
 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** remember'lı interactionSource üretici (okunabilirlik için). */
@androidx.compose.runtime.Composable
fun rememberPressSource(): MutableInteractionSource = remember { MutableInteractionSource() }

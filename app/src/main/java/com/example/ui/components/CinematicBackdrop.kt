package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CinemaTheme
import kotlin.random.Random

/**
 * Tüm ekranların arkasında duran sinematik ortam katmanı:
 * - Üstten süzülen sıcak projektör ışığı (altın),
 * - Sağ alttan kadife perde yansıması (kızıl),
 * - Kenarları karartan yumuşak vinyet,
 * - Çok hafif film greni (statik; her karede yeniden hesaplanmaz).
 */
@Composable
fun CinematicBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = CinemaTheme.colors
    val grain = remember {
        val rnd = Random(42)
        List(260) { Triple(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat()) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val glowAlpha = if (palette.isDark) 0.16f else 0.10f

            // Projektör ışığı (üst orta)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.marqueeGold.copy(alpha = glowAlpha), Color.Transparent),
                    center = Offset(w * 0.5f, -h * 0.05f),
                    radius = w * 0.95f
                ),
                radius = w * 0.95f,
                center = Offset(w * 0.5f, -h * 0.05f)
            )
            // Perde yansıması (sağ alt)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.velvetCrimson.copy(alpha = glowAlpha * 0.8f), Color.Transparent),
                    center = Offset(w * 1.05f, h * 0.92f),
                    radius = w * 0.85f
                ),
                radius = w * 0.85f,
                center = Offset(w * 1.05f, h * 0.92f)
            )
            // Vinyet
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = if (palette.isDark) 0.38f else 0.06f)
                    ),
                    center = Offset(w / 2f, h * 0.45f),
                    radius = maxOf(w, h) * 0.85f
                )
            )
            // Film greni
            val grainColor = if (palette.isDark) Color.White else Color.Black
            grain.forEach { (x, y, a) ->
                drawCircle(
                    color = grainColor.copy(alpha = 0.018f + a * 0.03f),
                    radius = 0.6f + a * 1.1f,
                    center = Offset(x * w, y * h)
                )
            }
        }
        content()
    }
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkCinemaPalette.marqueeGold,
    onPrimary = Color(0xFF1E1400),
    primaryContainer = Color(0xFF3F2C06),
    onPrimaryContainer = Color(0xFFFFE59A),
    secondary = DarkCinemaPalette.velvetCrimson,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A131E),
    onSecondaryContainer = Color(0xFFFFD9DF),
    tertiary = DarkCinemaPalette.filmTeal,
    onTertiary = Color(0xFF04221F),
    tertiaryContainer = Color(0xFF093834),
    onTertiaryContainer = Color(0xFFA8F5EC),
    background = DarkCinemaPalette.background,
    onBackground = DarkCinemaPalette.textPrimary,
    surface = DarkCinemaPalette.surface,
    onSurface = DarkCinemaPalette.textPrimary,
    surfaceVariant = DarkCinemaPalette.surfaceVariant,
    onSurfaceVariant = DarkCinemaPalette.textSecondary,
    outline = DarkCinemaPalette.border,
    error = DarkCinemaPalette.vetoRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightCinemaPalette.marqueeGold,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF0D6),
    onPrimaryContainer = Color(0xFF451A03),
    secondary = LightCinemaPalette.velvetCrimson,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE2E7),
    onSecondaryContainer = Color(0xFF4C0519),
    tertiary = LightCinemaPalette.filmTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF134E4A),
    background = LightCinemaPalette.background,
    onBackground = LightCinemaPalette.textPrimary,
    surface = LightCinemaPalette.surface,
    onSurface = LightCinemaPalette.textPrimary,
    surfaceVariant = LightCinemaPalette.surfaceVariant,
    onSurfaceVariant = LightCinemaPalette.textSecondary,
    outline = LightCinemaPalette.border,
    error = LightCinemaPalette.vetoRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkCinemaPalette else LightCinemaPalette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val wheelVisualTheme = WheelThemeManager.resolveWheelTheme(
        palette = palette,
        colorScheme = colorScheme
    )

    CompositionLocalProvider(
        LocalCinemaPalette provides palette,
        LocalWheelVisualTheme provides wheelVisualTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = CinemaShapes,
            content = content
        )
    }
}

package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CinemaPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val marqueeGold: Color,
    val marqueeGoldDark: Color,
    val velvetCrimson: Color,
    val rosePremiere: Color,
    val filmTeal: Color,
    val emeraldTicket: Color,
    val vetoRed: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val calloutSurface: Color,
    val selectedChipFill: Color,
    val onAccentTeal: Color,
    val onAccentCrimson: Color
)

/**
 * Koyu Tema — "Noir Sinema": Obsidyen salon karanlığı, kadife perde kızılı,
 * şampanya projektör altını ve Hollywood Teal vurguları.
 */
val DarkCinemaPalette = CinemaPalette(
    isDark = true,
    background = Color(0xFF0A0A0E),
    surface = Color(0xFF15141B),
    surfaceVariant = Color(0xFF211F28),
    border = Color(0xFF35313E),
    marqueeGold = Color(0xFFE9B949),
    marqueeGoldDark = Color(0xFFC49222),
    velvetCrimson = Color(0xFFD63447),
    rosePremiere = Color(0xFFE45A6F),
    filmTeal = Color(0xFF2EC4B6),
    emeraldTicket = Color(0xFF22C55E),
    vetoRed = Color(0xFFEF4444),
    textPrimary = Color(0xFFF6F2EA),
    textSecondary = Color(0xFFB5AEA3),
    textMuted = Color(0xFF7D776E),
    heroGradientStart = Color(0xFF2A161D),
    heroGradientEnd = Color(0xFF141118),
    calloutSurface = Color(0xFF1E1920),
    selectedChipFill = Color(0xFF2E1B22),
    onAccentTeal = Color(0xFF041F1C),
    onAccentCrimson = Color(0xFFFFFFFF)
)

/**
 * Açık Tema — "Matine Fildişi": Sıcak selüloit kremi, klasik afiş kağıdı beyazı,
 * koyu bordo perde tonu, yanık kehribar altını ve derin sinematik teal.
 */
val LightCinemaPalette = CinemaPalette(
    isDark = false,
    background = Color(0xFFF9F4EC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEFE7DA),
    border = Color(0xFFD9CDBD),
    marqueeGold = Color(0xFFB45309),
    marqueeGoldDark = Color(0xFF92400E),
    velvetCrimson = Color(0xFF9F1239),
    rosePremiere = Color(0xFFBE185D),
    filmTeal = Color(0xFF0F766E),
    emeraldTicket = Color(0xFF15803D),
    vetoRed = Color(0xFFDC2626),
    textPrimary = Color(0xFF1C1917),
    textSecondary = Color(0xFF57534E),
    textMuted = Color(0xFF78716C),
    heroGradientStart = Color(0xFFF6E8D6),
    heroGradientEnd = Color(0xFFECE0CE),
    calloutSurface = Color(0xFFF3ECE1),
    selectedChipFill = Color(0xFFF8E1E6),
    onAccentTeal = Color(0xFFFFFFFF),
    onAccentCrimson = Color(0xFFFFFFFF)
)

val LocalCinemaPalette = staticCompositionLocalOf { DarkCinemaPalette }

object CinemaTheme {
    val colors: CinemaPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalCinemaPalette.current
}

// Theme-reactive semantic color getters so all screens automatically support Light & Dark modes
val CinemaBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.background

val CinemaSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.surface

val CinemaSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.surfaceVariant

val CinemaBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.border

val PopcornGold: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.marqueeGold

val PopcornGoldDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.marqueeGoldDark

val NeonPurple: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.velvetCrimson

val NeonPink: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.rosePremiere

val ElectricCyan: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.filmTeal

val EmeraldGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.emeraldTicket

val VetoRed: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.vetoRed

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCinemaPalette.current.textMuted

// Cinematic Wheel slice colors (warm Hollywood film-grade palette)
val WheelColors = listOf(
    Color(0xFFB91C1C), // Velvet Theater Red
    Color(0xFFD97706), // Marquee Amber Gold
    Color(0xFF0F766E), // Hollywood Teal
    Color(0xFF9F1239), // Burgundy Premiere
    Color(0xFF15803D), // Emerald Ticket
    Color(0xFF1D4ED8), // Midnight IMAX Blue
    Color(0xFFC2410C), // Tungsten Burnt Orange
    Color(0xFF334155), // Noir Slate
    Color(0xFFCA8A04), // Oscar Brass
    Color(0xFF0E7490)  // Deep Cyan Grade
)

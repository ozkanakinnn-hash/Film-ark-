package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

/**
 * Seçili uygulama temasının (Koyu / Açık) ana renk paletine göre çarkın tüm görsel
 * katmanlarını (dilimler, metin kontrastı, çerçeve, ampuller, göbek ve ibre)
 * otomatik olarak türeten sinematik çark teması modeli.
 */
@Immutable
data class WheelVisualTheme(
    val themeId: String,
    val themeLabel: String,
    val isDark: Boolean,
    val sliceColors: List<Color>,
    val dividerColor: Color,
    val outerRimColor: Color,
    val outerRimAccentColor: Color,
    val marqueeLitBulbColor: Color,
    val marqueeDimBulbColor: Color,
    val innerVignetteColor: Color,
    val hubGradientColors: List<Color>,
    val hubBorderColor: Color,
    val hubCenterAccentColor: Color,
    val needleGradientColors: List<Color>,
    val needleBorderColor: Color,
    val needleShadowColor: Color,
    val needlePinColor: Color,
    val haloPrimaryColor: Color,
    val haloSecondaryColor: Color,
    val projectorRayColor: Color,
    val darkSliceTextColor: Color,
    val lightSliceTextColor: Color
) {
    fun sliceColorAt(index: Int): Color {
        if (sliceColors.isEmpty()) return outerRimColor
        val safeIndex = ((index % sliceColors.size) + sliceColors.size) % sliceColors.size
        return sliceColors[safeIndex]
    }

    /**
     * Dilim arka planının parlaklığına (luminance) göre otomatik yüksek kontrastlı metin rengi seçer.
     */
    fun sliceTextColorFor(sliceColor: Color): Color {
        return if (sliceColor.luminance() > 0.34f) darkSliceTextColor else lightSliceTextColor
    }

    fun sliceTextShadowArgbFor(sliceColor: Color): Int {
        return if (sliceColor.luminance() > 0.34f) {
            android.graphics.Color.argb(120, 255, 252, 245)
        } else {
            android.graphics.Color.argb(190, 10, 10, 14)
        }
    }
}

val LocalWheelVisualTheme = staticCompositionLocalOf {
    WheelThemeManager.resolveWheelTheme(DarkCinemaPalette)
}

/**
 * Kullanıcının seçtiği koyu/açık temanın ana renk paletini (ColorScheme + CinemaPalette)
 * okuyarak çarkın tüm görsel renk şemasını otomatik olarak yeniden hesaplayan tema yöneticisi.
 */
object WheelThemeManager {

    const val DARK_THEME_ID = "noir_cinema_dark"
    const val LIGHT_THEME_ID = "matinee_ivory_light"

    val current: WheelVisualTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalWheelVisualTheme.current

    fun resolveWheelTheme(isDark: Boolean): WheelVisualTheme {
        return resolveWheelTheme(if (isDark) DarkCinemaPalette else LightCinemaPalette)
    }

    fun resolveWheelTheme(
        palette: CinemaPalette,
        colorScheme: ColorScheme? = null
    ): WheelVisualTheme {
        val primary = colorScheme?.primary ?: palette.marqueeGold
        val primaryDark = palette.marqueeGoldDark
        val secondary = colorScheme?.secondary ?: palette.velvetCrimson
        val tertiary = colorScheme?.tertiary ?: palette.filmTeal
        val rose = palette.rosePremiere
        val emerald = palette.emeraldTicket
        val surface = colorScheme?.surface ?: palette.surface
        val surfaceVariant = colorScheme?.surfaceVariant ?: palette.surfaceVariant
        val background = colorScheme?.background ?: palette.background

        return if (palette.isDark) {
            // Koyu Tema ("Noir Sinema"): Derin kadife, şampanya altını ve neon-sinematik teal vurgular
            val slices = listOf(
                lerp(secondary, Color(0xFF7F1D1D), 0.25f),         // Kadife Perde Kızılı
                lerp(primary, primaryDark, 0.30f),                  // Marquee Kehribar Altın
                lerp(tertiary, Color(0xFF0A4F49), 0.32f),           // Hollywood Teal
                lerp(rose, Color(0xFF5B122C), 0.38f),               // Prömiyer Bordo
                lerp(emerald, Color(0xFF0E4B26), 0.42f),            // Zümrüt Bilet
                lerp(surfaceVariant, tertiary, 0.44f),              // Gece IMAX Teal-Slate
                lerp(primary, secondary, 0.46f),                    // Tungsten Bakır
                lerp(palette.heroGradientStart, surfaceVariant, 0.35f), // Noir Kadife
                lerp(primaryDark, Color(0xFF5C3D06), 0.28f),        // Oskar Pirinci
                lerp(tertiary, secondary, 0.28f)                    // Gece Turkuaz-Erik
            )

            WheelVisualTheme(
                themeId = DARK_THEME_ID,
                themeLabel = "Noir Sinema Çarkı",
                isDark = true,
                sliceColors = slices,
                dividerColor = palette.textPrimary.copy(alpha = 0.44f),
                outerRimColor = primary,
                outerRimAccentColor = primaryDark,
                marqueeLitBulbColor = Color(0xFFFFF8E7),
                marqueeDimBulbColor = primaryDark,
                innerVignetteColor = Color(0x94000000),
                hubGradientColors = listOf(
                    lerp(primary, Color.White, 0.48f),
                    primary,
                    primaryDark
                ),
                hubBorderColor = Color(0xFFFFF4D6),
                hubCenterAccentColor = secondary,
                needleGradientColors = listOf(
                    lerp(primary, Color.White, 0.52f),
                    primary,
                    secondary
                ),
                needleBorderColor = Color(0xFFFFF8E7),
                needleShadowColor = Color(0x99000000),
                needlePinColor = Color(0xFFFFF8E7),
                haloPrimaryColor = primary,
                haloSecondaryColor = secondary,
                projectorRayColor = primary,
                darkSliceTextColor = Color(0xFF1C1917),
                lightSliceTextColor = Color(0xFFFFFBF2)
            )
        } else {
            // Açık Tema ("Matine Fildişi"): Güneşli selüloit kremi, yanık kehribar, matine bordosu & teal
            val slices = listOf(
                lerp(background, primary, 0.24f),                   // Sıcak Şampanya Fildişi
                lerp(surface, secondary, 0.19f),                    // Afiş Gül Kurusu
                lerp(surface, tertiary, 0.22f),                     // Matine Deniz Teal
                lerp(primary, primaryDark, 0.14f),                  // Yanık Kehribar Altın
                lerp(surfaceVariant, rose, 0.24f),                  // Pastel Kadife Gül
                lerp(tertiary, Color(0xFF134E4A), 0.18f),           // Derin Hollywood Teal
                lerp(palette.heroGradientStart, background, 0.22f), // Selüloit Krem
                lerp(secondary, Color(0xFF881337), 0.14f),          // Prömiyer Bordo
                lerp(surfaceVariant, emerald, 0.22f),               // Nane Matine Bileti
                lerp(primary, Color(0xFFFDE68A), 0.42f)             // Parlak Matine Altını
            )

            WheelVisualTheme(
                themeId = LIGHT_THEME_ID,
                themeLabel = "Matine Fildişi Çarkı",
                isDark = false,
                sliceColors = slices,
                dividerColor = palette.border.copy(alpha = 0.92f),
                outerRimColor = primary,
                outerRimAccentColor = primaryDark,
                marqueeLitBulbColor = surface,
                marqueeDimBulbColor = lerp(primary, surface, 0.45f),
                innerVignetteColor = primaryDark.copy(alpha = 0.20f),
                hubGradientColors = listOf(
                    surface,
                    palette.heroGradientStart,
                    primary
                ),
                hubBorderColor = primaryDark,
                hubCenterAccentColor = secondary,
                needleGradientColors = listOf(
                    rose,
                    secondary,
                    primaryDark
                ),
                needleBorderColor = surface,
                needleShadowColor = primaryDark.copy(alpha = 0.36f),
                needlePinColor = surface,
                haloPrimaryColor = primary,
                haloSecondaryColor = tertiary,
                projectorRayColor = primary,
                darkSliceTextColor = Color(0xFF1C1917),
                lightSliceTextColor = Color(0xFFFFFBF2)
            )
        }
    }

    /**
     * Testler ve önizlemeler için ilk dilim renginin ARGB tam sayısını döndürür.
     */
    fun primarySliceArgb(isDark: Boolean): Int {
        return resolveWheelTheme(isDark).sliceColorAt(0).toArgb()
    }
}

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
    /**
     * [count] dilimli bir çarkta i. dilimin rengi. Son dilim ilk dilimle aynı renge denk gelirse
     * (ör. 5 ya da 9 dilim) bir sonraki renge kaydırılır; böylece yan yana aynı renk oluşmaz.
     */
    fun sliceColorFor(index: Int, count: Int): Color {
        val size = sliceColors.size
        if (size > 2 && count > 1 && index == count - 1 && (count - 1) % size == 0) {
            return sliceColorAt(index + 1)
        }
        return sliceColorAt(index)
    }

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
        val surface = colorScheme?.surface ?: palette.surface

        // Logo paleti: altın (#E9B949), kızıl (#D63447), noir (#0B0A0F)
        val brandGold = primary
        val brandCrimson = secondary
        val noirHubGradient = listOf(Color(0xFF2A2530), Color(0xFF15131A), Color(0xFF0B0A0F))
        val needleCrimson = listOf(lerp(brandCrimson, Color.White, 0.22f), brandCrimson, lerp(brandCrimson, Color.Black, 0.25f))

        return if (palette.isDark) {
            // Koyu Tema ("Noir Sinema"): logo gibi noir zemin üstünde altın ve kızıl dilimler
            val slices = listOf(
                Color(0xFF1D1A23),                                  // Noir
                brandGold,                                          // Logo altını
                Color(0xFF2E2734),                                  // Patlıcan noir
                lerp(brandCrimson, Color.Black, 0.35f)              // Koyu kadife kızıl
            )

            WheelVisualTheme(
                themeId = DARK_THEME_ID,
                themeLabel = "Noir Sinema Çarkı",
                isDark = true,
                sliceColors = slices,
                dividerColor = brandGold.copy(alpha = 0.35f),
                outerRimColor = brandGold,
                outerRimAccentColor = primaryDark,
                marqueeLitBulbColor = Color(0xFFFFF8E7),
                marqueeDimBulbColor = primaryDark,
                innerVignetteColor = Color(0x80000000),
                hubGradientColors = noirHubGradient,
                hubBorderColor = brandGold,
                hubCenterAccentColor = brandGold,
                needleGradientColors = needleCrimson,
                needleBorderColor = Color(0xFFFFF8E7).copy(alpha = 0.9f),
                needleShadowColor = Color(0x99000000),
                needlePinColor = Color.Transparent,
                haloPrimaryColor = brandGold,
                haloSecondaryColor = brandCrimson,
                projectorRayColor = brandGold,
                darkSliceTextColor = Color(0xFF1C1917),
                lightSliceTextColor = Color(0xFFFFFBF2)
            )
        } else {
            // Açık Tema ("Matine Fildişi"): fildişi ve kum zeminde kehribar altın ve kızıl dilimler
            val slices = listOf(
                Color(0xFFFFF9EE),                                  // Fildişi
                brandGold,                                          // Kehribar altın
                Color(0xFFEFE3CF),                                  // Kum
                brandCrimson                                        // Kızıl
            )

            WheelVisualTheme(
                themeId = LIGHT_THEME_ID,
                themeLabel = "Matine Fildişi Çarkı",
                isDark = false,
                sliceColors = slices,
                dividerColor = primaryDark.copy(alpha = 0.35f),
                outerRimColor = brandGold,
                outerRimAccentColor = primaryDark,
                marqueeLitBulbColor = surface,
                marqueeDimBulbColor = lerp(brandGold, surface, 0.45f),
                innerVignetteColor = primaryDark.copy(alpha = 0.14f),
                hubGradientColors = noirHubGradient,
                hubBorderColor = brandGold,
                hubCenterAccentColor = Color(0xFFE9B949),
                needleGradientColors = needleCrimson,
                needleBorderColor = surface,
                needleShadowColor = primaryDark.copy(alpha = 0.36f),
                needlePinColor = Color.Transparent,
                haloPrimaryColor = brandGold,
                haloSecondaryColor = brandCrimson,
                projectorRayColor = brandGold,
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

package com.example.data.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import coil.imageLoader
import coil.memory.MemoryCache
import com.example.R
import com.example.data.local.ThemePreferences
import com.example.data.model.Movie
import java.io.File
import java.io.FileOutputStream

/**
 * Seçilen günübirlik filmi veya kullanıcının favori listesini sosyal medyada paylaşılabilir
 * yüksek çözünürlüklü PNG poster görseline dönüştüren sinematik kart oluşturucu.
 */
object SharePosterRenderer {

    private const val CARD_WIDTH = 800
    private const val CARD_HEIGHT = 1120

    fun createMoviePosterShareUri(
        context: Context,
        movie: Movie,
        isDarkTheme: Boolean = ThemePreferences.isDarkMode.value
    ): Uri? {
        return runCatching {
            val cachedPoster = resolveCachedPosterBitmap(context, movie)
            val bitmap = renderSingleMoviePosterBitmap(
                movie = movie,
                isDarkTheme = isDarkTheme,
                posterBitmap = cachedPoster
            )
            saveBitmapAndGetUri(context, bitmap, "movie_${movie.id}.png")
        }.getOrNull()
    }

    fun createFavoritesPosterShareUri(
        context: Context,
        favorites: List<Movie>,
        isDarkTheme: Boolean = ThemePreferences.isDarkMode.value
    ): Uri? {
        if (favorites.isEmpty()) return null
        return runCatching {
            val posterMap = favorites.take(6).mapNotNull { movie ->
                resolveCachedPosterBitmap(context, movie)?.let { bmp -> movie.id to bmp }
            }.toMap()
            val bitmap = renderFavoritesListPosterBitmap(
                favorites = favorites,
                isDarkTheme = isDarkTheme,
                posterBitmaps = posterMap
            )
            saveBitmapAndGetUri(context, bitmap, "favorites_collage_${favorites.size}.png")
        }.getOrNull()
    }

    fun renderSingleMoviePosterBitmap(
        movie: Movie,
        isDarkTheme: Boolean = true,
        posterBitmap: Bitmap? = null
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgTop = if (isDarkTheme) 0xFF0A0A0E.toInt() else 0xFFF9F4EC.toInt()
        val bgBottom = if (isDarkTheme) 0xFF21141C.toInt() else 0xFFEDE2D2.toInt()
        val surfaceColor = if (isDarkTheme) 0xFF15141B.toInt() else 0xFFFFFFFF.toInt()
        val primaryText = if (isDarkTheme) 0xFFF6F2EA.toInt() else 0xFF1C1917.toInt()
        val secondaryText = if (isDarkTheme) 0xFFB5AEA3.toInt() else 0xFF57534E.toInt()
        val goldColor = if (isDarkTheme) 0xFFE9B949.toInt() else 0xFFB45309.toInt()
        val crimsonColor = if (isDarkTheme) 0xFFD63447.toInt() else 0xFF9F1239.toInt()
        val tealColor = if (isDarkTheme) 0xFF2EC4B6.toInt() else 0xFF0F766E.toInt()

        // Background gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
                bgTop, bgBottom, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), bgPaint)

        // Outer marquee frame
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = goldColor
        }
        canvas.drawRoundRect(RectF(24f, 24f, CARD_WIDTH - 24f, CARD_HEIGHT - 24f), 36f, 36f, framePaint)

        // Brand Header Pill
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surfaceColor }
        val pillRect = RectF(160f, 56f, CARD_WIDTH - 160f, 110f)
        canvas.drawRoundRect(pillRect, 28f, 28f, pillPaint)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            textSize = 22f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("NE İZLESEK? • GÜNÜN FİLM POSTERİ", CARD_WIDTH / 2f, 91f, headerPaint)

        // Center Poster Artwork Frame
        val posterRect = RectF(180f, 145f, CARD_WIDTH - 180f, 705f)
        drawClippedPosterArtwork(
            canvas = canvas,
            rect = posterRect,
            accentColorHex = movie.colorHex,
            title = movie.title,
            posterBitmap = posterBitmap,
            goldColor = goldColor
        )

        // IMDb Pill Overlapping Poster Top-Right
        val imdbRect = RectF(posterRect.right - 134f, posterRect.top + 16f, posterRect.right - 14f, posterRect.top + 62f)
        val imdbBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xE60A0A0E.toInt() }
        canvas.drawRoundRect(imdbRect, 14f, 14f, imdbBgPaint)
        val imdbTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE9B949.toInt()
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("★ ${movie.imdb}", imdbRect.centerX(), imdbRect.centerY() + 8f, imdbTextPaint)

        // Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryText
            textSize = 42f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val clippedTitle = if (movie.title.length > 26) movie.title.take(24) + "…" else movie.title
        canvas.drawText(clippedTitle, CARD_WIDTH / 2f, 775f, titlePaint)

        // Meta line
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tealColor
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("${movie.year}  •  ${movie.genre}  •  ${movie.durationMins} dk", CARD_WIDTH / 2f, 822f, metaPaint)

        // Hook / Logline box
        val cardBox = RectF(64f, 854f, CARD_WIDTH - 64f, 1004f)
        val cardBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surfaceColor }
        canvas.drawRoundRect(cardBox, 24f, 24f, cardBoxPaint)
        val boxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = crimsonColor
        }
        canvas.drawRoundRect(cardBox, 24f, 24f, boxStroke)

        val hookText = movie.whyWatch.ifBlank { movie.logline }.let {
            if (it.length > 90) it.take(87) + "…" else it
        }
        val hookPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryText
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        val midBreak = hookText.indexOf(' ', startIndex = (hookText.length / 2).coerceAtMost(hookText.length - 1).coerceAtLeast(0))
        if (hookText.length > 44 && midBreak > 0) {
            canvas.drawText(hookText.substring(0, midBreak), CARD_WIDTH / 2f, 916f, hookPaint)
            canvas.drawText(hookText.substring(midBreak + 1), CARD_WIDTH / 2f, 954f, hookPaint)
        } else {
            canvas.drawText(hookText, CARD_WIDTH / 2f, 936f, hookPaint)
        }

        // Platforms + Brand Footer
        val platformsText = if (movie.platforms.isNotEmpty()) {
            "İzlenebilir: ${movie.platforms.joinToString(" • ")}"
        } else {
            "Karar Çarkıyla Seçildi • neizlesek.app"
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            textSize = 21f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(platformsText, CARD_WIDTH / 2f, 1058f, footerPaint)

        return bitmap
    }

    fun renderFavoritesListPosterBitmap(
        favorites: List<Movie>,
        isDarkTheme: Boolean = true,
        posterBitmaps: Map<String, Bitmap> = emptyMap()
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgTop = if (isDarkTheme) 0xFF0A0A0E.toInt() else 0xFFF9F4EC.toInt()
        val bgBottom = if (isDarkTheme) 0xFF28121E.toInt() else 0xFFF0E4D4.toInt()
        val surfaceColor = if (isDarkTheme) 0xFF15141B.toInt() else 0xFFFFFFFF.toInt()
        val primaryText = if (isDarkTheme) 0xFFF6F2EA.toInt() else 0xFF1C1917.toInt()
        val secondaryText = if (isDarkTheme) 0xFFB5AEA3.toInt() else 0xFF57534E.toInt()
        val goldColor = if (isDarkTheme) 0xFFE9B949.toInt() else 0xFFB45309.toInt()
        val pinkColor = if (isDarkTheme) 0xFFE45A6F.toInt() else 0xFFBE185D.toInt()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
                bgTop, bgBottom, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), bgPaint)

        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = pinkColor
        }
        canvas.drawRoundRect(RectF(24f, 24f, CARD_WIDTH - 24f, CARD_HEIGHT - 24f), 36f, 36f, framePaint)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            textSize = 34f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("❤️ FAVORİ FİLM LİSTEM", CARD_WIDTH / 2f, 94f, headerPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryText
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Toplam ${favorites.size} Seçkin Film • Ne İzlesek? Koleksiyonu", CARD_WIDTH / 2f, 132f, subPaint)

        val displayItems = favorites.take(6)
        val cols = 2
        val gapX = 24f
        val gapY = 22f
        val startX = 52f
        val startY = 170f
        val tileW = (CARD_WIDTH - (startX * 2f) - gapX) / cols
        val tileH = 264f

        val tileBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surfaceColor }
        val tileBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = goldColor
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryText
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryText
            textSize = 18f
        }
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        displayItems.forEachIndexed { idx, movie ->
            val col = idx % cols
            val row = idx / cols
            val left = startX + col * (tileW + gapX)
            val top = startY + row * (tileH + gapY)
            val cardRect = RectF(left, top, left + tileW, top + tileH)

            canvas.drawRoundRect(cardRect, 22f, 22f, tileBgPaint)
            canvas.drawRoundRect(cardRect, 22f, 22f, tileBorderPaint)

            val miniPosterRect = RectF(left + 14f, top + 14f, left + 132f, top + tileH - 14f)
            drawClippedPosterArtwork(
                canvas = canvas,
                rect = miniPosterRect,
                accentColorHex = movie.colorHex,
                title = movie.title,
                posterBitmap = posterBitmaps[movie.id],
                goldColor = goldColor
            )

            val textX = miniPosterRect.right + 14f
            canvas.drawText("#${idx + 1} FAVORİ", textX, top + 48f, badgePaint)

            val shortTitle = if (movie.title.length > 15) movie.title.take(13) + "…" else movie.title
            canvas.drawText(shortTitle, textX, top + 92f, titlePaint)
            canvas.drawText("★ ${movie.imdb}", textX, top + 130f, badgePaint)
            canvas.drawText("${movie.year} • ${movie.durationMins} dk", textX, top + 168f, metaPaint)

            val shortGenre = movie.genre.split("/").firstOrNull()?.trim().orEmpty()
            canvas.drawText(shortGenre, textX, top + 206f, metaPaint)
        }

        val footerText = if (favorites.size > 6) {
            "+${favorites.size - 6} film daha • neizlesek.app ile listeni paylaş!"
        } else {
            "Ne İzlesek? Karar Çarkı • neizlesek.app"
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pinkColor
            textSize = 22f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(footerText, CARD_WIDTH / 2f, 1068f, footerPaint)

        return bitmap
    }

    private fun drawClippedPosterArtwork(
        canvas: Canvas,
        rect: RectF,
        accentColorHex: Long,
        title: String,
        posterBitmap: Bitmap?,
        goldColor: Int
    ) {
        val clipPath = Path().apply { addRoundRect(rect, 22f, 22f, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(clipPath)

        if (posterBitmap != null && !posterBitmap.isRecycled) {
            canvas.drawBitmap(posterBitmap, null, rect, Paint(Paint.FILTER_BITMAP_FLAG))
        } else {
            val topColor = (accentColorHex or 0xFF000000L).toInt()
            val gradPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    topColor, 0xFF0F0A1E.toInt(), Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(rect, gradPaint)

            // Stylized spotlight ornament + title inside poster card
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x33FFFFFF
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(rect.centerX(), rect.centerY() - 28f, rect.width() * 0.28f, glowPaint)

            val emblemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFF6F2EA.toInt()
                textSize = (rect.width() * 0.14f).coerceIn(18f, 44f)
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val initials = title.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
                .ifBlank { "🎬" }
            canvas.drawText(initials, rect.centerX(), rect.centerY() - 14f, emblemPaint)

            val titleBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xB3090710.toInt()
            }
            canvas.drawRect(rect.left, rect.bottom - rect.height() * 0.28f, rect.right, rect.bottom, titleBarPaint)
            val posterTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = (rect.width() * 0.085f).coerceIn(13f, 26f)
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val shortName = if (title.length > 18) title.take(16) + "…" else title
            canvas.drawText(shortName, rect.centerX(), rect.bottom - rect.height() * 0.11f, posterTitlePaint)
        }
        canvas.restore()

        val posterStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = goldColor
        }
        canvas.drawRoundRect(rect, 22f, 22f, posterStroke)
    }

    private fun resolveCachedPosterBitmap(context: Context, movie: Movie): Bitmap? {
        val url = movie.resolvedPosterUrl()
        if (!url.isNullOrBlank()) {
            val loader = context.imageLoader
            val memBitmap = loader.memoryCache?.get(MemoryCache.Key(url))?.bitmap
            if (memBitmap != null && !memBitmap.isRecycled) {
                return memBitmap
            }
            val diskSnapshot = runCatching { loader.diskCache?.openSnapshot(url) }.getOrNull()
            if (diskSnapshot != null) {
                diskSnapshot.use { snap ->
                    val decoded = BitmapFactory.decodeFile(snap.data.toFile().absolutePath)
                    if (decoded != null) return decoded
                }
            }
        }
        return runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_default_poster_placeholder)
        }.getOrNull()
    }

    private fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap, fileName: String): Uri {
        val dir = File(context.cacheDir, "shared_posters").apply {
            if (!exists()) mkdirs()
        }
        val outFile = File(dir, fileName)
        FileOutputStream(outFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream)
            stream.flush()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outFile
        )
    }

    fun buildFavoritesShareText(favorites: List<Movie>): String {
        return buildString {
            appendLine("🍿 NE İZLESEK? • FAVORİ FİLM LİSTEM ❤️")
            appendLine("${favorites.size} seçkin film önerisi:")
            appendLine()
            favorites.take(10).forEachIndexed { index, movie ->
                appendLine("${index + 1}. 🎬 ${movie.title} (${movie.year}) — ★ ${movie.imdb}")
            }
            if (favorites.size > 10) {
                appendLine("…ve +${favorites.size - 10} film daha!")
            }
            appendLine()
            append("Sen de film çarkını çevir: https://neizlesek.app")
        }.trim()
    }
}

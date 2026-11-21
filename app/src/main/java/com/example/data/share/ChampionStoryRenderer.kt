package com.example.data.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.Movie

/**
 * VS düellosunun kazananını Instagram/WhatsApp hikâyesi boyutunda (1080×1920, 9:16) PNG olarak çizer.
 * Ekrandaki ChampionStoryCard ile aynı düzeni kullanır: tam boy afiş, logo, KAZANAN rozeti, bilgiler.
 */
object ChampionStoryRenderer {

    const val WIDTH = 1080
    const val HEIGHT = 1920

    private const val GOLD = 0xFFE9B949.toInt()
    private const val CRIMSON = 0xFFD63447.toInt()
    private const val IVORY = 0xFFF6F2EA.toInt()
    private const val NOIR = 0xFF0B0A0F.toInt()

    fun createShareUri(context: Context, movie: Movie, roundsWon: Int): Uri? = runCatching {
        val poster = SharePosterRenderer.resolveCachedPosterBitmap(context, movie)
        val bitmap = render(context, movie, roundsWon, poster)
        SharePosterRenderer.saveBitmapAndGetUri(context, bitmap, "vs_champion_${movie.id}.png")
    }.onFailure { android.util.Log.w("ChampionStory", "Hikâye görseli üretilemedi", it) }.getOrNull()

    /** Hikâye görselini sistem paylaşım menüsüyle paylaşır. Başarılıysa true döner. */
    fun share(context: Context, movie: Movie, roundsWon: Int): Boolean {
        // Görsel üretilemezse (ör. bellek yetersiz) en azından metinle paylaş
        val uri = createShareUri(context, movie, roundsWon)
        val send = Intent(Intent.ACTION_SEND).apply {
            if (uri != null) {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
            } else {
                type = "text/plain"
            }
            putExtra(
                Intent.EXTRA_TEXT,
                "🏆 Film karşılaştırmasının kazananı: ${movie.title} (${movie.year}) • Ne İzlesek? ile seçtik!"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Kazananı paylaş").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching { context.startActivity(chooser) }.isSuccess
    }

    fun render(context: Context, movie: Movie, roundsWon: Int, poster: Bitmap?): Bitmap {
        val bmp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val w = WIDTH.toFloat()
        val h = HEIGHT.toFloat()

        // 1) Tam boy afiş (merkezden kırpılmış) ya da tür renginde degrade
        c.drawColor(NOIR)
        if (poster != null && !poster.isRecycled) {
            val scale = maxOf(w / poster.width, h / poster.height)
            val dw = poster.width * scale
            val dh = poster.height * scale
            c.drawBitmap(poster, null, RectF((w - dw) / 2f, (h - dh) / 2f, (w + dw) / 2f, (h + dh) / 2f),
                Paint(Paint.FILTER_BITMAP_FLAG))
        } else {
            val accent = (movie.colorHex or 0xFF000000L).toInt()
            c.drawRect(0f, 0f, w, h, Paint().apply {
                shader = LinearGradient(0f, 0f, w, h, accent, NOIR, Shader.TileMode.CLAMP)
            })
        }

        // 2) Okunabilirlik için üst ve alt karartma
        c.drawRect(0f, 0f, w, h * 0.30f, Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h * 0.30f, 0xE60B0A0F.toInt(), 0x000B0A0F, Shader.TileMode.CLAMP)
        })
        c.drawRect(0f, h * 0.42f, w, h, Paint().apply {
            shader = LinearGradient(0f, h * 0.42f, 0f, h, 0x000B0A0F, 0xF70B0A0F.toInt(), Shader.TileMode.CLAMP)
        })

        // 3) Logo karosu + kelime logosu
        val tile = RectF(72f, 96f, 72f + 132f, 96f + 132f)
        c.drawRoundRect(tile, 34f, 34f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = NOIR })
        c.drawRoundRect(tile, 34f, 34f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 3f; color = 0x55E9B949
        })
        ContextCompat.getDrawable(context, R.drawable.brand_mark)?.let { mark ->
            val mh = 92f
            val mw = mh * mark.intrinsicWidth / mark.intrinsicHeight.coerceAtLeast(1)
            val left = tile.centerX() - mw / 2f
            val top = tile.centerY() - mh / 2f
            mark.setBounds(left.toInt(), top.toInt(), (left + mw).toInt(), (top + mh).toInt())
            mark.draw(c)
        }
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = IVORY; textSize = 64f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = -0.02f
        }
        c.drawText("Ne İzlesek?", 236f, 176f, brandPaint)
        c.drawText("FİLM KARŞILAŞTIR", 238f, 222f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD; textSize = 30f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.25f
        })

        // 4) KAZANAN rozeti
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NOIR; textSize = 40f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.2f
        }
        val badgeText = "🏆  KAZANAN"
        val bw = badgePaint.measureText(badgeText) + 80f
        val badge = RectF(72f, h * 0.62f, 72f + bw, h * 0.62f + 84f)
        c.drawRoundRect(badge, 42f, 42f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD })
        c.drawText(badgeText, badge.left + 40f, badge.centerY() + 14f, badgePaint)

        // 5) Film adı (en fazla 3 satır), bilgiler, alt yazı
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = IVORY; textSize = 104f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = -0.02f
        }
        val textWidth = (w - 144f).toInt()
        val titleLayout = StaticLayout.Builder.obtain(movie.title, 0, movie.title.length, titlePaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(3)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
            .setLineSpacing(0f, 0.95f)
            .build()
        c.save()
        c.translate(72f, badge.bottom + 40f)
        titleLayout.draw(c)
        c.restore()

        var y = badge.bottom + 40f + titleLayout.height + 70f
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xCCF6F2EA.toInt(); textSize = 42f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        c.drawText("${movie.year}  •  ${movie.genre}  •  ${movie.durationMins} dk", 72f, y, metaPaint)
        y += 70f
        c.drawText("★ ${movie.imdb}", 72f, y, Paint(metaPaint).apply {
            color = GOLD; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        })
        val rounds = if (roundsWon > 0) "$roundsWon turda rakiplerini eledi" else "Karşılaştırmanın tartışmasız galibi"
        c.drawText(rounds, 72f + metaPaint.measureText("★ ${movie.imdb}") + 40f, y, metaPaint)

        // 6) Alt şerit
        c.drawRect(0f, h - 8f, w, h, Paint().apply {
            shader = LinearGradient(0f, 0f, w, 0f, GOLD, CRIMSON, Shader.TileMode.CLAMP)
        })
        c.drawText("Kararsızlığa son • Ne İzlesek? Karar Çarkı", 72f, h - 72f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x99F6F2EA.toInt(); textSize = 34f
        })
        return bmp
    }
}

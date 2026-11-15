package com.example.ui.components

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Movie
import com.example.data.share.SharePosterRenderer
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class SocialPlatform(
    val id: String,
    val label: String,
    val emoji: String,
    val packageName: String?,
    val badgeColorHex: Long
) {
    WHATSAPP("whatsapp", "WhatsApp", "💬", "com.whatsapp", 0xFF25D366),
    INSTAGRAM("instagram", "Instagram", "📸", "com.instagram.android", 0xFFE1306C),
    TWITTER_X("x_twitter", "X (Twitter)", "🐦", "com.twitter.android", 0xFF1D9BF0),
    TELEGRAM("telegram", "Telegram", "✈️", "org.telegram.messenger", 0xFF229ED9),
    FACEBOOK("facebook", "Facebook", "📘", "com.facebook.katana", 0xFF1877F2),
    SYSTEM_MORE("system_more", "Diğerleri", "🚀", null, 0xFFE9B949)
}

fun buildMovieShareText(
    movie: Movie,
    roomCode: String? = null,
    customHeader: String = "🎬 Bu Akşamın Film Seçimi!"
): String {
    val platformLine = if (movie.platforms.isNotEmpty()) {
        "📺 Platform: ${movie.platforms.joinToString(", ")}"
    } else {
        ""
    }
    val roomLine = if (!roomCode.isNullOrBlank()) "Ortak Oda Kodu: $roomCode • " else ""
    return buildString {
        appendLine("🍿 NE İZLESEK? KARAR ÇARKI 🎡")
        appendLine(customHeader)
        appendLine()
        appendLine("🎥 ${movie.title} (${movie.year})")
        appendLine("🎭 Tür: ${movie.genre}")
        appendLine("⭐️ Puan: ${movie.imdb} • ⏱ ${movie.durationMins} dk")
        if (platformLine.isNotEmpty()) appendLine(platformLine)
        appendLine()
        append("${roomLine}Sen de film çarkını çevir: https://neizlesek.app")
    }.trim()
}

fun buildShareIntentWithOptionalPoster(
    context: Context,
    subject: String,
    shareText: String,
    posterUri: Uri?
): Intent {
    return Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, shareText)
        if (posterUri != null) {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, posterUri)
            clipData = ClipData.newUri(context.contentResolver, subject, posterUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
    }
}

private fun dispatchShareIntent(
    context: Context,
    baseIntent: Intent,
    shareText: String,
    platform: SocialPlatform,
    chooserTitle: String,
    onCopyClipboard: ((String) -> Unit)?
) {
    if (platform == SocialPlatform.INSTAGRAM) {
        onCopyClipboard?.invoke(shareText)
    }

    val targetPkg = platform.packageName
    if (targetPkg != null) {
        val targetedIntent = Intent(baseIntent).apply { setPackage(targetPkg) }
        val resolved = runCatching { targetedIntent.resolveActivity(context.packageManager) }.getOrNull()
        if (resolved != null) {
            runCatching {
                context.startActivity(targetedIntent)
                return
            }
        }
    }

    runCatching {
        context.startActivity(Intent.createChooser(baseIntent, chooserTitle))
    }.onFailure {
        onCopyClipboard?.invoke(shareText)
        Toast.makeText(context, "Paylaşım metni panoya kopyalandı!", Toast.LENGTH_SHORT).show()
    }
}

fun shareMovieToPlatform(
    context: Context,
    movie: Movie,
    platform: SocialPlatform,
    roomCode: String? = null,
    onCopyClipboard: ((String) -> Unit)? = null
) {
    val shareText = buildMovieShareText(movie = movie, roomCode = roomCode)
    val posterUri = SharePosterRenderer.createMoviePosterShareUri(context, movie)
    val baseIntent = buildShareIntentWithOptionalPoster(
        context = context,
        subject = "Ne İzlesek? — ${movie.title}",
        shareText = shareText,
        posterUri = posterUri
    )
    val chooserTitle = if (platform == SocialPlatform.SYSTEM_MORE) {
        "Film Posterini Sosyal Medyada Paylaş"
    } else {
        "${platform.label} ile Poster Paylaş"
    }
    dispatchShareIntent(context, baseIntent, shareText, platform, chooserTitle, onCopyClipboard)
}

fun shareFavoritesListToPlatform(
    context: Context,
    favorites: List<Movie>,
    platform: SocialPlatform,
    onCopyClipboard: ((String) -> Unit)? = null
) {
    val shareText = SharePosterRenderer.buildFavoritesShareText(favorites)
    val posterUri = SharePosterRenderer.createFavoritesPosterShareUri(context, favorites)
    val baseIntent = buildShareIntentWithOptionalPoster(
        context = context,
        subject = "Ne İzlesek? — Favori Film Listem (${favorites.size} Film)",
        shareText = shareText,
        posterUri = posterUri
    )
    val chooserTitle = if (platform == SocialPlatform.SYSTEM_MORE) {
        "Favori Poster Listeni Paylaş"
    } else {
        "${platform.label} ile Favori Listeni Paylaş"
    }
    dispatchShareIntent(context, baseIntent, shareText, platform, chooserTitle, onCopyClipboard)
}

/**
 * Reusable direct Social Media Share Sheet dialog used from Wheel, Home, Profile Favorites,
 * Watched List, Collection Wall, and History screens — attaches a rendered PNG poster image.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialShareDialog(
    movie: Movie,
    roomCode: String? = null,
    onOpenStoryCard: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copied by remember(movie.id) { mutableStateOf(false) }
    val shareText = remember(movie.id, roomCode) { buildMovieShareText(movie, roomCode) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(NeonPurple, PopcornGold, ElectricCyan)),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("social_share_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = PopcornGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Posterle Sosyal Medyada Paylaş",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .background(CinemaSurfaceVariant, CircleShape)
                            .testTag("social_share_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Movie Preview Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CinemaTheme.colors.calloutSurface)
                        .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MoviePosterImage(
                        title = movie.title,
                        posterPath = movie.posterPath,
                        posterUrl = movie.posterUrl,
                        accentColorHex = movie.colorHex,
                        genre = movie.genre,
                        year = movie.year,
                        cornerRadius = 10.dp,
                        showFallbackTitleBadge = false,
                        modifier = Modifier
                            .width(52.dp)
                            .height(76.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = movie.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${movie.year} • ${movie.genre}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = PopcornGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "★ ${movie.imdb} • ${movie.durationMins} dk",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PopcornGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                PosterAttachmentReadyBadge()

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Platform Seç & Poster Görseliyle Paylaş",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                PlatformShareGrid(
                    onPlatformClick = { platform ->
                        shareMovieToPlatform(
                            context = context,
                            movie = movie,
                            platform = platform,
                            roomCode = roomCode,
                            onCopyClipboard = { txt ->
                                clipboardManager.setText(AnnotatedString(txt))
                                copied = true
                                Toast
                                    .makeText(
                                        context,
                                        "Instagram için açıklama panoya kopyalandı, poster gönderiliyor!",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(shareText))
                            copied = true
                            Toast.makeText(context, "Paylaşım metni kopyalandı!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_copy_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (copied) EmeraldGreen else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copied) "Kopyalandı ✓" else "Metni Kopyala",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (copied) EmeraldGreen else TextSecondary
                        )
                    }

                    if (onOpenStoryCard != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenStoryCard()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_open_story_card_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "9:16 Hikaye",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shares the user's entire Favorites list as a multi-poster collage PNG image + summary text.
 */
@Composable
fun FavoritesSocialShareDialog(
    favoriteMovies: List<Movie>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copied by remember(favoriteMovies.size) { mutableStateOf(false) }
    val shareText = remember(favoriteMovies) {
        SharePosterRenderer.buildFavoritesShareText(favoriteMovies)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(NeonPink, PopcornGold, ElectricCyan)),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("favorites_social_share_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Favori Listemi Posterle Paylaş",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .background(CinemaSurfaceVariant, CircleShape)
                            .testTag("favorites_share_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mini Poster Collage Preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CinemaTheme.colors.calloutSurface)
                        .border(1.dp, NeonPink.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "❤️ Seçkin Koleksiyon (${favoriteMovies.size} Film)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopcornGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        favoriteMovies.take(4).forEach { fav ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                MoviePosterImage(
                                    title = fav.title,
                                    posterPath = fav.posterPath,
                                    posterUrl = fav.posterUrl,
                                    accentColorHex = fav.colorHex,
                                    genre = fav.genre,
                                    year = fav.year,
                                    cornerRadius = 8.dp,
                                    showFallbackTitleBadge = false,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(74.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fav.title,
                                    fontSize = 10.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                PosterAttachmentReadyBadge()

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Platform Seç & Poster Kolajını Gönder",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                PlatformShareGrid(
                    onPlatformClick = { platform ->
                        shareFavoritesListToPlatform(
                            context = context,
                            favorites = favoriteMovies,
                            platform = platform,
                            onCopyClipboard = { txt ->
                                clipboardManager.setText(AnnotatedString(txt))
                                copied = true
                                Toast
                                    .makeText(
                                        context,
                                        "Liste açıklaması panoya kopyalandı, poster kolajı hazırlanıyor!",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(shareText))
                        copied = true
                        Toast.makeText(context, "Favori listesi metni kopyalandı!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("favorites_share_copy_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = if (copied) EmeraldGreen else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (copied) "Liste Kopyalandı ✓" else "Favori Listesini Kopyala",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (copied) EmeraldGreen else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterAttachmentReadyBadge() {
    Surface(
        color = ElectricCyan.copy(alpha = 0.14f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("share_poster_ready_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Yüksek kaliteli PNG poster görseli otomatik eklenecek ✓",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElectricCyan
            )
        }
    }
}

@Composable
private fun PlatformShareGrid(
    onPlatformClick: (SocialPlatform) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SocialPlatform.entries.chunked(3).forEach { rowPlatforms ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPlatforms.forEach { platform ->
                    val accent = Color(platform.badgeColorHex)
                    Surface(
                        color = accent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPlatformClick(platform) }
                            .testTag("share_platform_${platform.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = platform.emoji, fontSize = 19.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = platform.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
                repeat(3 - rowPlatforms.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


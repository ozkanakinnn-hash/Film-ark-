package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Movie
import com.example.data.share.SharePosterRenderer
import com.example.ui.theme.CinemaBackground
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StoryShareDialog(
    movie: Movie,
    isVetoedOnce: Boolean,
    roomCode: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    val quotes = listOf(
        "Çark bize saat $currentTime'ta bu filmi dayattı, kaderimiz buymuş! 🍿✨",
        "45 dakikalık 'Sen seç - Hayır sen seç' kavgası bitti. Çark kararını verdi! 🎬",
        "Suçlama yok, vicdan azabı yok. Bu akşamın tek sorumlusu Karar Çarkı! 🎡",
        if (isVetoedOnce) "1 Veto hakkı harcandı, kader kilitlendi! İtiraz yasaktır. 🔒" else "Tek seferde kaderimize boyun eğdik! 🍿"
    )

    var selectedQuoteIndex by remember { mutableStateOf(0) }
    var copiedState by remember { mutableStateOf(false) }

    val currentQuote = quotes[selectedQuoteIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = CinemaBackground,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CinemaBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instagram / WhatsApp Şablonu",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // The 9:16 Flex Story Card Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(NeonPurple, PopcornGold, ElectricCyan)),
                            RoundedCornerShape(20.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        CinemaTheme.colors.heroGradientStart,
                                        CinemaTheme.colors.heroGradientEnd,
                                        CinemaBackground
                                    )
                                )
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge / App branding
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(CinemaTheme.colors.selectedChipFill, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "🍿", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NE İZLESEK? KARAR ÇARKI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = PopcornGold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Clock stamp
                        Text(
                            text = "Kader Saati: $currentTime",
                            fontSize = 12.sp,
                            color = ElectricCyan,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Viral Punchline Quote
                        Text(
                            text = "“$currentQuote”",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Movie Card Showcase Inside Story
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CinemaTheme.colors.calloutSurface)
                                .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                    testTag = "story_movie_poster",
                                    modifier = Modifier
                                        .width(68.dp)
                                        .height(100.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = movie.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopcornGold,
                                        lineHeight = 22.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${movie.year} • ${movie.durationMins} dk",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "★ ${movie.imdb}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PopcornGold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Platforms
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        movie.platforms.take(3).forEach { p ->
                                            Surface(
                                                color = Color(0x44FFFFFF),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = p,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Footer tag
                        Text(
                            text = "Tartışma Yok • Sıfır Suçlama • %100 Çark Kararı",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quote Cycler Button
                OutlinedButton(
                    onClick = {
                        selectedQuoteIndex = (selectedQuoteIndex + 1) % quotes.size
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Sözü Değiştir (${selectedQuoteIndex + 1}/${quotes.size}) 🔄",
                        color = ElectricCyan,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Share Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy text
                    OutlinedButton(
                        onClick = {
                            val shareText = "🍿 Ne İzlesek? Karar Çarkı Sonucu:\n\n“$currentQuote”\n\n🎬 Film: ${movie.title} (${movie.year})\n⏱ Süre: ${movie.durationMins} dk\n⭐️ Puan: ${movie.imdb}\n📺 Platform: ${movie.platforms.joinToString(", ")}\n\nSen de seçim krizini bitirmek için odaya katıl: https://neizlesek.app"
                            clipboardManager.setText(AnnotatedString(shareText))
                            copiedState = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_story_text_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (copiedState) "Kopyalandı!" else "Kopyala", fontSize = 13.sp)
                    }

                    // Share Intent (WhatsApp, Instagram, etc.)
                    Button(
                        onClick = {
                            val shareText = "🍿 Ne İzlesek? Karar Çarkı Sonucu:\n\n“$currentQuote”\n\n🎬 Seçilen Film: ${movie.title} (${movie.year})\n⏱ Süre: ${movie.durationMins} dk | ⭐️ Puan: ${movie.imdb}\n📺 Platform: ${movie.platforms.joinToString(", ")}\n\nKaderimize boyun eğdik! Sen de arkadaşınla odaya katıl: https://neizlesek.app${if (roomCode != null) "/room/$roomCode" else ""}"
                            val posterUri = SharePosterRenderer.createMoviePosterShareUri(context, movie)
                            val intent = buildShareIntentWithOptionalPoster(
                                context = context,
                                subject = "Ne İzlesek? Akşamın Kararı Çıktı!",
                                shareText = shareText,
                                posterUri = posterUri
                            )
                            context.startActivity(Intent.createChooser(intent, "Hikayende Posterle Paylaş"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_story_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Poster Paylaş 🚀",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

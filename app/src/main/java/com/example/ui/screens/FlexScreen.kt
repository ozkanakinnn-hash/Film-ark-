package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Movie
import com.example.ui.components.MoviePosterImage
import com.example.ui.components.SocialPlatform
import com.example.ui.components.shareMovieToPlatform
import com.example.ui.theme.CinemaBackground
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FlexThemeOption(
    val name: String,
    val borderColors: List<Color>,
    val backgroundColors: List<Color>,
    val accentColor: Color
)

@Composable
private fun rememberFlexThemes(): List<FlexThemeOption> {
    val crimson = NeonPurple
    val gold = PopcornGold
    val teal = ElectricCyan
    val rose = NeonPink
    val emerald = EmeraldGreen
    return remember(crimson, gold, teal, rose, emerald) {
        listOf(
            FlexThemeOption(
                name = "Noir Sinema",
                borderColors = listOf(crimson, gold, teal),
                backgroundColors = listOf(Color(0xFF261219), Color(0xFF151218), Color(0xFF0A0A0E)),
                accentColor = Color(0xFFE9B949)
            ),
            FlexThemeOption(
                name = "Kadife Prömiyer",
                borderColors = listOf(rose, gold),
                backgroundColors = listOf(Color(0xFF3A0E1B), Color(0xFF1F0A12), Color(0xFF0D0609)),
                accentColor = Color(0xFFE45A6F)
            ),
            FlexThemeOption(
                name = "Zümrüt Teal",
                borderColors = listOf(emerald, teal),
                backgroundColors = listOf(Color(0xFF0A2A27), Color(0xFF061918), Color(0xFF030D0C)),
                accentColor = Color(0xFF2EC4B6)
            ),
            FlexThemeOption(
                name = "Altın Gala",
                borderColors = listOf(Color(0xFFFFDF00), Color(0xFFD97706)),
                backgroundColors = listOf(Color(0xFF332005), Color(0xFF1F1202), Color(0xFF0D0701)),
                accentColor = Color(0xFFFFD700)
            )
        )
    }
}

@Composable
fun FlexScreen(
    movie: Movie,
    isVetoedOnce: Boolean = false,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    // Themes & Quotes
    val flexThemes = rememberFlexThemes()
    var selectedThemeIndex by remember { mutableIntStateOf(0) }
    var selectedQuoteIndex by remember { mutableIntStateOf(0) }
    var isCopied by remember { mutableStateOf(false) }

    val currentTheme = flexThemes[selectedThemeIndex]

    val quotes = listOf(
        "Çark bize saat $currentTime'ta bu filmi dayattı, kaderimiz buymuş! 🍿✨",
        "45 dakikalık 'Sen seç - Hayır sen seç' kavgası bitti. Çark noktayı koydu! 🎬",
        "Suçlama yok, vicdan azabı yok. Bu akşamın tek sorumlusu Karar Çarkı! 🎡",
        if (isVetoedOnce) "1 Veto hakkı harcandı, kader kilitlendi! İtiraz yasaktır. 🔒" else "Kaderimize tek seferde boyun eğdik! 🍿"
    )

    val currentQuote = quotes[selectedQuoteIndex]

    // Formatted viral message
    val viralShareText = """
🍿 NE İZLESEK? KARAR ÇARKI 🎡

“$currentQuote”

🎬 Film: ${movie.title} (${movie.year})
⏱ Süre: ${movie.durationMins} dakika
⭐️ Puan: ${movie.imdb}
📺 Platform: ${movie.platforms.joinToString(", ")}

Seçim krizine son ver: Ne İzlesek? ile çarkı sen de çevir!
    """.trimIndent()

    // Intent dispatchers
    fun shareViaWhatsApp() {
        try {
            val uri = Uri.parse("whatsapp://send?text=" + Uri.encode(viralShareText))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, viralShareText)
            }, "WhatsApp ile Paylaş")
            context.startActivity(chooser)
        }
    }

    fun shareViaInstagram() {
        clipboardManager.setText(AnnotatedString(viralShareText))
        Toast.makeText(context, "Hikaye metni panoya kopyalandı! Instagram açılıyor...", Toast.LENGTH_SHORT).show()
        try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.instagram.android")
            if (intent != null) {
                context.startActivity(intent)
            } else {
                val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, viralShareText)
                }, "Instagram'da Paylaş")
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, viralShareText)
            }, "Paylaş")
            context.startActivity(chooser)
        }
    }

    fun shareViaTwitter() {
        try {
            val tweetUrl = "https://twitter.com/intent/tweet?text=" + Uri.encode(viralShareText)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, viralShareText)
            }, "X / Twitter'da Paylaş")
            context.startActivity(chooser)
        }
    }

    fun shareGeneric() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Ne İzlesek? Karar Çıktı!")
            putExtra(Intent.EXTRA_TEXT, viralShareText)
        }
        context.startActivity(Intent.createChooser(intent, "Kaderi Hikayende Paylaş"))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(CinemaSurface, CircleShape)
                        .testTag("flex_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Flex / Hikaye Ekranı",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = NeonPurple,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "9:16 Şablon 🍿",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Sosyal medyada paylaş, kaderinize herkes şahit olsun",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // THE AESTHETIC 9:16 STORY CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        2.5.dp,
                        Brush.linearGradient(currentTheme.borderColors),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("flex_story_card"),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(currentTheme.backgroundColors))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top App Brand Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .border(1.dp, currentTheme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = "🍿", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NE İZLESEK? KARAR ÇARKI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = currentTheme.accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clock stamp
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kader Saati: $currentTime",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Punchline Quote
                    Text(
                        text = "“$currentQuote”",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 23.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // MOVIE SHOWCASE (Afiş, İsim, Süre, Puan, Platform)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x55000000))
                            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. AFİŞ (POSTER)
                            MoviePosterImage(
                                title = movie.title,
                                posterPath = movie.posterPath,
                                posterUrl = movie.posterUrl,
                                accentColorHex = movie.colorHex,
                                genre = movie.genre,
                                year = movie.year,
                                cornerRadius = 12.dp,
                                testTag = "flex_movie_poster",
                                modifier = Modifier
                                    .width(84.dp)
                                    .height(120.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            // 2. İSİM, SÜRE VE PUAN DETAYLARI
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = movie.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = currentTheme.accentColor,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // SÜRE VE YIL
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "⏱ ${movie.durationMins} dk",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${movie.year}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // PUAN (IMDb)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = PopcornGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "★ ${movie.imdb}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PopcornGold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // PLATFORMLAR
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    movie.platforms.take(3).forEach { platform ->
                                        Surface(
                                            color = Color(0x33FFFFFF),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = platform,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Footer Sticker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "✨ Tartışma Yok • Sıfır Suçlama • %100 Kader ✨",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CUSTOMIZATION: QUOTE CYCLER & THEME SELECTOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Change Quote
                OutlinedButton(
                    onClick = { selectedQuoteIndex = (selectedQuoteIndex + 1) % quotes.size },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Slogan (${selectedQuoteIndex + 1}/${quotes.size})",
                        color = ElectricCyan,
                        fontSize = 12.sp
                    )
                }

                // Change Theme
                OutlinedButton(
                    onClick = { selectedThemeIndex = (selectedThemeIndex + 1) % flexThemes.size },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentTheme.name,
                        color = NeonPurple,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SOCIAL MEDIA INTENT ACTION BUTTONS
            Text(
                text = "Sosyal Medyada Paylaş 🚀",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. WhatsApp Intent Button
            Button(
                onClick = { shareViaWhatsApp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("whatsapp_intent_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "WhatsApp Durumunda / Arkadaşına Gönder 💬",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Instagram Story Intent Button
            Button(
                onClick = { shareViaInstagram() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("instagram_intent_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Instagram Hikayende Paylaş 📸",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. X (Twitter) & System Share Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // X (Twitter) Button
                OutlinedButton(
                    onClick = { shareViaTwitter() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("twitter_intent_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "X (Twitter) 🐦", fontSize = 13.sp, color = TextPrimary)
                }

                // General Share Chooser Button
                Button(
                    onClick = { shareGeneric() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("generic_share_intent_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Diğer...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3b. Telegram & Facebook Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        shareMovieToPlatform(
                            context = context,
                            movie = movie,
                            platform = SocialPlatform.TELEGRAM,
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("telegram_intent_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Telegram ✈️", fontSize = 13.sp, color = TextPrimary)
                }

                OutlinedButton(
                    onClick = {
                        shareMovieToPlatform(
                            context = context,
                            movie = movie,
                            platform = SocialPlatform.FACEBOOK,
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("facebook_intent_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Facebook 📘", fontSize = 13.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Copy Rich Text
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(viralShareText))
                    isCopied = true
                    Toast.makeText(context, "Paylaşım metni panoya kopyalandı! ✓", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_flex_text_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCopied) "Metin Kopyalandı! ✓" else "Paylaşım Metnini Kopyala",
                    fontSize = 13.sp,
                    color = if (isCopied) EmeraldGreen else TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.data.repository.SavedDecision
import com.example.data.repository.TrendingCinemaUiState
import com.example.ui.components.NewReleasesBannerStrip
import com.example.ui.components.SocialShareDialog
import com.example.ui.components.WeeklyTrendsSection
import com.example.ui.theme.CinemaBackground
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    savedDecisions: List<SavedDecision>,
    watchedCount: Int = 0,
    favoriteCount: Int = 0,
    customWheelListsCount: Int = 3,
    isDarkTheme: Boolean = true,
    dailyNewReleases: List<Movie> = MovieRepository.dailyUpdatedMovies.value,
    dailyFeedDateLabel: String = MovieRepository.dailyFeedLastUpdated.value,
    onRefreshNewReleases: () -> Unit = {},
    onNewReleaseMovieClick: (Movie) -> Unit = {},
    weeklyTrendsState: TrendingCinemaUiState = TrendingCinemaUiState.Loading,
    onRefreshTrends: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onSoloClick: () -> Unit,
    onRoomClick: () -> Unit,
    onCompareClick: () -> Unit = {},
    onCustomListsClick: () -> Unit = {},
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onSavedDecisionClick: (Movie) -> Unit = {}
) {
    val totalMinutesSaved = savedDecisions.sumOf { it.minutesSaved }
    var trendShareTarget by remember { mutableStateOf<Movie?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBackground)
            .padding(horizontal = 20.dp)
            .testTag("home_screen_list"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))

            // App Bar / Top Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CinemaTheme.colors.selectedChipFill,
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🍿", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NE İZLESEK?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = PopcornGold
                        )
                        Text(
                            text = "Karar Çarkı • Sıfır Suçlama",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .background(CinemaSurface, CircleShape)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Açık Temaya Geç" else "Koyu Temaya Geç",
                            tint = PopcornGold
                        )
                    }

                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .background(CinemaSurface, CircleShape)
                            .testTag("profile_icon_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (watchedCount > 0) {
                                    Badge(
                                        containerColor = ElectricCyan,
                                        contentColor = CinemaTheme.colors.onAccentTeal
                                    ) {
                                        Text(
                                            text = "$watchedCount",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profilim ve İzlediklerim",
                                tint = ElectricCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onHistoryClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .background(CinemaSurface, CircleShape)
                            .testTag("history_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Geçmişte Ne Seçtik?",
                            tint = PopcornGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Top Scrolling "Yeni Çıkanlar" Daily Poster Strip
            if (dailyNewReleases.isNotEmpty()) {
                NewReleasesBannerStrip(
                    movies = dailyNewReleases,
                    lastUpdatedDate = dailyFeedDateLabel,
                    onRefreshClick = onRefreshNewReleases,
                    onMovieClick = onNewReleaseMovieClick
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Psychological Value Hook Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                CinemaTheme.colors.heroGradientStart,
                                CinemaTheme.colors.heroGradientEnd
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(NeonPurple, PopcornGold)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚡", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SEÇİM YORGUNLUĞUNA SON",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "45 dakikalık 'Ne izlesek?' tartışmasını 10 saniyede bitirin.",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Koltukta kavga etmeyin, kararı kadere ve çarka bırakın. Beğenilmezse kimse suçlu değil!",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .background(CinemaSurface.copy(alpha = 0.72f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kurtarılan Zaman: ",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "$totalMinutesSaved+ Dakika 🎉",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Genre Statistics Quick Entry Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onStatsClick() }
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(ElectricCyan.copy(alpha = 0.65f), PopcornGold.copy(alpha = 0.65f))),
                        RoundedCornerShape(18.dp)
                    )
                    .testTag("home_stats_entry_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(listOf(ElectricCyan, NeonPink)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = "Tür istatistik paneli",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Geçmiş Analiz Dashboard 📊",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${savedDecisions.size} DB kaydı • En sık seçilen türler & filmler ($favoriteCount ❤️)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PopcornGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            WeeklyTrendsSection(
                uiState = weeklyTrendsState,
                onRefreshClick = onRefreshTrends,
                onShareMovieClick = { movie -> trendShareTarget = movie }
            )

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Nasıl Karar Verelim?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. HERO ACTION BUTTON: TEK BAŞINA KARAR VER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onSoloClick() }
                    .border(1.dp, CinemaBorder, RoundedCornerShape(24.dp))
                    .testTag("solo_mode_button"),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(listOf(CinemaTheme.colors.filmTeal, ElectricCyan)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tek Başına Karar Ver",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tek tıkla modunu seç, çark bu akşamki filmini anında belirlesin.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. HERO ACTION BUTTON: BİRİYLE KARAR VER (ORTAK ODA - PLG VIRAL LOOP)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onRoomClick() }
                    .border(
                        2.dp,
                        Brush.horizontalGradient(listOf(NeonPurple, PopcornGold)),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("room_mode_button"),
                colors = CardDefaults.cardColors(containerColor = CinemaSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    CinemaTheme.colors.heroGradientStart,
                                    CinemaTheme.colors.heroGradientEnd
                                )
                            )
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(listOf(NeonPurple, NeonPink)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Biriyle Karar Ver",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PopcornGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = NeonPink,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "ORTAK ODA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Eşin veya arkadaşınla bağlan. WhatsApp linkiyle 4 haneli kod paylaş, ortak zevkleriniz dönsün!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PopcornGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. HERO ACTION BUTTON: FILM KARSILASTIR (TINDER TARZI VS MODU)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onCompareClick() }
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(ElectricCyan, NeonPink)),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("compare_mode_button"),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(listOf(ElectricCyan, NeonPurple)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Film Karşılaştır",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = CinemaTheme.colors.selectedChipFill,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "TİNDER VS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonPink,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "İki filmi yan yana getir; puanı ve süreyi kıyasla, kaydırarak gecenin şampiyonunu seç!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. HERO ACTION BUTTON: OZEL CARK LISTELERI (CUSTOM CATEGORIES)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onCustomListsClick() }
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(PopcornGold, EmeraldGreen)),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("custom_lists_mode_button"),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(listOf(PopcornGold, EmeraldGreen)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Özel çark kategorileri",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Özel Çark Listeleri",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PopcornGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = EmeraldGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$customWheelListsCount LİSTE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Korku Gecesi, Aile Dostu, Klasikler veya tamamen kendi belirlediğin filmlerle özel çarkını oluştur!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PopcornGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recent Saved Evenings Header ("Geçmişte Ne Seçtik?")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Geçmişte Ne Seçtik?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${savedDecisions.size} film yerel veritabanında kayıtlı",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = CinemaTheme.colors.selectedChipFill,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onHistoryClick() }
                        .semantics(mergeDescendants = true) {}
                        .testTag("home_open_history_button")
                ) {
                    Text(
                        text = "Tümünü Gör →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopcornGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // List of past saved movies
        items(savedDecisions) { decision ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSavedDecisionClick(decision.movie) }
                    .testTag("home_saved_decision_${decision.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(CinemaTheme.colors.selectedChipFill, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalMovies,
                            contentDescription = null,
                            tint = PopcornGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = decision.movie.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${decision.dateText} • ${decision.mode}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        color = Color(0x3310B981),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+${decision.minutesSaved} dk",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { trendShareTarget = decision.movie },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .background(CinemaSurfaceVariant, CircleShape)
                            .testTag("home_decision_share_${decision.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Seçilen filmi posterle paylaş",
                            tint = PopcornGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    trendShareTarget?.let { movie ->
        SocialShareDialog(
            movie = movie,
            onOpenStoryCard = { onSavedDecisionClick(movie) },
            onDismiss = { trendShareTarget = null }
        )
    }
}

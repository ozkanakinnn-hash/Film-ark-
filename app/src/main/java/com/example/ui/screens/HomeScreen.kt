package com.example.ui.screens

import com.example.ui.components.MovieDetailSheet
import com.example.ui.components.MovieSearchDialog
import androidx.compose.ui.semantics.contentDescription
import com.example.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.components.NewReleasesBannerStrip
import com.example.ui.components.SocialShareDialog
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
    watchLaterIds: List<String> = emptyList(),
    onToggleWatchLater: (Movie) -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    personalPicks: List<Movie> = emptyList(),
    ticketPosters: List<Movie> = emptyList(),
    smartWheelLists: List<com.example.data.model.CustomWheelList> = emptyList(),
    popularMovieBackdrops: List<String> = com.example.data.repository.HighlightsRepository.popularMovieBackdrops().take(1),
    popularSeriesBackdrops: List<String> = com.example.data.repository.HighlightsRepository.popularSeriesBackdrops().take(1),
    onSpinSmartList: (String) -> Unit = {},
    onSoloClick: () -> Unit,
    onCompareClick: () -> Unit = {},
    onCustomListsClick: () -> Unit = {},
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onSavedDecisionClick: (Movie) -> Unit = {}
) {
    var shareTarget by remember { mutableStateOf<Movie?>(null) }

    var detailMovie by remember { mutableStateOf<Movie?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    if (showSearch) {
        MovieSearchDialog(
            movies = com.example.data.repository.MovieRepository.allMovies,
            onMovieClick = { movie -> detailMovie = movie },
            onDismiss = { showSearch = false }
        )
    }
    detailMovie?.let { movie ->
        MovieDetailSheet(
            movie = movie,
            onDismiss = { detailMovie = null },
            onAddToWheel = {
                detailMovie = null
                onNewReleaseMovieClick(movie)
            },
            isInWatchLater = watchLaterIds.contains(movie.id),
            onToggleWatchLater = { onToggleWatchLater(movie) }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                ) {
                    // Uygulama ikonuyla aynı logo karosu (marka tutarlılığı için her iki temada da noir)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFF1B1822), Color(0xFF0B0A0F))),
                                RoundedCornerShape(13.dp)
                            )
                            .border(1.dp, Color(0x40E9B949), RoundedCornerShape(13.dp))
                            .semantics { contentDescription = "Ne İzlesek? logosu" }
                            .testTag("home_brand_logo")
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.brand_mark),
                            contentDescription = null,
                            modifier = Modifier.height(34.dp)
                        )
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
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { showSearch = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .background(CinemaSurface, CircleShape)
                            .testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Film ara",
                            tint = PopcornGold
                        )
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .background(CinemaSurface, CircleShape)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = PopcornGold
                        )
                    }

                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
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

                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Top Scrolling "Yeni Çıkanlar" Daily Poster Strip
            if (dailyNewReleases.isNotEmpty()) {
                NewReleasesBannerStrip(
                    movies = dailyNewReleases,
                    lastUpdatedDate = dailyFeedDateLabel,
                    onRefreshClick = onRefreshNewReleases,
                    // Afişe dokununca önce film kartı açılır; çarka geçiş karttaki "Çarkta Aç" ile
                    onMovieClick = { movie -> detailMovie = movie }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            if (personalPicks.isNotEmpty()) {
                com.example.ui.components.PersonalPicksStrip(
                    picks = personalPicks,
                    onMovieClick = { movie -> detailMovie = movie }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Sinema bileti: solda Düello (Tinder), sağda koçanda Karar Çarkı
            com.example.ui.components.HomeTicketHero(
                duelPosters = ticketPosters.take(3),
                wheelPosters = ticketPosters.drop(3).take(8),
                onDuelClick = onCompareClick,
                onWheelClick = onSoloClick
            )

            // Hazır Çarklar: iki eşit klaket kutusu + "Bunu Niye İzledim?" kartı
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Hazır Çarklar",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            )
            Text(
                text = "Haftalık katalogdan otomatik oluşur • dokun ve çevir",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("home_smart_wheels_row")
            ) {
                com.example.ui.components.ClapperWheelCard(
                    title = "Popüler Filmler",
                    subtitle = "En çok izlenen",
                    backdropPaths = popularMovieBackdrops,
                    accent = Color(0xFFE9B949),
                    redButton = false,
                    onClick = { onSpinSmartList(com.example.data.repository.SmartWheelLists.POPULAR_MOVIES) },
                    testTag = "home_smart_wheel_${com.example.data.repository.SmartWheelLists.POPULAR_MOVIES}",
                    modifier = Modifier.weight(1f)
                )
                com.example.ui.components.ClapperWheelCard(
                    title = "Popüler Diziler",
                    subtitle = "Gündemdekiler",
                    backdropPaths = popularSeriesBackdrops,
                    accent = Color(0xFFD63447),
                    redButton = true,
                    onClick = { onSpinSmartList(com.example.data.repository.SmartWheelLists.POPULAR_SERIES) },
                    testTag = "home_smart_wheel_${com.example.data.repository.SmartWheelLists.POPULAR_SERIES}",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.components.WorstWheelCard(
                onClick = { onSpinSmartList(com.example.data.repository.SmartWheelLists.WORST) }
            )

            Spacer(modifier = Modifier.height(22.dp))


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

            // 1. HERO ACTION BUTTON: KARAR ÇARKINI ÇEVİR
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
                            text = "Karar Çarkını Çevir",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Modunu ve platformunu seç, çark bu akşamki filmini anında belirlesin.",
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

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    shareTarget?.let { movie ->
        SocialShareDialog(
            movie = movie,
            onOpenStoryCard = { onSavedDecisionClick(movie) },
            onDismiss = { shareTarget = null }
        )
    }
}

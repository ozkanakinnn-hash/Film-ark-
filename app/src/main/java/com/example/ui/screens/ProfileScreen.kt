package com.example.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchedMovieEntity
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.data.repository.SavedDecision
import com.example.ui.components.FavoritesSocialShareDialog
import com.example.ui.components.GenreStatisticsPanel
import com.example.ui.components.MovieCollectionWallCard
import com.example.ui.components.MoviePosterImage
import com.example.ui.components.SocialShareDialog
import com.example.ui.components.WatchedStatsChartCard
import com.example.widget.MovieWheelAppWidgetProvider
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
import com.example.ui.theme.VetoRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val QuickNoteChips = listOf(
    "🔥 Efsane",
    "😂 Çok Güldük",
    "🤯 Ters Köşe",
    "❤️ Favorim"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    watchedMovies: List<WatchedMovieEntity>,
    savedDecisions: List<SavedDecision>,
    favoriteMovieIds: Set<String> = emptySet(),
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onToggleFavorite: (String, String) -> Unit = { _, _ -> },
    onToggleRewatch: (String) -> Unit = {},
    onSaveNote: (String, String) -> Unit = { _, _ -> },
    onRemoveWatched: (String) -> Unit,
    onMovieFlexClick: (Movie) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val totalWatchedMins = watchedMovies.sumOf { it.durationMins }
    val totalHours = totalWatchedMins / 60
    val remainingMins = totalWatchedMins % 60
    val totalSavedMins = savedDecisions.sumOf { it.minutesSaved }
    var shareTargetMovie by remember { mutableStateOf<Movie?>(null) }
    var showFavoritesListShareDialog by remember { mutableStateOf(false) }

    val favoriteMoviesList = remember(favoriteMovieIds, watchedMovies) {
        favoriteMovieIds.mapNotNull { favId ->
            MovieRepository.getMovieById(favId) ?: watchedMovies.find { it.movieId == favId }?.let { w ->
                Movie(
                    id = w.movieId,
                    title = w.title,
                    originalTitle = w.title,
                    year = w.year,
                    durationMins = w.durationMins,
                    imdb = w.imdb,
                    moodTag = "favorite",
                    platforms = emptyList(),
                    genre = w.genre,
                    logline = w.personalNote.ifBlank { "${w.genre} • ${w.year}" },
                    whyWatch = "Favorilerimden öneri",
                    posterPath = w.posterPath ?: "",
                    colorHex = 0xFFDB2777
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("profile_screen_list"),
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
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .background(CinemaSurface, CircleShape)
                        .testTag("profile_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Profilim",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Not ekle, tekrar izlemek istediklerini çarka aç 🎬",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Zero-Login User Profile Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                CinemaTheme.colors.heroGradientStart,
                                CinemaTheme.colors.heroGradientEnd
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(NeonPurple, PopcornGold)),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(
                                    Brush.linearGradient(listOf(NeonPurple, ElectricCyan)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Kullanıcı profili",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sinema Gurmesi 🍿",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0x3306B6D4),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Sıfır Kayıt • Cihaz İçi Profil",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatTile(
                            value = "${watchedMovies.size}",
                            label = "İzlenen Film",
                            accent = PopcornGold,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            value = if (totalHours > 0) "${totalHours}s ${remainingMins}dk" else "${totalWatchedMins} dk",
                            label = "İzleme Süresi",
                            accent = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            value = "$totalSavedMins dk",
                            label = "Kurtarılan Zaman",
                            accent = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Theme Appearance Toggle (Açık / Koyu Sinema)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleTheme() }
                    .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
                    .testTag("profile_theme_toggle_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDarkTheme) "🌙 Koyu Tema: Noir Sinema" else "☀️ Açık Tema: Matine Fildişi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sinematik renk paletini tek dokunuşla değiştir",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        color = PopcornGold.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isDarkTheme) "Açığa Geç" else "Koyuya Geç",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Home Screen Widget Quick-Pin Card
            val profileContext = LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        val manager = AppWidgetManager.getInstance(profileContext)
                        val provider = ComponentName(profileContext, MovieWheelAppWidgetProvider::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager?.isRequestPinAppWidgetSupported == true) {
                            manager.requestPinAppWidget(provider, null, null)
                        } else {
                            Toast.makeText(
                                profileContext,
                                "Ana ekrana basılı tutup Widget'lar menüsünden 'Ne İzlesek? Çarkı'nı ekleyebilirsin!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .testTag("profile_pin_widget_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Ana ekran widgetı",
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ana Ekran Widget'ı Ekle",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Uygulamayı açmadan çark çevir & günün filmini gör",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Surface(
                        color = ElectricCyan.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Ekle",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Wheel-Spun vs. Liked Genre Distribution Panel (Charts)
            GenreStatisticsPanel(
                savedDecisions = savedDecisions,
                favoriteMovieIds = favoriteMovieIds,
                watchedMovies = watchedMovies
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Visual Genre & Watch-Time Summary Chart
            WatchedStatsChartCard(watchedMovies = watchedMovies)

            Spacer(modifier = Modifier.height(18.dp))

            // Film Collection Wall (3-column poster grid)
            MovieCollectionWallCard(
                watchedMovies = watchedMovies,
                favoriteMovieIds = favoriteMovieIds,
                onShareMovie = { shareTargetMovie = it }
            )

            if (favoriteMoviesList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_favorites_section")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "❤️ Favorilerim",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "En beğendiklerini doğrudan sosyal medyada paylaş",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = PopcornGold,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showFavoritesListShareDialog = true }
                                    .testTag("share_favorites_list_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Favori listesini posterle paylaş",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Listeyi Paylaş",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            Surface(
                                color = NeonPink.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${favoriteMoviesList.size} Favori",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonPink,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    favoriteMoviesList.forEach { favMovie ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, NeonPink.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .testTag("favorite_movie_card_${favMovie.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MoviePosterImage(
                                    title = favMovie.title,
                                    posterPath = favMovie.posterPath,
                                    posterUrl = favMovie.posterUrl,
                                    accentColorHex = favMovie.colorHex,
                                    cornerRadius = 10.dp,
                                    showFallbackTitleBadge = false,
                                    modifier = Modifier
                                        .width(42.dp)
                                        .height(60.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = favMovie.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "★ ${favMovie.imdb} • ${favMovie.year} • ${favMovie.genre}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = PopcornGold,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { shareTargetMovie = favMovie }
                                        .testTag("favorite_share_button_${favMovie.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Favoriyi paylaş",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Paylaş",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onToggleFavorite(favMovie.id, favMovie.title) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(44.dp)
                                        .testTag("favorite_remove_button_${favMovie.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Favorilerden çıkar",
                                        tint = NeonPink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "İzlediğim Filmler",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Varsayılan olarak çark dışıdır; 'Tekrar İzle' ile çarka açabilirsin",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Surface(
                    color = Color(0x33F59E0B),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${watchedMovies.size} Film",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopcornGold,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("profile_watched_count")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (watchedMovies.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CinemaBorder, RoundedCornerShape(18.dp))
                        .testTag("profile_watched_empty"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎞️", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Henüz izlediğin bir film yok",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Çarkta çıkan filmi izledikten sonra 'Bu Filmi İzledim' seçeneğine dokun; burada not alıp tekrar izleme işareti koyabilirsin!",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        } else {
            items(watchedMovies, key = { it.movieId }) { watched ->
                val isFav = watched.isFavorite || favoriteMovieIds.contains(watched.movieId)
                WatchedMovieCard(
                    watched = watched,
                    isFavorite = isFav,
                    onToggleFavorite = { onToggleFavorite(watched.movieId, watched.title) },
                    onToggleRewatch = { onToggleRewatch(watched.movieId) },
                    onSaveNote = { newNote -> onSaveNote(watched.movieId, newNote) },
                    onRemoveWatched = { onRemoveWatched(watched.movieId) },
                    onShareMovie = { shareTargetMovie = it },
                    onMovieFlexClick = onMovieFlexClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    shareTargetMovie?.let { movie ->
        SocialShareDialog(
            movie = movie,
            onOpenStoryCard = { onMovieFlexClick(movie) },
            onDismiss = { shareTargetMovie = null }
        )
    }

    if (showFavoritesListShareDialog && favoriteMoviesList.isNotEmpty()) {
        FavoritesSocialShareDialog(
            favoriteMovies = favoriteMoviesList,
            onDismiss = { showFavoritesListShareDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WatchedMovieCard(
    watched: WatchedMovieEntity,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleRewatch: () -> Unit,
    onSaveNote: (String) -> Unit,
    onRemoveWatched: () -> Unit,
    onShareMovie: (Movie) -> Unit,
    onMovieFlexClick: (Movie) -> Unit
) {
    val fullMovie = remember(watched.movieId) { MovieRepository.getMovieById(watched.movieId) }
    val shareableMovie = remember(watched, fullMovie) {
        fullMovie ?: Movie(
            id = watched.movieId,
            title = watched.title,
            originalTitle = watched.title,
            year = watched.year,
            durationMins = watched.durationMins,
            imdb = watched.imdb,
            moodTag = "watched",
            platforms = emptyList(),
            genre = watched.genre,
            logline = watched.personalNote.ifBlank { "${watched.genre} • ${watched.year}" },
            whyWatch = "Koleksiyonumdan öneri",
            posterPath = watched.posterPath ?: "",
            colorHex = 0xFF8B5CF6
        )
    }
    val formattedDate = remember(watched.watchedAtMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("tr-TR"))
            .format(Date(watched.watchedAtMillis))
    }

    var isEditingNote by remember(watched.movieId) { mutableStateOf(false) }
    var draftNote by remember(watched.movieId, watched.personalNote) {
        mutableStateOf(watched.personalNote)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = if (watched.wantToRewatch) 1.5.dp else 1.dp,
                color = if (watched.wantToRewatch) ElectricCyan else CinemaBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("watched_movie_card_${watched.movieId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Movie Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoviePosterImage(
                    title = watched.title,
                    posterPath = fullMovie?.posterPath,
                    posterUrl = fullMovie?.posterUrl,
                    accentColorHex = fullMovie?.colorHex ?: 0xFF8B5CF6,
                    cornerRadius = 10.dp,
                    showFallbackTitleBadge = false,
                    testTag = "watched_movie_poster_${watched.movieId}",
                    modifier = Modifier
                        .width(48.dp)
                        .height(68.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                    Text(
                        text = watched.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${watched.year} • ${watched.durationMins} dk • ${watched.genre}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = if (watched.wantToRewatch) Color(0x3306B6D4) else Color(0x3310B981),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (watched.wantToRewatch) Icons.Default.Replay else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (watched.wantToRewatch) ElectricCyan else EmeraldGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (watched.wantToRewatch) "Tekrar İzlenecek" else "Çark Dışı",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (watched.wantToRewatch) ElectricCyan else EmeraldGreen,
                            modifier = Modifier.testTag("watched_status_badge_${watched.movieId}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // IMDb + Date + Flex/Delete Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = PopcornGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${watched.imdb}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopcornGold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "İzlenme: $formattedDate",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(44.dp)
                            .testTag("favorite_toggle_${watched.movieId}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                            tint = if (isFavorite) NeonPink else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Surface(
                        color = PopcornGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PopcornGold.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onShareMovie(shareableMovie) }
                            .testTag("watched_share_button_${watched.movieId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Sosyal medyada paylaş",
                                tint = PopcornGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paylaş",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PopcornGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    if (fullMovie != null) {
                        Surface(
                            color = CinemaTheme.colors.selectedChipFill,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onMovieFlexClick(fullMovie) }
                                .testTag("watched_flex_button_${watched.movieId}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Flex",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PopcornGold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onRemoveWatched,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(44.dp)
                            .testTag("remove_watched_button_${watched.movieId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "İzlenenlerden çıkar",
                            tint = VetoRed,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Saved Personal Note Preview
            if (watched.personalNote.isNotBlank() && !isEditingNote) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CinemaTheme.colors.calloutSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, NeonPurple.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("movie_note_box_${watched.movieId}")
                ) {
                    Column {
                        Text(
                            text = "📝 Kişisel Notun",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = watched.personalNote,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp,
                            modifier = Modifier.testTag("movie_note_text_${watched.movieId}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Personalization Actions: [Tekrar İzle] & [Not Al / Düzenle]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = if (watched.wantToRewatch) Color(0x3306B6D4) else CinemaSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (watched.wantToRewatch) ElectricCyan else CinemaBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleRewatch() }
                        .testTag("rewatch_toggle_${watched.movieId}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Tekrar izle",
                            tint = if (watched.wantToRewatch) ElectricCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (watched.wantToRewatch) "Çarka Açık 🔁" else "Tekrar İzle",
                            fontSize = 12.sp,
                            fontWeight = if (watched.wantToRewatch) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (watched.wantToRewatch) ElectricCyan else TextPrimary,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    color = if (isEditingNote || watched.personalNote.isNotBlank()) Color(0x33F59E0B) else CinemaSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isEditingNote || watched.personalNote.isNotBlank()) PopcornGold else CinemaBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            draftNote = watched.personalNote
                            isEditingNote = !isEditingNote
                        }
                        .testTag("note_button_${watched.movieId}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Not al",
                            tint = if (isEditingNote || watched.personalNote.isNotBlank()) PopcornGold else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (watched.personalNote.isBlank()) "Not Al" else "Notu Düzenle",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isEditingNote || watched.personalNote.isNotBlank()) PopcornGold else TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Expandable Inline Note Editor
            AnimatedVisibility(
                visible = isEditingNote,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(CinemaTheme.colors.calloutSurface, RoundedCornerShape(14.dp))
                        .border(1.dp, PopcornGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Film Deneyimini Kişiselleştir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopcornGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = draftNote,
                        onValueChange = { if (it.length <= 200) draftNote = it },
                        placeholder = {
                            Text(
                                text = "Nasıl buldun? Kiminle izledin, aklında ne kaldı?",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PopcornGold,
                            unfocusedBorderColor = CinemaBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = PopcornGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_${watched.movieId}")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick 1-tap reaction chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickNoteChips.forEach { tag ->
                            Surface(
                                color = CinemaSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val updated = if (draftNote.isBlank()) tag else "$draftNote • $tag"
                                        if (updated.length <= 200) {
                                            draftNote = updated
                                        }
                                    }
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                draftNote = watched.personalNote
                                isEditingNote = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text(
                                text = "İptal",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                onSaveNote(draftNote)
                                isEditingNote = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_note_button_${watched.movieId}")
                        ) {
                            Text(
                                text = "Kaydet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatTile(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaSurfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

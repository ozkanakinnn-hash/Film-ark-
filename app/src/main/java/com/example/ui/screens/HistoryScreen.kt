package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchedMovieEntity
import com.example.data.model.HistoryDashboardSummary
import com.example.data.model.Movie
import com.example.data.model.analyzeDecisionHistory
import com.example.data.repository.SavedDecision
import com.example.ui.components.GenreStatisticsPanel
import com.example.ui.components.MoviePosterImage
import com.example.ui.components.SocialShareDialog
import com.example.ui.theme.CinemaBackground
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PopcornGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private enum class HistoryModeFilter(val id: String, val label: String, val tag: String) {
    ALL("all", "Tümü", "history_filter_all"),
    SOLO("solo", "Tek Başına", "history_filter_solo"),
    ROOM("room", "Ortak Oda", "history_filter_room"),
    CUSTOM("custom", "Özel Çark", "history_filter_custom"),
    COMPARE("compare", "VS Düello", "history_filter_compare")
}

@Composable
fun HistoryScreen(
    savedDecisions: List<SavedDecision>,
    favoriteMovieIds: Set<String> = emptySet(),
    watchedMovieIds: Set<String> = emptySet(),
    watchedMovies: List<WatchedMovieEntity> = emptyList(),
    historyDashboard: HistoryDashboardSummary = analyzeDecisionHistory(savedDecisions),
    onOpenDashboard: () -> Unit = {},
    onMovieFlexClick: (Movie) -> Unit = {},
    onDeleteDecision: (String) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val totalMinutes = savedDecisions.sumOf { it.minutesSaved }
    var shareTargetMovie by remember { mutableStateOf<Movie?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryModeFilter.ALL) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val topGenre = remember(savedDecisions) {
        savedDecisions
            .flatMap { it.movie.genre.split(",", "/", "•").map { part -> part.trim() } }
            .filter { it.isNotEmpty() }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }

    val filteredDecisions = remember(savedDecisions, searchQuery, selectedFilter) {
        val query = searchQuery.trim()
        savedDecisions.filter { decision ->
            val modeMatches = when (selectedFilter) {
                HistoryModeFilter.ALL -> true
                HistoryModeFilter.SOLO -> decision.mode.contains("Tek Başına", ignoreCase = true)
                HistoryModeFilter.ROOM -> decision.mode.contains("Ortak Oda", ignoreCase = true)
                HistoryModeFilter.CUSTOM -> decision.mode.contains("Özel Çark", ignoreCase = true)
                HistoryModeFilter.COMPARE -> decision.mode.contains("Karşılaştır", ignoreCase = true) ||
                    decision.mode.contains("VS", ignoreCase = true)
            }
            val queryMatches = if (query.isEmpty()) {
                true
            } else {
                decision.movie.title.contains(query, ignoreCase = true) ||
                    decision.movie.originalTitle.contains(query, ignoreCase = true) ||
                    decision.movie.genre.contains(query, ignoreCase = true) ||
                    decision.mode.contains(query, ignoreCase = true)
            }
            modeMatches && queryMatches
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("history_screen_list"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .background(CinemaSurface, CircleShape)
                            .testTag("history_back_button")
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
                            text = "Geçmişte Ne Seçtik?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.testTag("history_screen_title")
                        )
                        Text(
                            text = "Çark tarafından seçilen filmler cihazda saklanır 🍿",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                if (savedDecisions.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .background(CinemaSurface, CircleShape)
                            .testTag("history_clear_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Tüm seçim geçmişini temizle",
                            tint = PopcornGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Total Saved Stats Card
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
                    .testTag("history_summary_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ÇARK ARŞİVİ • YEREL VERİTABANI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${savedDecisions.size} Film Seçildi",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = PopcornGold
                            )
                            Text(
                                text = "$totalMinutes dakika karar verme süresi kurtarıldı",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = Color(0x3310B981),
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    if (topGenre != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = CinemaSurface.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.semantics(mergeDescendants = true) {}
                        ) {
                            Text(
                                text = "En Çok Çıkan Tür: $topGenre",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = PopcornGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, PopcornGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .clickable { onOpenDashboard() }
                            .testTag("history_open_dashboard_button")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = "📊 Veritabanı Dashboard: En Sık Seçilen Tür & Filmler →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PopcornGold
                            )
                            val previewGenre = historyDashboard.dominantGenre?.genre
                            val previewMovie = historyDashboard.dominantMovie
                            if (previewGenre != null && previewMovie != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "#1 Tür: $previewGenre • #1 Film: ${previewMovie.movie.title} (${previewMovie.selectionCount}x)",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            GenreStatisticsPanel(
                savedDecisions = savedDecisions,
                favoriteMovieIds = favoriteMovieIds,
                watchedMovies = watchedMovies
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                placeholder = {
                    Text(
                        text = "Film adı, tür veya mod ara...",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = ElectricCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("history_search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Aramayı temizle",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = CinemaBorder,
                    focusedContainerColor = CinemaSurface,
                    unfocusedContainerColor = CinemaSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(HistoryModeFilter.entries) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CinemaTheme.colors.selectedChipFill,
                            selectedLabelColor = PopcornGold,
                            containerColor = CinemaSurface,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(filter.tag)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seçim Kronolojisi (${filteredDecisions.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.testTag("history_chronology_header")
                )
                if (selectedFilter != HistoryModeFilter.ALL || searchQuery.isNotBlank()) {
                    Text(
                        text = "Filtreli görünüm",
                        fontSize = 12.sp,
                        color = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (filteredDecisions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("history_empty_state"),
                    shape = RoundedCornerShape(20.dp),
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
                            text = if (savedDecisions.isEmpty()) {
                                "Henüz çarktan seçilmiş film yok"
                            } else {
                                "Aramanla eşleşen film bulunamadı"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (savedDecisions.isEmpty()) {
                                "Çarkı çevir veya VS düellosu tamamla; seçilen tüm filmler burada saklanacak."
                            } else {
                                "Farklı bir film adı, tür veya mod filtresi deneyebilirsin."
                            },
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        if (savedDecisions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedFilter = HistoryModeFilter.ALL
                                },
                                modifier = Modifier.testTag("history_reset_filter_button")
                            ) {
                                Text("Filtreyi Sıfırla", color = ElectricCyan)
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredDecisions, key = { it.id }) { decision ->
                val isFavorite = favoriteMovieIds.contains(decision.movie.id)
                val isWatched = watchedMovieIds.contains(decision.movie.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, CinemaBorder.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .testTag("history_decision_card_${decision.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            MoviePosterImage(
                                title = decision.movie.title,
                                posterPath = decision.movie.posterPath,
                                posterUrl = decision.movie.posterUrl,
                                accentColorHex = decision.movie.colorHex,
                                genre = decision.movie.genre,
                                year = decision.movie.year,
                                cornerRadius = 12.dp,
                                showFallbackTitleBadge = false,
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(80.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = decision.movie.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
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
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${decision.dateText} • ${decision.mode}",
                                    fontSize = 12.sp,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${decision.movie.year} • ${decision.movie.durationMins} dk • ${decision.movie.genre}",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = PopcornGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${decision.movie.imdb}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PopcornGold
                                        )
                                    }

                                    if (isFavorite) {
                                        Surface(
                                            color = Color(0x29EF4444),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Favorite,
                                                    contentDescription = "Favori",
                                                    tint = Color(0xFFF87171),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Favori",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFFCA5A5)
                                                )
                                            }
                                        }
                                    }

                                    if (isWatched) {
                                        Surface(
                                            color = Color(0x2910B981),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "İzlendi",
                                                    tint = EmeraldGreen,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "İzlendi",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = EmeraldGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ElectricCyan.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { shareTargetMovie = decision.movie }
                                    .testTag("history_share_button_${decision.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Paylaş",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Paylaş",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                color = CinemaTheme.colors.selectedChipFill,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onMovieFlexClick(decision.movie) }
                                    .testTag("history_flex_button_${decision.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Flex Kartı",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PopcornGold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { onDeleteDecision(decision.id) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(48.dp)
                                    .background(CinemaSurfaceVariant, CircleShape)
                                    .testTag("history_delete_button_${decision.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "${decision.movie.title} kaydını geçmişten sil",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = CinemaSurface,
            modifier = Modifier.testTag("history_clear_dialog"),
            title = {
                Text(
                    text = "Tüm Seçim Geçmişini Temizle?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Çark tarafından seçilen ${savedDecisions.size} film kaydı yerel Room veritabanından silinecek.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("history_confirm_clear_button")
                ) {
                    Text("Evet, Temizle", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmDialog = false },
                    modifier = Modifier.testTag("history_cancel_clear_button")
                ) {
                    Text("Vazgeç", color = TextSecondary)
                }
            }
        )
    }

    shareTargetMovie?.let { movie ->
        SocialShareDialog(
            movie = movie,
            onOpenStoryCard = { onMovieFlexClick(movie) },
            onDismiss = { shareTargetMovie = null }
        )
    }
}

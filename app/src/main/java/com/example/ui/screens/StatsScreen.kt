package com.example.ui.screens

import com.example.ui.components.ScreenBackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.WatchedMovieEntity
import com.example.data.model.HistoryDashboardSummary
import com.example.data.model.Movie
import com.example.data.model.analyzeDecisionHistory
import com.example.data.repository.SavedDecision
import com.example.ui.components.DecisionHistoryDashboard
import com.example.ui.components.GenreStatisticsPanel
import com.example.ui.components.WatchedStatsChartCard
import com.example.ui.theme.CinemaBackground
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatsScreen(
    savedDecisions: List<SavedDecision>,
    favoriteMovieIds: Set<String>,
    watchedMovies: List<WatchedMovieEntity>,
    historyDashboard: HistoryDashboardSummary = analyzeDecisionHistory(savedDecisions),
    onMovieClick: (Movie) -> Unit = {},
    onBack: () -> Unit
) {
    ScreenBackHandler { onBack() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("stats_screen_list"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

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
                        .testTag("stats_back_button")
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
                        text = "İstatistik Paneli",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "En sık seçilen türler, filmler ve veritabanı analizi 📊",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            GenreStatisticsPanel(
                savedDecisions = savedDecisions,
                favoriteMovieIds = favoriteMovieIds,
                watchedMovies = watchedMovies
            )

            Spacer(modifier = Modifier.height(18.dp))
        }

        item {
            DecisionHistoryDashboard(
                dashboard = historyDashboard,
                onMovieClick = onMovieClick
            )

            Spacer(modifier = Modifier.height(18.dp))
        }

        item {
            WatchedStatsChartCard(watchedMovies = watchedMovies)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

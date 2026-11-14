package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.ui.components.MoviePosterImage
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
import com.example.ui.viewmodel.CompareUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompareScreen(
    compareState: CompareUiState,
    watchedMovieIds: Set<String>,
    onPickWinner: (Boolean) -> Unit,
    onSkipPair: () -> Unit,
    onLockChampion: (Movie) -> Unit,
    onGenreFilterSelected: (String?) -> Unit,
    onRestartDuel: () -> Unit,
    onShareChampion: (Movie) -> Unit,
    onMarkAsWatched: (Movie) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val left = compareState.leftMovie
    val right = compareState.rightMovie
    val champion = compareState.champion

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBackground)
            .padding(horizontal = 16.dp)
            .testTag("compare_screen_list"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(22.dp))

            // Top App Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .background(CinemaSurface, CircleShape)
                        .testTag("compare_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = NeonPink.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NeonPink)
                    ) {
                        Text(
                            text = "FİLM KARŞILAŞTIR • VS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = NeonPink,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tinder Tarzı Hızlı Seçim",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onRestartDuel,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .background(CinemaSurface, CircleShape)
                        .testTag("compare_restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yeni Karşılaştırma Başlat",
                        tint = PopcornGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Genre Filter Strip
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compare_genre_strip"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isAllSelected = compareState.selectedGenreFilter == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { onGenreFilterSelected(null) },
                        label = {
                            Text(
                                text = "Tümü 🎬",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurple,
                            selectedLabelColor = Color.White,
                            containerColor = CinemaSurface,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .testTag("compare_genre_all")
                    )
                }
                items(MovieRepository.allGenres) { genre ->
                    val selected = compareState.selectedGenreFilter == genre
                    FilterChip(
                        selected = selected,
                        onClick = { onGenreFilterSelected(if (selected) null else genre) },
                        label = {
                            Text(
                                text = genre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = CinemaTheme.colors.onAccentTeal,
                            containerColor = CinemaSurface,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .testTag("compare_genre_${genre.lowercase().replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (champion != null) {
            // CHAMPION REVEAL
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(initialScale = 0.94f)
                ) {
                    CompareChampionSection(
                        champion = champion,
                        isWatched = watchedMovieIds.contains(champion.id),
                        streakCount = compareState.currentStreakCount,
                        onShareChampion = { onShareChampion(champion) },
                        onMarkAsWatched = { onMarkAsWatched(champion) },
                        onRestartDuel = onRestartDuel
                    )
                }
            }
        } else if (left != null && right != null) {
            // ACTIVE SIDE-BY-SIDE DUEL
            item {
                // Round & Progress Banner
                val progress = (compareState.roundNumber.toFloat() / compareState.totalRounds.coerceAtLeast(1).toFloat())
                    .coerceIn(0f, 1f)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tur ${compareState.roundNumber} / ${compareState.totalRounds}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold,
                            modifier = Modifier.testTag("compare_round_label")
                        )

                        if (compareState.currentStreakCount >= 2) {
                            Surface(
                                color = Color(0x33F59E0B),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🔥 ${compareState.currentStreakCount} Galibiyet Serisi",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PopcornGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Kalan Rakip: ${compareState.queue.size}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonPink,
                        trackColor = CinemaSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Swipeable Side-by-Side Duel Arena with Spring Snap-Back & Tinder Choice Stamps
                val view = LocalView.current
                val dragScope = rememberCoroutineScope()
                val dragOffsetAnim = remember(left.id, right.id, compareState.roundNumber) {
                    Animatable(0f)
                }
                var crossedThresholdHaptic by remember(left.id, right.id, compareState.roundNumber) {
                    mutableStateOf(false)
                }
                val dragOffsetX = dragOffsetAnim.value
                val swipeThreshold = 110f
                val swipingLeft = dragOffsetX < -30f
                val swipingRight = dragOffsetX > 30f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(left.id, right.id, compareState.roundNumber) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    when {
                                        dragOffsetAnim.value <= -swipeThreshold -> onPickWinner(true)
                                        dragOffsetAnim.value >= swipeThreshold -> onPickWinner(false)
                                    }
                                    dragScope.launch {
                                        dragOffsetAnim.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f)
                                        )
                                    }
                                    crossedThresholdHaptic = false
                                },
                                onDragCancel = {
                                    dragScope.launch {
                                        dragOffsetAnim.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f)
                                        )
                                    }
                                    crossedThresholdHaptic = false
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    val nextOffset = (dragOffsetAnim.value + dragAmount).coerceIn(-180f, 180f)
                                    dragScope.launch {
                                        dragOffsetAnim.snapTo(nextOffset)
                                    }
                                    val reachedThreshold = kotlin.math.abs(nextOffset) >= swipeThreshold
                                    if (reachedThreshold && !crossedThresholdHaptic) {
                                        crossedThresholdHaptic = true
                                        runCatching {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        }
                                    } else if (!reachedThreshold) {
                                        crossedThresholdHaptic = false
                                    }
                                }
                            )
                        }
                        .testTag("compare_duel_arena")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset((dragOffsetX * 0.25f).roundToInt(), 0) }
                            .graphicsLayer {
                                rotationZ = (dragOffsetX / 160f) * 2.5f
                            },
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        CompareMovieSideCard(
                            movie = left,
                            opponent = right,
                            sideLabel = "SOLDAKİ",
                            isHighlighted = swipingLeft,
                            isDefendingChampion = compareState.currentStreakMovieId == left.id,
                            streakCount = compareState.currentStreakCount,
                            cardTestTag = "compare_left_card",
                            buttonTestTag = "compare_pick_left_button",
                            lockTestTag = "compare_lock_left_button",
                            accentColor = ElectricCyan,
                            onSelect = { onPickWinner(true) },
                            onLockNow = { onLockChampion(left) },
                            modifier = Modifier.weight(1f)
                        )

                        CompareMovieSideCard(
                            movie = right,
                            opponent = left,
                            sideLabel = "SAĞDAKİ",
                            isHighlighted = swipingRight,
                            isDefendingChampion = compareState.currentStreakMovieId == right.id,
                            streakCount = compareState.currentStreakCount,
                            cardTestTag = "compare_right_card",
                            buttonTestTag = "compare_pick_right_button",
                            lockTestTag = "compare_lock_right_button",
                            accentColor = NeonPink,
                            onSelect = { onPickWinner(false) },
                            onLockNow = { onLockChampion(right) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Dynamic Tinder Choice Stamps during Horizontal Drag
                    if (swipingLeft) {
                        val stampAlpha = ((kotlin.math.abs(dragOffsetX) - 25f) / 85f).coerceIn(0f, 1f)
                        Surface(
                            color = ElectricCyan.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(2.dp, Color.White),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 18.dp, top = 28.dp)
                                .graphicsLayer {
                                    alpha = stampAlpha
                                    rotationZ = -12f
                                }
                                .testTag("compare_swipe_stamp_left")
                        ) {
                            Text(
                                text = "👈 SOLDAKİ SEÇİLDİ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = CinemaTheme.colors.onAccentTeal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    } else if (swipingRight) {
                        val stampAlpha = ((kotlin.math.abs(dragOffsetX) - 25f) / 85f).coerceIn(0f, 1f)
                        Surface(
                            color = NeonPink.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(2.dp, Color.White),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 18.dp, top = 28.dp)
                                .graphicsLayer {
                                    alpha = stampAlpha
                                    rotationZ = 12f
                                }
                                .testTag("compare_swipe_stamp_right")
                        ) {
                            Text(
                                text = "SAĞDAKİ SEÇİLDİ 👉",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = CinemaTheme.colors.onAccentCrimson,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Center VS Badge
                    Surface(
                        color = PopcornGold,
                        shape = CircleShape,
                        shadowElevation = 6.dp,
                        border = BorderStroke(2.dp, CinemaBackground),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 74.dp)
                            .size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "VS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tinder-style Bottom Control Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onPickWinner(true) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, ElectricCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("compare_swipe_left_cta")
                    ) {
                        Text(
                            text = "← Soldaki",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }

                    OutlinedButton(
                        onClick = onSkipPair,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, CinemaBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .testTag("compare_skip_pair_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "İkisini Geç",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = { onPickWinner(false) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, NeonPink),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("compare_swipe_right_cta")
                    ) {
                        Text(
                            text = "Sağdaki →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "İpucu: Kartları sola/sağa kaydırabilir veya beğendiğin filme dokunabilirsin.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompareMovieSideCard(
    movie: Movie,
    opponent: Movie,
    sideLabel: String,
    isHighlighted: Boolean,
    isDefendingChampion: Boolean,
    streakCount: Int,
    cardTestTag: String,
    buttonTestTag: String,
    lockTestTag: String,
    accentColor: Color,
    onSelect: () -> Unit,
    onLockNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasHigherImdb = movie.imdb > opponent.imdb
    val isShorter = movie.durationMins < opponent.durationMins

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSelect() }
            .border(
                width = if (isHighlighted || isDefendingChampion) 2.dp else 1.dp,
                color = when {
                    isHighlighted -> EmeraldGreen
                    isDefendingChampion -> PopcornGold
                    else -> CinemaBorder
                },
                shape = RoundedCornerShape(20.dp)
            )
            .testTag(cardTestTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) CinemaTheme.colors.selectedChipFill else CinemaSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = sideLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (isDefendingChampion && streakCount >= 1) {
                    Surface(
                        color = Color(0x33F59E0B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "👑 ${streakCount}x",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            MoviePosterImage(
                title = movie.title,
                posterPath = movie.posterPath,
                posterUrl = movie.posterUrl,
                accentColorHex = movie.colorHex,
                cornerRadius = 12.dp,
                showFallbackTitleBadge = false,
                testTag = "${cardTestTag}_poster",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = movie.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${movie.year} • ${movie.genre}",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Side-by-Side Comparison Metrics
            Surface(
                color = CinemaSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "IMDb", fontSize = 10.sp, color = TextMuted)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (hasHigherImdb) EmeraldGreen else PopcornGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${movie.imdb}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (hasHigherImdb) EmeraldGreen else PopcornGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Süre", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = "${movie.durationMins} dk",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isShorter) ElectricCyan else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                movie.platforms.take(2).forEach { platform ->
                    Surface(
                        color = CinemaSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = platform,
                            fontSize = 10.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = movie.logline,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 3,
                minLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = if (accentColor == ElectricCyan) {
                        CinemaTheme.colors.onAccentTeal
                    } else {
                        CinemaTheme.colors.onAccentCrimson
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag(buttonTestTag)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Bunu Seç",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = onLockNow,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CinemaBorder),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag(lockTestTag)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = PopcornGold,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hemen Onayla",
                    fontSize = 11.sp,
                    color = PopcornGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompareChampionSection(
    champion: Movie,
    isWatched: Boolean,
    streakCount: Int,
    onShareChampion: () -> Unit,
    onMarkAsWatched: () -> Unit,
    onRestartDuel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compare_champion_card"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0x33F59E0B), RoundedCornerShape(12.dp))
                .border(1.dp, PopcornGold, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = PopcornGold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "DÜELLONUN ŞAMPİYONU • $streakCount GALİBİYET!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = PopcornGold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(listOf(PopcornGold, NeonPink)),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    MoviePosterImage(
                        title = champion.title,
                        posterPath = champion.posterPath,
                        posterUrl = champion.posterUrl,
                        accentColorHex = champion.colorHex,
                        cornerRadius = 14.dp,
                        testTag = "compare_champion_poster",
                        modifier = Modifier
                            .width(96.dp)
                            .height(142.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = champion.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            lineHeight = 24.sp,
                            modifier = Modifier.testTag("compare_champion_title")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${champion.year} • ${champion.durationMins} dk",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = champion.genre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0x33F59E0B),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = PopcornGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${champion.imdb} IMDb",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PopcornGold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    champion.platforms.forEach { platform ->
                        Surface(
                            color = CinemaSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CinemaBorder)
                        ) {
                            Text(
                                text = platform,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = champion.logline,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CinemaTheme.colors.calloutSurface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 Neden Bu Film?",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopcornGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = champion.whyWatch,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onShareChampion,
            colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("compare_share_champion_button")
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Bu Akşam Bunu İzliyoruz! (Paylaş 🍿)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isWatched) {
            Surface(
                color = Color(0x3310B981),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, EmeraldGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "İzlediklerime Eklendi",
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            OutlinedButton(
                onClick = onMarkAsWatched,
                border = BorderStroke(1.5.dp, EmeraldGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("compare_mark_watched_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bu Filmi İzledim ✓",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onRestartDuel,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("compare_new_duel_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Yeni Düello Başlat",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

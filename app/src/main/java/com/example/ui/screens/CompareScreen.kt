package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.data.share.ChampionStoryRenderer
import com.example.ui.components.MoviePosterImage
import com.example.ui.components.TrailerButton
import com.example.ui.theme.CinemaBorder
import com.example.ui.theme.CinemaMotion
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
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val BrandGold = Color(0xFFE9B949)
private val BrandCrimson = Color(0xFFD63447)
private val BrandNoir = Color(0xFF0B0A0F)
private val BrandIvory = Color(0xFFF6F2EA)

/**
 * VS Düello ekranı.
 *
 * - Tür seçildiğinde düello TAM EKRAN olur: başlık ve tür şeridi gizlenir, iki dev afiş kartı ekranı kaplar.
 * - Kartlar sola/sağa kaydırılarak (ya da "Seç" ile) seçilir; her yeni eşleşme animasyonla gelir.
 * - Kazanan, logolu 9:16 hikâye kartında animasyonla açılır; "Hikâye Olarak Paylaş" aynı kartı PNG olarak paylaşır.
 */
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
    var immersive by rememberSaveable { mutableStateOf(false) }
    BackHandler { if (immersive) immersive = false else onBack() }

    val left = compareState.leftMovie
    val right = compareState.rightMovie
    val champion = compareState.champion

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (immersive) 10.dp else 16.dp)
                .testTag("compare_screen_list"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (immersive) {
                ImmersiveTopBar(
                    genreLabel = compareState.selectedGenreFilter ?: "Tüm Türler",
                    onExitImmersive = { immersive = false },
                    onRestartDuel = onRestartDuel
                )
            } else {
                CompareHeader(onBack = onBack, onRestartDuel = onRestartDuel)
                Spacer(modifier = Modifier.height(14.dp))
                GenreStrip(
                    selectedGenre = compareState.selectedGenreFilter,
                    onGenreSelected = { genre ->
                        onGenreFilterSelected(genre)
                        immersive = true
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Bir tür seç: düello tam ekran başlasın. Kartları kaydırarak seç!",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (champion != null) {
                ChampionStorySection(
                    champion = champion,
                    roundsWon = (compareState.roundNumber - 1).coerceAtLeast(compareState.currentStreakCount),
                    isWatched = watchedMovieIds.contains(champion.id),
                    maxCardHeight = viewportHeight - (if (immersive) 190.dp else 260.dp),
                    onOpenMoreShareOptions = { onShareChampion(champion) },
                    onMarkAsWatched = { onMarkAsWatched(champion) },
                    onRestartDuel = onRestartDuel
                )
            } else if (left != null && right != null) {
                RoundProgress(compareState)
                Spacer(modifier = Modifier.height(10.dp))
                DuelArena(
                    compareState = compareState,
                    left = left,
                    right = right,
                    arenaHeight = if (immersive) (viewportHeight - 200.dp).coerceAtLeast(420.dp) else 470.dp,
                    onPickWinner = onPickWinner,
                    onLockChampion = onLockChampion
                )
                Spacer(modifier = Modifier.height(12.dp))
                DuelControls(onPickWinner = onPickWinner, onSkipPair = onSkipPair)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CompareHeader(onBack: () -> Unit, onRestartDuel: () -> Unit) {
    Spacer(modifier = Modifier.height(22.dp))
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
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
            Text(text = "Kaydır, seç, kazananı paylaş", fontSize = 11.sp, color = TextSecondary)
        }
        IconButton(
            onClick = onRestartDuel,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(48.dp)
                .background(CinemaSurface, CircleShape)
                .testTag("compare_restart_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Yeni Karşılaştırma Başlat", tint = PopcornGold)
        }
    }
}

@Composable
private fun GenreStrip(selectedGenre: String?, onGenreSelected: (String?) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compare_genre_strip"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedGenre == null,
                onClick = { onGenreSelected(null) },
                label = { Text(text = "Tümü 🎬", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
            val selected = selectedGenre == genre
            FilterChip(
                selected = selected,
                onClick = { onGenreSelected(genre) },
                label = { Text(text = genre, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
}

@Composable
private fun ImmersiveTopBar(genreLabel: String, onExitImmersive: () -> Unit, onRestartDuel: () -> Unit) {
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onExitImmersive,
            modifier = Modifier
                .size(44.dp)
                .background(CinemaSurface.copy(alpha = 0.8f), CircleShape)
                .testTag("compare_exit_fullscreen_button")
        ) {
            Icon(Icons.Default.Close, contentDescription = "Tam ekrandan çık", tint = TextPrimary)
        }
        Surface(
            color = CinemaSurface.copy(alpha = 0.85f),
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f)),
            modifier = Modifier.clickable(onClickLabel = "Türü değiştir") { onExitImmersive() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.brand_mark),
                    contentDescription = null,
                    modifier = Modifier.height(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "VS • $genreLabel", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Tune, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            }
        }
        IconButton(
            onClick = onRestartDuel,
            modifier = Modifier
                .size(44.dp)
                .background(CinemaSurface.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Yeni düello", tint = PopcornGold)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun RoundProgress(state: CompareUiState) {
    val progress = (state.roundNumber.toFloat() / state.totalRounds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tur ${state.roundNumber} / ${state.totalRounds}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PopcornGold,
                modifier = Modifier.testTag("compare_round_label")
            )
            Text(
                text = if (state.currentStreakCount >= 2) "🔥 ${state.currentStreakCount} Galibiyet Serisi"
                else "Kalan Rakip: ${state.queue.size}",
                fontSize = 12.sp,
                fontWeight = if (state.currentStreakCount >= 2) FontWeight.Bold else FontWeight.Normal,
                color = if (state.currentStreakCount >= 2) PopcornGold else TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = BrandGold,
            trackColor = CinemaSurfaceVariant
        )
    }
}

@Composable
private fun DuelArena(
    compareState: CompareUiState,
    left: Movie,
    right: Movie,
    arenaHeight: Dp,
    onPickWinner: (Boolean) -> Unit,
    onLockChampion: (Movie) -> Unit
) {
    val view = LocalView.current
    val dragScope = rememberCoroutineScope()
    val dragOffsetAnim = remember(left.id, right.id, compareState.roundNumber) { Animatable(0f) }
    var crossedThresholdHaptic by remember(left.id, right.id, compareState.roundNumber) { mutableStateOf(false) }
    val swipeThreshold = 110f
    val dragOffsetX = dragOffsetAnim.value
    // 0..1: kullanıcının hangi karta doğru ne kadar kaydırdığı
    val leftLean = ((-dragOffsetX - 20f) / 120f).coerceIn(0f, 1f)
    val rightLean = ((dragOffsetX - 20f) / 120f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(arenaHeight)
            .pointerInput(left.id, right.id, compareState.roundNumber) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragOffsetAnim.value <= -swipeThreshold -> onPickWinner(true)
                            dragOffsetAnim.value >= swipeThreshold -> onPickWinner(false)
                        }
                        dragScope.launch { dragOffsetAnim.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 420f)) }
                        crossedThresholdHaptic = false
                    },
                    onDragCancel = {
                        dragScope.launch { dragOffsetAnim.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 420f)) }
                        crossedThresholdHaptic = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val next = (dragOffsetAnim.value + dragAmount).coerceIn(-200f, 200f)
                        dragScope.launch { dragOffsetAnim.snapTo(next) }
                        val reached = abs(next) >= swipeThreshold
                        if (reached && !crossedThresholdHaptic) {
                            crossedThresholdHaptic = true
                            runCatching { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }
                        } else if (!reached) {
                            crossedThresholdHaptic = false
                        }
                    }
                )
            }
            .testTag("compare_duel_arena")
    ) {
        AnimatedContent(
            targetState = left to right,
            transitionSpec = {
                (fadeIn(tween(CinemaMotion.DurationMedium, easing = CinemaMotion.EmphasizedDecelerate)) +
                    scaleIn(initialScale = 0.9f, animationSpec = tween(CinemaMotion.DurationLong, easing = CinemaMotion.EmphasizedDecelerate)) +
                    slideInVertically(tween(CinemaMotion.DurationLong, easing = CinemaMotion.EmphasizedDecelerate)) { it / 10 }) togetherWith
                    fadeOut(tween(CinemaMotion.DurationShort))
            },
            contentKey = { (l, r) -> l.id + "|" + r.id },
            label = "duel_pair"
        ) { (l, r) ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset((dragOffsetX * 0.25f).roundToInt(), 0) }
                    .graphicsLayer { rotationZ = (dragOffsetX / 160f) * 2f },
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DuelPosterCard(
                    movie = l,
                    sideLabel = "SOLDAKİ",
                    accent = ElectricCyan,
                    lean = leftLean,
                    dim = rightLean,
                    isDefendingChampion = compareState.currentStreakMovieId == l.id,
                    streakCount = compareState.currentStreakCount,
                    cardTestTag = "compare_left_card",
                    pickTestTag = "compare_pick_left_button",
                    lockTestTag = "compare_lock_left_button",
                    onSelect = { onPickWinner(true) },
                    onLockNow = { onLockChampion(l) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                DuelPosterCard(
                    movie = r,
                    sideLabel = "SAĞDAKİ",
                    accent = NeonPink,
                    lean = rightLean,
                    dim = leftLean,
                    isDefendingChampion = compareState.currentStreakMovieId == r.id,
                    streakCount = compareState.currentStreakCount,
                    cardTestTag = "compare_right_card",
                    pickTestTag = "compare_pick_right_button",
                    lockTestTag = "compare_lock_right_button",
                    onSelect = { onPickWinner(false) },
                    onLockNow = { onLockChampion(r) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        // Kaydırırken beliren seçim damgaları
        if (leftLean > 0f) {
            SwipeStamp("👈 SOLDAKİ", ElectricCyan, CinemaTheme.colors.onAccentTeal, -12f, leftLean,
                Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 54.dp).testTag("compare_swipe_stamp_left"))
        } else if (rightLean > 0f) {
            SwipeStamp("SAĞDAKİ 👉", NeonPink, CinemaTheme.colors.onAccentCrimson, 12f, rightLean,
                Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = 54.dp).testTag("compare_swipe_stamp_right"))
        }

        // VS rozeti: logo renklerinde
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-40).dp)
                .size(48.dp)
                .background(Brush.radialGradient(listOf(Color(0xFF2A2530), BrandNoir)), CircleShape)
                .border(2.dp, BrandGold, CircleShape)
        ) {
            Text(text = "VS", fontSize = 15.sp, fontWeight = FontWeight.Black, color = BrandGold)
        }
    }
}

@Composable
private fun SwipeStamp(text: String, bg: Color, fg: Color, rotation: Float, progress: Float, modifier: Modifier) {
    Surface(
        color = bg.copy(alpha = 0.92f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, Color.White),
        modifier = modifier.graphicsLayer {
            alpha = progress
            rotationZ = rotation
            val s = 0.8f + 0.2f * progress
            scaleX = s
            scaleY = s
        }
    ) {
        Text(
            text = "$text SEÇİLDİ",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun DuelPosterCard(
    movie: Movie,
    sideLabel: String,
    accent: Color,
    lean: Float,
    dim: Float,
    isDefendingChampion: Boolean,
    streakCount: Int,
    cardTestTag: String,
    pickTestTag: String,
    lockTestTag: String,
    onSelect: () -> Unit,
    onLockNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .graphicsLayer {
                val s = 1f + lean * 0.04f - dim * 0.04f
                scaleX = s
                scaleY = s
                alpha = 1f - dim * 0.45f
            }
            .clip(shape)
            .background(CinemaSurface)
            .border(if (lean > 0f) 2.5.dp else 1.dp, if (lean > 0f) accent else CinemaBorder, shape)
            .testTag(cardTestTag)
    ) {
        MoviePosterImage(
            title = movie.title,
            posterPath = movie.posterPath,
            posterUrl = movie.posterUrl,
            accentColorHex = movie.colorHex,
            genre = movie.genre,
            year = movie.year,
            cornerRadius = 0.dp,
            modifier = Modifier.fillMaxSize()
        )
        // Alt karartma: bilgiler afişin üstünde okunur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to Color.Transparent,
                        0.72f to BrandNoir.copy(alpha = 0.78f),
                        1f to BrandNoir.copy(alpha = 0.97f)
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = accent.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp)) {
                Text(
                    text = sideLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    color = BrandNoir,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            if (isDefendingChampion && streakCount >= 1) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(color = BrandGold, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "👑 $streakCount",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandNoir,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = movie.title,
                fontSize = 17.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Black,
                color = BrandIvory,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${movie.year} • ${movie.genre}",
                fontSize = 11.sp,
                color = BrandIvory.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = BrandGold, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "${movie.imdb}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandGold)
                Text(text = "  •  ${movie.durationMins} dk", fontSize = 11.sp, color = BrandIvory.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = BrandNoir),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag(pickTestTag)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Seç", fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onLockNow,
                    modifier = Modifier
                        .size(42.dp)
                        .background(BrandNoir.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .border(1.dp, BrandGold.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .testTag(lockTestTag)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Hemen onayla: ${movie.title}", tint = BrandGold, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DuelControls(onPickWinner: (Boolean) -> Unit, onSkipPair: () -> Unit) {
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
                .height(46.dp)
                .testTag("compare_swipe_left_cta")
        ) {
            Text(text = "← Soldaki", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
        }
        OutlinedButton(
            onClick = onSkipPair,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.2.dp, CinemaBorder),
            contentPadding = PaddingValues(horizontal = 10.dp),
            modifier = Modifier
                .weight(1.1f)
                .height(46.dp)
                .testTag("compare_skip_pair_button")
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "İkisini Geç", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        }
        OutlinedButton(
            onClick = { onPickWinner(false) },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.2.dp, NeonPink),
            contentPadding = PaddingValues(horizontal = 10.dp),
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("compare_swipe_right_cta")
        ) {
            Text(text = "Sağdaki →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonPink)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Kazanan: logolu 9:16 hikâye kartı + paylaşım
// -------------------------------------------------------------------------------------------------

@Composable
private fun ChampionStorySection(
    champion: Movie,
    roundsWon: Int,
    isWatched: Boolean,
    maxCardHeight: Dp,
    onOpenMoreShareOptions: () -> Unit,
    onMarkAsWatched: () -> Unit,
    onRestartDuel: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    LaunchedEffect(champion.id) {
        runCatching { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM) }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        ChampionStoryCard(
            champion = champion,
            roundsWon = roundsWon,
            modifier = Modifier
                .heightIn(max = maxCardHeight.coerceAtLeast(360.dp))
                .aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { ChampionStoryRenderer.share(context, champion, roundsWon) },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = BrandNoir),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("compare_share_champion_button")
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Hikâye Olarak Paylaş", fontSize = 16.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (champion.trailerKey != null) {
            TrailerButton(movie = champion)
            Spacer(modifier = Modifier.height(10.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            if (isWatched) {
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, EmeraldGreen),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "İzlendi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onMarkAsWatched,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, EmeraldGreen),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("compare_mark_watched_button")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "İzledim", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }
            }
            OutlinedButton(
                onClick = onRestartDuel,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, CinemaBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("compare_new_duel_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Yeni Düello", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        TextButton(onClick = onOpenMoreShareOptions, modifier = Modifier.testTag("compare_more_share_options")) {
            Text(text = "Diğer paylaşım seçenekleri", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

/**
 * Ekranda gösterilen hikâye kartı. Açılış: 3B dönüşle büyüme, konfeti patlaması ve tek seferlik parıltı.
 * Tüm animasyon tek bir sonlu zaman çizelgesinden türetilir.
 */
@Composable
private fun ChampionStoryCard(champion: Movie, roundsWon: Int, modifier: Modifier = Modifier) {
    val timeline = remember(champion.id) { Animatable(0f) }
    LaunchedEffect(champion.id) { timeline.animateTo(1f, tween(1800, easing = LinearEasing)) }
    val t = timeline.value
    val enter = CinemaMotion.EmphasizedDecelerate.transform((t / 0.45f).coerceIn(0f, 1f))
    val confetti = remember(champion.id) {
        val rnd = Random(champion.id.hashCode())
        List(70) { floatArrayOf(rnd.nextFloat() * 6.283f, 0.5f + rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat()) }
    }
    val confettiColors = listOf(BrandGold, BrandCrimson, BrandIvory, Color(0xFF34D399), Color(0xFF60A5FA))
    val shape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = 0.72f + 0.28f * enter
                scaleX = s
                scaleY = s
                alpha = enter
                rotationY = (1f - enter) * -60f
                cameraDistance = 16f * density
            }
            .clip(shape)
            .background(BrandNoir)
            .border(1.5.dp, Brush.verticalGradient(listOf(BrandGold, BrandCrimson)), shape)
            .semantics { contentDescription = "Kazanan film: ${champion.title}" }
            .testTag("compare_champion_card")
    ) {
        MoviePosterImage(
            title = champion.title,
            posterPath = champion.posterPath,
            posterUrl = champion.posterUrl,
            accentColorHex = champion.colorHex,
            genre = champion.genre,
            year = champion.year,
            cornerRadius = 0.dp,
            testTag = "compare_champion_poster",
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to BrandNoir.copy(alpha = 0.9f),
                        0.22f to Color.Transparent,
                        0.5f to Color.Transparent,
                        0.78f to BrandNoir.copy(alpha = 0.85f),
                        1f to BrandNoir.copy(alpha = 0.98f)
                    )
                )
        )

        // Logo + kelime logosu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(BrandNoir, RoundedCornerShape(10.dp))
                    .border(1.dp, BrandGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            ) {
                Image(painter = painterResource(id = R.drawable.brand_mark), contentDescription = null, modifier = Modifier.height(24.dp))
            }
            Spacer(modifier = Modifier.width(9.dp))
            Column {
                Text(text = "Ne İzlesek?", fontSize = 17.sp, fontWeight = FontWeight.Black, color = BrandIvory, letterSpacing = (-0.3).sp)
                Text(text = "VS DÜELLO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = BrandGold, letterSpacing = 2.sp)
            }
        }

        // Alt bilgi bloğu
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(18.dp)
                .graphicsLayer {
                    val p = ((t - 0.3f) / 0.35f).coerceIn(0f, 1f)
                    alpha = p
                    translationY = (1f - CinemaMotion.EmphasizedDecelerate.transform(p)) * 30.dp.toPx()
                }
        ) {
            Surface(color = BrandGold, shape = RoundedCornerShape(999.dp)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BrandNoir, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "KAZANAN", fontSize = 12.sp, fontWeight = FontWeight.Black, color = BrandNoir, letterSpacing = 2.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = champion.title,
                fontSize = 28.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Black,
                color = BrandIvory,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("compare_champion_title")
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${champion.year} • ${champion.genre} • ${champion.durationMins} dk",
                fontSize = 12.sp,
                color = BrandIvory.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = BrandGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "${champion.imdb}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandGold)
                Text(
                    text = if (roundsWon > 0) "  •  $roundsWon turda rakiplerini eledi" else "  •  Düellonun galibi",
                    fontSize = 12.sp,
                    color = BrandIvory.copy(alpha = 0.8f)
                )
            }
        }

        // Konfeti patlaması + tek seferlik parıltı
        Canvas(modifier = Modifier.fillMaxSize()) {
            val burst = ((t - 0.18f) / 0.82f).coerceIn(0f, 1f)
            if (burst in 0.001f..0.999f) {
                val origin = Offset(size.width / 2f, size.height * 0.4f)
                confetti.forEachIndexed { i, p ->
                    val dist = size.width * 0.75f * p[1] * CinemaMotion.EmphasizedDecelerate.transform(burst)
                    val fall = size.height * 0.25f * burst * burst
                    val pos = Offset(origin.x + cos(p[0]) * dist, origin.y + sin(p[0]) * dist + fall)
                    val color = confettiColors[i % confettiColors.size].copy(alpha = 1f - burst)
                    rotate(degrees = p[2] * 720f * burst, pivot = pos) {
                        drawRect(color, topLeft = Offset(pos.x - 4f, pos.y - 2.5f), size = Size(8f + p[3] * 8f, 5f))
                    }
                }
            }
            val sweep = ((t - 0.5f) / 0.45f).coerceIn(0f, 1f)
            if (sweep in 0.001f..0.999f) {
                val x = -size.width * 0.5f + sweep * size.width * 2f
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.22f), Color.Transparent),
                        start = Offset(x - 120f, 0f),
                        end = Offset(x + 120f, size.height)
                    )
                )
            }
        }
    }
}

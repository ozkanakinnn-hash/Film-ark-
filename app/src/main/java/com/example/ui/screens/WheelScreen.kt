package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioLanguageOption
import com.example.data.model.CustomWheelList
import com.example.data.model.Movie
import com.example.data.model.kindNoun
import com.example.data.model.contentNoun
import com.example.data.audio.rememberWheelFeedbackCoordinator
import com.example.data.repository.MovieRepository
import com.example.data.sensor.rememberAccelerometerShakeEffect
import com.example.ui.components.MoviePosterImage
import com.example.ui.components.SimilarMoviesSection
import com.example.ui.components.SocialShareDialog
import com.example.ui.components.WheelCanvas
import com.example.ui.components.WheelRevealAnimationBanner
import com.example.ui.components.isHorrorMovie
import com.example.ui.components.resolveGenreRevealStyle
import com.example.ui.components.GenreRevealOverlay
import com.example.ui.components.TrailerButton
import com.example.ui.theme.pressScale
import com.example.ui.theme.rememberPressSource
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
import com.example.ui.theme.WheelThemeManager
import com.example.ui.viewmodel.SpinEvent
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WheelScreen(
    movies: List<Movie>,
    spinEvent: SpinEvent?,
    isSpinning: Boolean,
    selectedMovie: Movie?,
    isVetoedOnce: Boolean,
    isDecisionLocked: Boolean,
    activeCustomList: CustomWheelList? = null,
    selectedGenres: Set<String> = emptySet(),
    selectedPlatforms: Set<String> = emptySet(),
    selectedAudioOptions: Set<String> = emptySet(),
    minYear: Int = 1970,
    maxYear: Int = 2026,
    minRating: Double = 0.0,
    watchedMovieIds: Set<String> = emptySet(),
    excludedWheelMovieIds: Set<String> = watchedMovieIds,
    favoriteMovieIds: Set<String> = emptySet(),
    poolTotal: Int = movies.size,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onWheelMinRatingChange: (Double) -> Unit = {},
    onToggleFavorite: (Movie) -> Unit = {},
    onOpenCustomLists: () -> Unit = {},
    onSpinClick: () -> Unit,
    onSpinCompleted: (Long) -> Unit,
    onVetoClick: () -> Unit,
    onMarkAsWatched: (Movie) -> Unit = {},
    onAddToWatchLater: (Movie) -> Unit = {},
    onSelectAlternative: (Movie) -> Unit = {},
    onOpenStoryModal: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val view = LocalView.current
    var revealMovie by remember { mutableStateOf<Movie?>(null) }
    var lastRevealMovie by remember { mutableStateOf<Movie?>(null) }
    BackHandler(enabled = revealMovie != null) { revealMovie = null }
    val reduceMotion = com.example.data.local.LocalReduceMotion.current
    // Veto mührü animasyonu oynarken gösterilen film; bitince veto uygulanır
    var vetoStampMovie by remember { mutableStateOf<Movie?>(null) }
    var showDirectShareSheet by remember { mutableStateOf(false) }
    var isShakeEnabled by remember { mutableStateOf(true) }
    var isHapticEnabled by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var rawShakeIntensity by remember { mutableFloatStateOf(0f) }

    val wheelRotation = remember { Animatable(0f) }
    val needleAngle = remember { Animatable(0f) }
    val feedbackCoordinator = rememberWheelFeedbackCoordinator(
        hapticEnabled = isHapticEnabled,
        soundEnabled = isSoundEnabled
    )

    val hasAccelerometer = rememberAccelerometerShakeEffect(
        enabled = isShakeEnabled && movies.isNotEmpty() && !showDirectShareSheet,
        isSpinning = isSpinning,
        onShakeIntensityChange = { intensity ->
            rawShakeIntensity = intensity
        },
        onShakeTriggered = {
            if (!isSpinning && movies.isNotEmpty()) {
                if (isHapticEnabled) {
                    runCatching {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                }
                onSpinClick()
            }
        }
    )

    LaunchedEffect(rawShakeIntensity) {
        if (rawShakeIntensity > 0f) {
            delay(360L)
            rawShakeIntensity = 0f
        }
    }

    val animatedShakeIntensity by animateFloatAsState(
        targetValue = if (isSpinning) 1f else rawShakeIntensity,
        animationSpec = tween(durationMillis = 180),
        label = "shake_intensity_meter"
    )

    LaunchedEffect(spinEvent?.id) {
        val event = spinEvent ?: return@LaunchedEffect
        val current = ((wheelRotation.value % 360f) + 360f) % 360f
        wheelRotation.snapTo(current)

        feedbackCoordinator.onSpinStart()

        val sliceStepAngle = (360f / movies.size.coerceAtLeast(1)).coerceIn(18f, 45f)
        val totalDelta = kotlin.math.abs(event.targetAngle - current).coerceAtLeast(1f)
        var lastTickAngle = current
        var needleJob: Job? = null

        wheelRotation.animateTo(
            targetValue = event.targetAngle,
            animationSpec = tween(
                durationMillis = if (reduceMotion) 1600 else 4400,
                easing = CubicBezierEasing(0.12f, 0.8f, 0.2f, 1.0f)
            )
        ) {
            if (kotlin.math.abs(this.value - lastTickAngle) >= sliceStepAngle) {
                val spinProgress = (kotlin.math.abs(this.value - current) / totalDelta).coerceIn(0f, 1f)
                feedbackCoordinator.onSliceCrossing(spinProgress)
                if (isHapticEnabled) {
                    runCatching {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                }
                lastTickAngle = this.value
                needleJob?.cancel()
                needleJob = launch {
                    needleAngle.snapTo(-16f)
                    needleAngle.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = 800f))
                }
            }
        }

        val isHorrorWinner = event.winner.isHorrorMovie()
        feedbackCoordinator.onWheelStop(isHorror = isHorrorWinner)
        if (isHapticEnabled) {
            runCatching {
                view.performHapticFeedback(
                    if (isHorrorWinner) HapticFeedbackConstants.LONG_PRESS
                    else HapticFeedbackConstants.KEYBOARD_TAP
                )
            }
        }
        onSpinCompleted(event.id)
        lastRevealMovie = event.winner
        // Sonuç ekranı her zaman açılır (animasyonları azalt açıksa animasyonsuz); seçim yapılana kadar kalır
        revealMovie = event.winner
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .testTag("wheel_screen_list"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // App Bar
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
                            .testTag("wheel_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = TextPrimary
                        )
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
                                .testTag("wheel_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Açık Temaya Geç" else "Koyu Temaya Geç",
                                tint = PopcornGold
                            )
                        }

                        if (selectedMovie != null) {
                            IconButton(
                                onClick = { showDirectShareSheet = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(48.dp)
                                    .background(CinemaSurface, CircleShape)
                                    .testTag("open_story_modal_header_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Paylaş",
                                    tint = PopcornGold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val isWorstWheel = activeCustomList?.id == com.example.data.repository.SmartWheelLists.WORST
                if (isWorstWheel) {
                    com.example.ui.components.WorstWheelHeader(modifier = Modifier.padding(bottom = 12.dp))
                } else if (activeCustomList != null) {
                    val listAccent = Color(activeCustomList.accentColorHex)
                    Surface(
                        color = listAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, listAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .semantics(mergeDescendants = true) {}
                            .clickable { onOpenCustomLists() }
                            .testTag("wheel_custom_list_badge")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeCustomList.emoji} Özel Çark: ${activeCustomList.name} (${movies.size} ${movies.contentNoun()})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Listeler ✎",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = listAccent
                            )
                        }
                    }
                }

                val hasActiveNarrowing = selectedGenres.isNotEmpty() || selectedAudioOptions.isNotEmpty() || minYear > 1970 || maxYear < 2026 || minRating > 0.0
                if (hasActiveNarrowing) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("wheel_active_filters_row"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedGenres.forEach { genre ->
                            Surface(
                                color = CinemaTheme.colors.selectedChipFill,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple)
                            ) {
                                Text(
                                    text = "🎬 $genre",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PopcornGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        selectedAudioOptions.forEach { audioId ->
                            val label = AudioLanguageOption.entries.find { it.id == audioId }?.label ?: audioId
                            Surface(
                                color = Color(0x3306B6D4),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                            ) {
                                Text(
                                    text = "🎧 $label",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (minYear > 1970 || maxYear < 2026) {
                            Surface(
                                color = Color(0x3306B6D4),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                            ) {
                                Text(
                                    text = "📅 $minYear-$maxYear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (minRating > 0.0) {
                            Surface(
                                color = Color(0x33F59E0B),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PopcornGold)
                            ) {
                                Text(
                                    text = "★ ${String.format(Locale.US, "%.1f", minRating)}+",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PopcornGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Surface(
                            color = CinemaSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${movies.size} ${movies.contentNoun()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Veto Status Banner
                if (isDecisionLocked) {
                    Surface(
                        color = Color(0x3310B981),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KADER KİLİTLENDİ! İtiraz yasaktır, mısırları patlatın! 🍿",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }
                } else if (isVetoedOnce) {
                    Surface(
                        color = VetoRed.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VetoRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "1 Veto Kullanıldı • Bu tur son karardır! 🔒",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VetoRed
                            )
                        }
                    }
                } else {
                    Surface(
                        color = CinemaSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎯 Günlük 1 Veto Hakkı Mevcuttur (1/1)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PopcornGold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isWorstWheel) {
                    // Bu çarkta puan filtresi yok: Rezalet Ölçer bilgi kartı
                    com.example.ui.components.RezaletMeter(movies = movies, poolTotal = poolTotal)
                } else {
                    // MINIMUM IMDb PUANI FILTER BAR (Directly updates wheel pool)
                    WheelMinImdbFilterBar(
                        currentMinRating = minRating,
                        poolCount = movies.size,
                        poolNoun = movies.contentNoun(),
                        poolTotal = poolTotal,
                        isSpinning = isSpinning,
                        onSelectMinRating = onWheelMinRatingChange
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val wheelTheme = WheelThemeManager.current

                // THE INTERACTIVE WHEEL CANVAS
                WheelCanvas(
                    movies = movies,
                    showRatings = isWorstWheel,
                    rotationProvider = { wheelRotation.value },
                    needleDeflectionProvider = { needleAngle.value },
                    isSpinning = isSpinning,
                    selectedMovie = selectedMovie,
                    wheelTheme = wheelTheme,
                    onWheelClick = {
                        if (!isSpinning && movies.isNotEmpty()) {
                            onSpinClick()
                        }
                    },
                    modifier = Modifier
                        .widthIn(max = 340.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Active Wheel Theme Palette Indicator Pill
                Surface(
                    color = CinemaSurfaceVariant,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, wheelTheme.outerRimColor.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .semantics(mergeDescendants = true) {}
                        .testTag("wheel_theme_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Aktif çark teması",
                            tint = wheelTheme.outerRimColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = wheelTheme.themeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }

                if (isSpinning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = ElectricCyan.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .semantics(mergeDescendants = true) {}
                            .testTag("wheel_spinning_badge")
                    ) {
                        Text(
                            text = "🎡 Çark Dönüyor • Rotalar Hesaplanıyor...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                } else if (selectedMovie != null) {
                    val revealStyle = selectedMovie.resolveGenreRevealStyle()
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = revealStyle.primaryColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, revealStyle.primaryColor.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .semantics(mergeDescendants = true) {}
                            .testTag("wheel_winner_highlight_badge")
                    ) {
                        Text(
                            text = "✨ Seçilen ${selectedMovie.kindNoun} Vurgulandı (${revealStyle.badgeLabel}): ${selectedMovie.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = revealStyle.secondaryColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ACCELEROMETER SHAKE-TO-SPIN, HAPTIC VIBRATION & SOUND ENGINE CARD
                ShakeToSpinSensorCard(
                    hasAccelerometer = hasAccelerometer,
                    isShakeEnabled = isShakeEnabled,
                    isHapticEnabled = isHapticEnabled,
                    isSoundEnabled = isSoundEnabled,
                    isSpinning = isSpinning,
                    shakeIntensity = animatedShakeIntensity,
                    onToggleShake = { isShakeEnabled = !isShakeEnabled },
                    onToggleHaptic = { isHapticEnabled = !isHapticEnabled },
                    onToggleSound = {
                        val nextEnabled = !isSoundEnabled
                        isSoundEnabled = nextEnabled
                        feedbackCoordinator.soundEngine.isEnabled = nextEnabled
                        if (nextEnabled) {
                            feedbackCoordinator.soundEngine.playPreviewClick()
                        } else {
                            feedbackCoordinator.soundEngine.stopAll()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Spin CTA Button (Manual fallback / accessibility)
                if (selectedMovie == null || isSpinning) {
                    val spinPressSource = rememberPressSource()
                    Button(
                        onClick = onSpinClick,
                        enabled = !isSpinning && movies.isNotEmpty(),
                        interactionSource = spinPressSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .pressScale(spinPressSource)
                            .testTag("spin_wheel_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isWorstWheel) Color(0xFFD63447) else PopcornGold),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isSpinning) {
                            Text(
                                text = "Kader Dönüyor... 🎡",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isWorstWheel) "Kendi İsteğinle Çevir" else "Çarkı Çevir! 🍿",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isWorstWheel) Color.White else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Selected Movie Reveal Section
            if (selectedMovie != null && !isSpinning) {
                item {
                    val isHorrorReveal = selectedMovie.isHorrorMovie()
                    LaunchedEffect(selectedMovie.id, isHorrorReveal, isHapticEnabled) {
                        if (isHorrorReveal && isHapticEnabled) {
                            runCatching {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Cinematic or Horror Reveal Animation Banner
                            WheelRevealAnimationBanner(movie = selectedMovie)

                            Spacer(modifier = Modifier.height(14.dp))

                            // Genre-matched Movie Card Border
                            val revealStyle = selectedMovie.resolveGenreRevealStyle()
                            val cardBorderBrush = Brush.verticalGradient(
                                listOf(revealStyle.primaryColor, revealStyle.secondaryColor)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.6.dp,
                                        cardBorderBrush,
                                        RoundedCornerShape(24.dp)
                                    ),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        MoviePosterImage(
                                            title = selectedMovie.title,
                                            posterPath = selectedMovie.posterPath,
                                            posterUrl = selectedMovie.posterUrl,
                                            accentColorHex = selectedMovie.colorHex,
                                            genre = selectedMovie.genre,
                                            year = selectedMovie.year,
                                            cornerRadius = 14.dp,
                                            testTag = "wheel_movie_poster",
                                            modifier = Modifier
                                                .width(92.dp)
                                                .height(136.dp)
                                        )

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                                                    Text(
                                                        text = selectedMovie.title,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = TextPrimary,
                                                        lineHeight = 24.sp,
                                                        modifier = Modifier.testTag("wheel_selected_movie_title")
                                                    )
                                                    if (selectedMovie.originalTitle.isNotEmpty() && selectedMovie.originalTitle != selectedMovie.title) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = selectedMovie.originalTitle,
                                                            fontSize = 12.sp,
                                                            color = TextSecondary
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    color = Color(0x33F59E0B),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = PopcornGold,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "${selectedMovie.imdb}",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = PopcornGold
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = "${selectedMovie.year} • ${if (selectedMovie.isSeries) "Dizi • " + selectedMovie.durationLabel() else "${selectedMovie.durationMins} dakika"}",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = selectedMovie.genre,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ElectricCyan
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Platforms & Audio Badges
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        selectedMovie.platforms.forEach { platform ->
                                            Surface(
                                                color = CinemaSurfaceVariant,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaBorder)
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
                                        Surface(
                                            color = NeonPurple.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.55f)),
                                            modifier = Modifier.testTag("wheel_movie_audio_badge")
                                        ) {
                                            Text(
                                                text = "🎧 ${selectedMovie.audioBadgesText()}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PopcornGold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Synopsis
                                    Text(
                                        text = selectedMovie.logline,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Witty Recommendation Hook
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CinemaTheme.colors.calloutSurface, RoundedCornerShape(12.dp))
                                            .border(1.dp, CinemaBorder, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "💡 Neden Bu ${selectedMovie.kindNoun}?",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PopcornGold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val personalReason = remember(selectedMovie.id, selectedPlatforms, selectedGenres) {
                                                com.example.data.repository.buildWhyThisMovie(
                                                    movie = selectedMovie,
                                                    profile = com.example.data.repository.TasteProfileRepository.snapshot(),
                                                    selectedPlatforms = selectedPlatforms,
                                                    selectedGenres = selectedGenres
                                                )
                                            }
                                            Text(
                                                text = personalReason,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary,
                                                lineHeight = 16.sp,
                                                modifier = Modifier.testTag("wheel_why_this_movie")
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = selectedMovie.whyWatch,
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val isSelectedWatched = watchedMovieIds.contains(selectedMovie.id)

                            // MARK AS WATCHED ACTION (Excludes from future spins & saves to Profile)
                            if (isSelectedWatched) {
                                Surface(
                                    color = Color(0x3310B981),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("watched_confirmed_badge")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "İzlendi",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "İzlediklerime Eklendi • Çarktan Çıkarıldı",
                                            color = EmeraldGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            } else {
                                OutlinedButton(
                                    onClick = { onMarkAsWatched(selectedMovie) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("mark_watched_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "İzledim olarak işaretle",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bu Filmi İzledim ✓ (Çarktan Çıkar)",
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // ACTION BUTTONS: VETO & SOCIAL SHARE
                            // VETO ACTION: Elides the chosen movie and immediately re-spins the wheel
                            if (!isVetoedOnce && !isDecisionLocked && !isSelectedWatched) {
                                OutlinedButton(
                                    onClick = { vetoStampMovie = selectedMovie },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("veto_action_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VetoRed)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = VetoRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bu Filmi VETO ET! (1/1 Hak - Çarkı Yeniden Çevir)",
                                        color = VetoRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // DIRECT SOCIAL SHARE & FAVORITE TOGGLE ROW
                            val isFavorite = favoriteMovieIds.contains(selectedMovie.id)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showDirectShareSheet = true },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(52.dp)
                                        .testTag("wheel_direct_share_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PopcornGold),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Sosyal medyada paylaş",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Paylaş",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onToggleFavorite(selectedMovie) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("wheel_favorite_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isFavorite) NeonPink else CinemaBorder
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                                        tint = if (isFavorite) NeonPink else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isFavorite) "Favorimde" else "Favorile",
                                        color = if (isFavorite) NeonPink else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // RESMİ FRAGMAN (YouTube'da açılır)
                            if (selectedMovie.trailerKey != null) {
                                TrailerButton(movie = selectedMovie)
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Spin Again if locked or already watched
                            if (isDecisionLocked || isSelectedWatched) {
                                OutlinedButton(
                                    onClick = onSpinClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("spin_again_button"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Yeni Bir ${movies.contentNoun()} İçin Çevir",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            val similarAlternatives = remember(selectedMovie.id, excludedWheelMovieIds) {
                                MovieRepository.findSimilarAlternatives(
                                    targetMovie = selectedMovie,
                                    excludedMovieIds = excludedWheelMovieIds,
                                    limit = 3
                                )
                            }

                            SimilarMoviesSection(
                                targetMovie = selectedMovie,
                                isTargetWatched = isSelectedWatched,
                                recommendations = similarAlternatives,
                                onSelectAlternative = onSelectAlternative
                            )

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }

        // Çark durduğunda türe özel tam ekran sinematik açılış sahnesi
        AnimatedVisibility(
            visible = revealMovie != null,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(220))
        ) {
            val movieForReveal = revealMovie ?: lastRevealMovie
            if (movieForReveal != null) {
                GenreRevealOverlay(
                    movie = movieForReveal,
                    onFinished = { revealMovie = null },
                    actions = com.example.ui.components.RevealActions(
                        canVeto = !isVetoedOnce && !isDecisionLocked,
                        onVeto = {
                            revealMovie = null
                            onVetoClick()
                        },
                        onWatchLater = {
                            onAddToWatchLater(movieForReveal)
                            revealMovie = null
                        },
                        onShare = { showDirectShareSheet = true },
                        onClose = { revealMovie = null }
                    ),
                    stampText = if (activeCustomList?.id == com.example.data.repository.SmartWheelLists.WORST) {
                        "Bunu sen istedin! 🤷"
                    } else {
                        null
                    }
                )
            }
        }

        // Veto mührü: afişe kırmızı VETO damgası vurulur, bitince çark yeniden döner
        vetoStampMovie?.let { vetoed ->
            com.example.ui.components.VetoStampOverlay(
                movie = vetoed,
                onFinished = {
                    vetoStampMovie = null
                    onVetoClick()
                }
            )
        }
    }

    // Multi-Platform Direct Social Share Dialog
    if (showDirectShareSheet && selectedMovie != null) {
        SocialShareDialog(
            movie = selectedMovie,
            onOpenStoryCard = onOpenStoryModal,
            onDismiss = { showDirectShareSheet = false }
        )
    }
}

private data class WheelImdbPreset(
    val label: String,
    val minRating: Double,
    val tag: String
)

private val WheelImdbPresets = listOf(
    WheelImdbPreset("Tümü", 0.0, "wheel_imdb_chip_all"),
    WheelImdbPreset("7.0+", 7.0, "wheel_imdb_chip_7_0"),
    WheelImdbPreset("7.5+", 7.5, "wheel_imdb_chip_7_5"),
    WheelImdbPreset("8.0+", 8.0, "wheel_imdb_chip_8_0"),
    WheelImdbPreset("8.5+", 8.5, "wheel_imdb_chip_8_5")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WheelMinImdbFilterBar(
    currentMinRating: Double,
    poolCount: Int,
    poolNoun: String = "Film",
    poolTotal: Int = poolCount,
    isSpinning: Boolean,
    onSelectMinRating: (Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CinemaBorder, RoundedCornerShape(16.dp))
            .testTag("wheel_min_imdb_filter_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Minimum Puan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = EmeraldGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (poolTotal > poolCount) "$poolCount/$poolTotal $poolNoun" else "$poolCount $poolNoun",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("wheel_pool_count_badge")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WheelImdbPresets.forEach { preset ->
                    val isSelected = kotlin.math.abs(currentMinRating - preset.minRating) < 0.05
                    Surface(
                        color = if (isSelected) PopcornGold else CinemaSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PopcornGold else CinemaBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !isSpinning) { onSelectMinRating(preset.minRating) }
                            .testTag(preset.tag)
                    ) {
                        Text(
                            text = if (preset.minRating > 0.0) "★ ${preset.label}" else preset.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sallayarak çevirme, titreşim ve ses ayarlarını tek satırda gösteren sade ayar şeridi.
 */
@Composable
private fun ShakeToSpinSensorCard(
    hasAccelerometer: Boolean,
    isShakeEnabled: Boolean,
    isHapticEnabled: Boolean,
    isSoundEnabled: Boolean,
    isSpinning: Boolean,
    shakeIntensity: Float,
    onToggleShake: () -> Unit,
    onToggleHaptic: () -> Unit,
    onToggleSound: () -> Unit
) {
    val shakeActive = hasAccelerometer && isShakeEnabled
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wheel_shake_sensor_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasAccelerometer) {
                WheelSettingChip(
                    icon = Icons.Default.Sensors,
                    title = "Salla",
                    enabled = isShakeEnabled,
                    accent = ElectricCyan,
                    statusText = when {
                        isSpinning && shakeActive -> "Dönüyor"
                        isShakeEnabled -> "Açık"
                        else -> "Kapalı"
                    },
                    onClick = onToggleShake,
                    clickTag = "shake_sensor_toggle",
                    statusTag = "shake_sensor_status_text",
                    contentDescription = "Telefonu sallayarak çevirme",
                    modifier = Modifier.weight(1f)
                )
            }
            WheelSettingChip(
                icon = Icons.Default.Vibration,
                title = "Titreşim",
                enabled = isHapticEnabled,
                accent = PopcornGold,
                statusText = if (isHapticEnabled) "Açık" else "Kapalı",
                onClick = onToggleHaptic,
                clickTag = "wheel_haptic_toggle_button",
                statusTag = "wheel_haptic_status_label",
                contentDescription = "Titreşim geri bildirimi",
                modifier = Modifier.weight(1f)
            )
            WheelSettingChip(
                icon = if (isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                title = "Ses",
                enabled = isSoundEnabled,
                accent = ElectricCyan,
                statusText = if (isSoundEnabled) "Açık" else "Kapalı",
                onClick = onToggleSound,
                clickTag = "wheel_sound_toggle_button",
                statusTag = "wheel_sound_status_label",
                contentDescription = "Çark sesi",
                modifier = Modifier.weight(1f)
            )
        }
        if (shakeActive) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CinemaSurfaceVariant)
                    .testTag("shake_intensity_bar")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = shakeIntensity.coerceIn(0.04f, 1f))
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(ElectricCyan, PopcornGold)))
                )
            }
        }
    }
}

@Composable
private fun WheelSettingChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    enabled: Boolean,
    accent: Color,
    statusText: String,
    onClick: () -> Unit,
    clickTag: String,
    statusTag: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (enabled) accent.copy(alpha = 0.12f) else CinemaSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (enabled) accent.copy(alpha = 0.5f) else CinemaBorder),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClickLabel = "$title ayarını değiştir") { onClick() }
            .semantics { this.contentDescription = contentDescription }
            .testTag(clickTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) accent else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) TextPrimary else TextSecondary,
                    maxLines = 1
                )
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) accent else TextMuted,
                    maxLines = 1,
                    modifier = Modifier.testTag(statusTag)
                )
            }
        }
    }
}


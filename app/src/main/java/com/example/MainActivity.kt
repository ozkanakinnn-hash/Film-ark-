package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ThemePreferences
import com.example.data.repository.CustomWheelListRepository
import com.example.data.repository.MovieRepository
import com.example.data.repository.RoomRepository
import com.example.data.repository.WatchedMovieRepository
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.CustomListsScreen
import com.example.ui.screens.FilterScreen
import com.example.ui.screens.FlexScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoomScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.WheelScreen
import com.example.ui.components.CinematicBackdrop
import com.example.ui.theme.CinemaMotion
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DecisionViewModel
import com.example.ui.viewmodel.Screen
import com.example.widget.MovieWheelAppWidgetProvider

class MainActivity : ComponentActivity() {
    private val pendingWidgetMovieId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WatchedMovieRepository.init(applicationContext)
        CustomWheelListRepository.init(applicationContext)
        RoomRepository.init(applicationContext)
        ThemePreferences.init(applicationContext)
        MovieRepository.initDailyFeed(applicationContext)
        pendingWidgetMovieId.value = intent?.getStringExtra(MovieWheelAppWidgetProvider.EXTRA_WIDGET_MOVIE_ID)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by ThemePreferences.isDarkMode.collectAsState()
            val widgetMovieId by pendingWidgetMovieId
            MyApplicationTheme(darkTheme = isDarkTheme) {
                DecisionApp(
                    initialWidgetMovieId = widgetMovieId,
                    onConsumeWidgetMovieId = { pendingWidgetMovieId.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(MovieWheelAppWidgetProvider.EXTRA_WIDGET_MOVIE_ID)?.let {
            pendingWidgetMovieId.value = it
        }
    }
}

@Composable
fun DecisionApp(
    viewModel: DecisionViewModel = viewModel(),
    initialWidgetMovieId: String? = null,
    onConsumeWidgetMovieId: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val room by viewModel.currentRoom.collectAsState()

    LaunchedEffect(initialWidgetMovieId) {
        if (!initialWidgetMovieId.isNullOrBlank()) {
            viewModel.openMovieFromWidget(initialWidgetMovieId)
            onConsumeWidgetMovieId()
        }
    }
    val savedDecisions by viewModel.savedDecisions.collectAsState()
    val watchedMovies by viewModel.watchedMovies.collectAsState()
    val watchedMovieIds by viewModel.watchedMovieIds.collectAsState()
    val excludedWheelMovieIds by viewModel.excludedWheelMovieIds.collectAsState()
    val isSoloMode by viewModel.isSoloMode.collectAsState()
    val selectedMoods by viewModel.selectedMoods.collectAsState()
    val selectedPlatforms by viewModel.selectedPlatforms.collectAsState()
    val partnerMoods by viewModel.partnerMoods.collectAsState()
    val partnerPlatforms by viewModel.partnerPlatforms.collectAsState()
    val activeWheelMovies by viewModel.activeWheelMovies.collectAsState()
    val wheelPoolTotal by viewModel.wheelPoolTotal.collectAsState()
    val spinEvent by viewModel.spinEvent.collectAsState()
    val isSpinning by viewModel.isSpinning.collectAsState()
    val selectedMovie by viewModel.selectedMovie.collectAsState()
    val isVetoedOnce by viewModel.isVetoedOnce.collectAsState()
    val isDecisionLocked by viewModel.isDecisionLocked.collectAsState()
    val showStoryModal by viewModel.showStoryModal.collectAsState()
    val roomCodeInput by viewModel.roomCodeInput.collectAsState()
    val isAdvancedFilterMode by viewModel.isAdvancedFilterMode.collectAsState()
    val selectedGenres by viewModel.selectedGenres.collectAsState()
    val selectedAudioOptions by viewModel.selectedAudioOptions.collectAsState()
    val selectedYearOptionId by viewModel.selectedYearOptionId.collectAsState()
    val minYear by viewModel.minYear.collectAsState()
    val maxYear by viewModel.maxYear.collectAsState()
    val minRating by viewModel.minRating.collectAsState()
    val feedbackToast by viewModel.feedbackToast.collectAsState()
    val compareState by viewModel.compareState.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val favoriteMovieIds by viewModel.favoriteMovieIds.collectAsState()
    val customWheelLists by viewModel.customWheelLists.collectAsState()
    val activeCustomList by viewModel.activeCustomList.collectAsState()
    val dailyNewReleases by viewModel.dailyNewReleases.collectAsState()
    val dailyFeedDateLabel by viewModel.dailyFeedDateLabel.collectAsState()
    val weeklyTrendsState by viewModel.weeklyTrendsState.collectAsState()
    val historyDashboardStats by viewModel.historyDashboardStats.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackToast) {
        feedbackToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(currentScreen, selectedMovie) {
        if (currentScreen == Screen.FLEX && selectedMovie == null) {
            viewModel.navigateTo(Screen.WHEEL)
        }
    }

    CinematicBackdrop {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { screenTransition() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_transition"
        ) { screen ->
          Box(modifier = Modifier.fillMaxSize()) {
            when (screen) {
                Screen.HOME -> {
                    HomeScreen(
                        savedDecisions = savedDecisions,
                        watchedCount = watchedMovies.size,
                        favoriteCount = favoriteMovieIds.size,
                        customWheelListsCount = customWheelLists.size,
                        isDarkTheme = isDarkTheme,
                        dailyNewReleases = dailyNewReleases,
                        dailyFeedDateLabel = dailyFeedDateLabel,
                        onRefreshNewReleases = { viewModel.refreshDailyMovieFeed(forceRefresh = true) },
                        onNewReleaseMovieClick = { movie -> viewModel.openMovieInWheelFromShowcase(movie) },
                        weeklyTrendsState = weeklyTrendsState,
                        onRefreshTrends = { viewModel.refreshWeeklyTrends(forceRefresh = true) },
                        onToggleTheme = { viewModel.toggleTheme() },
                        onSoloClick = { viewModel.startSoloFlow() },
                        onRoomClick = { viewModel.openRoomHub() },
                        onCompareClick = { viewModel.openCompareMode() },
                        onCustomListsClick = { viewModel.openCustomLists() },
                        onHistoryClick = { viewModel.navigateTo(Screen.HISTORY) },
                        onProfileClick = { viewModel.navigateTo(Screen.PROFILE) },
                        onStatsClick = { viewModel.navigateTo(Screen.STATS) },
                        onSavedDecisionClick = { movie -> viewModel.selectMovieForFlex(movie) }
                    )
                }

                Screen.ROOM_HUB -> {
                    RoomScreen(
                        room = room,
                        roomCodeInput = roomCodeInput,
                        onCodeInputChange = { viewModel.setRoomCodeInput(it) },
                        onCreateRoom = { viewModel.createRoom() },
                        onJoinRoom = { viewModel.joinRoom(it) },
                        onSimulatePartner = { viewModel.simulatePartnerJoin() },
                        onProceedToFilters = { viewModel.navigateTo(Screen.FILTERING) },
                        onBack = { viewModel.resetToHome() }
                    )
                }

                Screen.FILTERING -> {
                    FilterScreen(
                        isSoloMode = isSoloMode,
                        selectedMoods = selectedMoods,
                        selectedPlatforms = selectedPlatforms,
                        partnerMoods = partnerMoods,
                        partnerPlatforms = partnerPlatforms,
                        isAdvancedFilterMode = isAdvancedFilterMode,
                        selectedGenres = selectedGenres,
                        selectedAudioOptions = selectedAudioOptions,
                        selectedYearOptionId = selectedYearOptionId,
                        minYear = minYear,
                        maxYear = maxYear,
                        minRating = minRating,
                        watchedMovieIds = excludedWheelMovieIds,
                        customWheelLists = customWheelLists,
                        onSpinCustomList = { listId -> viewModel.spinCustomWheelList(listId) },
                        onOpenCustomLists = { viewModel.openCustomLists() },
                        onToggleFilterMode = { viewModel.setFilterMode(it) },
                        onToggleMood = { mood, isPartner -> viewModel.toggleMood(mood, isPartner) },
                        onTogglePlatform = { platform, isPartner -> viewModel.togglePlatform(platform, isPartner) },
                        onGenreToggle = { viewModel.toggleGenre(it) },
                        onClearGenres = { viewModel.clearGenres() },
                        onToggleAudioOption = { viewModel.toggleAudioOption(it) },
                        onClearAudioOptions = { viewModel.clearAudioOptions() },
                        onSpinCultOtherClick = { viewModel.spinCultOtherCategory() },
                        onQuickSelectCultMovie = { movie -> viewModel.openMovieInWheelFromShowcase(movie) },
                        onYearOptionSelected = { viewModel.setYearOptionId(it.id) },
                        onYearRangeChange = { min, max -> viewModel.setYearRange(min, max) },
                        onRatingChange = { viewModel.setMinRating(it) },
                        onResetAdvancedFilters = { viewModel.resetAdvancedFilters() },
                        onProceedToWheel = { viewModel.proceedToWheel() },
                        onBack = {
                            if (isSoloMode) viewModel.navigateTo(Screen.HOME)
                            else viewModel.navigateTo(Screen.ROOM_HUB)
                        }
                    )
                }

                Screen.CUSTOM_LISTS -> {
                    CustomListsScreen(
                        customLists = customWheelLists,
                        onSpinList = { listId -> viewModel.spinCustomWheelList(listId) },
                        onSaveList = { editingId, name, emoji, desc, movies, colorHex ->
                            viewModel.saveCustomWheelList(editingId, name, emoji, desc, movies, colorHex)
                        },
                        onDeleteList = { listId -> viewModel.deleteCustomWheelList(listId) },
                        onRestorePresets = { viewModel.restoreStarterCustomLists() },
                        onBack = { viewModel.navigateTo(Screen.HOME) }
                    )
                }

                Screen.WHEEL -> {
                    WheelScreen(
                        movies = activeWheelMovies,
                        spinEvent = spinEvent,
                        isSpinning = isSpinning,
                        selectedMovie = selectedMovie,
                        isVetoedOnce = isVetoedOnce,
                        isDecisionLocked = isDecisionLocked,
                        showStoryModal = showStoryModal,
                        room = room,
                        activeCustomList = activeCustomList,
                        selectedGenres = selectedGenres,
                        selectedAudioOptions = selectedAudioOptions,
                        minYear = minYear,
                        maxYear = maxYear,
                        minRating = minRating,
                        watchedMovieIds = watchedMovieIds,
                        excludedWheelMovieIds = excludedWheelMovieIds,
                        favoriteMovieIds = favoriteMovieIds,
                        poolTotal = wheelPoolTotal,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { viewModel.toggleTheme() },
                        onWheelMinRatingChange = { viewModel.updateWheelMinRating(it) },
                        onToggleFavorite = { viewModel.toggleFavoriteMovie(it.id, it.title) },
                        onOpenCustomLists = { viewModel.openCustomLists() },
                        onSpinClick = { viewModel.spinWheel() },
                        onSpinCompleted = { eventId -> viewModel.completeSpin(eventId) },
                        onVetoClick = { viewModel.applyVeto() },
                        onMarkAsWatched = { viewModel.markMovieAsWatched(it) },
                        onSelectAlternative = { viewModel.chooseAlternativeMovie(it) },
                        onOpenStoryModal = { viewModel.navigateTo(Screen.FLEX) },
                        onCloseStoryModal = { viewModel.closeStoryModal() },
                        onBack = {
                            if (activeCustomList != null) viewModel.navigateTo(Screen.CUSTOM_LISTS)
                            else viewModel.navigateTo(Screen.FILTERING)
                        }
                    )
                }

                Screen.COMPARE -> {
                    CompareScreen(
                        compareState = compareState,
                        watchedMovieIds = watchedMovieIds,
                        onPickWinner = { keepLeft -> viewModel.pickCompareWinner(keepLeft) },
                        onSkipPair = { viewModel.skipComparePair() },
                        onLockChampion = { movie -> viewModel.lockCompareChampion(movie) },
                        onGenreFilterSelected = { genre -> viewModel.startCompareSession(genre) },
                        onRestartDuel = { viewModel.startCompareSession(compareState.selectedGenreFilter) },
                        onShareChampion = { movie -> viewModel.selectMovieForFlex(movie) },
                        onMarkAsWatched = { movie -> viewModel.markMovieAsWatched(movie) },
                        onBack = { viewModel.navigateTo(Screen.HOME) }
                    )
                }

                Screen.FLEX -> {
                    selectedMovie?.let { movie ->
                        FlexScreen(
                            movie = movie,
                            isVetoedOnce = isVetoedOnce,
                            roomCode = room?.roomCode,
                            onBack = { viewModel.navigateTo(Screen.WHEEL) }
                        )
                    }
                }

                Screen.HISTORY -> {
                    HistoryScreen(
                        savedDecisions = savedDecisions,
                        favoriteMovieIds = favoriteMovieIds,
                        watchedMovieIds = watchedMovieIds,
                        watchedMovies = watchedMovies,
                        historyDashboard = historyDashboardStats,
                        onOpenDashboard = { viewModel.navigateTo(Screen.STATS) },
                        onMovieFlexClick = { viewModel.selectMovieForFlex(it) },
                        onDeleteDecision = { viewModel.deleteSavedDecision(it) },
                        onClearHistory = { viewModel.clearSavedHistory() },
                        onBack = { viewModel.navigateTo(Screen.HOME) }
                    )
                }

                Screen.PROFILE -> {
                    ProfileScreen(
                        watchedMovies = watchedMovies,
                        savedDecisions = savedDecisions,
                        favoriteMovieIds = favoriteMovieIds,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { viewModel.toggleTheme() },
                        onToggleFavorite = { id, title -> viewModel.toggleFavoriteMovie(id, title) },
                        onToggleRewatch = { viewModel.toggleRewatchMovie(it) },
                        onSaveNote = { id, note -> viewModel.updateWatchedMovieNote(id, note) },
                        onRemoveWatched = { viewModel.removeWatchedMovie(it) },
                        onMovieFlexClick = { viewModel.selectMovieForFlex(it) },
                        onBack = { viewModel.navigateTo(Screen.HOME) }
                    )
                }

                Screen.STATS -> {
                    StatsScreen(
                        savedDecisions = savedDecisions,
                        favoriteMovieIds = favoriteMovieIds,
                        watchedMovies = watchedMovies,
                        historyDashboard = historyDashboardStats,
                        onMovieClick = { viewModel.selectMovieForFlex(it) },
                        onBack = { viewModel.navigateTo(Screen.HOME) }
                    )
                }
            }
          }
        }
    }
    }
}

/**
 * Ekranların gezinme derinliği. Derinlik artıyorsa "ileri", azalıyorsa "geri" geçişi oynatılır.
 */
private val Screen.depth: Int
    get() = when (this) {
        Screen.HOME -> 0
        Screen.ROOM_HUB, Screen.CUSTOM_LISTS, Screen.COMPARE, Screen.HISTORY, Screen.PROFILE -> 1
        Screen.FILTERING, Screen.STATS -> 2
        Screen.WHEEL -> 3
        Screen.FLEX -> 4
    }

/**
 * Material "shared axis X" esintili geçiş: yeni ekran kısa bir mesafeden kayarak ve belirerek gelir,
 * eski ekran ters yöne hafifçe kayıp hızla söner. Karar kartı (FLEX) ise sahneye "yakınlaşarak" açılır.
 */
private fun AnimatedContentTransitionScope<Screen>.screenTransition(): ContentTransform {
    val from = initialState
    val to = targetState
    val enterDuration = CinemaMotion.DurationLong
    val exitDuration = CinemaMotion.DurationMedium

    if (to == Screen.FLEX || from == Screen.FLEX) {
        val opening = to == Screen.FLEX
        return (fadeIn(tween(enterDuration, delayMillis = 60, easing = CinemaMotion.EmphasizedDecelerate)) +
            scaleIn(
                initialScale = if (opening) 0.88f else 1.06f,
                animationSpec = tween(enterDuration, easing = CinemaMotion.EmphasizedDecelerate)
            )) togetherWith
            (fadeOut(tween(exitDuration / 2, easing = CinemaMotion.EmphasizedAccelerate)) +
                scaleOut(
                    targetScale = if (opening) 1.04f else 0.92f,
                    animationSpec = tween(exitDuration, easing = CinemaMotion.EmphasizedAccelerate)
                ))
    }

    val forward = to.depth >= from.depth
    val direction = if (forward) 1 else -1
    return (slideInHorizontally(tween(enterDuration, easing = CinemaMotion.EmphasizedDecelerate)) { full ->
        direction * full / 5
    } + fadeIn(tween(enterDuration, delayMillis = 50, easing = CinemaMotion.EmphasizedDecelerate))) togetherWith
        (slideOutHorizontally(tween(exitDuration, easing = CinemaMotion.EmphasizedAccelerate)) { full ->
            -direction * full / 8
        } + fadeOut(tween(exitDuration / 2, easing = CinemaMotion.EmphasizedAccelerate)))
}

package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ThemePreferences
import com.example.data.local.WatchedMovieEntity
import com.example.data.model.AudioLanguageOption
import com.example.data.model.CustomWheelList
import com.example.data.model.HistoryDashboardSummary
import com.example.data.model.Mood
import com.example.data.model.Movie
import com.example.data.model.kindNoun
import com.example.data.model.contentNoun
import com.example.data.model.OTHER_PLATFORM_NAME
import com.example.data.repository.CustomWheelListRepository
import com.example.data.repository.MovieRepository
import com.example.data.repository.WheelPoolSampler
import com.example.data.repository.SmartWheelLists
import com.example.data.repository.HighlightsRepository
import com.example.data.repository.RoomRepository
import com.example.data.repository.SavedDecision
import com.example.data.repository.WatchedMovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    FILTERING,
    CUSTOM_LISTS,
    WHEEL,
    COMPARE,
    HISTORY,
    PROFILE,
    STATS,
    FLEX,
    // Alt menü sekmeleri
    UPCOMING,
    POPULAR,
    SERIES,
    FORUM,
    SETTINGS,
    ONBOARDING;

    /** Alt menüde görünen sekmeler. */
    val isBottomTab: Boolean get() = this == HOME || this == UPCOMING || this == POPULAR || this == SERIES || this == FORUM
}

data class SpinEvent(
    val id: Long,
    val winner: Movie,
    val targetAngle: Float
)

data class CompareUiState(
    val leftMovie: Movie? = null,
    val rightMovie: Movie? = null,
    val queue: List<Movie> = emptyList(),
    val roundNumber: Int = 1,
    val totalRounds: Int = 5,
    val selectedGenreFilter: String? = null,
    val champion: Movie? = null,
    val currentStreakMovieId: String? = null,
    val currentStreakCount: Int = 0
)

class DecisionViewModel(
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(
        if (com.example.data.local.OnboardingPreferences.shouldShow.value) Screen.ONBOARDING else Screen.HOME
    )
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val savedDecisions: StateFlow<List<SavedDecision>> = RoomRepository.savedDecisions
    val historyDashboardStats: StateFlow<HistoryDashboardSummary> = RoomRepository.historyDashboardStats
    val watchedMovies: StateFlow<List<WatchedMovieEntity>> = WatchedMovieRepository.watchedMovies
    val watchedMovieIds: StateFlow<Set<String>> = WatchedMovieRepository.watchedMovieIds
    val excludedWheelMovieIds: StateFlow<Set<String>> = WatchedMovieRepository.excludedWheelMovieIds
    val favoriteMovieIds: StateFlow<Set<String>> = WatchedMovieRepository.favoriteMovieIds
    val customWheelLists: StateFlow<List<CustomWheelList>> = CustomWheelListRepository.customLists

    /** Hazır çarklar (Şu An Popüler, Popüler Diziler, Bunu Niye İzledim?); uzaktan katalog gelince yenilenir. */
    val smartWheelLists: StateFlow<List<CustomWheelList>> =
        com.example.data.repository.RemoteCatalogRepository.catalog
            .map { SmartWheelLists.all() }
            .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, SmartWheelLists.all())
    val isDarkTheme: StateFlow<Boolean> = ThemePreferences.isDarkMode

    private val _activeCustomList = MutableStateFlow<CustomWheelList?>(null)
    val activeCustomList: StateFlow<CustomWheelList?> = _activeCustomList.asStateFlow()

    val dailyNewReleases: StateFlow<List<Movie>> = MovieRepository.dailyUpdatedMovies
    val dailyFeedDateLabel: StateFlow<String> = MovieRepository.dailyFeedLastUpdated



    // Host / User Filters
    private val _selectedMoods = MutableStateFlow<Set<String>>(setOf(Mood.EASY_WATCH.id))
    val selectedMoods: StateFlow<Set<String>> = _selectedMoods.asStateFlow()

    private val _selectedPlatforms = MutableStateFlow<Set<String>>(setOf("Netflix", "Prime Video"))
    val selectedPlatforms: StateFlow<Set<String>> = _selectedPlatforms.asStateFlow()

    // Advanced Filter Criteria (Tür, Yıl, Puan)
    private val _isAdvancedFilterMode = MutableStateFlow(false)
    val isAdvancedFilterMode: StateFlow<Boolean> = _isAdvancedFilterMode.asStateFlow()

    private val _selectedGenres = MutableStateFlow<Set<String>>(emptySet())
    val selectedGenres: StateFlow<Set<String>> = _selectedGenres.asStateFlow()

    private val _contentKindFilter = MutableStateFlow(com.example.data.model.ContentKindFilter.MOVIES)
    val contentKindFilter: StateFlow<com.example.data.model.ContentKindFilter> = _contentKindFilter.asStateFlow()

    fun setContentKindFilter(filter: com.example.data.model.ContentKindFilter) {
        _contentKindFilter.value = filter
    }

    private val _selectedAudioOptions = MutableStateFlow<Set<String>>(emptySet())
    val selectedAudioOptions: StateFlow<Set<String>> = _selectedAudioOptions.asStateFlow()

    private val _selectedYearOptionId = MutableStateFlow("all")
    val selectedYearOptionId: StateFlow<String> = _selectedYearOptionId.asStateFlow()

    private val _minYear = MutableStateFlow(1970)
    val minYear: StateFlow<Int> = _minYear.asStateFlow()

    private val _maxYear = MutableStateFlow(2026)
    val maxYear: StateFlow<Int> = _maxYear.asStateFlow()

    private val _minRating = MutableStateFlow(0.0)
    val minRating: StateFlow<Double> = _minRating.asStateFlow()

    private var activeSpinModeOverride: String? = null

    // Active Movies in Wheel
    private val _activeWheelMovies = MutableStateFlow<List<Movie>>(emptyList())
    val activeWheelMovies: StateFlow<List<Movie>> = _activeWheelMovies.asStateFlow()

    /** "Daha Sonra İzle" listesindeki film kimlikleri (en yeni başta). */
    val watchLaterIds: StateFlow<List<String>> = com.example.data.repository.WatchLaterRepository.ids

    fun toggleWatchLater(movie: Movie) {
        val added = com.example.data.repository.WatchLaterRepository.toggle(movie.id)
        _feedbackToast.value = if (added) "🔖 '${movie.title}' daha sonra izle listesine eklendi"
        else "'${movie.title}' listeden çıkarıldı"
    }

    /** Çarkın arkasındaki tam uygun film havuzu (çarkta en fazla 12 tanesi gösterilir). */
    private var fullWheelPool: List<Movie> = emptyList()
    private val _wheelPoolTotal = MutableStateFlow(0)
    val wheelPoolTotal: StateFlow<Int> = _wheelPoolTotal.asStateFlow()

    /** Tam havuzu saklar ve çarkta gösterilecek karışık örneklemi döndürür. */
    private fun sampleForWheel(pool: List<Movie>, mustInclude: Movie? = null): List<Movie> {
        fullWheelPool = pool
        _wheelPoolTotal.value = pool.distinctBy { it.id }.size
        val profile = com.example.data.repository.TasteProfileRepository.snapshot()
        return WheelPoolSampler.sample(pool, mustInclude, weightOf = profile::weightOf)
    }

    // Wheel spin state (animated in Compose UI with MonotonicFrameClock)
    private val _spinEvent = MutableStateFlow<SpinEvent?>(null)
    val spinEvent: StateFlow<SpinEvent?> = _spinEvent.asStateFlow()
    private var spinCounter = 0L

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private val _selectedMovie = MutableStateFlow<Movie?>(null)
    val selectedMovie: StateFlow<Movie?> = _selectedMovie.asStateFlow()

    private val _isVetoedOnce = MutableStateFlow(false)
    val isVetoedOnce: StateFlow<Boolean> = _isVetoedOnce.asStateFlow()

    private val _isDecisionLocked = MutableStateFlow(false)
    val isDecisionLocked: StateFlow<Boolean> = _isDecisionLocked.asStateFlow()



    private val _feedbackToast = MutableStateFlow<String?>(null)
    val feedbackToast: StateFlow<String?> = _feedbackToast.asStateFlow()

    private val _compareState = MutableStateFlow(CompareUiState())
    val compareState: StateFlow<CompareUiState> = _compareState.asStateFlow()

    fun refreshDailyMovieFeed(forceRefresh: Boolean = true) {
        MovieRepository.syncDailyMovieUpdates(forceRefresh = forceRefresh)
        if (forceRefresh) {
            _feedbackToast.value = "🔥 Öne çıkanlar güncellendi (${dailyNewReleases.value.size} film)"
        }
    }

    fun toggleTheme() {
        ThemePreferences.toggleTheme()
    }

    fun navigateTo(screen: Screen) {
        if (screen != Screen.WHEEL && _isSpinning.value) {
            _isSpinning.value = false
            _spinEvent.value = null
        }
        _currentScreen.value = screen
    }

    fun startSoloFlow() {
        _isVetoedOnce.value = false
        _isDecisionLocked.value = false
        _currentScreen.value = Screen.FILTERING
    }

    fun toggleMood(moodId: String) {
        val current = _selectedMoods.value.toMutableSet()
        if (current.contains(moodId)) {
            if (current.size > 1) current.remove(moodId)
        } else {
            current.add(moodId)
        }
        _selectedMoods.value = current
    }

    fun togglePlatform(platformName: String) {
        val current = _selectedPlatforms.value.toMutableSet()
        if (current.contains(platformName)) {
            if (current.size > 1) current.remove(platformName)
        } else {
            current.add(platformName)
        }
        _selectedPlatforms.value = current
    }

    fun toggleGenre(genre: String) {
        val current = _selectedGenres.value.toMutableSet()
        if (current.contains(genre)) {
            current.remove(genre)
        } else {
            current.add(genre)
        }
        _selectedGenres.value = current
    }

    fun clearGenres() {
        _selectedGenres.value = emptySet()
    }

    fun toggleAudioOption(audioId: String) {
        val current = _selectedAudioOptions.value.toMutableSet()
        if (current.contains(audioId)) {
            current.remove(audioId)
        } else {
            current.add(audioId)
        }
        _selectedAudioOptions.value = current
    }

    fun clearAudioOptions() {
        _selectedAudioOptions.value = emptySet()
    }

    fun setYearOptionId(optionId: String) {
        _selectedYearOptionId.value = optionId
        val preset = com.example.ui.components.YearFilterOptions.find { it.id == optionId }
        if (preset != null) {
            _minYear.value = preset.minYear
            _maxYear.value = preset.maxYear
        }
    }

    fun setYearRange(min: Int, max: Int) {
        val clampedMin = min.coerceIn(1970, 2026)
        val clampedMax = max.coerceIn(clampedMin, 2026)
        _minYear.value = clampedMin
        _maxYear.value = clampedMax
        val matchingPreset = com.example.ui.components.YearFilterOptions.find {
            it.minYear == clampedMin && it.maxYear == clampedMax
        }
        _selectedYearOptionId.value = matchingPreset?.id ?: "custom"
    }

    fun setMinRating(rating: Double) {
        _minRating.value = rating
    }

    fun setFilterMode(isAdvanced: Boolean) {
        _isAdvancedFilterMode.value = isAdvanced
    }

    fun resetAdvancedFilters() {
        _selectedGenres.value = emptySet()
        _selectedAudioOptions.value = emptySet()
        _selectedYearOptionId.value = "all"
        _minYear.value = 1970
        _maxYear.value = 2026
        _minRating.value = 0.0
    }

    fun updateWheelMinRating(minImdb: Double) {
        if (_isSpinning.value) return
        _minRating.value = minImdb
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        val activeCustom = _activeCustomList.value
        val filtered = if (activeCustom != null) {
            val pool = activeCustom.movies.filterNot { excludedIds.contains(it.id) }.ifEmpty { activeCustom.movies }
            if (minImdb > 0.0) {
                pool.filter { it.imdb >= minImdb }.ifEmpty { pool }
            } else {
                pool
            }
        } else {
            computeFilteredWheelMovies(minRatingOverride = minImdb)
        }
        val shown = if (activeCustom != null) {
            fullWheelPool = filtered
            _wheelPoolTotal.value = filtered.size
            filtered
        } else {
            sampleForWheel(filtered)
        }
        _activeWheelMovies.value = shown

        RoomRepository.prepareSoloSpin(
            _selectedMoods.value,
            _selectedPlatforms.value,
            shown,
            if (activeCustom != null) emptySet() else excludedIds
        )

        val currentSelected = _selectedMovie.value
        if (currentSelected != null && minImdb > 0.0 && currentSelected.imdb < minImdb) {
            _selectedMovie.value = null
        }

        _feedbackToast.value = if (minImdb <= 0.0) {
            "Puan filtresi kaldırıldı • Havuzda ${filtered.size} ${filtered.contentNoun().lowercase()}"
        } else {
            "★ ${minImdb}+ puan filtresi uygulandı • Havuzda ${filtered.size} ${filtered.contentNoun().lowercase()}"
        }
    }

    private fun computeFilteredWheelMovies(minRatingOverride: Double = _minRating.value): List<Movie> {
        val effectivePlatforms = _selectedPlatforms.value
        val effectiveMoods = if (_isAdvancedFilterMode.value) emptySet() else _selectedMoods.value

        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        val result = MovieRepository.filterMoviesWithCriteria(
            moods = effectiveMoods,
            genres = _selectedGenres.value,
            minYear = _minYear.value,
            maxYear = _maxYear.value,
            minRating = minRatingOverride,
            platforms = effectivePlatforms,
            excludedMovieIds = excludedIds,
            audioOptions = _selectedAudioOptions.value,
            contentKinds = _contentKindFilter.value
        ).movies

        val kindMovies = MovieRepository.allMovies.filter { _contentKindFilter.value.accepts(it) }
            .ifEmpty { MovieRepository.allMovies }
        val strictlyRated = if (minRatingOverride > 0.0) {
            result.filter { it.imdb >= minRatingOverride }.ifEmpty {
                kindMovies
                    .filter { !excludedIds.contains(it.id) && it.imdb >= minRatingOverride }
                    .ifEmpty { kindMovies.filter { it.imdb >= minRatingOverride } }
            }
        } else {
            result
        }

        return strictlyRated.ifEmpty {
            kindMovies
                .filterNot { excludedIds.contains(it.id) }
                .ifEmpty { kindMovies }
                .take(8)
        }
    }

    fun proceedToWheel() {
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        val filtered = sampleForWheel(computeFilteredWheelMovies())

        activeSpinModeOverride = null
        _activeCustomList.value = null
        _activeWheelMovies.value = filtered

        RoomRepository.prepareSoloSpin(
            _selectedMoods.value,
            _selectedPlatforms.value,
            filtered,
            excludedIds
        )
        _selectedMovie.value = null
        _isDecisionLocked.value = false
        _isSpinning.value = false
        _spinEvent.value = null
        _currentScreen.value = Screen.WHEEL
    }

    fun spinCultOtherCategory() {
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        val cultMovies = MovieRepository.getCultAndOtherMovies(
            excludedMovieIds = excludedIds,
            audioOptions = _selectedAudioOptions.value
        ).ifEmpty {
            MovieRepository.getCultAndOtherMovies(
                excludedMovieIds = emptySet(),
                audioOptions = _selectedAudioOptions.value
            )
        }.ifEmpty {
            MovieRepository.getCultAndOtherMovies()
        }
        _isVetoedOnce.value = false
        _isDecisionLocked.value = false
        _activeCustomList.value = null
        activeSpinModeOverride = "Diğer (Kült & Çok İzlenenler)"
        val cultTotal = cultMovies.size
        val shownCult = sampleForWheel(cultMovies)
        _activeWheelMovies.value = shownCult
        RoomRepository.prepareSoloSpin(
            moods = setOf(Mood.CULT_AWARD.id),
            platforms = setOf(OTHER_PLATFORM_NAME),
            customMovies = shownCult,
            excludedMovieIds = emptySet()
        )
        _selectedMovie.value = null
        _isSpinning.value = false
        _spinEvent.value = null
        _feedbackToast.value = "🏛️ Kült & Çok İzlenenler (${cultTotal} film) çarka alındı!"
        _currentScreen.value = Screen.WHEEL
    }

    fun openMovieInWheelFromShowcase(movie: Movie) {
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        val candidates = (listOf(movie) + dailyNewReleases.value + MovieRepository.allMovies)
            .distinctBy { it.id }
            .filterNot { it.id != movie.id && excludedIds.contains(it.id) }
        val basePool = sampleForWheel(candidates, mustInclude = movie)
        _isVetoedOnce.value = false
        _isDecisionLocked.value = false
        _activeCustomList.value = null
        activeSpinModeOverride = null
        _activeWheelMovies.value = basePool
        RoomRepository.prepareSoloSpin(
            moods = setOf(movie.moodTag),
            platforms = movie.platforms.toSet().ifEmpty { setOf(OTHER_PLATFORM_NAME) },
            customMovies = basePool,
            excludedMovieIds = excludedIds
        )
        _selectedMovie.value = movie
        _isSpinning.value = false
        _spinEvent.value = null
        _currentScreen.value = Screen.WHEEL
    }

    fun openCustomLists() {
        _currentScreen.value = Screen.CUSTOM_LISTS
    }

    fun saveCustomWheelList(
        editingListId: String?,
        name: String,
        emoji: String,
        description: String,
        movies: List<Movie>,
        accentColorHex: Long
    ): CustomWheelList {
        val saved = CustomWheelListRepository.saveCustomList(
            editingListId = editingListId,
            name = name,
            emoji = emoji,
            description = description,
            movies = movies,
            accentColorHex = accentColorHex
        )
        if (_activeCustomList.value?.id == saved.id) {
            _activeCustomList.value = saved
        }
        _feedbackToast.value = "✅ '${saved.name}' çark listesi kaydedildi!"
        return saved
    }

    fun deleteCustomWheelList(listId: String) {
        val existingName = CustomWheelListRepository.getListById(listId)?.name ?: "Liste"
        CustomWheelListRepository.deleteList(listId)
        if (_activeCustomList.value?.id == listId) {
            _activeCustomList.value = null
        }
        _feedbackToast.value = "🗑️ '$existingName' silindi."
    }

    fun restoreStarterCustomLists() {
        CustomWheelListRepository.restoreStarterPresets()
        _feedbackToast.value = "✨ Hazır kategoriler yenilendi!"
    }

    fun spinCustomWheelList(listId: String) {
        val list = CustomWheelListRepository.getListById(listId) ?: return
        if (list.movies.isEmpty()) {
            _feedbackToast.value = "Bu listede henüz film yok; önce birkaç film ekle!"
            return
        }
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        val unwatched = list.movies.filterNot { excludedIds.contains(it.id) }
        val pool = unwatched.ifEmpty { list.movies }
        val effectiveExcluded = if (unwatched.isEmpty()) emptySet() else excludedIds
        _isVetoedOnce.value = false
        _isDecisionLocked.value = false
        activeSpinModeOverride = null
        _activeCustomList.value = list
        // Hazır çarklar çok kalabalık olabilir: okunaklı kalsın diye 12 dilim örneklenir
        val shown = if (SmartWheelLists.isSmart(list.id)) sampleForWheel(pool) else pool
        fullWheelPool = pool
        _wheelPoolTotal.value = pool.size
        _activeWheelMovies.value = shown
        RoomRepository.prepareSoloSpin(
            moods = _selectedMoods.value,
            platforms = _selectedPlatforms.value,
            customMovies = shown,
            excludedMovieIds = effectiveExcluded
        )
        _selectedMovie.value = null
        _isSpinning.value = false
        _spinEvent.value = null
        _currentScreen.value = Screen.WHEEL
    }

    fun spinWheel() {
        if (_isSpinning.value) return
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value

        // Karar verilmiş bir çark yeniden çevrilirken dilimleri büyük havuzdan tazele
        val isRespin = _selectedMovie.value != null && !_isVetoedOnce.value &&
            (_activeCustomList.value == null || SmartWheelLists.isSmart(_activeCustomList.value?.id))
        if (isRespin && fullWheelPool.size > WheelPoolSampler.MAX_SLICES) {
            val freshPool = fullWheelPool.filterNot { excludedIds.contains(it.id) }
            if (freshPool.size > WheelPoolSampler.MAX_SLICES) {
                val profile = com.example.data.repository.TasteProfileRepository.snapshot()
                _activeWheelMovies.value = WheelPoolSampler.sample(freshPool, weightOf = profile::weightOf)
            }
        }
        val unwatchedActive = _activeWheelMovies.value.filterNot { excludedIds.contains(it.id) }
        _activeWheelMovies.value = unwatchedActive.ifEmpty {
            MovieRepository.allMovies
                .filterNot { excludedIds.contains(it.id) }
                .ifEmpty { MovieRepository.allMovies }
                .take(8)
        }
        if (RoomRepository.currentRoom.value == null) {
            RoomRepository.prepareSoloSpin(
                _selectedMoods.value,
                _selectedPlatforms.value,
                _activeWheelMovies.value,
                excludedIds
            )
        }

        // Kazanan, ekranda çizilen dilimlerden (aynı sırayla) seçilir: ibre ile sonuç her zaman eşleşir
        val result = RoomRepository.triggerSpin(excludedIds, wheelSlices = _activeWheelMovies.value) ?: return
        val winner = result.first
        val targetAngle = result.second

        _isSpinning.value = true
        _selectedMovie.value = null
        spinCounter += 1L
        _spinEvent.value = SpinEvent(
            id = spinCounter,
            winner = winner,
            targetAngle = targetAngle
        )
    }

    fun completeSpin(eventId: Long) {
        val currentEvent = _spinEvent.value ?: return
        if (currentEvent.id != eventId) return

        _selectedMovie.value = currentEvent.winner
        _isSpinning.value = false
        _spinEvent.value = null
        val customMode = _activeCustomList.value?.let { "Özel Çark (${it.name})" } ?: activeSpinModeOverride
        RoomRepository.finishSpin(modeOverride = customMode)
        if (_isVetoedOnce.value) {
            _isDecisionLocked.value = true
        }
    }

    fun applyVeto() {
        if (_isSpinning.value || _isVetoedOnce.value) return
        val hostId = RoomRepository.currentRoom.value?.host?.id ?: "solo_user"
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        _selectedMovie.value?.let { com.example.data.repository.TasteProfileRepository.recordVeto(it) }
        _isVetoedOnce.value = true
        RoomRepository.applyVeto(hostId, excludedIds)

        // Remove vetoed movie and watched movies from wheel pool while ensuring pool stays synced & non-empty
        val vetoedId = _selectedMovie.value?.id
        val remaining = _activeWheelMovies.value.filter { it.id != vetoedId && !excludedIds.contains(it.id) }
        _activeWheelMovies.value = if (remaining.isNotEmpty()) {
            remaining
        } else {
            val synced = RoomRepository.currentRoom.value?.eligibleMovieIds
                ?.mapNotNull { MovieRepository.getMovieById(it) ?: CustomWheelListRepository.findCustomMovieById(it) }
                ?.filterNot { excludedIds.contains(it.id) }
                ?.ifEmpty { null }
            synced ?: MovieRepository.allMovies
                .filter { it.id != vetoedId && !excludedIds.contains(it.id) }
                .ifEmpty { MovieRepository.allMovies.filter { it.id != vetoedId } }
                .take(6)
        }
        _selectedMovie.value = null

        _feedbackToast.value = "🚫 VETO KULLANILDI! Çark son kez kaderi kilitliyor..."

        // Trigger immediate final spin
        spinWheel()
    }

    fun markMovieAsWatched(movie: Movie) {
        WatchedMovieRepository.markAsWatched(movie)
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value

        val remaining = _activeWheelMovies.value.filterNot { it.id == movie.id || excludedIds.contains(it.id) }
        _activeWheelMovies.value = remaining.ifEmpty {
            MovieRepository.allMovies
                .filterNot { excludedIds.contains(it.id) }
                .ifEmpty { MovieRepository.allMovies }
                .take(8)
        }
        RoomRepository.excludeMovieFromCurrentRoom(movie.id)
        _feedbackToast.value = "✓ '${movie.title}' izlenenlere eklendi, bir daha çarkta çıkmayacak!"
    }

    fun updateWatchedMovieNote(movieId: String, note: String) {
        WatchedMovieRepository.updateNote(movieId, note)
        _feedbackToast.value = if (note.isBlank()) {
            "Film notu temizlendi."
        } else {
            "📝 Kişisel film notun kaydedildi!"
        }
    }

    fun toggleRewatchMovie(movieId: String) {
        val enabled = WatchedMovieRepository.toggleRewatch(movieId) ?: return
        _feedbackToast.value = if (enabled) {
            "🔁 Tekrar izleme işaretlendi! Bu film çarka yeniden girebilir."
        } else {
            "✓ Tekrar izleme kapatıldı, film çark dışı bırakıldı."
        }
    }

    fun removeWatchedMovie(movieId: String) {
        WatchedMovieRepository.removeWatched(movieId)
        _feedbackToast.value = "Film izlenenlerden çıkarıldı, tekrar çarka girebilir."
    }

    // ---------------- Kişiselleştirme ----------------

    /** İlk açılış seçim ekranında gösterilecek tanınmış yapımlar: şu an popüler + çok bilinen film ve diziler. */
    fun onboardingCandidates(): List<Movie> {
        val popular = HighlightsRepository.popularMovies().take(30) + HighlightsRepository.popularSeries().take(24)
        val classics = MovieRepository.allMovies
            .filter { it.imdb >= 7.8 && it.tmdbId != null }
            .sortedByDescending { it.imdb }
            .take(30)
        return (popular + classics).distinctBy { it.id }
    }

    /** Seçim ekranı bitti: izlenenler çarktan çıkar, beğenilenler favori olur (türleri öne çıkar). */
    fun completeTasteOnboarding(watched: List<Movie>, liked: List<Movie>) {
        (watched + liked).distinctBy { it.id }.forEach { movie ->
            if (!WatchedMovieRepository.isWatched(movie.id)) WatchedMovieRepository.markAsWatched(movie)
        }
        liked.forEach { movie ->
            if (!WatchedMovieRepository.isFavorite(movie.id)) WatchedMovieRepository.toggleFavorite(movie.id)
        }
        com.example.data.local.OnboardingPreferences.markDone()
        refreshPersonalPicks()
        val total = (watched + liked).distinctBy { it.id }.size
        if (total > 0) _feedbackToast.value = "✨ $total seçimine göre önerilerin hazır!"
        _currentScreen.value = Screen.HOME
    }

    fun skipTasteOnboarding() {
        com.example.data.local.OnboardingPreferences.markDone()
        _currentScreen.value = Screen.HOME
    }

    private val _personalPicks = MutableStateFlow<List<Movie>>(emptyList())
    /** "Sana Özel": zevk profiline göre izlenmemiş film ve diziler (profil boşsa boş). */
    val personalPicks: StateFlow<List<Movie>> = _personalPicks.asStateFlow()

    init {
        // İzlenenler, favoriler ya da katalog değişince "Sana Özel" listesi yenilenir
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                WatchedMovieRepository.watchedMovies,
                WatchedMovieRepository.favoriteMovieIds,
                com.example.data.repository.RemoteCatalogRepository.catalog
            ) { _, _, _ -> Unit }.collect { refreshPersonalPicks() }
        }
    }

    fun refreshPersonalPicks() {
        val profile = com.example.data.repository.TasteProfileRepository.snapshot()
        if (profile.likedGenres.isEmpty()) {
            _personalPicks.value = emptyList()
            return
        }
        val watched = WatchedMovieRepository.watchedMovieIds.value
        val popularIds = (HighlightsRepository.popularMovies() + HighlightsRepository.popularSeries()).map { it.id }.toSet()
        _personalPicks.value = MovieRepository.allMovies
            .asSequence()
            .filterNot { watched.contains(it.id) }
            .map { m ->
                val popularBonus = if (m.id in popularIds) 1.25 else 1.0
                val ratingFactor = (m.imdb.coerceIn(4.0, 9.5) - 3.0) / 6.5
                m to profile.weightOf(m) * ratingFactor * popularBonus
            }
            .filter { it.second > 0.9 }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(16)
            .toList()
    }

    fun toggleFavoriteMovie(movieId: String, title: String? = null) {
        val added = WatchedMovieRepository.toggleFavorite(movieId)
        val label = title ?: MovieRepository.getMovieById(movieId)?.title ?: "Film"
        _feedbackToast.value = if (added) {
            "❤️ '$label' favorilerine eklendi!"
        } else {
            "💔 '$label' favorilerinden çıkarıldı."
        }
    }

    fun deleteSavedDecision(decisionId: String) {
        val targetTitle = _savedDecisionsTitle(decisionId)
        RoomRepository.deleteSavedDecision(decisionId)
        _feedbackToast.value = if (targetTitle != null) {
            "🗑️ '$targetTitle' seçim geçmişinden kaldırıldı."
        } else {
            "🗑️ Seçim geçmişinden kaldırıldı."
        }
    }

    fun clearSavedHistory() {
        RoomRepository.clearSavedHistory()
        _feedbackToast.value = "🧹 Tüm 'Geçmişte Ne Seçtik?' kayıtları temizlendi."
    }

    private fun _savedDecisionsTitle(decisionId: String): String? {
        return savedDecisions.value.find { it.id == decisionId }?.movie?.title
    }

    fun clearToast() {
        _feedbackToast.value = null
    }

    fun chooseAlternativeMovie(movie: Movie) {
        _selectedMovie.value = movie
        RoomRepository.selectAlternativeMovie(movie)
        _feedbackToast.value = "🎬 Alternatif seçildi: ${movie.title}"
    }

    fun openCompareMode(genreFilter: String? = _compareState.value.selectedGenreFilter) {
        startCompareSession(genreFilter = genreFilter)
        _currentScreen.value = Screen.COMPARE
    }

    fun startCompareSession(genreFilter: String? = _compareState.value.selectedGenreFilter) {
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        val genreMatchingAll = if (genreFilter.isNullOrBlank()) {
            MovieRepository.allMovies
        } else {
            MovieRepository.allMovies.filter { it.genre.contains(genreFilter, ignoreCase = true) }
                .ifEmpty { MovieRepository.allMovies }
        }
        val unwatched = genreMatchingAll.filterNot { excludedIds.contains(it.id) }
        val pool = when {
            unwatched.size >= 2 -> unwatched
            genreMatchingAll.size >= 2 -> genreMatchingAll
            else -> MovieRepository.allMovies.filterNot { excludedIds.contains(it.id) }
                .ifEmpty { MovieRepository.allMovies }
        }.shuffled().take(6)

        val left = pool.getOrNull(0)
        val right = pool.getOrNull(1)
        val queue = if (pool.size > 2) pool.drop(2) else emptyList()
        val rounds = (pool.size - 1).coerceAtLeast(1)

        _compareState.value = CompareUiState(
            leftMovie = left,
            rightMovie = right,
            queue = queue,
            roundNumber = 1,
            totalRounds = rounds,
            selectedGenreFilter = genreFilter,
            champion = null,
            currentStreakMovieId = null,
            currentStreakCount = 0
        )
    }

    fun pickCompareWinner(keepLeft: Boolean) {
        val state = _compareState.value
        if (state.champion != null) return
        val winner = (if (keepLeft) state.leftMovie else state.rightMovie) ?: return
        val newStreak = if (state.currentStreakMovieId == winner.id) state.currentStreakCount + 1 else 1

        if (state.queue.isEmpty()) {
            _selectedMovie.value = winner
            RoomRepository.recordSavedDecision(winner, "Film Karşılaştır (VS)")
            _compareState.value = state.copy(
                champion = winner,
                currentStreakMovieId = winner.id,
                currentStreakCount = newStreak
            )
            _feedbackToast.value = "🏆 Gecenin Şampiyonu: ${winner.title}!"
            return
        }

        val nextChallenger = state.queue.first()
        val remaining = state.queue.drop(1)

        _compareState.value = if (keepLeft) {
            state.copy(
                leftMovie = winner,
                rightMovie = nextChallenger,
                queue = remaining,
                roundNumber = state.roundNumber + 1,
                currentStreakMovieId = winner.id,
                currentStreakCount = newStreak
            )
        } else {
            state.copy(
                leftMovie = nextChallenger,
                rightMovie = winner,
                queue = remaining,
                roundNumber = state.roundNumber + 1,
                currentStreakMovieId = winner.id,
                currentStreakCount = newStreak
            )
        }
    }

    fun skipComparePair() {
        val state = _compareState.value
        if (state.champion != null) return
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        val currentIds = setOfNotNull(state.leftMovie?.id, state.rightMovie?.id)

        val nextQueue = if (state.queue.size >= 2) {
            state.queue
        } else {
            val fresh = MovieRepository.allMovies
                .filter { movie ->
                    val genreOk = state.selectedGenreFilter.isNullOrBlank() ||
                        movie.genre.contains(state.selectedGenreFilter, ignoreCase = true)
                    genreOk && !excludedIds.contains(movie.id) && !currentIds.contains(movie.id)
                }
                .ifEmpty {
                    MovieRepository.allMovies.filterNot { currentIds.contains(it.id) }
                }
                .shuffled()
            (state.queue + fresh).distinctBy { it.id }
        }

        if (nextQueue.size < 2) {
            _feedbackToast.value = "Bu kategoride başka yeni çift kalmadı, birini seçebilirsin!"
            return
        }

        val newLeft = nextQueue[0]
        val newRight = nextQueue[1]
        val remaining = nextQueue.drop(2)

        _compareState.value = state.copy(
            leftMovie = newLeft,
            rightMovie = newRight,
            queue = remaining,
            currentStreakMovieId = null,
            currentStreakCount = 0
        )
    }

    fun lockCompareChampion(movie: Movie) {
        val state = _compareState.value
        _selectedMovie.value = movie
        RoomRepository.recordSavedDecision(movie, "Film Karşılaştır (VS)")
        _compareState.value = state.copy(
            champion = movie,
            currentStreakMovieId = movie.id,
            currentStreakCount = (state.currentStreakCount + 1).coerceAtLeast(1)
        )
        _feedbackToast.value = "🏆 Seçim kilitlendi: ${movie.title}!"
    }

    fun selectMovieForFlex(movie: Movie) {
        _selectedMovie.value = movie
        _currentScreen.value = Screen.FLEX
    }

    fun openMovieFromWidget(movieId: String) {
        val movie = MovieRepository.getMovieById(movieId) ?: return
        val excludedIds = WatchedMovieRepository.excludedWheelMovieIds.value
        val pool = MovieRepository.allMovies
            .filterNot { excludedIds.contains(it.id) }
            .ifEmpty { MovieRepository.allMovies }
        val ensuredPool = sampleForWheel(pool, mustInclude = movie)
        _activeCustomList.value = null
        _activeWheelMovies.value = ensuredPool
        RoomRepository.prepareSoloSpin(
            moods = _selectedMoods.value,
            platforms = _selectedPlatforms.value,
            customMovies = ensuredPool,
            excludedMovieIds = excludedIds
        )
        _selectedMovie.value = movie
        _isSpinning.value = false
        _spinEvent.value = null
        _currentScreen.value = Screen.WHEEL
    }

    fun resetToHome() {
        RoomRepository.clearRoom()
        _activeCustomList.value = null
        _selectedMovie.value = null
        _isVetoedOnce.value = false
        _isDecisionLocked.value = false
        _isSpinning.value = false
        _spinEvent.value = null
        _currentScreen.value = Screen.HOME
    }
}

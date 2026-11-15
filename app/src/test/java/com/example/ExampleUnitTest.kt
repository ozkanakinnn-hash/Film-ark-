package com.example

import com.example.data.audio.WheelAudioPlayer
import com.example.data.audio.WheelPcmSynthesizer
import com.example.data.audio.WheelSoundCue
import com.example.data.audio.WheelSoundEngine
import com.example.data.local.ThemePreferences
import com.example.data.local.WatchedMovieEntity
import com.example.data.local.toEntity
import com.example.data.model.AudioLanguageOption
import com.example.data.model.Mood
import com.example.data.model.Movie
import com.example.data.model.OTHER_PLATFORM_NAME
import com.example.data.model.TmdbImageConfig
import com.example.data.model.analyzeDecisionHistory
import com.example.data.model.computeSpunAndLikedGenreStats
import com.example.data.model.computeSpunAndLikedGenreStatsFromMovies
import com.example.data.model.resolveLikedMovies
import com.example.data.repository.CustomWheelListRepository
import com.example.data.repository.MovieRepository
import com.example.data.repository.WheelPoolSampler
import com.example.data.repository.RoomRepository
import com.example.data.repository.SavedDecision
import com.example.data.repository.WatchedMovieRepository
import com.example.data.sensor.ShakeDetector
import com.example.data.share.SharePosterRenderer
import com.example.ui.components.GenreRevealStyle
import com.example.ui.components.buildMovieShareText
import com.example.ui.components.computeWatchedSummaryStats
import com.example.ui.components.isHorrorMovie
import com.example.ui.components.resolveGenreRevealStyle
import com.example.ui.theme.DarkCinemaPalette
import com.example.ui.theme.LightCinemaPalette
import com.example.ui.theme.WheelThemeManager
import com.example.ui.viewmodel.DecisionViewModel
import com.example.ui.viewmodel.Screen
import com.example.widget.WidgetMovieStateStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DecisionUnitTest {

    @Before
    fun setUp() {
        RoomRepository.clearRoom()
        RoomRepository.resetSavedDecisionsForTests()
        WatchedMovieRepository.clearAllForTests()
        CustomWheelListRepository.resetForTests()
        ThemePreferences.setDarkMode(true)
    }

    @Test
    fun testMovieRepository_hasMoviesAndFilters() {
        val all = MovieRepository.allMovies
        assertTrue("Movie catalog should not be empty", all.isNotEmpty())

        val comedyMovies = MovieRepository.filterMovies(setOf(Mood.COMEDY.id), emptySet())
        assertTrue("Should return comedy movies", comedyMovies.isNotEmpty())
        assertTrue(comedyMovies.all { it.moodTag == Mood.COMEDY.id })
    }

    @Test
    fun testVetoMechanic_consumesVetoAndEliminatesMovie() {
        val room = RoomRepository.prepareSoloSpin(setOf(Mood.EASY_WATCH.id), setOf("Netflix"))
        val spinResult = RoomRepository.triggerSpin()
        assertNotNull(spinResult)

        val chosenMovie = spinResult!!.first
        assertNotNull(chosenMovie)

        // Apply Veto
        val vetoSuccess = RoomRepository.applyVeto(room.host.id)
        assertTrue(vetoSuccess)

        val current = RoomRepository.currentRoom.value
        assertNotNull(current)
        assertTrue("Veto history must record the vetoed movie", current!!.vetoHistory.contains(chosenMovie.id))
        assertFalse("Eligible pool must no longer contain vetoed movie", current.eligibleMovieIds.contains(chosenMovie.id))
        assertTrue("Host hasVetoed flag must be true", current.host.hasVetoed)

        // Trigger re-spin after veto
        val reSpinResult = RoomRepository.triggerSpin()
        assertNotNull(reSpinResult)
        val finalMovie = reSpinResult!!.first
        assertNotNull(finalMovie)
        assertTrue("Re-spun movie cannot be the vetoed movie", finalMovie.id != chosenMovie.id)
    }

    @Test
    fun testMovieRepository_filterAdvancedCriteria() {
        // Filter by Genre = "Komedi"
        val comedy = MovieRepository.filterMoviesAdvanced(genres = setOf("Komedi"))
        assertTrue("Comedy movies should be found", comedy.isNotEmpty())
        assertTrue(comedy.all { it.genre.contains("Komedi", ignoreCase = true) })

        // Filter by Year >= 2020
        val recent = MovieRepository.filterMoviesAdvanced(genres = emptySet(), minYear = 2020, maxYear = 2030)
        assertTrue(recent.isNotEmpty())
        assertTrue(recent.all { it.year in 2020..2030 })

        // Filter by Rating >= 8.0
        val topRated = MovieRepository.filterMoviesAdvanced(genres = emptySet(), minRating = 8.0)
        assertTrue(topRated.isNotEmpty())
        assertTrue(topRated.all { it.imdb >= 8.0 })
    }

    @Test
    fun testMovieRepository_combinedNarrowingAndFallback() {
        val exact = MovieRepository.filterMoviesWithCriteria(
            moods = setOf(Mood.MIND_BENDER.id),
            genres = setOf("Bilim Kurgu"),
            minYear = 2010,
            maxYear = 2015,
            minRating = 8.5,
            platforms = setOf("Netflix")
        )
        assertTrue("Should find exact match for Interstellar / Inception", exact.isExactMatch)
        assertTrue(exact.movies.all {
            it.moodTag == Mood.MIND_BENDER.id &&
                it.genre.contains("Bilim Kurgu", ignoreCase = true) &&
                it.year in 2010..2015 &&
                it.imdb >= 8.5 &&
                it.platforms.contains("Netflix")
        })

        // Impossible exact intersection triggers progressive fallback
        val fallback = MovieRepository.filterMoviesWithCriteria(
            moods = setOf(Mood.HORROR.id),
            genres = setOf("Komedi"),
            minYear = 2024,
            maxYear = 2025,
            minRating = 8.5,
            platforms = setOf("Mubi")
        )
        assertFalse("Impossible criteria should mark isExactMatch = false", fallback.isExactMatch)
        assertTrue("Fallback should still return non-empty movie pool", fallback.movies.isNotEmpty())
    }

    @Test
    fun testWatchedMovie_excludedFromWheelAndRoomPools() {
        val watched = MovieRepository.allMovies.first { it.id == "m13" } // Interstellar
        WatchedMovieRepository.markAsWatched(watched)

        assertTrue(WatchedMovieRepository.isWatched(watched.id))

        val filtered = MovieRepository.filterMoviesWithCriteria(
            moods = setOf(Mood.MIND_BENDER.id),
            excludedMovieIds = WatchedMovieRepository.watchedMovieIds.value
        )
        assertFalse("Watched movie must not appear in filtered pool", filtered.movies.any { it.id == watched.id })

        val room = RoomRepository.prepareSoloSpin(
            moods = setOf(Mood.MIND_BENDER.id),
            platforms = setOf("Netflix", "Prime Video")
        )
        assertFalse("Room eligibleMovieIds must exclude watched movie", room.eligibleMovieIds.contains(watched.id))

        // Removing from watched restores eligibility
        WatchedMovieRepository.removeWatched(watched.id)
        assertFalse(WatchedMovieRepository.isWatched(watched.id))
        val afterRemove = MovieRepository.filterMoviesWithCriteria(
            moods = setOf(Mood.MIND_BENDER.id),
            excludedMovieIds = WatchedMovieRepository.excludedWheelMovieIds.value
        )
        assertTrue("Unwatched movie can re-enter wheel pool", afterRemove.movies.any { it.id == watched.id })
    }

    @Test
    fun testWatchedMovie_noteAndRewatchPersonalization() {
        val movie = MovieRepository.allMovies.first { it.id == "m13" } // Interstellar
        WatchedMovieRepository.markAsWatched(movie)

        assertTrue(WatchedMovieRepository.excludedWheelMovieIds.value.contains(movie.id))

        // Add personal note
        WatchedMovieRepository.updateNote(movie.id, "Müzikleri efsaneydi, tekrar izlenir")
        val withNote = WatchedMovieRepository.watchedMovies.value.first { it.movieId == movie.id }
        assertEquals("Müzikleri efsaneydi, tekrar izlenir", withNote.personalNote)

        // Toggle rewatch ON -> stays in watched list, re-enters wheel eligibility
        val rewatchOn = WatchedMovieRepository.toggleRewatch(movie.id)
        assertEquals(true, rewatchOn)
        assertTrue(WatchedMovieRepository.isWatched(movie.id))
        assertFalse(WatchedMovieRepository.excludedWheelMovieIds.value.contains(movie.id))

        val reEligiblePool = MovieRepository.filterMoviesWithCriteria(
            moods = setOf(Mood.MIND_BENDER.id),
            excludedMovieIds = WatchedMovieRepository.excludedWheelMovieIds.value
        )
        assertTrue("Movie marked for rewatch must be eligible for wheel again", reEligiblePool.movies.any { it.id == movie.id })

        // Toggle rewatch OFF -> excluded from wheel again
        val rewatchOff = WatchedMovieRepository.toggleRewatch(movie.id)
        assertEquals(false, rewatchOff)
        assertTrue(WatchedMovieRepository.excludedWheelMovieIds.value.contains(movie.id))
    }

    @Test
    fun testWatchedStats_computesTopGenresAndWatchTime() {
        val emptyStats = computeWatchedSummaryStats(emptyList())
        assertEquals(0, emptyStats.totalMovies)
        assertEquals(0, emptyStats.totalMinutes)
        assertTrue(emptyStats.genreStats.isEmpty())

        val watched = listOf(
            WatchedMovieEntity(
                movieId = "m1",
                title = "Bıçaklar Çekildi",
                year = 2022,
                durationMins = 139,
                genre = "Gizem / Komedi",
                imdb = 7.1
            ),
            WatchedMovieEntity(
                movieId = "m2",
                title = "Ölümlü Dünya 2",
                year = 2023,
                durationMins = 116,
                genre = "Komedi / Aksiyon",
                imdb = 7.3
            )
        )

        val stats = computeWatchedSummaryStats(watched)
        assertEquals(2, stats.totalMovies)
        assertEquals(255, stats.totalMinutes)
        assertEquals("Komedi", stats.topGenre)
        assertEquals(128, stats.averageDurationMins)
        assertEquals(7.2, stats.averageImdb, 0.01)

        val comedyStat = stats.genreStats.first { it.genre == "Komedi" }
        assertEquals(2, comedyStat.movieCount)
        assertEquals(255, comedyStat.totalMinutes)
        assertEquals(0.5f, comedyStat.countShare, 0.01f)
        assertTrue(stats.tastePersonaTitle.contains("Kahkaha"))
        assertTrue(stats.tasteSummaryText.contains("Komedi"))
    }

    @Test
    fun testWheelSpin_randomSelectionAndAngle() {
        RoomRepository.prepareSoloSpin(setOf(Mood.EASY_WATCH.id), setOf("Netflix"))
        val result = RoomRepository.triggerSpin()
        assertNotNull(result)

        val (winner, angle) = result!!
        assertNotNull(winner)
        assertTrue("Winner must be in catalog", MovieRepository.allMovies.any { it.id == winner.id })
        assertTrue("Angle should be greater than 1800 deg for multiple full revolutions", angle >= 1800f)
    }

    @Test
    fun testTmdbPosterUrlBuilder_handlesPathsAndFallback() {
        assertEquals("https://image.tmdb.org/t/p/w500", TmdbImageConfig.IMAGE_BASE_URL)
        assertNull(TmdbImageConfig.getPosterUrl(null))
        assertNull(TmdbImageConfig.getPosterUrl(""))
        assertNull(TmdbImageConfig.getPosterUrl("   "))

        assertEquals(
            "https://image.tmdb.org/t/p/w500/vDGr1YdrlfbU9wxTOdpf3zChmv9.jpg",
            TmdbImageConfig.getPosterUrl("/vDGr1YdrlfbU9wxTOdpf3zChmv9.jpg")
        )
        assertEquals(
            "https://image.tmdb.org/t/p/w500/vDGr1YdrlfbU9wxTOdpf3zChmv9.jpg",
            TmdbImageConfig.getPosterUrl("vDGr1YdrlfbU9wxTOdpf3zChmv9.jpg")
        )
        assertEquals(
            "https://image.tmdb.org/t/p/w500/abc.jpg",
            TmdbImageConfig.getPosterUrl("https://image.tmdb.org/t/p/w500/abc.jpg")
        )

        val moviesWithPosterPaths = MovieRepository.allMovies.filter { it.posterPath.isNotBlank() }
        assertTrue("Majority of catalog movies should carry TMDb poster paths", moviesWithPosterPaths.size >= 40)
        assertTrue(
            "All catalog movies with poster paths should resolve to w500 TMDb URLs",
            moviesWithPosterPaths.all {
                it.resolvedPosterUrl()?.startsWith("https://image.tmdb.org/t/p/w500/") == true
            }
        )
        assertTrue(
            "Movies without a posterPath should resolve to null so bundled default poster is shown",
            MovieRepository.allMovies
                .filter { it.posterPath.isBlank() }
                .all { it.resolvedPosterUrl() == null }
        )
    }

    @Test
    fun testSimilarAlternatives_recommendsThreeMatchingGenreAndRatingMovies() {
        val target = MovieRepository.getMovieById("m13")!! // Yıldızlararası (Bilim Kurgu / Macera, IMDb 8.7)
        val recommendations = MovieRepository.findSimilarAlternatives(
            targetMovie = target,
            excludedMovieIds = setOf("m14"), // Exclude Başlangıç so next best similar movies fill top 3
            limit = 3
        )

        assertEquals(3, recommendations.size)
        assertFalse("Must not recommend the target movie itself", recommendations.any { it.movie.id == target.id })
        assertFalse("Must exclude already-watched movies", recommendations.any { it.movie.id == "m14" })
        assertTrue("Recommendations should include shared genre or close rating", recommendations.all {
            it.matchPercent in 65..98 && it.reasonLabel.isNotBlank()
        })
        assertTrue(
            "Top recommendation should share Bilim Kurgu or Macera genre with Interstellar",
            recommendations.first().sharedGenres.any {
                it.equals("Bilim Kurgu", ignoreCase = true) || it.equals("Macera", ignoreCase = true)
            }
        )
    }

    @Test
    fun testCompareMode_duelAdvancesAndCrownsChampion() {
        val vm = DecisionViewModel()
        vm.openCompareMode()

        val initial = vm.compareState.value
        assertNotNull(initial.leftMovie)
        assertNotNull(initial.rightMovie)
        assertTrue("Left and right duel movies must be distinct", initial.leftMovie!!.id != initial.rightMovie!!.id)
        assertEquals(1, initial.roundNumber)

        val firstLeft = initial.leftMovie!!
        vm.pickCompareWinner(keepLeft = true)

        val round2 = vm.compareState.value
        assertEquals(firstLeft.id, round2.leftMovie?.id)
        assertEquals(2, round2.roundNumber)
        assertEquals(1, round2.currentStreakCount)

        // Skip pair replaces both cards
        val beforeSkipLeft = round2.leftMovie!!.id
        val beforeSkipRight = round2.rightMovie!!.id
        vm.skipComparePair()
        val afterSkip = vm.compareState.value
        assertTrue(
            "Skipping pair should bring in fresh movies",
            afterSkip.leftMovie!!.id != beforeSkipLeft && afterSkip.rightMovie!!.id != beforeSkipRight
        )

        // Play remaining rounds until champion is crowned
        var guard = 0
        while (vm.compareState.value.champion == null && guard < 10) {
            vm.pickCompareWinner(keepLeft = true)
            guard++
        }
        assertNotNull("Champion should be crowned after all duel rounds", vm.compareState.value.champion)
    }

    @Test
    fun testThemePreferences_togglesBetweenDarkAndLightCinematicModes() {
        val vm = DecisionViewModel()
        assertTrue("Default theme should start in Dark Noir mode", vm.isDarkTheme.value)

        vm.toggleTheme()
        assertFalse("First toggle should switch to Light Matinee mode", vm.isDarkTheme.value)
        assertFalse(ThemePreferences.isDarkMode.value)

        vm.toggleTheme()
        assertTrue("Second toggle should return to Dark Noir mode", vm.isDarkTheme.value)
        assertTrue(ThemePreferences.isDarkMode.value)
    }

    @Test
    fun testHorrorRevealDetector_identifiesHorrorTitlesAccurately() {
        val conjuring = MovieRepository.getMovieById("m19")!! // Korku Seansı
        val hereditary = MovieRepository.getMovieById("m20")!! // Ayin
        val quietPlace = MovieRepository.getMovieById("m6")!! // Gerilim / Korku
        val interstellar = MovieRepository.getMovieById("m13")!! // Bilim Kurgu / Macera

        assertTrue(conjuring.isHorrorMovie())
        assertTrue(hereditary.isHorrorMovie())
        assertTrue(quietPlace.isHorrorMovie())
        assertFalse(interstellar.isHorrorMovie())
    }

    @Test
    fun testWheelMinImdbFilter_narrowsActivePoolAndRestoresOnAll() {
        val vm = DecisionViewModel()
        vm.setFilterMode(true)
        vm.proceedToWheel()
        // Çark en fazla 12 dilim gösterir; filtre davranışı arkadaki tam havuz üzerinden doğrulanır.
        val initialTotal = vm.wheelPoolTotal.value
        assertTrue(initialTotal > 10)
        assertTrue(vm.activeWheelMovies.value.size <= WheelPoolSampler.MAX_SLICES)

        vm.updateWheelMinRating(8.0)
        val filteredPool = vm.activeWheelMovies.value
        assertTrue("Filtered pool should not be empty", filteredPool.isNotEmpty())
        assertTrue(
            "Filtering >= 8.0 should strictly narrow the full pool",
            vm.wheelPoolTotal.value < initialTotal
        )
        assertTrue(
            "Every movie in wheel pool must meet >= 8.0 rating filter",
            filteredPool.all { it.imdb >= 8.0 }
        )

        vm.updateWheelMinRating(0.0)
        assertEquals(
            "Resetting min rating filter should restore full pool count",
            initialTotal,
            vm.wheelPoolTotal.value
        )
    }


    @Test
    fun testFavoritesAndShareText_togglesAndBuildsFormattedShareCopy() {
        val vm = DecisionViewModel()
        val movie = MovieRepository.getMovieById("m13")!!

        assertFalse(vm.favoriteMovieIds.value.contains(movie.id))
        vm.toggleFavoriteMovie(movie.id, movie.title)
        assertTrue(vm.favoriteMovieIds.value.contains(movie.id))

        val shareCopy = buildMovieShareText(movie = movie)
        assertTrue(shareCopy.contains(movie.title))
        assertTrue(shareCopy.contains("Puan: ${movie.imdb}"))
        assertFalse(shareCopy.contains("Ortak Oda"))

        vm.toggleFavoriteMovie(movie.id, movie.title)
        assertFalse(vm.favoriteMovieIds.value.contains(movie.id))
    }

    @Test
    fun testShakeDetector_ignoresRestingGravityAndTriggersOnDeliberateShakeWithCooldown() {
        val detector = ShakeDetector()

        // Resting Earth gravity (~9.81 m/s^2 -> 1.0g) must NOT trigger shake
        val restResult = detector.onAccelerometerReading(0f, 9.81f, 0f, timestampMs = 1_000L)
        assertFalse("Resting gravity must not trigger spin", restResult.didTriggerShake)
        assertEquals(0f, restResult.normalizedIntensity, 0.05f)

        // Single mild bump above 2.0g but below strong threshold needs a follow-up peak
        val peak1 = detector.onAccelerometerReading(16f, 12f, 6f, timestampMs = 1_200L)
        assertFalse("Single moderate bump should wait for second peak", peak1.didTriggerShake)
        assertTrue("Intensity should reflect motion", peak1.normalizedIntensity > 0.5f)

        // Second shake peak within 600ms triggers spin
        val peak2 = detector.onAccelerometerReading(-17f, 12f, 5f, timestampMs = 1_400L)
        assertTrue("Second shake peak within slop window must trigger spin", peak2.didTriggerShake)

        // Immediate motion inside 1200ms cooldown must be debounced
        val duringCooldown = detector.onAccelerometerReading(28f, 10f, 10f, timestampMs = 1_900L)
        assertFalse("Readings inside cooldown window must not double-trigger", duringCooldown.didTriggerShake)

        // Strong jolt after cooldown expires triggers cleanly
        val afterCooldown = detector.onAccelerometerReading(28f, 10f, 10f, timestampMs = 2_800L)
        assertTrue("Strong shake after cooldown should trigger spin", afterCooldown.didTriggerShake)
    }

    @Test
    fun testWheelSoundEngine_synthesizesCrispClicksAndThrottlesCorrectly() {
        // 1. Verify synthesized PCM waveforms for every cue have valid non-zero energy and no overflow
        WheelSoundCue.entries.forEach { cue ->
            val pcm = WheelPcmSynthesizer.pcmForCue(cue)
            assertTrue("PCM for $cue must meet minimum static buffer length", pcm.size >= 1_102)
            assertTrue("PCM for $cue must contain audible waveform energy", pcm.any { it != 0.toShort() })
        }

        // 2. Verify cue selection, escalating volume, throttling, and mute gate
        val played = mutableListOf<Pair<WheelSoundCue, Float>>()
        var stoppedAllCount = 0
        val fakePlayer = object : WheelAudioPlayer {
            override fun play(cue: WheelSoundCue, volumeScale: Float) {
                played += cue to volumeScale
            }

            override fun stopAll() {
                stoppedAllCount++
            }

            override fun release() = Unit
        }

        var fakeClockMs = 1_000L
        val engine = WheelSoundEngine(
            player = fakePlayer,
            timeProviderMs = { fakeClockMs },
            minTickIntervalMs = 24L
        )

        engine.playSpinStart()
        assertEquals(WheelSoundCue.SPIN_START, played.last().first)

        // Early spin -> FAST click
        assertTrue(engine.playSliceTick(spinProgress = 0.1f))
        assertEquals(WheelSoundCue.SLICE_TICK_FAST, played.last().first)

        // Within 24ms debounce -> suppressed
        fakeClockMs += 10L
        assertFalse("Clicks closer than 24ms should throttle", engine.playSliceTick(spinProgress = 0.2f))

        // Mid deceleration -> MID click
        fakeClockMs += 30L
        assertTrue(engine.playSliceTick(spinProgress = 0.5f))
        assertEquals(WheelSoundCue.SLICE_TICK_MID, played.last().first)

        // Late deceleration -> SLOW deeper clack with higher volume
        fakeClockMs += 40L
        assertTrue(engine.playSliceTick(spinProgress = 0.9f))
        val slowEntry = played.last()
        assertEquals(WheelSoundCue.SLICE_TICK_SLOW, slowEntry.first)
        assertTrue("Late tick volume should escalate", slowEntry.second > 0.9f)

        engine.playWheelStop(isHorror = false)
        assertEquals(WheelSoundCue.STOP_CELEBRATE, played.last().first)

        engine.playWheelStop(isHorror = true)
        assertEquals(WheelSoundCue.STOP_HORROR, played.last().first)

        // Mute state stops emitting sound cues
        val countBeforeMute = played.size
        engine.isEnabled = false
        fakeClockMs += 100L
        assertFalse(engine.playSliceTick(spinProgress = 0.5f))
        engine.playSpinStart()
        engine.playWheelStop(isHorror = false)
        assertEquals("Muted engine must not enqueue audio cues", countBeforeMute, played.size)

        engine.stopAll()
        assertEquals(1, stoppedAllCount)
    }

    @Test
    fun testGenreStatistics_computesSpunAndLikedDistributionsAndAlignment() {
        val emptyStats = computeSpunAndLikedGenreStatsFromMovies(emptyList(), emptyList())
        assertEquals(0, emptyStats.totalSpunMovies)
        assertEquals(0, emptyStats.totalLikedMovies)
        assertEquals(0, emptyStats.alignmentPercent)
        assertTrue(emptyStats.items.isEmpty())

        val interstellar = MovieRepository.getMovieById("m13")!! // Bilim Kurgu / Macera
        val inception = MovieRepository.getMovieById("m14")!!    // Bilim Kurgu / Aksiyon
        val comedy = MovieRepository.allMovies.first { it.genre.contains("Komedi") }

        val stats = computeSpunAndLikedGenreStatsFromMovies(
            spunMovies = listOf(interstellar, inception, comedy),
            likedMovies = listOf(interstellar, inception)
        )

        assertEquals(3, stats.totalSpunMovies)
        assertEquals(2, stats.totalLikedMovies)
        assertEquals("Bilim Kurgu", stats.dominantSpunGenre)
        assertEquals("Bilim Kurgu", stats.dominantLikedGenre)
        assertTrue("Shared top genres should yield high alignment", stats.alignmentPercent >= 50)
        assertTrue(stats.averageSpunImdb > 0.0)
        assertTrue(stats.averageLikedImdb > 0.0)
        assertTrue(stats.insightSummary.contains("Bilim Kurgu"))

        // Verify resolveLikedMovies deduplicates favorite + rewatch entries
        WatchedMovieRepository.markAsWatched(interstellar)
        WatchedMovieRepository.toggleRewatch(interstellar.id)
        RoomRepository.recordSavedDecision(interstellar, "Karar Çarkı")
        RoomRepository.recordSavedDecision(inception, "Karar Çarkı")
        val combinedStats = computeSpunAndLikedGenreStats(
            savedDecisions = RoomRepository.savedDecisions.value,
            favoriteMovieIds = setOf(interstellar.id, inception.id),
            watchedMovies = WatchedMovieRepository.watchedMovies.value
        )
        assertEquals(2, resolveLikedMovies(setOf(interstellar.id, inception.id), WatchedMovieRepository.watchedMovies.value).size)
        assertEquals(2, combinedStats.totalLikedMovies)
        assertTrue(combinedStats.totalSpunMovies >= 2)
    }

    @Test
    fun testWidgetDailyRecommendation_isDeterministicByDayKeyAndRespectsWatched() {
        val day1PickA = WidgetMovieStateStore.getDailyRecommendation(dayKey = 2026110, excludedIds = emptySet())
        val day1PickB = WidgetMovieStateStore.getDailyRecommendation(dayKey = 2026110, excludedIds = emptySet())
        assertEquals("Same calendar day must yield deterministic recommendation", day1PickA.id, day1PickB.id)
        assertTrue("Daily pick should come from high-rated curated pool", day1PickA.imdb >= 7.5)

        val dayNextPick = WidgetMovieStateStore.getDailyRecommendation(dayKey = 2026111, excludedIds = emptySet())
        assertTrue("Adjacent calendar day should rotate recommendation", dayNextPick.id != day1PickA.id)

        val pickExcludingDay1 = WidgetMovieStateStore.getDailyRecommendation(
            dayKey = 2026110,
            excludedIds = setOf(day1PickA.id)
        )
        assertTrue("Watched daily movie must be excluded cleanly", pickExcludingDay1.id != day1PickA.id)
    }

    @Test
    fun testOpenMovieFromWidget_setsSelectedMovieAndNavigatesToWheel() {
        val vm = DecisionViewModel()
        assertEquals(Screen.HOME, vm.currentScreen.value)

        val target = MovieRepository.getMovieById("m13")!!
        vm.openMovieFromWidget(target.id)

        assertEquals(Screen.WHEEL, vm.currentScreen.value)
        assertEquals(target.id, vm.selectedMovie.value?.id)
        assertFalse(vm.isSpinning.value)
        assertTrue(vm.activeWheelMovies.value.any { it.id == target.id })

        // Unknown movie ID leaves state untouched
        vm.resetToHome()
        vm.openMovieFromWidget("non_existent_widget_movie")
        assertEquals(Screen.HOME, vm.currentScreen.value)
        assertNull(vm.selectedMovie.value)
    }

    @Test
    fun testWheelThemeManager_recolorsWheelPalettesAndPreservesSliceTextContrast() {
        val darkWheel = WheelThemeManager.resolveWheelTheme(isDark = true)
        val lightWheel = WheelThemeManager.resolveWheelTheme(isDark = false)

        assertEquals(WheelThemeManager.DARK_THEME_ID, darkWheel.themeId)
        assertEquals(WheelThemeManager.LIGHT_THEME_ID, lightWheel.themeId)
        assertTrue(darkWheel.isDark)
        assertFalse(lightWheel.isDark)

        // Wheel rim and accents must mirror active theme's primary palette
        assertEquals(DarkCinemaPalette.marqueeGold, darkWheel.outerRimColor)
        assertEquals(LightCinemaPalette.marqueeGold, lightWheel.outerRimColor)
        assertEquals(DarkCinemaPalette.marqueeGoldDark, darkWheel.outerRimAccentColor)
        assertEquals(LightCinemaPalette.marqueeGoldDark, lightWheel.outerRimAccentColor)

        // Slices, hub, and needle must distinctly recolor across modes
        assertTrue(darkWheel.sliceColors.isNotEmpty())
        assertEquals(darkWheel.sliceColors.size, lightWheel.sliceColors.size)
        assertTrue(
            "Light mode wheel slices must recolor compared to dark mode",
            WheelThemeManager.primarySliceArgb(true) != WheelThemeManager.primarySliceArgb(false)
        )
        assertTrue(
            "Inner vignette should be softer and tinted in light mode",
            darkWheel.innerVignetteColor != lightWheel.innerVignetteColor
        )

        // Luminance-aware text contrast: bright matinee slice uses dark text, deep velvet slice uses light text
        val brightSlice = lightWheel.sliceColorAt(0)
        val deepSlice = darkWheel.sliceColorAt(0)
        assertEquals(lightWheel.darkSliceTextColor, lightWheel.sliceTextColorFor(brightSlice))
        assertEquals(darkWheel.lightSliceTextColor, darkWheel.sliceTextColorFor(deepSlice))
    }

    @Test
    fun testSharePosterRenderer_buildsFavoritesShareTextCorrectly() {
        val sampleMovies = MovieRepository.allMovies.take(12)
        val text = SharePosterRenderer.buildFavoritesShareText(sampleMovies)

        assertTrue(text.contains("FAVORİ FİLM LİSTEM"))
        assertTrue(text.contains("12 seçkin film"))
        assertTrue(text.contains(sampleMovies.first().title))
        assertTrue("Lists >10 movies should summarize overflow", text.contains("+2 film daha"))
    }

    @Test
    fun testCustomWheelLists_includesStarterCategoriesCreatesCustomMoviesAndSpinsCustomPool() {
        val vm = DecisionViewModel()
        val starters = vm.customWheelLists.value
        assertTrue(starters.any { it.name == "Korku Gecesi" })
        assertTrue(starters.any { it.name == "Aile Dostu" })
        assertTrue(starters.any { it.name == "Klasikler" })

        val customMovie = CustomWheelListRepository.createCustomMovie(
            title = "The Shining",
            genre = "Korku / Klasik",
            year = 1980,
            imdb = 8.4
        )
        val catalogMovie = MovieRepository.getMovieById("m19")!!

        val saved = vm.saveCustomWheelList(
            editingListId = null,
            name = "Cuma Korku Maratonu",
            emoji = "👻",
            description = "Ev yapımı korku gecesi",
            movies = listOf(customMovie, catalogMovie),
            accentColorHex = 0xFFDC2626L
        )

        assertEquals("Cuma Korku Maratonu", saved.name)
        assertTrue(vm.customWheelLists.value.any { it.id == saved.id })

        vm.spinCustomWheelList(saved.id)
        assertEquals(Screen.WHEEL, vm.currentScreen.value)
        assertEquals(saved.id, vm.activeCustomList.value?.id)
        assertEquals(2, vm.activeWheelMovies.value.size)

        vm.spinWheel()
        val spinEvent = vm.spinEvent.value
        assertNotNull("Custom wheel pool must produce valid spin event", spinEvent)
        assertTrue(
            "Winner must come strictly from custom wheel pool",
            spinEvent!!.winner.id == customMovie.id || spinEvent.winner.id == catalogMovie.id
        )
        vm.completeSpin(spinEvent.id)
        assertEquals(spinEvent.winner.id, vm.selectedMovie.value?.id)

        vm.deleteCustomWheelList(saved.id)
        assertFalse(vm.customWheelLists.value.any { it.id == saved.id })
        assertNull(vm.activeCustomList.value)
    }

    @Test
    fun testSavedDecisionEntity_roundTripsCatalogAndCustomMovies() {
        val customMovie = CustomWheelListRepository.createCustomMovie(
            title = "Blade Runner 2049",
            genre = "Bilim Kurgu",
            year = 2017,
            imdb = 8.0,
            accentColorHex = 0xFF06B6D4L
        )
        val decision = SavedDecision(
            id = "sd_custom_roundtrip",
            movie = customMovie,
            dateText = "Bugün • 22:15",
            minutesSaved = 55,
            mode = "Özel Çark (Bilim Kurgu)",
            selectedAtMillis = 1_750_000_000_000L
        )

        val entity = decision.toEntity()
        val restored = entity.toDomain()

        assertEquals(decision.id, restored.id)
        assertEquals(customMovie.id, restored.movie.id)
        assertEquals("Blade Runner 2049", restored.movie.title)
        assertEquals(2017, restored.movie.year)
        assertEquals(8.0, restored.movie.imdb, 0.01)
        assertEquals("Bilim Kurgu", restored.movie.genre)
        assertEquals(0xFF06B6D4L, restored.movie.colorHex)
        assertEquals("Bugün • 22:15", restored.dateText)
        assertEquals(55, restored.minutesSaved)
        assertEquals("Özel Çark (Bilim Kurgu)", restored.mode)
        assertEquals(1_750_000_000_000L, restored.selectedAtMillis)
    }

    @Test
    fun testDecisionViewModel_recordsDeletesAndClearsSavedHistory() {
        val vm = DecisionViewModel()
        val initialSize = vm.savedDecisions.value.size

        vm.spinCustomWheelList("preset_klasikler")
        vm.spinWheel()
        val spinEvent = vm.spinEvent.value!!
        vm.completeSpin(spinEvent.id)

        assertEquals(initialSize + 1, vm.savedDecisions.value.size)
        val newest = vm.savedDecisions.value.first()
        assertEquals(spinEvent.winner.id, newest.movie.id)
        assertTrue(newest.mode.contains("Özel Çark (Klasikler)"))

        vm.deleteSavedDecision(newest.id)
        assertEquals(initialSize, vm.savedDecisions.value.size)
        assertFalse(vm.savedDecisions.value.any { it.id == newest.id })

        vm.clearSavedHistory()
        assertTrue(vm.savedDecisions.value.isEmpty())
    }

    @Test
    fun testGenreRevealStyle_mapsEveryCatalogGenreToDistinctAnimationTheme() {
        val conjuring = MovieRepository.getMovieById("m19")!!
        val interstellar = MovieRepository.getMovieById("m13")!!
        val aboutTime = MovieRepository.getMovieById("m10")!!
        val hangover = MovieRepository.getMovieById("m7")!!
        val fastFive = MovieRepository.getMovieById("m3")!!
        val goneGirl = MovieRepository.getMovieById("m4")!!
        val spiritedAway = MovieRepository.getMovieById("m21")!!
        val anadolu = MovieRepository.getMovieById("m17")!!

        assertEquals(GenreRevealStyle.HORROR, conjuring.resolveGenreRevealStyle())
        assertEquals(GenreRevealStyle.SCI_FI, interstellar.resolveGenreRevealStyle())
        assertEquals(GenreRevealStyle.ROMANCE, aboutTime.resolveGenreRevealStyle())
        assertEquals(GenreRevealStyle.COMEDY, hangover.resolveGenreRevealStyle())
        assertEquals(GenreRevealStyle.ACTION, fastFive.resolveGenreRevealStyle())
        assertEquals(GenreRevealStyle.THRILLER, goneGirl.resolveGenreRevealStyle())
        assertEquals(GenreRevealStyle.ANIMATION, spiritedAway.resolveGenreRevealStyle())
        assertEquals(GenreRevealStyle.DRAMA, anadolu.resolveGenreRevealStyle())
    }

    @Test
    fun testHorrorCatalog_hasMultipleNewMoviesAcrossPlatformsAndNewReleasesSortFirst() {
        val horrorResult = MovieRepository.filterMoviesWithCriteria(
            moods = setOf(Mood.HORROR.id),
            platforms = setOf("Netflix", "Prime Video", "Disney+", "HBO Max", "Mubi", "Gain", "TOD", "Apple TV+")
        )
        assertTrue(horrorResult.isExactMatch)
        assertTrue("Horror pool must contain many movies across platforms", horrorResult.movies.size >= 8)
        assertTrue(
            "2024+ new horror releases must be prioritized first",
            horrorResult.movies.first().year >= 2024
        )
        val platformsCovered = horrorResult.movies.flatMap { it.platforms }.toSet()
        assertTrue(platformsCovered.contains("Netflix"))
        assertTrue(platformsCovered.contains("Prime Video"))
        assertTrue(platformsCovered.contains("Disney+"))
        assertTrue(platformsCovered.contains("Mubi"))
    }

    @Test
    fun testOtherCultCategoryAndAudioOptions_filterCorrectly() {
        val cultMovies = MovieRepository.getCultAndOtherMovies()
        assertTrue(cultMovies.size >= 6)
        assertTrue(cultMovies.any { it.title == "Baba" && it.platforms.contains(OTHER_PLATFORM_NAME) })
        assertTrue(cultMovies.any { it.title == "Esaretin Bedeli" && it.platforms.contains(OTHER_PLATFORM_NAME) })

        val dubbedOnly = MovieRepository.filterMoviesWithCriteria(
            audioOptions = setOf(AudioLanguageOption.DUBBED.id)
        )
        assertTrue(dubbedOnly.isExactMatch)
        assertTrue(dubbedOnly.movies.isNotEmpty())
        assertTrue(dubbedOnly.movies.all { it.audioOptions.contains(AudioLanguageOption.DUBBED.id) })

        val subtitledOnly = MovieRepository.filterMoviesWithCriteria(
            audioOptions = setOf(AudioLanguageOption.SUBTITLED.id)
        )
        assertTrue(subtitledOnly.isExactMatch)
        assertTrue(subtitledOnly.movies.isNotEmpty())
        assertTrue(subtitledOnly.movies.all { it.audioOptions.contains(AudioLanguageOption.SUBTITLED.id) })

        val vm = DecisionViewModel()
        vm.toggleAudioOption(AudioLanguageOption.DUBBED.id)
        assertEquals(setOf(AudioLanguageOption.DUBBED.id), vm.selectedAudioOptions.value)
        vm.spinCultOtherCategory()
        assertEquals(Screen.WHEEL, vm.currentScreen.value)
        assertTrue(vm.activeWheelMovies.value.all { it.platforms.contains(OTHER_PLATFORM_NAME) || it.isCultOrMostWatched })
    }

    @Test
    fun testDailyMovieFeedSync_addsNewDailyMoviesAndUpdatesBannerList() {
        MovieRepository.resetDailyFeedForTests()
        try {
            val dailySpecial = Movie(
                id = "daily_unit_test_1",
                title = "Günlük Yeni Film",
                originalTitle = "Daily Fresh Hit",
                year = 2026,
                durationMins = 115,
                imdb = 7.9,
                moodTag = Mood.THRILLER.id,
                platforms = listOf("Netflix"),
                genre = "Gerilim",
                logline = "Günlük güncellemeyle eklenen yeni test filmi.",
                whyWatch = "Bugün eklendi."
            )
            MovieRepository.syncDailyMovieUpdates(
                forceRefresh = true,
                extraDailyAdditions = listOf(dailySpecial)
            )
            assertTrue(MovieRepository.dailyUpdatedMovies.value.any { it.id == "daily_unit_test_1" })
            assertNotNull(MovieRepository.getMovieById("daily_unit_test_1"))
            assertTrue(MovieRepository.dailyFeedLastUpdated.value.isNotBlank())
        } finally {
            MovieRepository.resetDailyFeedForTests()
        }
    }

    @Test
    fun testAnalyzeDecisionHistory_aggregatesTopGenresTopMoviesAndModesFromHistory() {
        val empty = analyzeDecisionHistory(emptyList())
        assertEquals(0, empty.totalSelections)
        assertEquals(0, empty.uniqueMoviesCount)
        assertTrue(empty.topGenres.isEmpty())
        assertTrue(empty.topMovies.isEmpty())

        val sciFiHit = MovieRepository.getMovieById("m13")!! // Interstellar - Bilim Kurgu / Dram
        val thrillerHit = MovieRepository.getMovieById("m1")!! // Bıçaklar Çekildi

        val decisions = listOf(
            SavedDecision("d1", sciFiHit, "Bugün • 20:00", 45, "Tek Başına", 4_000L),
            SavedDecision("d2", sciFiHit, "Dün • 21:00", 50, "Ortak Oda (1234)", 3_000L),
            SavedDecision("d3", sciFiHit, "2 Gün Önce", 45, "Film Karşılaştır (VS)", 2_000L),
            SavedDecision("d4", thrillerHit, "3 Gün Önce", 40, "Özel Çark (Gizem)", 1_000L)
        )

        val summary = analyzeDecisionHistory(decisions)
        assertEquals(4, summary.totalSelections)
        assertEquals(2, summary.uniqueMoviesCount)
        assertEquals(180, summary.totalMinutesSaved)
        assertEquals(1, summary.repeatPickMovieCount)
        assertEquals(sciFiHit.id, summary.dominantMovie?.movie?.id)
        assertEquals(3, summary.dominantMovie?.selectionCount)
        assertEquals(1, summary.topMovies[1].selectionCount)
        assertTrue(summary.topGenres.any { it.genre == "Bilim Kurgu" && it.selectionCount == 3 })
        // "Tek Başına" ve eski "Ortak Oda" kayıtları "Karar Çarkı" kategorisinde birleşir
        assertTrue(summary.modeBreakdown.any { it.modeCategory == "Karar Çarkı" && it.selectionCount == 2 })
        assertTrue(summary.modeBreakdown.none { it.modeCategory == "Ortak Oda" })
        assertTrue(summary.insightText.contains(sciFiHit.title))

        // Verify RoomRepository keeps historyDashboardStats reactive on record/delete/clear
        RoomRepository.clearSavedHistory()
        assertEquals(0, RoomRepository.historyDashboardStats.value.totalSelections)
        RoomRepository.recordSavedDecision(sciFiHit, "Tek Başına")
        RoomRepository.recordSavedDecision(sciFiHit, "Ortak Oda (9999)")
        assertEquals(2, RoomRepository.historyDashboardStats.value.totalSelections)
        assertEquals(2, RoomRepository.historyDashboardStats.value.dominantMovie?.selectionCount)
    }

    @Test
    fun testExpandedCatalog_hasUniqueIdsValidTrailersAndFranchiseVariety() {
        val all = MovieRepository.allMovies
        assertTrue("Catalog should be expanded well beyond the original 53 titles", all.size >= 200)
        assertEquals("Movie ids must be unique", all.size, all.map { it.id }.toSet().size)
        val tmdbIds = all.mapNotNull { it.tmdbId }
        assertEquals("TMDB ids must not repeat", tmdbIds.size, tmdbIds.toSet().size)
        val keyPattern = Regex("^[A-Za-z0-9_-]{11}$")
        assertTrue("YouTube trailer keys must be 11-char video ids",
            all.mapNotNull { it.trailerKey }.all { keyPattern.matches(it) })
        assertEquals(
            "Trailer launcher must open the official YouTube watch URL",
            "https://www.youtube.com/watch?v=abcdefghijk",
            com.example.ui.components.TrailerLauncher.youtubeUrl("abcdefghijk")
        )
        val harryPotter = all.count { it.title.startsWith("Harry Potter") }
        assertTrue("At most one film per franchise", harryPotter <= 1)
    }

    @Test
    fun testWheelPoolSampler_capsSlicesKeepsRequiredMovieAndRotates() {
        WheelPoolSampler.resetForTests()
        val pool = MovieRepository.allMovies
        val target = pool.last()
        val first = WheelPoolSampler.sample(pool, mustInclude = target)
        assertEquals(WheelPoolSampler.MAX_SLICES, first.size)
        assertTrue(first.any { it.id == target.id })
        val second = WheelPoolSampler.sample(pool)
        assertTrue("Consecutive samples should show mostly fresh movies",
            second.count { s -> first.any { it.id == s.id } } <= 2)
        val small = pool.take(5)
        assertEquals("Small pools are kept as-is", small.map { it.id }, WheelPoolSampler.sample(small).map { it.id })
    }

    @Test
    fun testBluTvRebrandedAsHboMax_inCatalogAndLegacyHistory() {
        assertTrue(com.example.data.model.AvailablePlatforms.any { it.name == "HBO Max" })
        assertTrue(com.example.data.model.AvailablePlatforms.none { it.name == "BluTV" })
        assertTrue(MovieRepository.allMovies.none { it.platforms.contains("BluTV") })
        assertTrue(MovieRepository.allMovies.any { it.platforms.contains("HBO Max") })
        // Cihazda kayıtlı eski geçmiş kayıtları yeni adla okunur
        val legacy = com.example.data.local.SavedDecisionEntity(
            id = "legacy",
            movieId = "m15",
            movieTitle = "Prestij",
            movieYear = 2006,
            movieDurationMins = 130,
            movieImdb = 8.5,
            moviePlatformsCsv = "BluTV,Netflix",
            movieGenre = "Gizem / Dram",
            dateText = "01.01.2026",
            mode = "Tek Başına"
        ).toDomain()
        assertEquals(listOf("HBO Max", "Netflix"), legacy.movie.platforms)
    }
}

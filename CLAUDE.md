# CLAUDE.md

This file provides guidance to Claude Code when working with the **Ne İzlesek?** Android repository.

## Project Overview
- **App Name:** Ne İzlesek?
- **Application ID:** `com.aistudio.neizlesek.vrtxqw` (`namespace = "com.example"`)
- **Language & UI:** Kotlin, Jetpack Compose (Material Design 3), MVVM (`DecisionViewModel` + `StateFlow`)
- **Local Persistence:** Room (`AppDatabase` v5, `ne_izlesek_db`) + `SharedPreferences` (`ThemePreferences`, daily feed/widget state)
- **SDKs:** `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`, Java 11

## Build & Test Commands
Use the checked-in Gradle wrapper (`./gradlew`, Gradle 9.3.1, AGP 9.1.1, JDK 21 works).

First-time setup on a fresh machine (e.g. a cloud session):
- Android SDK needs `platforms;android-36.1` and `build-tools;36.1.0`; write `sdk.dir=<path>` to `local.properties` (gitignored).
- `debug.keystore` is gitignored but required by the `debugConfig` signing config. Generate it once:
  `keytool -genkeypair -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"`
- Maven Central may answer `429 Too Many Requests` on the first resolve; simply re-run the build (downloaded artifacts stay cached).
- Maven Central redirects some large jars (e.g. Robolectric `nativeruntime-dist-compat`) to `gcp.mirrors.maven.org`; in a cloud environment with limited network that host must be in the allowed hosts list.
- On first run Robolectric downloads `android-all-instrumented` (SDK 36) into `~/.m2` at test time and does not retry on 429; if `ExampleRobolectricTest` fails with `MavenArtifactFetcher` / HTTP 429, wait ~1 minute and re-run that test class.
- Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Run all JVM unit & Robolectric UI/DAO tests
./gradlew :app:testDebugUnitTest

# Run a single unit or Robolectric test class
./gradlew :app:testDebugUnitTest --tests "com.example.DecisionUnitTest"
./gradlew :app:testDebugUnitTest --tests "com.example.ExampleRobolectricTest"

# Android Lint check
./gradlew :app:lintDebug
```

## Architecture & Key Modules

### Entry Point & Navigation
- `app/src/main/java/com/example/MainActivity.kt` — Initializes repositories (`WatchedMovieRepository`, `CustomWheelListRepository`, `RoomRepository`, `ThemePreferences`, `MovieRepository.initDailyFeed`), handles edge-to-edge insets, widget launch intents, and state-driven `Screen` routing.
- `app/src/main/java/com/example/ui/viewmodel/DecisionViewModel.kt` — Central `ViewModel` managing navigation (`Screen` enum), filters (moods, platforms, `"Diğer"` cult category, Turkish Dublaj/Altyazı audio options, year, IMDb), wheel spins, Tinder-style VS compare mode (`CompareUiState`), custom lists, and reactive `historyDashboardStats`.

### Data & Room Layer (`app/src/main/java/com/example/data/`)
- `local/AppDatabase.kt` — Room database v5 with migrations `1->2->3->4->5`:
  - `watched_movies` (`WatchedMovieDao`)
  - `custom_wheel_lists` (`CustomWheelListDao`)
  - `saved_decisions` (`SavedDecisionDao`, including SQL aggregation queries `observeMostSelectedMovies()`, `getMostSelectedMoviesOnce()`, `observeModeSelectionStats()`)
- `repository/MovieRepository.kt` — Curated multi-platform 2024–2026 catalog (prioritizing recent releases), rich horror pool, `"Diğer"` cult & most-watched classics, Turkish Dublaj/Altyazı metadata, and daily `"Yeni Çıkanlar"` feed sync.
- `repository/RoomRepository.kt` — Solo/shared room spin orchestration + reactive `savedDecisions` & `historyDashboardStats` backed by `SavedDecisionDao`.
- `repository/WatchedMovieRepository.kt` & `repository/CustomWheelListRepository.kt` — Watched/favorite/rewatch state and user-created custom wheel categories.
- `model/HistoryDashboardAnalytics.kt` & `model/GenreStatistics.kt` — Analytics engines powering the DB Decision Dashboard (`analyzeDecisionHistory`) and Genre Statistics Panel.
- `audio/`, `haptic/`, `sensor/`, `share/` — Synthesized PCM wheel sound effects (`WheelSoundEngine`), haptics, shake-to-spin sensor, and `FileProvider`-backed PNG poster rendering (`SharePosterRenderer`).

### UI Screens & Components (`app/src/main/java/com/example/ui/`)
- `screens/` — `HomeScreen`, `RoomScreen`, `FilterScreen`, `WheelScreen`, `CompareScreen`, `CustomListsScreen`, `HistoryScreen` ("Geçmişte Ne Seçtik?"), `ProfileScreen`, `StatsScreen`, `FlexScreen`.
- `components/` — `AnimatedDecisionWheel`, `WheelCanvas`, `CinematicRevealEffects` (genre-based reveal animations), `DecisionHistoryDashboard` (Room DB top genres & top selected movies dashboard), `GenreStatisticsPanel`, `NewReleasesBannerStrip`, `SocialShareSheet`, `StoryShareCard`, `MoviePoster`.
- `theme/` — `Theme.kt`, `Color.kt`, `WheelThemeManager.kt` (auto dark/light wheel recoloring).
- `widget/` — `MovieWheelAppWidgetProvider.kt` & `WidgetMovieStateStore.kt` for home-screen widget spins and daily recommendation.

## Conventions
- Interactive Compose nodes use `Modifier.testTag("snake_case_name")` — preserve existing `testTag` identifiers when editing UI so `ExampleRobolectricTest` stays green.
- Use `CinemaTheme.colors` / `MaterialTheme.colorScheme` tokens instead of hardcoded color literals in UI composables.
- All user-facing UI strings are in Turkish.

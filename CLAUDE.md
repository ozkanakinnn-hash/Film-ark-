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
- Cloud sessions: use the personal `Film-ark-` environment. Its setup script installs the SDK, generates `debug.keystore`, and adds a Gradle init script (`~/.gradle/init.d/central-mirror.gradle`) that puts Google's official Maven Central mirror (`maven-central.storage-download.googleapis.com`) first and points Robolectric's `android-all` download at it.
- Without that mirror, Maven Central answers `429 Too Many Requests` here. Gradle then caches those artifacts as "Could not find", and later runs fail even after the rate limit ends. Do not use `--refresh-dependencies` to fix this, because it triggers more 429s. Use the mirror, or delete `~/.gradle/caches/modules-2/metadata-*` and run again.
- Visual check without an emulator: a throwaway Robolectric test with `@GraphicsMode(NATIVE)` and `@Config(qualifiers = "w400dp-h860dp-xhdpi")` can draw `activity.window.decorView` into a `Bitmap` (`captureToImage()` times out under Robolectric). With `mainClock.autoAdvance = false` you can capture animation frames. Delete the test afterwards.
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
- `components/GenreRevealOverlay.kt` — Full-screen genre-specific reveal scene shown by `WheelScreen` after a real spin finishes (curtains, warp field, confetti, explosion, radar…). Driven by ONE finite `Animatable` timeline (0→1, 3.8 s) so it ends on its own under the Compose test clock; tap or back skips it. Avoid infinite transitions inside it.
- `components/CinematicBackdrop.kt` — Ambient projector glow, vignette and film grain behind every screen (wrapped in `MainActivity`). Screen roots are transparent; do not add `.background(CinemaBackground)` back to them.
- `theme/` — `Theme.kt`, `Color.kt`, `Type.kt` (full typography scale), `Motion.kt` (`CinemaMotion` easing/duration tokens, `CinemaShapes`, `Modifier.pressScale`), `WheelThemeManager.kt` (auto dark/light wheel recoloring).
- Navigation transitions: `MainActivity` wraps screens in `AnimatedContent`; `Screen.depth` decides forward/back slide direction, FLEX opens with a scale transition. Give new screens a depth.
- App icon: logo "H1" (question mark + play triangle + red wheel needle). Adaptive layers are vectors in `res/drawable/ic_launcher_{background,foreground,monochrome}.xml` (mark kept inside the 66dp safe zone); legacy PNG mipmaps and `branding/play-store-icon-512.png` are rendered from `branding/logo-options/harman-H1-*.svg`. Brand colors: gold `#E9B949`, crimson `#D63447`, noir `#0B0A0F`.
- TMDB posters: `tools/tmdb_poster_sync.py report|apply` resolves each catalog movie (original title + year) via the TMDB search API and writes `posterPath` + `tmdbId` into `MovieRepository.kt` (and trend sample posters). The APK contains NO TMDB key; phones only load images from `image.tmdb.org`. In the `Film-ark-` cloud environment the proxy injects the TMDB Read Access Token for `api.themoviedb.org`; locally run with `TMDB_TOKEN=<token>`. Review `tools/tmdb_poster_report.json` before `apply` (status must be `kesin`).
- `BuildConfig.TMDB_POSTERS_ENABLED` (app/build.gradle.kts) gates all TMDB image URLs via `TmdbImageConfig.getPosterUrl`. TMDB's free key is non-commercial only: before adding ads/paid features either get a TMDB commercial license or set this to `false`. Required attribution lives in `components/DataSourceAttributionCard.kt` (Profile screen) and uses the official logo `res/drawable/tmdb_logo_short.xml` — do not recolor or remove it.
- Catalog: 53 hand-curated movies in `MovieRepository.kt` + ~200 generated ones in `data/repository/GeneratedMovies.kt` (DO NOT edit by hand; regenerate with `python3 tools/tmdb_catalog_build.py --limit 200`). The generator adds official Turkish titles, TR overviews, runtime, TMDB rating (field is still named `imdb`, UI label is "Puan"), Turkish streaming platforms (JustWatch via TMDB), official YouTube `trailerKey`, and keeps one film per franchise. It also syncs titles in the weekly-trends sample list to the catalog titles; all 253 titles match TMDB's official Turkish titles.
- Wheel pool: `WheelPoolSampler` shows at most 12 slices sampled from the full filtered pool (`DecisionViewModel.wheelPoolTotal`), deprioritising recently shown films; re-spins refresh slices. User-made custom lists are shown in full.
- Trailers: `components/TrailerButton.kt` opens the official trailer in the YouTube app/browser via `ACTION_VIEW` — never embed a player (keeps YouTube ToS/ads rules out of scope, no API key).
- Poster plan B: `components/GenrePosterCard.kt` (default style `neonbilet` = neon genre icon + real cut-out ticket shape, theme-aware dark/light; also `afis` | `bilet` | `neon`). `gradle.properties` → `neizlesek.posterMode=tmdb|cards`, `neizlesek.posterCardStyle=...`; one-off: `./gradlew assembleDebug -Pneizlesek.posterMode=cards`. Cards also replace failed/missing TMDB posters when `MoviePosterImage` gets `genre`/`year`.
- VS screen: picking a genre switches `CompareScreen` to full-screen swipe duel; the champion is a 9:16 logo story card and "Hikâye Olarak Paylaş" shares a 1080×1920 PNG from `data/share/ChampionStoryRenderer.kt` (falls back to text share if the image can't be written).
- Brand assets: `res/drawable/brand_mark.xml` (full logo mark) and `brand_mark_hook.xml` (no needle, used in the wheel hub). Home header, VS cards, story card and widget use them.
- Privacy: `PRIVACY.md` (no accounts, no analytics/ads SDKs, data stays on device). Firebase deps are commented out in `app/build.gradle.kts` because nothing uses them; update PRIVACY.md if you re-enable them or add AdMob.
- `widget/` — `MovieWheelAppWidgetProvider.kt` & `WidgetMovieStateStore.kt` for home-screen widget spins and daily recommendation.

## Conventions
- Interactive Compose nodes use `Modifier.testTag("snake_case_name")` — preserve existing `testTag` identifiers when editing UI so `ExampleRobolectricTest` stays green.
- Use `CinemaTheme.colors` / `MaterialTheme.colorScheme` tokens instead of hardcoded color literals in UI composables.
- All user-facing UI strings are in Turkish.

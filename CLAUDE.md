# CLAUDE.md

This file provides guidance to Claude Code when working with the **Ne İzlesek?** Android repository.

## Project Overview
- **App Name:** Ne İzlesek?
- **Application ID:** `com.ozkanakin.neizlesek` (`namespace = "com.example"`; Kotlin packages stay `com.example.*`). versionCode 2 / 1.1.0. The ID was changed from the AI Studio `com.aistudio.neizlesek.vrtxqw`; never change it again after the Play Store release (it would be a different app).
- **Language & UI:** Kotlin, Jetpack Compose (Material Design 3), MVVM (`DecisionViewModel` + `StateFlow`)
- **Local Persistence:** Room (`AppDatabase` v5, `ne_izlesek_db`) + `SharedPreferences` (`ThemePreferences`, `MotionPreferences`, `TasteProfileRepository`, `WatchLaterRepository`, favorites, daily feed/widget state). Android auto-backup is OFF (`allowBackup=false`, `data_extraction_rules.xml`/`backup_rules.xml` exclude everything); users export/import themselves (`data/backup/BackupManager.kt`).
- **SDKs:** `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`, Java 11. Dependencies (Nov 2026): Compose BOM `2026.06.01` (Compose 1.11), Lifecycle 2.10.0, Activity 1.13.0, Room 2.8.5, Coroutines 1.11.0, core-ktx 1.18.0. Newer Compose (BOM ≥ 2026.08), Lifecycle 2.11 and core 1.19 require compileSdk 37 + a newer AGP; upgrade those together.

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
- Test result XMLs: `app/build/test-results/testDebugUnitTest/*.xml`. Run Gradle with `LC_ALL=C.UTF-8` — otherwise the HTML report fails on Turkish test names ("Malformed input") and the build fails even if all tests passed.
- Release build: R8 (`isMinifyEnabled`) + `isShrinkResources` are ON; rules in `app/proguard-rules.pro`. Signing reads `KEYSTORE_PATH` (default `my-upload-key.jks` in repo root, gitignore it), `STORE_PASSWORD`, `KEY_PASSWORD`, alias `upload`. To test R8 without the real key, generate a throwaway keystore in a temp dir and pass those env vars to `./gradlew :app:assembleRelease`. Never commit a keystore or its passwords.

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Run all JVM unit & Robolectric UI/DAO tests
./gradlew :app:testDebugUnitTest

# Run a single unit or Robolectric test class
./gradlew :app:testDebugUnitTest --tests "com.example.DecisionUnitTest"
./gradlew :app:testDebugUnitTest --tests "com.example.ExampleRobolectricTest"
# Other test classes: SmartFeaturesUnitTest, BackupRestoreRobolectricTest, RemoteCatalogRobolectricTest

# Android Lint check
./gradlew :app:lintDebug
```

## Architecture (summary — details in `docs/ARCHITECTURE.md`, grep it for the file/module you touch)
- Entry: `MainActivity.kt` (repos init, `LocalReduceMotion`, `AnimatedContent` routing; every `Screen` needs a `depth`). State: `ui/viewmodel/DecisionViewModel.kt`.
- Data: `data/local/AppDatabase.kt` (Room v5, migrations 1→5), `data/repository/*` (DB writes on `Dispatchers.IO.limitedParallelism(1)` — keep it), `data/backup/BackupManager.kt` (restore MERGES, never deletes).
- UI: `ui/screens/*`, `ui/components/*`, `ui/theme/*`. Screen roots are transparent (`CinematicBackdrop` behind them). New decorative animations must check `LocalReduceMotion.current`; `GenreRevealOverlay` uses one finite `Animatable`, no infinite transitions.
- Catalog: `GeneratedMovies.kt` / `GeneratedSeries.kt` / `catalog/catalog.json` are GENERATED (tools/tmdb_*_build.py) — never edit by hand.
- Hard rules: no fabricated "trending" data; platforms shown by name only (no brand colors/logos, "HBO Max" not BluTV); trailers via `ACTION_VIEW` only (no embedded player); TMDB attribution card/logo must stay; manifest permissions INTERNET / ACCESS_NETWORK_STATE / VIBRATE; HTTPS only; widget provider handles only `APPWIDGET_UPDATE`.
- Throwaway Robolectric smoke/visual tests must be deleted afterwards.

## Token budget (usage limits)
- Do NOT read whole large files: `GeneratedMovies.kt` (~190 KB), `GeneratedSeries.kt` (~100 KB), `MovieRepository.kt` (~64 KB), `catalog/catalog.json` (~220 KB), `tools/tmdb_poster_report.json`. Use Grep / `sed -n 'a,bp'` on the needed lines.
- Do not open `branding/*.png` or other images unless the user asks to see them.
- Run single test classes (`--tests ...`) while iterating; run the full suite once at the end. Pipe Gradle output through `tail -n 40` / grep for failures instead of printing it all.
- Do not spawn subagents unless the user asks; do not re-read files you just edited.

## Monetization rules (ads / paid) — read before adding any
- Nothing is monetized yet. Before ANY ads or paid features: get a TMDB commercial license (or ship `neizlesek.posterMode=cards` and drop TMDB data), update `PRIVACY.md`, README ("Ücretsiz · Reklamsız · Ticari olmayan" text) and the Play Data Safety form, and add a consent (UMP) flow for EEA/UK users.
- Ad placement: NEVER show full-screen/interstitial or rewarded ads during a spin, on the result/reveal screen, in the VS duel, or on app start. Allowed: a single small banner on Home/History/Profile, or one interstitial at most every few sessions on a neutral screen transition — never interrupting a decision.
- Always offer a one-time "Reklamsız" in-app purchase (Play Billing) if ads are added; no subscriptions for core features (spin, filters, history, backup stay free).
- No ads/analytics SDK may access the decision history, notes or backup files. Keep the YouTube trailer as an external intent (no embedded player).

## Conventions
- Interactive Compose nodes use `Modifier.testTag("snake_case_name")` — preserve existing `testTag` identifiers when editing UI so `ExampleRobolectricTest` stays green.
- Use `CinemaTheme.colors` / `MaterialTheme.colorScheme` tokens instead of hardcoded color literals in UI composables.
- All user-facing UI strings are in Turkish.

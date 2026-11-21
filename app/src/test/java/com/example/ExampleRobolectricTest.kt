package com.example

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.widget.TextView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.core.app.ApplicationProvider
import com.example.data.audio.WheelAudioPlayer
import com.example.data.audio.WheelSoundCue
import com.example.data.audio.WheelSoundEngine
import com.example.data.audio.WheelSpinFeedbackCoordinator
import com.example.data.haptic.WheelHapticController
import com.example.data.local.AppDatabase
import com.example.data.local.ThemePreferences
import com.example.data.local.toEntity
import com.example.data.model.Mood
import com.example.data.repository.CustomWheelListRepository
import com.example.data.repository.MovieRepository
import com.example.data.repository.RoomRepository
import com.example.data.repository.SavedDecision
import com.example.data.repository.WatchedMovieRepository
import com.example.data.share.SharePosterRenderer
import com.example.ui.components.buildShareIntentWithOptionalPoster
import com.example.widget.MovieWheelAppWidgetProvider
import com.example.widget.WidgetDisplayMode
import com.example.widget.WidgetMovieStateStore
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.SensorEventBuilder
import org.robolectric.shadows.ShadowSensor

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  /** Yeni kurulumda geçmiş boştur; geçmiş gerektiren testler kendi kararlarını ekler. */
  private fun seedSampleDecisions() {
    RoomRepository.recordSavedDecision(MovieRepository.allMovies[12], "Karar Çarkı")
    RoomRepository.recordSavedDecision(MovieRepository.allMovies[0], "Karar Çarkı")
    composeTestRule.waitForIdle()
  }

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    RoomRepository.clearRoom()
    RoomRepository.resetSavedDecisionsForTests()
    WatchedMovieRepository.clearAllForTests()
    CustomWheelListRepository.resetForTests()
    ThemePreferences.setDarkMode(true)
    WidgetMovieStateStore.clearForTests(context)
    com.example.data.repository.WatchLaterRepository.init(context)
    com.example.data.repository.WatchLaterRepository.clearForTests()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ne İzlesek?", appName)
  }

  @Test
  fun `solo flow opens wheel, spins, vetoes, and opens flex screen without crashing`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("veto_action_button"))
    composeTestRule.onNodeWithTag("veto_action_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_movie_poster"))
    composeTestRule.onNodeWithTag("wheel_movie_poster").assertIsDisplayed()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_direct_share_button"))
    composeTestRule.onNodeWithTag("wheel_direct_share_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("share_open_story_card_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("flex_story_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("flex_movie_poster").assertIsDisplayed()
  }

  @Test
  fun `tapping wheel canvas directly spins without crashing`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_canvas_box").performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_direct_share_button"))
    composeTestRule.onNodeWithTag("wheel_direct_share_button").assertIsDisplayed()
  }

  @Test
  fun `advanced filter criteria narrow selection and show active badges on wheel`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("advanced_filter_tab").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("genre_chip_komedi").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("year_chip_2010s").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("rating_chip_7.5_plus").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_active_filters_row").assertIsDisplayed()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_direct_share_button"))
    composeTestRule.onNodeWithTag("wheel_direct_share_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("share_open_story_card_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("flex_story_card").assertIsDisplayed()
  }

  @Test
  fun `quick mode inline narrowing panel expands and filters pool`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("quick_narrow_filters_card"))
    composeTestRule.onNodeWithTag("quick_narrow_filters_card").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("rating_chip_8.0_plus").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("launch_wheel_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_active_filters_row").assertIsDisplayed()
  }

  @Test
  fun `marking spun movie as watched adds to profile list and excludes from wheel`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("mark_watched_button"))
    composeTestRule.onNodeWithTag("mark_watched_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("watched_confirmed_badge"))
    composeTestRule.onNodeWithTag("watched_confirmed_badge").assertIsDisplayed()

    val watchedId = WatchedMovieRepository.watchedMovieIds.value.singleOrNull()
    assertTrue("One movie should be recorded as watched", watchedId != null)

    // Navigate back to Home -> Profile to verify watched movie appears in user's profile list
    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_back_button"))
    composeTestRule.onNodeWithTag("wheel_back_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("filter_back_button"))
    composeTestRule.onNodeWithTag("filter_back_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_icon_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_stats_chart_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_pie_chart").assertIsDisplayed()
    composeTestRule.onNodeWithTag("film_zevkiniz_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("film_zevkiniz_title").assertTextContains("FİLM ZEVKİNİZ")
    composeTestRule.onNodeWithTag("film_zevkiniz_summary").assertIsDisplayed()
    composeTestRule.onNodeWithTag("top_genre_summary").assertIsDisplayed()
    composeTestRule.onNodeWithTag("stats_mode_duration").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_watched_count").assertTextContains("1 Film", substring = true)
    composeTestRule.onNodeWithTag("profile_screen_list")
      .performScrollToNode(hasTestTag("watched_movie_card_$watchedId"))
    composeTestRule.onNodeWithTag("watched_movie_card_$watchedId").assertIsDisplayed()
    composeTestRule.onNodeWithTag("watched_movie_poster_$watchedId").assertIsDisplayed()

    // Add personal note
    composeTestRule.onNodeWithTag("note_button_$watchedId").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("note_input_$watchedId").performScrollTo().performTextInput("Koltukta çok güldük")
    composeTestRule.onNodeWithTag("save_note_button_$watchedId").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("movie_note_text_$watchedId").assertTextContains("Koltukta çok güldük")

    // Toggle Tekrar İzle ON
    composeTestRule.onNodeWithTag("rewatch_toggle_$watchedId").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("watched_status_badge_$watchedId").assertTextContains("Tekrar İzlenecek")
    assertFalse("Rewatch-enabled movie should be allowed back on wheel", WatchedMovieRepository.excludedWheelMovieIds.value.contains(watchedId))
  }

  @Test
  fun `home screen has no shared room entry and shows wheel entry title`() {
    composeTestRule.onAllNodesWithTag("room_mode_button").assertCountEquals(0)
    composeTestRule.onAllNodesWithTag("weekly_trends_section").assertCountEquals(0)
    composeTestRule.onNodeWithText("Karar Çarkını Çevir").assertExists()
  }

  @Test
  fun `wheel displays 3 similar alternative movies and allows switching to an alternative`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    val spunMovie = RoomRepository.currentRoom.value?.selectedMovie
    assertTrue("A movie should be selected after spin", spunMovie != null)

    val alternatives = com.example.data.repository.MovieRepository.findSimilarAlternatives(spunMovie!!)
    assertEquals(3, alternatives.size)
    val firstAlt = alternatives.first().movie

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("similar_movies_module"))
    composeTestRule.onNodeWithTag("similar_movies_module").assertIsDisplayed()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("select_alternative_button_${firstAlt.id}"))
    composeTestRule.onNodeWithTag("select_alternative_button_${firstAlt.id}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_selected_movie_title"))
    composeTestRule.onNodeWithTag("wheel_selected_movie_title").assertTextContains(firstAlt.title)
  }

  @Test
  fun `compare mode displays two side by side movies, supports skipping, picking, and sharing champion`() {
    composeTestRule.onNodeWithTag("compare_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("compare_left_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("compare_right_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("compare_round_label").assertTextContains("Tur 1", substring = true)

    composeTestRule.onNodeWithTag("compare_skip_pair_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("compare_pick_left_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("compare_round_label").assertTextContains("Tur 2", substring = true)

    composeTestRule.onNodeWithTag("compare_lock_right_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("compare_champion_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("compare_share_champion_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // "Hikâye Olarak Paylaş" 9:16 kazanan görselini sistem paylaşım menüsüyle paylaşır
    val shareIntent = org.robolectric.Shadows.shadowOf(composeTestRule.activity).nextStartedActivity
    assertTrue("Share must open the system chooser", shareIntent?.action == android.content.Intent.ACTION_CHOOSER)

    // Diğer paylaşım seçenekleri hâlâ hikâye kartı ekranını açar
    composeTestRule.onNodeWithTag("compare_more_share_options").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("flex_story_card").assertIsDisplayed()
  }

  @Test
  fun `theme toggle switches between light and dark cinematic themes across screens`() {
    assertTrue("App starts in dark cinematic mode", ThemePreferences.isDarkMode.value)

    composeTestRule.onNodeWithTag("home_settings_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("theme_toggle_button").performClick()
    composeTestRule.waitForIdle()
    assertFalse("Clicking toggle should switch to light cinematic mode", ThemePreferences.isDarkMode.value)
    composeTestRule.onNodeWithTag("settings_back_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_icon_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_screen_list")
      .performScrollToNode(hasTestTag("profile_theme_toggle_card"))
    composeTestRule.onNodeWithTag("profile_theme_toggle_card").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Clicking profile theme card should switch back to dark cinematic mode", ThemePreferences.isDarkMode.value)
  }

  @Test
  fun `horror movie spin triggers dedicated horror reveal animation`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("mood_card_${Mood.HORROR.id}"))
    composeTestRule.onNodeWithTag("mood_card_${Mood.HORROR.id}").performClick()
    composeTestRule.waitForIdle()

    // Deselect default EASY_WATCH so only HORROR titles fill the wheel
    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("mood_card_${Mood.EASY_WATCH.id}"))
    composeTestRule.onNodeWithTag("mood_card_${Mood.EASY_WATCH.id}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("horror_reveal_animation"))
    composeTestRule.onNodeWithTag("horror_reveal_animation").assertIsDisplayed()
  }

  @Test
  fun `non horror movie spin triggers standard cinematic reveal animation`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("mood_card_${Mood.COMEDY.id}"))
    composeTestRule.onNodeWithTag("mood_card_${Mood.COMEDY.id}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("cinematic_reveal_animation"))
    composeTestRule.onNodeWithTag("cinematic_reveal_animation").assertIsDisplayed()
  }

  @Test
  fun `wheel screen supports inline minimum imdb filter, favorite toggle, and social share sheet`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_min_imdb_filter_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("wheel_imdb_chip_8_0").performClick()
    composeTestRule.waitForIdle()

    val eligibleMovies = RoomRepository.currentRoom.value?.eligibleMovieIds
      ?.mapNotNull { MovieRepository.getMovieById(it) }
      .orEmpty()
    assertTrue("Eligible pool after 8.0+ filter must be non-empty", eligibleMovies.isNotEmpty())
    assertTrue(
      "All movies in room pool must have IMDb >= 8.0",
      eligibleMovies.all { it.imdb >= 8.0 }
    )

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_favorite_button"))
    composeTestRule.onNodeWithTag("wheel_favorite_button").performClick()
    composeTestRule.waitForIdle()
    assertTrue(
      "Favorite toggle on wheel must record movie in favoriteMovieIds",
      WatchedMovieRepository.favoriteMovieIds.value.isNotEmpty()
    )

    composeTestRule.onNodeWithTag("wheel_direct_share_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("social_share_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("share_copy_button").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("social_share_close_button").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `profile screen displays film collection wall, favorites section, and direct share sheet`() {
    val sampleMovie = MovieRepository.getMovieById("m13")!!
    WatchedMovieRepository.markAsWatched(sampleMovie)
    WatchedMovieRepository.toggleFavorite(sampleMovie.id)

    composeTestRule.onNodeWithTag("profile_icon_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_screen_list")
      .performScrollToNode(hasTestTag("profile_collection_wall"))
    composeTestRule.onNodeWithTag("profile_collection_wall").assertIsDisplayed()
    composeTestRule.onNodeWithTag("collection_wall_tile_${sampleMovie.id}").assertIsDisplayed()

    composeTestRule.onNodeWithTag("profile_screen_list")
      .performScrollToNode(hasTestTag("profile_favorites_section"))
    composeTestRule.onNodeWithTag("profile_favorites_section").assertIsDisplayed()

    composeTestRule.onNodeWithTag("profile_screen_list")
      .performScrollToNode(hasTestTag("favorite_share_button_${sampleMovie.id}"))
    composeTestRule.onNodeWithTag("favorite_share_button_${sampleMovie.id}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("social_share_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("share_platform_whatsapp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("social_share_close_button").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `shaking device triggers accelerometer listener and spins the wheel`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val shadowSensorManager = Shadows.shadowOf(sensorManager)
    val accelerometer = ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER)
    shadowSensorManager.addSensor(accelerometer)

    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_shake_sensor_card"))
    composeTestRule.onNodeWithTag("wheel_shake_sensor_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("shake_sensor_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("shake_intensity_bar").assertIsDisplayed()
    assertEquals(
      "Default accelerometer sensor should resolve to added shadow sensor",
      accelerometer,
      sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    )

    // Toggle off and back on to verify interactive sensor switch
    composeTestRule.onNodeWithTag("shake_sensor_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("shake_sensor_status_text", useUnmergedTree = true)
      .assertTextContains("Kapalı", substring = true)

    composeTestRule.onNodeWithTag("shake_sensor_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("shake_intensity_bar").assertIsDisplayed()

    // Dispatch two crisp shake accelerometer readings above threshold (~2.5g)
    val event1 = SensorEventBuilder.newBuilder()
      .setSensor(accelerometer)
      .setTimestamp(2_000_000_000L)
      .setValues(floatArrayOf(22.0f, 12.0f, 6.0f))
      .build()
    val event2 = SensorEventBuilder.newBuilder()
      .setSensor(accelerometer)
      .setTimestamp(2_250_000_000L)
      .setValues(floatArrayOf(-23.0f, 11.0f, 5.0f))
      .build()

    shadowSensorManager.sendSensorEventToListeners(event1)
    shadowSensorManager.sendSensorEventToListeners(event2)
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    assertTrue(
      "Shaking the device must trigger spin and reveal a selected movie",
      RoomRepository.currentRoom.value?.selectedMovie != null
    )
    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_selected_movie_title"))
    composeTestRule.onNodeWithTag("wheel_selected_movie_title").assertIsDisplayed()
  }

  @Test
  fun `wheel haptic controller emits escalating slice ticks and stop waveforms on vibrator`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vibrator = WheelHapticController.resolveVibrator(context)!!
    val shadowVibrator = Shadows.shadowOf(vibrator)
    shadowVibrator.setHasVibrator(true)
    shadowVibrator.setHasAmplitudeControl(true)

    var fakeClockMs = 1_000L
    val controller = WheelHapticController(
      vibrator = vibrator,
      timeProviderMs = { fakeClockMs },
      minTickIntervalMs = 24L
    )

    controller.playSpinStart()
    assertTrue("Spin start must trigger vibrator", shadowVibrator.isVibrating)
    assertEquals(22L, shadowVibrator.milliseconds)

    fakeClockMs += 40L
    controller.playSliceTick(spinProgress = 0.0f)
    assertEquals(10L, shadowVibrator.milliseconds)

    fakeClockMs += 40L
    controller.playSliceTick(spinProgress = 1.0f)
    assertEquals(21L, shadowVibrator.milliseconds)

    controller.playWheelStop(isHorror = false)
    assertTrue("Standard wheel stop must play double-pulse waveform", shadowVibrator.isVibrating)
    assertEquals(4, shadowVibrator.pattern?.size)

    controller.playWheelStop(isHorror = true)
    assertTrue("Horror wheel stop must play triple-pulse waveform", shadowVibrator.isVibrating)
    assertEquals(6, shadowVibrator.pattern?.size)

    controller.cancel()
    controller.isEnabled = false
    fakeClockMs += 100L
    controller.playSliceTick(spinProgress = 0.5f)
    assertFalse("Disabled haptic controller must not vibrate", shadowVibrator.isVibrating)
  }

  @Test
  fun `wheel screen triggers vibrator on spin and supports haptic toggle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vibrator = WheelHapticController.resolveVibrator(context)!!
    val shadowVibrator = Shadows.shadowOf(vibrator)
    shadowVibrator.setHasVibrator(true)
    shadowVibrator.setHasAmplitudeControl(true)

    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_haptic_toggle_button"))
    composeTestRule.onNodeWithTag("wheel_haptic_status_label", useUnmergedTree = true)
      .assertTextContains("Açık", substring = true)

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("spin_wheel_button"))
    composeTestRule.onNodeWithTag("spin_wheel_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    assertTrue(
      "Spinning and stopping the wheel must activate hardware vibrator",
      shadowVibrator.isVibrating || shadowVibrator.pattern != null || shadowVibrator.milliseconds > 0L
    )

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_haptic_toggle_button"))
    composeTestRule.onNodeWithTag("wheel_haptic_toggle_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("wheel_haptic_status_label", useUnmergedTree = true)
      .assertTextContains("Kapalı", substring = true)
  }

  @Test
  fun `wheel feedback coordinator synchronizes sound clicks with vibrator haptics`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vibrator = WheelHapticController.resolveVibrator(context)!!
    val shadowVibrator = Shadows.shadowOf(vibrator)
    shadowVibrator.setHasVibrator(true)
    shadowVibrator.setHasAmplitudeControl(true)

    var fakeClockMs = 5_000L
    val emittedCues = mutableListOf<WheelSoundCue>()
    val fakePlayer = object : WheelAudioPlayer {
      override fun play(cue: WheelSoundCue, volumeScale: Float) {
        emittedCues += cue
      }

      override fun stopAll() = Unit
      override fun release() = Unit
    }

    val hapticController = WheelHapticController(vibrator, timeProviderMs = { fakeClockMs }, minTickIntervalMs = 24L)
    val soundEngine = WheelSoundEngine(fakePlayer, timeProviderMs = { fakeClockMs }, minTickIntervalMs = 24L)
    val coordinator = WheelSpinFeedbackCoordinator(hapticController, soundEngine, timeProviderMs = { fakeClockMs })

    coordinator.onSpinStart()
    assertTrue("Spin start must vibrate", shadowVibrator.isVibrating)
    assertEquals(listOf(WheelSoundCue.SPIN_START), emittedCues)

    // Both fire at exact same slice crossing
    fakeClockMs += 50L
    assertTrue(coordinator.onSliceCrossing(0.15f))
    assertEquals(11L, shadowVibrator.milliseconds)
    assertEquals(WheelSoundCue.SLICE_TICK_FAST, emittedCues.last())

    // Both suppress inside 24ms window
    fakeClockMs += 12L
    assertFalse(coordinator.onSliceCrossing(0.30f))
    assertEquals(2, emittedCues.size)

    // Late crossing triggers slow deep clack + firmer haptic together
    fakeClockMs += 40L
    assertTrue(coordinator.onSliceCrossing(0.95f))
    assertEquals(20L, shadowVibrator.milliseconds)
    assertEquals(WheelSoundCue.SLICE_TICK_SLOW, emittedCues.last())

    // Horror stop fires triple-heartbeat vibration + horror audio cue together
    coordinator.onWheelStop(isHorror = true)
    assertEquals(6, shadowVibrator.pattern?.size)
    assertEquals(WheelSoundCue.STOP_HORROR, emittedCues.last())

    // Muting sound keeps haptic alive on next slice crossing
    soundEngine.isEnabled = false
    val cuesWhenMuted = emittedCues.size
    fakeClockMs += 60L
    assertTrue(coordinator.onSliceCrossing(0.50f))
    assertEquals(15L, shadowVibrator.milliseconds)
    assertEquals(cuesWhenMuted, emittedCues.size)
  }

  @Test
  fun `wheel screen displays sound engine toggle and flips state on tap`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_sound_toggle_button"))
    composeTestRule.onNodeWithTag("wheel_sound_status_label", useUnmergedTree = true)
      .assertTextContains("Açık", substring = true)

    composeTestRule.onNodeWithTag("wheel_sound_toggle_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("wheel_sound_status_label", useUnmergedTree = true)
      .assertTextContains("Kapalı", substring = true)

    composeTestRule.onNodeWithTag("wheel_sound_toggle_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("wheel_sound_status_label", useUnmergedTree = true)
      .assertTextContains("Açık", substring = true)
  }

  @Test
  fun `stats panel opens from home card and renders donut, column, and breakdown charts with tabs`() {
    seedSampleDecisions()
    val favMovie = MovieRepository.getMovieById("m13")!!
    val watchedRewatch = MovieRepository.getMovieById("m14")!!
    WatchedMovieRepository.toggleFavorite(favMovie.id)
    WatchedMovieRepository.markAsWatched(watchedRewatch)
    WatchedMovieRepository.toggleRewatch(watchedRewatch.id)

    composeTestRule.onNodeWithTag("home_screen_list")
      .performScrollToNode(hasTestTag("home_stats_entry_card"))
    composeTestRule.onNodeWithTag("home_stats_entry_card").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("genre_statistics_panel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("stats_kpi_spun_count").assertIsDisplayed()
    composeTestRule.onNodeWithTag("stats_kpi_liked_count").assertIsDisplayed()
    composeTestRule.onNodeWithTag("stats_kpi_alignment").assertIsDisplayed()

    composeTestRule.onNodeWithTag("genre_distribution_donut_chart").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_column_comparison_chart").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_horizontal_breakdown").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_stats_insight_box").performScrollTo().assertIsDisplayed()

    // Switch filter tabs cleanly
    composeTestRule.onNodeWithTag("stats_filter_spun").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("genre_distribution_donut_chart").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("stats_filter_liked").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("genre_distribution_donut_chart").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("stats_filter_compare").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("genre_column_comparison_chart").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `profile screen displays spun and liked genre statistics panel`() {
    val favMovie = MovieRepository.getMovieById("m13")!!
    WatchedMovieRepository.toggleFavorite(favMovie.id)

    composeTestRule.onNodeWithTag("profile_icon_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_screen_list")
      .performScrollToNode(hasTestTag("genre_statistics_panel"))
    composeTestRule.onNodeWithTag("genre_statistics_panel").assertIsDisplayed()

    composeTestRule.onNodeWithTag("profile_screen_list")
      .performScrollToNode(hasTestTag("genre_distribution_donut_chart"))
    composeTestRule.onNodeWithTag("genre_distribution_donut_chart").assertIsDisplayed()
  }

  @Test
  fun `home screen widget displays daily recommendation, spins without opening app, and switches back`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val shadowApp = Shadows.shadowOf(app)
    val appWidgetManager = AppWidgetManager.getInstance(app)
    val shadowAppWidgetManager = Shadows.shadowOf(appWidgetManager)

    val widgetId = shadowAppWidgetManager.createWidget(
      MovieWheelAppWidgetProvider::class.java,
      R.layout.widget_movie_wheel
    )
    val component = ComponentName(app, MovieWheelAppWidgetProvider::class.java)
    assertTrue(appWidgetManager.getAppWidgetIds(component).contains(widgetId))

    val provider = MovieWheelAppWidgetProvider()
    provider.onUpdate(app, appWidgetManager, intArrayOf(widgetId))

    val initialView = shadowAppWidgetManager.getViewFor(widgetId)
    val initialBadge = initialView.findViewById<TextView>(R.id.widget_mode_badge).text.toString()
    val dailyTitle = initialView.findViewById<TextView>(R.id.widget_movie_title).text.toString()
    val dailyImdb = initialView.findViewById<TextView>(R.id.widget_imdb_badge).text.toString()

    assertTrue("Initial widget mode should show daily recommendation", initialBadge.contains("GÜNÜN ÖNERİSİ"))
    assertTrue("Widget title must not be blank", dailyTitle.isNotBlank())
    assertTrue("Widget IMDb badge must display star rating", dailyImdb.contains("★"))

    // Drain any activity launch recorded by ComposeTestRule startup before spinning widget
    shadowApp.clearNextStartedActivities()

    // Trigger spin directly via widget broadcast action (without opening app)
    val actionReceiver = com.example.widget.WidgetActionReceiver()
    val spinIntent = Intent(app, com.example.widget.WidgetActionReceiver::class.java).apply {
      action = com.example.widget.WidgetActionReceiver.ACTION_WIDGET_SPIN
    }
    actionReceiver.onReceive(app, spinIntent)

    assertNull(
      "Spinning from home-screen widget must update widget in-place without launching MainActivity",
      shadowApp.nextStartedActivity
    )

    val spunState = WidgetMovieStateStore.getOrInitState(app)
    assertEquals(WidgetDisplayMode.SPIN, spunState.mode)
    assertEquals(1, spunState.spinCount)

    val spunView = shadowAppWidgetManager.getViewFor(widgetId)
    val spunBadge = spunView.findViewById<TextView>(R.id.widget_mode_badge).text.toString()
    val spunTitle = spunView.findViewById<TextView>(R.id.widget_movie_title).text.toString()
    assertTrue("Widget badge should reflect wheel spin #1", spunBadge.contains("ÇARK SEÇİMİ • #1"))
    assertTrue("Spinning widget must pick a different movie", spunTitle != dailyTitle)

    // Switch back to today's recommendation via widget broadcast action
    val dailyIntent = Intent(app, com.example.widget.WidgetActionReceiver::class.java).apply {
      action = com.example.widget.WidgetActionReceiver.ACTION_WIDGET_DAILY
    }
    actionReceiver.onReceive(app, dailyIntent)

    // Dışa açık sağlayıcı özel eylemleri artık işlememeli (başka uygulamalar tetikleyemesin)
    val stateBefore = WidgetMovieStateStore.getOrInitState(app)
    provider.onReceive(app, Intent(app, MovieWheelAppWidgetProvider::class.java).apply {
      action = com.example.widget.WidgetActionReceiver.ACTION_WIDGET_SPIN
    })
    assertEquals(stateBefore.spinCount, WidgetMovieStateStore.getOrInitState(app).spinCount)

    val restoredView = shadowAppWidgetManager.getViewFor(widgetId)
    val restoredBadge = restoredView.findViewById<TextView>(R.id.widget_mode_badge).text.toString()
    val restoredTitle = restoredView.findViewById<TextView>(R.id.widget_movie_title).text.toString()
    assertTrue("Widget badge should restore daily pick label", restoredBadge.contains("GÜNÜN ÖNERİSİ"))
    assertEquals("Restored daily title should match initial daily pick", dailyTitle, restoredTitle)
  }

  @Test
  fun `profile screen displays quick pin widget shortcut card`() {
    composeTestRule.onNodeWithTag("profile_icon_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_pin_widget_card").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `wheel screen automatically recolors wheel theme when toggling between dark and light themes`() {
    assertTrue("App starts in dark theme", ThemePreferences.isDarkMode.value)

    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_theme_badge"))
    composeTestRule.onNodeWithTag("wheel_theme_badge")
      .assertTextContains("Noir Sinema Çarkı", substring = true)

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_theme_toggle_button"))
    composeTestRule.onNodeWithTag("wheel_theme_toggle_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(450L)
    composeTestRule.waitForIdle()

    assertFalse("Theme toggle on WheelScreen must switch to light theme", ThemePreferences.isDarkMode.value)
    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_theme_badge"))
    composeTestRule.onNodeWithTag("wheel_theme_badge")
      .assertTextContains("Matine Fildişi Çarkı", substring = true)
    composeTestRule.onNodeWithTag("wheel_canvas_box").assertIsDisplayed()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_theme_toggle_button"))
    composeTestRule.onNodeWithTag("wheel_theme_toggle_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(450L)
    composeTestRule.waitForIdle()

    assertTrue("Second toggle must restore dark theme", ThemePreferences.isDarkMode.value)
    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_theme_badge"))
    composeTestRule.onNodeWithTag("wheel_theme_badge")
      .assertTextContains("Noir Sinema Çarkı", substring = true)
  }

  @Test
  fun `share poster renderer creates PNG files and attaches image stream to intents`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val movie = MovieRepository.getMovieById("m13")!!
    val favorites = MovieRepository.allMovies.take(4)

    val movieBitmap = SharePosterRenderer.renderSingleMoviePosterBitmap(movie, isDarkTheme = true)
    assertEquals(800, movieBitmap.width)
    assertEquals(1120, movieBitmap.height)

    val favoritesBitmap = SharePosterRenderer.renderFavoritesListPosterBitmap(favorites, isDarkTheme = false)
    assertEquals(800, favoritesBitmap.width)
    assertEquals(1120, favoritesBitmap.height)

    val movieUri = SharePosterRenderer.createMoviePosterShareUri(context, movie)
    assertTrue("Movie poster URI must be generated via FileProvider", movieUri != null)
    val cachedMoviePng = File(File(context.cacheDir, "shared_posters"), "movie_${movie.id}.png")
    assertTrue("Rendered movie PNG must exist on disk", cachedMoviePng.exists() && cachedMoviePng.length() > 1_000L)

    val favUri = SharePosterRenderer.createFavoritesPosterShareUri(context, favorites)
    assertTrue("Favorites collage URI must be generated via FileProvider", favUri != null)
    val cachedFavPng = File(File(context.cacheDir, "shared_posters"), "favorites_collage_${favorites.size}.png")
    assertTrue("Rendered favorites PNG must exist on disk", cachedFavPng.exists() && cachedFavPng.length() > 1_000L)

    val shareIntent = buildShareIntentWithOptionalPoster(
      context = context,
      subject = "Ne İzlesek?",
      shareText = "Film önerisi",
      posterUri = movieUri
    )
    assertEquals(Intent.ACTION_SEND, shareIntent.action)
    assertEquals("image/png", shareIntent.type)
    @Suppress("DEPRECATION")
    assertEquals(movieUri, shareIntent.getParcelableExtra(Intent.EXTRA_STREAM))
    assertTrue((shareIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
  }

  @Test
  fun `profile favorites list share and home saved decision share open poster dialogs`() {
    seedSampleDecisions()
    val favA = MovieRepository.getMovieById("m13")!!
    val favB = MovieRepository.getMovieById("m14")!!
    WatchedMovieRepository.toggleFavorite(favA.id)
    WatchedMovieRepository.toggleFavorite(favB.id)

    // 1. Saved decision quick poster share directly from HomeScreen
    val firstDecision = RoomRepository.savedDecisions.value.first()
    composeTestRule.onNodeWithTag("home_screen_list")
      .performScrollToNode(hasTestTag("home_decision_share_${firstDecision.id}"))
    composeTestRule.onNodeWithTag("home_decision_share_${firstDecision.id}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("social_share_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("share_poster_ready_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("social_share_close_button").performClick()
    composeTestRule.waitForIdle()

    // 2. Favorites list collage share dialog from ProfileScreen
    composeTestRule.onNodeWithTag("home_screen_list")
      .performScrollToNode(hasTestTag("profile_icon_button"))
    composeTestRule.onNodeWithTag("profile_icon_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_screen_list")
      .performScrollToNode(hasTestTag("share_favorites_list_button"))
    composeTestRule.onNodeWithTag("share_favorites_list_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("favorites_social_share_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("share_poster_ready_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("favorites_share_copy_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("favorites_share_close_button").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `wheel screen highlights winning slice badge when spin stops`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo()
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.onNodeWithTag("spin_wheel_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)

    composeTestRule.onNodeWithTag("wheel_spinning_badge").assertExists()

    // Resume clock & finish smooth 4.4s spin
    composeTestRule.mainClock.autoAdvance = true
    composeTestRule.mainClock.advanceTimeBy(4800L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_winner_highlight_badge"))
    composeTestRule.onNodeWithTag("wheel_winner_highlight_badge")
      .assertIsDisplayed()
      .assertTextContains("Seçilen Film Vurgulandı", substring = true)
  }

  @Test
  fun `compare mode supports tinder horizontal swipe left and right gestures`() {
    composeTestRule.onNodeWithTag("compare_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("compare_round_label")
      .assertTextContains("Tur 1", substring = true)

    // Tinder swipe left -> keeps left movie and advances to round 2
    composeTestRule.onNodeWithTag("compare_duel_arena").performTouchInput {
      swipeLeft(startX = centerX + 120f, endX = centerX - 180f, durationMillis = 250L)
    }
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("compare_round_label")
      .assertTextContains("Tur 2", substring = true)

    // Tinder swipe right -> keeps right movie and advances to round 3
    composeTestRule.onNodeWithTag("compare_duel_arena").performTouchInput {
      swipeRight(startX = centerX - 120f, endX = centerX + 180f, durationMillis = 250L)
    }
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("compare_round_label")
      .assertTextContains("Tur 3", substring = true)
  }

  @Test
  fun `custom wheel lists screen displays starter presets, creates custom category, and spins on wheel`() {
    composeTestRule.onNodeWithTag("custom_lists_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("custom_lists_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("custom_lists_screen")
      .performScrollToNode(hasTestTag("custom_list_card_preset_korku_gecesi"))
    composeTestRule.onNodeWithTag("custom_list_card_preset_korku_gecesi").assertIsDisplayed()

    composeTestRule.onNodeWithTag("custom_lists_screen")
      .performScrollToNode(hasTestTag("custom_lists_create_button"))
    composeTestRule.onNodeWithTag("custom_lists_create_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("custom_list_editor_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("custom_list_name_input").performTextInput("Arkadaş Gecesi")
    composeTestRule.onNodeWithTag("custom_list_editor_list")
      .performScrollToNode(hasTestTag("custom_movie_title_input"))
    composeTestRule.onNodeWithTag("custom_movie_title_input").performTextInput("The Matrix")
    composeTestRule.onNodeWithTag("add_custom_movie_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("custom_list_editor_list")
      .performScrollToNode(hasTestTag("catalog_movie_toggle_m13"))
    composeTestRule.onNodeWithTag("catalog_movie_toggle_m13").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("custom_list_editor_list")
      .performScrollToNode(hasTestTag("save_custom_list_button"))
    composeTestRule.onNodeWithTag("save_custom_list_button").performClick()
    composeTestRule.waitForIdle()

    val createdList = CustomWheelListRepository.customLists.value.first { it.name == "Arkadaş Gecesi" }
    composeTestRule.onNodeWithTag("custom_lists_screen")
      .performScrollToNode(hasTestTag("spin_custom_list_${createdList.id}"))
    composeTestRule.onNodeWithTag("spin_custom_list_${createdList.id}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_custom_list_badge"))
    composeTestRule.onNodeWithTag("wheel_custom_list_badge")
      .assertIsDisplayed()
      .assertTextContains("Arkadaş Gecesi", substring = true)

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(4800L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_winner_highlight_badge"))
    composeTestRule.onNodeWithTag("wheel_winner_highlight_badge").assertIsDisplayed()
  }

  @Test
  fun `filter screen quick custom category chip launches wheel with preset pool`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_custom_list_preset_aile_dostu")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_custom_list_badge"))
    composeTestRule.onNodeWithTag("wheel_custom_list_badge")
      .assertIsDisplayed()
      .assertTextContains("Aile Dostu", substring = true)
  }

  @Test
  fun `savedDecisionDao persists queries deletes and clears wheel selections in Room`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dao = AppDatabase.getInstance(context).savedDecisionDao()

    runBlocking {
      dao.clearAll()
      val older = SavedDecision(
        id = "dao_test_1",
        movie = MovieRepository.allMovies[0],
        dateText = "Dün • 20:00",
        minutesSaved = 40,
        mode = "Tek Başına",
        selectedAtMillis = 1_000L
      )
      val newerCustom = SavedDecision(
        id = "dao_test_2",
        movie = CustomWheelListRepository.createCustomMovie(
          title = "Oldboy",
          genre = "Gerilim / Gizem",
          year = 2003,
          imdb = 8.3
        ),
        dateText = "Bugün • 21:45",
        minutesSaved = 55,
        mode = "Özel Çark (Klasikler)",
        selectedAtMillis = 2_000L
      )
      dao.upsertDecisions(listOf(older.toEntity(), newerCustom.toEntity()))

      val loaded = dao.getAllDecisionsOnce()
      assertEquals(2, loaded.size)
      assertEquals("dao_test_2", loaded[0].id)
      assertEquals("Oldboy", loaded[0].toDomain().movie.title)
      assertEquals("dao_test_1", loaded[1].id)

      dao.deleteDecisionById("dao_test_1")
      val afterDelete = dao.getAllDecisionsOnce()
      assertEquals(1, afterDelete.size)
      assertEquals("dao_test_2", afterDelete[0].id)

      dao.clearAll()
      assertTrue(dao.getAllDecisionsOnce().isEmpty())
    }
  }

  @Test
  fun `spinning wheel records movie into Geçmişte Ne Seçtik screen with search filter and delete`() {
    // 1. Spin a solo movie wheel so a new decision is recorded
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(4800L)
    composeTestRule.waitForIdle()

    val recorded = RoomRepository.savedDecisions.value.first()

    // 2. Navigate back to Home -> open 'Geçmişte Ne Seçtik?' via home_open_history_button
    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_back_button"))
    composeTestRule.onNodeWithTag("wheel_back_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("filter_back_button"))
    composeTestRule.onNodeWithTag("filter_back_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("home_screen_list")
      .performScrollToNode(hasTestTag("home_open_history_button"))
    composeTestRule.onNodeWithTag("home_open_history_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("history_screen_title")
      .assertIsDisplayed()
      .assertTextContains("Geçmişte Ne Seçtik?", substring = false)

    // 3. Verify the spun movie is displayed in the chronology list
    composeTestRule.onNodeWithTag("history_screen_list")
      .performScrollToNode(hasTestTag("history_decision_card_${recorded.id}"))
    composeTestRule.onNodeWithTag("history_decision_card_${recorded.id}").assertIsDisplayed()

    // 4. Filter using search query & mode chip
    composeTestRule.onNodeWithTag("history_screen_list")
      .performScrollToNode(hasTestTag("history_filter_solo"))
    composeTestRule.onNodeWithTag("history_filter_solo").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("history_search_input").performTextInput(recorded.movie.title)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("history_screen_list")
      .performScrollToNode(hasTestTag("history_delete_button_${recorded.id}"))
    composeTestRule.onNodeWithTag("history_delete_button_${recorded.id}").performClick()
    composeTestRule.waitForIdle()

    assertFalse(RoomRepository.savedDecisions.value.any { it.id == recorded.id })
    composeTestRule.onNodeWithTag("history_empty_state").assertIsDisplayed()
  }

  @Test
  fun `home open history button navigates to Geçmişte Ne Seçtik and clear all empties list`() {
    seedSampleDecisions()
    composeTestRule.onNodeWithTag("home_screen_list")
      .performScrollToNode(hasTestTag("home_open_history_button"))
    composeTestRule.onNodeWithTag("home_open_history_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("history_screen_title")
      .assertIsDisplayed()
      .assertTextContains("Geçmişte Ne Seçtik?", substring = false)

    composeTestRule.onNodeWithTag("history_clear_all_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("history_clear_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("history_confirm_clear_button").performClick()
    composeTestRule.waitForIdle()

    assertTrue(RoomRepository.savedDecisions.value.isEmpty())
    composeTestRule.onNodeWithTag("history_screen_list")
      .performScrollToNode(hasTestTag("history_empty_state"))
    composeTestRule.onNodeWithTag("history_empty_state").assertIsDisplayed()
  }

  @Test
  fun `wheel spin renders animation genre visual reveal when animation movie is selected`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("mood_card_${Mood.ANIMATION.id}"))
    composeTestRule.onNodeWithTag("mood_card_${Mood.ANIMATION.id}").performClick()
    composeTestRule.waitForIdle()

    // Deselect default EASY_WATCH so only ANIMATION titles remain
    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("mood_card_${Mood.EASY_WATCH.id}"))
    composeTestRule.onNodeWithTag("mood_card_${Mood.EASY_WATCH.id}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("launch_wheel_button"))
    composeTestRule.onNodeWithTag("launch_wheel_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("genre_reveal_animation"))
    composeTestRule.onNodeWithTag("genre_reveal_animation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_reveal_badge_animation")
      .assertIsDisplayed()
      .assertTextContains("ANİMASYON", substring = false)
  }

  @Test
  fun `wheel spin renders scifi genre visual reveal when scifi custom wheel spins`() {
    val scifiList = CustomWheelListRepository.saveCustomList(
      name = "Bilim Kurgu Gecesi",
      emoji = "🚀",
      description = "Uzay ve zaman",
      movies = listOf(MovieRepository.getMovieById("m13")!!, MovieRepository.getMovieById("m14")!!),
      accentColorHex = 0xFF06B6D4L
    )

    composeTestRule.onNodeWithTag("custom_lists_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("custom_lists_screen")
      .performScrollToNode(hasTestTag("spin_custom_list_${scifiList.id}"))
    composeTestRule.onNodeWithTag("spin_custom_list_${scifiList.id}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(5000L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("genre_reveal_scifi"))
    composeTestRule.onNodeWithTag("genre_reveal_scifi").assertIsDisplayed()
    composeTestRule.onNodeWithTag("genre_reveal_badge_scifi")
      .assertIsDisplayed()
      .assertTextContains("BİLİM KURGU", substring = false)
  }

  @Test
  fun `home screen displays top daily Yeni Çıkanlar scrolling poster strip and opens wheel on click`() {
    composeTestRule.onNodeWithTag("new_releases_strip_section").assertIsDisplayed()
    composeTestRule.onNodeWithTag("new_releases_carousel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("new_releases_date_badge").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("new_releases_refresh_button").assertCountEquals(0)

    val firstNewMovie = MovieRepository.dailyUpdatedMovies.value.first()
    composeTestRule.onNodeWithTag("new_releases_carousel")
      .performScrollToNode(hasTestTag("new_release_card_${firstNewMovie.id}"))
    composeTestRule.onNodeWithTag("new_release_card_${firstNewMovie.id}")
      .performScrollTo()
      .assertIsDisplayed()
      .performClick()
    composeTestRule.waitForIdle()

    // Afişe dokunmak önce film kartını açar; çarka geçiş "Çarka Ekle" ile
    composeTestRule.onNodeWithTag("movie_detail_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("movie_detail_title").assertTextContains(firstNewMovie.title, substring = false)
    composeTestRule.onNodeWithTag("movie_detail_open_wheel").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_selected_movie_title"))
    composeTestRule.onNodeWithTag("wheel_selected_movie_title")
      .assertIsDisplayed()
      .assertTextContains(firstNewMovie.title, substring = false)
    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_movie_audio_badge"))
    composeTestRule.onNodeWithTag("wheel_movie_audio_badge").assertIsDisplayed()
  }

  @Test
  fun `filter screen horror mood shows multiple movies, filters dublaj altyazi, and launches Diğer cult category`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Select HORROR mood and deselect default EASY_WATCH
    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("mood_card_${Mood.HORROR.id}"))
    composeTestRule.onNodeWithTag("mood_card_${Mood.HORROR.id}").performClick()
    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("mood_card_${Mood.EASY_WATCH.id}"))
    composeTestRule.onNodeWithTag("mood_card_${Mood.EASY_WATCH.id}").performClick()
    composeTestRule.waitForIdle()

    // Horror pool on Netflix+Prime must have > 1 movie now (not just 1!)
    val horrorPool = MovieRepository.filterMoviesWithCriteria(
      moods = setOf(Mood.HORROR.id),
      platforms = setOf("Netflix", "Prime Video")
    ).movies
    assertTrue("Horror pool must have multiple movies", horrorPool.size >= 4)

    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("pool_summary_count"))
    composeTestRule.onNodeWithTag("pool_summary_count")
      .assertTextContains("${horrorPool.size} Film Çarka Eklendi", substring = true)

    // Verify Turkish Dublaj & Altyazi selector controls
    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("audio_filter_section"))
    composeTestRule.onNodeWithTag("audio_filter_section").assertIsDisplayed()
    composeTestRule.onNodeWithTag("audio_chip_dublaj").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_chip_altyazi").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_chip_all").performClick()
    composeTestRule.waitForIdle()

    // Verify 'Diğer: Kült & Çok İzlenenler' category & spin trigger
    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("other_cult_category_section"))
    composeTestRule.onNodeWithTag("other_cult_category_section").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filter_screen_list")
      .performScrollToNode(hasTestTag("spin_cult_other_button"))
    composeTestRule.onNodeWithTag("spin_cult_other_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_pool_count_badge"))
    composeTestRule.onNodeWithTag("wheel_pool_count_badge").assertIsDisplayed()
  }

  @Test
  fun `savedDecisionDao aggregates most selected movies and selection modes in SQLite`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dao = AppDatabase.getInstance(context).savedDecisionDao()
    val movieA = MovieRepository.getMovieById("m13")!!
    val movieB = MovieRepository.getMovieById("m1")!!

    runBlocking {
      dao.clearAll()
      dao.upsertDecisions(
        listOf(
          SavedDecision("agg1", movieA, "Bugün", 45, "Tek Başına", 5_000L).toEntity(),
          SavedDecision("agg2", movieA, "Dün", 45, "Tek Başına", 4_000L).toEntity(),
          SavedDecision("agg3", movieA, "Önceki", 50, "Ortak Oda (4422)", 3_000L).toEntity(),
          SavedDecision("agg4", movieB, "Geçen Hafta", 40, "Film Karşılaştır (VS)", 2_000L).toEntity()
        )
      )

      val topMovies = dao.getMostSelectedMoviesOnce()
      assertEquals(2, topMovies.size)
      assertEquals(movieA.id, topMovies[0].movieId)
      assertEquals(3, topMovies[0].selectionCount)
      assertEquals(movieB.id, topMovies[1].movieId)
      assertEquals(1, topMovies[1].selectionCount)

      val observedFlow = dao.observeMostSelectedMovies().first()
      assertEquals(3, observedFlow.first().selectionCount)

      val modeStats = dao.observeModeSelectionStats().first()
      assertEquals("Tek Başına", modeStats.first().modeLabel)
      assertEquals(2, modeStats.first().selectionCount)

      dao.clearAll()
    }
  }

  @Test
  fun `decision history dashboard displays most selected genres and movies from history`() {
    val topMovie = MovieRepository.getMovieById("m13")!!
    RoomRepository.clearSavedHistory()
    RoomRepository.recordSavedDecision(topMovie, "Tek Başına", minutesSaved = 45)
    RoomRepository.recordSavedDecision(topMovie, "Ortak Oda (5511)", minutesSaved = 50)
    RoomRepository.recordSavedDecision(topMovie, "Film Karşılaştır (VS)", minutesSaved = 45)

    // Open History -> jump straight to DB Dashboard via history_open_dashboard_button
    composeTestRule.onNodeWithTag("home_screen_list")
      .performScrollToNode(hasTestTag("home_open_history_button"))
    composeTestRule.onNodeWithTag("home_open_history_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("history_open_dashboard_button")
      .performScrollTo()
      .assertIsDisplayed()
      .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("stats_screen_list")
      .performScrollToNode(hasTestTag("decision_history_dashboard"))
    composeTestRule.onNodeWithTag("decision_history_dashboard").assertIsDisplayed()

    composeTestRule.onNodeWithTag("dashboard_kpi_total_selections")
      .performScrollTo()
      .assertIsDisplayed()
      .assertTextContains("3", substring = true)

    composeTestRule.onNodeWithTag("dashboard_top_genres_section")
      .performScrollTo()
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_genres_spectrum_canvas")
      .performScrollTo()
      .assertIsDisplayed()

    composeTestRule.onNodeWithTag("dashboard_top_movies_section")
      .performScrollTo()
      .assertIsDisplayed()

    composeTestRule.onNodeWithTag("dashboard_movie_row_${topMovie.id}")
      .performScrollTo()
      .assertIsDisplayed()
      .assertTextContains("3x seçildi", substring = true)

    composeTestRule.onNodeWithTag("dashboard_movie_count_${topMovie.id}", useUnmergedTree = true)
      .assertIsDisplayed()
      .assertTextContains("3x seçildi", substring = true)

    composeTestRule.onNodeWithTag("dashboard_insight_box")
      .performScrollTo()
      .assertIsDisplayed()
  }

  @Test
  fun `crash reporter saves the stack trace and shows a share dialog on next launch`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    com.example.crash.CrashReporter.clear(app)
    com.example.crash.CrashReporter.write(app, Thread.currentThread(), IllegalStateException("Deneme çökmesi"))
    val report = com.example.crash.CrashReporter.pendingReport(app)
    assertTrue(report != null && report.contains("IllegalStateException") && report.contains("Deneme çökmesi"))
    assertTrue("Report must include device info", report!!.contains("Android"))

    composeTestRule.activityRule.scenario.recreate()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("crash_report_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("crash_report_share_button").performClick()
    composeTestRule.waitForIdle()
    assertNull("Report is cleared after sharing", com.example.crash.CrashReporter.pendingReport(app))
  }

  @Test
  fun `new release card opens movie detail with watch later and shows it in profile`() {
    val movie = MovieRepository.dailyUpdatedMovies.value.first()
    composeTestRule.onNodeWithTag("new_releases_carousel")
      .performScrollToNode(hasTestTag("new_release_card_${movie.id}"))
    composeTestRule.onNodeWithTag("new_release_card_${movie.id}").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("movie_detail_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("movie_detail_watch_later").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(com.example.data.repository.WatchLaterRepository.contains(movie.id))
    composeTestRule.onNodeWithTag("movie_detail_close").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profile_icon_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profile_screen_list")
      .performScrollToNode(hasTestTag("profile_watch_later_section"))
    composeTestRule.onNodeWithTag("watch_later_item_${movie.id}").assertIsDisplayed()
  }

  @Test
  fun `manifest requests real internet, network state and vibrate permissions`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val info = app.packageManager.getPackageInfo(app.packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
    val requested = info.requestedPermissions?.toSet().orEmpty()
    // Yanlış "android.intent.permission.*" adları Android tarafından yok sayılır ve uygulama internetsiz kalır
    assertTrue(requested.contains(android.Manifest.permission.INTERNET))
    assertTrue(requested.contains(android.Manifest.permission.ACCESS_NETWORK_STATE))
    assertTrue(requested.contains(android.Manifest.permission.VIBRATE))
    assertTrue(requested.none { it.startsWith("android.intent.permission.") })
  }

  @Test
  fun `home search finds a movie and opens its detail card`() {
    val movie = MovieRepository.allMovies.first { it.title.length >= 5 }
    composeTestRule.onNodeWithTag("home_search_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("movie_search_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("movie_search_field").performTextInput(movie.title)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("movie_search_result_${movie.id}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(movie.logline, substring = true).assertExists()
  }

  @Test
  fun `settings screen toggles reduce motion and shows backup buttons`() {
    com.example.data.local.MotionPreferences.resetForTests()
    composeTestRule.onNodeWithTag("home_settings_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_screen_list")
      .performScrollToNode(hasTestTag("profile_settings_section"))
    composeTestRule.onNodeWithTag("profile_backup_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_restore_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_reduce_motion_switch").performClick()
    composeTestRule.waitForIdle()
    assertTrue(com.example.data.local.MotionPreferences.reduceMotion.value)
    composeTestRule.onNodeWithTag("profile_reduce_motion_switch").performClick()
    composeTestRule.waitForIdle()
    assertFalse(com.example.data.local.MotionPreferences.reduceMotion.value)
  }

  @Test
  fun `bottom navigation opens upcoming, popular, series and forum tabs`() {
    composeTestRule.onNodeWithTag("bottom_nav_upcoming").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("upcoming_screen_grid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottom_nav_popular").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("popular_screen_grid").assertIsDisplayed()
    val firstPopular = com.example.data.repository.HighlightsRepository.popularMovies().first()
    composeTestRule.onNodeWithTag("popular_item_${firstPopular.id}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("movie_detail_open_wheel").assertExists()
    composeTestRule.onNodeWithTag("movie_detail_close").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("bottom_nav_series").performClick()
    composeTestRule.waitForIdle()
    val firstSeries = com.example.data.repository.HighlightsRepository.popularSeries().first()
    assertTrue(firstSeries.isSeries)
    composeTestRule.onNodeWithTag("series_item_${firstSeries.id}").assertExists()
    composeTestRule.onNodeWithTag("bottom_nav_forum").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("forum_coming_soon_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("home_screen_list").assertIsDisplayed()
  }

  @Test
  fun `taste onboarding marks picks as watched and liked and fills personal picks`() {
    composeTestRule.onNodeWithTag("home_settings_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasTestTag("settings_redo_onboarding"))
    composeTestRule.onNodeWithTag("settings_redo_onboarding").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("onboarding_screen").assertIsDisplayed()
    val candidates = com.example.data.repository.HighlightsRepository.popularMovies()
    val watched = candidates[0]
    val liked = candidates[1]
    composeTestRule.onNodeWithTag("onboarding_item_${watched.id}").performClick()
    composeTestRule.onNodeWithTag("onboarding_item_${liked.id}").performClick()
    composeTestRule.onNodeWithTag("onboarding_item_${liked.id}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("onboarding_counter").assertTextContains("1 izledim • 1 beğendim")
    composeTestRule.onNodeWithTag("onboarding_finish_button").performClick()
    composeTestRule.waitForIdle()
    assertTrue(WatchedMovieRepository.isWatched(watched.id))
    assertTrue(WatchedMovieRepository.isWatched(liked.id))
    assertTrue(WatchedMovieRepository.isFavorite(liked.id))
    composeTestRule.onNodeWithTag("home_screen_list").performScrollToNode(hasTestTag("home_personal_picks"))
    composeTestRule.onNodeWithTag("home_personal_picks").assertIsDisplayed()
  }

  @Test
  fun `worst movies smart wheel opens from filter strip and spins`() {
    composeTestRule.onNodeWithTag("solo_mode_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("filter_custom_lists_row")
      .performScrollToNode(hasTestTag("filter_custom_list_${com.example.data.repository.SmartWheelLists.WORST}"))
    composeTestRule.onNodeWithTag("filter_custom_list_${com.example.data.repository.SmartWheelLists.WORST}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("spin_wheel_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(6000L)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("wheel_screen_list")
      .performScrollToNode(hasTestTag("wheel_winner_highlight_badge"))
    composeTestRule.onNodeWithTag("wheel_winner_highlight_badge").assertExists()
  }

  @Test
  fun `manifest disables backup and keeps widget action receiver private`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val pm = app.packageManager
    val appInfo = pm.getApplicationInfo(app.packageName, 0)
    assertEquals(0, appInfo.flags and android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP)
    val actionReceiver = pm.getReceiverInfo(
      ComponentName(app, com.example.widget.WidgetActionReceiver::class.java), 0
    )
    assertFalse("Widget eylem alıcısı dışa açık olmamalı", actionReceiver.exported)
  }

  @Test
  fun `crash report screen shows the saved report`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    com.example.crash.CrashReporter.write(app, Thread.currentThread(), IllegalStateException("Açılış hatası"))
    val controller = org.robolectric.Robolectric.buildActivity(com.example.crash.CrashReportActivity::class.java).setup()
    val root = controller.get().window.decorView
    fun texts(v: android.view.View): List<String> = when (v) {
      is android.widget.TextView -> listOf(v.text.toString())
      is android.view.ViewGroup -> (0 until v.childCount).flatMap { texts(v.getChildAt(it)) }
      else -> emptyList()
    }
    val all = texts(root).joinToString("\n")
    assertTrue(all.contains("Açılış hatası"))
    assertTrue(all.contains("Raporu Paylaş"))
    com.example.crash.CrashReporter.clear(app)
  }
}

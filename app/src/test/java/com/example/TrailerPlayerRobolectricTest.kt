package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.data.repository.MovieRepository
import com.example.ui.components.TrailerButton
import com.example.ui.components.TrailerLauncher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TrailerPlayerRobolectricTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun embedUrl_usesPrivacyEnhancedYoutubeAndRejectsUnsafeKeys() {
        assertEquals(
            "https://www.youtube-nocookie.com/embed/abcdefghijk?autoplay=1&playsinline=1&rel=0",
            TrailerLauncher.embedUrl("abcdefghijk")
        )
        assertNull(TrailerLauncher.embedUrl("\"><script>alert(1)</script>"))
        assertNull(TrailerLauncher.embedUrl("a b"))
    }

    @Test
    fun trailerButton_opensInAppPlayerWindowWithYoutubeFallback() {
        val movie = MovieRepository.allMovies.first { it.trailerKey != null }
        rule.setContent { TrailerButton(movie = movie) }
        rule.onNodeWithTag("trailer_button").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("trailer_player_dialog").assertIsDisplayed()
        rule.onNodeWithTag("trailer_open_youtube").assertIsDisplayed()
        rule.onNodeWithTag("trailer_player_close").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("trailer_player_dialog").assertDoesNotExist()
    }
}

package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.MovieRepository
import com.example.ui.components.TrailerButton
import com.example.ui.components.TrailerLauncher
import com.example.ui.components.TrailerPlayerActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
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
    fun trailerButton_opensDedicatedPlayerScreen() {
        val movie = MovieRepository.allMovies.first { it.trailerKey != null }
        rule.setContent { TrailerButton(movie = movie) }
        rule.onNodeWithTag("trailer_button").performClick()
        rule.waitForIdle()
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val started = Shadows.shadowOf(app).nextStartedActivity
        assertNotNull(started)
        assertEquals(TrailerPlayerActivity::class.java.name, started.component?.className)
        assertEquals(movie.trailerKey, started.getStringExtra(TrailerPlayerActivity.EXTRA_KEY))
    }

    @Test
    fun playerActivity_buildsWebViewAndFinishesOnInvalidKey() {
        val ok = Robolectric.buildActivity(
            TrailerPlayerActivity::class.java,
            TrailerPlayerActivity.intent(ApplicationProvider.getApplicationContext(), "Film", "abcdefghijk")
        ).setup().get()
        assertTrue(!ok.isFinishing)
        val bad = Robolectric.buildActivity(
            TrailerPlayerActivity::class.java,
            TrailerPlayerActivity.intent(ApplicationProvider.getApplicationContext(), "Film", "<bad>")
        ).setup().get()
        assertTrue(bad.isFinishing)
    }
}

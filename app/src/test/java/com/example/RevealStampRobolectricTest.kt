package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertTextContains
import com.example.data.repository.HighlightsRepository
import com.example.data.repository.SmartWheelLists
import androidx.compose.ui.test.assertIsDisplayed
import com.example.ui.components.GenreRevealOverlay
import com.example.ui.components.RezaletMeter
import com.example.ui.components.VetoStampOverlay
import com.example.ui.components.WorstWheelHeader
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RevealStampRobolectricTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun worstWheelReveal_showsYouAskedForItStamp() {
        val worst = HighlightsRepository.worstMovies()
        assertTrue("En kötüler listesi dolu olmalı", worst.size >= 10)
        assertTrue("Puana göre artan sırada", worst.zipWithNext().all { (a, b) -> a.imdb <= b.imdb })
        assertTrue(SmartWheelLists.byId(SmartWheelLists.WORST) != null)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            GenreRevealOverlay(movie = worst.first(), onFinished = {}, stampText = "Bunu sen istedin! 🤷")
        }
        rule.mainClock.advanceTimeBy(2500L)
        rule.onNodeWithTag("reveal_stamp_text", useUnmergedTree = true).assertTextContains("Bunu sen istedin", substring = true)
    }

    @Test
    fun vetoStamp_landsOnPoster_thenFinishesOnItsOwn() {
        val movie = HighlightsRepository.worstMovies().first()
        val finished = mutableStateOf(false)
        rule.mainClock.autoAdvance = false
        rule.setContent { VetoStampOverlay(movie = movie, onFinished = { finished.value = true }) }
        rule.mainClock.advanceTimeBy(700L)
        rule.onNodeWithTag("veto_stamp_overlay").assertIsDisplayed()
        rule.onNodeWithText("VETO", useUnmergedTree = true).assertExists()
        assertTrue("Mühür animasyonu henüz bitmemeli", !finished.value)
        rule.mainClock.advanceTimeBy(2500L)
        assertTrue("Mühür animasyonu kendiliğinden bitmeli", finished.value)
    }

    @Test
    fun worstWheelHeaderAndRezaletMeter_render() {
        val worst = HighlightsRepository.worstMovies()
        rule.setContent {
            Column {
                WorstWheelHeader()
                RezaletMeter(movies = worst.take(12), poolTotal = worst.size)
            }
        }
        rule.onNodeWithTag("worst_wheel_header").assertIsDisplayed()
        rule.onNodeWithTag("rezalet_meter").assertIsDisplayed()
    }

    @Test
    fun resultScreen_staysOpen_andVetoStampThenCallback() {
        val movie = HighlightsRepository.worstMovies().first()
        var vetoed = false
        var closed = false
        rule.setContent {
            GenreRevealOverlay(
                movie = movie,
                onFinished = { closed = true },
                actions = com.example.ui.components.RevealActions(
                    canVeto = true,
                    onVeto = { vetoed = true },
                    onWatchLater = {},
                    onShare = {},
                    onClose = { closed = true }
                )
            )
        }
        rule.mainClock.advanceTimeBy(8000L)
        rule.waitForIdle()
        assertTrue("Sonuç ekranı kendiliğinden kapanmamalı", !closed)
        rule.onNodeWithTag("genre_reveal_overlay").assertIsDisplayed()
        rule.onNodeWithTag("reveal_veto_button").performScrollTo().performClick()
        rule.mainClock.advanceTimeBy(600L)
        rule.onNodeWithTag("reveal_veto_stamp", useUnmergedTree = true).assertExists()
        rule.mainClock.advanceTimeBy(1500L)
        rule.waitForIdle()
        assertTrue("Mühür bitince veto uygulanmalı", vetoed)
    }

    @Test
    fun resultScreen_watchLaterStampThenCallback() {
        val movie = HighlightsRepository.worstMovies().first()
        var watch = false
        rule.setContent {
            GenreRevealOverlay(
                movie = movie,
                onFinished = {},
                actions = com.example.ui.components.RevealActions(
                    canVeto = false,
                    onVeto = {},
                    onWatchLater = { watch = true },
                    onShare = {},
                    onClose = {}
                )
            )
        }
        rule.mainClock.advanceTimeBy(5000L)
        rule.waitForIdle()
        assertTrue(rule.onAllNodesWithTag("reveal_veto_button").fetchSemanticsNodes().isEmpty())
        rule.onNodeWithTag("reveal_share_button").assertExists()
        rule.onNodeWithTag("reveal_watch_button").performScrollTo().performClick()
        rule.mainClock.advanceTimeBy(2200L)
        rule.waitForIdle()
        assertTrue(watch)
    }
}

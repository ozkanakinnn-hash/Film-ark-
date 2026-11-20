package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertTextContains
import com.example.data.repository.HighlightsRepository
import com.example.data.repository.SmartWheelLists
import com.example.ui.components.GenreRevealOverlay
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
}

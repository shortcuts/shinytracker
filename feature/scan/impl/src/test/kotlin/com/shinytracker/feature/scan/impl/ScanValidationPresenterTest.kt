package com.shinytracker.feature.scan.impl

import android.graphics.Bitmap
import com.shinytracker.core.data.CaughtRepository
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.MatchResult
import com.shinytracker.core.testing.FakeCaughtDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScanValidationPresenterTest {
    private val fakeDao = FakeCaughtDao()
    private val caughtRepository = CaughtRepository(fakeDao)
    private val presenter = ScanValidationPresenter(caughtRepository)

    private fun review(name: String) =
        PendingReview(
            crop = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
            candidates = listOf(MatchResult(DexEntry(1, 0, 0, name), shiny = true, confidence = 0.5f)),
        )

    @Test
    fun `confirm records the chosen match and removes the entry`() =
        runTest {
            val entry = review("Pikachu")
            presenter.show(listOf(entry))

            presenter.confirm(0, entry.candidates.first())

            assertTrue(caughtRepository.observeCaught().first().any { it.dexEntry.name == "Pikachu" })
            assertEquals(emptyList<PendingReview>(), presenter.entries.value)
        }

    @Test
    fun `reject removes the entry without recording`() =
        runTest {
            presenter.show(listOf(review("Eevee")))

            presenter.reject(0)

            assertTrue(caughtRepository.observeCaught().first().isEmpty())
            assertEquals(emptyList<PendingReview>(), presenter.entries.value)
        }

    @Test
    fun `clear empties the list`() =
        runTest {
            presenter.show(listOf(review("Bulbasaur"), review("Charmander")))

            presenter.clear()

            assertEquals(emptyList<PendingReview>(), presenter.entries.value)
        }
}

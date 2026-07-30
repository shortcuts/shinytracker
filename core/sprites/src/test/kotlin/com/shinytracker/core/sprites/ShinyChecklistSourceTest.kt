package com.shinytracker.core.sprites

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShinyChecklistSourceTest {
    private val source = ShinyChecklistSource(ApplicationProvider.getApplicationContext<Context>())

    @Test
    fun `observeChecklist parses the bundled checklist asset`() =
        runTest {
            val checklist = source.observeChecklist().first()

            assertTrue(checklist.isNotEmpty())
            assertEquals("Bulbasaur", source.nameFor(1))
        }

    @Test
    fun `refresh failure leaves the previously loaded checklist untouched`() =
        runTest {
            val before = source.observeChecklist().first()

            val result = source.refreshFrom("http://127.0.0.1:1/unreachable")

            assertTrue(result.isFailure)
            assertEquals(before, source.observeChecklist().first())
        }
}
